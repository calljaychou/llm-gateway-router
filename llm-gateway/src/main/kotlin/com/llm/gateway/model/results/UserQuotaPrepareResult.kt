package com.llm.gateway.model.results

import com.llm.gateway.common.enums.DepartmentQuotaPeriod
import io.swagger.annotations.ApiModel
import io.swagger.annotations.ApiModelProperty
import java.math.BigDecimal

@ApiModel("用户配额预处理结果")
data class UserQuotaPrepareResult(
    @ApiModelProperty(value = "请求ID", required = true)
    val requestId: String,
    @ApiModelProperty(value = "用户ID", required = true)
    val userId: Long,
    @ApiModelProperty(value = "部门ID", required = true)
    val deptId: Long,
    @ApiModelProperty(value = "配额周期", required = true)
    val period: DepartmentQuotaPeriod,
    @ApiModelProperty(value = "配额周期键", required = true)
    val periodKey: String,
    @ApiModelProperty(value = "额度上限金额，", required = true)
    val quotaAmount: BigDecimal,
    @ApiModelProperty(value = "本次预占金额，", required = true)
    val reservedAmount: BigDecimal,
    @ApiModelProperty(value = "当前已用金额，", required = true)
    val usedAmount: BigDecimal,
    @ApiModelProperty(value = "当前处理中预占金额，", required = true)
    val reservedInFlightAmount: BigDecimal,
    @ApiModelProperty(value = "是否允许继续请求", required = true)
    val allowed: Boolean,
    @ApiModelProperty(value = "拒绝错误码", required = false)
    val rejectCode: String? = null,
    @ApiModelProperty(value = "拒绝原因", required = false)
    val rejectMessage: String? = null,
)
