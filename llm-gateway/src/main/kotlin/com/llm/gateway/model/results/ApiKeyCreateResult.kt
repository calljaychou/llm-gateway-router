package com.llm.gateway.model.results

import io.swagger.annotations.ApiModel
import io.swagger.annotations.ApiModelProperty

@ApiModel("创建虚拟密钥结果")
data class ApiKeyCreateResult(
    @ApiModelProperty(value = "密钥ID", required = true)
    val keyId: Long,
    @ApiModelProperty(value = "密钥名称", required = true)
    val name: String,
    @ApiModelProperty(value = "虚拟密钥，仅首次返回", required = true)
    val apiKey: String,
    @ApiModelProperty(value = "密钥展示前缀", required = true)
    val keyPrefix: String,
)
