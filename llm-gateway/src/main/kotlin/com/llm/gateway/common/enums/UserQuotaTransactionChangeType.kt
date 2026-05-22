package com.llm.gateway.common.enums

enum class UserQuotaTransactionChangeType(val value: String) {
    ADMIN_GRANT("ADMIN_GRANT"),
    ADMIN_RECLAIM("ADMIN_RECLAIM"),
    TRANSFER_OUT("TRANSFER_OUT"),
    TRANSFER_IN("TRANSFER_IN"),
    USAGE_RESERVE("USAGE_RESERVE"),
    USAGE_SETTLE("USAGE_SETTLE"),
    USAGE_REFUND("USAGE_REFUND"),
    QUOTA_EXPIRE("QUOTA_EXPIRE");

    companion object {
        fun parse(raw: String?): UserQuotaTransactionChangeType? {
            val normalized = raw?.trim()?.uppercase() ?: return null
            return entries.firstOrNull { it.value == normalized }
        }
    }
}
