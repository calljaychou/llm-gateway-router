/*
 * Auto-generated file. Created by MyBatis Generator
 * Generation date: 2026-05-08T17:25:34.939344+08:00
 */
package com.llm.gateway.dal.mapper

import java.sql.JDBCType
import java.util.Date
import org.mybatis.dynamic.sql.SqlTable

object DepartmentDynamicSqlSupport {
    object Department : SqlTable("department") {
        val id = column<Long>("id", JDBCType.BIGINT)

        val parentId = column<Long>("parent_id", JDBCType.BIGINT)

        val deptName = column<String>("dept_name", JDBCType.VARCHAR)

        val orderNum = column<Int>("order_num", JDBCType.INTEGER)

        val leaderUserId = column<Long>("leader_user_id", JDBCType.BIGINT)

        val tel = column<String>("tel", JDBCType.VARCHAR)

        val status = column<Int>("status", JDBCType.INTEGER)

        val delFlag = column<Boolean>("del_flag", JDBCType.BIT)

        val createdTime = column<Date>("created_time", JDBCType.TIMESTAMP)

        val updatedTime = column<Date>("updated_time", JDBCType.TIMESTAMP)
    }
}