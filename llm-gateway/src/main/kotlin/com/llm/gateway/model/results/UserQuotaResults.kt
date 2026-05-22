package com.llm.gateway.model.results

import com.fasterxml.jackson.annotation.JsonFormat
import io.swagger.annotations.ApiModel
import io.swagger.annotations.ApiModelProperty
import java.util.Date

@ApiModel("用户配额账户快照")
data class UserQuotaAccountResult(
    @ApiModelProperty(value = "用户ID", required = true)
    val userId: Long,
    @ApiModelProperty(value = "当前仍在有效期内的总配额", required = true)
    val currentQuotaTokens: Long,
    @ApiModelProperty(value = "当前已消耗Token", required = true)
    val usedTokens: Long,
    @ApiModelProperty(value = "当前已过期Token累计", required = true)
    val expiredTokens: Long,
    @ApiModelProperty(value = "累计转入Token", required = true)
    val transferredInTokens: Long,
    @ApiModelProperty(value = "累计转出Token", required = true)
    val transferredOutTokens: Long,
    @ApiModelProperty(value = "当前可消费、可转配剩余额度", required = true)
    val availableTokens: Long,
    @ApiModelProperty(value = "是否允许向外转配", required = true)
    val allowTransferOut: Boolean,
    @ApiModelProperty(value = "当前有效配额中的最早过期时间", required = false)
    @field:JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Shanghai")
    val earliestExpireAt: Date?,
    @ApiModelProperty(value = "更新时间", required = false)
    @field:JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Shanghai")
    val updatedAt: Date?,
    @ApiModelProperty(value = "最近即将过期的有效配额批次", required = true)
    val activeGrants: List<UserQuotaGrantResult> = emptyList(),
)

@ApiModel("用户配额批次")
data class UserQuotaGrantResult(
    @ApiModelProperty(value = "批次ID", required = true)
    val grantId: Long,
    @ApiModelProperty(value = "用户ID", required = true)
    val userId: Long,
    @ApiModelProperty(value = "配额来源", required = true)
    val sourceType: String,
    @ApiModelProperty(value = "来源用户ID", required = false)
    val sourceUserId: Long?,
    @ApiModelProperty(value = "来源配额批次ID", required = false)
    val sourceGrantId: Long?,
    @ApiModelProperty(value = "发放额度", required = true)
    val grantedTokens: Long,
    @ApiModelProperty(value = "当前剩余额度", required = true)
    val remainingTokens: Long,
    @ApiModelProperty(value = "已消费额度", required = true)
    val consumedTokens: Long,
    @ApiModelProperty(value = "过期额度", required = true)
    val expiredTokens: Long,
    @ApiModelProperty(value = "过期时间", required = true)
    @field:JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Shanghai")
    val expiresAt: Date,
    @ApiModelProperty(value = "批次状态", required = true)
    val status: String,
    @ApiModelProperty(value = "操作人", required = false)
    val grantedBy: Long?,
    @ApiModelProperty(value = "备注", required = false)
    val remark: String?,
    @ApiModelProperty(value = "创建时间", required = false)
    @field:JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Shanghai")
    val createdAt: Date?,
    @ApiModelProperty(value = "更新时间", required = false)
    @field:JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Shanghai")
    val updatedAt: Date?,
)

@ApiModel("用户配额流水")
data class UserQuotaTransactionResult(
    @ApiModelProperty(value = "流水ID", required = true)
    val transactionId: Long,
    @ApiModelProperty(value = "业务流水号", required = true)
    val bizNo: String,
    @ApiModelProperty(value = "用户ID", required = true)
    val userId: Long,
    @ApiModelProperty(value = "关联配额批次ID", required = false)
    val grantId: Long?,
    @ApiModelProperty(value = "变更类型", required = true)
    val changeType: String,
    @ApiModelProperty(value = "变更额度", required = true)
    val deltaTokens: Long,
    @ApiModelProperty(value = "变更前当前有效总配额", required = true)
    val quotaBefore: Long,
    @ApiModelProperty(value = "变更后当前有效总配额", required = true)
    val quotaAfter: Long,
    @ApiModelProperty(value = "变更前剩余额度", required = true)
    val availableBefore: Long,
    @ApiModelProperty(value = "变更后剩余额度", required = true)
    val availableAfter: Long,
    @ApiModelProperty(value = "对手方用户ID", required = false)
    val counterpartyUserId: Long?,
    @ApiModelProperty(value = "关联请求ID", required = false)
    val requestId: String?,
    @ApiModelProperty(value = "操作人", required = false)
    val operatorUserId: Long?,
    @ApiModelProperty(value = "备注", required = false)
    val remark: String?,
    @ApiModelProperty(value = "创建时间", required = false)
    @field:JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Shanghai")
    val createdAt: Date?,
)

@ApiModel("用户配额转配结果")
data class UserQuotaTransferResult(
    @ApiModelProperty(value = "转出用户ID", required = true)
    val fromUserId: Long,
    @ApiModelProperty(value = "转入用户ID", required = true)
    val targetUserId: Long,
    @ApiModelProperty(value = "转配Token数量", required = true)
    val transferTokens: Long,
    @ApiModelProperty(value = "转出方最新账户快照", required = true)
    val fromAccount: UserQuotaAccountResult,
    @ApiModelProperty(value = "转入方最新账户快照", required = true)
    val targetAccount: UserQuotaAccountResult,
)
