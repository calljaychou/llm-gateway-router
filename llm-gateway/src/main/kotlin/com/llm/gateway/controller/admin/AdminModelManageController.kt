package com.llm.gateway.controller.admin

import com.llm.gateway.model.ApiResult
import com.llm.gateway.model.params.MasterKeyCreateParams
import com.llm.gateway.model.params.ModelCreateParams
import com.llm.gateway.model.params.VendorCreateParams
import com.llm.gateway.model.results.MasterKeyCreateResult
import com.llm.gateway.model.results.MasterKeyListResult
import com.llm.gateway.model.results.ModelCreateResult
import com.llm.gateway.model.results.VendorCreateResult
import com.llm.gateway.service.AdminModelManageService
import io.swagger.annotations.Api
import io.swagger.annotations.ApiOperation
import io.swagger.annotations.ApiParam
import io.swagger.annotations.ApiResponse
import io.swagger.annotations.ApiResponses
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import javax.validation.Valid

@Api(tags = ["管理端-模型接入"])
@RestController
@RequestMapping("/admin")
@PreAuthorize("hasAnyRole('admin','llm-lead')")
class AdminModelManageController(
    private val adminModelManageService: AdminModelManageService,
) {

    @ApiOperation(value = "新增供应商")
    @ApiResponses(
        ApiResponse(code = 200, message = "成功"),
        ApiResponse(code = 400, message = "参数错误"),
        ApiResponse(code = 401, message = "未认证"),
        ApiResponse(code = 403, message = "无权限"),
        ApiResponse(code = 500, message = "系统异常"),
    )
    @PostMapping("/vendors")
    fun createVendor(@RequestBody @Valid params: VendorCreateParams): ApiResult<VendorCreateResult> {
        return ApiResult.success(adminModelManageService.createVendor(params))
    }

    @ApiOperation(value = "新增主密钥")
    @ApiResponses(
        ApiResponse(code = 200, message = "成功"),
        ApiResponse(code = 400, message = "参数错误"),
        ApiResponse(code = 401, message = "未认证"),
        ApiResponse(code = 403, message = "无权限"),
        ApiResponse(code = 500, message = "系统异常"),
    )
    @PostMapping("/master-keys")
    fun createMasterKey(@RequestBody @Valid params: MasterKeyCreateParams): ApiResult<MasterKeyCreateResult> {
        return ApiResult.success(adminModelManageService.createMasterKey(params))
    }

    @ApiOperation(value = "查询主密钥列表")
    @ApiResponses(
        ApiResponse(code = 200, message = "成功"),
        ApiResponse(code = 400, message = "参数错误"),
        ApiResponse(code = 401, message = "未认证"),
        ApiResponse(code = 403, message = "无权限"),
        ApiResponse(code = 500, message = "系统异常"),
    )
    @GetMapping("/master-keys")
    fun listMasterKeys(
        @ApiParam(value = "供应商ID，不传则查询全部") @RequestParam("vendorId", required = false) vendorId: Long?,
    ): ApiResult<MasterKeyListResult> {
        return ApiResult.success(adminModelManageService.listMasterKeys(vendorId))
    }

    @ApiOperation(value = "查询供应商主密钥列表")
    @ApiResponses(
        ApiResponse(code = 200, message = "成功"),
        ApiResponse(code = 400, message = "参数错误"),
        ApiResponse(code = 401, message = "未认证"),
        ApiResponse(code = 403, message = "无权限"),
        ApiResponse(code = 500, message = "系统异常"),
    )
    @GetMapping("/vendors/{vendorId}/master-keys")
    fun listVendorMasterKeys(
        @ApiParam(value = "供应商ID", required = true) @PathVariable("vendorId") vendorId: Long,
    ): ApiResult<MasterKeyListResult> {
        return ApiResult.success(adminModelManageService.listMasterKeys(vendorId))
    }

    @ApiOperation(value = "新增模型映射")
    @ApiResponses(
        ApiResponse(code = 200, message = "成功"),
        ApiResponse(code = 400, message = "参数错误"),
        ApiResponse(code = 401, message = "未认证"),
        ApiResponse(code = 403, message = "无权限"),
        ApiResponse(code = 500, message = "系统异常"),
    )
    @PostMapping("/models")
    fun createModel(@RequestBody @Valid params: ModelCreateParams): ApiResult<ModelCreateResult> {
        return ApiResult.success(adminModelManageService.createModel(params))
    }
}
