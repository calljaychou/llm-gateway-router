package com.llm.gateway.common.enums

enum class TokenCalcSource(val value: String) {
    UPSTREAM("UPSTREAM"),
    STREAM("STREAM"),
    LOCAL_ESTIMATE("LOCAL_ESTIMATE"),
}
