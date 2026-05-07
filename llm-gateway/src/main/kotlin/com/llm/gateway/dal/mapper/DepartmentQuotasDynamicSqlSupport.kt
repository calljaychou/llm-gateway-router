/*
 * Auto-generated file. Created by MyBatis Generator
 * Generation date: 2026-05-07T18:46:02.173296+08:00
 */
package com.llm.gateway.dal.mapper

import java.sql.JDBCType
import java.util.Date
import org.mybatis.dynamic.sql.SqlTable

object DepartmentQuotasDynamicSqlSupport {
    object DepartmentQuotas : SqlTable("department_quotas") {
        val id = column<Long>("id", JDBCType.BIGINT)

        val deptId = column<Long>("dept_id", JDBCType.BIGINT)

        val totalTokens = column<Long>("total_tokens", JDBCType.BIGINT)

        val usedTokens = column<Long>("used_tokens", JDBCType.BIGINT)

        val period = column<String>("period", JDBCType.CHAR)

        val lastResetAt = column<Date>("last_reset_at", JDBCType.TIMESTAMP)

        val createdAt = column<Date>("created_at", JDBCType.TIMESTAMP)

        val updatedAt = column<Date>("updated_at", JDBCType.TIMESTAMP)
    }
}