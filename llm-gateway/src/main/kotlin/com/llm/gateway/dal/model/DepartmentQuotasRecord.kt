/*
 * Auto-generated file. Created by MyBatis Generator
 * Generation date: 2026-05-07T18:46:02.173239+08:00
 */
package com.llm.gateway.dal.model

import java.util.Date

data class DepartmentQuotasRecord(
    var id: Long? = null,
    var deptId: Long? = null,
    var totalTokens: Long? = null,
    var usedTokens: Long? = null,
    var period: String? = null,
    var lastResetAt: Date? = null,
    var createdAt: Date? = null,
    var updatedAt: Date? = null
)