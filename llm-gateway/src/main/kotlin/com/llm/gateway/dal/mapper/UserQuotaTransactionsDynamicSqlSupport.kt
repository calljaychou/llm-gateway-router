/*
 * Auto-generated file. Created by MyBatis Generator
 * Generation date: 2026-05-21T18:47:35.939453+08:00
 */
package com.llm.gateway.dal.mapper

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

        val deltaTokens = column<Long>("delta_tokens", JDBCType.BIGINT)

        val quotaBefore = column<Long>("quota_before", JDBCType.BIGINT)

        val quotaAfter = column<Long>("quota_after", JDBCType.BIGINT)

        val availableBefore = column<Long>("available_before", JDBCType.BIGINT)

        val availableAfter = column<Long>("available_after", JDBCType.BIGINT)

        val counterpartyUserId = column<Long>("counterparty_user_id", JDBCType.BIGINT)

        val requestId = column<String>("request_id", JDBCType.VARCHAR)

        val operatorUserId = column<Long>("operator_user_id", JDBCType.BIGINT)

        val remark = column<String>("remark", JDBCType.VARCHAR)

        val createdTime = column<Date>("created_time", JDBCType.TIMESTAMP)
    }
}