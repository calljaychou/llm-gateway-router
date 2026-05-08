/*
 * Auto-generated file. Created by MyBatis Generator
 * Generation date: 2026-05-08T17:25:34.951103+08:00
 */
package com.llm.gateway.dal.model

import java.util.Date

data class DepartmentModelPermissionsRecord(
    var id: Long? = null,
    var deptId: Long? = null,
    var modelId: Long? = null,
    var scope: String? = null,
    var status: Int? = null,
    var createdBy: String? = null,
    var createdTime: Date? = null,
    var updatedBy: String? = null,
    var updatedTime: Date? = null
)