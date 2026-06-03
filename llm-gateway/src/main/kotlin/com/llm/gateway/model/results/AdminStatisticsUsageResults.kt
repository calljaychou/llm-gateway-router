package com.llm.gateway.model.results

import com.fasterxml.jackson.annotation.JsonFormat
import io.swagger.annotations.ApiModel
import io.swagger.annotations.ApiModelProperty
import java.math.BigDecimal
import java.util.Date

@ApiModel("管理端-使用日志列表项")
data class AdminStatisticsUsageLogItemResult(
    @ApiModelProperty(value = "使用日志ID", required = true)
    val usageLogId: Long,
    @ApiModelProperty(value = "请求ID", required = true)
    val requestId: String,
    @ApiModelProperty(value = "用户ID", required = true)
    val userId: Long,
    @ApiModelProperty(value = "用户姓名", required = true)
    val userName: String,
    @ApiModelProperty(value = "用户账号", required = true)
    val userAccount: String,
    @ApiModelProperty(value = "部门名称", required = true)
    val deptName: String,
    @ApiModelProperty(value = "供应商ID", required = false)
    val vendorId: Long?,
    @ApiModelProperty(value = "供应商名称", required = true)
    val vendorName: String,
    @ApiModelProperty(value = "模型ID", required = false)
    val modelId: Long?,
    @ApiModelProperty(value = "模型名称", required = true)
    val modelName: String,
    @ApiModelProperty(value = "输入Token", required = true)
    val inputTokens: Int,
    @ApiModelProperty(value = "输出Token", required = true)
    val outputTokens: Int,
    @ApiModelProperty(value = "总Token", required = true)
    val totalTokens: Int,
    @ApiModelProperty(value = "缓存Token", required = true)
    val cachedTokens: Int,
    @ApiModelProperty(value = "使用时间", required = false)
    @field:JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Shanghai")
    val usedAt: Date?,
    @ApiModelProperty(value = "耗时，单位毫秒", required = true)
    val latencyMs: Int,
)

@ApiModel("管理端-部门使用统计列表项")
data class AdminStatisticsDepartmentUsageItemResult(
    @ApiModelProperty(value = "部门ID", required = true)
    val deptId: Long,
    @ApiModelProperty(value = "部门名称", required = true)
    val deptName: String,
    @ApiModelProperty(value = "使用次数", required = true)
    val usageCount: Long,
    @ApiModelProperty(value = "使用Token数", required = true)
    val totalTokens: Long,
)

@ApiModel("管理端-用户使用统计列表项")
data class AdminStatisticsUserUsageItemResult(
    @ApiModelProperty(value = "用户ID", required = true)
    val userId: Long,
    @ApiModelProperty(value = "用户姓名", required = true)
    val userName: String,
    @ApiModelProperty(value = "用户名", required = false)
    val username: String?,
    @ApiModelProperty(value = "手机号", required = false)
    val mobile: String?,
    @ApiModelProperty(value = "邮箱", required = false)
    val email: String?,
    @ApiModelProperty(value = "部门名称", required = true)
    val deptName: String,
    @ApiModelProperty(value = "余额", required = true)
    val balance: BigDecimal,
    @ApiModelProperty(value = "使用Token数", required = true)
    val totalTokens: Long,
    @ApiModelProperty(value = "使用次数", required = true)
    val usageCount: Long,
)
