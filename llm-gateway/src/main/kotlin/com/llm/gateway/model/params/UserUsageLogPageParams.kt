package com.llm.gateway.model.params

import com.fasterxml.jackson.annotation.JsonFormat
import com.llm.gateway.model.PageParams
import io.swagger.annotations.ApiModel
import io.swagger.annotations.ApiModelProperty
import java.util.Date
import org.springframework.format.annotation.DateTimeFormat

@ApiModel("用户使用日志分页参数")
class UserUsageLogPageParams : PageParams() {
    @ApiModelProperty(value = "开始日期，格式 yyyy-MM-dd", required = false, example = "2026-06-01")
    @field:JsonFormat(pattern = "yyyy-MM-dd", timezone = "Asia/Shanghai")
    @field:DateTimeFormat(pattern = "yyyy-MM-dd")
    var startDate: Date? = null

    @ApiModelProperty(value = "结束日期，格式 yyyy-MM-dd", required = false, example = "2026-06-03")
    @field:JsonFormat(pattern = "yyyy-MM-dd", timezone = "Asia/Shanghai")
    @field:DateTimeFormat(pattern = "yyyy-MM-dd")
    var endDate: Date? = null
}
