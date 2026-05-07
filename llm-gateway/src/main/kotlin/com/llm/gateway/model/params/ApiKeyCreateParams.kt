package com.llm.gateway.model.params

import io.swagger.annotations.ApiModel
import io.swagger.annotations.ApiModelProperty
import javax.validation.constraints.NotBlank
import javax.validation.constraints.Size

@ApiModel("创建虚拟密钥参数")
data class ApiKeyCreateParams(
    @ApiModelProperty(value = "密钥名称", required = true, example = "数据分析服务Key")
    @field:NotBlank(message = "密钥名称不能为空")
    @field:Size(max = 64, message = "密钥名称长度不能超过64个字符")
    val name: String,
)
