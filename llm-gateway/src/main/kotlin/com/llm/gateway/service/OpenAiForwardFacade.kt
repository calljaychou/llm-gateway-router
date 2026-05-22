package com.llm.gateway.service

import com.alibaba.fastjson2.JSONObject
import com.alibaba.fastjson2.toJSONString
import com.llm.gateway.common.enums.TokenCalcSource
import com.llm.gateway.common.enums.UsageAccountingStatus
import com.llm.gateway.common.exceptions.BizException
import com.llm.gateway.common.logger
import com.llm.gateway.model.dto.ForwardContextDto
import com.llm.gateway.model.dto.RateLimitContextDto
import com.llm.gateway.model.dto.RateLimitDecisionDto
import com.llm.gateway.model.dto.TokenUsageDto
import com.llm.gateway.model.dto.UsageLogRecordCommand
import com.llm.gateway.model.dto.UserQuotaReservationDto
import com.llm.gateway.ratelimit.RateLimitService
import java.util.UUID
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
    private val userQuotaUsageService: UserQuotaUsageService,
    private val usageLogService: UsageLogService,
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
    private fun chatCompletionsJson(
        userId: Long,
        payload: Map<String, Any?>,
        virtualApiKey: String,
    ): Mono<ResponseEntity<*>> {
        val requestId = UUID.randomUUID().toString()
        val startedAt = System.currentTimeMillis()
        // 构建请求上下文 包装阻塞代码，延迟执行
        return Mono.fromCallable { openAiForwardService.buildForwardContext(userId, payload) }
            .subscribeOn(Schedulers.boundedElastic())
            .flatMap { context ->
                // 发送请求核心逻辑
                chatCompletionsJsonCore(userId, payload, virtualApiKey, requestId, startedAt, context)
            }
            .onErrorResume(BizException::class.java) { e ->
                Mono.just(
                    openAiErrorResponse(
                        mapBizCodeToHttpStatus(e.code),
                        e.message ?: "请求失败",
                        mapBizCodeToErrorType(e.code),
                        mapBizCodeToErrorCode(e.code),
                    )
                )
            }
            .onErrorResume {
                Mono.just(
                    openAiErrorResponse(
                        HttpStatus.INTERNAL_SERVER_ERROR,
                        "供应商转发失败，请稍后重试",
                        "server_error",
                        "FORWARD_FAILED"
                    )
                )
            }
    }

    private fun chatCompletionsJsonCore(
        userId: Long,
        payload: Map<String, Any?>,
        virtualApiKey: String,
        requestId: String,
        startedAt: Long,
        context: ForwardContextDto,
    ): Mono<ResponseEntity<*>> {
        // 限流评估
        val decision = rateLimitService.evaluate(buildRateLimitContext(userId, virtualApiKey, context))
        if (decision != null) {
            logger().warn("限流评估,decision:{}", decision.toJSONString())
            return Mono.just(openAiErrorResponseWithRateLimit(decision))
        }
        // 预估token 进行预占用
        val estimatedTokens = estimateReserveTokens(payload)
        val reservation = tryReserveQuota(userId, requestId, context, estimatedTokens, startedAt)

        return openAiForwardService.forwardJson(context)
            .flatMap { upstreamResponse ->
                Mono.fromCallable<ResponseEntity<*>> {
                    // 请求成功 进行结算
                    if (upstreamResponse.statusCode.is2xxSuccessful) settleAndRecordSuccess(
                        userId = userId,
                        requestId = requestId,
                        context = context,
                        reservation = reservation,
                        upstreamResponse = upstreamResponse,
                        startedAt = startedAt,
                    ) else refundAndRecordFailure(
                        userId = userId,
                        requestId = requestId,
                        context = context,
                        reservation = reservation,
                        status = upstreamResponse.statusCode,
                        errorCode = "UPSTREAM_${upstreamResponse.statusCodeValue}",
                        startedAt = startedAt,
                    )
                    upstreamResponse
                }.subscribeOn(Schedulers.boundedElastic())
            }
            .onErrorResume { error ->
                Mono.fromRunnable<ResponseEntity<*>> {
                    refundAndRecordFailure(
                        userId = userId,
                        requestId = requestId,
                        context = context,
                        reservation = reservation,
                        status = HttpStatus.INTERNAL_SERVER_ERROR,
                        errorCode = "FORWARD_FAILED",
                        startedAt = startedAt,
                    )
                }
                    .subscribeOn(Schedulers.boundedElastic()).doOnError { compensateError ->
                        logger().error("配额补偿失败 requestId:{},e:", requestId, compensateError)
                    }
                    .onErrorResume { Mono.empty() }
                    .then(Mono.error(error))
            }
    }

    private fun tryReserveQuota(
        userId: Long,
        requestId: String,
        context: ForwardContextDto,
        estimatedTokens: Long,
        startedAt: Long,
    ): UserQuotaReservationDto {
        return try {
            logger().info("reserveQuota,userId:$userId, requestId:$requestId, startedAt:$startedAt")
            val reserve = userQuotaUsageService.reserve(userId, requestId, estimatedTokens)
            logger().info(
                "reserveQuota,userId:$userId, requestId:$requestId, startedAt:$startedAt \nreserve:{}",
                reserve.toJSONString()
            )
            reserve
        } catch (e: Exception) {
            logger().error("reserveQuota:", e)
            val bizCode = (e as? BizException)?.code ?: -1
            // 记录失败的  usageLog
            recordQuotaRejected(
                userId = userId,
                requestId = requestId,
                context = context,
                estimatedTokens = estimatedTokens,
                status = mapBizCodeToHttpStatus(bizCode),
                errorCode = mapBizCodeToErrorCode(bizCode),
                startedAt = startedAt,
            )
            throw e
        }
    }

    private fun settleAndRecordSuccess(
        userId: Long,
        requestId: String,
        context: ForwardContextDto,
        reservation: UserQuotaReservationDto,
        upstreamResponse: ResponseEntity<Any>,
        startedAt: Long,
    ) {
        val upstreamUsage = extractTokenUsage(upstreamResponse.body)
        val usage = upstreamUsage ?: TokenUsageDto(
            promptTokens = 0,
            completionTokens = 0,
            totalTokens = reservation.reservedTokens.toInt(),
        )
        val settleResult = userQuotaUsageService.settle(reservation, usage.totalTokens.toLong())
        usageLogService.record(
            UsageLogRecordCommand(
                requestId = requestId,
                userId = userId,
                vendorId = context.vendorId,
                modelAlias = context.modelAlias,
                endpoint = "chat.completions",
                stream = false,
                reservedTokens = reservation.reservedTokens.toInt(),
                usage = usage,
                latencyMs = elapsedMs(startedAt),
                statusCode = upstreamResponse.statusCodeValue,
                errorCode = null,
                accountingStatus = if (settleResult.settled) UsageAccountingStatus.SUCCEEDED.value else UsageAccountingStatus.FAILED.value,
                calcSource = if (upstreamUsage != null) TokenCalcSource.UPSTREAM.value else TokenCalcSource.LOCAL_ESTIMATE.value,
            )
        )
    }

    private fun refundAndRecordFailure(
        userId: Long,
        requestId: String,
        context: ForwardContextDto,
        reservation: UserQuotaReservationDto,
        status: HttpStatus,
        errorCode: String,
        startedAt: Long,
    ) {
        userQuotaUsageService.refundAll(reservation)
        usageLogService.record(
            UsageLogRecordCommand(
                requestId = requestId,
                userId = userId,
                vendorId = context.vendorId,
                modelAlias = context.modelAlias,
                endpoint = "chat.completions",
                stream = false,
                reservedTokens = reservation.reservedTokens.toInt(),
                usage = TokenUsageDto(promptTokens = 0, completionTokens = 0, totalTokens = 0),
                latencyMs = elapsedMs(startedAt),
                statusCode = status.value(),
                errorCode = errorCode,
                accountingStatus = UsageAccountingStatus.COMPENSATED.value,
                calcSource = null,
            )
        )
    }

    private fun recordQuotaRejected(
        userId: Long,
        requestId: String,
        context: ForwardContextDto,
        estimatedTokens: Long,
        status: HttpStatus,
        errorCode: String,
        startedAt: Long,
    ) {
        usageLogService.record(
            UsageLogRecordCommand(
                requestId = requestId,
                userId = userId,
                vendorId = context.vendorId,
                modelAlias = context.modelAlias,
                endpoint = "chat.completions",
                stream = false,
                reservedTokens = estimatedTokens.toInt(),
                usage = TokenUsageDto(promptTokens = 0, completionTokens = 0, totalTokens = 0),
                latencyMs = elapsedMs(startedAt),
                statusCode = status.value(),
                errorCode = errorCode,
                accountingStatus = UsageAccountingStatus.FAILED.value,
                calcSource = null,
            )
        )
    }

    private fun estimateReserveTokens(payload: Map<String, Any?>): Long {
        val maxTokens = payload["max_tokens"]?.toString()?.toLongOrNull()
        return maxTokens?.coerceAtLeast(1L) ?: 1024L
    }

    @Suppress("UNCHECKED_CAST")
    private fun extractTokenUsage(body: Any?): TokenUsageDto? {
        val root = body as? JSONObject ?: return null
        val usage = root.getJSONObject("usage") ?: return null

        val promptTokens = usage.getInteger("prompt_tokens") ?: 0
        val completionTokens = usage.getInteger("completion_tokens") ?: 0
        val totalTokens = usage.getInteger("total_tokens") ?: (promptTokens + completionTokens)
        return TokenUsageDto(
            promptTokens = promptTokens,
            completionTokens = completionTokens,
            totalTokens = totalTokens,
        )
    }

    private fun elapsedMs(startedAt: Long): Int {
        return (System.currentTimeMillis() - startedAt).coerceAtMost(Int.MAX_VALUE.toLong()).toInt()
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
    private fun buildRateLimitContext(
        userId: Long,
        virtualApiKey: String,
        context: ForwardContextDto,
    ): RateLimitContextDto {
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
            402 -> HttpStatus.PAYMENT_REQUIRED
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
            402 -> "insufficient_quota"
            429 -> "rate_limit_error"
            else -> "server_error"
        }
    }

    /** 将业务异常码映射为稳定的错误代码文本。 */
    private fun mapBizCodeToErrorCode(code: Int): String {
        return when (code) {
            400 -> "BAD_REQUEST"
            401 -> "UNAUTHORIZED"
            402 -> "INSUFFICIENT_QUOTA"
            403 -> "RBAC_MODEL_FORBIDDEN"
            429 -> "RATE_LIMIT_EXCEEDED"
            else -> "FORWARD_FAILED"
        }
    }
}
