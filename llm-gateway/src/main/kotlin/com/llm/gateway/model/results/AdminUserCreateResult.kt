package com.llm.gateway.model.results

import io.swagger.annotations.ApiModel
import io.swagger.annotations.ApiModelProperty

@ApiModel("管理端-创建用户结果")
data class AdminUserCreateResult(
    @ApiModelProperty(value = "用户ID", required = true)
    val userId: Long,
    @ApiModelProperty(value = "是否已完成密码修改", required = true)
    val passwordChanged: Boolean,
)
