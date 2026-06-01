/*
 * Auto-generated file. Created by MyBatis Generator
 * Generation date: 2026-05-31T14:53:40.60965+08:00
 */
package com.llm.gateway.dal.mapper

import com.llm.gateway.dal.model.LlmUsageLogRecord
import org.apache.ibatis.annotations.DeleteProvider
import org.apache.ibatis.annotations.Insert
import org.apache.ibatis.annotations.InsertProvider
import org.apache.ibatis.annotations.Mapper
import org.apache.ibatis.annotations.Options
import org.apache.ibatis.annotations.Param
import org.apache.ibatis.annotations.Result
import org.apache.ibatis.annotations.ResultMap
import org.apache.ibatis.annotations.Results
import org.apache.ibatis.annotations.SelectProvider
import org.apache.ibatis.annotations.UpdateProvider
import org.apache.ibatis.type.JdbcType
import org.mybatis.dynamic.sql.delete.render.DeleteStatementProvider
import org.mybatis.dynamic.sql.insert.render.InsertStatementProvider
import org.mybatis.dynamic.sql.select.render.SelectStatementProvider
import org.mybatis.dynamic.sql.update.render.UpdateStatementProvider
import org.mybatis.dynamic.sql.util.SqlProviderAdapter

@Mapper
interface LlmUsageLogMapper {
    @SelectProvider(type=SqlProviderAdapter::class, method="select")
    fun count(selectStatement: SelectStatementProvider): Long

    @DeleteProvider(type=SqlProviderAdapter::class, method="delete")
    fun delete(deleteStatement: DeleteStatementProvider): Int

    @InsertProvider(type=SqlProviderAdapter::class, method="insert")
    @Options(useGeneratedKeys=true,keyProperty="record.id")
    fun insert(insertStatement: InsertStatementProvider<LlmUsageLogRecord>): Int

    @Insert(
        "\${insertStatement}"
    )
    @Options(useGeneratedKeys=true,keyProperty="records.id")
    fun insertMultiple(@Param("insertStatement") insertStatement: String, @Param("records") records: List<LlmUsageLogRecord>): Int

    @SelectProvider(type=SqlProviderAdapter::class, method="select")
    @ResultMap("LlmUsageLogRecordResult")
    fun selectOne(selectStatement: SelectStatementProvider): LlmUsageLogRecord?

    @SelectProvider(type=SqlProviderAdapter::class, method="select")
    @Results(id="LlmUsageLogRecordResult", value = [
        Result(column="id", property="id", jdbcType=JdbcType.BIGINT, id=true),
        Result(column="request_id", property="requestId", jdbcType=JdbcType.VARCHAR),
        Result(column="user_id", property="userId", jdbcType=JdbcType.BIGINT),
        Result(column="dept_id", property="deptId", jdbcType=JdbcType.BIGINT),
        Result(column="api_key_id", property="apiKeyId", jdbcType=JdbcType.BIGINT),
        Result(column="vendor_id", property="vendorId", jdbcType=JdbcType.BIGINT),
        Result(column="model_id", property="modelId", jdbcType=JdbcType.BIGINT),
        Result(column="endpoint", property="endpoint", jdbcType=JdbcType.VARCHAR),
        Result(column="token_protocol", property="tokenProtocol", jdbcType=JdbcType.VARCHAR),
        Result(column="use_stream", property="useStream", jdbcType=JdbcType.BIT),
        Result(column="request_model", property="requestModel", jdbcType=JdbcType.VARCHAR),
        Result(column="upstream_model", property="upstreamModel", jdbcType=JdbcType.VARCHAR),
        Result(column="resolved_model", property="resolvedModel", jdbcType=JdbcType.VARCHAR),
        Result(column="model_encoding", property="modelEncoding", jdbcType=JdbcType.VARCHAR),
        Result(column="token_calc_source", property="tokenCalcSource", jdbcType=JdbcType.VARCHAR),
        Result(column="token_calc_supported", property="tokenCalcSupported", jdbcType=JdbcType.BIT),
        Result(column="input_tokens", property="inputTokens", jdbcType=JdbcType.INTEGER),
        Result(column="output_tokens", property="outputTokens", jdbcType=JdbcType.INTEGER),
        Result(column="total_tokens", property="totalTokens", jdbcType=JdbcType.INTEGER),
        Result(column="billable_input_tokens", property="billableInputTokens", jdbcType=JdbcType.INTEGER),
        Result(column="billable_output_tokens", property="billableOutputTokens", jdbcType=JdbcType.INTEGER),
        Result(column="reserved_amount_cny", property="reservedAmountCny", jdbcType=JdbcType.DECIMAL),
        Result(column="amount_cny", property="amountCny", jdbcType=JdbcType.DECIMAL),
        Result(column="billing_strategy", property="billingStrategy", jdbcType=JdbcType.VARCHAR),
        Result(column="billing_currency", property="billingCurrency", jdbcType=JdbcType.VARCHAR),
        Result(column="latency_ms", property="latencyMs", jdbcType=JdbcType.INTEGER),
        Result(column="status_code", property="statusCode", jdbcType=JdbcType.INTEGER),
        Result(column="error_code", property="errorCode", jdbcType=JdbcType.VARCHAR),
        Result(column="accounting_status", property="accountingStatus", jdbcType=JdbcType.VARCHAR),
        Result(column="request_started_at", property="requestStartedAt", jdbcType=JdbcType.TIMESTAMP),
        Result(column="settled_at", property="settledAt", jdbcType=JdbcType.TIMESTAMP),
        Result(column="created_at", property="createdAt", jdbcType=JdbcType.TIMESTAMP),
        Result(column="token_calc_note", property="tokenCalcNote", jdbcType=JdbcType.LONGVARCHAR),
        Result(column="token_calc_detail", property="tokenCalcDetail", jdbcType=JdbcType.LONGVARCHAR),
        Result(column="billing_detail", property="billingDetail", jdbcType=JdbcType.LONGVARCHAR)
    ])
    fun selectMany(selectStatement: SelectStatementProvider): List<LlmUsageLogRecord>

    @UpdateProvider(type=SqlProviderAdapter::class, method="update")
    fun update(updateStatement: UpdateStatementProvider): Int
}