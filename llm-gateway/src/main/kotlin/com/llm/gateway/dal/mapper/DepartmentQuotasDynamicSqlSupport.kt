/*
 * Auto-generated file. Created by MyBatis Generator
 * Generation date: 2026-05-08T17:25:34.952372+08:00
 */
package com.llm.gateway.dal.mapper

import java.sql.JDBCType
import java.util.Date
import org.mybatis.dynamic.sql.SqlTable

object DepartmentQuotasDynamicSqlSupport {
    object DepartmentQuotas : SqlTable("department_quotas") {
        val id = column<Long>("id", JDBCType.BIGINT)

        val deptId = column<Long>("dept_id", JDBCType.BIGINT)

        val quotaTokens = column<Long>("quota_tokens", JDBCType.BIGINT)

        val period = column<String>("period", JDBCType.CHAR)

        val status = column<Int>("status", JDBCType.INTEGER)

        val remark = column<String>("remark", JDBCType.VARCHAR)

        val createdAt = column<Date>("created_at", JDBCType.TIMESTAMP)

        val updatedAt = column<Date>("updated_at", JDBCType.TIMESTAMP)
    }
}