package com.llm.gateway.controller.admin

import com.llm.gateway.model.ApiResult
import com.llm.gateway.model.PageResult
import com.llm.gateway.model.params.AdminUserCreateParams
import com.llm.gateway.model.params.AdminUserPageParams
import com.llm.gateway.model.params.AdminUserPasswordChangeParams
import com.llm.gateway.model.params.AdminUserUpdateParams
import com.llm.gateway.model.params.DepartmentPermissionsUpdateParams
import com.llm.gateway.model.results.AdminUserCreateResult
import com.llm.gateway.model.results.AdminUserDetailResult
import com.llm.gateway.model.results.AdminUserPageItemResult
import com.llm.gateway.model.results.AdminUserPasswordChangeResult
import com.llm.gateway.model.results.AdminUserUpdateResult
import com.llm.gateway.model.results.DepartmentPermissionsUpdateResult
import com.llm.gateway.model.results.DepartmentPermissionsViewResult
import com.llm.gateway.model.results.UserEffectivePermissionsResult
import com.llm.gateway.security.CustomUserDetails
import com.llm.gateway.service.AdminUserPermissionService
import io.swagger.annotations.Api
import io.swagger.annotations.ApiOperation
import io.swagger.annotations.ApiParam
import io.swagger.annotations.ApiResponse
import io.swagger.annotations.ApiResponses
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

    @ApiOperation("查询用户详情")
    @ApiResponses(
        value = [
            ApiResponse(code = 200, message = "查询成功"),
            ApiResponse(code = 400, message = "用户不存在"),
            ApiResponse(code = 403, message = "无权限访问"),
        ]
    )
    @GetMapping("/users/{id}/detail")
    fun getUserDetail(
        @ApiParam(value = "用户ID", required = true) @PathVariable("id") userId: Long,
    ): ApiResult<AdminUserDetailResult> {
        return ApiResult.success(adminUserPermissionService.getUserDetail(userId))
    }

    @ApiOperation("修改用户密码")
    @ApiResponses(
        value = [
            ApiResponse(code = 200, message = "修改成功"),
            ApiResponse(code = 400, message = "用户不存在或状态不允许"),
            ApiResponse(code = 403, message = "无权限访问"),
        ]
    )
    @PutMapping("/users/{id}/password")
    fun changeUserPassword(
        @ApiParam(value = "用户ID", required = true) @PathVariable("id") userId: Long,
        @RequestBody @Valid params: AdminUserPasswordChangeParams,
    ): ApiResult<AdminUserPasswordChangeResult> {
        return ApiResult.success(adminUserPermissionService.changeUserPassword(userId, params))
    }

    @ApiOperation("修改用户信息")
    @ApiResponses(
        value = [
            ApiResponse(code = 200, message = "修改成功"),
            ApiResponse(code = 400, message = "参数错误或用户不存在"),
            ApiResponse(code = 403, message = "无权限访问"),
        ]
    )
    @PutMapping("/users/{id}")
    fun updateUser(
        @ApiParam(value = "用户ID", required = true) @PathVariable("id") userId: Long,
        @RequestBody @Valid params: AdminUserUpdateParams,
    ): ApiResult<AdminUserUpdateResult> {
        return ApiResult.success(adminUserPermissionService.updateUser(userId, params))
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
