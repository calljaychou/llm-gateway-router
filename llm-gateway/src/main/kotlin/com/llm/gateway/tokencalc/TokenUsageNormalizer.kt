package com.llm.gateway.tokencalc

import com.llm.gateway.tokencalc.model.TokenDetailDto
import com.llm.gateway.tokencalc.model.TokenDirection
import com.llm.gateway.tokencalc.model.TokenUsageSummaryDto

object TokenUsageNormalizer {

    fun normalizeUsage(usage: TokenUsageSummaryDto): TokenUsageSummaryDto {
        val inputTokens = usage.inputTokens.coerceAtLeast(0)
        val outputTokens = usage.outputTokens.coerceAtLeast(0)
        val normalizedTotal = usage.totalTokens.coerceAtLeast(0)
        val tokenSum = inputTokens + outputTokens
        val totalTokens = when {
            normalizedTotal == 0 && tokenSum > 0 -> tokenSum
            normalizedTotal < tokenSum -> tokenSum
            else -> normalizedTotal
        }
        return TokenUsageSummaryDto(
            inputTokens = inputTokens,
            outputTokens = outputTokens,
            totalTokens = totalTokens,
        )
    }

    fun mergeUsage(reported: TokenUsageSummaryDto, estimated: TokenUsageSummaryDto): TokenUsageSummaryDto {
        val normalizedReported = normalizeUsage(reported)
        val normalizedEstimated = normalizeUsage(estimated)
        return normalizeUsage(
            TokenUsageSummaryDto(
                inputTokens = normalizedReported.inputTokens.takeIf { it > 0 } ?: normalizedEstimated.inputTokens,
                outputTokens = normalizedReported.outputTokens.takeIf { it > 0 } ?: normalizedEstimated.outputTokens,
                totalTokens = normalizedReported.totalTokens.takeIf { it > 0 } ?: normalizedEstimated.totalTokens,
            )
        )
    }

    fun usageNeedsMerge(usage: TokenUsageSummaryDto): Boolean {
        val normalizedUsage = normalizeUsage(usage)
        if (!normalizedUsage.hasAny()) return true
        return normalizedUsage.inputTokens == 0 ||
            normalizedUsage.outputTokens == 0 ||
            normalizedUsage.totalTokens == 0
    }

    fun normalizeDetails(details: List<TokenDetailDto>): List<TokenDetailDto> {
        return details.mapNotNull { detail ->
            val tokens = detail.tokens.coerceAtLeast(0)
            val billableTokens = detail.billableTokens.coerceAtLeast(0)
            if (tokens == 0 && billableTokens == 0) return@mapNotNull null
            detail.copy(
                tokens = tokens,
                billableTokens = billableTokens,
            )
        }
    }

    fun summarizeDetails(details: List<TokenDetailDto>): TokenUsageSummaryDto {
        val normalizedDetails = normalizeDetails(details)
        val inputTokens = normalizedDetails
            .filter { it.direction == TokenDirection.INPUT }
            .sumOf { it.tokens }
        val outputTokens = normalizedDetails
            .filter { it.direction == TokenDirection.OUTPUT }
            .sumOf { it.tokens }
        return normalizeUsage(
            TokenUsageSummaryDto(
                inputTokens = inputTokens,
                outputTokens = outputTokens,
                totalTokens = inputTokens + outputTokens,
            )
        )
    }
}
