package com.llm.gateway.model.results

import io.swagger.annotations.ApiModel
import io.swagger.annotations.ApiModelProperty

@ApiModel("管理端-用户生效权限结果")
data class UserEffectivePermissionsResult(
    @ApiModelProperty(value = "用户ID", required = true)
    val userId: Long,
    @ApiModelProperty(value = "部门ID", required = true)
    val deptId: Long,
    @ApiModelProperty(value = "生效模型别名列表", required = true)
    val allowedModels: List<String>,
)
