package com.llm.gateway.ratelimit

import org.redisson.api.RRateLimiter
import org.redisson.api.RateIntervalUnit
import org.redisson.api.RateType
import org.redisson.api.RedissonClient
import org.springframework.stereotype.Component

@Component
class RedissonRateLimiterProvider(
    private val redissonClient: RedissonClient,
) : RateLimiterProvider {

    override fun tryAcquire(scope: String, targetId: String, permitsPerSecond: Long): RateLimiterAcquireResult {
        if (permitsPerSecond <= 0) {
            return RateLimiterAcquireResult(
                allowed = true,
                retryAfterSeconds = 0,
                remainingPermits = Long.MAX_VALUE,
            )
        }
        val limiter = buildRateLimiter(scope, targetId)
        limiter.trySetRate(RateType.OVERALL, permitsPerSecond, 1, RateIntervalUnit.SECONDS)
        val allowed = limiter.tryAcquire(1)
        val remaining = runCatching { limiter.availablePermits() }.getOrDefault(0L).coerceAtLeast(0L)
        return RateLimiterAcquireResult(
            allowed = allowed,
            retryAfterSeconds = if (allowed) 0 else 1,
            remainingPermits = remaining,
        )
    }

    /** 生成并返回指定维度的 Redisson 分布式限流器实例。 */
    private fun buildRateLimiter(scope: String, targetId: String): RRateLimiter {
        return redissonClient.getRateLimiter("RATE_LIMIT::chat::$scope::$targetId")
    }
}
