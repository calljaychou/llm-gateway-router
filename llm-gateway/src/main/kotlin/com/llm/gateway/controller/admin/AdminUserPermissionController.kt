package com.llm.gateway.controller.admin

import com.llm.gateway.model.ApiResult
import com.llm.gateway.model.PageResult
import com.llm.gateway.model.params.AdminUserCreateParams
import com.llm.gateway.model.params.AdminUserPageParams
import com.llm.gateway.model.params.DepartmentPermissionsUpdateParams
import com.llm.gateway.model.results.AdminUserCreateResult
import com.llm.gateway.model.results.AdminUserPageItemResult
import com.llm.gateway.model.results.DepartmentPermissionsUpdateResult
import com.llm.gateway.model.results.DepartmentPermissionsViewResult
import com.llm.gateway.model.results.UserEffectivePermissionsResult
import com.llm.gateway.security.CustomUserDetails
import com.llm.gateway.service.AdminUserPermissionService
import io.swagger.annotations.Api
import io.swagger.annotations.ApiOperation
import io.swagger.annotations.ApiParam
import javax.validation.Valid
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@Api(tags = ["管理端-用户与权限"])
@RestController
@RequestMapping("/admin")
@PreAuthorize("hasAnyRole('admin','llm-lead')")
class AdminUserPermissionController(
    private val adminUserPermissionService: AdminUserPermissionService,
) {

    @ApiOperation("创建用户")
    @PostMapping("/users")
    fun createUser(@RequestBody @Valid params: AdminUserCreateParams): ApiResult<AdminUserCreateResult> {
        return ApiResult.success(adminUserPermissionService.createUser(params))
    }

    @ApiOperation("分页查询用户列表")
    @GetMapping("/users")
    fun listUsers(@Valid params: AdminUserPageParams): ApiResult<PageResult<AdminUserPageItemResult>> {
        return ApiResult.success(adminUserPermissionService.listUsers(params))
    }

    @ApiOperation("覆盖设置部门模型权限")
    @PutMapping("/departments/{id}/permissions")
    fun replaceDepartmentPermissions(
        @ApiParam(value = "部门ID", required = true) @PathVariable("id") deptId: Long,
        @RequestBody @Valid params: DepartmentPermissionsUpdateParams,
        authentication: Authentication,
    ): ApiResult<DepartmentPermissionsUpdateResult> {
        val operator = (authentication.principal as? CustomUserDetails)?.username
        return ApiResult.success(adminUserPermissionService.replaceDepartmentPermissions(deptId, params, operator))
    }

    @ApiOperation("查询部门权限")
    @GetMapping("/departments/{id}/permissions")
    fun getDepartmentPermissions(
        @ApiParam(value = "部门ID", required = true) @PathVariable("id") deptId: Long,
        @ApiParam(value = "视图(direct/effective)", required = true) @RequestParam("view", defaultValue = "direct") view: String,
    ): ApiResult<DepartmentPermissionsViewResult> {
        return ApiResult.success(adminUserPermissionService.getDepartmentPermissions(deptId, view))
    }

    @ApiOperation("查询用户生效权限")
    @GetMapping("/users/{id}/permissions/effective")
    fun getUserEffectivePermissions(
        @ApiParam(value = "用户ID", required = true) @PathVariable("id") userId: Long,
    ): ApiResult<UserEffectivePermissionsResult> {
        return ApiResult.success(adminUserPermissionService.getUserEffectivePermissions(userId))
    }
}
