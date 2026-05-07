package com.llm.gateway.model.results

import io.swagger.annotations.ApiModel
import io.swagger.annotations.ApiModelProperty

@ApiModel("管理端-部门权限视图")
data class DepartmentPermissionsViewResult(
    @ApiModelProperty(value = "部门ID", required = true)
    val deptId: Long,
    @ApiModelProperty(value = "权限项")
    val models: List<DepartmentPermissionViewItem>,
)

@ApiModel("管理端-部门权限项")
data class DepartmentPermissionViewItem(
    @ApiModelProperty(value = "模型别名", required = true)
    val modelAlias: String,
    @ApiModelProperty(value = "真实模型名称", required = true)
    val realModelName: String,
    @ApiModelProperty(value = "供应商ID", required = true)
    val vendorId: Long,
    @ApiModelProperty(value = "计费类型(FREE/PAID)", required = true)
    val billingType: String,
    @ApiModelProperty(value = "模型是否启用", required = true)
    val active: Boolean,
    @ApiModelProperty(value = "权限来源部门ID", required = true)
    val sourceDeptId: Long,
    @ApiModelProperty(value = "作用域(SELF/SUBTREE)", required = true)
    val scope: String,
)
