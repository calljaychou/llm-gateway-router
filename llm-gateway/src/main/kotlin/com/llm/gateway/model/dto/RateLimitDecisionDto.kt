package com.llm.gateway.model.dto

import io.swagger.annotations.ApiModel
import io.swagger.annotations.ApiModelProperty

@ApiModel("限流决策结果")
data class RateLimitDecisionDto(
    @field:ApiModelProperty("限流来源: gateway/upstream")
    val source: String,
    @field:ApiModelProperty("命中的限流维度: api_key/user/model/vendor/global")
    val scope: String,
    @field:ApiModelProperty("命中的目标标识")
    val targetId: String,
    @field:ApiModelProperty("建议重试秒数")
    val retryAfterSeconds: Long,
    @field:ApiModelProperty("当前维度限流阈值")
    val limit: String,
    @field:ApiModelProperty("当前维度剩余配额")
    val remaining: String,
    @field:ApiModelProperty("重置时间戳(秒)")
    val resetEpochSecond: String,
)