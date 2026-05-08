package com.llm.gateway.model.results

import com.fasterxml.jackson.annotation.JsonFormat
import io.swagger.annotations.ApiModel
import io.swagger.annotations.ApiModelProperty
import java.util.Date

@ApiModel("管理端-部门配额项")
data class AdminDepartmentQuotaItemResult(
    @ApiModelProperty(value = "主键ID", required = true)
    val id: Long,
    @ApiModelProperty(value = "部门ID", required = true)
    val deptId: Long,
    @ApiModelProperty(value = "每用户Token额度", required = true)
    val quotaTokens: Long,
    @ApiModelProperty(value = "周期(MONTHLY/FOREVER)", required = true)
    val period: String,
    @ApiModelProperty(value = "状态(1启用/0停用)", required = true)
    val status: Int,
    @ApiModelProperty(value = "备注", required = false)
    val remark: String?,
    @ApiModelProperty(value = "创建时间(yyyy-MM-dd HH:mm:ss)", required = false)
    @field:JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Shanghai")
    val createdAt: Date?,
    @ApiModelProperty(value = "更新时间(yyyy-MM-dd HH:mm:ss)", required = false)
    @field:JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Shanghai")
    val updatedAt: Date?,
)

@ApiModel("管理端-部门配额分页结果")
data class AdminDepartmentQuotaPageResult(
    @ApiModelProperty(value = "页码", required = true)
    val pageNum: Int,
    @ApiModelProperty(value = "每页数量", required = true)
    val pageSize: Int,
    @ApiModelProperty(value = "总数", required = true)
    val total: Long,
    @ApiModelProperty(value = "数据列表", required = true)
    val records: List<AdminDepartmentQuotaItemResult>,
)

@ApiModel("管理端-部门配额操作结果")
data class AdminDepartmentQuotaOperateResult(
    @ApiModelProperty(value = "部门ID", required = true)
    val deptId: Long,
    @ApiModelProperty(value = "策略ID", required = true)
    val id: Long,
    @ApiModelProperty(value = "最新更新时间(yyyy-MM-dd HH:mm:ss)", required = false)
    @field:JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Shanghai")
    val updatedAt: Date?,
)
