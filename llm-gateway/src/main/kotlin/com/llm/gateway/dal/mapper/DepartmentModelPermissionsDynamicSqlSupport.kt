/*
 * Auto-generated file. Created by MyBatis Generator
 * Generation date: 2026-05-08T17:25:34.951168+08:00
 */
package com.llm.gateway.dal.mapper

import java.sql.JDBCType
import java.util.Date
import org.mybatis.dynamic.sql.SqlTable

object DepartmentModelPermissionsDynamicSqlSupport {
    object DepartmentModelPermissions : SqlTable("department_model_permissions") {
        val id = column<Long>("id", JDBCType.BIGINT)

        val deptId = column<Long>("dept_id", JDBCType.BIGINT)

        val modelId = column<Long>("model_id", JDBCType.BIGINT)

        val scope = column<String>("scope", JDBCType.CHAR)

        val status = column<Int>("status", JDBCType.INTEGER)

        val createdBy = column<String>("created_by", JDBCType.VARCHAR)

        val createdTime = column<Date>("created_time", JDBCType.TIMESTAMP)

        val updatedBy = column<String>("updated_by", JDBCType.VARCHAR)

        val updatedTime = column<Date>("updated_time", JDBCType.TIMESTAMP)
    }
}