/*
 * Auto-generated file. Created by MyBatis Generator
 * Generation date: 2026-04-24T09:36:04.323+08:00
 */
package com.llm.gateway.dal.mapper

import java.sql.JDBCType
import java.util.Date
import org.mybatis.dynamic.sql.SqlTable

object ModelsDynamicSqlSupport {
    object Models : SqlTable("models") {
        val id = column<Long>("id", JDBCType.BIGINT)

        val modelAlias = column<String>("model_alias", JDBCType.VARCHAR)

        val realModelName = column<String>("real_model_name", JDBCType.VARCHAR)

        val vendorId = column<Long>("vendor_id", JDBCType.BIGINT)

        val billingType = column<String>("billing_type", JDBCType.CHAR)

        val active = column<Boolean>("active", JDBCType.BIT)

        val createdTime = column<Date>("created_time", JDBCType.TIMESTAMP)

        val updatedTime = column<Date>("updated_time", JDBCType.TIMESTAMP)
    }
}