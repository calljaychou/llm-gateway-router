package com.llm.gateway.tokencalc.model

/**
 * Token 计算协议。
 * 用于路由到不同供应商或兼容协议的 Token 估算器。
 */
enum class TokenProtocol(val value: String) {
    /** OpenAI Chat Completions 协议，当前阶段优先支持 /v1/chat/completions。 */
    OPENAI_CHAT("openai_chat"),

    /** OpenAI Responses 协议，预留给后续 /v1/responses。 */
    OPENAI_RESPONSES("openai_responses"),

    /** Anthropic Messages 协议，预留给后续 Claude messages。 */
    ANTHROPIC_MESSAGES("anthropic_messages"),

    /** Gemini generateContent 协议，预留给后续 Google Gemini。 */
    GEMINI_CONTENTS("gemini_contents"),
}

/**
 * Token 方向。
 * 对应落库表 llm_usage_token_detail.token_direction。
 */
enum class TokenDirection(val value: String) {
    /** 输入 Token，包括 prompt、system、tools、response_format、输入音频等。 */
    INPUT("INPUT"),

    /** 输出 Token，包括 assistant 回复、reasoning、输出音频、prediction 等。 */
    OUTPUT("OUTPUT"),
}

/**
 * Token 类型。
 * 用于拆分不同内容形态和特殊计费维度。
 */
enum class TokenType(val value: String) {
    /** 普通文本 Token。 */
    TEXT("TEXT"),

    /** 音频 Token，包括输入音频或输出音频。 */
    AUDIO("AUDIO"),

    /** 图片 Token，预留给多模态输入。 */
    IMAGE("IMAGE"),

    /** 文件 Token，预留给文件输入或检索上下文。 */
    FILE("FILE"),

    /** 推理 Token，例如 reasoning_tokens。 */
    REASONING("REASONING"),

    /** 工具调用相关 Token，例如 tool call 参数或工具定义。 */
    TOOL("TOOL"),

    /** 预测输出 Token，例如 accepted/rejected prediction tokens。 */
    PREDICTION("PREDICTION"),

    /** 无法归入以上类别的 Token。 */
    OTHER("OTHER"),
}

/**
 * Token 缓存类型。
 * 用于区分普通输入、缓存命中、缓存未命中和缓存写入，后续计费会按该维度拆价。
 */
enum class TokenCacheType(val value: String) {
    /** 不涉及缓存或供应商未返回缓存语义。 */
    NONE("NONE"),

    /** 命中 prompt cache 的输入 Token。 */
    CACHE_HIT("CACHE_HIT"),

    /** 未命中 prompt cache 的输入 Token。 */
    CACHE_MISS("CACHE_MISS"),

    /** 写入 prompt cache 的输入 Token，预留给支持缓存写入计费的供应商。 */
    CACHE_WRITE("CACHE_WRITE"),
}

/**
 * Token 计算来源。
 * 对应落库表 llm_usage_log.token_calc_source。
 */
enum class TokenEstimateSource(val value: String) {
    /** 上游返回完整 usage，直接以 response.usage 为准。 */
    REPORTED_USAGE("REPORTED_USAGE"),

    /** 上游没有返回 usage，使用本地估算结果。 */
    LOCAL_ESTIMATE("LOCAL_ESTIMATE"),

    /** 上游 usage 不完整，使用本地估算补齐缺失字段。 */
    MERGED("MERGED"),

    /** 当前协议或载荷不支持 Token 计算。 */
    UNSUPPORTED("UNSUPPORTED"),
}

/**
 * Token 明细来源。
 * 对应落库表 llm_usage_token_detail.source。
 */
enum class TokenDetailSource(val value: String) {
    /** 明细来自上游明确返回字段。 */
    REPORTED("REPORTED"),

    /** 明细来自本地估算。 */
    LOCAL("LOCAL"),

    /** 明细由上游字段和本地估算合并得到。 */
    MERGED("MERGED"),

    /** 明细由其他字段推导得到，例如 cache miss = prompt - cached。 */
    DERIVED("DERIVED"),
}
