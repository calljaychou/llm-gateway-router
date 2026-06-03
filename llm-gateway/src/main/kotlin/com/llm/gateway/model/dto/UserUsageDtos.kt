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
