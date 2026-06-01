package com.llm.gateway.billing

import com.alibaba.fastjson2.JSON
import com.llm.gateway.common.logger
import com.llm.gateway.tokencalc.TokenUsageNormalizer
import com.llm.gateway.tokencalc.model.TokenDetailDto
import com.llm.gateway.tokencalc.model.TokenDirection
import java.math.BigDecimal
import java.math.RoundingMode
import org.springframework.stereotype.Service

@Service
class DefaultTokenBillingService(
    private val modelPriceRuleResolver: ModelPriceRuleResolver,
    private val tokenChargeItemResolver: TokenChargeItemResolver,
) : TokenBillingService {

    companion object {
        private val MILLION = BigDecimal("1000000")
        private const val AMOUNT_SCALE = 8
    }

    private val log = logger()

    /**
     * 基于 Token 明细逐项计费。
     * 本服务不解析 request/response，不写日志表，只负责输出金额明细和金额快照。
     */
    override fun calculate(params: TokenBillingParams): TokenBillingResult {
        val tokenDetails = TokenUsageNormalizer.normalizeDetails(params.tokenDetails)
        val priceRules = modelPriceRuleResolver.resolve(
            vendorId = params.vendorId,
            modelId = params.modelId,
            currency = params.currency,
        )
        val billingDetails = tokenDetails.mapNotNull { detail -> calculateDetail(detail, priceRules) }
        val amountCny = billingDetails
            .fold(BigDecimal.ZERO.setScale(AMOUNT_SCALE)) { acc, detail -> acc + detail.amountCny }
            .setScale(AMOUNT_SCALE, RoundingMode.HALF_UP)
        val billableInputTokens = tokenDetails
            .filter { it.direction == TokenDirection.INPUT }
            .sumOf { it.billableTokens.coerceAtLeast(0) }
        val billableOutputTokens = tokenDetails
            .filter { it.direction == TokenDirection.OUTPUT }
            .sumOf { it.billableTokens.coerceAtLeast(0) }

        log.info(
            "Token计费完成 vendorId={}, modelId={}, detailCount={}, billingDetailCount={}, inputBillableTokens={}, outputBillableTokens={}, amountCny={}",
            params.vendorId,
            params.modelId,
            tokenDetails.size,
            billingDetails.size,
            billableInputTokens,
            billableOutputTokens,
            amountCny,
        )
        return TokenBillingResult(
            amountCny = amountCny,
            billingStrategy = params.billingStrategy,
            currency = params.currency,
            billableInputTokens = billableInputTokens,
            billableOutputTokens = billableOutputTokens,
            billingDetails = billingDetails,
            billingDetailJson = buildBillingDetailJson(params, billingDetails, amountCny),
        )
    }

    /** 计算单条 Token 明细金额。 */
    private fun calculateDetail(
        detail: TokenDetailDto,
        priceRules: ModelPriceRuleSnapshot,
    ): TokenBillingDetailDto? {
        val tokens = detail.billableTokens.coerceAtLeast(0)
        if (tokens == 0) return null
        val chargeItem = tokenChargeItemResolver.resolve(detail)
        val priceRule = resolvePriceRule(chargeItem, priceRules)
        val amount = calculateTokenAmount(tokens, priceRule.priceCnyPerMillion)
        return TokenBillingDetailDto(
            chargeItem = chargeItem,
            direction = detail.direction,
            tokenType = detail.tokenType,
            cacheType = detail.cacheType,
            tokens = tokens,
            priceCnyPerMillion = priceRule.priceCnyPerMillion,
            amountCny = amount,
            pricingRule = priceRule.pricingRule,
        )
    }

    /** 根据计费项读取价格，缺失时按任务书定义回退。 */
    private fun resolvePriceRule(
        chargeItem: TokenChargeItem,
        priceRules: ModelPriceRuleSnapshot,
    ): ModelPriceRuleItem {
        val directRule = priceRules.rules[chargeItem]
        if (directRule != null) return directRule

        val fallbackItem = resolveFallbackChargeItem(chargeItem)
        val fallbackRule = fallbackItem?.let { priceRules.rules[it] }
        if (fallbackRule != null) {
            log.warn(
                "Token计费项价格缺失, 使用回退价格 vendorId={}, modelId={}, chargeItem={}, fallbackChargeItem={}",
                priceRules.vendorId,
                priceRules.modelId,
                chargeItem.value,
                fallbackItem.value,
            )
            return fallbackRule.copy(
                chargeItem = chargeItem,
                pricingRule = "fallback:${chargeItem.value}->${fallbackItem.value}:${fallbackRule.pricingRule}",
            )
        }

        log.warn(
            "Token计费项价格缺失, 按0元计费 vendorId={}, modelId={}, chargeItem={}",
            priceRules.vendorId,
            priceRules.modelId,
            chargeItem.value,
        )
        return ModelPriceRuleItem(
            chargeItem = chargeItem,
            priceCnyPerMillion = BigDecimal.ZERO.setScale(AMOUNT_SCALE),
            pricingRule = "missing:${chargeItem.value}",
        )
    }

    /** 定义缺失价格时的兼容回退关系。 */
    private fun resolveFallbackChargeItem(chargeItem: TokenChargeItem): TokenChargeItem? {
        return when (chargeItem) {
            TokenChargeItem.INPUT_CACHE_MISS -> TokenChargeItem.INPUT_TEXT
            TokenChargeItem.INPUT_CACHE_HIT -> TokenChargeItem.INPUT_TEXT
            TokenChargeItem.INPUT_CACHE_WRITE -> TokenChargeItem.INPUT_TEXT
            TokenChargeItem.OUTPUT_REASONING -> TokenChargeItem.OUTPUT_TEXT
            TokenChargeItem.OUTPUT_ACCEPTED_PREDICTION -> TokenChargeItem.OUTPUT_TEXT
            TokenChargeItem.OUTPUT_REJECTED_PREDICTION -> TokenChargeItem.OUTPUT_TEXT
            else -> null
        }
    }

    /** 按百万 Token 单价计算金额。 */
    private fun calculateTokenAmount(tokens: Int, priceCnyPerMillion: BigDecimal): BigDecimal {
        if (tokens <= 0 || priceCnyPerMillion <= BigDecimal.ZERO) {
            return BigDecimal.ZERO.setScale(AMOUNT_SCALE)
        }
        return BigDecimal(tokens)
            .multiply(priceCnyPerMillion)
            .divide(MILLION, 12, RoundingMode.HALF_UP)
            .setScale(AMOUNT_SCALE, RoundingMode.HALF_UP)
    }

    /** 构造计费过程快照。 */
    private fun buildBillingDetailJson(
        params: TokenBillingParams,
        billingDetails: List<TokenBillingDetailDto>,
        amountCny: BigDecimal,
    ): String {
        return JSON.toJSONString(
            mapOf(
                "strategy" to params.billingStrategy,
                "vendorId" to params.vendorId,
                "modelId" to params.modelId,
                "currency" to params.currency,
                "amountCny" to amountCny,
                "billingDetails" to billingDetails,
            )
        )
    }
}
