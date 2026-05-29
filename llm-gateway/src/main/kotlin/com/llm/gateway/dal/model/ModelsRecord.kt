/*
 * Auto-generated file. Created by MyBatis Generator
 * Generation date: 2026-05-08T17:25:34.950559+08:00
 */
package com.llm.gateway.dal.model

import java.math.BigDecimal
import java.util.Date

data class ModelsRecord(
    var id: Long? = null,
    var modelAlias: String? = null,
    var realModelName: String? = null,
    var vendorId: Long? = null,
    var billingType: String? = null,
    var inputPriceCnyPerMillion: BigDecimal? = null,
    var outputPriceCnyPerMillion: BigDecimal? = null,
    var active: Boolean? = null,
    var createdTime: Date? = null,
    var updatedTime: Date? = null
)
