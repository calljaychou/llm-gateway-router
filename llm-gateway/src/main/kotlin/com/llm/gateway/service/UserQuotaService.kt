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
        val transferTokens = params.transferTokens ?: throw BizException(BizException.BUSINESS_FAILED, "转配额度不能为空")
        if (targetUserId == fromUserId) {
            throw BizException(BizException.BUSINESS_FAILED, "不允许给自己转配额度")
        }
        if (transferTokens <= 0) {
            throw BizException(BizException.BUSINESS_FAILED, "转配额度必须大于0")
        }
        ensureActiveUser(targetUserId)

        return withQuotaTransferLocks(fromUserId, targetUserId) {
            transactionTemplate.execute {
                transferQuotaCore(fromUserId, targetUserId, transferTokens, params.remark?.trim())
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
        transferTokens: Long,
        remark: String?,
    ): UserQuotaTransferResult {
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
                remark = remark,
            )
        }

        val fromAfter = rebuildAccount(fromUserId)
        val targetAfter = rebuildAccount(targetUserId)
        fromAfter.transferredOutTokens = (fromAfter.transferredOutTokens ?: 0L) + transferTokens
        fromAfter.updatedTime = Date()
        userQuotaAccountsMapper.updateByPrimaryKeySelective(fromAfter)
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

        log.info(
            "用户配额转配成功 fromUserId={}, targetUserId={}, transferTokens={}, fromAvailableBefore={}, fromAvailableAfter={}, targetAvailableBefore={}, targetAvailableAfter={}",
            fromUserId,
            targetUserId,
            transferTokens,
            fromBefore.availableTokens,
            fromAfter.availableTokens,
            targetBefore.availableTokens,
            targetAfter.availableTokens,
        )

        return UserQuotaTransferResult(
            fromUserId = fromUserId,
            targetUserId = targetUserId,
            transferTokens = transferTokens,
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
                locked += lock
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

    /**
     * 按最早过期批次优先扣减用户剩余额度，单个批次使用条件更新避免并发转配超扣。
     */
    private fun deductFromActiveGrants(userId: Long, tokens: Long): List<QuotaDeductSplit> {
        var remainingToDeduct = tokens
        val splits = mutableListOf<QuotaDeductSplit>()

        while (remainingToDeduct > 0) {
            val grant = queryEarliestActiveGrant(userId)
                ?: break
            val grantId = grant.id ?: throw BizException(BizException.BUSINESS_FAILED, "配额账户数据异常，请重试")
            val grantRemaining = grant.remainingTokens ?: 0L
            val deductTokens = minOf(grantRemaining, remainingToDeduct)
            if (deductTokens <= 0) break

            // 通过数据库条件更新保证并发场景下只有一个请求能成功扣减该批次。
            val updated = userQuotaGrantsMapper.deductRemainingTokens(
                grantId = grantId,
                userId = userId,
                deductTokens = deductTokens,
                activeStatus = UserQuotaGrantStatus.ACTIVE.value,
                depletedStatus = UserQuotaGrantStatus.DEPLETED.value,
                now = Date(),
                updatedTime = Date(),
            )
            if (updated == 0) {
                log.warn("用户配额批次扣减冲突 userId={}, grantId={}, deductTokens={}", userId, grantId, deductTokens)
                continue
            }

            splits += QuotaDeductSplit(
                sourceGrantId = grantId,
                tokens = deductTokens,
                expiresAt = grant.expiresAt ?: throw BizException(BizException.BUSINESS_FAILED, "配额批次过期时间缺失"),
            )
            remainingToDeduct -= deductTokens
        }

        if (remainingToDeduct > 0) {
            log.warn("用户配额扣减不足 userId={}, tokens={}, remainingToDeduct={}", userId, tokens, remainingToDeduct)
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
            and { UserQuotaGrantsDynamicSqlSupport.UserQuotaGrants.remainingTokens isGreaterThan 0L }
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
                (it.remainingTokens ?: 0L) > 0 &&
                it.expiresAt?.after(now) == true
        }
        val currentQuotaTokens = notExpiredGrants.sumOf { (it.remainingTokens ?: 0L) + (it.consumedTokens ?: 0L) }
        val availableTokens = activeGrants.sumOf { it.remainingTokens ?: 0L }
        val usedTokens = notExpiredGrants.sumOf { it.consumedTokens ?: 0L }
        val expiredTokens = grants
            .filter { it.status == UserQuotaGrantStatus.EXPIRED.value || it.expiresAt?.after(Date()) == false }
            .sumOf { (it.expiredTokens ?: 0L).takeIf { value -> value > 0 } ?: (it.remainingTokens ?: 0L) }
        val transferredInTokens = grants
            .filter { it.sourceType == UserQuotaGrantSourceType.TRANSFER_IN.value }
            .sumOf { it.grantedTokens ?: 0L }
        val transferredOutTokens = sumTransactionDelta(userId, UserQuotaTransactionChangeType.TRANSFER_OUT).let { -it }
        val earliestExpireAt = activeGrants.mapNotNull { it.expiresAt }.minOrNull()

        account.currentQuotaTokens = currentQuotaTokens
        account.usedTokens = usedTokens
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
