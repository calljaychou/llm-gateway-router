/*
 * Auto-generated file. Created by MyBatis Generator
 * Generation date: 2026-05-29T14:51:25.344284+08:00
 */
package com.llm.gateway.dal.mapper

import java.math.BigDecimal
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

        val grantedAmount = column<BigDecimal>("granted_amount", JDBCType.DECIMAL)

        val remainingAmount = column<BigDecimal>("remaining_amount", JDBCType.DECIMAL)

        val consumedAmount = column<BigDecimal>("consumed_amount", JDBCType.DECIMAL)

        val expiredAmount = column<BigDecimal>("expired_amount", JDBCType.DECIMAL)

        val expiresAt = column<Date>("expires_at", JDBCType.TIMESTAMP)

        val status = column<String>("status", JDBCType.VARCHAR)

        val grantedBy = column<Long>("granted_by", JDBCType.BIGINT)

        val remark = column<String>("remark", JDBCType.VARCHAR)

        val createdTime = column<Date>("created_time", JDBCType.TIMESTAMP)

        val updatedTime = column<Date>("updated_time", JDBCType.TIMESTAMP)
    }
}