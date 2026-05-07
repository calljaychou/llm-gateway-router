package com.llm.gateway.ratelimit

import io.swagger.annotations.ApiModel
import io.swagger.annotations.ApiModelProperty

interface RateLimiterProvider {
    fun tryAcquire(scope: String, targetId: String, permitsPerSecond: Long): RateLimiterAcquireResult
}

@ApiModel("限流器获取结果")
data class RateLimiterAcquireResult(
    @field:ApiModelProperty("是否允许通过")
    val allowed: Boolean,
    @field:ApiModelProperty("拒绝时建议重试秒数")
    val retryAfterSeconds: Long,
    @field:ApiModelProperty("当前剩余令牌数")
    val remainingPermits: Long,
)
