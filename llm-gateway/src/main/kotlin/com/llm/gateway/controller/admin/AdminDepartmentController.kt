package com.llm.gateway.controller.admin

import com.llm.gateway.model.ApiResult
import com.llm.gateway.model.params.DepartmentCreateParams
import com.llm.gateway.model.results.DepartmentCreateResult
import com.llm.gateway.model.results.DepartmentDeleteResult
import com.llm.gateway.model.results.DepartmentTreeResult
import com.llm.gateway.service.DepartmentService
import io.swagger.annotations.Api
import io.swagger.annotations.ApiOperation
import io.swagger.annotations.ApiParam
import io.swagger.annotations.ApiResponse
import io.swagger.annotations.ApiResponses
import javax.validation.Valid
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@Api(tags = ["管理端-部门管理"])
@RestController
@RequestMapping("/admin/departments")
@PreAuthorize("hasAnyRole('admin','llm-lead')")
class AdminDepartmentController(
    private val departmentService: DepartmentService,
) {

    @ApiOperation(value = "新建一级部门")
    @PostMapping
    fun createRootDepartment(@RequestBody @Valid params: DepartmentCreateParams): ApiResult<DepartmentCreateResult> {
        return ApiResult.success(departmentService.createRootDepartment(params))
    }

    @ApiOperation(value = "添加子部门")
    @PostMapping("/{id}/children")
    fun createChildDepartment(
        @ApiParam(value = "父部门ID", required = true) @PathVariable("id") parentDeptId: Long,
        @RequestBody @Valid params: DepartmentCreateParams,
    ): ApiResult<DepartmentCreateResult> {
        return ApiResult.success(departmentService.createChildDepartment(parentDeptId, params))
    }

    @ApiOperation(value = "删除部门")
    @DeleteMapping("/{id}")
    fun deleteDepartment(
        @ApiParam(value = "部门ID", required = true) @PathVariable("id") deptId: Long,
    ): ApiResult<DepartmentDeleteResult> {
        return ApiResult.success(departmentService.deleteDepartment(deptId))
    }

    @ApiOperation(value = "查询部门树")
    @GetMapping("/tree")
    fun getDepartmentTree(): ApiResult<List<DepartmentTreeResult>> {
        return ApiResult.success(departmentService.getDepartmentTree())
    }
}
