package com.llm.gateway.model.params

import com.llm.gateway.common.enums.NormalStatus
import io.swagger.annotations.ApiModel
import io.swagger.annotations.ApiModelProperty
import javax.validation.Valid
import javax.validation.constraints.Max
import javax.validation.constraints.Min
import javax.validation.constraints.NotBlank

@ApiModel("管理端-部门模型权限更新参数")
data class DepartmentPermissionsUpdateParams(
    @ApiModelProperty(value = "授权项，传空数组表示清空该部门直接权限", required = true)
    @field:Valid
    val items: List<DepartmentPermissionItemParams>,
)

@ApiModel("管理端-部门模型权限项")
data class DepartmentPermissionItemParams(
    @ApiModelProperty(value = "模型别名", required = true, example = "gpt-4-enterprise")
    @field:NotBlank(message = "模型别名不能为空")
    val modelAlias: String,
    @ApiModelProperty(value = "作用域(SELF/SUBTREE)", required = true, example = "SUBTREE")
    @field:NotBlank(message = "作用域不能为空")
    val scope: String,
    @ApiModelProperty(value = "授权状态，1=启用，0=停用", example = "1")
    @field:Min(value = 0, message = "授权状态仅支持 0 或 1")
    @field:Max(value = 1, message = "授权状态仅支持 0 或 1")
    val status: Int? = NormalStatus,
)
