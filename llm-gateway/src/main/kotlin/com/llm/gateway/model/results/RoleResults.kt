package com.llm.gateway.model.results

import com.fasterxml.jackson.annotation.JsonFormat
import io.swagger.annotations.ApiModel
import io.swagger.annotations.ApiModelProperty
import java.util.Date

@ApiModel("管理端-角色列表项")
data class RoleListItemResult(
    @ApiModelProperty(value = "角色ID", required = true)
    val id: Long,
    @ApiModelProperty(value = "角色名称", required = true)
    val roleName: String,
    @ApiModelProperty(value = "角色标识", required = true)
    val roleKey: String,
    @ApiModelProperty(value = "排序号", required = true)
    val roleSort: Int,
    @ApiModelProperty(value = "创建人")
    val createdBy: String?,
    @ApiModelProperty(value = "创建时间")
    @field:JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Shanghai")
    val createdTime: Date?,
)

@ApiModel("管理端-角色创建结果")
data class RoleCreateResult(
    @ApiModelProperty(value = "角色ID", required = true)
    val roleId: Long,
    @ApiModelProperty(value = "角色名称", required = true)
    val roleName: String,
    @ApiModelProperty(value = "角色标识", required = true)
    val roleKey: String,
)

@ApiModel("管理端-角色删除结果")
data class RoleDeleteResult(
    @ApiModelProperty(value = "角色ID", required = true)
    val roleId: Long,
    @ApiModelProperty(value = "是否已删除", required = true)
    val deleted: Boolean,
    @ApiModelProperty(value = "已删除的用户角色关联数量", required = true)
    val removedUserRoleRelCount: Long,
)
