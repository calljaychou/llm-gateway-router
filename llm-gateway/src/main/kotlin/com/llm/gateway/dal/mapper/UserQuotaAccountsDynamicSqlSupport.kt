/*
 * Auto-generated file. Created by MyBatis Generator
 * Generation date: 2026-05-29T14:51:25.335813+08:00
 */
package com.llm.gateway.dal.mapper

import java.math.BigDecimal
import java.sql.JDBCType
import java.util.Date
import org.mybatis.dynamic.sql.SqlTable

object UserQuotaAccountsDynamicSqlSupport {
    object UserQuotaAccounts : SqlTable("user_quota_accounts") {
        val id = column<Long>("id", JDBCType.BIGINT)

        val userId = column<Long>("user_id", JDBCType.BIGINT)

        val currentQuotaAmount = column<BigDecimal>("current_quota_amount", JDBCType.DECIMAL)

        val usedAmount = column<BigDecimal>("used_amount", JDBCType.DECIMAL)

        val expiredAmount = column<BigDecimal>("expired_amount", JDBCType.DECIMAL)

        val transferredInAmount = column<BigDecimal>("transferred_in_amount", JDBCType.DECIMAL)

        val transferredOutAmount = column<BigDecimal>("transferred_out_amount", JDBCType.DECIMAL)

        val availableAmount = column<BigDecimal>("available_amount", JDBCType.DECIMAL)

        val allowTransferOut = column<Boolean>("allow_transfer_out", JDBCType.BIT)

        val earliestExpireAt = column<Date>("earliest_expire_at", JDBCType.TIMESTAMP)

        val updatedTime = column<Date>("updated_time", JDBCType.TIMESTAMP)

        val createdTime = column<Date>("created_time", JDBCType.TIMESTAMP)
    }
}