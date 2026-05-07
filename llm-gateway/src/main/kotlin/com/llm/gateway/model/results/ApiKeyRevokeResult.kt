package com.llm.gateway.model.results

import io.swagger.annotations.ApiModel
import io.swagger.annotations.ApiModelProperty

@ApiModel("吊销虚拟密钥结果")
data class ApiKeyRevokeResult(
    @ApiModelProperty(value = "密钥ID", required = true)
    val keyId: Long,
    @ApiModelProperty(value = "是否已吊销", required = true)
    val revoked: Boolean,
)
