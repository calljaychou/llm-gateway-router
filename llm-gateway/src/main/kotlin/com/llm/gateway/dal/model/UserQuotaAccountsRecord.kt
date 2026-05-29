/*
 * Auto-generated file. Created by MyBatis Generator
 * Generation date: 2026-05-29T14:51:25.334098+08:00
 */
package com.llm.gateway.dal.model

import java.math.BigDecimal
import java.util.Date

data class UserQuotaAccountsRecord(
    var id: Long? = null,
    var userId: Long? = null,
    var currentQuotaAmount: BigDecimal? = null,
    var usedAmount: BigDecimal? = null,
    var expiredAmount: BigDecimal? = null,
    var transferredInAmount: BigDecimal? = null,
    var transferredOutAmount: BigDecimal? = null,
    var availableAmount: BigDecimal? = null,
    var allowTransferOut: Boolean? = null,
    var earliestExpireAt: Date? = null,
    var updatedTime: Date? = null,
    var createdTime: Date? = null
)