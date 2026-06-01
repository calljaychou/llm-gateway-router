/*
 * Auto-generated file. Created by MyBatis Generator
 * Generation date: 2026-05-31T14:53:40.611123+08:00
 */
package com.llm.gateway.dal.mapper

import com.llm.gateway.dal.model.LlmUsageTokenDetailRecord
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
interface LlmUsageTokenDetailMapper {
    @SelectProvider(type=SqlProviderAdapter::class, method="select")
    fun count(selectStatement: SelectStatementProvider): Long

    @DeleteProvider(type=SqlProviderAdapter::class, method="delete")
    fun delete(deleteStatement: DeleteStatementProvider): Int

    @InsertProvider(type=SqlProviderAdapter::class, method="insert")
    @Options(useGeneratedKeys=true,keyProperty="record.id")
    fun insert(insertStatement: InsertStatementProvider<LlmUsageTokenDetailRecord>): Int

    @Insert(
        "\${insertStatement}"
    )
    @Options(useGeneratedKeys=true,keyProperty="records.id")
    fun insertMultiple(@Param("insertStatement") insertStatement: String, @Param("records") records: List<LlmUsageTokenDetailRecord>): Int

    @SelectProvider(type=SqlProviderAdapter::class, method="select")
    @ResultMap("LlmUsageTokenDetailRecordResult")
    fun selectOne(selectStatement: SelectStatementProvider): LlmUsageTokenDetailRecord?

    @SelectProvider(type=SqlProviderAdapter::class, method="select")
    @Results(id="LlmUsageTokenDetailRecordResult", value = [
        Result(column="id", property="id", jdbcType=JdbcType.BIGINT, id=true),
        Result(column="usage_log_id", property="usageLogId", jdbcType=JdbcType.BIGINT),
        Result(column="request_id", property="requestId", jdbcType=JdbcType.VARCHAR),
        Result(column="token_direction", property="tokenDirection", jdbcType=JdbcType.VARCHAR),
        Result(column="token_type", property="tokenType", jdbcType=JdbcType.VARCHAR),
        Result(column="cache_type", property="cacheType", jdbcType=JdbcType.VARCHAR),
        Result(column="tokens", property="tokens", jdbcType=JdbcType.INTEGER),
        Result(column="billable_tokens", property="billableTokens", jdbcType=JdbcType.INTEGER),
        Result(column="source", property="source", jdbcType=JdbcType.VARCHAR),
        Result(column="provider_field", property="providerField", jdbcType=JdbcType.VARCHAR),
        Result(column="note", property="note", jdbcType=JdbcType.VARCHAR),
        Result(column="created_at", property="createdAt", jdbcType=JdbcType.TIMESTAMP)
    ])
    fun selectMany(selectStatement: SelectStatementProvider): List<LlmUsageTokenDetailRecord>

    @UpdateProvider(type=SqlProviderAdapter::class, method="update")
    fun update(updateStatement: UpdateStatementProvider): Int
}