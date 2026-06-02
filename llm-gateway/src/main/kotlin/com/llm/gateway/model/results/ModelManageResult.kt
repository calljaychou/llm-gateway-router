package com.llm.gateway.model.results

import io.swagger.annotations.ApiModel
import io.swagger.annotations.ApiModelProperty

@ApiModel("编辑模型结果")
data class ModelUpdateResult(
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
    @ApiModelProperty(value = "是否激活", required = true)
    val active: Boolean,
)

@ApiModel("删除模型结果")
data class ModelDeleteResult(
    @ApiModelProperty(value = "模型ID", required = true)
    val modelId: Long,
    @ApiModelProperty(value = "是否已删除", required = true)
    val deleted: Boolean,
)
