/*
 * Auto-generated file. Created by MyBatis Generator
 * Generation date: 2026-05-29T14:51:25.345095+08:00
 */
package com.llm.gateway.dal.model

import java.math.BigDecimal
import java.util.Date

data class UserQuotaTransactionsRecord(
    var id: Long? = null,
    var bizNo: String? = null,
    var userId: Long? = null,
    var grantId: Long? = null,
    var changeType: String? = null,
    var deltaAmount: BigDecimal? = null,
    var quotaBeforeAmount: BigDecimal? = null,
    var quotaAfterAmount: BigDecimal? = null,
    var availableBeforeAmount: BigDecimal? = null,
    var availableAfterAmount: BigDecimal? = null,
    var counterpartyUserId: Long? = null,
    var requestId: String? = null,
    var operatorUserId: Long? = null,
    var remark: String? = null,
    var createdTime: Date? = null
)