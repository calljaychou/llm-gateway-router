package com.llm.gateway.service

import com.llm.gateway.common.enums.NormalStatus
import com.llm.gateway.common.exceptions.BizException
import com.llm.gateway.common.logger
import com.llm.gateway.dal.mapper.ApiKeysDynamicSqlSupport
import com.llm.gateway.dal.mapper.ApiKeysMapper
import com.llm.gateway.dal.mapper.insert
import com.llm.gateway.dal.mapper.select
import com.llm.gateway.dal.mapper.selectOne
import com.llm.gateway.dal.mapper.updateByPrimaryKeySelective
import com.llm.gateway.dal.model.ApiKeysRecord
import com.llm.gateway.model.params.ApiKeyCreateParams
import com.llm.gateway.model.results.ApiKeyCreateResult
import com.llm.gateway.model.results.ApiKeyListItemResult
import com.llm.gateway.model.results.ApiKeyListResult
import com.llm.gateway.model.results.ApiKeyRevokeResult
import com.llm.gateway.security.CustomUserDetails
import com.llm.gateway.security.CustomUserDetailsService
import java.security.MessageDigest
import java.security.SecureRandom
import java.util.Calendar
import java.util.Date
import org.springframework.beans.factory.annotation.Value
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class VirtualApiKeyService(
    private val apiKeysMapper: ApiKeysMapper,
    private val customUserDetailsService: CustomUserDetailsService,
    @Value("\${gateway.api-key-expires-day:30}") private val apiKeyExpiresDay: Int,
) {
    companion object {
        private const val VIRTUAL_KEY_PREFIX = "sk-vkey-"
        private const val RANDOM_PART_LENGTH = 32
        private const val REVOKED_STATUS = 0
        private val RANDOM = SecureRandom()
        private const val ALPHABET = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789"
    }

    @Transactional(rollbackFor = [Exception::class])
    fun createApiKey(userId: Long, params: ApiKeyCreateParams): ApiKeyCreateResult {
        val normalizedName = params.name.trim()
        val existed = apiKeysMapper.selectOne {
            where { ApiKeysDynamicSqlSupport.ApiKeys.userId isEqualTo userId }
            and { ApiKeysDynamicSqlSupport.ApiKeys.name isEqualTo normalizedName }
            and { ApiKeysDynamicSqlSupport.ApiKeys.status isEqualTo NormalStatus }
        }
        if (existed != null) throw BizException(BizException.BUSINESS_FAILED, "密钥名称已存在")

        // 最多 3 次，避免用户一次失败就报错
        repeat(3) {
            val rawKey = generateVirtualKey()
            val now = Date()
            val record = ApiKeysRecord(
                userId = userId,
                name = normalizedName,
                apiKeyHash = hashApiKey(rawKey),
                apiKeyPrefix = buildDisplayPrefix(rawKey),
                status = NormalStatus,
                expiresAt = Calendar.getInstance().also { it.add(Calendar.DATE, apiKeyExpiresDay) }.time,
                createdTime = now,
                updatedTime = now,
            )

            try {
                apiKeysMapper.insert(record)
                return ApiKeyCreateResult(
                    keyId = record.id!!,
                    name = record.name ?: normalizedName,
                    apiKey = rawKey,
                    keyPrefix = record.apiKeyPrefix ?: "",
                ).also {
                    logger().info("Created api key ${it.apiKey}")
                }
            } catch (e: DataIntegrityViolationException) {
                if (!isDuplicateKeyError(e)) {
                    throw BizException(BizException.SYSTEM_FAILED, "创建虚拟密钥失败")
                }
            }
        }

        throw BizException(BizException.SYSTEM_FAILED, "创建虚拟密钥失败，请重试")
    }

    fun listApiKeys(userId: Long): ApiKeyListResult {
        val records = apiKeysMapper.select {
            where { ApiKeysDynamicSqlSupport.ApiKeys.userId isEqualTo userId }
            and { ApiKeysDynamicSqlSupport.ApiKeys.status isEqualTo NormalStatus }
        }.sortedByDescending { it.id ?: 0L }

        val items = records.mapNotNull { record ->
            val keyId = record.id ?: return@mapNotNull null
            ApiKeyListItemResult(
                keyId = keyId,
                name = record.name ?: "",
                keyPrefix = record.apiKeyPrefix ?: "",
                status = record.status ?: REVOKED_STATUS,
                expiresAt = record.expiresAt,
            )
        }
        return ApiKeyListResult(keys = items)
    }

    @Transactional(rollbackFor = [Exception::class])
    fun revokeApiKey(userId: Long, keyId: Long): ApiKeyRevokeResult {
        val record = apiKeysMapper.selectOne {
            where { ApiKeysDynamicSqlSupport.ApiKeys.id isEqualTo keyId }
            and { ApiKeysDynamicSqlSupport.ApiKeys.userId isEqualTo userId }
        } ?: throw BizException(BizException.BUSINESS_FAILED, "密钥不存在")

        if (record.status != REVOKED_STATUS) {
            record.status = REVOKED_STATUS
            record.updatedTime = Date()
            apiKeysMapper.updateByPrimaryKeySelective(record)
        }

        return ApiKeyRevokeResult(
            keyId = keyId,
            revoked = true,
        )
    }

    fun authenticateByVirtualKey(apiKey: String): CustomUserDetails? {
        if (!apiKey.startsWith(VIRTUAL_KEY_PREFIX)) {
            return null
        }

        val hash = hashApiKey(apiKey)
        val keyRecord = apiKeysMapper.selectOne {
            where { ApiKeysDynamicSqlSupport.ApiKeys.apiKeyHash isEqualTo hash }
            and { ApiKeysDynamicSqlSupport.ApiKeys.status isEqualTo NormalStatus }
        } ?: return null

        val userId = keyRecord.userId ?: return null
        return runCatching {
            customUserDetailsService.loadUserById(userId, emptyList())
        }.getOrNull()
    }

    private fun generateVirtualKey(): String {
        val randomPart = buildString(RANDOM_PART_LENGTH) {
            repeat(RANDOM_PART_LENGTH) {
                append(ALPHABET[RANDOM.nextInt(ALPHABET.length)])
            }
        }
        return VIRTUAL_KEY_PREFIX + randomPart
    }

    private fun buildDisplayPrefix(apiKey: String): String {
        val value = apiKey.trim()
        if (value.isEmpty()) {
            return value
        }
        val tail = if (value.length >= 4) value.takeLast(4) else value
        return "${VIRTUAL_KEY_PREFIX}***${tail}"
    }

    private fun hashApiKey(apiKey: String): String {
        val digest = MessageDigest.getInstance("SHA-256").digest(apiKey.toByteArray())
        return digest.joinToString(separator = "") { "%02x".format(it) }
    }

    private fun isDuplicateKeyError(e: DataIntegrityViolationException): Boolean {
        val message = e.message.orEmpty().lowercase()
        return message.contains("duplicate") || message.contains("unique")
    }
}
