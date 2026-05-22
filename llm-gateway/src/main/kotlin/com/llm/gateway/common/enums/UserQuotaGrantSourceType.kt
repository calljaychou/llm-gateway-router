package com.llm.gateway.common.enums

enum class UserQuotaGrantSourceType(val value: String) {
    ADMIN_GRANT("ADMIN_GRANT"),
    TRANSFER_IN("TRANSFER_IN"),
    COMPENSATE("COMPENSATE");

    companion object {
        fun parse(raw: String?): UserQuotaGrantSourceType? {
            val normalized = raw?.trim()?.uppercase() ?: return null
            return entries.firstOrNull { it.value == normalized }
        }
    }
}
