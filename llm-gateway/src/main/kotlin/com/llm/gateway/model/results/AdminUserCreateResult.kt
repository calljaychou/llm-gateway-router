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

@ApiModel("管理端-修改用户密码结果")
data class AdminUserPasswordChangeResult(
    @ApiModelProperty(value = "用户ID", required = true)
    val userId: Long,
    @ApiModelProperty(value = "是否已完成密码修改", required = true)
    val passwordChanged: Boolean,
)

@ApiModel("管理端-修改用户信息结果")
data class AdminUserUpdateResult(
    @ApiModelProperty(value = "用户ID", required = true)
    val userId: Long,
    @ApiModelProperty(value = "已绑定角色数量", required = true)
    val roleCount: Int,
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

@ApiModel("管理端-用户详情")
data class AdminUserDetailResult(
    @ApiModelProperty(value = "用户基础信息", required = true)
    val user: AdminUserBaseInfoResult,
    @ApiModelProperty(value = "用户关联部门")
    val department: AdminUserDepartmentResult?,
    @ApiModelProperty(value = "用户关联角色", required = true)
    val roles: List<RoleListItemResult>,
    @ApiModelProperty(value = "用户可用模型权限列表", required = true)
    val modelPermissions: List<DepartmentPermissionViewItem>,
    @ApiModelProperty(value = "用户配额配置详情")
    val quota: AdminUserQuotaConfigResult?,
)

@ApiModel("管理端-用户基础信息")
data class AdminUserBaseInfoResult(
    @ApiModelProperty(value = "用户ID", required = true)
    val userId: Long,
    @ApiModelProperty(value = "姓名")
    val name: String?,
    @ApiModelProperty(value = "部门ID")
    val deptId: Long?,
    @ApiModelProperty(value = "用户名")
    val username: String?,
    @ApiModelProperty(value = "邮箱")
    val email: String?,
    @ApiModelProperty(value = "手机号")
    val mobile: String?,
    @ApiModelProperty(value = "性别")
    val gender: Int?,
    @ApiModelProperty(value = "头像地址")
    val avatarUrl: String?,
    @ApiModelProperty(value = "是否已完成密码修改")
    val passwordChanged: Boolean?,
    @ApiModelProperty(value = "备注")
    val remark: String?,
    @ApiModelProperty(value = "状态")
    val status: Int?,
    @ApiModelProperty(value = "是否删除")
    val delFlag: Boolean?,
    @ApiModelProperty(value = "创建时间")
    @field:JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Shanghai")
    val createdTime: Date?,
    @ApiModelProperty(value = "更新时间")
    @field:JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Shanghai")
    val updatedTime: Date?,
)

@ApiModel("管理端-用户关联部门")
data class AdminUserDepartmentResult(
    @ApiModelProperty(value = "部门ID", required = true)
    val deptId: Long,
    @ApiModelProperty(value = "父部门ID", required = true)
    val parentId: Long,
    @ApiModelProperty(value = "部门名称", required = true)
    val deptName: String,
    @ApiModelProperty(value = "排序号", required = true)
    val orderNum: Int,
    @ApiModelProperty(value = "负责人用户")
    val leaderName: String?,
    @ApiModelProperty(value = "联系电话")
    val tel: String?,
    @ApiModelProperty(value = "状态", required = true)
    val status: Int,
)

@ApiModel("管理端-用户配额配置详情")
data class AdminUserQuotaConfigResult(
    @ApiModelProperty(value = "用户ID", required = true)
    val userId: Long,
    @ApiModelProperty(value = "当前仍在有效期内的总配额", required = true)
    val currentQuotaTokens: Long,
    @ApiModelProperty(value = "当前可用配额", required = true)
    val availableTokens: Long,
    @ApiModelProperty(value = "当前已消耗Token", required = true)
    val usedTokens: Long,
    @ApiModelProperty(value = "当前已过期Token累计", required = true)
    val expiredTokens: Long,
    @ApiModelProperty(value = "累计转入Token", required = true)
    val transferredInTokens: Long,
    @ApiModelProperty(value = "累计转出Token", required = true)
    val transferredOutTokens: Long,
    @ApiModelProperty(value = "是否允许向外转配", required = true)
    val allowTransferOut: Boolean,
    @ApiModelProperty(value = "当前有效配额中的最早过期时间")
    @field:JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Shanghai")
    val earliestExpireAt: Date?,
    @ApiModelProperty(value = "更新时间")
    @field:JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Shanghai")
    val updatedAt: Date?,
)
