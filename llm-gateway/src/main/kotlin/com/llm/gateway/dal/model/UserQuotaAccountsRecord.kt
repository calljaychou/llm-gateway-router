/*
 * Auto-generated file. Created by MyBatis Generator
 * Generation date: 2026-05-21T18:47:35.929425+08:00
 */
package com.llm.gateway.dal.model

import java.util.Date

data class UserQuotaAccountsRecord(
    var id: Long? = null,
    var userId: Long? = null,
    var currentQuotaTokens: Long? = null,
    var usedTokens: Long? = null,
    var expiredTokens: Long? = null,
    var transferredInTokens: Long? = null,
    var transferredOutTokens: Long? = null,
    var availableTokens: Long? = null,
    var allowTransferOut: Boolean? = null,
    var earliestExpireAt: Date? = null,
    var updatedTime: Date? = null,
    var createdTime: Date? = null
)