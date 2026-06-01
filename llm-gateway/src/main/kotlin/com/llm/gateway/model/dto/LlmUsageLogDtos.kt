package com.llm.gateway.model.dto

import com.llm.gateway.billing.TokenBillingResult
import com.llm.gateway.common.enums.UsageAccountingStatus
import com.llm.gateway.tokencalc.model.TokenEstimateResult
import com.llm.gateway.tokencalc.model.TokenProtocol
import java.math.BigDecimal
import java.util.Date

/**
 * LLM 用量日志写入命令。
 * 该对象面向 llm_usage_log 三表，不复用旧 usage_logs 的 UsageLogRecordCommand。
 */
data class LlmUsageLogRecordCommand(
    /** 请求 ID，作为三张新日志表的幂等键。 */
    val requestId: String,

    /** 用户 ID。 */
    val userId: Long,

    /** 部门 ID。 */
    val deptId: Long? = null,

    /** 虚拟 Key ID。 */
    val apiKeyId: Long? = null,

    /** 供应商 ID。 */
    val vendorId: Long,

    /** 模型 ID。 */
    val modelId: Long? = null,

    /** 接口类型，例如 chat.completions。 */
    val endpoint: String,

    /** Token 计算协议。 */
    val tokenProtocol: TokenProtocol,

    /** 是否流式请求。 */
    val stream: Boolean = false,

    /** 请求模型名或模型别名。 */
    val requestModel: String? = null,

    /** 上游真实模型名。 */
    val upstreamModel: String? = null,

    /** 预占金额，单位 CNY。 */
    val reservedAmountCny: BigDecimal = BigDecimal.ZERO,

    /** Token 计算结果，失败请求或未支持计算时可为空。 */
    val tokenEstimate: TokenEstimateResult? = null,

    /** Token 计费结果，失败请求或未计费时可为空。 */
    val billing: TokenBillingResult? = null,

    /** 请求耗时毫秒。 */
    val latencyMs: Int? = null,

    /** HTTP 状态码。 */
    val statusCode: Int,

    /** 错误码。 */
    val errorCode: String? = null,

    /** 结算状态。 */
    val accountingStatus: UsageAccountingStatus,

    /** 请求开始时间。 */
    val requestStartedAt: Date? = null,

    /** 结算时间。 */
    val settledAt: Date? = null,
)

/**
 * LLM 用量日志写入结果。
 */
data class LlmUsageLogWriteResult(
    /** 用量主日志 ID。 */
    val usageLogId: Long,

    /** 请求 ID。 */
    val requestId: String,

    /** 是否为本次新插入。false 表示 requestId 已存在，命中幂等保护。 */
    val inserted: Boolean,
)
