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
import java.math.BigDecimal
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
        private val MIN_RESERVE_AMOUNT = BigDecimal("0.000001")
        private val ZERO_AMOUNT = BigDecimal.ZERO
        private const val QUOTA_HTTP_CODE = 402
    }

    @Transactional(rollbackFor = [Exception::class])
    fun reserve(userId: Long, requestId: String, estimatedAmount: BigDecimal): UserQuotaReservationDto {
        val reserveAmount = estimatedAmount.max(MIN_RESERVE_AMOUNT)
        val accountBefore = requireAccount(userId)
        if ((accountBefore.availableAmount ?: ZERO_AMOUNT) < reserveAmount) {
            throw BizException(QUOTA_HTTP_CODE, "用户剩余额度不足")
        }

        val splits = deductFromActiveGrants(userId, reserveAmount)
        val accountAfter = rebuildAccount(userId)
        splits.forEach { split ->
            insertTransaction(
                userId = userId,
                grantId = split.grantId,
                changeType = UserQuotaTransactionChangeType.USAGE_RESERVE,
                deltaAmount = split.amount.negate(),
                before = accountBefore,
                after = accountAfter,
                requestId = requestId,
                remark = "请求预占额度",
            )
        }
        return UserQuotaReservationDto(
            requestId = requestId,
            userId = userId,
            reservedAmount = reserveAmount,
            splits = splits,
        )
    }

    @Transactional(rollbackFor = [Exception::class])
    fun settle(reservation: UserQuotaReservationDto, actualAmount: BigDecimal): UserQuotaUsageSettleDto {
        val normalizedActualAmount = actualAmount.max(ZERO_AMOUNT)
        return when {
            // 多退
            normalizedActualAmount < reservation.reservedAmount -> {
                val refundAmount = reservation.reservedAmount - normalizedActualAmount
                refund(reservation, refundAmount)
                UserQuotaUsageSettleDto(
                    requestId = reservation.requestId,
                    settled = true,
                    actualAmount = normalizedActualAmount,
                    refundedAmount = refundAmount,
                    extraDeductedAmount = ZERO_AMOUNT,
                )
            }
            // 少补
            normalizedActualAmount > reservation.reservedAmount -> {
                val extraAmount = normalizedActualAmount - reservation.reservedAmount
                val before = requireAccount(reservation.userId)
                // 不够扣减了
                if ((before.availableAmount ?: ZERO_AMOUNT) < extraAmount) {
                    insertTransaction(
                        userId = reservation.userId,
                        grantId = null,
                        changeType = UserQuotaTransactionChangeType.USAGE_SETTLE,
                        deltaAmount = ZERO_AMOUNT,
                        before = before,
                        after = before,
                        requestId = reservation.requestId,
                        remark = "实际用量超过预占额度，追加扣减失败",
                    )
                    return UserQuotaUsageSettleDto(
                        requestId = reservation.requestId,
                        settled = false,
                        actualAmount = normalizedActualAmount,
                        refundedAmount = ZERO_AMOUNT,
                        extraDeductedAmount = ZERO_AMOUNT,
                    )
                }
                val splits = deductFromActiveGrants(reservation.userId, extraAmount)
                val after = rebuildAccount(reservation.userId)
                splits.forEach { split ->
                    insertTransaction(
                        userId = reservation.userId,
                        grantId = split.grantId,
                        changeType = UserQuotaTransactionChangeType.USAGE_SETTLE,
                        deltaAmount = split.amount.negate(),
                        before = before,
                        after = after,
                        requestId = reservation.requestId,
                        remark = "实际用量超过预占额度，追加扣减",
                    )
                }
                UserQuotaUsageSettleDto(
                    requestId = reservation.requestId,
                    settled = true,
                    actualAmount = normalizedActualAmount,
                    refundedAmount = ZERO_AMOUNT,
                    extraDeductedAmount = extraAmount,
                )
            }

            else -> {
                val account = requireAccount(reservation.userId)
                insertTransaction(
                    userId = reservation.userId,
                    grantId = null,
                    changeType = UserQuotaTransactionChangeType.USAGE_SETTLE,
                    deltaAmount = ZERO_AMOUNT,
                    before = account,
                    after = account,
                    requestId = reservation.requestId,
                    remark = "实际用量等于预占额度",
                )
                UserQuotaUsageSettleDto(
                    requestId = reservation.requestId,
                    settled = true,
                    actualAmount = normalizedActualAmount,
                    refundedAmount = ZERO_AMOUNT,
                    extraDeductedAmount = ZERO_AMOUNT,
                )
            }
        }
    }

    @Transactional(rollbackFor = [Exception::class])
    fun refundAll(reservation: UserQuotaReservationDto, remark: String = "请求失败退回预占额度") {
        refund(reservation, reservation.reservedAmount, remark)
    }

    private fun refund(
        reservation: UserQuotaReservationDto,
        refundAmount: BigDecimal,
        remark: String = "实际用量小于预占额度，退回差额",
    ) {
        if (refundAmount <= ZERO_AMOUNT) return
        var remaining = refundAmount
        reservation.splits.asReversed().forEach { split ->
            if (remaining <= ZERO_AMOUNT) return@forEach
            val refund = split.amount.min(remaining)
            val before = requireAccount(reservation.userId)
            val grant = userQuotaGrantsMapper.selectOne {
                where { UserQuotaGrantsDynamicSqlSupport.UserQuotaGrants.id isEqualTo split.grantId }
            } ?: throw BizException(BizException.BUSINESS_FAILED, "配额批次不存在")
            grant.remainingAmount = (grant.remainingAmount ?: ZERO_AMOUNT) + refund
            grant.consumedAmount = ((grant.consumedAmount ?: ZERO_AMOUNT) - refund).max(ZERO_AMOUNT)
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
                deltaAmount = refund,
                before = before,
                after = after,
                requestId = reservation.requestId,
                remark = remark,
            )
            remaining = remaining - refund
        }
    }

    private fun requireAccount(userId: Long): UserQuotaAccountsRecord {
        return userQuotaAccountsMapper.selectOne {
            where { UserQuotaAccountsDynamicSqlSupport.UserQuotaAccounts.userId isEqualTo userId }
        } ?: throw BizException(BizException.BUSINESS_FAILED, "用户未配置额度账户")
    }

    private fun deductFromActiveGrants(userId: Long, amount: BigDecimal): List<UserQuotaReserveSplitDto> {
        val grants = userQuotaGrantsMapper.select {
            where { UserQuotaGrantsDynamicSqlSupport.UserQuotaGrants.userId isEqualTo userId }
            and { UserQuotaGrantsDynamicSqlSupport.UserQuotaGrants.status isEqualTo UserQuotaGrantStatus.ACTIVE.value }
            and { UserQuotaGrantsDynamicSqlSupport.UserQuotaGrants.remainingAmount isGreaterThan ZERO_AMOUNT }
            and { UserQuotaGrantsDynamicSqlSupport.UserQuotaGrants.expiresAt isGreaterThan Date() }
            orderBy(
                UserQuotaGrantsDynamicSqlSupport.UserQuotaGrants.expiresAt,
                UserQuotaGrantsDynamicSqlSupport.UserQuotaGrants.id
            )
        }
        var remaining = amount
        val splits = mutableListOf<UserQuotaReserveSplitDto>()
        // 按过期时间排序依次扣减
        grants.forEach { grant ->
            if (remaining <= ZERO_AMOUNT) return@forEach
            val grantId = grant.id ?: throw BizException(BizException.BUSINESS_FAILED, "配额批次数据异常")
            val deduct = (grant.remainingAmount ?: ZERO_AMOUNT).min(remaining)
            if (deduct <= ZERO_AMOUNT) return@forEach
            logger().info(
                "用户配额扣减,before,remainingAmount:{},consumedAmount:{},status:{}",
                grant.remainingAmount, grant.consumedAmount, grant.status
            )
            grant.remainingAmount = (grant.remainingAmount ?: ZERO_AMOUNT) - deduct
            grant.consumedAmount = (grant.consumedAmount ?: ZERO_AMOUNT) + deduct
            grant.status =
                if ((grant.remainingAmount ?: ZERO_AMOUNT).compareTo(ZERO_AMOUNT) == 0) {
                    UserQuotaGrantStatus.DEPLETED.value
                } else {
                    UserQuotaGrantStatus.ACTIVE.value
                }
            grant.updatedTime = Date()
            logger().info(
                "用户配额扣减,after,remainingAmount:{},consumedAmount:{},status:{}",
                grant.remainingAmount, grant.consumedAmount, grant.status
            )
            userQuotaGrantsMapper.updateByPrimaryKeySelective(grant)
            splits.add(UserQuotaReserveSplitDto(grantId = grantId, amount = deduct))
            remaining = remaining - deduct
        }
        if (remaining > ZERO_AMOUNT) {
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
            it.status == UserQuotaGrantStatus.ACTIVE.value && (it.remainingAmount ?: ZERO_AMOUNT) > ZERO_AMOUNT && it.expiresAt?.after(now) == true
        }
        account.availableAmount = activeGrants.fold(ZERO_AMOUNT) { total, it -> total + (it.remainingAmount ?: ZERO_AMOUNT) }
        account.currentQuotaAmount = notExpiredGrants.fold(ZERO_AMOUNT) { total, it -> total + (it.remainingAmount ?: ZERO_AMOUNT) + (it.consumedAmount ?: ZERO_AMOUNT) }
        account.usedAmount = notExpiredGrants.fold(ZERO_AMOUNT) { total, it -> total + (it.consumedAmount ?: ZERO_AMOUNT) }
        account.earliestExpireAt = activeGrants.mapNotNull { it.expiresAt }.minOrNull()
        account.updatedTime = Date()
        userQuotaAccountsMapper.updateByPrimaryKeySelective(account)
        return account
    }

    private fun insertTransaction(
        userId: Long,
        grantId: Long?,
        changeType: UserQuotaTransactionChangeType,
        deltaAmount: BigDecimal,
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
                deltaAmount = deltaAmount,
                quotaBeforeAmount = before.currentQuotaAmount ?: ZERO_AMOUNT,
                quotaAfterAmount = after.currentQuotaAmount ?: ZERO_AMOUNT,
                availableBeforeAmount = before.availableAmount ?: ZERO_AMOUNT,
                availableAfterAmount = after.availableAmount ?: ZERO_AMOUNT,
                requestId = requestId,
                remark = remark,
                createdTime = Date(),
            )
        )
    }
}
