package com.llm.gateway.model.params

import com.fasterxml.jackson.annotation.JsonFormat
import io.swagger.annotations.ApiModel
import io.swagger.annotations.ApiModelProperty
import java.util.Date
import javax.validation.constraints.Max
import javax.validation.constraints.Min

@ApiModel("管理端-部门配额状态更新参数")
data class AdminDepartmentQuotaStatusUpdateParams(
    @ApiModelProperty(value = "状态(1启用/0停用)", required = true)
    @field:Min(value = 0, message = "status仅支持0或1")
    @field:Max(value = 1, message = "status仅支持0或1")
    val status: Int,
    @ApiModelProperty(value = "客户端期望更新时间(毫秒时间戳)", required = true)
    @field:JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Shanghai")
    val expectedUpdatedAt: Date,
)
