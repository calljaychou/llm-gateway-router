package com.llm.gateway.service

import com.llm.gateway.common.exceptions.BizException
import com.llm.gateway.model.dto.ForwardContextDto
import com.llm.gateway.model.dto.RateLimitContextDto
import com.llm.gateway.model.dto.RateLimitDecisionDto
import com.llm.gateway.ratelimit.RateLimitService
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono
import reactor.core.scheduler.Schedulers

@Service
class OpenAiForwardFacade(
    private val openAiForwardService: OpenAiForwardService,
    private val rateLimitService: RateLimitService,
) {

    fun chatCompletions(userId: Long, payload: Map<String, Any?>, virtualApiKey: String): Any {
        val stream = payload["stream"]?.toString()?.equals("true", ignoreCase = true) ?: false
        return if (stream) {
            chatCompletionsStream(userId, payload, virtualApiKey)
        } else {
            chatCompletionsJson(userId, payload, virtualApiKey)
        }
    }

    /** 处理非流式请求：构建上下文、限流决策、上游转发与异常映射。 */
    private fun chatCompletionsJson(userId: Long, payload: Map<String, Any?>, virtualApiKey: String): Mono<ResponseEntity<*>> {
        return Mono.fromCallable { openAiForwardService.buildForwardContext(userId, payload) }
            .subscribeOn(Schedulers.boundedElastic())
            .flatMap { context ->
                val decision = rateLimitService.evaluate(buildRateLimitContext(userId, virtualApiKey, context))
                if (decision != null) {
                    Mono.just(openAiErrorResponseWithRateLimit(decision))
                } else {
                    openAiForwardService.forwardJson(context)
                }
            }
            .onErrorResume(BizException::class.java) { e ->
                Mono.just(
                    openAiErrorResponse(
                        status = mapBizCodeToHttpStatus(e.code),
                        message = e.message ?: "请求失败",
                        type = mapBizCodeToErrorType(e.code),
                        code = mapBizCodeToErrorCode(e.code),
                    )
                )
            }
            .onErrorResume {
                Mono.just(
                    openAiErrorResponse(
                        status = HttpStatus.INTERNAL_SERVER_ERROR,
                        message = "供应商转发失败，请稍后重试",
                        type = "server_error",
                        code = "FORWARD_FAILED",
                    )
                )
            }
    }

    /** 处理流式请求：构建上下文、限流决策，并返回 SSE 流。 */
    private fun chatCompletionsStream(userId: Long, payload: Map<String, Any?>, virtualApiKey: String): Any {
        return try {
            val context = openAiForwardService.buildForwardContext(userId, payload)
            val decision = rateLimitService.evaluate(buildRateLimitContext(userId, virtualApiKey, context))
            if (decision != null) {
                return openAiErrorResponseWithRateLimit(decision)
            }
            openAiForwardService.forwardStream(context)
        } catch (e: BizException) {
            openAiErrorResponse(
                status = mapBizCodeToHttpStatus(e.code),
                message = e.message ?: "请求失败",
                type = mapBizCodeToErrorType(e.code),
                code = mapBizCodeToErrorCode(e.code),
            )
        } catch (_: Exception) {
            openAiErrorResponse(
                status = HttpStatus.INTERNAL_SERVER_ERROR,
                message = "供应商转发失败，请稍后重试",
                type = "server_error",
                code = "FORWARD_FAILED",
            )
        }
    }

    /** 从转发上下文提取限流所需维度，构建统一限流上下文。 */
    private fun buildRateLimitContext(userId: Long, virtualApiKey: String, context: ForwardContextDto): RateLimitContextDto {
        return RateLimitContextDto(
            userId = userId,
            virtualApiKey = virtualApiKey,
            modelAlias = context.modelAlias,
            vendorId = context.vendorId,
            path = "/v1/chat/completions",
            stream = context.stream,
        )
    }

    /** 构建 OpenAI 协议格式的通用错误响应体。 */
    private fun openAiErrorResponse(
        status: HttpStatus,
        message: String,
        type: String,
        code: String,
    ): ResponseEntity<Map<String, Any?>> {
        val errorBody = mapOf(
            "error" to mapOf(
                "message" to message,
                "type" to type,
                "code" to code,
            )
        )
        return ResponseEntity.status(status)
            .contentType(MediaType.APPLICATION_JSON)
            .body(errorBody)
    }

    /** 构建限流错误响应并附带标准限流头信息。 */
    private fun openAiErrorResponseWithRateLimit(decision: RateLimitDecisionDto): ResponseEntity<Map<String, Any?>> {
        val errorBody = mapOf(
            "error" to mapOf(
                "message" to "请求过于频繁，请稍后再试",
                "type" to "rate_limit_error",
                "code" to "GATEWAY_RATE_LIMIT_EXCEEDED",
                "source" to decision.source,
                "scope" to decision.scope,
            )
        )
        return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
            .header("Retry-After", decision.retryAfterSeconds.toString())
            .header("X-RateLimit-Limit", decision.limit)
            .header("X-RateLimit-Remaining", decision.remaining)
            .header("X-RateLimit-Reset", decision.resetEpochSecond)
            .header("X-RateLimit-Scope", decision.scope)
            .header("X-RateLimit-Source", decision.source)
            .contentType(MediaType.APPLICATION_JSON)
            .body(errorBody)
    }

    /** 将业务异常码映射为 HTTP 状态码。 */
    private fun mapBizCodeToHttpStatus(code: Int): HttpStatus {
        return when (code) {
            400 -> HttpStatus.BAD_REQUEST
            401 -> HttpStatus.UNAUTHORIZED
            403 -> HttpStatus.FORBIDDEN
            429 -> HttpStatus.TOO_MANY_REQUESTS
            else -> HttpStatus.INTERNAL_SERVER_ERROR
        }
    }

    /** 将业务异常码映射为 OpenAI 错误类型。 */
    private fun mapBizCodeToErrorType(code: Int): String {
        return when (code) {
            400, 403 -> "invalid_request_error"
            401 -> "invalid_api_key"
            429 -> "rate_limit_error"
            else -> "server_error"
        }
    }

    /** 将业务异常码映射为稳定的错误代码文本。 */
    private fun mapBizCodeToErrorCode(code: Int): String {
        return when (code) {
            400 -> "BAD_REQUEST"
            401 -> "UNAUTHORIZED"
            403 -> "RBAC_MODEL_FORBIDDEN"
            429 -> "RATE_LIMIT_EXCEEDED"
            else -> "FORWARD_FAILED"
        }
    }
}
