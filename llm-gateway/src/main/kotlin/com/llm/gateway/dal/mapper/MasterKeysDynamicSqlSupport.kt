/*
 * Auto-generated file. Created by MyBatis Generator
 * Generation date: 2026-04-24T09:36:04.322+08:00
 */
package com.llm.gateway.dal.mapper

import java.sql.JDBCType
import java.util.Date
import org.mybatis.dynamic.sql.SqlTable

object MasterKeysDynamicSqlSupport {
    object MasterKeys : SqlTable("master_keys") {
        val id = column<Long>("id", JDBCType.BIGINT)

        val vendorId = column<Long>("vendor_id", JDBCType.BIGINT)

        val weight = column<Int>("weight", JDBCType.INTEGER)

        val status = column<Int>("status", JDBCType.INTEGER)

        val errorCount = column<Int>("error_count", JDBCType.INTEGER)

        val lastCheckedAt = column<Date>("last_checked_at", JDBCType.TIMESTAMP)

        val createdTime = column<Date>("created_time", JDBCType.TIMESTAMP)

        val updatedTime = column<Date>("updated_time", JDBCType.TIMESTAMP)

        val apiKeyEncrypted = column<String>("api_key_encrypted", JDBCType.LONGVARCHAR)
    }
}