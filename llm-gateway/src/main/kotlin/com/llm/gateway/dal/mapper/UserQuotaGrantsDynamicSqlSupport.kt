/*
 * Auto-generated file. Created by MyBatis Generator
 * Generation date: 2026-05-21T18:47:35.937726+08:00
 */
package com.llm.gateway.dal.mapper

import java.sql.JDBCType
import java.util.Date
import org.mybatis.dynamic.sql.SqlTable

object UserQuotaGrantsDynamicSqlSupport {
    object UserQuotaGrants : SqlTable("user_quota_grants") {
        val id = column<Long>("id", JDBCType.BIGINT)

        val userId = column<Long>("user_id", JDBCType.BIGINT)

        val sourceType = column<String>("source_type", JDBCType.VARCHAR)

        val sourceUserId = column<Long>("source_user_id", JDBCType.BIGINT)

        val sourceGrantId = column<Long>("source_grant_id", JDBCType.BIGINT)

        val grantedTokens = column<Long>("granted_tokens", JDBCType.BIGINT)

        val remainingTokens = column<Long>("remaining_tokens", JDBCType.BIGINT)

        val consumedTokens = column<Long>("consumed_tokens", JDBCType.BIGINT)

        val expiredTokens = column<Long>("expired_tokens", JDBCType.BIGINT)

        val expiresAt = column<Date>("expires_at", JDBCType.TIMESTAMP)

        val status = column<String>("status", JDBCType.VARCHAR)

        val grantedBy = column<Long>("granted_by", JDBCType.BIGINT)

        val remark = column<String>("remark", JDBCType.VARCHAR)

        val createdTime = column<Date>("created_time", JDBCType.TIMESTAMP)

        val updatedTime = column<Date>("updated_time", JDBCType.TIMESTAMP)
    }
}