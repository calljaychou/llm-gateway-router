package com.llm.gateway.model.dto

/**
 * 用户小时维度用量聚合行。
 */
data class UserUsageHourlyCountDto(
    var statDate: String = "",
    var statHour: Int = 0,
    var requestCount: Long = 0L,
)

/**
 * 用户模型维度用量聚合行。
 */
data class UserUsageModelCountDto(
    var modelId: Long = 0L,
    var modelName: String = "",
    var vendorId: Long = 0L,
    var usageCount: Long = 0L,
)

/**
 * 用户使用日志列表查询行。
 */
data class UserUsageLogListItemDto(
    var usageLogId: Long = 0L,
    var requestId: String = "",
    var vendorId: Long? = null,
    var modelId: Long? = null,
    var requestModel: String? = null,
    var resolvedModel: String? = null,
    var inputTokens: Int = 0,
    var outputTokens: Int = 0,
    var totalTokens: Int = 0,
    var usedAt: java.util.Date? = null,
    var latencyMs: Int = 0,
)
