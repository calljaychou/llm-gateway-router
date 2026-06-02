package com.llm.gateway.tokencalc.provider

import com.alibaba.fastjson2.JSONObject
import com.llm.gateway.tokencalc.model.TokenDetailDto
import com.llm.gateway.tokencalc.model.TokenEstimateParams
import com.llm.gateway.tokencalc.model.TokenEstimateSource
import com.llm.gateway.tokencalc.model.TokenProtocol
import com.llm.gateway.tokencalc.model.TokenUsageSummaryDto
import com.llm.gateway.tokencalc.text.TokenTextBuilder
import com.llm.gateway.tokencalc.text.TokenTextExtractResult
import org.springframework.stereotype.Component

@Component
class AnthropicMessagesEstimator : AbstractTokenProviderEstimator() {

    override val protocol: TokenProtocol = TokenProtocol.ANTHROPIC_MESSAGES

    override val providerName: String = "Anthropic Messages"

    override fun buildPromptText(params: TokenEstimateParams): TokenTextExtractResult {
        return TokenTextBuilder.buildAnthropicPromptText(params.requestBody)
    }

    override fun buildCompletionText(params: TokenEstimateParams): TokenTextExtractResult {
        return TokenTextBuilder.buildAnthropicCompletionText(params.responseBody, params.stream)
    }

    /** 构造 Anthropic Messages 默认 Token 明细。 */
    override fun buildTokenDetails(
        params: TokenEstimateParams,
        usage: TokenUsageSummaryDto,
        source: TokenEstimateSource,
        prompt: TokenTextExtractResult,
        completion: TokenTextExtractResult,
    ): List<TokenDetailDto> {
        return buildDefaultTokenDetails(usage, source, prompt, completion)
    }

    /** 提取 Anthropic usage。 */
    override fun extractUsageFromObject(root: JSONObject): TokenUsageSummaryDto {
        val usage = root.getJSONObject("usage") ?: return TokenUsageSummaryDto()
        return TokenUsageSummaryDto(
            inputTokens = usage.getIntValue("input_tokens"),
            outputTokens = usage.getIntValue("output_tokens"),
            totalTokens = usage.getIntValue("total_tokens"),
        )
    }

    /** 流式 message_delta 事件可能将 usage 放在 message 对象内。 */
    override fun extractUsageFromStreamEvent(event: JSONObject): TokenUsageSummaryDto {
        val eventUsage = extractUsageFromObject(event)
        if (eventUsage.hasAny()) return eventUsage
        return event.getJSONObject("message")?.let { extractUsageFromObject(it) } ?: TokenUsageSummaryDto()
    }

    override fun extractResponseModel(responseBody: String?, stream: Boolean): String? {
        val root = TokenTextBuilder.parseResponseObject(responseBody, stream) ?: return null
        return root.getString("model")?.takeIf { it.isNotBlank() }
            ?: root.getJSONObject("message")?.getString("model")?.takeIf { it.isNotBlank() }
    }
}
