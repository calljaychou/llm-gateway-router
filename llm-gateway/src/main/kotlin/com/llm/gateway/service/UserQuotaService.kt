package com.llm.gateway.service

import com.github.pagehelper.PageInfo
import com.github.pagehelper.page.PageMethod
import com.llm.gateway.common.enums.NormalStatus
import com.llm.gateway.common.enums.UserQuotaGrantSourceType
import com.llm.gateway.common.enums.UserQuotaGrantStatus
import com.llm.gateway.common.enums.UserQuotaTransactionChangeType
import com.llm.gateway.common.exceptions.BizException
import com.llm.gateway.common.logger
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
import com.llm.gateway.dal.model.UsersRecord
import com.llm.gateway.model.PageParams
import com.llm.gateway.model.params.AdminUserQuotaAdjustmentParams
import com.llm.gateway.model.params.AdminUserQuotaTransferPermissionParams
import com.llm.gateway.model.params.UserQuotaTransactionsPageParams
import com.llm.gateway.model.params.UserQuotaTransferParams
import com.llm.gateway.model.PageResult
import com.llm.gateway.model.results.UserQuotaAccountResult
import com.llm.gateway.model.results.UserQuotaGrantResult
import com.llm.gateway.model.results.UserQuotaTransactionResult
import com.llm.gateway.model.results.UserQuotaTransferResult
import java.math.BigDecimal
import java.util.Date
import java.util.UUID
import java.util.concurrent.TimeUnit
import org.redisson.api.RLock
import org.redisson.api.RedissonClient
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.transaction.support.TransactionTemplate

@Service
class UserQuotaService(
    private val usersMapper: UsersMapper,
    private val userQuotaAccountsMapper: UserQuotaAccountsMapper,
    private val userQuotaGrantsMapper: UserQuotaGrantsMapper,
    private val userQuotaTransactionsMapper: UserQuotaTransactionsMapper,
    private val redissonClient: RedissonClient,
    private val transactionTemplate: TransactionTemplate,
) {
    private val log = logger()

    companion object {
        private const val ACTIVE_GRANT_LIMIT = 5
        private const val QUOTA_TRANSFER_LOCK_PREFIX = "gateway:user-quota:transfer:"
        private const val QUOTA_TRANSFER_LOCK_WAIT_SECONDS = 5L
        private val ZERO_AMOUNT = BigDecimal.ZERO
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
        PageMethod.startPage<UserQuotaTransactionsRecord>(params.pageNum, params.pageSize)
        val records = userQuotaTransactionsMapper.select {
            where { UserQuotaTransactionsDynamicSqlSupport.UserQuotaTransactions.userId isEqualTo userId }
            and { UserQuotaTransactionsDynamicSqlSupport.UserQuotaTransactions.changeType isEqualToWhenPresent params.type?.value }
            and {
                UserQuotaTransactionsDynamicSqlSupport.UserQuotaTransactions.changeType isNotIn listOf(
                    UserQuotaTransactionChangeType.USAGE_RESERVE.value,
                    UserQuotaTransactionChangeType.USAGE_REFUND.value,
                )
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
        val adjustAmount = params.adjustAmount ?: throw BizException(BizException.BUSINESS_FAILED, "调整额度不能为空")
        if (adjustAmount.compareTo(ZERO_AMOUNT) == 0) {
            throw BizException(BizException.BUSINESS_FAILED, "调整额度不能为0")
        }

        val before = findAccount(userId) ?: createZeroAccount(userId)
        if (adjustAmount > ZERO_AMOUNT) {
            val expiresAt = params.expiresAt ?: throw BizException(BizException.BUSINESS_FAILED, "新增额度必须指定过期时间")
            if (!expiresAt.after(Date())) {
                throw BizException(BizException.BUSINESS_FAILED, "过期时间必须晚于当前时间")
            }
            createGrant(
                userId = userId,
                sourceType = UserQuotaGrantSourceType.ADMIN_GRANT,
                sourceUserId = null,
                sourceGrantId = null,
                amount = adjustAmount,
                expiresAt = expiresAt,
                operatorUserId = operatorUserId,
                remark = params.remark?.trim(),
            )
            val after = rebuildAccount(userId)
            insertTransaction(
                userId = userId,
                grantId = null,
                changeType = UserQuotaTransactionChangeType.ADMIN_GRANT,
                deltaAmount = adjustAmount,
                before = before,
                after = after,
                counterpartyUserId = null,
                requestId = null,
                operatorUserId = operatorUserId,
                remark = params.remark?.trim(),
            )
            return mapAccountResult(after, includeActiveGrants = true)
        }

        reclaimQuota(userId, adjustAmount.negate())
        val after = rebuildAccount(userId)
        insertTransaction(
            userId = userId,
            grantId = null,
            changeType = UserQuotaTransactionChangeType.ADMIN_RECLAIM,
            deltaAmount = adjustAmount,
            before = before,
            after = after,
            counterpartyUserId = null,
            requestId = null,
            operatorUserId = operatorUserId,
            remark = params.remark?.trim(),
        )
        return mapAccountResult(after, includeActiveGrants = true)
    }

    /**
     * 更新用户是否允许向外转配额度。
     */
    fun updateTransferPermission(
        userId: Long,
        params: AdminUserQuotaTransferPermissionParams,
        operatorUserId: Long?,
    ): UserQuotaAccountResult {
        ensureActiveUser(userId)
        val allowTransferOut = params.allowTransferOut
            ?: throw BizException(BizException.BUSINESS_FAILED, "是否允许转配不能为空")
        val account = findAccount(userId) ?: createZeroAccount(userId)
        account.allowTransferOut = allowTransferOut
        account.updatedTime = Date()
        userQuotaAccountsMapper.updateByPrimaryKeySelective(account)
        log.info("管理员更新用户配额转配开关 userId={}, allowTransferOut={}, operatorUserId={}", userId, allowTransferOut, operatorUserId)
        return mapAccountResult(account, includeActiveGrants = true)
    }

    fun transferQuota(fromUserId: Long, params: UserQuotaTransferParams): UserQuotaTransferResult {
        ensureActiveUser(fromUserId)
        validateTransferPermission(fromUserId)
        val targetUserId = resolveTransferTargetUserId(params.targetUser)
        val transferAmount = params.transferAmount ?: throw BizException(BizException.BUSINESS_FAILED, "转配额度不能为空")
        if (targetUserId == fromUserId) {
            throw BizException(BizException.BUSINESS_FAILED, "不允许给自己转配额度")
        }
        if (transferAmount <= ZERO_AMOUNT) {
            throw BizException(BizException.BUSINESS_FAILED, "转配额度必须大于0")
        }
        ensureActiveUser(targetUserId)

        return withQuotaTransferLocks(fromUserId, targetUserId) {
            transactionTemplate.execute {
                transferQuotaCore(fromUserId, targetUserId, transferAmount, params.remark?.trim())
            } ?: throw BizException(BizException.BUSINESS_FAILED, "配额转配失败")
        }
    }

    /**
     * 校验当前用户是否已开启配额转配权限。
     */
    private fun validateTransferPermission(userId: Long) {
        val account = requireAccount(userId)
        if (account.allowTransferOut != true) {
            throw BizException(BizException.BUSINESS_FAILED, "当前用户未开启额度转配权限")
        }
    }

    /**
     * 根据用户名、手机号或邮箱精确锁定转入用户。
     */
    private fun resolveTransferTargetUserId(targetUser: String?): Long {
        val keyword = targetUser?.trim()?.takeIf { it.isNotBlank() }
            ?: throw BizException(BizException.BUSINESS_FAILED, "转入用户账号不能为空")
        val matchedUsers = listOf(
            findActiveUserByUsername(keyword),
            findActiveUserByMobile(keyword),
            findActiveUserByEmail(keyword.lowercase()),
        ).flatten().distinctBy { it.id }

        if (matchedUsers.isEmpty()) {
            throw BizException(BizException.BUSINESS_FAILED, "转入用户不存在或已禁用")
        }
        if (matchedUsers.size > 1) {
            throw BizException(BizException.BUSINESS_FAILED, "转入用户账号匹配到多个用户，请使用唯一的用户名、手机号或邮箱")
        }
        return matchedUsers.first().id ?: throw BizException(BizException.BUSINESS_FAILED, "转入用户ID缺失")
    }

    /**
     * 按用户名查询有效用户。
     */
    private fun findActiveUserByUsername(username: String): List<UsersRecord> {
        return usersMapper.select {
            where { UsersDynamicSqlSupport.Users.username isEqualTo username }
            and { UsersDynamicSqlSupport.Users.status isEqualTo NormalStatus }
            and { UsersDynamicSqlSupport.Users.delFlag isEqualTo false }
        }
    }

    /**
     * 按手机号查询有效用户。
     */
    private fun findActiveUserByMobile(mobile: String): List<UsersRecord> {
        return usersMapper.select {
            where { UsersDynamicSqlSupport.Users.mobile isEqualTo mobile }
            and { UsersDynamicSqlSupport.Users.status isEqualTo NormalStatus }
            and { UsersDynamicSqlSupport.Users.delFlag isEqualTo false }
        }
    }

    /**
     * 按邮箱查询有效用户。
     */
    private fun findActiveUserByEmail(email: String): List<UsersRecord> {
        return usersMapper.select {
            where { UsersDynamicSqlSupport.Users.email isEqualTo email }
            and { UsersDynamicSqlSupport.Users.status isEqualTo NormalStatus }
            and { UsersDynamicSqlSupport.Users.delFlag isEqualTo false }
        }
    }

    /**
     * 执行用户配额转配核心流程：校验转出余额、扣减转出批次、创建转入批次并记录双边流水。
     */
    private fun transferQuotaCore(
        fromUserId: Long,
        targetUserId: Long,
        transferAmount: BigDecimal,
        remark: String?,
    ): UserQuotaTransferResult {
        val fromBefore = requireAccount(fromUserId)
        if (fromBefore.allowTransferOut != true) {
            throw BizException(BizException.BUSINESS_FAILED, "当前用户不允许发起额度转配")
        }
        if ((fromBefore.availableAmount ?: ZERO_AMOUNT) < transferAmount) {
            throw BizException(BizException.BUSINESS_FAILED, "剩余额度不足，无法转配")
        }
        val targetBefore = findAccount(targetUserId) ?: createZeroAccount(targetUserId)

        val splits = deductFromActiveGrants(fromUserId, transferAmount)
        splits.forEach { split ->
            createGrant(
                userId = targetUserId,
                sourceType = UserQuotaGrantSourceType.TRANSFER_IN,
                sourceUserId = fromUserId,
                sourceGrantId = split.sourceGrantId,
                amount = split.amount,
                expiresAt = split.expiresAt,
                operatorUserId = fromUserId,
                remark = remark,
            )
        }

        val fromAfter = rebuildAccount(fromUserId)
        val targetAfter = rebuildAccount(targetUserId)
        fromAfter.transferredOutAmount = (fromAfter.transferredOutAmount ?: ZERO_AMOUNT) + transferAmount
        fromAfter.updatedTime = Date()
        userQuotaAccountsMapper.updateByPrimaryKeySelective(fromAfter)
        insertTransaction(
            userId = fromUserId,
            grantId = null,
            changeType = UserQuotaTransactionChangeType.TRANSFER_OUT,
            deltaAmount = transferAmount.negate(),
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
            deltaAmount = transferAmount,
            before = targetBefore,
            after = targetAfter,
            counterpartyUserId = fromUserId,
            requestId = null,
            operatorUserId = fromUserId,
            remark = remark,
        )

        log.info(
            "用户配额转配成功 fromUserId={}, targetUserId={}, transferAmount={}, fromAvailableBefore={}, fromAvailableAfter={}, targetAvailableBefore={}, targetAvailableAfter={}",
            fromUserId,
            targetUserId,
            transferAmount,
            fromBefore.availableAmount,
            fromAfter.availableAmount,
            targetBefore.availableAmount,
            targetAfter.availableAmount,
        )

        return UserQuotaTransferResult(
            fromUserId = fromUserId,
            targetUserId = targetUserId,
            transferAmount = transferAmount,
            fromAccount = mapAccountResult(fromAfter, includeActiveGrants = true),
            targetAccount = mapAccountResult(targetAfter, includeActiveGrants = true),
        )
    }

    /**
     * 获取转出和转入用户的配额转配锁，按用户ID排序加锁以避免互转死锁。
     */
    private fun <T> withQuotaTransferLocks(fromUserId: Long, targetUserId: Long, block: () -> T): T {
        val locks = listOf(fromUserId, targetUserId)
            .distinct()
            .sorted()
            .map { redissonClient.getLock("$QUOTA_TRANSFER_LOCK_PREFIX$it") }
        val locked = mutableListOf<RLock>()
        try {
            locks.forEach { lock ->
                val acquired = lock.tryLock(QUOTA_TRANSFER_LOCK_WAIT_SECONDS, TimeUnit.SECONDS)
                if (!acquired) {
                    log.warn("用户配额转配锁获取失败 fromUserId={}, targetUserId={}", fromUserId, targetUserId)
                    throw BizException(BizException.BUSINESS_FAILED, "配额转配处理中，请稍后重试")
                }
                locked.add(lock)
            }
            return block()
        } finally {
            locked.asReversed().forEach { lock ->
                if (lock.isHeldByCurrentThread) {
                    lock.unlock()
                }
            }
        }
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
            currentQuotaAmount = ZERO_AMOUNT,
            usedAmount = ZERO_AMOUNT,
            expiredAmount = ZERO_AMOUNT,
            transferredInAmount = ZERO_AMOUNT,
            transferredOutAmount = ZERO_AMOUNT,
            availableAmount = ZERO_AMOUNT,
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
        amount: BigDecimal,
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
            grantedAmount = amount,
            remainingAmount = amount,
            consumedAmount = ZERO_AMOUNT,
            expiredAmount = ZERO_AMOUNT,
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

    private fun reclaimQuota(userId: Long, amount: BigDecimal) {
        if ((requireAccount(userId).availableAmount ?: ZERO_AMOUNT) < amount) {
            throw BizException(BizException.BUSINESS_FAILED, "剩余额度不足，无法回收")
        }
        deductFromActiveGrants(userId, amount)
    }

    /**
     * 按最早过期批次优先扣减用户剩余额度，单个批次使用条件更新避免并发转配超扣。
     */
    private fun deductFromActiveGrants(userId: Long, amount: BigDecimal): List<QuotaDeductSplit> {
        var remainingToDeduct = amount
        val splits = mutableListOf<QuotaDeductSplit>()

        while (remainingToDeduct > ZERO_AMOUNT) {
            val grant = queryEarliestActiveGrant(userId)
                ?: break
            val grantId = grant.id ?: throw BizException(BizException.BUSINESS_FAILED, "配额账户数据异常，请重试")
            val grantRemaining = grant.remainingAmount ?: ZERO_AMOUNT
            val deductAmount = grantRemaining.min(remainingToDeduct)
            if (deductAmount <= ZERO_AMOUNT) break

            grant.remainingAmount = grantRemaining - deductAmount
            grant.consumedAmount = (grant.consumedAmount ?: ZERO_AMOUNT) + deductAmount
            grant.status =
                if ((grant.remainingAmount ?: ZERO_AMOUNT).compareTo(ZERO_AMOUNT) == 0) {
                    UserQuotaGrantStatus.DEPLETED.value
                } else {
                    UserQuotaGrantStatus.ACTIVE.value
                }
            grant.updatedTime = Date()
            userQuotaGrantsMapper.updateByPrimaryKeySelective(grant)

            splits.add(
                QuotaDeductSplit(
                    sourceGrantId = grantId,
                    amount = deductAmount,
                    expiresAt = grant.expiresAt ?: throw BizException(BizException.BUSINESS_FAILED, "配额批次过期时间缺失"),
                )
            )
            remainingToDeduct = remainingToDeduct - deductAmount
        }

        if (remainingToDeduct > ZERO_AMOUNT) {
            log.warn("用户配额扣减不足 userId={}, amount={}, remainingToDeduct={}", userId, amount, remainingToDeduct)
            throw BizException(BizException.BUSINESS_FAILED, "剩余额度不足")
        }
        return splits
    }

    /**
     * 查询用户最早过期的有效配额批次，用于转配和回收时按过期时间优先扣减。
     */
    private fun queryEarliestActiveGrant(userId: Long): UserQuotaGrantsRecord? {
        return userQuotaGrantsMapper.select {
            where { UserQuotaGrantsDynamicSqlSupport.UserQuotaGrants.userId isEqualTo userId }
            and { UserQuotaGrantsDynamicSqlSupport.UserQuotaGrants.status isEqualTo UserQuotaGrantStatus.ACTIVE.value }
            and { UserQuotaGrantsDynamicSqlSupport.UserQuotaGrants.remainingAmount isGreaterThan ZERO_AMOUNT }
            and { UserQuotaGrantsDynamicSqlSupport.UserQuotaGrants.expiresAt isGreaterThan Date() }
            orderBy(
                UserQuotaGrantsDynamicSqlSupport.UserQuotaGrants.expiresAt,
                UserQuotaGrantsDynamicSqlSupport.UserQuotaGrants.id
            )
            limit(1L)
        }.firstOrNull()
    }

    private fun rebuildAccount(userId: Long): UserQuotaAccountsRecord {
        val account = findAccount(userId) ?: createZeroAccount(userId)
        val grants = userQuotaGrantsMapper.select {
            where { UserQuotaGrantsDynamicSqlSupport.UserQuotaGrants.userId isEqualTo userId }
        }
        val now = Date()
        val notExpiredGrants = grants.filter {
            it.expiresAt?.after(now) == true &&
                it.status in setOf(UserQuotaGrantStatus.ACTIVE.value, UserQuotaGrantStatus.DEPLETED.value)
        }
        val activeGrants = grants.filter {
            it.status == UserQuotaGrantStatus.ACTIVE.value &&
                (it.remainingAmount ?: ZERO_AMOUNT) > ZERO_AMOUNT &&
                it.expiresAt?.after(now) == true
        }
        val currentQuotaAmount = notExpiredGrants.fold(ZERO_AMOUNT) { total, it -> total + (it.remainingAmount ?: ZERO_AMOUNT) + (it.consumedAmount ?: ZERO_AMOUNT) }
        val availableAmount = activeGrants.fold(ZERO_AMOUNT) { total, it -> total + (it.remainingAmount ?: ZERO_AMOUNT) }
        val usedAmount = notExpiredGrants.fold(ZERO_AMOUNT) { total, it -> total + (it.consumedAmount ?: ZERO_AMOUNT) }
        val expiredAmount = grants
            .filter { it.status == UserQuotaGrantStatus.EXPIRED.value || it.expiresAt?.after(Date()) == false }
            .fold(ZERO_AMOUNT) { total, it -> total + ((it.expiredAmount ?: ZERO_AMOUNT).takeIf { value -> value > ZERO_AMOUNT } ?: (it.remainingAmount ?: ZERO_AMOUNT)) }
        val transferredInAmount = grants
            .filter { it.sourceType == UserQuotaGrantSourceType.TRANSFER_IN.value }
            .fold(ZERO_AMOUNT) { total, it -> total + (it.grantedAmount ?: ZERO_AMOUNT) }
        val transferredOutAmount = sumTransactionDelta(userId, UserQuotaTransactionChangeType.TRANSFER_OUT).negate()
        val earliestExpireAt = activeGrants.mapNotNull { it.expiresAt }.minOrNull()

        account.currentQuotaAmount = currentQuotaAmount
        account.usedAmount = usedAmount
        account.availableAmount = availableAmount
        account.expiredAmount = expiredAmount
        account.transferredInAmount = transferredInAmount
        account.transferredOutAmount = transferredOutAmount
        account.earliestExpireAt = earliestExpireAt
        account.updatedTime = Date()
        userQuotaAccountsMapper.updateByPrimaryKeySelective(account)
        return account
    }

    private fun sumTransactionDelta(userId: Long, changeType: UserQuotaTransactionChangeType): BigDecimal {
        return userQuotaTransactionsMapper.select {
            where { UserQuotaTransactionsDynamicSqlSupport.UserQuotaTransactions.userId isEqualTo userId }
            and { UserQuotaTransactionsDynamicSqlSupport.UserQuotaTransactions.changeType isEqualTo changeType.value }
        }.fold(ZERO_AMOUNT) { total, it -> total + (it.deltaAmount ?: ZERO_AMOUNT) }
    }

    private fun insertTransaction(
        userId: Long,
        grantId: Long?,
        changeType: UserQuotaTransactionChangeType,
        deltaAmount: BigDecimal,
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
                deltaAmount = deltaAmount,
                quotaBeforeAmount = before.currentQuotaAmount ?: ZERO_AMOUNT,
                quotaAfterAmount = after.currentQuotaAmount ?: ZERO_AMOUNT,
                availableBeforeAmount = before.availableAmount ?: ZERO_AMOUNT,
                availableAfterAmount = after.availableAmount ?: ZERO_AMOUNT,
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
            and { UserQuotaGrantsDynamicSqlSupport.UserQuotaGrants.remainingAmount isGreaterThan ZERO_AMOUNT }
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
            currentQuotaAmount = record.currentQuotaAmount ?: ZERO_AMOUNT,
            usedAmount = record.usedAmount ?: ZERO_AMOUNT,
            expiredAmount = record.expiredAmount ?: ZERO_AMOUNT,
            transferredInAmount = record.transferredInAmount ?: ZERO_AMOUNT,
            transferredOutAmount = record.transferredOutAmount ?: ZERO_AMOUNT,
            availableAmount = record.availableAmount ?: ZERO_AMOUNT,
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
            grantedAmount = record.grantedAmount ?: ZERO_AMOUNT,
            remainingAmount = record.remainingAmount ?: ZERO_AMOUNT,
            consumedAmount = record.consumedAmount ?: ZERO_AMOUNT,
            expiredAmount = record.expiredAmount ?: ZERO_AMOUNT,
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
            deltaAmount = record.deltaAmount ?: ZERO_AMOUNT,
            quotaBeforeAmount = record.quotaBeforeAmount ?: ZERO_AMOUNT,
            quotaAfterAmount = record.quotaAfterAmount ?: ZERO_AMOUNT,
            availableBeforeAmount = record.availableBeforeAmount ?: ZERO_AMOUNT,
            availableAfterAmount = record.availableAfterAmount ?: ZERO_AMOUNT,
            counterpartyUserId = record.counterpartyUserId,
            requestId = record.requestId,
            operatorUserId = record.operatorUserId,
            remark = record.remark,
            createdAt = record.createdTime,
        )
    }

    private data class QuotaDeductSplit(
        val sourceGrantId: Long,
        val amount: BigDecimal,
        val expiresAt: Date,
    )
}
