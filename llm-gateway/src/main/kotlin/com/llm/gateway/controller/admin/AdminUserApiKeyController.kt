package com.llm.gateway.controller.admin

import com.llm.gateway.common.exceptions.BizException
import com.llm.gateway.model.ApiResult
import com.llm.gateway.model.params.ApiKeyCreateParams
import com.llm.gateway.model.results.ApiKeyCreateResult
import com.llm.gateway.model.results.ApiKeyListResult
import com.llm.gateway.model.results.ApiKeyRevokeResult
import com.llm.gateway.security.CustomUserDetails
import com.llm.gateway.service.VirtualApiKeyService
import io.swagger.annotations.Api
import io.swagger.annotations.ApiOperation
import javax.validation.Valid
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@Api(tags = ["用户端-虚拟密钥管理"])
@RestController
@RequestMapping("/admin")
class AdminUserApiKeyController(
    private val virtualApiKeyService: VirtualApiKeyService,
) {
    @ApiOperation("创建虚拟密钥")
    @PostMapping("/user/keys")
    fun createApiKey(
        @RequestBody @Valid params: ApiKeyCreateParams,
        authentication: Authentication,
    ): ApiResult<ApiKeyCreateResult> {
        val userId = requireUserId(authentication)
        return ApiResult.Companion.success(virtualApiKeyService.createApiKey(userId, params))
    }

    @ApiOperation("查询虚拟密钥列表")
    @GetMapping("/user/keys")
    fun listApiKeys(authentication: Authentication): ApiResult<ApiKeyListResult> {
        val userId = requireUserId(authentication)
        return ApiResult.Companion.success(virtualApiKeyService.listApiKeys(userId))
    }

    @ApiOperation("吊销虚拟密钥")
    @DeleteMapping("/user/keys/{id}")
    fun revokeApiKey(
        @PathVariable("id") keyId: Long,
        authentication: Authentication,
    ): ApiResult<ApiKeyRevokeResult> {
        val userId = requireUserId(authentication)
        return ApiResult.Companion.success(virtualApiKeyService.revokeApiKey(userId, keyId))
    }

    private fun requireUserId(authentication: Authentication): Long {
        val principal = authentication.principal as? CustomUserDetails
            ?: throw BizException(BizException.Companion.UNAUTHORIZED, "未认证")
        return principal.getUser().id ?: throw BizException(BizException.Companion.UNAUTHORIZED, "用户ID缺失")
    }
}
