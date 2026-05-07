/*
 * Auto-generated file. Created by MyBatis Generator
 * Generation date: 2026-04-24T09:36:04.324+08:00
 */
package com.llm.gateway.dal.mapper

import java.sql.JDBCType
import java.util.Date
import org.mybatis.dynamic.sql.SqlTable

object ApiKeysDynamicSqlSupport {
    object ApiKeys : SqlTable("api_keys") {
        val id = column<Long>("id", JDBCType.BIGINT)

        val userId = column<Long>("user_id", JDBCType.BIGINT)

        val name = column<String>("name", JDBCType.VARCHAR)

        val apiKeyHash = column<String>("api_key_hash", JDBCType.VARCHAR)

        val apiKeyPrefix = column<String>("api_key_prefix", JDBCType.VARCHAR)

        val status = column<Int>("status", JDBCType.INTEGER)

        val expiresAt = column<Date>("expires_at", JDBCType.TIMESTAMP)

        val createdTime = column<Date>("created_time", JDBCType.TIMESTAMP)

        val updatedTime = column<Date>("updated_time", JDBCType.TIMESTAMP)
    }
}