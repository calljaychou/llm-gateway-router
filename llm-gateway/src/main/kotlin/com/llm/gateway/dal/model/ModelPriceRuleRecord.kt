/*
 * Auto-generated file. Created by MyBatis Generator
 * Generation date: 2026-05-31T14:53:40.613314+08:00
 */
package com.llm.gateway.dal.model

import java.math.BigDecimal
import java.util.Date

data class ModelPriceRuleRecord(
    var id: Long? = null,
    var modelId: Long? = null,
    var vendorId: Long? = null,
    var chargeItem: String? = null,
    var priceCnyPerMillion: BigDecimal? = null,
    var currency: String? = null,
    var active: Boolean? = null,
    var createdAt: Date? = null,
    var updatedAt: Date? = null
)