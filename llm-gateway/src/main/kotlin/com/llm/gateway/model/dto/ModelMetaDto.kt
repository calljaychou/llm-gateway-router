package com.llm.gateway.model.dto

data class ModelMetaDto(
    val modelAlias: String,
    val realModelName: String,
    val vendorId: Long,
    val vendorName: String,
    val billingType: String,
    val active: Boolean,
)
