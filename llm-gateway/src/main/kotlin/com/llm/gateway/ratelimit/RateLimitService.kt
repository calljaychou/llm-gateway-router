package com.llm.gateway.ratelimit

import com.llm.gateway.model.dto.RateLimitContextDto
import com.llm.gateway.model.dto.RateLimitDecisionDto

interface RateLimitService {

    /**
     * 评估
     * @param [ctx] 上下文
     * @return [RateLimitDecision?]
     */
    fun evaluate(ctx: RateLimitContextDto): RateLimitDecisionDto?
}
