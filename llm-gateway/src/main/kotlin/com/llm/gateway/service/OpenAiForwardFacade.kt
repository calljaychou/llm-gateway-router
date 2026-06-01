package com.llm.gateway.service

import com.alibaba.fastjson2.toJSONString
import com.llm.gateway.billing.TokenBillingParams
import com.llm.gateway.billing.TokenBillingResult
import com.llm.gateway.billing.TokenBillingService
import com.llm.gateway.common.enums.NormalStatus
import com.llm.gateway.common.enums.UsageAccountingStatus
import com.llm.gateway.common.exceptions.BizException
import com.llm.gateway.common.logger
import com.llm.gateway.dal.mapper.ApiKeysDynamicSqlSupport
import com.llm.gateway.dal.mapper.ApiKeysMapper
import com.llm.gateway.dal.mapper.selectOne
import com.llm.gateway.dal.model.ApiKeysRecord
import com.llm.gateway.model.dto.ForwardContextDto
import com.llm.gateway.model.dto.LlmUsageLogRecordCommand
import com.llm.gateway.model.dto.RateLimitContextDto
import com.llm.gateway.model.dto.RateLimitDecisionDto
import com.llm.gateway.model.dto.UserQuotaReservationDto
import com.llm.gateway.ratelimit.RateLimitService
import com.llm.gateway.tokencalc.TokenCalcService
import com.llm.gateway.tokencalc.model.TokenCacheType
import com.llm.gateway.tokencalc.model.TokenDetailDto
import com.llm.gateway.tokencalc.model.TokenDetailSource
import com.llm.gateway.tokencalc.model.TokenDirection
import com.llm.gateway.tokencalc.model.TokenEstimateParams
import com.llm.gateway.tokencalc.model.TokenEstimateResult
import com.llm.gateway.tokencalc.model.TokenProtocol
import com.llm.gateway.tokencalc.model.TokenType
import com.llm.gateway.tokencalc.model.TokenUsageSummaryDto
import java.math.BigDecimal
import java.security.MessageDigest
import java.util.Date
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
    private val tokenCalcService: TokenCalcService,
    private val tokenBillingService: TokenBillingService,
    private val usageLogWriteService: UsageLogWriteService,
    private val apiKeysMapper: ApiKeysMapper,
) {

    companion object {
        private const val DEFAULT_RESERVED_OUTPUT_TOKENS = 1024
    }

    fun chatCompletions(userId: Long, deptId: Long?, payload: Map<String, Any?>, virtualApiKey: String): Any {
        val stream = payload["stream"]?.toString()?.equals("true", ignoreCase = true) ?: false
        return if (stream) {
            chatCompletionsStream(userId, payload, virtualApiKey)
        } else {
            chatCompletionsJson(userId, deptId, payload, virtualApiKey)
        }
    }

    /** 处理非流式请求：构建上下文、限流决策、上游转发与异常映射。 */
    private fun chatCompletionsJson(
        userId: Long,
        deptId: Long?,
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
                chatCompletionsJsonCore(userId, deptId, virtualApiKey, requestId, startedAt, context)
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
        deptId: Long?,
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
        val requestStartedAt = Date(startedAt)
        val apiKey = findApiKeyByVirtualKey(userId, virtualApiKey)
        val estimatedTokenResult = estimateOpenAiChatTokens(context, null).withReservedOutputTokens(context)
        val estimatedBilling = calculateTokenBilling(context, estimatedTokenResult)
        val reservation = tryReserveQuota(
            userId = userId,
            deptId = deptId,
            requestId = requestId,
            context = context,
            apiKey = apiKey,
            estimatedTokenResult = estimatedTokenResult,
            estimatedBilling = estimatedBilling,
            startedAt = startedAt,
            requestStartedAt = requestStartedAt,
        )

        return openAiForwardService.forwardJson(context)
            .flatMap { upstreamResponse ->
                Mono.fromCallable<ResponseEntity<*>> {
                    // 请求成功 进行结算
                    if (upstreamResponse.statusCode.is2xxSuccessful) settleAndRecordSuccess(
                        userId = userId,
                        deptId = deptId,
                        requestId = requestId,
                        context = context,
                        apiKey = apiKey,
                        reservation = reservation,
                        upstreamResponse = upstreamResponse,
                        startedAt = startedAt,
                        requestStartedAt = requestStartedAt,
                    ) else refundAndRecordFailure(
                        userId = userId,
                        deptId = deptId,
                        requestId = requestId,
                        context = context,
                        apiKey = apiKey,
                        reservation = reservation,
                        tokenEstimate = estimatedTokenResult,
                        billing = estimatedBilling,
                        status = upstreamResponse.statusCode,
                        errorCode = "UPSTREAM_${upstreamResponse.statusCodeValue}",
                        startedAt = startedAt,
                        requestStartedAt = requestStartedAt,
                    )
                    upstreamResponse
                }.subscribeOn(Schedulers.boundedElastic())
            }
            .onErrorResume { error ->
                Mono.fromRunnable<ResponseEntity<*>> {
                    refundAndRecordFailure(
                        userId = userId,
                        deptId = deptId,
                        requestId = requestId,
                        context = context,
                        apiKey = apiKey,
                        reservation = reservation,
                        tokenEstimate = estimatedTokenResult,
                        billing = estimatedBilling,
                        status = HttpStatus.INTERNAL_SERVER_ERROR,
                        errorCode = "FORWARD_FAILED",
                        startedAt = startedAt,
                        requestStartedAt = requestStartedAt,
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
        deptId: Long?,
        requestId: String,
        context: ForwardContextDto,
        apiKey: ApiKeysRecord?,
        estimatedTokenResult: TokenEstimateResult,
        estimatedBilling: TokenBillingResult,
        startedAt: Long,
        requestStartedAt: Date,
    ): UserQuotaReservationDto {
        return try {
            logger().info("reserveQuota,userId:$userId, requestId:$requestId, startedAt:$startedAt")
            val reserve = userQuotaUsageService.reserve(userId, requestId, estimatedBilling.amountCny)
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
                deptId = deptId,
                requestId = requestId,
                context = context,
                apiKey = apiKey,
                tokenEstimate = estimatedTokenResult,
                billing = estimatedBilling,
                status = mapBizCodeToHttpStatus(bizCode),
                errorCode = mapBizCodeToErrorCode(bizCode),
                startedAt = startedAt,
                requestStartedAt = requestStartedAt,
            )
            throw e
        }
    }

    private fun settleAndRecordSuccess(
        userId: Long,
        deptId: Long?,
        requestId: String,
        context: ForwardContextDto,
        apiKey: ApiKeysRecord?,
        reservation: UserQuotaReservationDto,
        upstreamResponse: ResponseEntity<Any>,
        startedAt: Long,
        requestStartedAt: Date,
    ) {
        val tokenEstimate = estimateOpenAiChatTokens(context, upstreamResponse.body)
        val billing = calculateTokenBilling(context, tokenEstimate)
        val settleResult = userQuotaUsageService.settle(reservation, billing.amountCny)
        // 记录使用日志
        usageLogWriteService.record(
            LlmUsageLogRecordCommand(
                requestId = requestId,
                userId = userId,
                deptId = deptId,
                apiKeyId = apiKey?.id,
                vendorId = context.vendorId,
                modelId = context.modelId,
                endpoint = "chat.completions",
                tokenProtocol = TokenProtocol.OPENAI_CHAT,
                stream = false,
                requestModel = context.modelAlias,
                upstreamModel = context.payload["model"]?.toString(),
                reservedAmountCny = reservation.reservedAmount,
                tokenEstimate = tokenEstimate,
                billing = billing,
                latencyMs = elapsedMs(startedAt),
                statusCode = upstreamResponse.statusCodeValue,
                errorCode = null,
                accountingStatus = if (settleResult.settled) UsageAccountingStatus.SUCCEEDED else UsageAccountingStatus.FAILED,
                requestStartedAt = requestStartedAt,
                settledAt = Date(),
            )
        )
    }

    private fun refundAndRecordFailure(
        userId: Long,
        deptId: Long?,
        requestId: String,
        context: ForwardContextDto,
        apiKey: ApiKeysRecord?,
        reservation: UserQuotaReservationDto,
        tokenEstimate: TokenEstimateResult,
        billing: TokenBillingResult,
        status: HttpStatus,
        errorCode: String,
        startedAt: Long,
        requestStartedAt: Date,
    ) {
        userQuotaUsageService.refundAll(reservation)
        usageLogWriteService.record(
            LlmUsageLogRecordCommand(
                requestId = requestId,
                userId = userId,
                deptId = deptId,
                apiKeyId = apiKey?.id,
                vendorId = context.vendorId,
                modelId = context.modelId,
                endpoint = "chat.completions",
                tokenProtocol = TokenProtocol.OPENAI_CHAT,
                stream = false,
                requestModel = context.modelAlias,
                upstreamModel = context.payload["model"]?.toString(),
                reservedAmountCny = reservation.reservedAmount,
                tokenEstimate = tokenEstimate,
                billing = billing.copy(amountCny = BigDecimal.ZERO),
                latencyMs = elapsedMs(startedAt),
                statusCode = status.value(),
                errorCode = errorCode,
                accountingStatus = UsageAccountingStatus.COMPENSATED,
                requestStartedAt = requestStartedAt,
                settledAt = Date(),
            )
        )
    }

    private fun recordQuotaRejected(
        userId: Long,
        deptId: Long?,
        requestId: String,
        context: ForwardContextDto,
        apiKey: ApiKeysRecord?,
        tokenEstimate: TokenEstimateResult,
        billing: TokenBillingResult,
        status: HttpStatus,
        errorCode: String,
        startedAt: Long,
        requestStartedAt: Date,
    ) {
        usageLogWriteService.record(
            LlmUsageLogRecordCommand(
                requestId = requestId,
                userId = userId,
                deptId = deptId,
                apiKeyId = apiKey?.id,
                vendorId = context.vendorId,
                modelId = context.modelId,
                endpoint = "chat.completions",
                tokenProtocol = TokenProtocol.OPENAI_CHAT,
                stream = false,
                requestModel = context.modelAlias,
                upstreamModel = context.payload["model"]?.toString(),
                reservedAmountCny = billing.amountCny,
                tokenEstimate = tokenEstimate,
                billing = billing.copy(amountCny = BigDecimal.ZERO),
                latencyMs = elapsedMs(startedAt),
                statusCode = status.value(),
                errorCode = errorCode,
                accountingStatus = UsageAccountingStatus.FAILED,
                requestStartedAt = requestStartedAt,
                settledAt = Date(),
            )
        )
    }

    /** 执行 OpenAI Chat Token 计算，成功响应会携带 responseBody 用于读取上游 usage。 */
    private fun estimateOpenAiChatTokens(context: ForwardContextDto, responseBody: Any?): TokenEstimateResult {
        return tokenCalcService.estimate(
            TokenEstimateParams(
                protocol = TokenProtocol.OPENAI_CHAT,
                requestModel = context.modelAlias,
                upstreamModel = context.payload["model"]?.toString(),
                stream = context.stream,
                requestBody = context.payload.toJSONString(),
                responseBody = responseBody?.toJSONString(),
            )
        )
    }

    /** 预占阶段按请求 max_tokens 补入输出 Token 上限，避免只估 prompt 导致额度预占过低。 */
    private fun TokenEstimateResult.withReservedOutputTokens(context: ForwardContextDto): TokenEstimateResult {
        val reservedOutputTokens = resolveReservedOutputTokens(context)
        if (reservedOutputTokens <= usage.outputTokens) return this
        val reservedUsage = TokenUsageSummaryDto(
            inputTokens = usage.inputTokens,
            outputTokens = reservedOutputTokens,
            totalTokens = usage.inputTokens + reservedOutputTokens,
        )
        val reservedDetails = tokenDetails
            .filterNot { it.direction == TokenDirection.OUTPUT }
            .plus(
                TokenDetailDto(
                    direction = TokenDirection.OUTPUT,
                    tokenType = TokenType.TEXT,
                    cacheType = TokenCacheType.NONE,
                    tokens = reservedOutputTokens,
                    billableTokens = reservedOutputTokens,
                    source = TokenDetailSource.LOCAL,
                    note = "reserve:max_tokens",
                )
            )
        return copy(
            usage = reservedUsage,
            tokenDetails = reservedDetails,
            note = "${note.ifBlank { "预占阶段Token估算" }}，按max_tokens预占输出Token",
            calcDetail = "",
        )
    }

    /** 解析请求声明的最大输出 Token，缺省时沿用网关历史默认预占 1024。 */
    private fun resolveReservedOutputTokens(context: ForwardContextDto): Int {
        return context.payload["max_tokens"]?.toString()?.toIntOrNull()?.coerceAtLeast(0)
            ?: context.payload["max_completion_tokens"]?.toString()?.toIntOrNull()?.coerceAtLeast(0)
            ?: DEFAULT_RESERVED_OUTPUT_TOKENS
    }

    /** 基于 Token 明细计算金额，金额结果同时用于配额和新日志落库。 */
    private fun calculateTokenBilling(context: ForwardContextDto, tokenEstimate: TokenEstimateResult): TokenBillingResult {
        return tokenBillingService.calculate(
            TokenBillingParams(
                vendorId = context.vendorId,
                modelId = context.modelId,
                tokenDetails = tokenEstimate.tokenDetails,
            )
        )
    }

    /** 根据虚拟 Key 原文查询 keyId，用于写入 llm_usage_log.api_key_id。 */
    private fun findApiKeyByVirtualKey(userId: Long, virtualApiKey: String): ApiKeysRecord? {
        return apiKeysMapper.selectOne {
            where { ApiKeysDynamicSqlSupport.ApiKeys.apiKeyHash isEqualTo hashApiKey(virtualApiKey) }
            and { ApiKeysDynamicSqlSupport.ApiKeys.userId isEqualTo userId }
            and { ApiKeysDynamicSqlSupport.ApiKeys.status isEqualTo NormalStatus }
        }
    }

    /** 计算虚拟 API Key 的 SHA-256 摘要，与 VirtualApiKeyService 保持一致。 */
    private fun hashApiKey(apiKey: String): String {
        val digest = MessageDigest.getInstance("SHA-256").digest(apiKey.toByteArray())
        return digest.joinToString(separator = "") { "%02x".format(it) }
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
