/*
 * Auto-generated file. Created by MyBatis Generator
 * Generation date: 2026-05-08T17:25:34.953433+08:00
 */
package com.llm.gateway.dal.model

import java.util.Date

data class UsageStatsDailyDepartmentRecord(
    var statDate: Date? = null,
    var deptId: Long? = null,
    var totalTokens: Long? = null,
    var requestCnt: Long? = null,
    var errorCnt: Long? = null,
    var activeUsers: Int? = null,
    var updatedAt: Date? = null
)