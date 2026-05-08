/*
 * Auto-generated file. Created by MyBatis Generator
 * Generation date: 2026-05-08T17:25:34.954061+08:00
 */
package com.llm.gateway.dal.mapper

import com.llm.gateway.dal.model.UsageStatsDailyUserRecord
import org.apache.ibatis.annotations.DeleteProvider
import org.apache.ibatis.annotations.Insert
import org.apache.ibatis.annotations.InsertProvider
import org.apache.ibatis.annotations.Mapper
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
interface UsageStatsDailyUserMapper {
    @SelectProvider(type=SqlProviderAdapter::class, method="select")
    fun count(selectStatement: SelectStatementProvider): Long

    @DeleteProvider(type=SqlProviderAdapter::class, method="delete")
    fun delete(deleteStatement: DeleteStatementProvider): Int

    @InsertProvider(type=SqlProviderAdapter::class, method="insert")
    fun insert(insertStatement: InsertStatementProvider<UsageStatsDailyUserRecord>): Int

    @Insert(
        "\${insertStatement}"
    )
    fun insertMultiple(@Param("insertStatement") insertStatement: String, @Param("records") records: List<UsageStatsDailyUserRecord>): Int

    @SelectProvider(type=SqlProviderAdapter::class, method="select")
    @ResultMap("UsageStatsDailyUserRecordResult")
    fun selectOne(selectStatement: SelectStatementProvider): UsageStatsDailyUserRecord?

    @SelectProvider(type=SqlProviderAdapter::class, method="select")
    @Results(id="UsageStatsDailyUserRecordResult", value = [
        Result(column="stat_date", property="statDate", jdbcType=JdbcType.DATE, id=true),
        Result(column="user_id", property="userId", jdbcType=JdbcType.BIGINT, id=true),
        Result(column="dept_id", property="deptId", jdbcType=JdbcType.BIGINT),
        Result(column="total_tokens", property="totalTokens", jdbcType=JdbcType.BIGINT),
        Result(column="request_cnt", property="requestCnt", jdbcType=JdbcType.BIGINT),
        Result(column="error_cnt", property="errorCnt", jdbcType=JdbcType.BIGINT),
        Result(column="updated_at", property="updatedAt", jdbcType=JdbcType.TIMESTAMP)
    ])
    fun selectMany(selectStatement: SelectStatementProvider): List<UsageStatsDailyUserRecord>

    @UpdateProvider(type=SqlProviderAdapter::class, method="update")
    fun update(updateStatement: UpdateStatementProvider): Int
}