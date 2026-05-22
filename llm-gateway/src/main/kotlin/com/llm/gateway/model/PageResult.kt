package com.llm.gateway.model

import io.swagger.annotations.ApiModel
import io.swagger.annotations.ApiModelProperty

@ApiModel("通用分页结果")
data class PageResult<T>(
    @ApiModelProperty(value = "页码", required = true)
    val pageNum: Int,
    @ApiModelProperty(value = "每页数量", required = true)
    val pageSize: Int,
    @ApiModelProperty(value = "总数", required = true)
    val total: Long,
    @ApiModelProperty(value = "数据列表", required = true)
    val list: List<T>,
)