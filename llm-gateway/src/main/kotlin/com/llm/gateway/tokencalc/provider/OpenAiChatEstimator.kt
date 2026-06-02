package com.llm.gateway.tokencalc.provider

import com.alibaba.fastjson2.JSONObject
import com.llm.gateway.tokencalc.TokenUsageNormalizer
import com.llm.gateway.tokencalc.model.TokenCacheType
import com.llm.gateway.tokencalc.model.TokenDetailDto
import com.llm.gateway.tokencalc.model.TokenDetailSource
import com.llm.gateway.tokencalc.model.TokenDirection
import com.llm.gateway.tokencalc.model.TokenEstimateParams
import com.llm.gateway.tokencalc.model.TokenEstimateSource
import com.llm.gateway.tokencalc.model.TokenProtocol
import com.llm.gateway.tokencalc.model.TokenType
import com.llm.gateway.tokencalc.model.TokenUsageSummaryDto
import com.llm.gateway.tokencalc.text.TokenTextBuilder
import com.llm.gateway.tokencalc.text.TokenTextExtractResult
import org.springframework.stereotype.Component

@Component
class OpenAiChatEstimator : AbstractTokenProviderEstimator() {

    override val protocol: TokenProtocol = TokenProtocol.OPENAI_CHAT

    override val providerName: String = "OpenAI Chat"

    override fun buildPromptText(params: TokenEstimateParams): TokenTextExtractResult {
        return TokenTextExtractResult(text = TokenTextBuilder.buildOpenAiChatPromptText(params.requestBody))
    }

    override fun buildCompletionText(params: TokenEstimateParams): TokenTextExtractResult {
        return TokenTextExtractResult(text = TokenTextBuilder.buildOpenAiChatCompletionText(params.responseBody))
    }

    /** 提取 OpenAI Chat response.usage 汇总字段。 */
    override fun extractUsageFromObject(root: JSONObject): TokenUsageSummaryDto {
        val usage = root.getJSONObject("usage") ?: return TokenUsageSummaryDto()
        return TokenUsageSummaryDto(
            inputTokens = usage.getIntValue("prompt_tokens"),
            outputTokens = usage.getIntValue("completion_tokens"),
            totalTokens = usage.getIntValue("total_tokens"),
        )
    }

    /** 生成可计费 Token 明细，保留 OpenAI Chat 专有 usage details。 */
    override fun buildTokenDetails(
        params: TokenEstimateParams,
        usage: TokenUsageSummaryDto,
        source: TokenEstimateSource,
        prompt: TokenTextExtractResult,
        completion: TokenTextExtractResult,
    ): List<TokenDetailDto> {
        val usageObject = parseUsageObject(params.responseBody)
        if (usageObject == null) {
            log.info(
                "OpenAI Chat Token明细使用本地估算 inputTokens={}, outputTokens={}, totalTokens={}",
                usage.inputTokens,
                usage.outputTokens,
                usage.totalTokens,
            )
            return localTextDetails(usage)
        }

        val detailSource = when (source) {
            TokenEstimateSource.REPORTED_USAGE -> TokenDetailSource.REPORTED
            TokenEstimateSource.MERGED -> TokenDetailSource.MERGED
            else -> TokenDetailSource.LOCAL
        }
        val promptDetails = usageObject.getJSONObject("prompt_tokens_details")
        val completionDetails = usageObject.getJSONObject("completion_tokens_details")
        val promptCachedTokens = promptDetails.getPromptCachedTokens(usageObject)
        val promptAudioTokens = promptDetails.getTokenDetail("audio_tokens")
        val promptCacheMissTokens = promptDetails.getPromptCacheMissTokens(
            usage = usageObject,
            promptTokens = usage.inputTokens,
            promptCachedTokens = promptCachedTokens,
            promptAudioTokens = promptAudioTokens,
        )
        val completionReasoningTokens = completionDetails.getTokenDetail("reasoning_tokens")
        val completionAudioTokens = completionDetails.getTokenDetail("audio_tokens")
        val acceptedPredictionTokens = completionDetails.getTokenDetail("accepted_prediction_tokens")
        val rejectedPredictionTokens = completionDetails.getTokenDetail("rejected_prediction_tokens")
        val inputTextTokens = (usage.inputTokens - promptCachedTokens - promptCacheMissTokens - promptAudioTokens)
            .coerceAtLeast(0)
        val outputTextTokens = (
            usage.outputTokens -
                completionReasoningTokens -
                completionAudioTokens -
                acceptedPredictionTokens -
                rejectedPredictionTokens
            ).coerceAtLeast(0)
        log.info(
            "OpenAI Chat usage明细解析 inputCachedTokens={}, inputCacheMissTokens={}, inputAudioTokens={}, outputReasoningTokens={}, outputAudioTokens={}, acceptedPredictionTokens={}, rejectedPredictionTokens={}",
            promptCachedTokens,
            promptCacheMissTokens,
            promptAudioTokens,
            completionReasoningTokens,
            completionAudioTokens,
            acceptedPredictionTokens,
            rejectedPredictionTokens,
        )

        return TokenUsageNormalizer.normalizeDetails(
            listOf(
                buildDetail(TokenDirection.INPUT, TokenType.TEXT, TokenCacheType.CACHE_HIT, promptCachedTokens, TokenDetailSource.REPORTED, resolveCacheHitField(usageObject)),
                buildDetail(TokenDirection.INPUT, TokenType.TEXT, TokenCacheType.CACHE_MISS, promptCacheMissTokens, resolveCacheMissSource(usageObject), resolveCacheMissField(usageObject)),
                buildDetail(TokenDirection.INPUT, TokenType.AUDIO, TokenCacheType.NONE, promptAudioTokens, TokenDetailSource.REPORTED, "prompt_tokens_details.audio_tokens"),
                buildDetail(TokenDirection.INPUT, TokenType.TEXT, TokenCacheType.NONE, inputTextTokens, detailSource, "prompt_tokens"),
                buildDetail(TokenDirection.OUTPUT, TokenType.REASONING, TokenCacheType.NONE, completionReasoningTokens, TokenDetailSource.REPORTED, "completion_tokens_details.reasoning_tokens"),
                buildDetail(TokenDirection.OUTPUT, TokenType.AUDIO, TokenCacheType.NONE, completionAudioTokens, TokenDetailSource.REPORTED, "completion_tokens_details.audio_tokens"),
                buildDetail(TokenDirection.OUTPUT, TokenType.PREDICTION, TokenCacheType.NONE, acceptedPredictionTokens, TokenDetailSource.REPORTED, "completion_tokens_details.accepted_prediction_tokens", "accepted"),
                buildDetail(TokenDirection.OUTPUT, TokenType.PREDICTION, TokenCacheType.NONE, rejectedPredictionTokens, TokenDetailSource.REPORTED, "completion_tokens_details.rejected_prediction_tokens", "rejected"),
                buildDetail(TokenDirection.OUTPUT, TokenType.TEXT, TokenCacheType.NONE, outputTextTokens, detailSource, "completion_tokens"),
            )
        )
    }

    /** 构造纯本地估算明细。 */
    private fun localTextDetails(usage: TokenUsageSummaryDto): List<TokenDetailDto> {
        return TokenUsageNormalizer.normalizeDetails(
            listOf(
                buildDetail(TokenDirection.INPUT, TokenType.TEXT, TokenCacheType.NONE, usage.inputTokens, TokenDetailSource.LOCAL, null),
                buildDetail(TokenDirection.OUTPUT, TokenType.TEXT, TokenCacheType.NONE, usage.outputTokens, TokenDetailSource.LOCAL, null),
            )
        )
    }

    /** 解析 responseBody.usage。 */
    private fun parseUsageObject(responseBody: String?): JSONObject? {
        return parseObject(responseBody)?.getJSONObject("usage")
    }

    /** 提取 usage detail 字段，缺失或负数按 0 处理。 */
    private fun JSONObject?.getTokenDetail(fieldName: String): Int {
        return this?.getIntValue(fieldName)?.coerceAtLeast(0) ?: 0
    }

    /** 提取输入缓存命中 Token，兼容 DeepSeek 的 prompt_cache_hit_tokens。 */
    private fun JSONObject?.getPromptCachedTokens(usage: JSONObject): Int {
        return getTokenDetail("cached_tokens").takeIf { it > 0 }
            ?: usage.getIntValue("prompt_cache_hit_tokens").coerceAtLeast(0)
    }

    /** 提取输入缓存未命中 Token，OpenAI 无显式字段时按输入总量减缓存命中推导。 */
    private fun JSONObject?.getPromptCacheMissTokens(
        usage: JSONObject,
        promptTokens: Int,
        promptCachedTokens: Int,
        promptAudioTokens: Int,
    ): Int {
        if (usage.containsKey("prompt_cache_miss_tokens")) {
            return usage.getIntValue("prompt_cache_miss_tokens").coerceAtLeast(0)
        }
        return (promptTokens - promptCachedTokens - promptAudioTokens).coerceAtLeast(0)
    }

    /** 确定 cache hit 对应的上游字段名。 */
    private fun resolveCacheHitField(usage: JSONObject): String {
        return if (usage.containsKey("prompt_cache_hit_tokens")) {
            "prompt_cache_hit_tokens"
        } else {
            "prompt_tokens_details.cached_tokens"
        }
    }

    /** 确定 cache miss 明细来源。 */
    private fun resolveCacheMissSource(usage: JSONObject): TokenDetailSource {
        return if (usage.containsKey("prompt_cache_miss_tokens")) TokenDetailSource.REPORTED else TokenDetailSource.DERIVED
    }

    /** 确定 cache miss 对应的上游字段名。 */
    private fun resolveCacheMissField(usage: JSONObject): String {
        return if (usage.containsKey("prompt_cache_miss_tokens")) {
            "prompt_cache_miss_tokens"
        } else {
            "prompt_tokens - cached_tokens"
        }
    }
}
