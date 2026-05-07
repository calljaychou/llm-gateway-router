/*
 * Auto-generated file. Created by MyBatis Generator
 * Generation date: 2026-04-24T09:36:04.322+08:00
 */
package com.llm.gateway.dal.model

import java.util.Date

data class MasterKeysRecord(
    var id: Long? = null,
    var vendorId: Long? = null,
    var weight: Int? = null,
    var status: Int? = null,
    var errorCount: Int? = null,
    var lastCheckedAt: Date? = null,
    var createdTime: Date? = null,
    var updatedTime: Date? = null,
    var apiKeyEncrypted: String? = null
)