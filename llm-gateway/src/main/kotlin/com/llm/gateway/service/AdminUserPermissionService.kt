package com.llm.gateway.service

import com.llm.gateway.common.enums.DepartmentPermissionScope
import com.llm.gateway.common.enums.NormalStatus
import com.llm.gateway.common.exceptions.BizException
import com.llm.gateway.dal.mapper.DepartmentModelPermissionsDynamicSqlSupport
import com.llm.gateway.dal.mapper.DepartmentModelPermissionsMapper
import com.llm.gateway.dal.mapper.ModelsDynamicSqlSupport
import com.llm.gateway.dal.mapper.ModelsMapper
import com.llm.gateway.dal.mapper.RolesDynamicSqlSupport
import com.llm.gateway.dal.mapper.RolesMapper
import com.llm.gateway.dal.mapper.UserRoleRelMapper
import com.llm.gateway.dal.mapper.UsersDynamicSqlSupport
import com.llm.gateway.dal.mapper.UsersMapper
import com.llm.gateway.dal.mapper.insert
import com.llm.gateway.dal.mapper.insertMultiple
import com.llm.gateway.dal.mapper.select
import com.llm.gateway.dal.mapper.selectOne
import com.llm.gateway.dal.mapper.updateByPrimaryKeySelective
import com.llm.gateway.dal.model.DepartmentModelPermissionsRecord
import com.llm.gateway.dal.model.UserRoleRelRecord
import com.llm.gateway.dal.model.UsersRecord
import com.llm.gateway.model.dto.ModelMetaDto
import com.llm.gateway.model.dto.PermissionPairDto
import com.llm.gateway.model.params.AdminUserCreateParams
import com.llm.gateway.model.params.DepartmentPermissionsUpdateParams
import com.llm.gateway.model.results.AdminUserCreateResult
import com.llm.gateway.model.results.DepartmentPermissionViewItem
import com.llm.gateway.model.results.DepartmentPermissionsUpdateResult
import com.llm.gateway.model.results.DepartmentPermissionsViewResult
import com.llm.gateway.model.results.UserEffectivePermissionsResult
import java.util.Date
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class AdminUserPermissionService(
    private val usersMapper: UsersMapper,
    private val departmentService: DepartmentService,
    private val rolesMapper: RolesMapper,
    private val userRoleRelMapper: UserRoleRelMapper,
    private val modelsMapper: ModelsMapper,
    private val departmentModelPermissionsMapper: DepartmentModelPermissionsMapper,
    private val passwordEncoder: PasswordEncoder,
) {

    @Transactional(rollbackFor = [Exception::class])
    fun createUser(params: AdminUserCreateParams): AdminUserCreateResult {
        val deptId = params.deptId ?: throw BizException(BizException.BUSINESS_FAILED, "部门ID不能为空")
        departmentService.ensureDeptExists(deptId)

        val normalizedRoleKeys = params.roleKeys
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

        val now = Date()
        val userRecord = UsersRecord(
            deptId = deptId,
            username = params.username.trim(),
            email = params.email.trim().lowercase(),
            mobile = params.mobile?.trim(),
            gender = 3,
            avatarUrl = null,
            password = passwordEncoder.encode(params.password),
            passwordChanged = !(params.forcePasswordChange ?: true),
            remark = null,
            status = NormalStatus,
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
        val userRoleRecords = roleRecords.map { role ->
            UserRoleRelRecord(
                userId = userId,
                roleId = role.id ?: throw BizException(BizException.SYSTEM_FAILED, "角色ID异常"),
            )
        }
        userRoleRelMapper.insertMultiple(userRoleRecords)

        return AdminUserCreateResult(
            userId = userId,
            passwordChanged = userRecord.passwordChanged ?: false,
        )
    }

    @Transactional(rollbackFor = [Exception::class])
    fun replaceDepartmentPermissions(
        deptId: Long,
        params: DepartmentPermissionsUpdateParams,
        operator: String?,
    ): DepartmentPermissionsUpdateResult {
        departmentService.ensureDeptExists(deptId)

        val normalizedItems = params.items.map { item ->
            val scopeEnum = DepartmentPermissionScope.parse(item.scope)
                ?: throw BizException(BizException.BUSINESS_FAILED, "作用域仅支持 SELF 或 SUBTREE")
            item.modelAlias.trim() to scopeEnum.value
        }.distinct()

        val modelAliases = normalizedItems.map { it.first }.distinct()
        val models = modelsMapper.select {
            where { ModelsDynamicSqlSupport.Models.modelAlias isIn modelAliases }
        }
        if (models.size != modelAliases.size) {
            throw BizException(BizException.BUSINESS_FAILED, "存在无效或未启用的模型别名")
        }
        val modelByAlias = models.associateBy { it.modelAlias!! }

        val desiredPairs = normalizedItems.map { pair ->
            val modelId = modelByAlias[pair.first]?.id ?: throw BizException(BizException.BUSINESS_FAILED, "模型不存在")
            PermissionPairDto(modelId, pair.second)
        }.toSet()

        val existing = departmentModelPermissionsMapper.select {
            where { DepartmentModelPermissionsDynamicSqlSupport.DepartmentModelPermissions.deptId isEqualTo deptId }
        }

        val now = Date()
        var added = 0
        var removed = 0
        var updated = 0
        val existingByPair = existing.associateBy { PermissionPairDto(it.modelId ?: -1L, it.scope ?: "") }

        desiredPairs.forEach { pair ->
            val existed = existingByPair[pair]
            if (existed == null) {
                departmentModelPermissionsMapper.insert(
                    DepartmentModelPermissionsRecord(
                        deptId = deptId,
                        modelId = pair.modelId,
                        scope = pair.scope,
                        status = NormalStatus,
                        createdBy = operator,
                        createdTime = now,
                        updatedBy = operator,
                        updatedTime = now,
                    )
                )
                added++
                return@forEach
            }
            if (existed.status != NormalStatus) {
                existed.status = NormalStatus
                existed.updatedBy = operator
                existed.updatedTime = now
                departmentModelPermissionsMapper.updateByPrimaryKeySelective(existed)
                updated++
            }
        }

        existing.forEach { record ->
            val modelId = record.modelId ?: return@forEach
            val scope = record.scope ?: return@forEach
            val pair = PermissionPairDto(modelId, scope)
            if (pair !in desiredPairs && record.status == NormalStatus) {
                record.status = 0
                record.updatedBy = operator
                record.updatedTime = now
                departmentModelPermissionsMapper.updateByPrimaryKeySelective(record)
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
     * 查询指定部门的直接授权视图，仅返回该部门自身配置且状态生效的模型权限。
     */
    private fun buildDirectPermissionView(deptId: Long): List<DepartmentPermissionViewItem> {
        val directPermissions = departmentModelPermissionsMapper.select {
            where { DepartmentModelPermissionsDynamicSqlSupport.DepartmentModelPermissions.deptId isEqualTo deptId }
            and { DepartmentModelPermissionsDynamicSqlSupport.DepartmentModelPermissions.status isEqualTo NormalStatus }
        }
        if (directPermissions.isEmpty()) {
            return emptyList()
        }
        val modelMetaMap = queryModelMetaMap(directPermissions.mapNotNull { it.modelId }.distinct())
        return directPermissions.mapNotNull { record ->
            val modelMeta = modelMetaMap[record.modelId] ?: return@mapNotNull null
            DepartmentPermissionViewItem(
                modelAlias = modelMeta.modelAlias,
                realModelName = modelMeta.realModelName,
                vendorId = modelMeta.vendorId,
                billingType = modelMeta.billingType,
                active = modelMeta.active,
                sourceDeptId = deptId,
                scope = record.scope ?: DepartmentPermissionScope.SELF.value,
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
                billingType = modelMeta.billingType,
                active = modelMeta.active,
                sourceDeptId = sourceDeptId,
                scope = sourceScope,
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
        return modelsMapper.select {
            where { ModelsDynamicSqlSupport.Models.id isIn modelIds }
        }.mapNotNull { model ->
            val id = model.id ?: return@mapNotNull null
            val modelAlias = model.modelAlias ?: return@mapNotNull null
            val realModelName = model.realModelName ?: return@mapNotNull null
            val vendorId = model.vendorId ?: return@mapNotNull null
            val billingType = model.billingType ?: return@mapNotNull null
            id to ModelMetaDto(
                modelAlias = modelAlias,
                realModelName = realModelName,
                vendorId = vendorId,
                billingType = billingType,
                active = model.active ?: false,
            )
        }.toMap()
    }
}
