package com.llm.gateway.common.enums

enum class UserQuotaGrantStatus(val value: String) {
    ACTIVE("ACTIVE"),
    DEPLETED("DEPLETED"),
    EXPIRED("EXPIRED");

    companion object {
        fun parse(raw: String?): UserQuotaGrantStatus? {
            val normalized = raw?.trim()?.uppercase() ?: return null
            return entries.firstOrNull { it.value == normalized }
        }
    }
}
