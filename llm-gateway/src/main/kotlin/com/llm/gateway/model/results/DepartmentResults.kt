package com.llm.gateway.model.results

import io.swagger.annotations.ApiModel
import io.swagger.annotations.ApiModelProperty

@ApiModel("管理端-部门创建结果")
data class DepartmentCreateResult(
    @ApiModelProperty(value = "部门ID", required = true)
    val deptId: Long,
    @ApiModelProperty(value = "父部门ID，一级部门为0", required = true)
    val parentId: Long,
    @ApiModelProperty(value = "部门名称", required = true)
    val deptName: String,
)

@ApiModel("管理端-部门删除结果")
data class DepartmentDeleteResult(
    @ApiModelProperty(value = "部门ID", required = true)
    val deptId: Long,
    @ApiModelProperty(value = "是否已删除", required = true)
    val deleted: Boolean,
    @ApiModelProperty(value = "已解除部门归属的用户数量", required = true)
    val unboundUserCount: Long,
)

@ApiModel("管理端-部门树节点")
data class DepartmentTreeResult(
    @ApiModelProperty(value = "部门ID", required = true)
    val id: Long,
    @ApiModelProperty(value = "部门名称", required = true)
    val name: String,
    @ApiModelProperty(value = "父部门ID，一级部门为0", required = true)
    val parentId: Long,
    @ApiModelProperty(value = "排序号", required = true)
    val orderNum: Int,
    @ApiModelProperty(value = "负责人")
    val leaderName: String?,
    @ApiModelProperty(value = "联系电话")
    val tel: String?,
    @ApiModelProperty(value = "状态", required = true)
    val status: Int,
    @ApiModelProperty(value = "子部门")
    val children: List<DepartmentTreeResult> = emptyList(),
)
