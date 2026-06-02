package com.llm.gateway.model.params

import io.swagger.annotations.ApiModel
import io.swagger.annotations.ApiModelProperty

@ApiModel("模型计费规则列表查询参数")
data class ModelPriceRuleListParams(
    @ApiModelProperty(value = "模型ID", example = "1")
    val modelId: Long? = null,
    @ApiModelProperty(value = "供应商ID", example = "1")
    val vendorId: Long? = null,
    @ApiModelProperty(value = "计费项", example = "INPUT_TEXT")
    val chargeItem: String? = null,
    @ApiModelProperty(value = "币种", example = "CNY")
    val currency: String? = null,
    @ApiModelProperty(value = "是否启用", example = "true")
    val active: Boolean? = null,
)
