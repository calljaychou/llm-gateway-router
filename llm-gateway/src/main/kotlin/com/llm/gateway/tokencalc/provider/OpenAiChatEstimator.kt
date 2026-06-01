package com.llm.gateway.tokencalc.provider

import com.alibaba.fastjson2.JSON
import com.alibaba.fastjson2.JSONObject
import com.llm.gateway.common.logger
import com.llm.gateway.tokencalc.TokenUsageNormalizer
import com.llm.gateway.tokencalc.model.TokenCacheType
import com.llm.gateway.tokencalc.model.TokenDetailDto
import com.llm.gateway.tokencalc.model.TokenDetailSource
import com.llm.gateway.tokencalc.model.TokenDirection
import com.llm.gateway.tokencalc.model.TokenEstimateParams
import com.llm.gateway.tokencalc.model.TokenEstimateResult
import com.llm.gateway.tokencalc.model.TokenEstimateSource
import com.llm.gateway.tokencalc.model.TokenProtocol
import com.llm.gateway.tokencalc.model.TokenType
import com.llm.gateway.tokencalc.model.TokenUsageSummaryDto
import com.llm.gateway.tokencalc.text.TokenTextBuilder
import org.springframework.stereotype.Component
import kotlin.math.ceil

@Component
class OpenAiChatEstimator : TokenProviderEstimator {

    companion object {
        private const val DEFAULT_ENCODING = "cl100k_base"
        private const val CHARS_PER_TOKEN = 4.0
    }

    private val log = logger()

    override fun supports(params: TokenEstimateParams): Boolean {
        return params.protocol == TokenProtocol.OPENAI_CHAT
    }

    /**
     * 计算 OpenAI Chat Completions 的 Token 使用情况。
     *
     * 计算优先级：
     * 1. 上游 response.usage 完整时直接使用；
     * 2. response.usage 缺少汇总字段时使用本地估算补齐；
     * 3. response.usage 不存在时完全使用本地轻量估算。
     *
     * 注意：本阶段不读取数据库、不计算金额、不做落库，输出结果供后续计费和日志服务消费。
     */
    override fun estimate(params: TokenEstimateParams): TokenEstimateResult {
        val promptText = TokenTextBuilder.buildOpenAiChatPromptText(params.requestBody)
        val completionText = TokenTextBuilder.buildOpenAiChatCompletionText(params.responseBody)
        val usageObject = parseUsageObject(params.responseBody)
        val reportedUsage = extractReportedUsage(params, usageObject)
        val estimatedUsage = buildEstimatedUsage(promptText, completionText)
        val usage = when {
            reportedUsage.hasAny() && usageNeedsMerge(params, usageObject) ->
                TokenUsageNormalizer.mergeUsage(reportedUsage, estimatedUsage)
            reportedUsage.hasAny() -> TokenUsageNormalizer.normalizeUsage(reportedUsage)
            else -> estimatedUsage
        }
        val source = resolveSource(params, usageObject)
        val details = buildTokenDetails(params, usage, source)
        logEstimateSummary(params, usageObject, usage, details, source, promptText.length, completionText.length)

        return TokenEstimateResult(
            usage = usage,
            tokenDetails = details,
            resolvedModel = params.upstreamModel ?: params.requestModel.orEmpty(),
            encoding = DEFAULT_ENCODING,
            source = source,
            supported = true,
            note = buildNote(source),
            promptTextLen = promptText.length,
            completionTextLen = completionText.length,
            calcDetail = buildCalcDetail(params, usage, details),
        )
    }

    /** 提取上游 response.usage 或调用方显式传入的 usage 汇总。 */
    private fun extractReportedUsage(params: TokenEstimateParams, usage: JSONObject?): TokenUsageSummaryDto {
        val explicitUsage = params.reportedUsage
        if (explicitUsage != null && explicitUsage.hasAny()) {
            log.info(
                "Token计算使用显式reportedUsage requestModel={}, upstreamModel={}, inputTokens={}, outputTokens={}, totalTokens={}",
                params.requestModel,
                params.upstreamModel,
                explicitUsage.inputTokens,
                explicitUsage.outputTokens,
                explicitUsage.totalTokens,
            )
            return TokenUsageNormalizer.normalizeUsage(explicitUsage)
        }
        if (usage == null) {
            log.info(
                "Token计算未发现上游usage, 使用本地估算 requestModel={}, upstreamModel={}",
                params.requestModel,
                params.upstreamModel,
            )
            return TokenUsageSummaryDto()
        }
        return TokenUsageNormalizer.normalizeUsage(
            TokenUsageSummaryDto(
                inputTokens = usage.getIntValue("prompt_tokens"),
                outputTokens = usage.getIntValue("completion_tokens"),
                totalTokens = usage.getIntValue("total_tokens"),
            )
        )
    }

    /** 按当前轻量策略估算 Token，后续可替换为真实 tokenizer。 */
    private fun buildEstimatedUsage(promptText: String, completionText: String): TokenUsageSummaryDto {
        val inputTokens = estimateTextTokens(promptText)
        val outputTokens = estimateTextTokens(completionText)
        return TokenUsageNormalizer.normalizeUsage(
            TokenUsageSummaryDto(
                inputTokens = inputTokens,
                outputTokens = outputTokens,
                totalTokens = inputTokens + outputTokens,
            )
        )
    }

    /** 根据中文、英文和 JSON 文本的平均字符密度做兜底估算。 */
    private fun estimateTextTokens(text: String): Int {
        if (text.isBlank()) return 0
        return ceil(text.length / CHARS_PER_TOKEN).toInt().coerceAtLeast(1)
    }

    /** 生成可计费 Token 明细，优先使用上游 usage details。 */
    private fun buildTokenDetails(
        params: TokenEstimateParams,
        usage: TokenUsageSummaryDto,
        source: TokenEstimateSource,
    ): List<TokenDetailDto> {
        val usageObject = parseUsageObject(params.responseBody)
        if (usageObject == null) {
            log.info(
                "Token明细使用本地估算 inputTokens={}, outputTokens={}, totalTokens={}",
                usage.inputTokens,
                usage.outputTokens,
                usage.totalTokens,
            )
            return localTextDetails(usage)
        }

        val detailSource = if (source == TokenEstimateSource.MERGED) TokenDetailSource.MERGED else TokenDetailSource.REPORTED
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

    /** 构造单条 Token 明细。 */
    private fun buildDetail(
        direction: TokenDirection,
        tokenType: TokenType,
        cacheType: TokenCacheType,
        tokens: Int,
        source: TokenDetailSource,
        providerField: String?,
        note: String? = null,
    ): TokenDetailDto {
        return TokenDetailDto(
            direction = direction,
            tokenType = tokenType,
            cacheType = cacheType,
            tokens = tokens,
            billableTokens = tokens,
            source = source,
            providerField = providerField,
            note = note,
        )
    }

    /** 判断最终计算来源。 */
    private fun resolveSource(params: TokenEstimateParams, usage: JSONObject?): TokenEstimateSource {
        if (params.reportedUsage?.hasAny() == true) return TokenEstimateSource.REPORTED_USAGE
        if (usage == null) return TokenEstimateSource.LOCAL_ESTIMATE
        return if (usageNeedsMerge(params, usage)) TokenEstimateSource.MERGED else TokenEstimateSource.REPORTED_USAGE
    }

    /** 判断上游 usage 汇总字段是否完整。 */
    private fun usageNeedsMerge(params: TokenEstimateParams, usage: JSONObject?): Boolean {
        if (params.reportedUsage?.hasAny() == true) return false
        if (usage == null) return false
        val missing = !usage.containsKey("prompt_tokens") ||
            !usage.containsKey("completion_tokens") ||
            !usage.containsKey("total_tokens")
        if (missing) {
            log.warn(
                "上游usage汇总字段不完整, 将进行MERGED补齐 requestModel={}, upstreamModel={}, hasPromptTokens={}, hasCompletionTokens={}, hasTotalTokens={}",
                params.requestModel,
                params.upstreamModel,
                usage.containsKey("prompt_tokens"),
                usage.containsKey("completion_tokens"),
                usage.containsKey("total_tokens"),
            )
        }
        return missing
    }

    /** 输出计算说明。 */
    private fun buildNote(source: TokenEstimateSource): String {
        return when (source) {
            TokenEstimateSource.REPORTED_USAGE -> "使用上游 response.usage 作为 Token 结果"
            TokenEstimateSource.LOCAL_ESTIMATE -> "上游未返回 usage，使用本地轻量估算结果"
            TokenEstimateSource.MERGED -> "上游 usage 不完整，使用本地估算补齐缺失字段"
            TokenEstimateSource.UNSUPPORTED -> "当前协议暂不支持 Token 计算"
        }
    }

    /** 构造计算快照，后续落库到 token_calc_detail。 */
    private fun buildCalcDetail(
        params: TokenEstimateParams,
        usage: TokenUsageSummaryDto,
        details: List<TokenDetailDto>,
    ): String {
        return JSON.toJSONString(
            mapOf(
                "protocol" to params.protocol.value,
                "requestModel" to params.requestModel,
                "upstreamModel" to params.upstreamModel,
                "usage" to usage,
                "tokenDetails" to details,
            )
        )
    }

    /** 解析 responseBody.usage。 */
    private fun parseUsageObject(responseBody: String?): JSONObject? {
        if (responseBody.isNullOrBlank()) return null
        return runCatching { JSON.parseObject(responseBody).getJSONObject("usage") }
            .onFailure {
                log.warn(
                    "解析OpenAI Chat响应usage失败 responseBodyLength={}, error={}",
                    responseBody.length,
                    it.message,
                )
            }.getOrNull()
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

    /** 输出 Token 计算关键摘要，避免打印原始 prompt 或 completion 内容。 */
    private fun logEstimateSummary(
        params: TokenEstimateParams,
        usageObject: JSONObject?,
        usage: TokenUsageSummaryDto,
        details: List<TokenDetailDto>,
        source: TokenEstimateSource,
        promptTextLen: Int,
        completionTextLen: Int,
    ) {
        log.info(
            "OpenAI Chat Token计算完成 source={}, requestModel={}, upstreamModel={}, usageExists={}, inputTokens={}, outputTokens={}, totalTokens={}, detailCount={}, promptTextLen={}, completionTextLen={}",
            source.value,
            params.requestModel,
            params.upstreamModel,
            usageObject != null,
            usage.inputTokens,
            usage.outputTokens,
            usage.totalTokens,
            details.size,
            promptTextLen,
            completionTextLen,
        )
    }
}
