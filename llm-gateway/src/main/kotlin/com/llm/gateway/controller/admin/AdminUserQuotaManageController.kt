package com.llm.gateway.controller.admin

import com.llm.gateway.common.exceptions.BizException
import com.llm.gateway.model.ApiResult
import com.llm.gateway.model.PageParams
import com.llm.gateway.model.PageResult
import com.llm.gateway.model.params.AdminUserQuotaAdjustmentParams
import com.llm.gateway.model.results.UserQuotaAccountResult
import com.llm.gateway.model.results.UserQuotaGrantResult
import com.llm.gateway.security.CustomUserDetails
import com.llm.gateway.service.UserQuotaService
import io.swagger.annotations.Api
import io.swagger.annotations.ApiOperation
import io.swagger.annotations.ApiParam
import javax.validation.Valid
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@Api(tags = ["管理端-用户配额"])
@RestController
@RequestMapping("/admin")
@PreAuthorize("hasAnyRole('admin','llm-lead')")
class AdminUserQuotaManageController(
    private val userQuotaService: UserQuotaService,
) {

    @ApiOperation("查询用户当前配额账户快照")
    @GetMapping("/users/{id}/quota")
    fun getUserQuota(
        @ApiParam(value = "用户ID", required = true) @PathVariable("id") userId: Long,
    ): ApiResult<UserQuotaAccountResult> {
        return ApiResult.success(userQuotaService.getCurrentQuota(userId))
    }

    @ApiOperation("查询用户已过期配额列表")
    @GetMapping("/users/{id}/quota/expired-grants")
    fun listExpiredGrants(
        @ApiParam(value = "用户ID", required = true) @PathVariable("id") userId: Long,
        @Valid params: PageParams,
    ): ApiResult<PageResult<UserQuotaGrantResult>> {
        return ApiResult.success(userQuotaService.listExpiredGrants(userId, params))
    }

    @ApiOperation("管理员调整用户配额")
    @PostMapping("/users/{id}/quota/adjustments")
    fun adjustUserQuota(
        @ApiParam(value = "用户ID", required = true) @PathVariable("id") userId: Long,
        @RequestBody @Valid params: AdminUserQuotaAdjustmentParams,
        authentication: Authentication,
    ): ApiResult<UserQuotaAccountResult> {
        return ApiResult.success(
            userQuotaService.adjustQuota(
                userId = userId,
                params = params,
                operatorUserId = currentUserId(authentication),
            )
        )
    }

    private fun currentUserId(authentication: Authentication): Long? {
        val principal = authentication.principal as? CustomUserDetails ?: return null
        return principal.getUser().id ?: throw BizException(BizException.UNAUTHORIZED, "用户ID缺失")
    }
}
