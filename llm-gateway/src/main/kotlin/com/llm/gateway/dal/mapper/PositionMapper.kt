/*
 * Auto-generated file. Created by MyBatis Generator
 * Generation date: 2026-05-07T18:46:02.168913+08:00
 */
package com.llm.gateway.dal.mapper

import com.llm.gateway.dal.model.PositionRecord
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
interface PositionMapper {
    @SelectProvider(type=SqlProviderAdapter::class, method="select")
    fun count(selectStatement: SelectStatementProvider): Long

    @DeleteProvider(type=SqlProviderAdapter::class, method="delete")
    fun delete(deleteStatement: DeleteStatementProvider): Int

    @InsertProvider(type=SqlProviderAdapter::class, method="insert")
    @Options(useGeneratedKeys=true,keyProperty="record.id")
    fun insert(insertStatement: InsertStatementProvider<PositionRecord>): Int

    @Insert(
        "\${insertStatement}"
    )
    @Options(useGeneratedKeys=true,keyProperty="records.id")
    fun insertMultiple(@Param("insertStatement") insertStatement: String, @Param("records") records: List<PositionRecord>): Int

    @SelectProvider(type=SqlProviderAdapter::class, method="select")
    @ResultMap("PositionRecordResult")
    fun selectOne(selectStatement: SelectStatementProvider): PositionRecord?

    @SelectProvider(type=SqlProviderAdapter::class, method="select")
    @Results(id="PositionRecordResult", value = [
        Result(column="id", property="id", jdbcType=JdbcType.BIGINT, id=true),
        Result(column="post_code", property="postCode", jdbcType=JdbcType.VARCHAR),
        Result(column="post_name", property="postName", jdbcType=JdbcType.VARCHAR),
        Result(column="post_sort", property="postSort", jdbcType=JdbcType.INTEGER),
        Result(column="status", property="status", jdbcType=JdbcType.INTEGER),
        Result(column="created_by", property="createdBy", jdbcType=JdbcType.VARCHAR),
        Result(column="updated_by", property="updatedBy", jdbcType=JdbcType.VARCHAR),
        Result(column="created_time", property="createdTime", jdbcType=JdbcType.TIMESTAMP),
        Result(column="updated_time", property="updatedTime", jdbcType=JdbcType.TIMESTAMP)
    ])
    fun selectMany(selectStatement: SelectStatementProvider): List<PositionRecord>

    @UpdateProvider(type=SqlProviderAdapter::class, method="update")
    fun update(updateStatement: UpdateStatementProvider): Int
}