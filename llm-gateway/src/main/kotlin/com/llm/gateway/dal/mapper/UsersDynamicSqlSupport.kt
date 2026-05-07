/*
 * Auto-generated file. Created by MyBatis Generator
 * Generation date: 2026-04-24T09:36:04.318+08:00
 */
package com.llm.gateway.dal.mapper

import java.sql.JDBCType
import java.util.Date
import org.mybatis.dynamic.sql.SqlTable

object UsersDynamicSqlSupport {
    object Users : SqlTable("users") {
        val id = column<Long>("id", JDBCType.BIGINT)

        val deptId = column<Long>("dept_id", JDBCType.BIGINT)

        val username = column<String>("username", JDBCType.VARCHAR)

        val email = column<String>("email", JDBCType.VARCHAR)

        val mobile = column<String>("mobile", JDBCType.VARCHAR)

        val gender = column<Int>("gender", JDBCType.INTEGER)

        val avatarUrl = column<String>("avatar_url", JDBCType.VARCHAR)

        val password = column<String>("password", JDBCType.VARCHAR)

        val passwordChanged = column<Boolean>("password_changed", JDBCType.BIT)

        val remark = column<String>("remark", JDBCType.VARCHAR)

        val status = column<Int>("status", JDBCType.INTEGER)

        val delFlag = column<Boolean>("del_flag", JDBCType.BIT)

        val createdTime = column<Date>("created_time", JDBCType.TIMESTAMP)

        val updatedTime = column<Date>("updated_time", JDBCType.TIMESTAMP)
    }
}