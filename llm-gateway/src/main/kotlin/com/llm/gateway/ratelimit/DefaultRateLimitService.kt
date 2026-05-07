package com.llm.gateway.ratelimit

import com.llm.gateway.model.dto.RateLimitContextDto
import com.llm.gateway.model.dto.RateLimitDecisionDto
import java.time.Instant
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service

@Service
class DefaultRateLimitService(
    private val rateLimiterProvider: RateLimiterProvider,
    @Value("\${gateway.rate-limit.redisson.enabled:true}") private val enabled: Boolean,
    @Value("\${gateway.rate-limit.redisson.api-key-per-second:3}") private val apiKeyRatePerSecond: Long,
    @Value("\${gateway.rate-limit.redisson.user-per-second:5}") private val userRatePerSecond: Long,
    @Value("\${gateway.rate-limit.redisson.model-per-second:20}") private val modelRatePerSecond: Long,
    @Value("\${gateway.rate-limit.redisson.vendor-per-second:50}") private val vendorRatePerSecond: Long,
    @Value("\${gateway.rate-limit.redisson.global-per-second:100}") private val globalRatePerSecond: Long,
) : RateLimitService {

    override fun evaluate(ctx: RateLimitContextDto): RateLimitDecisionDto? {
        if (!enabled) return null

        checkRule("api_key", maskApiKeyForScope(ctx.virtualApiKey), apiKeyRatePerSecond)?.let { return it }
        checkRule("user", ctx.userId.toString(), userRatePerSecond)?.let { return it }
        checkRule("model", ctx.modelAlias, modelRatePerSecond)?.let { return it }
        checkRule("vendor", ctx.vendorId.toString(), vendorRatePerSecond)?.let { return it }
        checkRule("global", "all", globalRatePerSecond)?.let { return it }
        return null
    }

    /**
     * 执行单个维度的限流检查，并在命中时构造统一决策对象。
     * @param [scope] 范围
     * @param [targetId] 目标ID
     * @param [permitsPerSecond] 每秒许可
     * @return [RateLimitDecision?]
     */
    private fun checkRule(scope: String, targetId: String, permitsPerSecond: Long): RateLimitDecisionDto? {
        if (permitsPerSecond <= 0) return null
        val result = rateLimiterProvider.tryAcquire(scope, targetId, permitsPerSecond)
        if (result.allowed) return null
        val resetAt = Instant.now().epochSecond + result.retryAfterSeconds
        return RateLimitDecisionDto(
            source = "gateway",
            scope = scope,
            targetId = targetId,
            retryAfterSeconds = result.retryAfterSeconds,
            limit = "${permitsPerSecond}/s",
            remaining = result.remainingPermits.toString(),
            resetEpochSecond = resetAt.toString(),
        )
    }

    /** 对虚拟密钥做脱敏，避免把完整密钥作为限流维度标识暴露。 */
    private fun maskApiKeyForScope(virtualApiKey: String): String {
        if (virtualApiKey.length <= 8) return virtualApiKey
        return "${virtualApiKey.take(8)}****${virtualApiKey.takeLast(4)}"
    }
}
