package com.llm.gateway.controller.admin

import com.llm.gateway.model.ApiResult
import com.llm.gateway.model.params.RoleCreateParams
import com.llm.gateway.model.results.RoleCreateResult
import com.llm.gateway.model.results.RoleDeleteResult
import com.llm.gateway.model.results.RoleListItemResult
import com.llm.gateway.security.CustomUserDetails
import com.llm.gateway.service.AdminRoleService
import io.swagger.annotations.Api
import io.swagger.annotations.ApiOperation
import io.swagger.annotations.ApiParam
import io.swagger.annotations.ApiResponse
import io.swagger.annotations.ApiResponses
import javax.validation.Valid
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@Api(tags = ["管理端-角色管理"])
@RestController
@RequestMapping("/admin/roles")
@PreAuthorize("hasAnyRole('admin','llm-lead')")
class AdminRoleController(
    private val adminRoleService: AdminRoleService,
) {

    @ApiOperation(value = "查询角色列表")
    @GetMapping
    fun listRoles(): ApiResult<List<RoleListItemResult>> {
        return ApiResult.success(adminRoleService.listRoles())
    }

    @ApiOperation(value = "新增角色")
    @PostMapping
    fun createRole(
        @RequestBody @Valid params: RoleCreateParams,
        authentication: Authentication,
    ): ApiResult<RoleCreateResult> {
        val operator = (authentication.principal as? CustomUserDetails)?.username
        return ApiResult.success(adminRoleService.createRole(params, operator))
    }

    @ApiOperation(value = "删除角色")
    @DeleteMapping("/{id}")
    fun deleteRole(
        @ApiParam(value = "角色ID", required = true) @PathVariable("id") roleId: Long,
    ): ApiResult<RoleDeleteResult> {
        return ApiResult.success(adminRoleService.deleteRole(roleId))
    }
}
