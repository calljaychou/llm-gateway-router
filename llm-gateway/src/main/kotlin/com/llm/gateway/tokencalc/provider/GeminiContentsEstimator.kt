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
class GeminiContentsEstimator : AbstractTokenProviderEstimator() {

    override val protocol: TokenProtocol = TokenProtocol.GEMINI_CONTENTS

    override val providerName: String = "Gemini Contents"

    override fun buildPromptText(params: TokenEstimateParams): TokenTextExtractResult {
        return TokenTextBuilder.buildGeminiPromptText(params.requestBody)
    }

    override fun buildCompletionText(params: TokenEstimateParams): TokenTextExtractResult {
        return TokenTextBuilder.buildGeminiCompletionText(params.responseBody, params.stream)
    }

    /** 构造 Gemini Contents 默认 Token 明细。 */
    override fun buildTokenDetails(
        params: TokenEstimateParams,
        usage: TokenUsageSummaryDto,
        source: TokenEstimateSource,
        prompt: TokenTextExtractResult,
        completion: TokenTextExtractResult,
    ): List<TokenDetailDto> {
        return buildDefaultTokenDetails(usage, source, prompt, completion)
    }

    /** 提取 Gemini usageMetadata。 */
    override fun extractUsageFromObject(root: JSONObject): TokenUsageSummaryDto {
        val usage = root.getJSONObject("usageMetadata") ?: return TokenUsageSummaryDto()
        return TokenUsageSummaryDto(
            inputTokens = usage.getIntValue("promptTokenCount"),
            outputTokens = usage.getIntValue("candidatesTokenCount"),
            totalTokens = usage.getIntValue("totalTokenCount"),
        )
    }

    override fun extractResponseModel(responseBody: String?, stream: Boolean): String? {
        val root = TokenTextBuilder.parseResponseObject(responseBody, stream) ?: return null
        return root.getString("model")?.takeIf { it.isNotBlank() }
            ?: root.getString("modelVersion")?.takeIf { it.isNotBlank() }
            ?: root.getString("model_version")?.takeIf { it.isNotBlank() }
            ?: root.getJSONObject("response")?.getString("model")?.takeIf { it.isNotBlank() }
            ?: root.getJSONObject("response")?.getString("modelVersion")?.takeIf { it.isNotBlank() }
    }
}
