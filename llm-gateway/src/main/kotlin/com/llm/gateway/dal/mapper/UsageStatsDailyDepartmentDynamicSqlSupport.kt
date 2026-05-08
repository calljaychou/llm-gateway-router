/*
 * Auto-generated file. Created by MyBatis Generator
 * Generation date: 2026-05-08T17:25:34.953491+08:00
 */
package com.llm.gateway.dal.mapper

import java.sql.JDBCType
import java.util.Date
import org.mybatis.dynamic.sql.SqlTable

object UsageStatsDailyDepartmentDynamicSqlSupport {
    object UsageStatsDailyDepartment : SqlTable("usage_stats_daily_department") {
        val statDate = column<Date>("stat_date", JDBCType.DATE)

        val deptId = column<Long>("dept_id", JDBCType.BIGINT)

        val totalTokens = column<Long>("total_tokens", JDBCType.BIGINT)

        val requestCnt = column<Long>("request_cnt", JDBCType.BIGINT)

        val errorCnt = column<Long>("error_cnt", JDBCType.BIGINT)

        val activeUsers = column<Int>("active_users", JDBCType.INTEGER)

        val updatedAt = column<Date>("updated_at", JDBCType.TIMESTAMP)
    }
}