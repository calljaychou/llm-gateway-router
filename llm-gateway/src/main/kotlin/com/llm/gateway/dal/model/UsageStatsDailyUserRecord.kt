/*
 * Auto-generated file. Created by MyBatis Generator
 * Generation date: 2026-05-07T18:46:02.175022+08:00
 */
package com.llm.gateway.dal.model

import java.util.Date

data class UsageStatsDailyUserRecord(
    var statDate: Date? = null,
    var userId: Long? = null,
    var deptId: Long? = null,
    var totalTokens: Long? = null,
    var requestCnt: Long? = null,
    var errorCnt: Long? = null,
    var updatedAt: Date? = null
)