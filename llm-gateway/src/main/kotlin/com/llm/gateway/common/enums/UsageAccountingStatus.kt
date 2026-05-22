package com.llm.gateway.common.enums

enum class UsageAccountingStatus(val value: String) {
    PROCESSING("PROCESSING"),
    SUCCEEDED("SUCCEEDED"),
    FAILED("FAILED"),
    COMPENSATED("COMPENSATED"),
}
