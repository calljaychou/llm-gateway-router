/*
 * Auto-generated file. Created by MyBatis Generator
 * Generation date: 2026-04-24T09:36:04.316+08:00
 */
package com.llm.gateway.dal.model

import java.util.Date

data class UsersRecord(
    var id: Long? = null,
    var deptId: Long? = null,
    var username: String? = null,
    var email: String? = null,
    var mobile: String? = null,
    var gender: Int? = null,
    var avatarUrl: String? = null,
    var password: String? = null,
    var passwordChanged: Boolean? = null,
    var remark: String? = null,
    var status: Int? = null,
    var delFlag: Boolean? = null,
    var createdTime: Date? = null,
    var updatedTime: Date? = null
)