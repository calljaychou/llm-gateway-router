package com.llm.gateway.common.enums

enum class DepartmentPermissionScope(val value: String) {
    SELF("SELF"),
    SUBTREE("SUBTREE");

    companion object {
        fun parse(raw: String): DepartmentPermissionScope? {
            val normalized = raw.trim().uppercase()
            return DepartmentPermissionScope.entries.firstOrNull { it.value == normalized }
        }
    }
}
