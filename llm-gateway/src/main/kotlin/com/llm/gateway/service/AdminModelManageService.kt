package com.llm.gateway.service

import com.llm.gateway.common.enums.BillingType
import com.llm.gateway.common.enums.NormalStatus
import com.llm.gateway.common.exceptions.BizException
import com.llm.gateway.dal.mapper.MasterKeysMapper
import com.llm.gateway.dal.mapper.ModelsDynamicSqlSupport
import com.llm.gateway.dal.mapper.ModelsMapper
import com.llm.gateway.dal.mapper.VendorsDynamicSqlSupport
import com.llm.gateway.dal.mapper.VendorsMapper
import com.llm.gateway.dal.mapper.insert
import com.llm.gateway.dal.mapper.selectOne
import com.llm.gateway.dal.model.MasterKeysRecord
import com.llm.gateway.dal.model.ModelsRecord
import com.llm.gateway.dal.model.VendorsRecord
import com.llm.gateway.model.params.MasterKeyCreateParams
import com.llm.gateway.model.params.ModelCreateParams
import com.llm.gateway.model.params.VendorCreateParams
import com.llm.gateway.model.results.MasterKeyCreateResult
import com.llm.gateway.model.results.ModelCreateResult
import com.llm.gateway.model.results.VendorCreateResult
import java.net.URI
import java.nio.charset.StandardCharsets
import java.security.MessageDigest
import java.security.SecureRandom
import java.util.Base64
import java.util.Date
import javax.annotation.PostConstruct
import javax.crypto.Cipher
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.SecretKeySpec
import org.springframework.beans.factory.annotation.Value
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class AdminModelManageService(
    private val vendorsMapper: VendorsMapper,
    private val masterKeysMapper: MasterKeysMapper,
    private val modelsMapper: ModelsMapper,
    @Value("\${gateway.aes-secret}") private val aesSecret: String,
) {

    @PostConstruct
    fun validateAesSecret() {
        if (aesSecret.isBlank() || aesSecret.length < 32) {
            throw IllegalStateException("gateway.aes-secret 未配置或长度不足(至少32字符)")
        }
    }

    @Transactional(rollbackFor = [Exception::class])
    fun createVendor(params: VendorCreateParams): VendorCreateResult {
        val existed = vendorsMapper.selectOne {
            where { VendorsDynamicSqlSupport.Vendors.name isEqualTo params.name.trim() }
        }
        if (existed != null) {
            throw BizException(BizException.BUSINESS_FAILED, "供应商名称已存在")
        }

        val now = Date()
        val normalizedBaseUrl = params.baseUrl.trim().trimEnd('/')
        if (!isValidBaseUrl(normalizedBaseUrl)) {
            throw BizException(BizException.BUSINESS_FAILED, "供应商Base URL不合法")
        }
        val record = VendorsRecord(
            name = params.name.trim(),
            baseUrl = normalizedBaseUrl,
            status = params.status ?: NormalStatus,
            createdTime = now,
            updatedTime = now,
        )
        try {
            vendorsMapper.insert(record)
        } catch (e: DataIntegrityViolationException) {
            throw mapDataIntegrityException(e, "供应商名称或URL重复")
        }
        val vendorId = record.id ?: throw BizException(BizException.SYSTEM_FAILED, "创建供应商失败")

        return VendorCreateResult(
            vendorId = vendorId,
            name = record.name!!,
            baseUrl = record.baseUrl!!,
            status = record.status!!,
        )
    }

    @Transactional(rollbackFor = [Exception::class])
    fun createMasterKey(params: MasterKeyCreateParams): MasterKeyCreateResult {
        val vendorId = params.vendorId ?: throw BizException(BizException.BUSINESS_FAILED, "供应商ID不能为空")
        val vendor = vendorsMapper.selectOne {
            where { VendorsDynamicSqlSupport.Vendors.id isEqualTo vendorId }
        } ?: throw BizException(BizException.BUSINESS_FAILED, "供应商不存在")

        if (vendor.status != NormalStatus) {
            throw BizException(BizException.BUSINESS_FAILED, "供应商状态异常，无法新增主密钥")
        }

        val now = Date()
        val record = MasterKeysRecord(
            vendorId = vendorId,
            weight = params.weight ?: 10,
            status = params.status ?: NormalStatus,
            errorCount = 0,
            lastCheckedAt = null,
            createdTime = now,
            updatedTime = now,
            apiKeyEncrypted = encryptApiKey(params.apiKey.trim()),
        )
        try {
            masterKeysMapper.insert(record)
        } catch (e: DataIntegrityViolationException) {
            throw mapDataIntegrityException(e, "主密钥创建失败，请检查数据是否重复")
        }
        val masterKeyId = record.id ?: throw BizException(BizException.SYSTEM_FAILED, "创建主密钥失败")

        return MasterKeyCreateResult(
            masterKeyId = masterKeyId,
            vendorId = vendorId,
            status = record.status!!,
            weight = record.weight!!,
            keyPrefix = buildKeyPrefix(params.apiKey),
        )
    }

    @Transactional(rollbackFor = [Exception::class])
    fun createModel(params: ModelCreateParams): ModelCreateResult {
        val vendorId = params.vendorId ?: throw BizException(BizException.BUSINESS_FAILED, "供应商ID不能为空")
        val vendor = vendorsMapper.selectOne {
            where { VendorsDynamicSqlSupport.Vendors.id isEqualTo vendorId }
        } ?: throw BizException(BizException.BUSINESS_FAILED, "供应商不存在")

        if (vendor.status != NormalStatus) {
            throw BizException(BizException.BUSINESS_FAILED, "供应商状态异常，无法新增模型")
        }

        val alias = params.modelAlias.trim()
        val existedModel = modelsMapper.selectOne {
            where { ModelsDynamicSqlSupport.Models.modelAlias isEqualTo alias }
        }
        if (existedModel != null) {
            throw BizException(BizException.BUSINESS_FAILED, "模型别名已存在")
        }

        val now = Date()
        val billingType = (params.billingType ?: BillingType.PAID).value

        val record = ModelsRecord(
            modelAlias = alias,
            realModelName = params.realModelName.trim(),
            vendorId = vendorId,
            billingType = billingType,
            active = params.active ?: true,
            createdTime = now,
            updatedTime = now,
        )
        try {
            modelsMapper.insert(record)
        } catch (e: DataIntegrityViolationException) {
            throw mapDataIntegrityException(e, "模型别名重复")
        }
        val modelId = record.id ?: throw BizException(BizException.SYSTEM_FAILED, "创建模型失败")

        return ModelCreateResult(
            modelId = modelId,
            modelAlias = record.modelAlias!!,
            realModelName = record.realModelName!!,
            vendorId = record.vendorId!!,
            billingType = record.billingType!!,
            active = record.active ?: true,
        )
    }

    private fun encryptApiKey(apiKey: String): String {
        if (apiKey.isBlank()) {
            throw BizException(BizException.BUSINESS_FAILED, "API Key不能为空")
        }
        val keySpec = buildAesKey()
        val iv = ByteArray(12).also { SecureRandom().nextBytes(it) }
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.ENCRYPT_MODE, keySpec, GCMParameterSpec(128, iv))
        val encryptedBytes = cipher.doFinal(apiKey.toByteArray(StandardCharsets.UTF_8))
        val ivEncoded = Base64.getEncoder().encodeToString(iv)
        val dataEncoded = Base64.getEncoder().encodeToString(encryptedBytes)
        return "$ivEncoded:$dataEncoded"
    }

    private fun buildAesKey(): SecretKeySpec {
        val digest = MessageDigest.getInstance("SHA-256")
            .digest(aesSecret.toByteArray(StandardCharsets.UTF_8))
        return SecretKeySpec(digest, "AES")
    }

    private fun buildKeyPrefix(apiKey: String): String {
        val value = apiKey.trim()
        if (value.length <= 8) {
            return "****"
        }
        return "${value.take(6)}****${value.takeLast(2)}"
    }

    private fun isValidBaseUrl(url: String): Boolean {
        if (url.isBlank()) return false
        return try {
            val uri = URI(url)
            val validScheme = uri.scheme == "http" || uri.scheme == "https"
            validScheme && !uri.host.isNullOrBlank()
        } catch (_: Exception) {
            false
        }
    }

    private fun mapDataIntegrityException(e: DataIntegrityViolationException, duplicateMessage: String): BizException {
        val message = e.message.orEmpty().lowercase()
        return if (message.contains("duplicate") || message.contains("unique")) {
            BizException(BizException.BUSINESS_FAILED, duplicateMessage)
        } else {
            BizException(BizException.SYSTEM_FAILED, "数据写入失败，请检查输入内容")
        }
    }
}
