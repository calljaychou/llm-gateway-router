/*
 * Auto-generated file. Created by MyBatis Generator
 * Generation date: 2026-05-31T14:53:40.609012+08:00
 */
package com.llm.gateway.dal.mapper

import java.math.BigDecimal
import java.sql.JDBCType
import java.util.Date
import org.mybatis.dynamic.sql.SqlTable

object LlmUsageLogDynamicSqlSupport {
    object LlmUsageLog : SqlTable("llm_usage_log") {
        val id = column<Long>("id", JDBCType.BIGINT)

        val requestId = column<String>("request_id", JDBCType.VARCHAR)

        val userId = column<Long>("user_id", JDBCType.BIGINT)

        val deptId = column<Long>("dept_id", JDBCType.BIGINT)

        val apiKeyId = column<Long>("api_key_id", JDBCType.BIGINT)

        val vendorId = column<Long>("vendor_id", JDBCType.BIGINT)

        val modelId = column<Long>("model_id", JDBCType.BIGINT)

        val endpoint = column<String>("endpoint", JDBCType.VARCHAR)

        val tokenProtocol = column<String>("token_protocol", JDBCType.VARCHAR)

        val useStream = column<Boolean>("use_stream", JDBCType.BIT)

        val requestModel = column<String>("request_model", JDBCType.VARCHAR)

        val upstreamModel = column<String>("upstream_model", JDBCType.VARCHAR)

        val resolvedModel = column<String>("resolved_model", JDBCType.VARCHAR)

        val modelEncoding = column<String>("model_encoding", JDBCType.VARCHAR)

        val tokenCalcSource = column<String>("token_calc_source", JDBCType.VARCHAR)

        val tokenCalcSupported = column<Boolean>("token_calc_supported", JDBCType.BIT)

        val inputTokens = column<Int>("input_tokens", JDBCType.INTEGER)

        val outputTokens = column<Int>("output_tokens", JDBCType.INTEGER)

        val totalTokens = column<Int>("total_tokens", JDBCType.INTEGER)

        val billableInputTokens = column<Int>("billable_input_tokens", JDBCType.INTEGER)

        val billableOutputTokens = column<Int>("billable_output_tokens", JDBCType.INTEGER)

        val reservedAmountCny = column<BigDecimal>("reserved_amount_cny", JDBCType.DECIMAL)

        val amountCny = column<BigDecimal>("amount_cny", JDBCType.DECIMAL)

        val billingStrategy = column<String>("billing_strategy", JDBCType.VARCHAR)

        val billingCurrency = column<String>("billing_currency", JDBCType.VARCHAR)

        val latencyMs = column<Int>("latency_ms", JDBCType.INTEGER)

        val statusCode = column<Int>("status_code", JDBCType.INTEGER)

        val errorCode = column<String>("error_code", JDBCType.VARCHAR)

        val accountingStatus = column<String>("accounting_status", JDBCType.VARCHAR)

        val requestStartedAt = column<Date>("request_started_at", JDBCType.TIMESTAMP)

        val settledAt = column<Date>("settled_at", JDBCType.TIMESTAMP)

        val createdAt = column<Date>("created_at", JDBCType.TIMESTAMP)

        val tokenCalcNote = column<String>("token_calc_note", JDBCType.LONGVARCHAR)

        val tokenCalcDetail = column<String>("token_calc_detail", JDBCType.LONGVARCHAR)

        val billingDetail = column<String>("billing_detail", JDBCType.LONGVARCHAR)
    }
}