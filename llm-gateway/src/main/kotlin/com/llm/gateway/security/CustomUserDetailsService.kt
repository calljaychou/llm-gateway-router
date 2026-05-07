package com.llm.gateway.security

import com.llm.gateway.common.enums.NormalStatus
import com.llm.gateway.dal.mapper.RolesDynamicSqlSupport
import com.llm.gateway.dal.mapper.RolesMapper
import com.llm.gateway.dal.mapper.UserRoleRelDynamicSqlSupport
import com.llm.gateway.dal.mapper.UserRoleRelMapper
import com.llm.gateway.dal.mapper.UsersDynamicSqlSupport
import com.llm.gateway.dal.mapper.UsersMapper
import com.llm.gateway.dal.mapper.select
import com.llm.gateway.dal.mapper.selectOne
import com.llm.gateway.dal.model.UsersRecord
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.stereotype.Service

@Service
class CustomUserDetailsService(
    private val usersMapper: UsersMapper,
    private val userRoleRelMapper: UserRoleRelMapper,
    private val rolesMapper: RolesMapper,
) : UserDetailsService {

    override fun loadUserByUsername(username: String): UserDetails {
        val user = findUserByUsernameOrEmail(username)
            ?: throw UsernameNotFoundException("User not found: $username")

        if (user.delFlag == true || user.status != NormalStatus) {
            throw UsernameNotFoundException("User account is disabled or deleted: $username")
        }

        val roleIds = userRoleRelMapper.select {
            where { UserRoleRelDynamicSqlSupport.UserRoleRel.userId isEqualTo user.id!! }
        }.map { it.roleId!! }

        val roles = if (roleIds.isEmpty()) emptyList() else rolesMapper.select {
            where { RolesDynamicSqlSupport.Roles.id isIn roleIds }
        }.mapNotNull { it.roleKey }

        return CustomUserDetails(user, roles)
    }

    fun loadUserById(userId: Long, _roles: List<String>): CustomUserDetails {
        val user = usersMapper.selectOne {
            where { UsersDynamicSqlSupport.Users.id isEqualTo userId }
        } ?: throw UsernameNotFoundException("User not found: $userId")

        if (user.delFlag == true || user.status != NormalStatus) {
            throw UsernameNotFoundException("User account is disabled or deleted: $userId")
        }

        val latestRoles = userRoleRelMapper.select {
            where { UserRoleRelDynamicSqlSupport.UserRoleRel.userId isEqualTo userId }
        }.mapNotNull { it.roleId }.takeIf { it.isNotEmpty() }?.let { roleIds ->
            rolesMapper.select {
                where { RolesDynamicSqlSupport.Roles.id isIn roleIds }
            }.mapNotNull { it.roleKey }
        } ?: emptyList()

        return CustomUserDetails(user, latestRoles)
    }

    private fun findUserByUsernameOrEmail(username: String): UsersRecord? {
        return usersMapper.selectOne {
            where { UsersDynamicSqlSupport.Users.username isEqualTo username }
            or { UsersDynamicSqlSupport.Users.email isEqualTo username }
        }
    }
}
