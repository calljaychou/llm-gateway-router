/*
 * Auto-generated file. Created by MyBatis Generator
 * Generation date: 2026-05-31T14:53:40.610889+08:00
 */
package com.llm.gateway.dal.mapper

import java.sql.JDBCType
import java.util.Date
import org.mybatis.dynamic.sql.SqlTable

object LlmUsageTokenDetailDynamicSqlSupport {
    object LlmUsageTokenDetail : SqlTable("llm_usage_token_detail") {
        val id = column<Long>("id", JDBCType.BIGINT)

        val usageLogId = column<Long>("usage_log_id", JDBCType.BIGINT)

        val requestId = column<String>("request_id", JDBCType.VARCHAR)

        val tokenDirection = column<String>("token_direction", JDBCType.VARCHAR)

        val tokenType = column<String>("token_type", JDBCType.VARCHAR)

        val cacheType = column<String>("cache_type", JDBCType.VARCHAR)

        val tokens = column<Int>("tokens", JDBCType.INTEGER)

        val billableTokens = column<Int>("billable_tokens", JDBCType.INTEGER)

        val source = column<String>("source", JDBCType.VARCHAR)

        val providerField = column<String>("provider_field", JDBCType.VARCHAR)

        val note = column<String>("note", JDBCType.VARCHAR)

        val createdAt = column<Date>("created_at", JDBCType.TIMESTAMP)
    }
}