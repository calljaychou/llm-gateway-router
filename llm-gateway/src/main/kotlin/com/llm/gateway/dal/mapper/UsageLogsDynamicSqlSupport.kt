/*
 * Auto-generated file. Created by MyBatis Generator
 * Generation date: 2026-05-08T17:25:34.952899+08:00
 */
package com.llm.gateway.dal.mapper

import java.sql.JDBCType
import java.util.Date
import org.mybatis.dynamic.sql.SqlTable

object UsageLogsDynamicSqlSupport {
    object UsageLogs : SqlTable("usage_logs") {
        val id = column<Long>("id", JDBCType.BIGINT)

        val requestId = column<String>("request_id", JDBCType.VARCHAR)

        val userId = column<Long>("user_id", JDBCType.BIGINT)

        val deptId = column<Long>("dept_id", JDBCType.BIGINT)

        val apiKeyId = column<Long>("api_key_id", JDBCType.BIGINT)

        val vendorId = column<Long>("vendor_id", JDBCType.BIGINT)

        val modelId = column<Long>("model_id", JDBCType.BIGINT)

        val endpoint = column<String>("endpoint", JDBCType.VARCHAR)

        val isStream = column<Boolean>("is_stream", JDBCType.BIT)

        val promptTokens = column<Int>("prompt_tokens", JDBCType.INTEGER)

        val completionTokens = column<Int>("completion_tokens", JDBCType.INTEGER)

        val totalTokens = column<Int>("total_tokens", JDBCType.INTEGER)

        val latencyMs = column<Int>("latency_ms", JDBCType.INTEGER)

        val statusCode = column<Int>("status_code", JDBCType.INTEGER)

        val errorCode = column<String>("error_code", JDBCType.VARCHAR)

        val createdAt = column<Date>("created_at", JDBCType.TIMESTAMP)
    }
}