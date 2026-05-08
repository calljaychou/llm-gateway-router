package com.llm.gateway.model.params

import com.llm.gateway.common.enums.DepartmentQuotaPeriod
import com.llm.gateway.model.PageParams
import io.swagger.annotations.ApiModel
import io.swagger.annotations.ApiModelProperty
import javax.validation.constraints.Max
import javax.validation.constraints.Min

@ApiModel("管理端-部门配额分页查询参数")
class AdminDepartmentQuotaPageParams(
    @ApiModelProperty(value = "部门ID", required = false)
    var deptId: Long? = null,

    @ApiModelProperty(value = "周期(MONTHLY/FOREVER)", required = false)
    var period: DepartmentQuotaPeriod? = null,

    @ApiModelProperty(value = "状态(0停用/1启用)", required = false)
    @field:Min(value = 0, message = "status仅支持0或1")
    @field:Max(value = 1, message = "status仅支持0或1")
    var status: Int? = null,
) : PageParams()
