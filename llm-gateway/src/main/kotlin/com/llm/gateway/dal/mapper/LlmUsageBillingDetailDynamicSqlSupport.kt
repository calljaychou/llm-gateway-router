/*
 * Auto-generated file. Created by MyBatis Generator
 * Generation date: 2026-05-31T14:53:40.612263+08:00
 */
package com.llm.gateway.dal.mapper

import java.math.BigDecimal
import java.sql.JDBCType
import java.util.Date
import org.mybatis.dynamic.sql.SqlTable

object LlmUsageBillingDetailDynamicSqlSupport {
    object LlmUsageBillingDetail : SqlTable("llm_usage_billing_detail") {
        val id = column<Long>("id", JDBCType.BIGINT)

        val usageLogId = column<Long>("usage_log_id", JDBCType.BIGINT)

        val requestId = column<String>("request_id", JDBCType.VARCHAR)

        val chargeItem = column<String>("charge_item", JDBCType.VARCHAR)

        val tokenDirection = column<String>("token_direction", JDBCType.VARCHAR)

        val tokenType = column<String>("token_type", JDBCType.VARCHAR)

        val cacheType = column<String>("cache_type", JDBCType.VARCHAR)

        val tokens = column<Int>("tokens", JDBCType.INTEGER)

        val priceCnyPerMillion = column<BigDecimal>("price_cny_per_million", JDBCType.DECIMAL)

        val amountCny = column<BigDecimal>("amount_cny", JDBCType.DECIMAL)

        val pricingRule = column<String>("pricing_rule", JDBCType.VARCHAR)

        val createdAt = column<Date>("created_at", JDBCType.TIMESTAMP)
    }
}