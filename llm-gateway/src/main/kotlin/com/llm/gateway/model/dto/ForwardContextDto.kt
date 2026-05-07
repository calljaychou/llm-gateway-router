package com.llm.gateway.model.dto

import io.swagger.annotations.ApiModel
import io.swagger.annotations.ApiModelProperty

@ApiModel("上游转发上下文")
data class ForwardContextDto(
    @field:ApiModelProperty("上游完整请求地址")
    val targetUrl: String,
    @field:ApiModelProperty("供应商主密钥明文")
    val apiKey: String,
    @field:ApiModelProperty("已替换真实模型名的请求体")
    val payload: Map<String, Any?>,
    @field:ApiModelProperty("是否流式请求")
    val stream: Boolean,
    @field:ApiModelProperty("网关模型别名")
    val modelAlias: String,
    @field:ApiModelProperty("供应商ID")
    val vendorId: Long,
)
