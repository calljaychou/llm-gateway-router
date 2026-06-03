package com.llm.gateway.security

import com.llm.gateway.common.exceptions.BizException
import org.springframework.security.core.Authentication

object SecurityUtil {

    fun getCurrentUserDetails(authentication: Authentication): CustomUserDetails? {
        return authentication.principal as? CustomUserDetails
    }

    fun getRequiredUserDetails(authentication: Authentication): CustomUserDetails {
        return getCurrentUserDetails(authentication)
            ?: throw BizException(BizException.UNAUTHORIZED, "未认证")
    }

    fun getRequiredUserId(authentication: Authentication): Long {
        val principal = getRequiredUserDetails(authentication)
        return principal.getUser().id ?: throw BizException(BizException.UNAUTHORIZED, "用户ID缺失")
    }

    fun getCurrentUserId(authentication: Authentication): Long? {
        val principal = getCurrentUserDetails(authentication) ?: return null
        return principal.getUser().id ?: throw BizException(BizException.UNAUTHORIZED, "用户ID缺失")
    }
}
