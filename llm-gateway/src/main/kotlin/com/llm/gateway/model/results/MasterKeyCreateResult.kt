package com.llm.gateway.model.results

import io.swagger.annotations.ApiModel
import io.swagger.annotations.ApiModelProperty

@ApiModel("创建主密钥结果")
data class MasterKeyCreateResult(
    @ApiModelProperty(value = "主密钥ID", required = true)
    val masterKeyId: Long,
    @ApiModelProperty(value = "供应商ID", required = true)
    val vendorId: Long,
    @ApiModelProperty(value = "状态", required = true)
    val status: Int,
    @ApiModelProperty(value = "权重", required = true)
    val weight: Int,
    @ApiModelProperty(value = "密钥展示前缀", required = true)
    val keyPrefix: String,
)

