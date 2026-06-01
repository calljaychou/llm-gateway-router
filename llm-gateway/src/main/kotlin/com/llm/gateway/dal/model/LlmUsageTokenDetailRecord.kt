/*
 * Auto-generated file. Created by MyBatis Generator
 * Generation date: 2026-05-31T14:53:40.610732+08:00
 */
package com.llm.gateway.dal.model

import java.util.Date

data class LlmUsageTokenDetailRecord(
    var id: Long? = null,
    var usageLogId: Long? = null,
    var requestId: String? = null,
    var tokenDirection: String? = null,
    var tokenType: String? = null,
    var cacheType: String? = null,
    var tokens: Int? = null,
    var billableTokens: Int? = null,
    var source: String? = null,
    var providerField: String? = null,
    var note: String? = null,
    var createdAt: Date? = null
)