/*
 * Auto-generated file. Created by MyBatis Generator
 * Generation date: 2026-04-24T09:36:04.318+08:00
 */
package com.llm.gateway.dal.mapper

import com.llm.gateway.dal.model.UsersRecord
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
interface UsersMapper {
    @SelectProvider(type=SqlProviderAdapter::class, method="select")
    fun count(selectStatement: SelectStatementProvider): Long

    @DeleteProvider(type=SqlProviderAdapter::class, method="delete")
    fun delete(deleteStatement: DeleteStatementProvider): Int

    @InsertProvider(type=SqlProviderAdapter::class, method="insert")
    @Options(useGeneratedKeys=true,keyProperty="record.id")
    fun insert(insertStatement: InsertStatementProvider<UsersRecord>): Int

    @Insert(
        "\${insertStatement}"
    )
    @Options(useGeneratedKeys=true,keyProperty="records.id")
    fun insertMultiple(@Param("insertStatement") insertStatement: String, @Param("records") records: List<UsersRecord>): Int

    @SelectProvider(type=SqlProviderAdapter::class, method="select")
    @ResultMap("UsersRecordResult")
    fun selectOne(selectStatement: SelectStatementProvider): UsersRecord?

    @SelectProvider(type=SqlProviderAdapter::class, method="select")
    @Results(id="UsersRecordResult", value = [
        Result(column="id", property="id", jdbcType=JdbcType.BIGINT, id=true),
        Result(column="dept_id", property="deptId", jdbcType=JdbcType.BIGINT),
        Result(column="username", property="username", jdbcType=JdbcType.VARCHAR),
        Result(column="email", property="email", jdbcType=JdbcType.VARCHAR),
        Result(column="mobile", property="mobile", jdbcType=JdbcType.VARCHAR),
        Result(column="gender", property="gender", jdbcType=JdbcType.INTEGER),
        Result(column="avatar_url", property="avatarUrl", jdbcType=JdbcType.VARCHAR),
        Result(column="password", property="password", jdbcType=JdbcType.VARCHAR),
        Result(column="password_changed", property="passwordChanged", jdbcType=JdbcType.BIT),
        Result(column="remark", property="remark", jdbcType=JdbcType.VARCHAR),
        Result(column="status", property="status", jdbcType=JdbcType.INTEGER),
        Result(column="del_flag", property="delFlag", jdbcType=JdbcType.BIT),
        Result(column="created_time", property="createdTime", jdbcType=JdbcType.TIMESTAMP),
        Result(column="updated_time", property="updatedTime", jdbcType=JdbcType.TIMESTAMP)
    ])
    fun selectMany(selectStatement: SelectStatementProvider): List<UsersRecord>

    @UpdateProvider(type=SqlProviderAdapter::class, method="update")
    fun update(updateStatement: UpdateStatementProvider): Int
}