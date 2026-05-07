/*
 * Auto-generated file. Created by MyBatis Generator
 * Generation date: 2026-04-24T09:36:04.324+08:00
 */
package com.llm.gateway.dal.model

import java.util.Date

data class ApiKeysRecord(
    var id: Long? = null,
    var userId: Long? = null,
    var name: String? = null,
    var apiKeyHash: String? = null,
    var apiKeyPrefix: String? = null,
    var status: Int? = null,
    var expiresAt: Date? = null,
    var createdTime: Date? = null,
    var updatedTime: Date? = null
)