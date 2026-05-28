package com.llm.gateway.model.results

import com.fasterxml.jackson.annotation.JsonFormat
import io.swagger.annotations.ApiModel
import io.swagger.annotations.ApiModelProperty
import java.util.Date

@ApiModel("管理端-创建用户结果")
data class AdminUserCreateResult(
    @ApiModelProperty(value = "用户ID", required = true)
    val userId: Long,
    @ApiModelProperty(value = "是否已完成密码修改", required = true)
    val passwordChanged: Boolean,
)

@ApiModel("管理端-用户分页列表项")
data class AdminUserPageItemResult(
    @ApiModelProperty(value = "用户ID", required = true)
    val userId: Long,
    @ApiModelProperty(value = "名称", required = true)
    val name: String?,
    @ApiModelProperty(value = "用户名", required = true)
    val username: String,
    @ApiModelProperty(value = "手机号")
    val mobile: String?,
    @ApiModelProperty(value = "所属部门ID")
    val deptId: Long?,
    @ApiModelProperty(value = "所属部门名称")
    val deptName: String?,
    @ApiModelProperty(value = "邮箱")
    val email: String?,
    @ApiModelProperty(value = "性别", required = true)
    val gender: Int,
    @ApiModelProperty(value = "状态", required = true)
    val status: Int,
    @ApiModelProperty(value = "创建时间")
    @field:JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Shanghai")
    val createdTime: Date?,
)
