package com.llm.gateway.model.dto

data class ModelMetaDto(
    val modelAlias: String,
    val realModelName: String,
    val vendorId: Long,
    val billingType: String,
    val active: Boolean,
)
