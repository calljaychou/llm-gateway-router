package com.llm.gateway.service

import com.llm.gateway.common.enums.UserQuotaGrantStatus
import com.llm.gateway.common.enums.UserQuotaTransactionChangeType
import com.llm.gateway.common.exceptions.BizException
import com.llm.gateway.common.logger
import com.llm.gateway.dal.mapper.UserQuotaAccountsDynamicSqlSupport
import com.llm.gateway.dal.mapper.UserQuotaAccountsMapper
import com.llm.gateway.dal.mapper.UserQuotaGrantsDynamicSqlSupport
import com.llm.gateway.dal.mapper.UserQuotaGrantsMapper
import com.llm.gateway.dal.mapper.UserQuotaTransactionsMapper
import com.llm.gateway.dal.mapper.insertSelective
import com.llm.gateway.dal.mapper.select
import com.llm.gateway.dal.mapper.selectOne
import com.llm.gateway.dal.mapper.updateByPrimaryKeySelective
import com.llm.gateway.dal.model.UserQuotaAccountsRecord
import com.llm.gateway.dal.model.UserQuotaGrantsRecord
import com.llm.gateway.dal.model.UserQuotaTransactionsRecord
import com.llm.gateway.model.dto.UserQuotaReservationDto
import com.llm.gateway.model.dto.UserQuotaReserveSplitDto
import com.llm.gateway.model.dto.UserQuotaUsageSettleDto
import java.util.Date
import java.util.UUID
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class UserQuotaUsageService(
    private val userQuotaAccountsMapper: UserQuotaAccountsMapper,
    private val userQuotaGrantsMapper: UserQuotaGrantsMapper,
    private val userQuotaTransactionsMapper: UserQuotaTransactionsMapper,
) {
    companion object {
        private const val MIN_RESERVE_TOKENS = 1L
        private const val QUOTA_HTTP_CODE = 402
    }

    @Transactional(rollbackFor = [Exception::class])
    fun reserve(userId: Long, requestId: String, estimatedTokens: Long): UserQuotaReservationDto {
        val reserveTokens = estimatedTokens.coerceAtLeast(MIN_RESERVE_TOKENS)
        val accountBefore = requireAccount(userId)
        if ((accountBefore.availableTokens ?: 0L) < reserveTokens) {
            throw BizException(QUOTA_HTTP_CODE, "用户剩余额度不足")
        }

        val splits = deductFromActiveGrants(userId, reserveTokens)
        val accountAfter = rebuildAccount(userId)
        splits.forEach { split ->
            insertTransaction(
                userId = userId,
                grantId = split.grantId,
                changeType = UserQuotaTransactionChangeType.USAGE_RESERVE,
                deltaTokens = -split.tokens,
                before = accountBefore,
                after = accountAfter,
                requestId = requestId,
                remark = "请求预占额度",
            )
        }
        return UserQuotaReservationDto(
            requestId = requestId,
            userId = userId,
            reservedTokens = reserveTokens,
            splits = splits,
        )
    }

    @Transactional(rollbackFor = [Exception::class])
    fun settle(reservation: UserQuotaReservationDto, actualTokens: Long): UserQuotaUsageSettleDto {
        val normalizedActualTokens = actualTokens.coerceAtLeast(0L)
        return when {
            // 多退
            normalizedActualTokens < reservation.reservedTokens -> {
                val refundTokens = reservation.reservedTokens - normalizedActualTokens
                refund(reservation, refundTokens)
                UserQuotaUsageSettleDto(
                    requestId = reservation.requestId,
                    settled = true,
                    actualTokens = normalizedActualTokens,
                    refundedTokens = refundTokens,
                    extraDeductedTokens = 0,
                )
            }
            // 少补
            normalizedActualTokens > reservation.reservedTokens -> {
                val extraTokens = normalizedActualTokens - reservation.reservedTokens
                val before = requireAccount(reservation.userId)
                // 不够扣减了
                if ((before.availableTokens ?: 0L) < extraTokens) {
                    insertTransaction(
                        userId = reservation.userId,
                        grantId = null,
                        changeType = UserQuotaTransactionChangeType.USAGE_SETTLE,
                        deltaTokens = 0,
                        before = before,
                        after = before,
                        requestId = reservation.requestId,
                        remark = "实际用量超过预占额度，追加扣减失败",
                    )
                    return UserQuotaUsageSettleDto(
                        requestId = reservation.requestId,
                        settled = false,
                        actualTokens = normalizedActualTokens,
                        refundedTokens = 0,
                        extraDeductedTokens = 0,
                    )
                }
                val splits = deductFromActiveGrants(reservation.userId, extraTokens)
                val after = rebuildAccount(reservation.userId)
                splits.forEach { split ->
                    insertTransaction(
                        userId = reservation.userId,
                        grantId = split.grantId,
                        changeType = UserQuotaTransactionChangeType.USAGE_SETTLE,
                        deltaTokens = -split.tokens,
                        before = before,
                        after = after,
                        requestId = reservation.requestId,
                        remark = "实际用量超过预占额度，追加扣减",
                    )
                }
                UserQuotaUsageSettleDto(
                    requestId = reservation.requestId,
                    settled = true,
                    actualTokens = normalizedActualTokens,
                    refundedTokens = 0,
                    extraDeductedTokens = extraTokens,
                )
            }

            else -> {
                val account = requireAccount(reservation.userId)
                insertTransaction(
                    userId = reservation.userId,
                    grantId = null,
                    changeType = UserQuotaTransactionChangeType.USAGE_SETTLE,
                    deltaTokens = 0,
                    before = account,
                    after = account,
                    requestId = reservation.requestId,
                    remark = "实际用量等于预占额度",
                )
                UserQuotaUsageSettleDto(
                    requestId = reservation.requestId,
                    settled = true,
                    actualTokens = normalizedActualTokens,
                    refundedTokens = 0,
                    extraDeductedTokens = 0,
                )
            }
        }
    }

    @Transactional(rollbackFor = [Exception::class])
    fun refundAll(reservation: UserQuotaReservationDto, remark: String = "请求失败退回预占额度") {
        refund(reservation, reservation.reservedTokens, remark)
    }

    private fun refund(
        reservation: UserQuotaReservationDto,
        refundTokens: Long,
        remark: String = "实际用量小于预占额度，退回差额",
    ) {
        if (refundTokens <= 0) return
        var remaining = refundTokens
        reservation.splits.asReversed().forEach { split ->
            if (remaining <= 0) return@forEach
            val refund = minOf(split.tokens, remaining)
            val before = requireAccount(reservation.userId)
            val grant = userQuotaGrantsMapper.selectOne {
                where { UserQuotaGrantsDynamicSqlSupport.UserQuotaGrants.id isEqualTo split.grantId }
            } ?: throw BizException(BizException.BUSINESS_FAILED, "配额批次不存在")
            grant.remainingTokens = (grant.remainingTokens ?: 0L) + refund
            grant.consumedTokens = ((grant.consumedTokens ?: 0L) - refund).coerceAtLeast(0L)
            if (grant.expiresAt?.after(Date()) == true) {
                grant.status = UserQuotaGrantStatus.ACTIVE.value
            }
            grant.updatedTime = Date()
            userQuotaGrantsMapper.updateByPrimaryKeySelective(grant)
            val after = rebuildAccount(reservation.userId)
            insertTransaction(
                userId = reservation.userId,
                grantId = split.grantId,
                changeType = UserQuotaTransactionChangeType.USAGE_REFUND,
                deltaTokens = refund,
                before = before,
                after = after,
                requestId = reservation.requestId,
                remark = remark,
            )
            remaining -= refund
        }
    }

    private fun requireAccount(userId: Long): UserQuotaAccountsRecord {
        return userQuotaAccountsMapper.selectOne {
            where { UserQuotaAccountsDynamicSqlSupport.UserQuotaAccounts.userId isEqualTo userId }
        } ?: throw BizException(BizException.BUSINESS_FAILED, "用户未配置额度账户")
    }

    private fun deductFromActiveGrants(userId: Long, tokens: Long): List<UserQuotaReserveSplitDto> {
        val grants = userQuotaGrantsMapper.select {
            where { UserQuotaGrantsDynamicSqlSupport.UserQuotaGrants.userId isEqualTo userId }
            and { UserQuotaGrantsDynamicSqlSupport.UserQuotaGrants.status isEqualTo UserQuotaGrantStatus.ACTIVE.value }
            and { UserQuotaGrantsDynamicSqlSupport.UserQuotaGrants.remainingTokens isGreaterThan 0L }
            and { UserQuotaGrantsDynamicSqlSupport.UserQuotaGrants.expiresAt isGreaterThan Date() }
            orderBy(
                UserQuotaGrantsDynamicSqlSupport.UserQuotaGrants.expiresAt,
                UserQuotaGrantsDynamicSqlSupport.UserQuotaGrants.id
            )
        }
        var remaining = tokens
        val splits = mutableListOf<UserQuotaReserveSplitDto>()
        // 按过期时间排序依次扣减
        grants.forEach { grant ->
            if (remaining <= 0) return@forEach
            val grantId = grant.id ?: throw BizException(BizException.BUSINESS_FAILED, "配额批次数据异常")
            val deduct = minOf(grant.remainingTokens ?: 0L, remaining)
            if (deduct <= 0) return@forEach
            logger().info(
                "用户配额扣减,before,remainingTokens:{},consumedTokens:{},status:{}",
                grant.remainingTokens, grant.consumedTokens, grant.status
            )
            grant.remainingTokens = (grant.remainingTokens ?: 0L) - deduct
            grant.consumedTokens = (grant.consumedTokens ?: 0L) + deduct
            grant.status =
                if (grant.remainingTokens == 0L) UserQuotaGrantStatus.DEPLETED.value else UserQuotaGrantStatus.ACTIVE.value
            grant.updatedTime = Date()
            logger().info(
                "用户配额扣减,after,remainingTokens:{},consumedTokens:{},status:{}",
                grant.remainingTokens, grant.consumedTokens, grant.status
            )
            userQuotaGrantsMapper.updateByPrimaryKeySelective(grant)
            splits += UserQuotaReserveSplitDto(grantId = grantId, tokens = deduct)
            remaining -= deduct
        }
        if (remaining > 0) {
            throw BizException(QUOTA_HTTP_CODE, "用户剩余额度不足")
        }
        return splits
    }

    private fun rebuildAccount(userId: Long): UserQuotaAccountsRecord {
        val account = requireAccount(userId)
        val grants = userQuotaGrantsMapper.select {
            where { UserQuotaGrantsDynamicSqlSupport.UserQuotaGrants.userId isEqualTo userId }
        }
        val now = Date()
        val notExpiredGrants = grants.filter {
            it.expiresAt?.after(now) == true && it.status in setOf(
                UserQuotaGrantStatus.ACTIVE.value,
                UserQuotaGrantStatus.DEPLETED.value
            )
        }
        val activeGrants = grants.filter {
            it.status == UserQuotaGrantStatus.ACTIVE.value && (it.remainingTokens
                ?: 0L) > 0 && it.expiresAt?.after(now) == true
        }
        account.availableTokens = activeGrants.sumOf { it.remainingTokens ?: 0L }
        account.currentQuotaTokens = notExpiredGrants.sumOf { (it.remainingTokens ?: 0L) + (it.consumedTokens ?: 0L) }
        account.usedTokens = notExpiredGrants.sumOf { it.consumedTokens ?: 0L }
        account.earliestExpireAt = activeGrants.mapNotNull { it.expiresAt }.minOrNull()
        account.updatedTime = Date()
        userQuotaAccountsMapper.updateByPrimaryKeySelective(account)
        return account
    }

    private fun insertTransaction(
        userId: Long,
        grantId: Long?,
        changeType: UserQuotaTransactionChangeType,
        deltaTokens: Long,
        before: UserQuotaAccountsRecord,
        after: UserQuotaAccountsRecord,
        requestId: String,
        remark: String,
    ) {
        userQuotaTransactionsMapper.insertSelective(
            UserQuotaTransactionsRecord(
                bizNo = "quota-${UUID.randomUUID()}",
                userId = userId,
                grantId = grantId,
                changeType = changeType.value,
                deltaTokens = deltaTokens,
                quotaBefore = before.currentQuotaTokens ?: 0L,
                quotaAfter = after.currentQuotaTokens ?: 0L,
                availableBefore = before.availableTokens ?: 0L,
                availableAfter = after.availableTokens ?: 0L,
                requestId = requestId,
                remark = remark,
                createdTime = Date(),
            )
        )
    }
}
