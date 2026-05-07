/*
 * Auto-generated file. Created by MyBatis Generator
 * Generation date: 2026-05-07T18:46:02.168795+08:00
 */
package com.llm.gateway.dal.mapper

import java.sql.JDBCType
import java.util.Date
import org.mybatis.dynamic.sql.SqlTable

object PositionDynamicSqlSupport {
    object Position : SqlTable("position") {
        val id = column<Long>("id", JDBCType.BIGINT)

        val postCode = column<String>("post_code", JDBCType.VARCHAR)

        val postName = column<String>("post_name", JDBCType.VARCHAR)

        val postSort = column<Int>("post_sort", JDBCType.INTEGER)

        val status = column<Int>("status", JDBCType.INTEGER)

        val createdBy = column<String>("created_by", JDBCType.VARCHAR)

        val updatedBy = column<String>("updated_by", JDBCType.VARCHAR)

        val createdTime = column<Date>("created_time", JDBCType.TIMESTAMP)

        val updatedTime = column<Date>("updated_time", JDBCType.TIMESTAMP)
    }
}