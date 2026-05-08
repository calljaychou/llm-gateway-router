/*
 * Auto-generated file. Created by MyBatis Generator
 * Generation date: 2026-05-08T17:25:34.948886+08:00
 */
package com.llm.gateway.dal.mapper

import java.sql.JDBCType
import org.mybatis.dynamic.sql.SqlTable

object UserRoleRelDynamicSqlSupport {
    object UserRoleRel : SqlTable("user_role_rel") {
        val userId = column<Long>("user_id", JDBCType.BIGINT)

        val roleId = column<Long>("role_id", JDBCType.BIGINT)
    }
}