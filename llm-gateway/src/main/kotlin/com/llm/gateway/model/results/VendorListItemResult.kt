package com.llm.gateway.model.results

import com.fasterxml.jackson.annotation.JsonFormat
import io.swagger.annotations.ApiModel
import io.swagger.annotations.ApiModelProperty
import java.util.Date

@ApiModel("供应商列表项")
data class VendorListItemResult(
    @ApiModelProperty(value = "供应商ID", required = true)
    val id: Long,
    @ApiModelProperty(value = "供应商名称", required = true)
    val name: String,
    @ApiModelProperty(value = "供应商Base URL", required = true)
    val baseUrl: String,
    @ApiModelProperty(value = "状态", required = true)
    val status: Int,
    @ApiModelProperty(value = "创建时间")
    @field:JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Shanghai")
    val createdTime: Date?,
)
