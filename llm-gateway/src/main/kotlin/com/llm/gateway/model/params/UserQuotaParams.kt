package com.llm.gateway.model.params

import com.fasterxml.jackson.annotation.JsonFormat
import com.fasterxml.jackson.annotation.JsonAlias
import com.llm.gateway.common.enums.UserQuotaTransactionQueryType
import com.llm.gateway.model.PageParams
import io.swagger.annotations.ApiModel
import io.swagger.annotations.ApiModelProperty
import java.math.BigDecimal
import java.util.Date
import javax.validation.constraints.DecimalMin
import javax.validation.constraints.NotBlank
import javax.validation.constraints.NotNull
import javax.validation.constraints.Size

@ApiModel("管理端-用户配额调额参数")
data class AdminUserQuotaAdjustmentParams(
    @ApiModelProperty(value = "调整金额，正数为新增，负数为回收", required = true, example = "100.000000")
    @field:NotNull(message = "调整额度不能为空")
    @field:JsonAlias("adjustTokens")
    val adjustAmount: BigDecimal?,
    @ApiModelProperty(value = "新增额度过期时间，新增额度时必填", required = false)
    @field:JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Shanghai")
    val expiresAt: Date? = null,
    @ApiModelProperty(value = "备注", required = false)
    @field:Size(max = 255, message = "备注不能超过255个字符")
    val remark: String? = null,
)

@ApiModel("管理端-用户配额转配权限参数")
data class AdminUserQuotaTransferPermissionParams(
    @ApiModelProperty(value = "是否允许用户向外转配额度", required = true)
    @field:NotNull(message = "是否允许转配不能为空")
    val allowTransferOut: Boolean?,
)

@ApiModel("用户端-配额转配参数")
data class UserQuotaTransferParams(
    @ApiModelProperty(value = "转入用户账号", required = true)
    @field:NotBlank(message = "转入用户账号不能为空")
    val targetUser: String?,
    @ApiModelProperty(value = "转配金额", required = true)
    @field:NotNull(message = "转配额度不能为空")
    @field:DecimalMin(value = "0.000001", message = "转配额度必须大于0")
    @field:JsonAlias("transferTokens")
    val transferAmount: BigDecimal?,
    @ApiModelProperty(value = "备注", required = false)
    @field:Size(max = 255, message = "备注不能超过255个字符")
    val remark: String? = null,
)

@ApiModel("用户配额流水分页参数")
class UserQuotaTransactionsPageParams : PageParams() {
    @ApiModelProperty(
        value = "变更类型：ADMIN_GRANT、ADMIN_RECLAIM、TRANSFER_OUT、TRANSFER_IN、USAGE_SETTLE、QUOTA_EXPIRE；不支持查询 USAGE_RESERVE、USAGE_REFUND",
        required = false
    )
    var type: UserQuotaTransactionQueryType? = null
}
