/*
 * Auto-generated file. Created by MyBatis Generator
 * Generation date: 2026-05-29T14:51:25.344142+08:00
 */
package com.llm.gateway.dal.model

import java.math.BigDecimal
import java.util.Date

data class UserQuotaGrantsRecord(
    var id: Long? = null,
    var userId: Long? = null,
    var sourceType: String? = null,
    var sourceUserId: Long? = null,
    var sourceGrantId: Long? = null,
    var grantedAmount: BigDecimal? = null,
    var remainingAmount: BigDecimal? = null,
    var consumedAmount: BigDecimal? = null,
    var expiredAmount: BigDecimal? = null,
    var expiresAt: Date? = null,
    var status: String? = null,
    var grantedBy: Long? = null,
    var remark: String? = null,
    var createdTime: Date? = null,
    var updatedTime: Date? = null
)