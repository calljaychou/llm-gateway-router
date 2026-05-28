package com.llm.gateway.model.params

import com.llm.gateway.model.PageParams
import io.swagger.annotations.ApiModel
import io.swagger.annotations.ApiModelProperty
import javax.validation.constraints.Email
import javax.validation.constraints.NotBlank
import javax.validation.constraints.NotEmpty
import javax.validation.constraints.NotNull

@ApiModel("管理端-创建用户参数")
data class AdminUserCreateParams(
    @ApiModelProperty(value = "姓名", required = true, example = "alice")
    @field:NotBlank(message = "用户姓名名不能为空")
    val name: String,
    @ApiModelProperty(value = "用户名", required = true, example = "alice")
    @field:NotBlank(message = "用户名不能为空")
    val username: String,
    @ApiModelProperty(value = "邮箱", required = true, example = "alice@company.com")
    @field:NotBlank(message = "邮箱不能为空")
    @field:Email(message = "邮箱格式不正确")
    val email: String,
    @ApiModelProperty(value = "手机号", example = "13800138000")
    val mobile: String? = null,
    @ApiModelProperty(value = "部门ID", required = true, example = "2001")
    @field:NotNull(message = "部门ID不能为空")
    val deptId: Long?,
    @ApiModelProperty(value = "角色标识列表", required = true, example = "[\"user\"]")
    @field:NotEmpty(message = "角色列表不能为空")
    val roleKeys: List<String>,
    @ApiModelProperty(value = "初始密码", required = true, example = "Temp@123456")
    @field:NotBlank(message = "初始密码不能为空")
    val password: String,
    @ApiModelProperty(value = "是否强制首登改密", example = "true")
    val forcePasswordChange: Boolean? = true,
)

@ApiModel("管理端-用户分页查询参数")
class AdminUserPageParams : PageParams() {
    @ApiModelProperty(value = "部门ID", required = false, example = "2001")
    var deptId: Long? = null

    @ApiModelProperty(value = "手机号", required = false, example = "13800138000")
    var mobile: String? = null

    @ApiModelProperty(value = "邮箱", required = false, example = "alice@company.com")
    var email: String? = null
}
