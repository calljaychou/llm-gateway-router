package com.llm.gateway.model.params

import io.swagger.annotations.ApiModel
import io.swagger.annotations.ApiModelProperty
import javax.validation.constraints.NotBlank
import javax.validation.constraints.PositiveOrZero

@ApiModel("管理端-创建角色参数")
data class RoleCreateParams(
    @ApiModelProperty(value = "角色名称", required = true, example = "普通用户")
    @field:NotBlank(message = "角色名称不能为空")
    val roleName: String,
    @ApiModelProperty(value = "角色标识", required = true, example = "user")
    @field:NotBlank(message = "角色标识不能为空")
    val roleKey: String,
    @ApiModelProperty(value = "排序号，数值越小越靠前", example = "10")
    @field:PositiveOrZero(message = "排序号不能小于0")
    val roleSort: Int? = 0,
)
