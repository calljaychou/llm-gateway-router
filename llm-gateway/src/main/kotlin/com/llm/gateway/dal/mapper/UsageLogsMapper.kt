/*
 * Auto-generated file. Created by MyBatis Generator
 * Generation date: 2026-05-21T18:47:35.940679+08:00
 */
package com.llm.gateway.dal.mapper

import com.llm.gateway.dal.model.UsageLogsRecord
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
interface UsageLogsMapper {
    @SelectProvider(type=SqlProviderAdapter::class, method="select")
    fun count(selectStatement: SelectStatementProvider): Long

    @DeleteProvider(type=SqlProviderAdapter::class, method="delete")
    fun delete(deleteStatement: DeleteStatementProvider): Int

    @InsertProvider(type=SqlProviderAdapter::class, method="insert")
    @Options(useGeneratedKeys=true,keyProperty="record.id")
    fun insert(insertStatement: InsertStatementProvider<UsageLogsRecord>): Int

    @Insert(
        "\${insertStatement}"
    )
    @Options(useGeneratedKeys=true,keyProperty="list.id")
    fun insertMultiple(@Param("insertStatement") insertStatement: String, @Param("list") records: List<UsageLogsRecord>): Int

    @SelectProvider(type=SqlProviderAdapter::class, method="select")
    @ResultMap("UsageLogsRecordResult")
    fun selectOne(selectStatement: SelectStatementProvider): UsageLogsRecord?

    @SelectProvider(type=SqlProviderAdapter::class, method="select")
    @Results(id="UsageLogsRecordResult", value = [
        Result(column="id", property="id", jdbcType=JdbcType.BIGINT, id=true),
        Result(column="request_id", property="requestId", jdbcType=JdbcType.VARCHAR),
        Result(column="user_id", property="userId", jdbcType=JdbcType.BIGINT),
        Result(column="dept_id", property="deptId", jdbcType=JdbcType.BIGINT),
        Result(column="api_key_id", property="apiKeyId", jdbcType=JdbcType.BIGINT),
        Result(column="vendor_id", property="vendorId", jdbcType=JdbcType.BIGINT),
        Result(column="model_id", property="modelId", jdbcType=JdbcType.BIGINT),
        Result(column="endpoint", property="endpoint", jdbcType=JdbcType.VARCHAR),
        Result(column="use_stream", property="useStream", jdbcType=JdbcType.BIT),
        Result(column="reserved_tokens", property="reservedTokens", jdbcType=JdbcType.INTEGER),
        Result(column="prompt_tokens", property="promptTokens", jdbcType=JdbcType.INTEGER),
        Result(column="completion_tokens", property="completionTokens", jdbcType=JdbcType.INTEGER),
        Result(column="total_tokens", property="totalTokens", jdbcType=JdbcType.INTEGER),
        Result(column="latency_ms", property="latencyMs", jdbcType=JdbcType.INTEGER),
        Result(column="status_code", property="statusCode", jdbcType=JdbcType.INTEGER),
        Result(column="error_code", property="errorCode", jdbcType=JdbcType.VARCHAR),
        Result(column="accounting_status", property="accountingStatus", jdbcType=JdbcType.VARCHAR),
        Result(column="settled_at", property="settledAt", jdbcType=JdbcType.TIMESTAMP),
        Result(column="retry_count", property="retryCount", jdbcType=JdbcType.INTEGER),
        Result(column="calc_source", property="calcSource", jdbcType=JdbcType.VARCHAR),
        Result(column="created_at", property="createdAt", jdbcType=JdbcType.TIMESTAMP)
    ])
    fun selectMany(selectStatement: SelectStatementProvider): List<UsageLogsRecord>

    @UpdateProvider(type=SqlProviderAdapter::class, method="update")
    fun update(updateStatement: UpdateStatementProvider): Int
}