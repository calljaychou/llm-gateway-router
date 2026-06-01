/*
 * Auto-generated file. Created by MyBatis Generator
 * Generation date: 2026-05-31T14:53:40.612018+08:00
 */
package com.llm.gateway.dal.model

import java.math.BigDecimal
import java.util.Date

data class LlmUsageBillingDetailRecord(
    var id: Long? = null,
    var usageLogId: Long? = null,
    var requestId: String? = null,
    var chargeItem: String? = null,
    var tokenDirection: String? = null,
    var tokenType: String? = null,
    var cacheType: String? = null,
    var tokens: Int? = null,
    var priceCnyPerMillion: BigDecimal? = null,
    var amountCny: BigDecimal? = null,
    var pricingRule: String? = null,
    var createdAt: Date? = null
)