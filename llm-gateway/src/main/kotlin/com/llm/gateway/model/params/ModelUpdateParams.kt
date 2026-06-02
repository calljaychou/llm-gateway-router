package com.llm.gateway.model.params

import com.llm.gateway.common.enums.BillingType
import io.swagger.annotations.ApiModel
import io.swagger.annotations.ApiModelProperty
import javax.validation.constraints.NotBlank
import javax.validation.constraints.NotNull

@ApiModel("编辑模型参数")
data class ModelUpdateParams(
    @ApiModelProperty(value = "模型别名", example = "gpt-4-enterprise")
    @field:NotBlank(message = "模型别名不能为空")
    val modelAlias: String? = null,
    @ApiModelProperty(value = "真实模型名称", example = "gpt-4o")
    @field:NotBlank(message = "真实模型名不能为空")
    val realModelName: String? = null,
    @ApiModelProperty(value = "供应商ID", example = "1")
    @field:NotNull(message = "供应商ID不能为空")
    val vendorId: Long? = null,
    @ApiModelProperty(value = "计费类型(FREE/PAID)", example = "PAID")
    val billingType: BillingType? = null,
    @ApiModelProperty(value = "是否激活", example = "true")
    val active: Boolean? = null,
)
