/*
 * Auto-generated file. Created by MyBatis Generator
 * Generation date: 2026-05-21T18:47:35.937941+08:00
 */
package com.llm.gateway.dal.mapper

import com.llm.gateway.dal.model.UserQuotaGrantsRecord
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
import org.apache.ibatis.annotations.Update
import org.apache.ibatis.annotations.UpdateProvider
import org.apache.ibatis.type.JdbcType
import org.mybatis.dynamic.sql.delete.render.DeleteStatementProvider
import org.mybatis.dynamic.sql.insert.render.InsertStatementProvider
import org.mybatis.dynamic.sql.select.render.SelectStatementProvider
import org.mybatis.dynamic.sql.update.render.UpdateStatementProvider
import org.mybatis.dynamic.sql.util.SqlProviderAdapter
import java.util.Date

@Mapper
interface UserQuotaGrantsMapper {
    @SelectProvider(type=SqlProviderAdapter::class, method="select")
    fun count(selectStatement: SelectStatementProvider): Long

    @DeleteProvider(type=SqlProviderAdapter::class, method="delete")
    fun delete(deleteStatement: DeleteStatementProvider): Int

    @InsertProvider(type=SqlProviderAdapter::class, method="insert")
    @Options(useGeneratedKeys=true,keyProperty="record.id")
    fun insert(insertStatement: InsertStatementProvider<UserQuotaGrantsRecord>): Int

    @Insert(
        "\${insertStatement}"
    )
    @Options(useGeneratedKeys=true,keyProperty="list.id")
    fun insertMultiple(@Param("insertStatement") insertStatement: String, @Param("list") records: List<UserQuotaGrantsRecord>): Int

    @SelectProvider(type=SqlProviderAdapter::class, method="select")
    @ResultMap("UserQuotaGrantsRecordResult")
    fun selectOne(selectStatement: SelectStatementProvider): UserQuotaGrantsRecord?

    @SelectProvider(type=SqlProviderAdapter::class, method="select")
    @Results(id="UserQuotaGrantsRecordResult", value = [
        Result(column="id", property="id", jdbcType=JdbcType.BIGINT, id=true),
        Result(column="user_id", property="userId", jdbcType=JdbcType.BIGINT),
        Result(column="source_type", property="sourceType", jdbcType=JdbcType.VARCHAR),
        Result(column="source_user_id", property="sourceUserId", jdbcType=JdbcType.BIGINT),
        Result(column="source_grant_id", property="sourceGrantId", jdbcType=JdbcType.BIGINT),
        Result(column="granted_tokens", property="grantedTokens", jdbcType=JdbcType.BIGINT),
        Result(column="remaining_tokens", property="remainingTokens", jdbcType=JdbcType.BIGINT),
        Result(column="consumed_tokens", property="consumedTokens", jdbcType=JdbcType.BIGINT),
        Result(column="expired_tokens", property="expiredTokens", jdbcType=JdbcType.BIGINT),
        Result(column="expires_at", property="expiresAt", jdbcType=JdbcType.TIMESTAMP),
        Result(column="status", property="status", jdbcType=JdbcType.VARCHAR),
        Result(column="granted_by", property="grantedBy", jdbcType=JdbcType.BIGINT),
        Result(column="remark", property="remark", jdbcType=JdbcType.VARCHAR),
        Result(column="created_time", property="createdTime", jdbcType=JdbcType.TIMESTAMP),
        Result(column="updated_time", property="updatedTime", jdbcType=JdbcType.TIMESTAMP)
    ])
    fun selectMany(selectStatement: SelectStatementProvider): List<UserQuotaGrantsRecord>

    @UpdateProvider(type=SqlProviderAdapter::class, method="update")
    fun update(updateStatement: UpdateStatementProvider): Int

    @Update(
        """
        update user_quota_grants
        set status = case when remaining_tokens - #{deductTokens} = 0 then #{depletedStatus} else #{activeStatus} end,
            remaining_tokens = remaining_tokens - #{deductTokens},
            updated_time = #{updatedTime}
        where id = #{grantId}
          and user_id = #{userId}
          and status = #{activeStatus}
          and remaining_tokens >= #{deductTokens}
          and expires_at > #{now}
        """
    )
    fun deductRemainingTokens(
        @Param("grantId") grantId: Long,
        @Param("userId") userId: Long,
        @Param("deductTokens") deductTokens: Long,
        @Param("activeStatus") activeStatus: String,
        @Param("depletedStatus") depletedStatus: String,
        @Param("now") now: Date,
        @Param("updatedTime") updatedTime: Date,
    ): Int
}
