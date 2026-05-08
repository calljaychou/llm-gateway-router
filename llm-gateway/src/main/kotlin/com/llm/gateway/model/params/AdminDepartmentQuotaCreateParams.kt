package com.llm.gateway.model.params

import com.llm.gateway.common.enums.DepartmentQuotaPeriod
import io.swagger.annotations.ApiModel
import io.swagger.annotations.ApiModelProperty
import javax.validation.constraints.Max
import javax.validation.constraints.Min
import javax.validation.constraints.Size

@ApiModel("管理端-部门配额创建参数")
data class AdminDepartmentQuotaCreateParams(
    @ApiModelProperty(value = "部门ID", required = true)
    val deptId: Long,
    @ApiModelProperty(value = "每用户Token额度", required = true)
    @field:Min(value = 1, message = "quotaTokens必须大于0")
    @field:Max(value = 1000000000000, message = "quotaTokens超出允许范围")
    val quotaTokens: Long,
    @ApiModelProperty(value = "周期(MONTHLY/FOREVER)", required = true)
    val period: DepartmentQuotaPeriod,
    @ApiModelProperty(value = "状态(1启用/0停用)", required = false, example = "1")
    @field:Min(value = 0, message = "status仅支持0或1")
    @field:Max(value = 1, message = "status仅支持0或1")
    val status: Int? = null,
    @ApiModelProperty(value = "备注", required = false)
    @field:Size(max = 255, message = "remark长度不能超过255")
    val remark: String? = null,
)
