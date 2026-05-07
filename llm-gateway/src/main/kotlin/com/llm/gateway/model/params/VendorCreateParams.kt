package com.llm.gateway.model.params

import io.swagger.annotations.ApiModel
import io.swagger.annotations.ApiModelProperty
import javax.validation.constraints.Max
import javax.validation.constraints.Min
import javax.validation.constraints.NotBlank

@ApiModel("创建供应商参数")
data class VendorCreateParams(
    @ApiModelProperty(value = "供应商名称", required = true, example = "OpenAI")
    @field:NotBlank(message = "供应商名称不能为空")
    val name: String,
    @ApiModelProperty(value = "供应商 Base URL", required = true, example = "https://api.openai.com")
    @field:NotBlank(message = "供应商Base URL不能为空")
    val baseUrl: String,
    @ApiModelProperty(value = "状态(1-正常,2-停用)", example = "1")
    @field:Min(value = 1, message = "状态值最小为1")
    @field:Max(value = 2, message = "状态值最大为2")
    val status: Int? = null,
)
