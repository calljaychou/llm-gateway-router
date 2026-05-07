package com.llm.gateway.model.results

import io.swagger.annotations.ApiModel
import io.swagger.annotations.ApiModelProperty

@ApiModel("管理端-部门权限更新结果")
data class DepartmentPermissionsUpdateResult(
    @ApiModelProperty(value = "部门ID", required = true)
    val deptId: Long,
    @ApiModelProperty(value = "新增条数", required = true)
    val added: Int,
    @ApiModelProperty(value = "移除条数", required = true)
    val removed: Int,
    @ApiModelProperty(value = "更新条数", required = true)
    val updated: Int,
)
