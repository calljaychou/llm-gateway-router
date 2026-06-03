package com.llm.gateway.model.dto

/**
 * 用户小时维度用量聚合行。
 */
data class UserUsageHourlyCountDto(
    var statDate: String = "",
    var statHour: Int = 0,
    var requestCount: Long = 0L,
)
