/*
 * Auto-generated file. Created by MyBatis Generator
 * Generation date: 2026-05-29T14:51:25.345226+08:00
 */
package com.llm.gateway.dal.mapper

import java.math.BigDecimal
import java.sql.JDBCType
import java.util.Date
import org.mybatis.dynamic.sql.SqlTable

object UserQuotaTransactionsDynamicSqlSupport {
    object UserQuotaTransactions : SqlTable("user_quota_transactions") {
        val id = column<Long>("id", JDBCType.BIGINT)

        val bizNo = column<String>("biz_no", JDBCType.VARCHAR)

        val userId = column<Long>("user_id", JDBCType.BIGINT)

        val grantId = column<Long>("grant_id", JDBCType.BIGINT)

        val changeType = column<String>("change_type", JDBCType.VARCHAR)

        val deltaAmount = column<BigDecimal>("delta_amount", JDBCType.DECIMAL)

        val quotaBeforeAmount = column<BigDecimal>("quota_before_amount", JDBCType.DECIMAL)

        val quotaAfterAmount = column<BigDecimal>("quota_after_amount", JDBCType.DECIMAL)

        val availableBeforeAmount = column<BigDecimal>("available_before_amount", JDBCType.DECIMAL)

        val availableAfterAmount = column<BigDecimal>("available_after_amount", JDBCType.DECIMAL)

        val counterpartyUserId = column<Long>("counterparty_user_id", JDBCType.BIGINT)

        val requestId = column<String>("request_id", JDBCType.VARCHAR)

        val operatorUserId = column<Long>("operator_user_id", JDBCType.BIGINT)

        val remark = column<String>("remark", JDBCType.VARCHAR)

        val createdTime = column<Date>("created_time", JDBCType.TIMESTAMP)
    }
}