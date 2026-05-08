package com.llm.gateway.common.enums

enum class DepartmentQuotaPeriod(val value: String) {
    MONTHLY("MONTHLY"),
    FOREVER("FOREVER");

    companion object {
        fun parse(raw: String): DepartmentQuotaPeriod? {
            val normalized = raw.trim().uppercase()
            return entries.firstOrNull { it.value == normalized }
        }
    }
}
