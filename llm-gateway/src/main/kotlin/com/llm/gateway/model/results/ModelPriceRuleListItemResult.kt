package com.llm.gateway.model.results

import com.fasterxml.jackson.annotation.JsonFormat
import io.swagger.annotations.ApiModel
import io.swagger.annotations.ApiModelProperty
import java.math.BigDecimal
import java.util.Date

@ApiModel("模型计费规则列表项")
data class ModelPriceRuleListItemResult(
    @ApiModelProperty(value = "计费规则ID", required = true)
    val id: Long,
    @ApiModelProperty(value = "模型ID", required = true)
    val modelId: Long,
    @ApiModelProperty(value = "供应商ID", required = true)
    val vendorId: Long,
    @ApiModelProperty(value = "计费项", required = true)
    val chargeItem: String,
    @ApiModelProperty(value = "百万Token单价", required = true)
    val priceCnyPerMillion: BigDecimal,
    @ApiModelProperty(value = "币种", required = true)
    val currency: String,
    @ApiModelProperty(value = "是否启用", required = true)
    val active: Boolean,
    @ApiModelProperty(value = "创建时间")
    @field:JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Shanghai")
    val createdAt: Date?,
    @ApiModelProperty(value = "更新时间")
    @field:JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Shanghai")
    val updatedAt: Date?,
)

@ApiModel("删除模型计费规则结果")
data class ModelPriceRuleDeleteResult(
    @ApiModelProperty(value = "计费规则ID", required = true)
    val ruleId: Long,
    @ApiModelProperty(value = "是否已删除", required = true)
    val deleted: Boolean,
)
