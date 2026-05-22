/*
 * Auto-generated file. Created by MyBatis Generator
 * Generation date: 2026-05-21T18:47:35.937605+08:00
 */
package com.llm.gateway.dal.model

import java.util.Date

data class UserQuotaGrantsRecord(
    var id: Long? = null,
    var userId: Long? = null,
    var sourceType: String? = null,
    var sourceUserId: Long? = null,
    var sourceGrantId: Long? = null,
    var grantedTokens: Long? = null,
    var remainingTokens: Long? = null,
    var consumedTokens: Long? = null,
    var expiredTokens: Long? = null,
    var expiresAt: Date? = null,
    var status: String? = null,
    var grantedBy: Long? = null,
    var remark: String? = null,
    var createdTime: Date? = null,
    var updatedTime: Date? = null
)