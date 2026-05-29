package com.llm.gateway.model.params

import com.fasterxml.jackson.annotation.JsonFormat
import com.llm.gateway.model.PageParams
import io.swagger.annotations.ApiModel
import io.swagger.annotations.ApiModelProperty
import java.util.Date
import javax.validation.constraints.Min
import javax.validation.constraints.NotBlank
import javax.validation.constraints.NotNull
import javax.validation.constraints.Size

@ApiModel("管理端-用户配额调额参数")
data class AdminUserQuotaAdjustmentParams(
    @ApiModelProperty(value = "调整Token数量，正数为新增，负数为回收", required = true, example = "100000")
    @field:NotNull(message = "调整额度不能为空")
    val adjustTokens: Long?,
    @ApiModelProperty(value = "新增额度过期时间，新增额度时必填", required = false)
    @field:JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Shanghai")
    val expiresAt: Date? = null,
    @ApiModelProperty(value = "备注", required = false)
    @field:Size(max = 255, message = "备注不能超过255个字符")
    val remark: String? = null,
)

@ApiModel("用户端-配额转配参数")
data class UserQuotaTransferParams(
    @ApiModelProperty(value = "转入用户账号", required = true)
    @field:NotBlank(message = "转入用户账号不能为空")
    val targetUser: String?,
    @ApiModelProperty(value = "转配Token数量", required = true)
    @field:NotNull(message = "转配额度不能为空")
    @field:Min(value = 1, message = "转配额度必须大于0")
    val transferTokens: Long?,
    @ApiModelProperty(value = "备注", required = false)
    @field:Size(max = 255, message = "备注不能超过255个字符")
    val remark: String? = null,
)

@ApiModel("用户配额流水分页参数")
class UserQuotaTransactionsPageParams : PageParams() {
    @ApiModelProperty(value = "变更类型", required = false)
    var changeType: String? = null
}
