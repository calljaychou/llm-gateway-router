/*
 * Auto-generated file. Created by MyBatis Generator
 * Generation date: 2026-05-07T18:46:02.17076+08:00
 */
package com.llm.gateway.dal.mapper

import java.sql.JDBCType
import java.util.Date
import org.mybatis.dynamic.sql.SqlTable

object VendorsDynamicSqlSupport {
    object Vendors : SqlTable("vendors") {
        val id = column<Long>("id", JDBCType.BIGINT)

        val name = column<String>("name", JDBCType.VARCHAR)

        val baseUrl = column<String>("base_url", JDBCType.VARCHAR)

        val status = column<Int>("status", JDBCType.INTEGER)

        val createdTime = column<Date>("created_time", JDBCType.TIMESTAMP)

        val updatedTime = column<Date>("updated_time", JDBCType.TIMESTAMP)
    }
}