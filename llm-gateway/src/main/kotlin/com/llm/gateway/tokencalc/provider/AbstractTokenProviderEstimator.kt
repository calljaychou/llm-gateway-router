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
import com.llm.gateway.tokencalc.text.TokenTextExtractResult
import kotlin.math.ceil

/**
 * Token 协议估算抽象基类。
 * 负责统一估算流程、usage 合并、本地兜底估算和默认明细构造；子类只处理协议差异。
 */
abstract class AbstractTokenProviderEstimator : TokenProviderEstimator {

    companion object {
        private const val DEFAULT_ENCODING = "cl100k_base"
        private const val CHARS_PER_TOKEN = 4.0
    }

    protected val log = logger()

    protected abstract val protocol: TokenProtocol

    protected abstract val providerName: String

    override fun supports(params: TokenEstimateParams): Boolean {
        return params.protocol == protocol
    }

    /**
     * 执行通用 Token 估算流程。
     * 子类负责协议文本提取、usage 提取和 Token 明细构造。
     */
    override fun estimate(params: TokenEstimateParams): TokenEstimateResult {
        val prompt = buildPromptText(params)
        val completion = buildCompletionText(params)
        val responseUsage = extractResponseUsage(params)
        val hasCallerUsage = params.reportedUsage?.let { sanitizeUsage(it).hasAny() } == true
        val hasResponseUsage = sanitizeUsage(responseUsage).hasAny()
        val reportedUsage = mergeReportedUsage(params.reportedUsage, responseUsage)
        val estimatedUsage = buildEstimatedUsage(prompt, completion)
        val usage = when {
            reportedUsage.hasAny() && TokenUsageNormalizer.usageNeedsMerge(reportedUsage) ->
                TokenUsageNormalizer.mergeUsage(reportedUsage, estimatedUsage)
            reportedUsage.hasAny() -> TokenUsageNormalizer.normalizeUsage(reportedUsage)
            else -> estimatedUsage
        }
        val source = resolveSource(reportedUsage, responseUsage, estimatedUsage)
        val details = buildTokenDetails(params, usage, source, prompt, completion)
        val resolvedModel = resolveModel(params)

        log.info(
            "{} Token计算完成 protocol={}, source={}, requestModel={}, upstreamModel={}, resolvedModel={}, stream={}, inputTokens={}, outputTokens={}, totalTokens={}, promptTextLen={}, completionTextLen={}",
            providerName,
            protocol.value,
            source.value,
            params.requestModel,
            params.upstreamModel,
            resolvedModel,
            params.stream,
            usage.inputTokens,
            usage.outputTokens,
            usage.totalTokens,
            prompt.text.length,
            completion.text.length,
        )

        return TokenEstimateResult(
            usage = usage,
            tokenDetails = details,
            resolvedModel = resolvedModel,
            encoding = DEFAULT_ENCODING,
            source = source,
            supported = true,
            note = buildNote(source, hasCallerUsage, hasResponseUsage, listOf(prompt.note, completion.note)),
            promptTextLen = prompt.text.length,
            completionTextLen = completion.text.length,
            calcDetail = buildCalcDetail(params, usage, details, resolvedModel),
        )
    }

    protected abstract fun buildPromptText(params: TokenEstimateParams): TokenTextExtractResult

    protected abstract fun buildCompletionText(params: TokenEstimateParams): TokenTextExtractResult

    protected abstract fun extractUsageFromObject(root: JSONObject): TokenUsageSummaryDto

    protected abstract fun buildTokenDetails(
        params: TokenEstimateParams,
        usage: TokenUsageSummaryDto,
        source: TokenEstimateSource,
        prompt: TokenTextExtractResult,
        completion: TokenTextExtractResult,
    ): List<TokenDetailDto>

    protected open fun extractUsageFromStreamEvent(event: JSONObject): TokenUsageSummaryDto {
        return extractUsageFromObject(event)
    }

    protected open fun extractRequestModel(requestBody: String?): String? {
        return parseObject(requestBody)?.getString("model")?.takeIf { it.isNotBlank() }
    }

    protected open fun extractResponseModel(responseBody: String?, stream: Boolean): String? {
        val root = TokenTextBuilder.parseResponseObject(responseBody, stream) ?: return null
        return root.getString("model")?.takeIf { it.isNotBlank() }
    }

    /** 生成跨协议通用 Token 明细，协议无专有 usage details 时可直接复用。 */
    protected fun buildDefaultTokenDetails(
        usage: TokenUsageSummaryDto,
        source: TokenEstimateSource,
        prompt: TokenTextExtractResult,
        completion: TokenTextExtractResult,
    ): List<TokenDetailDto> {
        val detailSource = when (source) {
            TokenEstimateSource.REPORTED_USAGE -> TokenDetailSource.REPORTED
            TokenEstimateSource.MERGED -> TokenDetailSource.MERGED
            else -> TokenDetailSource.LOCAL
        }
        if (source == TokenEstimateSource.REPORTED_USAGE) {
            return TokenUsageNormalizer.normalizeDetails(
                listOf(
                    buildDetail(
                        direction = TokenDirection.INPUT,
                        tokenType = TokenType.TEXT,
                        tokens = usage.inputTokens,
                        source = TokenDetailSource.REPORTED,
                    ),
                    buildDetail(
                        direction = TokenDirection.OUTPUT,
                        tokenType = TokenType.TEXT,
                        tokens = usage.outputTokens,
                        source = TokenDetailSource.REPORTED,
                    ),
                )
            )
        }

        val inputImageTokens = prompt.imageTokens.takeIf { usage.inputTokens >= prompt.extraTokens } ?: 0
        val inputAudioTokens = prompt.audioTokens.takeIf { usage.inputTokens >= prompt.extraTokens } ?: 0
        val inputFileTokens = prompt.fileTokens.takeIf { usage.inputTokens >= prompt.extraTokens } ?: 0
        val outputImageTokens = completion.imageTokens.takeIf { usage.outputTokens >= completion.extraTokens } ?: 0
        val outputAudioTokens = completion.audioTokens.takeIf { usage.outputTokens >= completion.extraTokens } ?: 0
        val outputFileTokens = completion.fileTokens.takeIf { usage.outputTokens >= completion.extraTokens } ?: 0
        val inputTextTokens = (usage.inputTokens - inputImageTokens - inputAudioTokens - inputFileTokens)
            .coerceAtLeast(0)
        val outputTextTokens = (usage.outputTokens - outputImageTokens - outputAudioTokens - outputFileTokens)
            .coerceAtLeast(0)

        return TokenUsageNormalizer.normalizeDetails(
            listOf(
                buildDetail(TokenDirection.INPUT, TokenType.TEXT, tokens = inputTextTokens, source = detailSource),
                buildDetail(TokenDirection.INPUT, TokenType.IMAGE, tokens = inputImageTokens, source = TokenDetailSource.LOCAL, note = "placeholder"),
                buildDetail(TokenDirection.INPUT, TokenType.AUDIO, tokens = inputAudioTokens, source = TokenDetailSource.LOCAL, note = "placeholder"),
                buildDetail(TokenDirection.INPUT, TokenType.FILE, tokens = inputFileTokens, source = TokenDetailSource.LOCAL, note = "placeholder"),
                buildDetail(TokenDirection.OUTPUT, TokenType.TEXT, tokens = outputTextTokens, source = detailSource),
                buildDetail(TokenDirection.OUTPUT, TokenType.IMAGE, tokens = outputImageTokens, source = TokenDetailSource.LOCAL, note = "placeholder"),
                buildDetail(TokenDirection.OUTPUT, TokenType.AUDIO, tokens = outputAudioTokens, source = TokenDetailSource.LOCAL, note = "placeholder"),
                buildDetail(TokenDirection.OUTPUT, TokenType.FILE, tokens = outputFileTokens, source = TokenDetailSource.LOCAL, note = "placeholder"),
            )
        )
    }

    /** 构造单条 Token 明细，支持协议子类补充缓存类型、上游字段和说明。 */
    protected fun buildDetail(
        direction: TokenDirection,
        tokenType: TokenType,
        cacheType: TokenCacheType = TokenCacheType.NONE,
        tokens: Int,
        source: TokenDetailSource,
        providerField: String? = null,
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

    /** 从响应体或流式事件中提取上游 usage。 */
    private fun extractResponseUsage(params: TokenEstimateParams): TokenUsageSummaryDto {
        if (params.responseBody.isNullOrBlank()) return TokenUsageSummaryDto()
        return if (params.stream) {
            TokenTextBuilder.parseStreamEvents(params.responseBody)
                .fold(TokenUsageSummaryDto()) { current, event ->
                    mergeExtractedUsage(current, extractUsageFromStreamEvent(event))
                }
        } else {
            parseObject(params.responseBody)
                ?.let { sanitizeUsage(extractUsageFromObject(it)) }
                ?: TokenUsageSummaryDto()
        }
    }

    /** 按调用方显式 usage 优先、响应体 usage 兜底的顺序合并上游 usage。 */
    private fun mergeReportedUsage(
        callerUsage: TokenUsageSummaryDto?,
        responseUsage: TokenUsageSummaryDto,
    ): TokenUsageSummaryDto {
        val sanitizedCaller = callerUsage?.let { sanitizeUsage(it) } ?: TokenUsageSummaryDto()
        val sanitizedResponse = sanitizeUsage(responseUsage)
        if (!sanitizedCaller.hasAny()) return sanitizedResponse
        return sanitizeUsage(
            TokenUsageSummaryDto(
                inputTokens = sanitizedCaller.inputTokens.takeIf { it > 0 } ?: sanitizedResponse.inputTokens,
                outputTokens = sanitizedCaller.outputTokens.takeIf { it > 0 } ?: sanitizedResponse.outputTokens,
                totalTokens = sanitizedCaller.totalTokens.takeIf { it > 0 } ?: sanitizedResponse.totalTokens,
            )
        )
    }

    /** 合并流式事件中的 usage 字段，保留缺失字段状态用于来源判断。 */
    private fun mergeExtractedUsage(
        current: TokenUsageSummaryDto,
        incoming: TokenUsageSummaryDto,
    ): TokenUsageSummaryDto {
        val sanitizedCurrent = sanitizeUsage(current)
        val sanitizedIncoming = sanitizeUsage(incoming)
        return TokenUsageSummaryDto(
            inputTokens = sanitizedIncoming.inputTokens.takeIf { it > 0 } ?: sanitizedCurrent.inputTokens,
            outputTokens = sanitizedIncoming.outputTokens.takeIf { it > 0 } ?: sanitizedCurrent.outputTokens,
            totalTokens = sanitizedIncoming.totalTokens.takeIf { it > 0 } ?: sanitizedCurrent.totalTokens,
        )
    }

    /** 仅清理负数，保留缺失字段状态用于 MERGED 来源判断。 */
    private fun sanitizeUsage(usage: TokenUsageSummaryDto): TokenUsageSummaryDto {
        return TokenUsageSummaryDto(
            inputTokens = usage.inputTokens.coerceAtLeast(0),
            outputTokens = usage.outputTokens.coerceAtLeast(0),
            totalTokens = usage.totalTokens.coerceAtLeast(0),
        )
    }

    /** 按当前轻量策略估算文本和多模态占位 Token。 */
    private fun buildEstimatedUsage(
        prompt: TokenTextExtractResult,
        completion: TokenTextExtractResult,
    ): TokenUsageSummaryDto {
        val inputTokens = estimateTextTokens(prompt.text) + prompt.extraTokens
        val outputTokens = estimateTextTokens(completion.text) + completion.extraTokens
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

    /** 判断本次结果来源。 */
    private fun resolveSource(
        reportedUsage: TokenUsageSummaryDto,
        responseUsage: TokenUsageSummaryDto,
        estimatedUsage: TokenUsageSummaryDto,
    ): TokenEstimateSource {
        if (reportedUsage.hasAny() && !TokenUsageNormalizer.usageNeedsMerge(reportedUsage)) {
            return TokenEstimateSource.REPORTED_USAGE
        }
        if (reportedUsage.hasAny() && estimatedUsage.hasAny()) return TokenEstimateSource.MERGED
        if (responseUsage.hasAny()) return TokenEstimateSource.MERGED
        return TokenEstimateSource.LOCAL_ESTIMATE
    }

    /** 解析最终模型名。 */
    private fun resolveModel(params: TokenEstimateParams): String {
        return params.upstreamModel
            ?: params.requestModel
            ?: extractRequestModel(params.requestBody)
            ?: extractResponseModel(params.responseBody, params.stream)
            ?: ""
    }

    /** 输出计算说明。 */
    private fun buildNote(
        source: TokenEstimateSource,
        hasCallerUsage: Boolean,
        hasResponseUsage: Boolean,
        extractNotes: List<String>,
    ): String {
        val notes = linkedSetOf<String>()
        when (source) {
            TokenEstimateSource.REPORTED_USAGE -> {
                notes.add(if (hasCallerUsage) "使用调用方显式 reportedUsage 作为 Token 结果" else "使用响应体或流式事件中的上游 usage 作为 Token 结果")
            }
            TokenEstimateSource.LOCAL_ESTIMATE -> notes.add("上游未返回 usage，使用本地轻量估算结果")
            TokenEstimateSource.MERGED -> notes.add("上游或调用方 usage 不完整，使用本地估算补齐缺失字段")
            TokenEstimateSource.UNSUPPORTED -> notes.add("当前协议暂不支持 Token 计算")
        }
        extractNotes.filter { it.isNotBlank() }.forEach { notes.add(it) }
        return notes.joinToString("; ")
    }

    /** 构造计算快照，后续落库到 token_calc_detail。 */
    private fun buildCalcDetail(
        params: TokenEstimateParams,
        usage: TokenUsageSummaryDto,
        details: List<TokenDetailDto>,
        resolvedModel: String,
    ): String {
        return JSON.toJSONString(
            mapOf(
                "protocol" to params.protocol.value,
                "requestModel" to params.requestModel,
                "upstreamModel" to params.upstreamModel,
                "resolvedModel" to resolvedModel,
                "usage" to usage,
                "tokenDetails" to details,
            )
        )
    }

    /** 解析 JSON 对象，失败时按空对象兜底。 */
    protected fun parseObject(body: String?): JSONObject? {
        if (body.isNullOrBlank()) return null
        return runCatching { JSON.parseObject(body) }
            .onFailure {
                log.warn("{} Token响应JSON解析失败 bodyLength={}, error={}", providerName, body.length, it.message)
            }.getOrNull()
    }
}
