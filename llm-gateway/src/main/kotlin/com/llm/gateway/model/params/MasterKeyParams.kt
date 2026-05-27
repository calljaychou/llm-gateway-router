package com.llm.gateway.model.params

import com.llm.gateway.model.PageParams
import io.swagger.annotations.ApiModel
import io.swagger.annotations.ApiModelProperty
import javax.validation.constraints.Max
import javax.validation.constraints.Min
import javax.validation.constraints.NotBlank
import javax.validation.constraints.NotNull

@ApiModel("创建主密钥参数")
data class MasterKeyCreateParams(
    @ApiModelProperty(value = "供应商ID", required = true, example = "1")
    @field:NotNull(message = "供应商ID不能为空")
    val vendorId: Long?,
    @ApiModelProperty(value = "真实供应商 API Key", required = true)
    @field:NotBlank(message = "API Key不能为空")
    val apiKey: String,
    @ApiModelProperty(value = "负载均衡权重", example = "10")
    @field:Min(value = 1, message = "权重最小为1")
    val weight: Int? = null,
    @ApiModelProperty(value = "状态(1-正常,2-异常)", example = "1")
    @field:Min(value = 1, message = "状态值最小为1")
    @field:Max(value = 2, message = "状态值最大为2")
    val status: Int? = null,
)

@ApiModel("主密钥分页查询参数")
class MasterKeyPageParams : PageParams() {
    @ApiModelProperty(value = "供应商ID，不传则查询全部", required = false, example = "1")
    var vendorId: Long? = null

    @ApiModelProperty(value = "状态(1-正常,2-异常)，不传则查询全部", required = false, example = "1")
    @field:Min(value = 1, message = "状态值最小为1")
    @field:Max(value = 2, message = "状态值最大为2")
    var status: Int? = null
}
