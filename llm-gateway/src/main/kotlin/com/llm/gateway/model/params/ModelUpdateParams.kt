package com.llm.gateway.model.params

import com.llm.gateway.common.enums.BillingType
import io.swagger.annotations.ApiModel
import io.swagger.annotations.ApiModelProperty
import java.math.BigDecimal
import javax.validation.constraints.DecimalMin
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
    @ApiModelProperty(value = "输入Token每百万单价", example = "18.000000")
    @field:DecimalMin(value = "0.000000", message = "输入单价不能小于0")
    val inputPriceCnyPerMillion: BigDecimal? = null,
    @ApiModelProperty(value = "输出Token每百万单价", example = "72.000000")
    @field:DecimalMin(value = "0.000000", message = "输出单价不能小于0")
    val outputPriceCnyPerMillion: BigDecimal? = null,
    @ApiModelProperty(value = "是否激活", example = "true")
    val active: Boolean? = null,
)
