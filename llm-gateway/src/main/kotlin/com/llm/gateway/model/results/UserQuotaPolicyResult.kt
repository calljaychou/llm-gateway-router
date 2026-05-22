package com.llm.gateway.model.results

import com.llm.gateway.common.enums.DepartmentQuotaPeriod
import io.swagger.annotations.ApiModel
import io.swagger.annotations.ApiModelProperty

@ApiModel("用户配额策略结果")
data class UserQuotaPolicyResult(
    @ApiModelProperty(value = "用户ID", required = true)
    val userId: Long,
    @ApiModelProperty(value = "部门ID", required = true)
    val deptId: Long,
    @ApiModelProperty(value = "部门配额策略主键", required = true)
    val quotaPolicyId: Long,
    @ApiModelProperty(value = "每用户额度上限", required = true)
    val quotaTokens: Long,
    @ApiModelProperty(value = "配额周期", required = true)
    val period: DepartmentQuotaPeriod,
    @ApiModelProperty(value = "策略状态", required = true)
    val status: Int,
)
