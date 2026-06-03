package com.llm.gateway.controller.admin

import com.llm.gateway.model.ApiResult
import com.llm.gateway.model.results.AdminUserQuotaConfigResult
import com.llm.gateway.model.results.UserEffectivePermissionsResult
import com.llm.gateway.model.results.UserModelUsageCountResult
import com.llm.gateway.model.results.UserUsageHourlyHeatmapResult
import com.llm.gateway.security.SecurityUtil
import com.llm.gateway.service.AdminUserPermissionService
import com.llm.gateway.service.UserUsageService
import io.swagger.annotations.Api
import io.swagger.annotations.ApiOperation
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

/**
 * @author JayCHou <a href="calljaychou@qq.com">Email</a>
 */
@Api(tags = ["用户端-使用情况"])
@RestController
@RequestMapping("/admin/user/usage")
class AdminUserUsageController(
    private val userUsageService: UserUsageService,
    private val adminUserPermissionService: AdminUserPermissionService
) {

    @ApiOperation("查询当前用户最近一个月每天24小时使用次数热图")
    @GetMapping("/recent-month-hourly-heatmap")
    fun getRecentMonthHourlyHeatmap(authentication: Authentication): ApiResult<UserUsageHourlyHeatmapResult> {
        return ApiResult.success(userUsageService.getRecentMonthHourlyHeatmap(SecurityUtil.getRequiredUserId(authentication)))
    }

    @ApiOperation("查询当前用户各模型使用次数统计")
    @GetMapping("/models/usage-counts")
    fun listModelUsageCounts(authentication: Authentication): ApiResult<List<UserModelUsageCountResult>> {
        return ApiResult.success(userUsageService.listModelUsageCounts(SecurityUtil.getRequiredUserId(authentication)))
    }

    @ApiOperation("查询用户生效权限")
    @GetMapping("/users/permissions/effective")
    fun getUserEffectivePermissions(authentication: Authentication): ApiResult<UserEffectivePermissionsResult> {
        return ApiResult.success(adminUserPermissionService.getUserEffectivePermissions(SecurityUtil.getRequiredUserId(authentication)))
    }

    @ApiOperation("查询当前用户配额配置")
    @GetMapping("/quota/config")
    fun getUserQuotaConfig(authentication: Authentication): ApiResult<AdminUserQuotaConfigResult> {
        return ApiResult.success(adminUserPermissionService.getUserQuotaConfig(SecurityUtil.getRequiredUserId(authentication)))
    }
}
