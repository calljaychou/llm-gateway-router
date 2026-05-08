/*
 * Auto-generated file. Created by MyBatis Generator
 * Generation date: 2026-05-08T17:25:34.94845+08:00
 */
package com.llm.gateway.dal.mapper

import java.sql.JDBCType
import org.mybatis.dynamic.sql.SqlTable

object UserPostRelDynamicSqlSupport {
    object UserPostRel : SqlTable("user_post_rel") {
        val userId = column<Long>("user_id", JDBCType.BIGINT)

        val postId = column<Long>("post_id", JDBCType.BIGINT)
    }
}