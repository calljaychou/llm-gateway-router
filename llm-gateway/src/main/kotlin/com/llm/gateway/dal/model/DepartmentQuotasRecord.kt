/*
 * Auto-generated file. Created by MyBatis Generator
 * Generation date: 2026-05-08T17:25:34.95231+08:00
 */
package com.llm.gateway.dal.model

import java.util.Date

data class DepartmentQuotasRecord(
    var id: Long? = null,
    var deptId: Long? = null,
    var quotaTokens: Long? = null,
    var period: String? = null,
    var status: Int? = null,
    var remark: String? = null,
    var createdAt: Date? = null,
    var updatedAt: Date? = null
)