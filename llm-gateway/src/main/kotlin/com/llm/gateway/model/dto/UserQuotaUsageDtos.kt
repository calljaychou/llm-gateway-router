package com.llm.gateway.model.dto

import io.swagger.annotations.ApiModel
import io.swagger.annotations.ApiModelProperty

@ApiModel("用户配额预占拆分")
data class UserQuotaReserveSplitDto(
    @field:ApiModelProperty("配额批次ID")
    val grantId: Long,
    @field:ApiModelProperty("预占Token数")
    val tokens: Long,
)

@ApiModel("用户配额预占上下文")
data class UserQuotaReservationDto(
    @field:ApiModelProperty("请求ID")
    val requestId: String,
    @field:ApiModelProperty("用户ID")
    val userId: Long,
    @field:ApiModelProperty("预占Token数")
    val reservedTokens: Long,
    @field:ApiModelProperty("预占拆分")
    val splits: List<UserQuotaReserveSplitDto>,
)

@ApiModel("用户配额结算结果")
data class UserQuotaUsageSettleDto(
    @field:ApiModelProperty("请求ID")
    val requestId: String,
    @field:ApiModelProperty("是否结算成功")
    val settled: Boolean,
    @field:ApiModelProperty("实际Token数")
    val actualTokens: Long,
    @field:ApiModelProperty("退款Token数")
    val refundedTokens: Long,
    @field:ApiModelProperty("追加扣减Token数")
    val extraDeductedTokens: Long,
)

@ApiModel("Token用量明细")
data class TokenUsageDto(
    @field:ApiModelProperty("输入Token")
    val promptTokens: Int,
    @field:ApiModelProperty("输出Token")
    val completionTokens: Int,
    @field:ApiModelProperty("总Token")
    val totalTokens: Int,
)

@ApiModel("用量日志记录命令")
data class UsageLogRecordCommand(
    @field:ApiModelProperty("请求ID")
    val requestId: String,
    @field:ApiModelProperty("用户ID")
    val userId: Long,
    @field:ApiModelProperty("供应商ID")
    val vendorId: Long,
    @field:ApiModelProperty("模型别名")
    val modelAlias: String,
    @field:ApiModelProperty("接口语义")
    val endpoint: String,
    @field:ApiModelProperty("是否流式")
    val stream: Boolean,
    @field:ApiModelProperty("预占Token")
    val reservedTokens: Int,
    @field:ApiModelProperty("Token用量")
    val usage: TokenUsageDto,
    @field:ApiModelProperty("耗时毫秒")
    val latencyMs: Int?,
    @field:ApiModelProperty("HTTP状态码")
    val statusCode: Int,
    @field:ApiModelProperty("错误码")
    val errorCode: String?,
    @field:ApiModelProperty("结算状态")
    val accountingStatus: String,
    @field:ApiModelProperty("Token来源")
    val calcSource: String?,
)
