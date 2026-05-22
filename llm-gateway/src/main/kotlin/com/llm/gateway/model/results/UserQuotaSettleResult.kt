package com.llm.gateway.model.results

import com.fasterxml.jackson.annotation.JsonFormat
import com.llm.gateway.common.enums.TokenCalcSource
import com.llm.gateway.common.enums.UsageAccountingStatus
import io.swagger.annotations.ApiModel
import io.swagger.annotations.ApiModelProperty
import java.util.Date

@ApiModel("用户配额结算结果")
data class UserQuotaSettleResult(
    @ApiModelProperty(value = "请求ID", required = true)
    val requestId: String,
    @ApiModelProperty(value = "是否结算成功", required = true)
    val settled: Boolean,
    @ApiModelProperty(value = "结算后已用token", required = true)
    val usedTokens: Long,
    @ApiModelProperty(value = "结算后处理中预占token", required = true)
    val reservedInFlightTokens: Long,
    @ApiModelProperty(value = "本次最终结算token", required = true)
    val settledTotalTokens: Int,
    @ApiModelProperty(value = "结算状态", required = true)
    val accountingStatus: UsageAccountingStatus,
    @ApiModelProperty(value = "Token计算来源", required = true)
    val calcSource: TokenCalcSource,
    @ApiModelProperty(value = "补偿次数", required = true)
    val retryCount: Int,
    @ApiModelProperty(value = "最终结算时间", required = false)
    @field:JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Shanghai")
    val settledAt: Date?,
)
