/*
 * Auto-generated file. Created by MyBatis Generator
 * Generation date: 2026-05-08T17:25:34.95281+08:00
 */
package com.llm.gateway.dal.model

import java.util.Date

data class UsageLogsRecord(
    var id: Long? = null,
    var requestId: String? = null,
    var userId: Long? = null,
    var deptId: Long? = null,
    var apiKeyId: Long? = null,
    var vendorId: Long? = null,
    var modelId: Long? = null,
    var endpoint: String? = null,
    var isStream: Boolean? = null,
    var promptTokens: Int? = null,
    var completionTokens: Int? = null,
    var totalTokens: Int? = null,
    var latencyMs: Int? = null,
    var statusCode: Int? = null,
    var errorCode: String? = null,
    var createdAt: Date? = null
)