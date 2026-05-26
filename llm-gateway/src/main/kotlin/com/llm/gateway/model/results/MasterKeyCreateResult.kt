package com.llm.gateway.model.results

import io.swagger.annotations.ApiModel
import io.swagger.annotations.ApiModelProperty
import java.util.Date

@ApiModel("创建主密钥结果")
data class MasterKeyCreateResult(
    @ApiModelProperty(value = "主密钥ID", required = true)
    val masterKeyId: Long,
    @ApiModelProperty(value = "供应商ID", required = true)
    val vendorId: Long,
    @ApiModelProperty(value = "状态", required = true)
    val status: Int,
    @ApiModelProperty(value = "权重", required = true)
    val weight: Int,
    @ApiModelProperty(value = "密钥展示前缀", required = true)
    val keyPrefix: String,
)

@ApiModel("供应商主密钥列表项")
data class MasterKeyListItemResult(
    @ApiModelProperty(value = "主密钥ID", required = true)
    val masterKeyId: Long,
    @ApiModelProperty(value = "供应商ID", required = true)
    val vendorId: Long,
    @ApiModelProperty(value = "密钥安全指纹", required = true)
    val keyFingerprint: String,
    @ApiModelProperty(value = "权重", required = true)
    val weight: Int,
    @ApiModelProperty(value = "状态", required = true)
    val status: Int,
    @ApiModelProperty(value = "错误次数", required = true)
    val errorCount: Int,
    @ApiModelProperty(value = "最近检测时间")
    val lastCheckedAt: Date?,
    @ApiModelProperty(value = "创建时间")
    val createdTime: Date?,
    @ApiModelProperty(value = "更新时间")
    val updatedTime: Date?,
)

@ApiModel("供应商主密钥列表结果")
data class MasterKeyListResult(
    @ApiModelProperty(value = "供应商ID，为空表示查询全部")
    val vendorId: Long?,
    @ApiModelProperty(value = "主密钥列表", required = true)
    val keys: List<MasterKeyListItemResult>,
)
