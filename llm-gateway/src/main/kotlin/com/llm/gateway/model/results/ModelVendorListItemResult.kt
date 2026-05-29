package com.llm.gateway.model.results

import com.fasterxml.jackson.annotation.JsonFormat
import io.swagger.annotations.ApiModel
import io.swagger.annotations.ApiModelProperty
import java.math.BigDecimal
import java.util.Date

@ApiModel("模型供应商列表项")
data class ModelVendorListItemResult(
    @ApiModelProperty(value = "模型ID", required = true)
    val id: Long,
    @ApiModelProperty(value = "模型别名", required = true)
    val modelAlias: String,
    @ApiModelProperty(value = "真实模型名称", required = true)
    val realModelName: String,
    @ApiModelProperty(value = "供应商ID", required = true)
    val vendorId: Long,
    @ApiModelProperty(value = "供应商名称", required = true)
    val vendorName: String,
    @ApiModelProperty(value = "计费类型", required = true)
    val billingType: String,
    @ApiModelProperty(value = "输入Token每百万单价", required = true)
    val inputPriceCnyPerMillion: BigDecimal,
    @ApiModelProperty(value = "输出Token每百万单价", required = true)
    val outputPriceCnyPerMillion: BigDecimal,
    @ApiModelProperty(value = "是否激活", required = true)
    val active: Boolean,
    @ApiModelProperty(value = "创建时间")
    @field:JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Shanghai")
    val createdTime: Date?,
)
