/*
 * Auto-generated file. Created by MyBatis Generator
 * Generation date: 2026-04-24T09:36:04.293+08:00
 */
package com.llm.gateway.dal.model

import java.util.Date

data class DepartmentRecord(
    var id: Long? = null,
    var parentId: Long? = null,
    var deptName: String? = null,
    var orderNum: Int? = null,
    var leader: String? = null,
    var tel: String? = null,
    var status: Int? = null,
    var delFlag: Boolean? = null,
    var createdTime: Date? = null,
    var updatedTime: Date? = null
)