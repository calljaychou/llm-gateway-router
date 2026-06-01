/*
 * Auto-generated file. Created by MyBatis Generator
 * Generation date: 2026-05-31T14:53:40.613432+08:00
 */
package com.llm.gateway.dal.mapper

import java.math.BigDecimal
import java.sql.JDBCType
import java.util.Date
import org.mybatis.dynamic.sql.SqlTable

object ModelPriceRuleDynamicSqlSupport {
    object ModelPriceRule : SqlTable("model_price_rule") {
        val id = column<Long>("id", JDBCType.BIGINT)

        val modelId = column<Long>("model_id", JDBCType.BIGINT)

        val vendorId = column<Long>("vendor_id", JDBCType.BIGINT)

        val chargeItem = column<String>("charge_item", JDBCType.VARCHAR)

        val priceCnyPerMillion = column<BigDecimal>("price_cny_per_million", JDBCType.DECIMAL)

        val currency = column<String>("currency", JDBCType.VARCHAR)

        val active = column<Boolean>("active", JDBCType.BIT)

        val createdAt = column<Date>("created_at", JDBCType.TIMESTAMP)

        val updatedAt = column<Date>("updated_at", JDBCType.TIMESTAMP)
    }
}