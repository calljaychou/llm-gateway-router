package com.llm.gateway.tokencalc.provider

import com.llm.gateway.tokencalc.DefaultTokenCalcService
import com.llm.gateway.tokencalc.model.TokenCacheType
import com.llm.gateway.tokencalc.model.TokenDetailSource
import com.llm.gateway.tokencalc.model.TokenDirection
import com.llm.gateway.tokencalc.model.TokenEstimateParams
import com.llm.gateway.tokencalc.model.TokenEstimateSource
import com.llm.gateway.tokencalc.model.TokenProtocol
import com.llm.gateway.tokencalc.model.TokenType
import com.llm.gateway.tokencalc.model.TokenUsageSummaryDto
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AdditionalProtocolEstimatorTest {

    private val openAiChatEstimator = OpenAiChatEstimator()
    private val openAiResponsesEstimator = OpenAiResponsesEstimator()
    private val anthropicEstimator = AnthropicMessagesEstimator()
    private val geminiEstimator = GeminiContentsEstimator()

    @Test
    fun openAiChatShouldKeepProviderUsageDetails() {
        val result = openAiChatEstimator.estimate(
            TokenEstimateParams(
                protocol = TokenProtocol.OPENAI_CHAT,
                requestBody = """
                    {
                      "model": "gpt-4o-mini",
                      "messages": [{"role": "user", "content": "hello"}]
                    }
                """.trimIndent(),
                responseBody = """
                    {
                      "model": "gpt-4o-mini",
                      "choices": [{"message": {"content": "hello back"}}],
                      "usage": {
                        "prompt_tokens": 100,
                        "completion_tokens": 40,
                        "total_tokens": 140,
                        "prompt_tokens_details": {
                          "cached_tokens": 30,
                          "audio_tokens": 5
                        },
                        "completion_tokens_details": {
                          "reasoning_tokens": 7,
                          "audio_tokens": 3,
                          "accepted_prediction_tokens": 2,
                          "rejected_prediction_tokens": 1
                        }
                      }
                    }
                """.trimIndent(),
            )
        )

        assertEquals(TokenEstimateSource.REPORTED_USAGE, result.source)
        assertEquals(TokenUsageSummaryDto(inputTokens = 100, outputTokens = 40, totalTokens = 140), result.usage)
        assertEquals("gpt-4o-mini", result.resolvedModel)
        assertTrue(
            result.tokenDetails.any {
                it.direction == TokenDirection.INPUT &&
                    it.cacheType == TokenCacheType.CACHE_HIT &&
                    it.tokens == 30 &&
                    it.providerField == "prompt_tokens_details.cached_tokens"
            }
        )
        assertTrue(
            result.tokenDetails.any {
                it.direction == TokenDirection.OUTPUT &&
                    it.tokenType == TokenType.REASONING &&
                    it.tokens == 7 &&
                    it.providerField == "completion_tokens_details.reasoning_tokens"
            }
        )
        assertTrue(
            result.tokenDetails.any {
                it.direction == TokenDirection.OUTPUT &&
                    it.tokenType == TokenType.PREDICTION &&
                    it.tokens == 2 &&
                    it.note == "accepted"
            }
        )
    }

    @Test
    fun openAiResponsesShouldUseResponseUsage() {
        val result = openAiResponsesEstimator.estimate(
            TokenEstimateParams(
                protocol = TokenProtocol.OPENAI_RESPONSES,
                requestBody = openAiResponsesRequest(),
                responseBody = """
                    {
                      "output": [
                        {"type": "message", "content": [{"type": "output_text", "text": "Gravity pulls objects together."}]}
                      ],
                      "usage": {
                        "input_tokens": 15,
                        "output_tokens": 6,
                        "total_tokens": 21
                      }
                    }
                """.trimIndent(),
            )
        )

        assertEquals(TokenEstimateSource.REPORTED_USAGE, result.source)
        assertEquals(TokenUsageSummaryDto(inputTokens = 15, outputTokens = 6, totalTokens = 21), result.usage)
        assertEquals("gpt-4.1-mini", result.resolvedModel)
        assertTrue(result.tokenDetails.all { it.source == TokenDetailSource.REPORTED })
    }

    @Test
    fun openAiResponsesShouldMergeCallerPromptUsageWithLocalCompletion() {
        val result = openAiResponsesEstimator.estimate(
            TokenEstimateParams(
                protocol = TokenProtocol.OPENAI_RESPONSES,
                requestBody = openAiResponsesRequest(),
                responseBody = """
                    {
                      "output": [
                        {"type": "message", "content": [{"type": "output_text", "text": "Gravity pulls objects together."}]}
                      ]
                    }
                """.trimIndent(),
                reportedUsage = TokenUsageSummaryDto(inputTokens = 50),
            )
        )

        assertEquals(TokenEstimateSource.MERGED, result.source)
        assertEquals(50, result.usage.inputTokens)
        assertTrue(result.usage.outputTokens > 0)
        assertEquals(result.usage.inputTokens + result.usage.outputTokens, result.usage.totalTokens)
    }

    @Test
    fun anthropicShouldCountImagePlaceholderWhenUsageMissing() {
        val result = anthropicEstimator.estimate(
            TokenEstimateParams(
                protocol = TokenProtocol.ANTHROPIC_MESSAGES,
                requestModel = "claude-3-5-sonnet",
                requestBody = """
                    {
                      "system": "You are Claude.",
                      "messages": [
                        {
                          "role": "user",
                          "content": [
                            {"type": "text", "text": "Say hi."},
                            {"type": "image", "source": {"type": "base64", "media_type": "image/png", "data": "abc"}}
                          ]
                        }
                      ]
                    }
                """.trimIndent(),
                responseBody = """{"content":[{"type":"text","text":"Hi there."}]}""",
            )
        )

        assertEquals(TokenEstimateSource.LOCAL_ESTIMATE, result.source)
        assertTrue(result.usage.inputTokens >= 256)
        assertTrue(result.note.contains("image parts counted by placeholder policy"))
        assertTrue(
            result.tokenDetails.any {
                it.direction == TokenDirection.INPUT && it.tokenType == TokenType.IMAGE && it.tokens == 256
            }
        )
    }

    @Test
    fun anthropicShouldNormalizeUsageWithoutTotalTokens() {
        val result = anthropicEstimator.estimate(
            TokenEstimateParams(
                protocol = TokenProtocol.ANTHROPIC_MESSAGES,
                requestBody = """{"model":"claude-3-5-sonnet","messages":[{"role":"user","content":"hi"}]}""",
                responseBody = """
                    {
                      "content": [{"type": "text", "text": "Hi there."}],
                      "usage": {
                        "input_tokens": 9,
                        "output_tokens": 4
                      }
                    }
                """.trimIndent(),
            )
        )

        assertEquals(TokenEstimateSource.REPORTED_USAGE, result.source)
        assertEquals(TokenUsageSummaryDto(inputTokens = 9, outputTokens = 4, totalTokens = 13), result.usage)
    }

    @Test
    fun geminiShouldUseUsageMetadataAndDetectResponseModel() {
        val result = geminiEstimator.estimate(
            TokenEstimateParams(
                protocol = TokenProtocol.GEMINI_CONTENTS,
                requestBody = geminiRequest(),
                responseBody = """
                    {
                      "modelVersion": "gemini-2.0-flash",
                      "candidates": [
                        {"content": {"parts": [{"text": "Stars are hot balls of gas."}]}}
                      ],
                      "usageMetadata": {
                        "promptTokenCount": 12,
                        "candidatesTokenCount": 5,
                        "totalTokenCount": 17
                      }
                    }
                """.trimIndent(),
            )
        )

        assertEquals(TokenEstimateSource.REPORTED_USAGE, result.source)
        assertEquals(TokenUsageSummaryDto(inputTokens = 12, outputTokens = 5, totalTokens = 17), result.usage)
        assertEquals("gemini-2.0-flash", result.resolvedModel)
    }

    @Test
    fun defaultTokenCalcServiceShouldRouteAdditionalProtocols() {
        val service = DefaultTokenCalcService(
            listOf(openAiResponsesEstimator, anthropicEstimator, geminiEstimator)
        )

        val result = service.estimate(
            TokenEstimateParams(
                protocol = TokenProtocol.GEMINI_CONTENTS,
                requestBody = geminiRequest(),
                responseBody = """{"candidates":[{"content":{"parts":[{"text":"Stars are hot balls of gas."}]}}]}""",
            )
        )

        assertEquals(TokenEstimateSource.LOCAL_ESTIMATE, result.source)
        assertTrue(result.supported)
        assertTrue(result.usage.inputTokens > 0)
        assertTrue(result.usage.outputTokens > 0)
    }

    @Test
    fun openAiResponsesShouldExtractUsageFromStreamResponseObject() {
        val result = openAiResponsesEstimator.estimate(
            TokenEstimateParams(
                protocol = TokenProtocol.OPENAI_RESPONSES,
                requestModel = "gpt-4.1-mini",
                stream = true,
                responseBody = """
                    data: {"type":"response.output_text.delta","delta":"Hello"}

                    data: {"type":"response.completed","response":{"model":"gpt-4.1-mini","usage":{"input_tokens":8,"output_tokens":3,"total_tokens":11}}}

                    data: [DONE]
                """.trimIndent(),
            )
        )

        assertEquals(TokenEstimateSource.REPORTED_USAGE, result.source)
        assertEquals(TokenUsageSummaryDto(inputTokens = 8, outputTokens = 3, totalTokens = 11), result.usage)
    }

    private fun openAiResponsesRequest(): String {
        return """
            {
              "model": "gpt-4.1-mini",
              "instructions": "Be concise.",
              "input": [
                {
                  "role": "user",
                  "content": [
                    {"type": "input_text", "text": "Explain gravity."}
                  ]
                }
              ]
            }
        """.trimIndent()
    }

    private fun geminiRequest(): String {
        return """
            {
              "systemInstruction": {"parts": [{"text": "Keep it short."}]},
              "contents": [
                {"role": "user", "parts": [{"text": "Summarize stars."}]}
              ]
            }
        """.trimIndent()
    }
}
