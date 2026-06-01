/*
 * Auto-generated file. Created by MyBatis Generator
 * Generation date: 2026-05-31T14:53:40.612618+08:00
 */
package com.llm.gateway.dal.mapper

import com.llm.gateway.dal.model.LlmUsageBillingDetailRecord
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
interface LlmUsageBillingDetailMapper {
    @SelectProvider(type=SqlProviderAdapter::class, method="select")
    fun count(selectStatement: SelectStatementProvider): Long

    @DeleteProvider(type=SqlProviderAdapter::class, method="delete")
    fun delete(deleteStatement: DeleteStatementProvider): Int

    @InsertProvider(type=SqlProviderAdapter::class, method="insert")
    @Options(useGeneratedKeys=true,keyProperty="record.id")
    fun insert(insertStatement: InsertStatementProvider<LlmUsageBillingDetailRecord>): Int

    @Insert(
        "\${insertStatement}"
    )
    @Options(useGeneratedKeys=true,keyProperty="records.id")
    fun insertMultiple(@Param("insertStatement") insertStatement: String, @Param("records") records: List<LlmUsageBillingDetailRecord>): Int

    @SelectProvider(type=SqlProviderAdapter::class, method="select")
    @ResultMap("LlmUsageBillingDetailRecordResult")
    fun selectOne(selectStatement: SelectStatementProvider): LlmUsageBillingDetailRecord?

    @SelectProvider(type=SqlProviderAdapter::class, method="select")
    @Results(id="LlmUsageBillingDetailRecordResult", value = [
        Result(column="id", property="id", jdbcType=JdbcType.BIGINT, id=true),
        Result(column="usage_log_id", property="usageLogId", jdbcType=JdbcType.BIGINT),
        Result(column="request_id", property="requestId", jdbcType=JdbcType.VARCHAR),
        Result(column="charge_item", property="chargeItem", jdbcType=JdbcType.VARCHAR),
        Result(column="token_direction", property="tokenDirection", jdbcType=JdbcType.VARCHAR),
        Result(column="token_type", property="tokenType", jdbcType=JdbcType.VARCHAR),
        Result(column="cache_type", property="cacheType", jdbcType=JdbcType.VARCHAR),
        Result(column="tokens", property="tokens", jdbcType=JdbcType.INTEGER),
        Result(column="price_cny_per_million", property="priceCnyPerMillion", jdbcType=JdbcType.DECIMAL),
        Result(column="amount_cny", property="amountCny", jdbcType=JdbcType.DECIMAL),
        Result(column="pricing_rule", property="pricingRule", jdbcType=JdbcType.VARCHAR),
        Result(column="created_at", property="createdAt", jdbcType=JdbcType.TIMESTAMP)
    ])
    fun selectMany(selectStatement: SelectStatementProvider): List<LlmUsageBillingDetailRecord>

    @UpdateProvider(type=SqlProviderAdapter::class, method="update")
    fun update(updateStatement: UpdateStatementProvider): Int
}