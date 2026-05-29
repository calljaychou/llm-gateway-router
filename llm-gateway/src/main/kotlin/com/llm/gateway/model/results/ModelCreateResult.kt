package com.llm.gateway.model.results

import io.swagger.annotations.ApiModel
import io.swagger.annotations.ApiModelProperty
import java.math.BigDecimal

@ApiModel("创建模型结果")
data class ModelCreateResult(
    @ApiModelProperty(value = "模型ID", required = true)
    val modelId: Long,
    @ApiModelProperty(value = "模型别名", required = true)
    val modelAlias: String,
    @ApiModelProperty(value = "真实模型名", required = true)
    val realModelName: String,
    @ApiModelProperty(value = "供应商ID", required = true)
    val vendorId: Long,
    @ApiModelProperty(value = "计费类型", required = true)
    val billingType: String,
    @ApiModelProperty(value = "输入Token每百万单价", required = true)
    val inputPriceCnyPerMillion: BigDecimal,
    @ApiModelProperty(value = "输出Token每百万单价", required = true)
    val outputPriceCnyPerMillion: BigDecimal,
    @ApiModelProperty(value = "是否激活", required = true)
    val active: Boolean,
)
