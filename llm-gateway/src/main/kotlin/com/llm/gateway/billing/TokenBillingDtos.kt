package com.llm.gateway.billing

import com.llm.gateway.tokencalc.model.TokenCacheType
import com.llm.gateway.tokencalc.model.TokenDetailDto
import com.llm.gateway.tokencalc.model.TokenDirection
import com.llm.gateway.tokencalc.model.TokenType
import java.math.BigDecimal

/**
 * Token 计费项。
 * 对应 model_price_rule.charge_item 和 llm_usage_billing_detail.charge_item。
 */
enum class TokenChargeItem(val value: String) {
    /** 普通输入文本 Token。 */
    INPUT_TEXT("INPUT_TEXT"),

    /** 输入缓存命中 Token。 */
    INPUT_CACHE_HIT("INPUT_CACHE_HIT"),

    /** 输入缓存未命中 Token。 */
    INPUT_CACHE_MISS("INPUT_CACHE_MISS"),

    /** 输入缓存写入 Token。 */
    INPUT_CACHE_WRITE("INPUT_CACHE_WRITE"),

    /** 输入音频 Token。 */
    INPUT_AUDIO("INPUT_AUDIO"),

    /** 输入图片 Token。 */
    INPUT_IMAGE("INPUT_IMAGE"),

    /** 普通输出文本 Token。 */
    OUTPUT_TEXT("OUTPUT_TEXT"),

    /** 输出推理 Token。 */
    OUTPUT_REASONING("OUTPUT_REASONING"),

    /** 输出音频 Token。 */
    OUTPUT_AUDIO("OUTPUT_AUDIO"),

    /** 已接受预测输出 Token。 */
    OUTPUT_ACCEPTED_PREDICTION("OUTPUT_ACCEPTED_PREDICTION"),

    /** 已拒绝预测输出 Token。 */
    OUTPUT_REJECTED_PREDICTION("OUTPUT_REJECTED_PREDICTION"),
}

/**
 * Token 计费入参。
 * 计费层只消费 Token 明细和模型价格上下文，不重新解析请求或响应。
 */
data class TokenBillingParams(
    /** 供应商 ID，用于查询模型价格规则。 */
    val vendorId: Long,

    /** 模型 ID，用于查询模型价格规则。 */
    val modelId: Long,

    /** Token 明细列表，金额计算必须基于该列表逐项计算。 */
    val tokenDetails: List<TokenDetailDto>,

    /** 计费策略名称，默认使用模型价格规则。 */
    val billingStrategy: String = TokenBillingStrategy.MODEL_PRICE_RULE,

    /** 计费币种，当前默认 CNY。 */
    val currency: String = "CNY",
)

/**
 * Token 计费结果。
 * 后续落库服务会将该对象写入主日志和金额明细表。
 */
data class TokenBillingResult(
    /** 最终结算金额，单位 CNY，保留 8 位。 */
    val amountCny: BigDecimal,

    /** 计费策略名称。 */
    val billingStrategy: String,

    /** 计费币种。 */
    val currency: String,

    /** 参与计费的输入 Token 数。 */
    val billableInputTokens: Int,

    /** 参与计费的输出 Token 数。 */
    val billableOutputTokens: Int,

    /** 按计费项拆分后的金额明细。 */
    val billingDetails: List<TokenBillingDetailDto>,

    /** 金额计算过程 JSON 快照，后续可写入 llm_usage_log.billing_detail。 */
    val billingDetailJson: String,
)

/**
 * Token 金额明细。
 * 对应 llm_usage_billing_detail 的核心字段。
 */
data class TokenBillingDetailDto(
    /** 命中的计费项。 */
    val chargeItem: TokenChargeItem,

    /** Token 方向。 */
    val direction: TokenDirection,

    /** Token 类型。 */
    val tokenType: TokenType,

    /** 缓存类型。 */
    val cacheType: TokenCacheType,

    /** 本计费项参与计费的 Token 数。 */
    val tokens: Int,

    /** 百万 Token 单价快照。 */
    val priceCnyPerMillion: BigDecimal,

    /** 本计费项金额，单位 CNY，保留 8 位。 */
    val amountCny: BigDecimal,

    /** 命中的价格规则或回退规则说明。 */
    val pricingRule: String?,
)

/**
 * 模型价格规则快照。
 * 一次计费只读取一次价格规则，后续明细计算使用该快照，避免同一请求内价格不一致。
 */
data class ModelPriceRuleSnapshot(
    /** 供应商 ID。 */
    val vendorId: Long,

    /** 模型 ID。 */
    val modelId: Long,

    /** 价格规则币种。 */
    val currency: String,

    /** 按计费项聚合的价格规则。 */
    val rules: Map<TokenChargeItem, ModelPriceRuleItem>,
)

/**
 * 单个计费项的价格规则。
 */
data class ModelPriceRuleItem(
    /** 计费项。 */
    val chargeItem: TokenChargeItem,

    /** 百万 Token 单价。 */
    val priceCnyPerMillion: BigDecimal,

    /** 价格规则说明。 */
    val pricingRule: String,
)

object TokenBillingStrategy {
    const val MODEL_PRICE_RULE = "MODEL_PRICE_RULE"
}
