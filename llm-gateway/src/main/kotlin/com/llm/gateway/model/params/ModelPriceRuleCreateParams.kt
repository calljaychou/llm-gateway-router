package com.llm.gateway.model.params

import io.swagger.annotations.ApiModel
import io.swagger.annotations.ApiModelProperty
import java.math.BigDecimal
import javax.validation.constraints.DecimalMin
import javax.validation.constraints.NotBlank
import javax.validation.constraints.NotNull

@ApiModel("新增模型计费规则参数")
data class ModelPriceRuleCreateParams(
    @ApiModelProperty(value = "模型ID", required = true, example = "1")
    @field:NotNull(message = "模型ID不能为空")
    val modelId: Long?,
    @ApiModelProperty(value = "计费项", required = true, example = "INPUT_TEXT")
    @field:NotBlank(message = "计费项不能为空")
    val chargeItem: String,
    @ApiModelProperty(value = "百万Token单价", required = true, example = "18.00000000")
    @field:NotNull(message = "百万Token单价不能为空")
    @field:DecimalMin(value = "0.00000000", message = "百万Token单价不能小于0")
    val priceCnyPerMillion: BigDecimal?,
    @ApiModelProperty(value = "币种", example = "CNY")
    val currency: String? = null,
    @ApiModelProperty(value = "是否启用", example = "true")
    val active: Boolean? = null,
)
