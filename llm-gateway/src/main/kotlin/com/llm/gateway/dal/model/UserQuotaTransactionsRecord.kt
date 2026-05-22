/*
 * Auto-generated file. Created by MyBatis Generator
 * Generation date: 2026-05-21T18:47:35.938532+08:00
 */
package com.llm.gateway.dal.model

import java.util.Date

data class UserQuotaTransactionsRecord(
    var id: Long? = null,
    var bizNo: String? = null,
    var userId: Long? = null,
    var grantId: Long? = null,
    var changeType: String? = null,
    var deltaTokens: Long? = null,
    var quotaBefore: Long? = null,
    var quotaAfter: Long? = null,
    var availableBefore: Long? = null,
    var availableAfter: Long? = null,
    var counterpartyUserId: Long? = null,
    var requestId: String? = null,
    var operatorUserId: Long? = null,
    var remark: String? = null,
    var createdTime: Date? = null
)