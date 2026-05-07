package com.llm.gateway.model.dto

import io.swagger.annotations.ApiModel
import io.swagger.annotations.ApiModelProperty

@ApiModel("限流上下文")
data class RateLimitContextDto(
    @field:ApiModelProperty("调用用户ID")
    val userId: Long,
    @field:ApiModelProperty("虚拟API Key原文")
    val virtualApiKey: String,
    @field:ApiModelProperty("模型别名")
    val modelAlias: String,
    @field:ApiModelProperty("供应商ID")
    val vendorId: Long,
    @field:ApiModelProperty("请求路径")
    val path: String,
    @field:ApiModelProperty("是否流式请求")
    val stream: Boolean,
)