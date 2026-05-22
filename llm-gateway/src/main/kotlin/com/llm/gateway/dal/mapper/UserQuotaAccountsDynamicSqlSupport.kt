/*
 * Auto-generated file. Created by MyBatis Generator
 * Generation date: 2026-05-21T18:47:35.930903+08:00
 */
package com.llm.gateway.dal.mapper

import java.sql.JDBCType
import java.util.Date
import org.mybatis.dynamic.sql.SqlTable

object UserQuotaAccountsDynamicSqlSupport {
    object UserQuotaAccounts : SqlTable("user_quota_accounts") {
        val id = column<Long>("id", JDBCType.BIGINT)

        val userId = column<Long>("user_id", JDBCType.BIGINT)

        val currentQuotaTokens = column<Long>("current_quota_tokens", JDBCType.BIGINT)

        val usedTokens = column<Long>("used_tokens", JDBCType.BIGINT)

        val expiredTokens = column<Long>("expired_tokens", JDBCType.BIGINT)

        val transferredInTokens = column<Long>("transferred_in_tokens", JDBCType.BIGINT)

        val transferredOutTokens = column<Long>("transferred_out_tokens", JDBCType.BIGINT)

        val availableTokens = column<Long>("available_tokens", JDBCType.BIGINT)

        val allowTransferOut = column<Boolean>("allow_transfer_out", JDBCType.BIT)

        val earliestExpireAt = column<Date>("earliest_expire_at", JDBCType.TIMESTAMP)

        val updatedTime = column<Date>("updated_time", JDBCType.TIMESTAMP)

        val createdTime = column<Date>("created_time", JDBCType.TIMESTAMP)
    }
}