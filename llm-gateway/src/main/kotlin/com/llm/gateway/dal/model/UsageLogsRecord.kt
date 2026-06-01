/*
 * Auto-generated file. Created by MyBatis Generator
 * Generation date: 2026-05-31T14:53:40.597334+08:00
 */
package com.llm.gateway.dal.model

import java.math.BigDecimal
import java.util.Date

data class UsageLogsRecord(
    var id: Long? = null,
    var requestId: String? = null,
    var userId: Long? = null,
    var deptId: Long? = null,
    var apiKeyId: Long? = null,
    var vendorId: Long? = null,
    var modelId: Long? = null,
    var endpoint: String? = null,
    var useStream: Boolean? = null,
    var reservedTokens: Int? = null,
    var estimatedAmountCny: BigDecimal? = null,
    var promptTokens: Int? = null,
    var promptCachedTokens: Int? = null,
    var promptCacheMissTokens: Int? = null,
    var promptAudioTokens: Int? = null,
    var completionTokens: Int? = null,
    var completionReasoningTokens: Int? = null,
    var completionAudioTokens: Int? = null,
    var completionAcceptedPredictionTokens: Int? = null,
    var completionRejectedPredictionTokens: Int? = null,
    var totalTokens: Int? = null,
    var amountCny: BigDecimal? = null,
    var latencyMs: Int? = null,
    var statusCode: Int? = null,
    var errorCode: String? = null,
    var accountingStatus: String? = null,
    var settledAt: Date? = null,
    var retryCount: Int? = null,
    var calcSource: String? = null,
    var createdAt: Date? = null,
    var amountCalcDetail: String? = null
)