package com.llm.gateway.model

import io.swagger.annotations.ApiModel
import io.swagger.annotations.ApiModelProperty
import javax.validation.constraints.Min

@ApiModel("通用分页参数")
open class PageParams {
    @ApiModelProperty(value = "页码(从1开始)", required = false, example = "1")
    @field:Min(value = 1, message = "pageNum必须大于0")
    open var pageNum: Int = 1
        set(value) {
            field = when {
                value < 1 -> 1
                else -> value
            }
        }

    @ApiModelProperty(value = "每页大小(最大200)", required = false, example = "20")
    @field:Min(value = 1, message = "pageSize必须大于0")
    open var pageSize: Int = 20
        set(value) {
            field = when {
                value < 1 -> 20
                value > 200 -> 200
                else -> value
            }
        }
}