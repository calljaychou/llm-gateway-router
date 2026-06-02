package com.llm.gateway.controller.admin

import com.llm.gateway.model.ApiResult
import com.llm.gateway.model.PageResult
import com.llm.gateway.model.params.MasterKeyCreateParams
import com.llm.gateway.model.params.MasterKeyPageParams
import com.llm.gateway.model.params.ModelCreateParams
import com.llm.gateway.model.params.ModelPriceRuleListParams
import com.llm.gateway.model.params.ModelPriceRuleUpdateParams
import com.llm.gateway.model.params.ModelUpdateParams
import com.llm.gateway.model.params.VendorCreateParams
import com.llm.gateway.model.results.MasterKeyCreateResult
import com.llm.gateway.model.results.MasterKeyListResult
import com.llm.gateway.model.results.MasterKeyPageItemResult
import com.llm.gateway.model.results.ModelCreateResult
import com.llm.gateway.model.results.ModelDeleteResult
import com.llm.gateway.model.results.ModelPriceRuleListItemResult
import com.llm.gateway.model.results.ModelUpdateResult
import com.llm.gateway.model.results.ModelVendorListItemResult
import com.llm.gateway.model.results.VendorCreateResult
import com.llm.gateway.model.results.VendorListItemResult
import com.llm.gateway.service.AdminModelManageService
import io.swagger.annotations.Api
import io.swagger.annotations.ApiOperation
import io.swagger.annotations.ApiParam
import io.swagger.annotations.ApiResponse
import io.swagger.annotations.ApiResponses
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
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
    @PostMapping("/vendors")
    fun createVendor(@RequestBody @Valid params: VendorCreateParams): ApiResult<VendorCreateResult> {
        return ApiResult.success(adminModelManageService.createVendor(params))
    }

    @ApiOperation(value = "查询供应商列表")
    @GetMapping(value = ["/vendors", "/vendors/list"])
    fun listVendors(): ApiResult<List<VendorListItemResult>> {
        return ApiResult.success(adminModelManageService.listVendors())
    }

    @ApiOperation(value = "新增主密钥")
    @PostMapping("/master-keys")
    fun createMasterKey(@RequestBody @Valid params: MasterKeyCreateParams): ApiResult<MasterKeyCreateResult> {
        return ApiResult.success(adminModelManageService.createMasterKey(params))
    }

    @ApiOperation(value = "查询主密钥列表")
    @GetMapping("/master-keys")
    fun listMasterKeys(@Valid params: MasterKeyPageParams): ApiResult<PageResult<MasterKeyPageItemResult>> {
        return ApiResult.success(adminModelManageService.listMasterKeys(params))
    }

    @ApiOperation(value = "查询供应商主密钥列表")
    @GetMapping("/vendors/{vendorId}/master-keys")
    fun listVendorMasterKeys(
        @ApiParam(value = "供应商ID", required = true) @PathVariable("vendorId") vendorId: Long,
    ): ApiResult<MasterKeyListResult> {
        return ApiResult.success(adminModelManageService.listMasterKeys(vendorId))
    }

    @ApiOperation(value = "新增模型映射")
    @PostMapping("/models")
    fun createModel(@RequestBody @Valid params: ModelCreateParams): ApiResult<ModelCreateResult> {
        return ApiResult.success(adminModelManageService.createModel(params))
    }

    @ApiOperation(value = "编辑模型映射")
    @PutMapping("/models/{modelId}")
    fun updateModel(
        @ApiParam(value = "模型ID", required = true) @PathVariable("modelId") modelId: Long,
        @RequestBody @Valid params: ModelUpdateParams,
    ): ApiResult<ModelUpdateResult> {
        return ApiResult.success(adminModelManageService.updateModel(modelId, params))
    }

    @ApiOperation(value = "删除模型映射")
    @DeleteMapping("/models/{modelId}")
    fun deleteModel(
        @ApiParam(value = "模型ID", required = true) @PathVariable("modelId") modelId: Long,
    ): ApiResult<ModelDeleteResult> {
        return ApiResult.success(adminModelManageService.deleteModel(modelId))
    }

    @ApiOperation(value = "查询模型供应商列表")
    @GetMapping("/models")
    fun listModelVendors(): ApiResult<List<ModelVendorListItemResult>> {
        return ApiResult.success(adminModelManageService.listModelVendors())
    }

    @ApiOperation(value = "查询模型计费规则列表")
    @GetMapping("/model-price-rules")
    fun listModelPriceRules(@Valid params: ModelPriceRuleListParams): ApiResult<List<ModelPriceRuleListItemResult>> {
        return ApiResult.success(adminModelManageService.listModelPriceRules(params))
    }

    @ApiOperation(value = "编辑模型计费规则")
    @PutMapping("/model-price-rules/{ruleId}")
    fun updateModelPriceRule(
        @ApiParam(value = "计费规则ID", required = true) @PathVariable("ruleId") ruleId: Long,
        @RequestBody @Valid params: ModelPriceRuleUpdateParams,
    ): ApiResult<ModelPriceRuleListItemResult> {
        return ApiResult.success(adminModelManageService.updateModelPriceRule(ruleId, params))
    }
}
