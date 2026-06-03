package com.llm.gateway.controller.admin

import com.llm.gateway.model.ApiResult
import com.llm.gateway.model.PageParams
import com.llm.gateway.model.params.UserQuotaTransactionsPageParams
import com.llm.gateway.model.params.UserQuotaTransferParams
import com.llm.gateway.model.PageResult
import com.llm.gateway.model.results.UserQuotaAccountResult
import com.llm.gateway.model.results.UserQuotaGrantResult
import com.llm.gateway.model.results.UserQuotaTransactionResult
import com.llm.gateway.model.results.UserQuotaTransferResult
import com.llm.gateway.security.SecurityUtil
import com.llm.gateway.service.UserQuotaService
import io.swagger.annotations.Api
import io.swagger.annotations.ApiOperation
import javax.validation.Valid
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@Api(tags = ["用户端-配额"])
@RestController
@RequestMapping("/admin/user/quotas")
class AdminUserQuotaController(
    private val userQuotaService: UserQuotaService,
) {

    @ApiOperation("用户查看自己的当前配额")
    @GetMapping("/current")
    fun getCurrentQuota(authentication: Authentication): ApiResult<UserQuotaAccountResult> {
        return ApiResult.Companion.success(userQuotaService.getCurrentQuota(SecurityUtil.getRequiredUserId(authentication)))
    }

    @ApiOperation("用户查看自己的已过期配额列表")
    @GetMapping("/expired-grants")
    fun listExpiredGrants(
        @Valid params: PageParams,
        authentication: Authentication,
    ): ApiResult<PageResult<UserQuotaGrantResult>> {
        return ApiResult.Companion.success(userQuotaService.listExpiredGrants(SecurityUtil.getRequiredUserId(authentication), params))
    }

    @ApiOperation("查询当前用户的配额流水")
    @GetMapping("/transactions")
    fun listTransactions(
        @Valid params: UserQuotaTransactionsPageParams,
        authentication: Authentication,
    ): ApiResult<PageResult<UserQuotaTransactionResult>> {
        return ApiResult.Companion.success(userQuotaService.listTransactions(SecurityUtil.getRequiredUserId(authentication), params))
    }

    @ApiOperation("用户将剩余额度转配给其他用户")
    @PostMapping("/transfer")
    fun transferQuota(
        @RequestBody @Valid params: UserQuotaTransferParams,
        authentication: Authentication,
    ): ApiResult<UserQuotaTransferResult> {
        return ApiResult.Companion.success(userQuotaService.transferQuota(SecurityUtil.getRequiredUserId(authentication), params))
    }
}
