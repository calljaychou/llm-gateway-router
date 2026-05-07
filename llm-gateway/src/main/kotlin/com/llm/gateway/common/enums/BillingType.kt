package com.llm.gateway.common.enums

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonValue

enum class BillingType(@get:JsonValue val value: String) {
    FREE("FREE"),
    PAID("PAID");

    companion object {
        @JvmStatic
        @JsonCreator
        fun from(value: String): BillingType {
            return BillingType.entries.firstOrNull {
                it.value.equals(value.trim(), ignoreCase = true)
            } ?: throw IllegalArgumentException("billingType仅支持 FREE 或 PAID")
        }
    }
}

