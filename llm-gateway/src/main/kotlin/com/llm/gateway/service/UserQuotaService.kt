package com.llm.gateway.service

import com.github.pagehelper.PageInfo
import com.github.pagehelper.page.PageMethod
import com.llm.gateway.common.enums.NormalStatus
import com.llm.gateway.common.enums.UserQuotaGrantSourceType
import com.llm.gateway.common.enums.UserQuotaGrantStatus
import com.llm.gateway.common.enums.UserQuotaTransactionChangeType
import com.llm.gateway.common.exceptions.BizException
import com.llm.gateway.dal.mapper.UserQuotaAccountsDynamicSqlSupport
import com.llm.gateway.dal.mapper.UserQuotaAccountsMapper
import com.llm.gateway.dal.mapper.UserQuotaGrantsDynamicSqlSupport
import com.llm.gateway.dal.mapper.UserQuotaGrantsMapper
import com.llm.gateway.dal.mapper.UserQuotaTransactionsDynamicSqlSupport
import com.llm.gateway.dal.mapper.UserQuotaTransactionsMapper
import com.llm.gateway.dal.mapper.UsersDynamicSqlSupport
import com.llm.gateway.dal.mapper.UsersMapper
import com.llm.gateway.dal.mapper.insert
import com.llm.gateway.dal.mapper.insertSelective
import com.llm.gateway.dal.mapper.select
import com.llm.gateway.dal.mapper.selectOne
import com.llm.gateway.dal.mapper.updateByPrimaryKeySelective
import com.llm.gateway.dal.model.UserQuotaAccountsRecord
import com.llm.gateway.dal.model.UserQuotaGrantsRecord
import com.llm.gateway.dal.model.UserQuotaTransactionsRecord
import com.llm.gateway.model.PageParams
import com.llm.gateway.model.params.AdminUserQuotaAdjustmentParams
import com.llm.gateway.model.params.UserQuotaTransactionsPageParams
import com.llm.gateway.model.params.UserQuotaTransferParams
import com.llm.gateway.model.PageResult
import com.llm.gateway.model.results.UserQuotaAccountResult
import com.llm.gateway.model.results.UserQuotaGrantResult
import com.llm.gateway.model.results.UserQuotaTransactionResult
import com.llm.gateway.model.results.UserQuotaTransferResult
import java.util.Date
import java.util.UUID
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class UserQuotaService(
    private val usersMapper: UsersMapper,
    private val userQuotaAccountsMapper: UserQuotaAccountsMapper,
    private val userQuotaGrantsMapper: UserQuotaGrantsMapper,
    private val userQuotaTransactionsMapper: UserQuotaTransactionsMapper,
) {
    companion object {
        private const val ACTIVE_GRANT_LIMIT = 5
    }

    fun getCurrentQuota(userId: Long): UserQuotaAccountResult {
        ensureActiveUser(userId)
        return mapAccountResult(requireAccount(userId), includeActiveGrants = true)
    }

    fun listExpiredGrants(userId: Long, params: PageParams): PageResult<UserQuotaGrantResult> {
        ensureActiveUser(userId)
        PageMethod.startPage<UserQuotaGrantsRecord>(params.pageNum, params.pageSize)
        val records = userQuotaGrantsMapper.select {
            where { UserQuotaGrantsDynamicSqlSupport.UserQuotaGrants.userId isEqualTo userId }
            and { UserQuotaGrantsDynamicSqlSupport.UserQuotaGrants.status isEqualTo UserQuotaGrantStatus.EXPIRED.value }
            orderBy(
                UserQuotaGrantsDynamicSqlSupport.UserQuotaGrants.expiresAt.descending(),
                UserQuotaGrantsDynamicSqlSupport.UserQuotaGrants.id.descending()
            )
        }
        val pageInfo = PageInfo.of(records)
        return PageResult(
            pageNum = pageInfo.pageNum,
            pageSize = pageInfo.pageSize,
            total = pageInfo.total,
            list = records.map { mapGrantResult(it) },
        )
    }

    fun listTransactions(userId: Long, params: UserQuotaTransactionsPageParams): PageResult<UserQuotaTransactionResult> {
        ensureActiveUser(userId)
        val normalizedChangeType = params.changeType?.trim()?.uppercase()?.takeIf { it.isNotBlank() }
        PageMethod.startPage<UserQuotaTransactionsRecord>(params.pageNum, params.pageSize)
        val records = userQuotaTransactionsMapper.select {
            where { UserQuotaTransactionsDynamicSqlSupport.UserQuotaTransactions.userId isEqualTo userId }
            if (normalizedChangeType != null) {
                and { UserQuotaTransactionsDynamicSqlSupport.UserQuotaTransactions.changeType isEqualTo normalizedChangeType }
            }
            orderBy(
                UserQuotaTransactionsDynamicSqlSupport.UserQuotaTransactions.createdTime.descending(),
                UserQuotaTransactionsDynamicSqlSupport.UserQuotaTransactions.id.descending()
            )
        }
        val pageInfo = PageInfo.of(records)
        return PageResult(
            pageNum = pageInfo.pageNum,
            pageSize = pageInfo.pageSize,
            total = pageInfo.total,
            list = records.map { mapTransactionResult(it) },
        )
    }

    @Transactional(rollbackFor = [Exception::class])
    fun adjustQuota(
        userId: Long,
        params: AdminUserQuotaAdjustmentParams,
        operatorUserId: Long?,
    ): UserQuotaAccountResult {
        ensureActiveUser(userId)
        val adjustTokens = params.adjustTokens ?: throw BizException(BizException.BUSINESS_FAILED, "调整额度不能为空")
        if (adjustTokens == 0L) {
            throw BizException(BizException.BUSINESS_FAILED, "调整额度不能为0")
        }

        val before = findAccount(userId) ?: createZeroAccount(userId)
        if (adjustTokens > 0) {
            val expiresAt = params.expiresAt ?: throw BizException(BizException.BUSINESS_FAILED, "新增额度必须指定过期时间")
            if (!expiresAt.after(Date())) {
                throw BizException(BizException.BUSINESS_FAILED, "过期时间必须晚于当前时间")
            }
            createGrant(
                userId = userId,
                sourceType = UserQuotaGrantSourceType.ADMIN_GRANT,
                sourceUserId = null,
                sourceGrantId = null,
                tokens = adjustTokens,
                expiresAt = expiresAt,
                operatorUserId = operatorUserId,
                remark = params.remark?.trim(),
            )
            val after = rebuildAccount(userId)
            insertTransaction(
                userId = userId,
                grantId = null,
                changeType = UserQuotaTransactionChangeType.ADMIN_GRANT,
                deltaTokens = adjustTokens,
                before = before,
                after = after,
                counterpartyUserId = null,
                requestId = null,
                operatorUserId = operatorUserId,
                remark = params.remark?.trim(),
            )
            return mapAccountResult(after, includeActiveGrants = true)
        }

        reclaimQuota(userId, -adjustTokens)
        val after = rebuildAccount(userId)
        insertTransaction(
            userId = userId,
            grantId = null,
            changeType = UserQuotaTransactionChangeType.ADMIN_RECLAIM,
            deltaTokens = adjustTokens,
            before = before,
            after = after,
            counterpartyUserId = null,
            requestId = null,
            operatorUserId = operatorUserId,
            remark = params.remark?.trim(),
        )
        return mapAccountResult(after, includeActiveGrants = true)
    }

    @Transactional(rollbackFor = [Exception::class])
    fun transferQuota(fromUserId: Long, params: UserQuotaTransferParams): UserQuotaTransferResult {
        ensureActiveUser(fromUserId)
        val targetUserId = params.targetUserId ?: throw BizException(BizException.BUSINESS_FAILED, "转入用户ID不能为空")
        val transferTokens = params.transferTokens ?: throw BizException(BizException.BUSINESS_FAILED, "转配额度不能为空")
        if (targetUserId == fromUserId) {
            throw BizException(BizException.BUSINESS_FAILED, "不允许给自己转配额度")
        }
        if (transferTokens <= 0) {
            throw BizException(BizException.BUSINESS_FAILED, "转配额度必须大于0")
        }
        ensureActiveUser(targetUserId)

        val fromBefore = requireAccount(fromUserId)
        if (fromBefore.allowTransferOut != true) {
            throw BizException(BizException.BUSINESS_FAILED, "当前用户不允许发起额度转配")
        }
        if ((fromBefore.availableTokens ?: 0L) < transferTokens) {
            throw BizException(BizException.BUSINESS_FAILED, "剩余额度不足，无法转配")
        }
        val targetBefore = findAccount(targetUserId) ?: createZeroAccount(targetUserId)

        val splits = deductFromActiveGrants(fromUserId, transferTokens)
        splits.forEach { split ->
            createGrant(
                userId = targetUserId,
                sourceType = UserQuotaGrantSourceType.TRANSFER_IN,
                sourceUserId = fromUserId,
                sourceGrantId = split.sourceGrantId,
                tokens = split.tokens,
                expiresAt = split.expiresAt,
                operatorUserId = fromUserId,
                remark = params.remark?.trim(),
            )
        }

        val fromAfter = rebuildAccount(fromUserId)
        val targetAfter = rebuildAccount(targetUserId)
        fromAfter.transferredOutTokens = (fromAfter.transferredOutTokens ?: 0L) + transferTokens
        fromAfter.updatedTime = Date()
        userQuotaAccountsMapper.updateByPrimaryKeySelective(fromAfter)
        val remark = params.remark?.trim()
        insertTransaction(
            userId = fromUserId,
            grantId = null,
            changeType = UserQuotaTransactionChangeType.TRANSFER_OUT,
            deltaTokens = -transferTokens,
            before = fromBefore,
            after = fromAfter,
            counterpartyUserId = targetUserId,
            requestId = null,
            operatorUserId = fromUserId,
            remark = remark,
        )
        insertTransaction(
            userId = targetUserId,
            grantId = null,
            changeType = UserQuotaTransactionChangeType.TRANSFER_IN,
            deltaTokens = transferTokens,
            before = targetBefore,
            after = targetAfter,
            counterpartyUserId = fromUserId,
            requestId = null,
            operatorUserId = fromUserId,
            remark = remark,
        )

        return UserQuotaTransferResult(
            fromUserId = fromUserId,
            targetUserId = targetUserId,
            transferTokens = transferTokens,
            fromAccount = mapAccountResult(fromAfter, includeActiveGrants = true),
            targetAccount = mapAccountResult(targetAfter, includeActiveGrants = true),
        )
    }

    private fun ensureActiveUser(userId: Long) {
        usersMapper.selectOne {
            where { UsersDynamicSqlSupport.Users.id isEqualTo userId }
            and { UsersDynamicSqlSupport.Users.status isEqualTo NormalStatus }
            and { UsersDynamicSqlSupport.Users.delFlag isEqualTo false }
        } ?: throw BizException(BizException.BUSINESS_FAILED, "用户不存在或已禁用")
    }

    private fun findAccount(userId: Long): UserQuotaAccountsRecord? {
        return userQuotaAccountsMapper.selectOne {
            where { UserQuotaAccountsDynamicSqlSupport.UserQuotaAccounts.userId isEqualTo userId }
        }
    }

    private fun requireAccount(userId: Long): UserQuotaAccountsRecord {
        return findAccount(userId) ?: throw BizException(BizException.BUSINESS_FAILED, "用户未配置额度账户")
    }

    private fun createZeroAccount(userId: Long): UserQuotaAccountsRecord {
        val now = Date()
        val record = UserQuotaAccountsRecord(
            userId = userId,
            currentQuotaTokens = 0,
            usedTokens = 0,
            expiredTokens = 0,
            transferredInTokens = 0,
            transferredOutTokens = 0,
            availableTokens = 0,
            allowTransferOut = true,
            earliestExpireAt = null,
            createdTime = now,
            updatedTime = now,
        )
        userQuotaAccountsMapper.insert(record)
        return record
    }

    private fun createGrant(
        userId: Long,
        sourceType: UserQuotaGrantSourceType,
        sourceUserId: Long?,
        sourceGrantId: Long?,
        tokens: Long,
        expiresAt: Date,
        operatorUserId: Long?,
        remark: String?,
    ): UserQuotaGrantsRecord {
        val now = Date()
        val record = UserQuotaGrantsRecord(
            userId = userId,
            sourceType = sourceType.value,
            sourceUserId = sourceUserId,
            sourceGrantId = sourceGrantId,
            grantedTokens = tokens,
            remainingTokens = tokens,
            consumedTokens = 0,
            expiredTokens = 0,
            expiresAt = expiresAt,
            status = UserQuotaGrantStatus.ACTIVE.value,
            grantedBy = operatorUserId,
            remark = remark,
            createdTime = now,
            updatedTime = now,
        )
        userQuotaGrantsMapper.insert(record)
        return record
    }

    private fun reclaimQuota(userId: Long, tokens: Long) {
        if ((requireAccount(userId).availableTokens ?: 0L) < tokens) {
            throw BizException(BizException.BUSINESS_FAILED, "剩余额度不足，无法回收")
        }
        deductFromActiveGrants(userId, tokens)
    }

    private fun deductFromActiveGrants(userId: Long, tokens: Long): List<QuotaDeductSplit> {
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
        var remainingToDeduct = tokens
        val splits = mutableListOf<QuotaDeductSplit>()

        grants.forEach { grant ->
            if (remainingToDeduct <= 0) return@forEach
            val grantId = grant.id ?: throw BizException(BizException.BUSINESS_FAILED, "配额账户数据异常，请重试")
            val grantRemaining = grant.remainingTokens ?: 0L
            val deduct = minOf(grantRemaining, remainingToDeduct)
            if (deduct <= 0) return@forEach

            grant.remainingTokens = grantRemaining - deduct
            grant.status = if (grant.remainingTokens == 0L) UserQuotaGrantStatus.DEPLETED.value else UserQuotaGrantStatus.ACTIVE.value
            grant.updatedTime = Date()
            userQuotaGrantsMapper.updateByPrimaryKeySelective(grant)

            splits += QuotaDeductSplit(
                sourceGrantId = grantId,
                tokens = deduct,
                expiresAt = grant.expiresAt ?: throw BizException(BizException.BUSINESS_FAILED, "配额批次过期时间缺失"),
            )
            remainingToDeduct -= deduct
        }

        if (remainingToDeduct > 0) {
            throw BizException(BizException.BUSINESS_FAILED, "剩余额度不足")
        }
        return splits
    }

    private fun rebuildAccount(userId: Long): UserQuotaAccountsRecord {
        val account = findAccount(userId) ?: createZeroAccount(userId)
        val grants = userQuotaGrantsMapper.select {
            where { UserQuotaGrantsDynamicSqlSupport.UserQuotaGrants.userId isEqualTo userId }
        }
        val activeGrants = grants.filter {
            it.status == UserQuotaGrantStatus.ACTIVE.value &&
                (it.remainingTokens ?: 0L) > 0 &&
                it.expiresAt?.after(Date()) == true
        }
        val currentQuotaTokens = activeGrants.sumOf { (it.remainingTokens ?: 0L) + (it.consumedTokens ?: 0L) }
        val availableTokens = activeGrants.sumOf { it.remainingTokens ?: 0L }
        val expiredTokens = grants
            .filter { it.status == UserQuotaGrantStatus.EXPIRED.value || it.expiresAt?.after(Date()) == false }
            .sumOf { (it.expiredTokens ?: 0L).takeIf { value -> value > 0 } ?: (it.remainingTokens ?: 0L) }
        val transferredInTokens = grants
            .filter { it.sourceType == UserQuotaGrantSourceType.TRANSFER_IN.value }
            .sumOf { it.grantedTokens ?: 0L }
        val transferredOutTokens = sumTransactionDelta(userId, UserQuotaTransactionChangeType.TRANSFER_OUT).let { -it }
        val earliestExpireAt = activeGrants.mapNotNull { it.expiresAt }.minOrNull()

        account.currentQuotaTokens = currentQuotaTokens
        account.availableTokens = availableTokens
        account.expiredTokens = expiredTokens
        account.transferredInTokens = transferredInTokens
        account.transferredOutTokens = transferredOutTokens
        account.earliestExpireAt = earliestExpireAt
        account.updatedTime = Date()
        userQuotaAccountsMapper.updateByPrimaryKeySelective(account)
        return account
    }

    private fun sumTransactionDelta(userId: Long, changeType: UserQuotaTransactionChangeType): Long {
        return userQuotaTransactionsMapper.select {
            where { UserQuotaTransactionsDynamicSqlSupport.UserQuotaTransactions.userId isEqualTo userId }
            and { UserQuotaTransactionsDynamicSqlSupport.UserQuotaTransactions.changeType isEqualTo changeType.value }
        }.sumOf { it.deltaTokens ?: 0L }
    }

    private fun insertTransaction(
        userId: Long,
        grantId: Long?,
        changeType: UserQuotaTransactionChangeType,
        deltaTokens: Long,
        before: UserQuotaAccountsRecord,
        after: UserQuotaAccountsRecord,
        counterpartyUserId: Long?,
        requestId: String?,
        operatorUserId: Long?,
        remark: String?,
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
                counterpartyUserId = counterpartyUserId,
                requestId = requestId,
                operatorUserId = operatorUserId,
                remark = remark,
                createdTime = Date(),
            )
        )
    }

    private fun queryActiveGrants(userId: Long): List<UserQuotaGrantResult> {
        return userQuotaGrantsMapper.select {
            where { UserQuotaGrantsDynamicSqlSupport.UserQuotaGrants.userId isEqualTo userId }
            and { UserQuotaGrantsDynamicSqlSupport.UserQuotaGrants.status isEqualTo UserQuotaGrantStatus.ACTIVE.value }
            and { UserQuotaGrantsDynamicSqlSupport.UserQuotaGrants.remainingTokens isGreaterThan 0L }
            and { UserQuotaGrantsDynamicSqlSupport.UserQuotaGrants.expiresAt isGreaterThan Date() }
            orderBy(
                UserQuotaGrantsDynamicSqlSupport.UserQuotaGrants.expiresAt,
                UserQuotaGrantsDynamicSqlSupport.UserQuotaGrants.id
            )
            limit(ACTIVE_GRANT_LIMIT.toLong())
        }.map { mapGrantResult(it) }
    }

    private fun mapAccountResult(record: UserQuotaAccountsRecord, includeActiveGrants: Boolean): UserQuotaAccountResult {
        val userId = record.userId ?: 0L
        return UserQuotaAccountResult(
            userId = userId,
            currentQuotaTokens = record.currentQuotaTokens ?: 0L,
            usedTokens = record.usedTokens ?: 0L,
            expiredTokens = record.expiredTokens ?: 0L,
            transferredInTokens = record.transferredInTokens ?: 0L,
            transferredOutTokens = record.transferredOutTokens ?: 0L,
            availableTokens = record.availableTokens ?: 0L,
            allowTransferOut = record.allowTransferOut ?: false,
            earliestExpireAt = record.earliestExpireAt,
            updatedAt = record.updatedTime,
            activeGrants = if (includeActiveGrants) queryActiveGrants(userId) else emptyList(),
        )
    }

    private fun mapGrantResult(record: UserQuotaGrantsRecord): UserQuotaGrantResult {
        return UserQuotaGrantResult(
            grantId = record.id ?: 0L,
            userId = record.userId ?: 0L,
            sourceType = record.sourceType ?: "",
            sourceUserId = record.sourceUserId,
            sourceGrantId = record.sourceGrantId,
            grantedTokens = record.grantedTokens ?: 0L,
            remainingTokens = record.remainingTokens ?: 0L,
            consumedTokens = record.consumedTokens ?: 0L,
            expiredTokens = record.expiredTokens ?: 0L,
            expiresAt = record.expiresAt ?: Date(0),
            status = record.status ?: "",
            grantedBy = record.grantedBy,
            remark = record.remark,
            createdAt = record.createdTime,
            updatedAt = record.updatedTime,
        )
    }

    private fun mapTransactionResult(record: UserQuotaTransactionsRecord): UserQuotaTransactionResult {
        return UserQuotaTransactionResult(
            transactionId = record.id ?: 0L,
            bizNo = record.bizNo ?: "",
            userId = record.userId ?: 0L,
            grantId = record.grantId,
            changeType = record.changeType ?: "",
            deltaTokens = record.deltaTokens ?: 0L,
            quotaBefore = record.quotaBefore ?: 0L,
            quotaAfter = record.quotaAfter ?: 0L,
            availableBefore = record.availableBefore ?: 0L,
            availableAfter = record.availableAfter ?: 0L,
            counterpartyUserId = record.counterpartyUserId,
            requestId = record.requestId,
            operatorUserId = record.operatorUserId,
            remark = record.remark,
            createdAt = record.createdTime,
        )
    }

    private data class QuotaDeductSplit(
        val sourceGrantId: Long,
        val tokens: Long,
        val expiresAt: Date,
    )
}
