package com.llm.gateway.model.results

import io.swagger.annotations.ApiModel
import io.swagger.annotations.ApiModelProperty

@ApiModel("创建供应商结果")
data class VendorCreateResult(
    @ApiModelProperty(value = "供应商ID", required = true)
    val vendorId: Long,
    @ApiModelProperty(value = "供应商名称", required = true)
    val name: String,
    @ApiModelProperty(value = "供应商Base URL", required = true)
    val baseUrl: String,
    @ApiModelProperty(value = "状态", required = true)
    val status: Int,
)

