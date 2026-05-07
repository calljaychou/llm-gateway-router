/*
 * Auto-generated file. Created by MyBatis Generator
 * Generation date: 2026-05-07T18:46:02.175075+08:00
 */
package com.llm.gateway.dal.mapper

import java.sql.JDBCType
import java.util.Date
import org.mybatis.dynamic.sql.SqlTable

object UsageStatsDailyUserDynamicSqlSupport {
    object UsageStatsDailyUser : SqlTable("usage_stats_daily_user") {
        val statDate = column<Date>("stat_date", JDBCType.DATE)

        val userId = column<Long>("user_id", JDBCType.BIGINT)

        val deptId = column<Long>("dept_id", JDBCType.BIGINT)

        val totalTokens = column<Long>("total_tokens", JDBCType.BIGINT)

        val requestCnt = column<Long>("request_cnt", JDBCType.BIGINT)

        val errorCnt = column<Long>("error_cnt", JDBCType.BIGINT)

        val updatedAt = column<Date>("updated_at", JDBCType.TIMESTAMP)
    }
}