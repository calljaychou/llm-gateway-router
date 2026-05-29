package com.llm.gateway.service

import com.github.pagehelper.PageInfo
import com.github.pagehelper.page.PageMethod
import com.llm.gateway.common.enums.DepartmentPermissionScope
import com.llm.gateway.common.enums.NormalStatus
import com.llm.gateway.common.exceptions.BizException
import com.llm.gateway.common.logger
import com.llm.gateway.dal.mapper.DepartmentDynamicSqlSupport
import com.llm.gateway.dal.mapper.DepartmentMapper
import com.llm.gateway.dal.mapper.DepartmentModelPermissionsDynamicSqlSupport
import com.llm.gateway.dal.mapper.DepartmentModelPermissionsMapper
import com.llm.gateway.dal.mapper.ModelsDynamicSqlSupport
import com.llm.gateway.dal.mapper.ModelsMapper
import com.llm.gateway.dal.mapper.RolesDynamicSqlSupport
import com.llm.gateway.dal.mapper.RolesMapper
import com.llm.gateway.dal.mapper.UserRoleRelMapper
import com.llm.gateway.dal.mapper.UserRoleRelDynamicSqlSupport
import com.llm.gateway.dal.mapper.UserQuotaAccountsDynamicSqlSupport
import com.llm.gateway.dal.mapper.UserQuotaAccountsMapper
import com.llm.gateway.dal.mapper.VendorsDynamicSqlSupport
import com.llm.gateway.dal.mapper.VendorsMapper
import com.llm.gateway.dal.mapper.UsersDynamicSqlSupport
import com.llm.gateway.dal.mapper.UsersMapper
import com.llm.gateway.dal.mapper.insert
import com.llm.gateway.dal.mapper.insertMultiple
import com.llm.gateway.dal.mapper.select
import com.llm.gateway.dal.mapper.selectOne
import com.llm.gateway.dal.mapper.delete
import com.llm.gateway.dal.mapper.update
import com.llm.gateway.dal.mapper.updateByPrimaryKeySelective
import com.llm.gateway.dal.model.DepartmentModelPermissionsRecord
import com.llm.gateway.dal.model.DepartmentRecord
import com.llm.gateway.dal.model.RolesRecord
import com.llm.gateway.dal.model.UserQuotaAccountsRecord
import com.llm.gateway.dal.model.UserRoleRelRecord
import com.llm.gateway.dal.model.UsersRecord
import com.llm.gateway.model.PageResult
import com.llm.gateway.model.dto.ModelMetaDto
import com.llm.gateway.model.params.AdminUserCreateParams
import com.llm.gateway.model.params.AdminUserPageParams
import com.llm.gateway.model.params.AdminUserPasswordChangeParams
import com.llm.gateway.model.params.AdminUserUpdateParams
import com.llm.gateway.model.params.DepartmentPermissionsUpdateParams
import com.llm.gateway.model.results.AdminUserCreateResult
import com.llm.gateway.model.results.AdminUserBaseInfoResult
import com.llm.gateway.model.results.AdminUserDepartmentResult
import com.llm.gateway.model.results.AdminUserDetailResult
import com.llm.gateway.model.results.AdminUserPageItemResult
import com.llm.gateway.model.results.AdminUserPasswordChangeResult
import com.llm.gateway.model.results.AdminUserQuotaConfigResult
import com.llm.gateway.model.results.AdminUserUpdateResult
import com.llm.gateway.model.results.DepartmentPermissionViewItem
import com.llm.gateway.model.results.DepartmentPermissionsUpdateResult
import com.llm.gateway.model.results.DepartmentPermissionsViewResult
import com.llm.gateway.model.results.RoleListItemResult
import com.llm.gateway.model.results.UserEffectivePermissionsResult
import java.math.BigDecimal
import java.util.Date
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class AdminUserPermissionService(
    private val usersMapper: UsersMapper,
    private val departmentService: DepartmentService,
    private val departmentMapper: DepartmentMapper,
    private val rolesMapper: RolesMapper,
    private val userRoleRelMapper: UserRoleRelMapper,
    private val userQuotaAccountsMapper: UserQuotaAccountsMapper,
    private val vendorsMapper: VendorsMapper,
    private val modelsMapper: ModelsMapper,
    private val departmentModelPermissionsMapper: DepartmentModelPermissionsMapper,
    private val passwordEncoder: PasswordEncoder,
) {

    private val log = logger()

    companion object {
        private val ZERO_AMOUNT = BigDecimal.ZERO
    }

    @Transactional(rollbackFor = [Exception::class])
    fun createUser(params: AdminUserCreateParams): AdminUserCreateResult {
        val deptId = params.deptId ?: throw BizException(BizException.BUSINESS_FAILED, "部门ID不能为空")
        departmentService.ensureDeptExists(deptId)

        val roleRecords = listValidRoles(params.roleKeys)

        val now = Date()
        val userRecord = UsersRecord(
            deptId = deptId,
            name = params.name,
            username = params.username.trim(),
            email = params.email.trim().lowercase(),
            mobile = params.mobile?.trim(),
            gender = params.gender ?: 3,
            avatarUrl = null,
            password = passwordEncoder.encode(params.password),
            passwordChanged = !(params.forcePasswordChange ?: true),
            remark = null,
            status = params.status ?: NormalStatus,
            delFlag = false,
            createdTime = now,
            updatedTime = now,
        )

        try {
            usersMapper.insert(userRecord)
        } catch (e: DataIntegrityViolationException) {
            val message = e.message.orEmpty().lowercase()
            if (message.contains("username")) {
                throw BizException(BizException.BUSINESS_FAILED, "用户名已存在")
            }
            if (message.contains("email")) {
                throw BizException(BizException.BUSINESS_FAILED, "邮箱已存在")
            }
            throw BizException(BizException.SYSTEM_FAILED, "创建用户失败")
        }

        val userId = userRecord.id ?: throw BizException(BizException.SYSTEM_FAILED, "创建用户失败")
        insertUserRoles(userId, roleRecords)

        return AdminUserCreateResult(
            userId = userId,
            passwordChanged = userRecord.passwordChanged ?: false,
        )
    }

    @Transactional(rollbackFor = [Exception::class])
    fun changeUserPassword(userId: Long, params: AdminUserPasswordChangeParams): AdminUserPasswordChangeResult {
        ensureUserExists(userId)

        usersMapper.updateByPrimaryKeySelective(
            UsersRecord(
                id = userId,
                password = passwordEncoder.encode(params.password),
                passwordChanged = false,
                updatedTime = Date(),
            )
        )
        log.info("管理员修改用户密码成功 userId={}", userId)

        return AdminUserPasswordChangeResult(
            userId = userId,
            passwordChanged = false,
        )
    }

    @Transactional(rollbackFor = [Exception::class])
    fun updateUser(userId: Long, params: AdminUserUpdateParams): AdminUserUpdateResult {
        ensureUserExists(userId)
        val deptId = params.deptId ?: throw BizException(BizException.BUSINESS_FAILED, "部门ID不能为空")
        departmentService.ensureDeptExists(deptId)
        val roleRecords = listValidRoles(params.roleKeys)

        val now = Date()
        val mobile = params.mobile?.trim()?.ifBlank { null }
        try {
            usersMapper.update {
                set(UsersDynamicSqlSupport.Users.deptId).equalTo(deptId)
                set(UsersDynamicSqlSupport.Users.name).equalTo(params.name.trim())
                set(UsersDynamicSqlSupport.Users.username).equalTo(params.username.trim())
                set(UsersDynamicSqlSupport.Users.email).equalTo(params.email.trim().lowercase())
                set(UsersDynamicSqlSupport.Users.gender).equalToWhenPresent(params.gender)
                set(UsersDynamicSqlSupport.Users.status).equalToWhenPresent(params.status)
                if (mobile == null) {
                    set(UsersDynamicSqlSupport.Users.mobile).equalToNull()
                } else {
                    set(UsersDynamicSqlSupport.Users.mobile).equalTo(mobile)
                }
                set(UsersDynamicSqlSupport.Users.updatedTime).equalTo(now)
                where { UsersDynamicSqlSupport.Users.id isEqualTo userId }
                and { UsersDynamicSqlSupport.Users.delFlag isEqualTo false }
            }
        } catch (e: DataIntegrityViolationException) {
            val message = e.message.orEmpty().lowercase()
            if (message.contains("username")) {
                throw BizException(BizException.BUSINESS_FAILED, "用户名已存在")
            }
            if (message.contains("email")) {
                throw BizException(BizException.BUSINESS_FAILED, "邮箱已存在")
            }
            throw BizException(BizException.SYSTEM_FAILED, "修改用户信息失败")
        }

        replaceUserRoles(userId, roleRecords)
        log.info("修改用户信息成功 userId={}, roleCount={}", userId, roleRecords.size)

        return AdminUserUpdateResult(
            userId = userId,
            roleCount = roleRecords.size,
        )
    }

    fun getUserDetail(userId: Long): AdminUserDetailResult {
        val user = usersMapper.selectOne {
            where { UsersDynamicSqlSupport.Users.id isEqualTo userId }
            and { UsersDynamicSqlSupport.Users.delFlag isEqualTo false }
        } ?: throw BizException(BizException.BUSINESS_FAILED, "用户不存在")
        val department = user.deptId?.let { getUserDepartment(it) }
        val roles = listUserRoles(userId)
        val quota = getUserQuotaConfig(userId)
        val modelPermissions = user.deptId?.let { buildEffectivePermissionView(it) }.orEmpty()

        return AdminUserDetailResult(
            user = mapAdminUserBaseInfo(user),
            department = department,
            roles = roles,
            modelPermissions = modelPermissions,
            quota = quota,
        )
    }

    fun listUsers(params: AdminUserPageParams): PageResult<AdminUserPageItemResult> {
        val mobile = params.mobile?.trim()?.ifBlank { null }
        val email = params.email?.trim()?.lowercase()?.ifBlank { null }
        val deptIds = params.deptId?.let { departmentService.listSubtreeDeptIds(it) }

        PageMethod.startPage<UsersRecord>(params.pageNum, params.pageSize)
        val records = usersMapper.select {
            where { UsersDynamicSqlSupport.Users.delFlag isEqualTo false }
            if (deptIds != null) {
                and { UsersDynamicSqlSupport.Users.deptId isIn deptIds }
            }
            and { UsersDynamicSqlSupport.Users.mobile isEqualToWhenPresent mobile }
            and { UsersDynamicSqlSupport.Users.email isEqualToWhenPresent email }
            orderBy(UsersDynamicSqlSupport.Users.id.descending())
        }
        val pageInfo = PageInfo.of(records)
        val departmentNames = listDepartmentNames(records)

        return PageResult(
            pageNum = pageInfo.pageNum,
            pageSize = pageInfo.pageSize,
            total = pageInfo.total,
            list = records.map { mapAdminUserPageItem(it, departmentNames) },
        )
    }

    @Transactional(rollbackFor = [Exception::class])
    fun replaceDepartmentPermissions(
        deptId: Long,
        params: DepartmentPermissionsUpdateParams,
        operator: String?,
    ): DepartmentPermissionsUpdateResult {
        departmentService.ensureDeptExists(deptId)

        val existing = departmentModelPermissionsMapper.select {
            where { DepartmentModelPermissionsDynamicSqlSupport.DepartmentModelPermissions.deptId isEqualTo deptId }
        }
        if (params.items.isEmpty()) {
            return clearDepartmentPermissions(deptId, existing, operator)
        }

        val normalizedItems = params.items.map { item ->
            val scopeEnum = DepartmentPermissionScope.parse(item.scope)
                ?: throw BizException(BizException.BUSINESS_FAILED, "作用域仅支持 SELF 或 SUBTREE")
            DepartmentPermissionUpdateItemDto(
                modelAlias = item.modelAlias.trim(),
                scope = scopeEnum.value,
                status = item.status ?: NormalStatus,
            )
        }.distinct()

        val modelAliases = normalizedItems.map { it.modelAlias }.distinct()
        val models = modelsMapper.select {
            where { ModelsDynamicSqlSupport.Models.modelAlias isIn modelAliases }
        }
        if (models.size != modelAliases.size) {
            throw BizException(BizException.BUSINESS_FAILED, "存在无效或未启用的模型别名")
        }
        val modelByAlias = models.associateBy { it.modelAlias!! }

        val desiredPermissions = normalizedItems.map { item ->
            val modelId =
                modelByAlias[item.modelAlias]?.id ?: throw BizException(BizException.BUSINESS_FAILED, "模型不存在")
            DepartmentPermissionUpdateItemDto(
                modelAlias = item.modelAlias,
                modelId = modelId,
                scope = item.scope,
                status = item.status,
            )
        }
        val desiredByRule = desiredPermissions.associateBy { PermissionRuleKey(it.modelId, it.scope) }
        if (desiredByRule.size != desiredPermissions.size) {
            throw BizException(BizException.BUSINESS_FAILED, "同一模型和作用域不能重复配置")
        }

        val now = Date()
        var added = 0
        var removed = 0
        var updated = 0
        val existingByRule = existing.associateBy { PermissionRuleKey(it.modelId ?: -1L, it.scope ?: "") }

        desiredByRule.forEach { (ruleKey, item) ->
            val existed = existingByRule[ruleKey]
            if (existed == null) {
                departmentModelPermissionsMapper.insert(
                    DepartmentModelPermissionsRecord(
                        deptId = deptId,
                        modelId = item.modelId,
                        scope = item.scope,
                        status = item.status,
                        createdBy = operator,
                        createdTime = now,
                        updatedBy = operator,
                        updatedTime = now,
                    )
                )
                added++
                return@forEach
            }
            if (existed.status != item.status) {
                existed.status = item.status
                existed.updatedBy = operator
                existed.updatedTime = now
                departmentModelPermissionsMapper.updateByPrimaryKeySelective(existed)
                updated++
            }
        }

        existing.forEach { record ->
            val modelId = record.modelId ?: return@forEach
            val scope = record.scope ?: return@forEach
            val ruleKey = PermissionRuleKey(modelId, scope)
            if (ruleKey !in desiredByRule.keys) {
                val recordId = record.id ?: return@forEach
                departmentModelPermissionsMapper.delete {
                    where { DepartmentModelPermissionsDynamicSqlSupport.DepartmentModelPermissions.id isEqualTo recordId }
                }
                removed++
            }
        }

        return DepartmentPermissionsUpdateResult(
            deptId = deptId,
            added = added,
            removed = removed,
            updated = updated,
        )
    }

    /**
     * 清空部门直接配置的全部模型权限。
     */
    private fun clearDepartmentPermissions(
        deptId: Long,
        existing: List<DepartmentModelPermissionsRecord>,
        operator: String?,
    ): DepartmentPermissionsUpdateResult {
        var removed = 0
        existing.forEach { record ->
            val recordId = record.id ?: return@forEach
            departmentModelPermissionsMapper.delete {
                where { DepartmentModelPermissionsDynamicSqlSupport.DepartmentModelPermissions.id isEqualTo recordId }
            }
            removed++
        }

        return DepartmentPermissionsUpdateResult(
            deptId = deptId,
            added = 0,
            removed = removed,
            updated = 0,
        )
    }

    /**
     * 批量查询用户所属部门名称。
     */
    private fun listDepartmentNames(records: List<UsersRecord>): Map<Long, String> {
        val deptIds = records.mapNotNull { it.deptId }.distinct()
        if (deptIds.isEmpty()) return emptyMap()

        return departmentMapper.select {
            where { DepartmentDynamicSqlSupport.Department.id isIn deptIds }
        }.mapNotNull { department ->
            val deptId = department.id ?: return@mapNotNull null
            deptId to (department.deptName ?: "")
        }.toMap()
    }

    /**
     * 校验用户存在且未删除。
     */
    private fun ensureUserExists(userId: Long): UsersRecord {
        return usersMapper.selectOne {
            where { UsersDynamicSqlSupport.Users.id isEqualTo userId }
            and { UsersDynamicSqlSupport.Users.delFlag isEqualTo false }
        } ?: throw BizException(BizException.BUSINESS_FAILED, "用户不存在")
    }

    /**
     * 校验角色标识合法，并返回对应角色记录。
     */
    private fun listValidRoles(roleKeys: List<String>): List<RolesRecord> {
        val normalizedRoleKeys = roleKeys
            .map { it.trim().lowercase() }
            .filter { it.isNotBlank() }
            .distinct()
        if (normalizedRoleKeys.isEmpty()) {
            throw BizException(BizException.BUSINESS_FAILED, "角色列表不能为空")
        }

        val roleRecords = rolesMapper.select {
            where { RolesDynamicSqlSupport.Roles.roleKey isIn normalizedRoleKeys }
        }
        if (roleRecords.size != normalizedRoleKeys.size) {
            throw BizException(BizException.BUSINESS_FAILED, "存在非法角色标识")
        }
        return roleRecords
    }

    /**
     * 覆盖用户角色关联关系。
     */
    private fun replaceUserRoles(userId: Long, roleRecords: List<RolesRecord>) {
        userRoleRelMapper.delete {
            where { UserRoleRelDynamicSqlSupport.UserRoleRel.userId isEqualTo userId }
        }
        insertUserRoles(userId, roleRecords)
    }

    /**
     * 新增用户角色关联关系。
     */
    private fun insertUserRoles(userId: Long, roleRecords: List<RolesRecord>) {
        val userRoleRecords = roleRecords.map { role ->
            UserRoleRelRecord(
                userId = userId,
                roleId = role.id ?: throw BizException(BizException.SYSTEM_FAILED, "角色ID异常"),
            )
        }
        userRoleRelMapper.insertMultiple(userRoleRecords)
    }

    /**
     * 转换管理端用户分页列表项。
     */
    private fun mapAdminUserPageItem(
        record: UsersRecord,
        departmentNames: Map<Long, String>,
    ): AdminUserPageItemResult {
        val userId = record.id ?: throw BizException(BizException.SYSTEM_FAILED, "用户ID异常")
        val deptId = record.deptId
        return AdminUserPageItemResult(
            userId = userId,
            name = record.name,
            username = record.username.orEmpty(),
            mobile = record.mobile,
            deptId = deptId,
            deptName = deptId?.let { departmentNames[it] },
            email = record.email,
            gender = record.gender ?: 0,
            status = record.status ?: 0,
            createdTime = record.createdTime,
        )
    }

    /**
     * 转换用户表完整基础信息，排除密码哈希等敏感字段。
     */
    private fun mapAdminUserBaseInfo(record: UsersRecord): AdminUserBaseInfoResult {
        val userId = record.id ?: throw BizException(BizException.SYSTEM_FAILED, "用户ID异常")
        return AdminUserBaseInfoResult(
            userId = userId,
            name = record.name,
            deptId = record.deptId,
            username = record.username,
            email = record.email,
            mobile = record.mobile,
            gender = record.gender,
            avatarUrl = record.avatarUrl,
            passwordChanged = record.passwordChanged,
            remark = record.remark,
            status = record.status,
            delFlag = record.delFlag,
            createdTime = record.createdTime,
            updatedTime = record.updatedTime,
        )
    }

    /**
     * 查询用户绑定部门详情。
     */
    private fun getUserDepartment(deptId: Long): AdminUserDepartmentResult? {
        return departmentMapper.selectOne {
            where { DepartmentDynamicSqlSupport.Department.id isEqualTo deptId }
            and { DepartmentDynamicSqlSupport.Department.delFlag isEqualTo false }
        }?.let { mapUserDepartment(it) }
    }

    /**
     * 转换用户关联部门信息。
     */
    private fun mapUserDepartment(record: DepartmentRecord): AdminUserDepartmentResult {
        return AdminUserDepartmentResult(
            deptId = record.id ?: throw BizException(BizException.SYSTEM_FAILED, "部门ID异常"),
            parentId = record.parentId ?: 0L,
            deptName = record.deptName.orEmpty(),
            orderNum = record.orderNum ?: 0,
            leaderName = record.leaderName,
            tel = record.tel,
            status = record.status ?: 0,
        )
    }

    /**
     * 查询用户关联角色列表。
     */
    private fun listUserRoles(userId: Long): List<RoleListItemResult> {
        val roleIds = userRoleRelMapper.select {
            where { UserRoleRelDynamicSqlSupport.UserRoleRel.userId isEqualTo userId }
        }.mapNotNull { it.roleId }.distinct()
        if (roleIds.isEmpty()) return emptyList()

        return rolesMapper.select {
            where { RolesDynamicSqlSupport.Roles.id isIn roleIds }
            orderBy(RolesDynamicSqlSupport.Roles.roleSort, RolesDynamicSqlSupport.Roles.id)
        }.map { mapRoleListItem(it) }
    }

    /**
     * 转换角色列表项。
     */
    private fun mapRoleListItem(record: RolesRecord): RoleListItemResult {
        return RoleListItemResult(
            id = record.id ?: throw BizException(BizException.SYSTEM_FAILED, "角色ID异常"),
            roleName = record.roleName.orEmpty(),
            roleKey = record.roleKey.orEmpty(),
            roleSort = record.roleSort ?: 0,
            createdBy = record.createdBy,
            createdTime = record.createdTime,
        )
    }

    /**
     * 查询用户配额配置详情，未配置时返回空。
     */
    private fun getUserQuotaConfig(userId: Long): AdminUserQuotaConfigResult? {
        return userQuotaAccountsMapper.selectOne {
            where { UserQuotaAccountsDynamicSqlSupport.UserQuotaAccounts.userId isEqualTo userId }
        }?.let { mapUserQuotaConfig(it) } ?: emptyQuota(userId)
    }

    private fun emptyQuota(userId: Long): AdminUserQuotaConfigResult? {
        return AdminUserQuotaConfigResult(
            userId = userId,
            currentQuotaAmount = ZERO_AMOUNT,
            availableAmount = ZERO_AMOUNT,
            usedAmount = ZERO_AMOUNT,
            expiredAmount = ZERO_AMOUNT,
            transferredInAmount = ZERO_AMOUNT,
            transferredOutAmount = ZERO_AMOUNT,
            allowTransferOut = false,
            earliestExpireAt = null,
            updatedAt = null
        )
    }

    /**
     * 转换用户配额账户快照。
     */
    private fun mapUserQuotaConfig(record: UserQuotaAccountsRecord): AdminUserQuotaConfigResult {
        return AdminUserQuotaConfigResult(
            userId = record.userId ?: 0L,
            currentQuotaAmount = record.currentQuotaAmount ?: ZERO_AMOUNT,
            availableAmount = record.availableAmount ?: ZERO_AMOUNT,
            usedAmount = record.usedAmount ?: ZERO_AMOUNT,
            expiredAmount = record.expiredAmount ?: ZERO_AMOUNT,
            transferredInAmount = record.transferredInAmount ?: ZERO_AMOUNT,
            transferredOutAmount = record.transferredOutAmount ?: ZERO_AMOUNT,
            allowTransferOut = record.allowTransferOut ?: false,
            earliestExpireAt = record.earliestExpireAt,
            updatedAt = record.updatedTime,
        )
    }

    fun getDepartmentPermissions(deptId: Long, view: String): DepartmentPermissionsViewResult {
        departmentService.ensureDeptExists(deptId)
        val normalizedView = view.trim().lowercase()
        return when (normalizedView) {
            "direct" -> DepartmentPermissionsViewResult(
                deptId = deptId,
                models = buildDirectPermissionView(deptId),
            )

            "effective" -> DepartmentPermissionsViewResult(
                deptId = deptId,
                models = buildEffectivePermissionView(deptId),
            )

            else -> throw BizException(BizException.BUSINESS_FAILED, "view 仅支持 direct 或 effective")
        }
    }

    fun getUserEffectivePermissions(userId: Long): UserEffectivePermissionsResult {
        val user = usersMapper.selectOne {
            where { UsersDynamicSqlSupport.Users.id isEqualTo userId }
            and { UsersDynamicSqlSupport.Users.delFlag isEqualTo false }
            and { UsersDynamicSqlSupport.Users.status isEqualTo NormalStatus }
        } ?: throw BizException(BizException.BUSINESS_FAILED, "用户不存在或已禁用")

        val deptId = user.deptId ?: throw BizException(BizException.BUSINESS_FAILED, "用户未绑定部门")
        val allowedModels = resolveEffectiveModelAliases(deptId)
        return UserEffectivePermissionsResult(
            userId = userId,
            deptId = deptId,
            allowedModels = allowedModels,
        )
    }

    fun checkUserCanAccessModel(userId: Long, modelAlias: String) {
        val allowed = getUserEffectivePermissions(userId).allowedModels.toSet()
        if (!allowed.contains(modelAlias.trim())) {
            throw BizException(BizException.BUSINESS_FAILED, "RBAC_MODEL_FORBIDDEN")
        }
    }

    /**
     * 查询指定部门的直接授权视图，返回该部门自身配置的模型权限及启停状态。
     */
    private fun buildDirectPermissionView(deptId: Long): List<DepartmentPermissionViewItem> {
        val directPermissions = departmentModelPermissionsMapper.select {
            where { DepartmentModelPermissionsDynamicSqlSupport.DepartmentModelPermissions.deptId isEqualTo deptId }
        }
        if (directPermissions.isEmpty()) {
            return emptyList()
        }
        val modelMetaMap = queryModelMetaMap(directPermissions.mapNotNull { it.modelId }.distinct())
        val departmentNames = listDepartmentNamesByIds(listOf(deptId))
        return directPermissions.mapNotNull { record ->
            val modelMeta = modelMetaMap[record.modelId] ?: return@mapNotNull null
            DepartmentPermissionViewItem(
                modelAlias = modelMeta.modelAlias,
                realModelName = modelMeta.realModelName,
                vendorId = modelMeta.vendorId,
                vendorName = modelMeta.vendorName,
                billingType = modelMeta.billingType,
                active = modelMeta.active,
                sourceDeptId = deptId,
                sourceDeptName = departmentNames[deptId].orEmpty(),
                scope = record.scope ?: DepartmentPermissionScope.SELF.value,
                status = record.status ?: 0,
            )
        }.sortedBy { it.modelAlias }
    }

    /**
     * 计算指定部门的生效权限视图。
     * 规则：当前部门的 SELF/SUBTREE 均生效，祖先部门仅 SUBTREE 生效。
     */
    private fun buildEffectivePermissionView(deptId: Long): List<DepartmentPermissionViewItem> {
        val ancestorPath = resolveAncestorPath(deptId)

        val permissions = departmentModelPermissionsMapper.select {
            where { DepartmentModelPermissionsDynamicSqlSupport.DepartmentModelPermissions.deptId isIn ancestorPath }
            and { DepartmentModelPermissionsDynamicSqlSupport.DepartmentModelPermissions.status isEqualTo NormalStatus }
        }
        if (permissions.isEmpty()) {
            return emptyList()
        }

        val modelMetaMap = queryModelMetaMap(permissions.mapNotNull { it.modelId }.distinct())
        val departmentNames = listDepartmentNamesByIds(permissions.mapNotNull { it.deptId }.distinct())
        val distanceByDept = ancestorPath.withIndex().associate { it.value to it.index }
        val winnerByModel = linkedMapOf<Long, DepartmentPermissionViewItem>()

        permissions.sortedBy { distanceByDept[it.deptId] ?: Int.MAX_VALUE }.forEach { permission ->
            val sourceDeptId = permission.deptId ?: return@forEach
            val sourceScope = permission.scope ?: return@forEach
            val modelId = permission.modelId ?: return@forEach
            if (sourceDeptId != deptId && sourceScope != DepartmentPermissionScope.SUBTREE.value) {
                return@forEach
            }
            val modelMeta = modelMetaMap[modelId] ?: return@forEach
            winnerByModel[modelId] = DepartmentPermissionViewItem(
                modelAlias = modelMeta.modelAlias,
                realModelName = modelMeta.realModelName,
                vendorId = modelMeta.vendorId,
                vendorName = modelMeta.vendorName,
                billingType = modelMeta.billingType,
                active = modelMeta.active,
                sourceDeptId = sourceDeptId,
                sourceDeptName = departmentNames[sourceDeptId].orEmpty(),
                scope = sourceScope,
                status = permission.status ?: 0,
            )
        }

        return winnerByModel.values.sortedBy { it.modelAlias }
    }

    /**
     * 提取部门最终可访问的模型别名集合，并做去重与排序。
     */
    private fun resolveEffectiveModelAliases(deptId: Long): List<String> {
        return buildEffectivePermissionView(deptId)
            .map { it.modelAlias }
            .distinct()
            .sorted()
    }

    /**
     * 从部门服务读取祖先路径（优先缓存），并返回根到当前节点顺序。
     */
    private fun resolveAncestorPath(startDeptId: Long): List<Long> {
        return departmentService.getAncestorPath(startDeptId)
    }

    /**
     * 按模型ID批量查询模型元信息。
     */
    private fun queryModelMetaMap(modelIds: List<Long>): Map<Long, ModelMetaDto> {
        if (modelIds.isEmpty()) {
            return emptyMap()
        }
        val models = modelsMapper.select {
            where { ModelsDynamicSqlSupport.Models.id isIn modelIds }
        }
        val vendorNames = listVendorNames(models.mapNotNull { it.vendorId }.distinct())
        return models.mapNotNull { model ->
            val id = model.id ?: return@mapNotNull null
            val modelAlias = model.modelAlias ?: return@mapNotNull null
            val realModelName = model.realModelName ?: return@mapNotNull null
            val vendorId = model.vendorId ?: return@mapNotNull null
            val billingType = model.billingType ?: return@mapNotNull null
            id to ModelMetaDto(
                modelAlias = modelAlias,
                realModelName = realModelName,
                vendorId = vendorId,
                vendorName = vendorNames[vendorId].orEmpty(),
                billingType = billingType,
                active = model.active ?: false,
            )
        }.toMap()
    }

    /**
     * 批量查询部门名称。
     */
    private fun listDepartmentNamesByIds(deptIds: List<Long>): Map<Long, String> {
        if (deptIds.isEmpty()) return emptyMap()

        return departmentMapper.select {
            where { DepartmentDynamicSqlSupport.Department.id isIn deptIds }
        }.mapNotNull { department ->
            val deptId = department.id ?: return@mapNotNull null
            deptId to department.deptName.orEmpty()
        }.toMap()
    }

    /**
     * 批量查询供应商名称。
     */
    private fun listVendorNames(vendorIds: List<Long>): Map<Long, String> {
        if (vendorIds.isEmpty()) return emptyMap()

        return vendorsMapper.select {
            where { VendorsDynamicSqlSupport.Vendors.id isIn vendorIds }
        }.mapNotNull { vendor ->
            val vendorId = vendor.id ?: return@mapNotNull null
            vendorId to vendor.name.orEmpty()
        }.toMap()
    }

    private data class DepartmentPermissionUpdateItemDto(
        val modelAlias: String,
        val modelId: Long = 0L,
        val scope: String,
        val status: Int,
    )

    private data class PermissionRuleKey(
        val modelId: Long,
        val scope: String,
    )
}
