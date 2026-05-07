package com.llm.gateway.security

import com.llm.gateway.common.enums.NormalStatus
import com.llm.gateway.dal.model.UsersRecord
import net.sf.jsqlparser.util.validation.metadata.NamedObject
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.UserDetails

class CustomUserDetails(
    private val user: UsersRecord,
    private val roles: List<String>,
) : UserDetails {

    companion object {
        const val AUTHORITY_PREFIX = "ROLE_"
    }

    override fun getAuthorities(): Collection<GrantedAuthority> {
        return roles.map {
            SimpleGrantedAuthority(if (it.startsWith(AUTHORITY_PREFIX)) it else AUTHORITY_PREFIX + it)
        }
    }

    override fun getPassword(): String? = user.password

    override fun getUsername(): String? = user.username ?: user.email

    override fun isAccountNonExpired(): Boolean = true

    override fun isAccountNonLocked(): Boolean = true

    override fun isCredentialsNonExpired(): Boolean = true

    override fun isEnabled(): Boolean = user.status == NormalStatus && user.delFlag != true

    fun getUser(): UsersRecord = user
}
