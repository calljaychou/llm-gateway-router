package com.llm.gateway.service

import com.llm.gateway.billing.TokenBillingDetailDto
import com.llm.gateway.common.enums.UsageAccountingStatus
import com.llm.gateway.common.logger
import com.llm.gateway.dal.mapper.LlmUsageBillingDetailMapper
import com.llm.gateway.dal.mapper.LlmUsageLogDynamicSqlSupport
import com.llm.gateway.dal.mapper.LlmUsageLogMapper
import com.llm.gateway.dal.mapper.LlmUsageTokenDetailMapper
import com.llm.gateway.dal.mapper.insertMultiple
import com.llm.gateway.dal.mapper.insertSelective
import com.llm.gateway.dal.mapper.selectOne
import com.llm.gateway.dal.model.LlmUsageBillingDetailRecord
import com.llm.gateway.dal.model.LlmUsageLogRecord
import com.llm.gateway.dal.model.LlmUsageTokenDetailRecord
import com.llm.gateway.model.dto.LlmUsageLogRecordCommand
import com.llm.gateway.model.dto.LlmUsageLogWriteResult
import com.llm.gateway.tokencalc.model.TokenDetailDto
import com.llm.gateway.tokencalc.model.TokenEstimateResult
import com.llm.gateway.tokencalc.model.TokenEstimateSource
import com.llm.gateway.tokencalc.model.TokenUsageSummaryDto
import java.math.BigDecimal
import java.math.RoundingMode
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class UsageLogWriteService(
    private val llmUsageLogMapper: LlmUsageLogMapper,
    private val llmUsageTokenDetailMapper: LlmUsageTokenDetailMapper,
    private val llmUsageBillingDetailMapper: LlmUsageBillingDetailMapper,
) {

    companion object {
        private const val AMOUNT_SCALE = 8
    }

    private val log = logger()

    /**
     * 写入LLM 用量日志三表。
     * 落库服务只消费 Token 计算结果和金额计算结果，不重新解析请求、响应或价格。
     */
    @Transactional(rollbackFor = [Exception::class])
    fun record(command: LlmUsageLogRecordCommand): LlmUsageLogWriteResult {
        val existed = llmUsageLogMapper.selectOne {
            where { LlmUsageLogDynamicSqlSupport.LlmUsageLog.requestId isEqualTo command.requestId }
        }
        if (existed?.id != null) {
            log.warn("LLM用量日志已存在, 跳过重复写入 requestId={}, usageLogId={}", command.requestId, existed.id)
            return LlmUsageLogWriteResult(
                usageLogId = existed.id!!,
                requestId = command.requestId,
                inserted = false,
            )
        }

        val usageLog = buildUsageLogRecord(command).also { llmUsageLogMapper.insertSelective(it) }
        val usageLogId = usageLog.id ?: throw IllegalStateException("llm_usage_log 主键回填失败")
        val tokenDetails = buildTokenDetailRecords(
            usageLogId, command.requestId, command.tokenEstimate?.tokenDetails.orEmpty()
        )
        val billingDetails = buildBillingDetailRecords(
            usageLogId, command.requestId, command.billing?.billingDetails.orEmpty()
        )
        if (tokenDetails.isNotEmpty()) llmUsageTokenDetailMapper.insertMultiple(tokenDetails)
        if (billingDetails.isNotEmpty()) llmUsageBillingDetailMapper.insertMultiple(billingDetails)
        log.info(
            "LLM用量日志写入完成 requestId={}, usageLogId={}, tokenDetailCount={}, billingDetailCount={}, accountingStatus={}",
            command.requestId,
            usageLogId,
            tokenDetails.size,
            billingDetails.size,
            command.accountingStatus.value,
        )
        return LlmUsageLogWriteResult(
            usageLogId = usageLogId,
            requestId = command.requestId,
            inserted = true,
        )
    }

    /** 构造 llm_usage_log 主表记录。 */
    private fun buildUsageLogRecord(command: LlmUsageLogRecordCommand): LlmUsageLogRecord {
        val tokenEstimate = command.tokenEstimate ?: emptyTokenEstimate()
        val usage = tokenEstimate.usage
        val billing = command.billing
        return LlmUsageLogRecord(
            requestId = command.requestId,
            userId = command.userId,
            deptId = command.deptId,
            apiKeyId = command.apiKeyId,
            vendorId = command.vendorId,
            modelId = command.modelId,
            endpoint = command.endpoint,
            tokenProtocol = command.tokenProtocol.value,
            useStream = command.stream,
            requestModel = command.requestModel,
            upstreamModel = command.upstreamModel,
            resolvedModel = tokenEstimate.resolvedModel.ifBlank { null },
            modelEncoding = tokenEstimate.encoding.ifBlank { null },
            tokenCalcSource = tokenEstimate.source.value,
            tokenCalcSupported = tokenEstimate.supported,
            tokenCalcNote = tokenEstimate.note.ifBlank { null },
            inputTokens = usage.inputTokens.coerceAtLeast(0),
            outputTokens = usage.outputTokens.coerceAtLeast(0),
            totalTokens = usage.totalTokens.coerceAtLeast(0),
            billableInputTokens = billing?.billableInputTokens?.coerceAtLeast(0) ?: 0,
            billableOutputTokens = billing?.billableOutputTokens?.coerceAtLeast(0) ?: 0,
            reservedAmountCny = normalizeAmount(command.reservedAmountCny),
            amountCny = normalizeAmount(billing?.amountCny ?: BigDecimal.ZERO),
            billingStrategy = billing?.billingStrategy ?: "NONE",
            billingCurrency = billing?.currency ?: "CNY",
            latencyMs = command.latencyMs,
            statusCode = command.statusCode,
            errorCode = command.errorCode,
            accountingStatus = command.accountingStatus.value,
            requestStartedAt = command.requestStartedAt,
            settledAt = command.settledAt,
            tokenCalcDetail = tokenEstimate.calcDetail.ifBlank { null },
            billingDetail = billing?.billingDetailJson?.ifBlank { null },
        )
    }

    /** 构造 llm_usage_token_detail 明细记录。 */
    private fun buildTokenDetailRecords(
        usageLogId: Long,
        requestId: String,
        details: List<TokenDetailDto>,
    ): List<LlmUsageTokenDetailRecord> {
        return details.map { detail ->
            LlmUsageTokenDetailRecord(
                usageLogId = usageLogId,
                requestId = requestId,
                tokenDirection = detail.direction.value,
                tokenType = detail.tokenType.value,
                cacheType = detail.cacheType.value,
                tokens = detail.tokens.coerceAtLeast(0),
                billableTokens = detail.billableTokens.coerceAtLeast(0),
                source = detail.source.value,
                providerField = detail.providerField,
                note = detail.note,
            )
        }
    }

    /** 构造 llm_usage_billing_detail 明细记录。 */
    private fun buildBillingDetailRecords(
        usageLogId: Long,
        requestId: String,
        details: List<TokenBillingDetailDto>,
    ): List<LlmUsageBillingDetailRecord> {
        return details.map { detail ->
            LlmUsageBillingDetailRecord(
                usageLogId = usageLogId,
                requestId = requestId,
                chargeItem = detail.chargeItem.value,
                tokenDirection = detail.direction.value,
                tokenType = detail.tokenType.value,
                cacheType = detail.cacheType.value,
                tokens = detail.tokens.coerceAtLeast(0),
                priceCnyPerMillion = normalizeAmount(detail.priceCnyPerMillion),
                amountCny = normalizeAmount(detail.amountCny),
                pricingRule = detail.pricingRule,
            )
        }
    }

    /** 构造失败或未计算 Token 时的默认结果。 */
    private fun emptyTokenEstimate(): TokenEstimateResult {
        return TokenEstimateResult(
            usage = TokenUsageSummaryDto(),
            source = TokenEstimateSource.UNSUPPORTED,
            supported = false,
            note = "未执行 Token 计算",
        )
    }

    /** 统一金额精度，保持与新日志表 DECIMAL(18,8) 一致。 */
    private fun normalizeAmount(amount: BigDecimal): BigDecimal {
        return amount.max(BigDecimal.ZERO).setScale(AMOUNT_SCALE, RoundingMode.HALF_UP)
    }
}
