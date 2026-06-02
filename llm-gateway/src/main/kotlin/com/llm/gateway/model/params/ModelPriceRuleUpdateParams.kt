package com.llm.gateway.model.params

import io.swagger.annotations.ApiModel
import io.swagger.annotations.ApiModelProperty
import java.math.BigDecimal
import javax.validation.constraints.DecimalMin

@ApiModel("编辑模型计费规则参数")
data class ModelPriceRuleUpdateParams(
    @ApiModelProperty(value = "百万Token单价", example = "18.00000000")
    @field:DecimalMin(value = "0.00000000", message = "百万Token单价不能小于0")
    val priceCnyPerMillion: BigDecimal? = null,
    @ApiModelProperty(value = "币种", example = "CNY")
    val currency: String? = null,
    @ApiModelProperty(value = "是否启用", example = "true")
    val active: Boolean? = null,
)
