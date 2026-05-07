/*
 * Auto-generated file. Created by MyBatis Generator
 * Generation date: 2026-05-07T18:46:02.173385+08:00
 */
package com.llm.gateway.dal.mapper

import com.llm.gateway.dal.model.DepartmentQuotasRecord
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
interface DepartmentQuotasMapper {
    @SelectProvider(type=SqlProviderAdapter::class, method="select")
    fun count(selectStatement: SelectStatementProvider): Long

    @DeleteProvider(type=SqlProviderAdapter::class, method="delete")
    fun delete(deleteStatement: DeleteStatementProvider): Int

    @InsertProvider(type=SqlProviderAdapter::class, method="insert")
    @Options(useGeneratedKeys=true,keyProperty="record.id")
    fun insert(insertStatement: InsertStatementProvider<DepartmentQuotasRecord>): Int

    @Insert(
        "\${insertStatement}"
    )
    @Options(useGeneratedKeys=true,keyProperty="records.id")
    fun insertMultiple(@Param("insertStatement") insertStatement: String, @Param("records") records: List<DepartmentQuotasRecord>): Int

    @SelectProvider(type=SqlProviderAdapter::class, method="select")
    @ResultMap("DepartmentQuotasRecordResult")
    fun selectOne(selectStatement: SelectStatementProvider): DepartmentQuotasRecord?

    @SelectProvider(type=SqlProviderAdapter::class, method="select")
    @Results(id="DepartmentQuotasRecordResult", value = [
        Result(column="id", property="id", jdbcType=JdbcType.BIGINT, id=true),
        Result(column="dept_id", property="deptId", jdbcType=JdbcType.BIGINT),
        Result(column="total_tokens", property="totalTokens", jdbcType=JdbcType.BIGINT),
        Result(column="used_tokens", property="usedTokens", jdbcType=JdbcType.BIGINT),
        Result(column="period", property="period", jdbcType=JdbcType.CHAR),
        Result(column="last_reset_at", property="lastResetAt", jdbcType=JdbcType.TIMESTAMP),
        Result(column="created_at", property="createdAt", jdbcType=JdbcType.TIMESTAMP),
        Result(column="updated_at", property="updatedAt", jdbcType=JdbcType.TIMESTAMP)
    ])
    fun selectMany(selectStatement: SelectStatementProvider): List<DepartmentQuotasRecord>

    @UpdateProvider(type=SqlProviderAdapter::class, method="update")
    fun update(updateStatement: UpdateStatementProvider): Int
}