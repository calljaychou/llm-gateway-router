package com.llm.gateway.model.dto

import java.math.BigDecimal
import java.util.Date

/**
 * 管理端使用日志列表查询行。
 */
data class AdminStatisticsUsageLogItemDto(
    var usageLogId: Long = 0L,
    var requestId: String = "",
    var userId: Long = 0L,
    var userName: String = "",
    var userAccount: String = "",
    var deptName: String = "",
    var vendorId: Long? = null,
    var vendorName: String = "",
    var modelId: Long? = null,
    var modelName: String = "",
    var inputTokens: Int = 0,
    var outputTokens: Int = 0,
    var totalTokens: Int = 0,
    var usedAt: Date? = null,
    var latencyMs: Int = 0,
)

/**
 * 管理端部门使用统计查询行。
 */
data class AdminStatisticsDepartmentUsageItemDto(
    var deptId: Long = 0L,
    var deptName: String = "",
    var usageCount: Long = 0L,
    var totalTokens: Long = 0L,
)

/**
 * 管理端用户使用统计查询行。
 */
data class AdminStatisticsUserUsageItemDto(
    var userId: Long = 0L,
    var userName: String = "",
    var username: String? = null,
    var mobile: String? = null,
    var email: String? = null,
    var deptName: String = "",
    var balance: BigDecimal = BigDecimal.ZERO,
    var totalTokens: Long = 0L,
    var usageCount: Long = 0L,
)
