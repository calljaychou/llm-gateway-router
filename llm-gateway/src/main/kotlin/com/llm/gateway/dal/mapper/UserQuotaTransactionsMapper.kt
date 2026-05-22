/*
 * Auto-generated file. Created by MyBatis Generator
 * Generation date: 2026-05-21T18:47:35.939659+08:00
 */
package com.llm.gateway.dal.mapper

import com.llm.gateway.dal.model.UserQuotaTransactionsRecord
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
interface UserQuotaTransactionsMapper {
    @SelectProvider(type=SqlProviderAdapter::class, method="select")
    fun count(selectStatement: SelectStatementProvider): Long

    @DeleteProvider(type=SqlProviderAdapter::class, method="delete")
    fun delete(deleteStatement: DeleteStatementProvider): Int

    @InsertProvider(type=SqlProviderAdapter::class, method="insert")
    @Options(useGeneratedKeys=true,keyProperty="record.id")
    fun insert(insertStatement: InsertStatementProvider<UserQuotaTransactionsRecord>): Int

    @Insert(
        "\${insertStatement}"
    )
    @Options(useGeneratedKeys=true,keyProperty="list.id")
    fun insertMultiple(@Param("insertStatement") insertStatement: String, @Param("list") records: List<UserQuotaTransactionsRecord>): Int

    @SelectProvider(type=SqlProviderAdapter::class, method="select")
    @ResultMap("UserQuotaTransactionsRecordResult")
    fun selectOne(selectStatement: SelectStatementProvider): UserQuotaTransactionsRecord?

    @SelectProvider(type=SqlProviderAdapter::class, method="select")
    @Results(id="UserQuotaTransactionsRecordResult", value = [
        Result(column="id", property="id", jdbcType=JdbcType.BIGINT, id=true),
        Result(column="biz_no", property="bizNo", jdbcType=JdbcType.VARCHAR),
        Result(column="user_id", property="userId", jdbcType=JdbcType.BIGINT),
        Result(column="grant_id", property="grantId", jdbcType=JdbcType.BIGINT),
        Result(column="change_type", property="changeType", jdbcType=JdbcType.VARCHAR),
        Result(column="delta_tokens", property="deltaTokens", jdbcType=JdbcType.BIGINT),
        Result(column="quota_before", property="quotaBefore", jdbcType=JdbcType.BIGINT),
        Result(column="quota_after", property="quotaAfter", jdbcType=JdbcType.BIGINT),
        Result(column="available_before", property="availableBefore", jdbcType=JdbcType.BIGINT),
        Result(column="available_after", property="availableAfter", jdbcType=JdbcType.BIGINT),
        Result(column="counterparty_user_id", property="counterpartyUserId", jdbcType=JdbcType.BIGINT),
        Result(column="request_id", property="requestId", jdbcType=JdbcType.VARCHAR),
        Result(column="operator_user_id", property="operatorUserId", jdbcType=JdbcType.BIGINT),
        Result(column="remark", property="remark", jdbcType=JdbcType.VARCHAR),
        Result(column="created_time", property="createdTime", jdbcType=JdbcType.TIMESTAMP)
    ])
    fun selectMany(selectStatement: SelectStatementProvider): List<UserQuotaTransactionsRecord>

    @UpdateProvider(type=SqlProviderAdapter::class, method="update")
    fun update(updateStatement: UpdateStatementProvider): Int
}