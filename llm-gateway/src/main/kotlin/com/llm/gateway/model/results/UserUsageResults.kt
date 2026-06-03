package com.llm.gateway.model.results

import io.swagger.annotations.ApiModel
import io.swagger.annotations.ApiModelProperty

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
