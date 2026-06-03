package com.llm.gateway.controller.admin

import com.llm.gateway.model.ApiResult
import com.llm.gateway.model.PageResult
import com.llm.gateway.model.params.AdminStatisticsDepartmentUsagePageParams
import com.llm.gateway.model.params.AdminStatisticsUsageLogPageParams
import com.llm.gateway.model.params.AdminStatisticsUserUsagePageParams
import com.llm.gateway.model.results.AdminStatisticsDepartmentUsageItemResult
import com.llm.gateway.model.results.AdminStatisticsUsageLogItemResult
import com.llm.gateway.model.results.AdminStatisticsUserUsageItemResult
import com.llm.gateway.service.AdminStatisticsUsageService
import io.swagger.annotations.Api
import io.swagger.annotations.ApiOperation
import javax.validation.Valid
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

/**
 * @author JayCHou <a href="calljaychou@qq.com">Email</a>
 */
@Api(tags = ["管理端-统计使用情况"])
@RestController
@RequestMapping("/admin/statistics/usage")
@PreAuthorize("hasAnyRole('admin','llm-lead')")
class AdminStatisticsUsageController(
    private val adminStatisticsUsageService: AdminStatisticsUsageService,
) {

    @ApiOperation("分页查询使用日志列表")
    @GetMapping("/logs")
    fun listUsageLogs(
        @Valid params: AdminStatisticsUsageLogPageParams,
    ): ApiResult<PageResult<AdminStatisticsUsageLogItemResult>> {
        return ApiResult.success(adminStatisticsUsageService.listUsageLogs(params))
    }

    @ApiOperation("分页查询部门使用数量统计")
    @GetMapping("/departments")
    fun listDepartmentUsageStats(
        @Valid params: AdminStatisticsDepartmentUsagePageParams,
    ): ApiResult<PageResult<AdminStatisticsDepartmentUsageItemResult>> {
        return ApiResult.success(adminStatisticsUsageService.listDepartmentUsageStats(params))
    }

    @ApiOperation("分页查询用户使用情况")
    @GetMapping("/users")
    fun listUserUsageStats(
        @Valid params: AdminStatisticsUserUsagePageParams,
    ): ApiResult<PageResult<AdminStatisticsUserUsageItemResult>> {
        return ApiResult.success(adminStatisticsUsageService.listUserUsageStats(params))
    }
}
