package com.llm.gateway.model.results

import com.fasterxml.jackson.annotation.JsonFormat
import io.swagger.annotations.ApiModel
import io.swagger.annotations.ApiModelProperty
import java.util.Date

/**
 * @author JayCHou <a href="calljaychou@qq.com">Email</a>
 */
@ApiModel("登录结果")
data class LoginResult(
    @ApiModelProperty(value = "JWT Token", required = true)
    val token: String,
    @ApiModelProperty(value = "Token 类型", required = true, example = "Bearer")
    val tokenType: String,
    @ApiModelProperty(value = "过期时间(秒)", required = true)
    val expiresInSeconds: Long,
    @ApiModelProperty(value = "用户ID", required = true)
    val userId: Long,
    @ApiModelProperty(value = "用户名", required = true)
    val username: String,
    @ApiModelProperty(value = "邮箱")
    val email: String?,
    @ApiModelProperty(value = "角色列表", required = true)
    val roles: List<String>,
    @ApiModelProperty(value = "是否已改初始密码", required = true)
    val passwordChanged: Boolean,
    @ApiModelProperty(value = "登录使用时间")
    @field:JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Shanghai")
    val useTime: Date?,
)
