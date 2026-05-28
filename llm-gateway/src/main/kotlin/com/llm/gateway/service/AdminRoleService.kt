package com.llm.gateway.service

import com.llm.gateway.common.exceptions.BizException
import com.llm.gateway.common.logger
import com.llm.gateway.dal.mapper.RolesDynamicSqlSupport
import com.llm.gateway.dal.mapper.RolesMapper
import com.llm.gateway.dal.mapper.UserRoleRelDynamicSqlSupport
import com.llm.gateway.dal.mapper.UserRoleRelMapper
import com.llm.gateway.dal.mapper.count
import com.llm.gateway.dal.mapper.delete
import com.llm.gateway.dal.mapper.deleteByPrimaryKey
import com.llm.gateway.dal.mapper.insert
import com.llm.gateway.dal.mapper.select
import com.llm.gateway.dal.mapper.selectOne
import com.llm.gateway.dal.model.RolesRecord
import com.llm.gateway.model.params.RoleCreateParams
import com.llm.gateway.model.results.RoleCreateResult
import com.llm.gateway.model.results.RoleDeleteResult
import com.llm.gateway.model.results.RoleListItemResult
import java.util.Date
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class AdminRoleService(
    private val rolesMapper: RolesMapper,
    private val userRoleRelMapper: UserRoleRelMapper,
) {

    private val log = logger()

    fun listRoles(): List<RoleListItemResult> {
        return rolesMapper.select {
            orderBy(
                RolesDynamicSqlSupport.Roles.roleSort,
                RolesDynamicSqlSupport.Roles.id.descending(),
            )
        }.map { mapRoleListItem(it) }
    }

    @Transactional(rollbackFor = [Exception::class])
    fun createRole(params: RoleCreateParams, operator: String?): RoleCreateResult {
        val roleName = params.roleName.trim()
        val roleKey = params.roleKey.trim().lowercase()
        checkRoleKeyUnique(roleKey)

        val now = Date()
        val roleRecord = RolesRecord(
            roleName = roleName,
            roleKey = roleKey,
            roleSort = params.roleSort ?: 0,
            createdBy = operator,
            createdTime = now,
            updatedBy = operator,
            updatedTime = now,
        )

        try {
            rolesMapper.insert(roleRecord)
        } catch (e: DataIntegrityViolationException) {
            log.warn("创建角色数据冲突 roleKey={}", roleKey, e)
            throw BizException(BizException.BUSINESS_FAILED, "角色标识已存在")
        }

        val roleId = roleRecord.id ?: throw BizException(BizException.SYSTEM_FAILED, "创建角色失败")
        log.info("创建角色成功 roleId={}, roleKey={}", roleId, roleKey)

        return RoleCreateResult(
            roleId = roleId,
            roleName = roleName,
            roleKey = roleKey,
        )
    }

    @Transactional(rollbackFor = [Exception::class])
    fun deleteRole(roleId: Long): RoleDeleteResult {
        ensureRoleExists(roleId)

        val removedUserRoleRelCount = userRoleRelMapper.count {
            where { UserRoleRelDynamicSqlSupport.UserRoleRel.roleId isEqualTo roleId }
        }
        if (removedUserRoleRelCount > 0L) {
            // 删除角色前先清理用户角色关联关系，避免产生无效授权。
            userRoleRelMapper.delete {
                where { UserRoleRelDynamicSqlSupport.UserRoleRel.roleId isEqualTo roleId }
            }
        }
        rolesMapper.deleteByPrimaryKey(roleId)
        log.info("删除角色成功 roleId={}, removedUserRoleRelCount={}", roleId, removedUserRoleRelCount)

        return RoleDeleteResult(
            roleId = roleId,
            deleted = true,
            removedUserRoleRelCount = removedUserRoleRelCount,
        )
    }

    /**
     * 校验角色标识唯一，避免创建重复系统角色。
     */
    private fun checkRoleKeyUnique(roleKey: String) {
        val existing = rolesMapper.selectOne {
            where { RolesDynamicSqlSupport.Roles.roleKey isEqualTo roleKey }
        }
        if (existing != null) throw BizException(BizException.BUSINESS_FAILED, "角色标识已存在")
    }

    /**
     * 校验角色存在，不存在时抛出业务异常。
     */
    private fun ensureRoleExists(roleId: Long): RolesRecord {
        return rolesMapper.selectOne {
            where { RolesDynamicSqlSupport.Roles.id isEqualTo roleId }
        } ?: throw BizException(BizException.BUSINESS_FAILED, "角色不存在")
    }

    /**
     * 转换管理端角色列表项。
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
}
