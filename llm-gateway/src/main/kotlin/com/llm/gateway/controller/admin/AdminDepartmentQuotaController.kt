package com.llm.gateway.controller.admin

import com.llm.gateway.model.ApiResult
import com.llm.gateway.model.params.AdminDepartmentQuotaCreateParams
import com.llm.gateway.model.params.AdminDepartmentQuotaPageParams
import com.llm.gateway.model.params.AdminDepartmentQuotaStatusUpdateParams
import com.llm.gateway.model.params.AdminDepartmentQuotaUpdateParams
import com.llm.gateway.model.results.AdminDepartmentQuotaItemResult
import com.llm.gateway.model.results.AdminDepartmentQuotaOperateResult
import com.llm.gateway.model.results.AdminDepartmentQuotaPageResult
import com.llm.gateway.service.AdminDepartmentQuotaService
import io.swagger.annotations.Api
import io.swagger.annotations.ApiOperation
import io.swagger.annotations.ApiParam
import io.swagger.annotations.ApiResponse
import io.swagger.annotations.ApiResponses
import javax.validation.Valid
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@Api(tags = ["管理端-部门配额管理"])
@RestController
@RequestMapping("/admin")
@PreAuthorize("hasAnyRole('admin','llm-lead')")
class AdminDepartmentQuotaController(
    private val adminDepartmentQuotaService: AdminDepartmentQuotaService,
) {

    @ApiOperation(value = "分页查询部门配额策略")
    @ApiResponses(
        ApiResponse(code = 200, message = "成功"),
        ApiResponse(code = 400, message = "参数错误"),
        ApiResponse(code = 401, message = "未认证"),
        ApiResponse(code = 403, message = "无权限"),
        ApiResponse(code = 500, message = "系统异常"),
    )
    @GetMapping("/department-quotas")
    fun pageDepartmentQuotas(
        @Valid params: AdminDepartmentQuotaPageParams,
    ): ApiResult<AdminDepartmentQuotaPageResult> {
        return ApiResult.success(adminDepartmentQuotaService.pageDepartmentQuotas(params))
    }

    @ApiOperation(value = "按部门查询配额策略")
    @ApiResponses(
        ApiResponse(code = 200, message = "成功"),
        ApiResponse(code = 400, message = "参数错误"),
        ApiResponse(code = 401, message = "未认证"),
        ApiResponse(code = 403, message = "无权限"),
        ApiResponse(code = 500, message = "系统异常"),
    )
    @GetMapping("/department-quotas/{deptId}")
    fun getDepartmentQuotaByDeptId(
        @ApiParam(value = "部门ID", required = true) @PathVariable("deptId") deptId: Long,
    ): ApiResult<AdminDepartmentQuotaItemResult> {
        return ApiResult.success(adminDepartmentQuotaService.getDepartmentQuotaByDeptId(deptId))
    }

    @ApiOperation(value = "新增部门配额策略")
    @ApiResponses(
        ApiResponse(code = 200, message = "成功"),
        ApiResponse(code = 400, message = "参数错误"),
        ApiResponse(code = 401, message = "未认证"),
        ApiResponse(code = 403, message = "无权限"),
        ApiResponse(code = 500, message = "系统异常"),
    )
    @PostMapping("/department-quotas")
    fun createDepartmentQuota(
        @RequestBody @Valid params: AdminDepartmentQuotaCreateParams,
    ): ApiResult<AdminDepartmentQuotaOperateResult> {
        return ApiResult.success(adminDepartmentQuotaService.createDepartmentQuota(params))
    }

    @ApiOperation(value = "更新部门配额策略")
    @ApiResponses(
        ApiResponse(code = 200, message = "成功"),
        ApiResponse(code = 400, message = "参数错误"),
        ApiResponse(code = 401, message = "未认证"),
        ApiResponse(code = 403, message = "无权限"),
        ApiResponse(code = 500, message = "系统异常"),
    )
    @PutMapping("/department-quotas/{deptId}")
    fun updateDepartmentQuota(
        @ApiParam(value = "部门ID", required = true) @PathVariable("deptId") deptId: Long,
        @RequestBody @Valid params: AdminDepartmentQuotaUpdateParams,
    ): ApiResult<AdminDepartmentQuotaOperateResult> {
        return ApiResult.success(adminDepartmentQuotaService.updateDepartmentQuota(deptId, params))
    }

    @ApiOperation(value = "启停部门配额策略")
    @ApiResponses(
        ApiResponse(code = 200, message = "成功"),
        ApiResponse(code = 400, message = "参数错误"),
        ApiResponse(code = 401, message = "未认证"),
        ApiResponse(code = 403, message = "无权限"),
        ApiResponse(code = 500, message = "系统异常"),
    )
    @PutMapping("/department-quotas/{deptId}/status")
    fun updateDepartmentQuotaStatus(
        @ApiParam(value = "部门ID", required = true) @PathVariable("deptId") deptId: Long,
        @RequestBody @Valid params: AdminDepartmentQuotaStatusUpdateParams,
    ): ApiResult<AdminDepartmentQuotaOperateResult> {
        return ApiResult.success(adminDepartmentQuotaService.updateDepartmentQuotaStatus(deptId, params))
    }
}
