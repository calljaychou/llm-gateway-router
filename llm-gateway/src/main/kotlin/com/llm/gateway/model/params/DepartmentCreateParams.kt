package com.llm.gateway.model.params

import io.swagger.annotations.ApiModel
import io.swagger.annotations.ApiModelProperty
import javax.validation.constraints.NotBlank
import javax.validation.constraints.PositiveOrZero

@ApiModel("管理端-创建部门参数")
data class DepartmentCreateParams(
    @ApiModelProperty(value = "部门名称", required = true, example = "研发中心")
    @field:NotBlank(message = "部门名称不能为空")
    val deptName: String,
    @ApiModelProperty(value = "排序号，数值越小越靠前", example = "10")
    @field:PositiveOrZero(message = "排序号不能小于0")
    val orderNum: Int? = 0,
    @ApiModelProperty(value = "负责人", example = "1001")
    val leaderName: String? = null,
    @ApiModelProperty(value = "部门联系电话", example = "010-88886666")
    val tel: String? = null,
)
