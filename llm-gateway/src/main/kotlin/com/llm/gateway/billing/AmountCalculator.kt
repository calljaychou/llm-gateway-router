package com.llm.gateway.billing

import com.alibaba.fastjson2.JSON
import com.llm.gateway.model.dto.ForwardContextDto
import com.llm.gateway.model.dto.TokenUsageDto
import java.math.BigDecimal
import java.math.RoundingMode
import org.springframework.stereotype.Component
import org.springframework.stereotype.Service

data class AmountCalcResult(
    val amountCny: BigDecimal,
    val detail: String,
)

interface AmountCalculator {
    fun calculate(context: ForwardContextDto, usage: TokenUsageDto): AmountCalcResult
}

@Component
class DefaultAmountCalculator : AmountCalculator {
    override fun calculate(context: ForwardContextDto, usage: TokenUsageDto): AmountCalcResult {
        val inputAmount = calculateTokenAmount(usage.promptTokens, context.inputPriceCnyPerMillion)
        val outputAmount = calculateTokenAmount(usage.completionTokens, context.outputPriceCnyPerMillion)
        val amount = (inputAmount + outputAmount).setScale(6, RoundingMode.HALF_UP)
        return AmountCalcResult(
            amountCny = amount,
            detail = JSON.toJSONString(
                mapOf(
                    "strategy" to "DEFAULT_INPUT_OUTPUT",
                    "promptTokens" to usage.promptTokens,
                    "completionTokens" to usage.completionTokens,
                    "inputPriceCnyPerMillion" to context.inputPriceCnyPerMillion,
                    "outputPriceCnyPerMillion" to context.outputPriceCnyPerMillion,
                    "inputAmountCny" to inputAmount,
                    "outputAmountCny" to outputAmount,
                )
            ),
        )
    }

    /**
     * 按百万 Token 单价计算金额。
     */
    private fun calculateTokenAmount(tokens: Int, priceCnyPerMillion: BigDecimal): BigDecimal {
        if (tokens <= 0 || priceCnyPerMillion <= BigDecimal.ZERO) return BigDecimal.ZERO.setScale(6)
        return BigDecimal(tokens)
            .multiply(priceCnyPerMillion)
            .divide(MILLION, 10, RoundingMode.HALF_UP)
            .setScale(6, RoundingMode.HALF_UP)
    }

    companion object {
        private val MILLION = BigDecimal("1000000")
    }
}

@Service
class AmountCalculatorRegistry(
    private val defaultAmountCalculator: DefaultAmountCalculator,
) {
    fun getCalculator(context: ForwardContextDto): AmountCalculator {
        return defaultAmountCalculator
    }
}
