package com.llm.gateway.tokencalc.model

/**
 * Token 计算入参。
 * 该对象只描述一次协议载荷和上游返回，不携带用户、配额、价格或数据库信息。
 */
data class TokenEstimateParams(
    /** Token 计算协议，用于选择对应 TokenProviderEstimator。 */
    val protocol: TokenProtocol,

    /** 请求中的模型名或模型别名，例如用户请求的 model 字段。 */
    val requestModel: String? = null,

    /** 实际转发到上游的真实模型名。 */
    val upstreamModel: String? = null,

    /** 是否流式请求 */
    val stream: Boolean = false,

    /** 原始请求体 JSON 字符串，用于本地估算 prompt token。 */
    val requestBody: String? = null,

    /** 原始响应体 JSON 字符串，用于提取上游 usage 或本地估算 completion token。 */
    val responseBody: String? = null,

    /** 调用方已解析出的上游 usage 汇总，存在时优先使用。 */
    val reportedUsage: TokenUsageSummaryDto? = null,
)

/**
 * Token 计算结果。
 * 后续计费、落库只能消费该结果，不应重新解析 requestBody 或 responseBody。
 */
data class TokenEstimateResult(
    /** 输入、输出和总 Token 汇总。 */
    val usage: TokenUsageSummaryDto = TokenUsageSummaryDto(),

    /** 可计费 Token 明细，按方向、类型和缓存维度拆分。 */
    val tokenDetails: List<TokenDetailDto> = emptyList(),

    /** Token 计算使用的最终模型名，优先为上游真实模型名。 */
    val resolvedModel: String = "",

    /** Token 计算使用的 encoding 名称，当前 OpenAI Chat 默认 cl100k_base。 */
    val encoding: String = "",

    /** Token 计算来源，用于审计本次结果可信度。 */
    val source: TokenEstimateSource = TokenEstimateSource.UNSUPPORTED,

    /** 当前载荷是否支持 Token 计算。 */
    val supported: Boolean = false,

    /** 计算说明，例如使用上游 usage、本地估算或合并补齐。 */
    val note: String = "",

    /** 本地提取出的 prompt 文本长度，只用于诊断估算输入规模，不落原文。 */
    val promptTextLen: Int = 0,

    /** 本地提取出的 completion 文本长度，只用于诊断估算输入规模，不落原文。 */
    val completionTextLen: Int = 0,

    /** 计算过程快照 JSON，后续可写入 llm_usage_log.token_calc_detail。 */
    val calcDetail: String = "",
)

/**
 * Token 汇总。
 * 汇总字段必须满足 totalTokens >= inputTokens + outputTokens，归一化由 TokenUsageNormalizer 处理。
 */
data class TokenUsageSummaryDto(
    /** 输入 Token 总数。 */
    val inputTokens: Int = 0,

    /** 输出 Token 总数。 */
    val outputTokens: Int = 0,

    /** 总 Token 数。 */
    val totalTokens: Int = 0,
) {
    /** 判断当前汇总是否包含任意有效 Token。 */
    fun hasAny(): Boolean {
        return inputTokens > 0 || outputTokens > 0 || totalTokens > 0
    }
}

/**
 * Token 明细。
 * 该结构是后续 Token 计费的最小输入单位，金额计算必须基于明细而不是只看汇总。
 */
data class TokenDetailDto(
    /** Token 方向：输入或输出。 */
    val direction: TokenDirection,

    /** Token 类型：文本、音频、推理、预测等。 */
    val tokenType: TokenType,

    /** 缓存类型：无缓存、缓存命中、缓存未命中或缓存写入。 */
    val cacheType: TokenCacheType = TokenCacheType.NONE,

    /** 实际 Token 数量。 */
    val tokens: Int,

    /** 参与计费的 Token 数量，默认等于 tokens，后续可按策略调整。 */
    val billableTokens: Int = tokens,

    /** 明细来源：上游返回、本地估算、合并或推导。 */
    val source: TokenDetailSource,

    /** 对应的上游原始字段名，例如 prompt_tokens_details.cached_tokens。 */
    val providerField: String? = null,

    /** 明细补充说明，例如 accepted/rejected prediction 或推导规则。 */
    val note: String? = null,
)
