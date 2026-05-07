/*
 * Auto-generated file. Created by MyBatis Generator
 * Generation date: 2026-04-24T09:36:04.32+08:00
 */
package com.llm.gateway.dal.mapper

import java.sql.JDBCType
import java.util.Date
import org.mybatis.dynamic.sql.SqlTable

object RolesDynamicSqlSupport {
    object Roles : SqlTable("roles") {
        val id = column<Long>("id", JDBCType.BIGINT)

        val roleName = column<String>("role_name", JDBCType.VARCHAR)

        val roleKey = column<String>("role_key", JDBCType.VARCHAR)

        val roleSort = column<Int>("role_sort", JDBCType.INTEGER)

        val createdBy = column<String>("created_by", JDBCType.VARCHAR)

        val createdTime = column<Date>("created_time", JDBCType.TIMESTAMP)

        val updatedBy = column<String>("updated_by", JDBCType.VARCHAR)

        val updatedTime = column<Date>("updated_time", JDBCType.TIMESTAMP)
    }
}