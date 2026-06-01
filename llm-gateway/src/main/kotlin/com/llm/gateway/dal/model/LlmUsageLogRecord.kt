/*
 * Auto-generated file. Created by MyBatis Generator
 * Generation date: 2026-05-31T14:53:40.60859+08:00
 */
package com.llm.gateway.dal.model

import java.math.BigDecimal
import java.util.Date

data class LlmUsageLogRecord(
    var id: Long? = null,
    var requestId: String? = null,
    var userId: Long? = null,
    var deptId: Long? = null,
    var apiKeyId: Long? = null,
    var vendorId: Long? = null,
    var modelId: Long? = null,
    var endpoint: String? = null,
    var tokenProtocol: String? = null,
    var useStream: Boolean? = null,
    var requestModel: String? = null,
    var upstreamModel: String? = null,
    var resolvedModel: String? = null,
    var modelEncoding: String? = null,
    var tokenCalcSource: String? = null,
    var tokenCalcSupported: Boolean? = null,
    var inputTokens: Int? = null,
    var outputTokens: Int? = null,
    var totalTokens: Int? = null,
    var billableInputTokens: Int? = null,
    var billableOutputTokens: Int? = null,
    var reservedAmountCny: BigDecimal? = null,
    var amountCny: BigDecimal? = null,
    var billingStrategy: String? = null,
    var billingCurrency: String? = null,
    var latencyMs: Int? = null,
    var statusCode: Int? = null,
    var errorCode: String? = null,
    var accountingStatus: String? = null,
    var requestStartedAt: Date? = null,
    var settledAt: Date? = null,
    var createdAt: Date? = null,
    var tokenCalcNote: String? = null,
    var tokenCalcDetail: String? = null,
    var billingDetail: String? = null
)