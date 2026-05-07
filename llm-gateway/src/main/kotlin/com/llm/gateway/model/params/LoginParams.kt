package com.llm.gateway.model.params

import io.swagger.annotations.ApiModel
import io.swagger.annotations.ApiModelProperty
import javax.validation.constraints.NotBlank

/**
 * @author JayCHou <a href="calljaychou@qq.com">Email</a>
 */
@ApiModel("登录请求")
data class LoginParams(
    @ApiModelProperty(value = "用户名或邮箱", required = true)
    @field:NotBlank(message = "用户名或邮箱不能为空")
    val username: String,
    @ApiModelProperty(value = "密码", required = true)
    @field:NotBlank(message = "密码不能为空")
    val password: String,
)