/*
 * Auto-generated file. Created by MyBatis Generator
 * Generation date: 2026-05-21T18:47:35.931595+08:00
 */
package com.llm.gateway.dal.mapper

import com.llm.gateway.dal.model.UserQuotaAccountsRecord
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
interface UserQuotaAccountsMapper {
    @SelectProvider(type=SqlProviderAdapter::class, method="select")
    fun count(selectStatement: SelectStatementProvider): Long

    @DeleteProvider(type=SqlProviderAdapter::class, method="delete")
    fun delete(deleteStatement: DeleteStatementProvider): Int

    @InsertProvider(type=SqlProviderAdapter::class, method="insert")
    @Options(useGeneratedKeys=true,keyProperty="record.id")
    fun insert(insertStatement: InsertStatementProvider<UserQuotaAccountsRecord>): Int

    @Insert(
        "\${insertStatement}"
    )
    @Options(useGeneratedKeys=true,keyProperty="list.id")
    fun insertMultiple(@Param("insertStatement") insertStatement: String, @Param("list") records: List<UserQuotaAccountsRecord>): Int

    @SelectProvider(type=SqlProviderAdapter::class, method="select")
    @ResultMap("UserQuotaAccountsRecordResult")
    fun selectOne(selectStatement: SelectStatementProvider): UserQuotaAccountsRecord?

    @SelectProvider(type=SqlProviderAdapter::class, method="select")
    @Results(id="UserQuotaAccountsRecordResult", value = [
        Result(column="id", property="id", jdbcType=JdbcType.BIGINT, id=true),
        Result(column="user_id", property="userId", jdbcType=JdbcType.BIGINT),
        Result(column="current_quota_tokens", property="currentQuotaTokens", jdbcType=JdbcType.BIGINT),
        Result(column="used_tokens", property="usedTokens", jdbcType=JdbcType.BIGINT),
        Result(column="expired_tokens", property="expiredTokens", jdbcType=JdbcType.BIGINT),
        Result(column="transferred_in_tokens", property="transferredInTokens", jdbcType=JdbcType.BIGINT),
        Result(column="transferred_out_tokens", property="transferredOutTokens", jdbcType=JdbcType.BIGINT),
        Result(column="available_tokens", property="availableTokens", jdbcType=JdbcType.BIGINT),
        Result(column="allow_transfer_out", property="allowTransferOut", jdbcType=JdbcType.BIT),
        Result(column="earliest_expire_at", property="earliestExpireAt", jdbcType=JdbcType.TIMESTAMP),
        Result(column="updated_time", property="updatedTime", jdbcType=JdbcType.TIMESTAMP),
        Result(column="created_time", property="createdTime", jdbcType=JdbcType.TIMESTAMP)
    ])
    fun selectMany(selectStatement: SelectStatementProvider): List<UserQuotaAccountsRecord>

    @UpdateProvider(type=SqlProviderAdapter::class, method="update")
    fun update(updateStatement: UpdateStatementProvider): Int
}