package com.llm.gateway.model.results

import com.fasterxml.jackson.annotation.JsonFormat
import io.swagger.annotations.ApiModel
import io.swagger.annotations.ApiModelProperty
import java.util.Date

@ApiModel("用户最近一个月小时使用次数热图")
data class UserUsageHourlyHeatmapResult(
    @ApiModelProperty(value = "统计开始日期，格式 yyyy-MM-dd", required = true)
    val startDate: String,
    @ApiModelProperty(value = "统计结束日期，格式 yyyy-MM-dd", required = true)
    val endDate: String,
    @ApiModelProperty(value = "统计总请求次数", required = true)
    val totalCount: Long,
    @ApiModelProperty(value = "单小时最大请求次数", required = true)
    val maxHourlyCount: Long,
    @ApiModelProperty(value = "每天24小时使用次数", required = true)
    val days: List<UserUsageHourlyHeatmapDayResult>,
)

@ApiModel("用户单日小时使用次数")
data class UserUsageHourlyHeatmapDayResult(
    @ApiModelProperty(value = "统计日期，格式 yyyy-MM-dd", required = true)
    val date: String,
    @ApiModelProperty(value = "单日请求次数", required = true)
    val totalCount: Long,
    @ApiModelProperty(value = "0-23点使用次数", required = true)
    val hours: List<UserUsageHourlyHeatmapHourResult>,
)

@ApiModel("用户单小时使用次数")
data class UserUsageHourlyHeatmapHourResult(
    @ApiModelProperty(value = "小时，取值范围 0-23", required = true)
    val hour: Int,
    @ApiModelProperty(value = "该小时请求次数", required = true)
    val requestCount: Long,
)

@ApiModel("用户模型使用次数统计")
data class UserModelUsageCountResult(
    @ApiModelProperty(value = "模型ID", required = true)
    val modelId: Long,
    @ApiModelProperty(value = "模型名称", required = true)
    val modelName: String,
    @ApiModelProperty(value = "模型供应商ID", required = true)
    val vendorId: Long,
    @ApiModelProperty(value = "使用次数", required = true)
    val usageCount: Long,
)

@ApiModel("用户使用日志列表项")
data class UserUsageLogListItemResult(
    @ApiModelProperty(value = "使用日志ID", required = true)
    val usageLogId: Long,
    @ApiModelProperty(value = "请求ID", required = true)
    val requestId: String,
    @ApiModelProperty(value = "供应商名称", required = true)
    val vendorName: String,
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
