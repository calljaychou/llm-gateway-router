package com.llm.gateway.model.results

import com.fasterxml.jackson.annotation.JsonFormat
import io.swagger.annotations.ApiModel
import io.swagger.annotations.ApiModelProperty
import java.util.Date

@ApiModel("虚拟密钥列表结果")
data class ApiKeyListResult(
    @ApiModelProperty(value = "密钥列表", required = true)
    val keys: List<ApiKeyListItemResult>,
)

@ApiModel("虚拟密钥列表项")
data class ApiKeyListItemResult(
    @ApiModelProperty(value = "密钥ID", required = true)
    val keyId: Long,
    @ApiModelProperty(value = "密钥名称", required = true)
    val name: String,
    @ApiModelProperty(value = "密钥展示前缀", required = true)
    val keyPrefix: String,
    @ApiModelProperty(value = "状态，1-生效，0-吊销", required = true)
    val status: Int,
    @ApiModelProperty(value = "过期时间")
    @field:JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Shanghai")
    val expiresTime: Date?,
)
