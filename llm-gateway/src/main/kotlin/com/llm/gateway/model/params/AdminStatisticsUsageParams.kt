package com.llm.gateway.model.params

import com.fasterxml.jackson.annotation.JsonFormat
import com.llm.gateway.model.PageParams
import io.swagger.annotations.ApiModel
import io.swagger.annotations.ApiModelProperty
import java.util.Date
import org.springframework.format.annotation.DateTimeFormat

@ApiModel("管理端-使用日志分页参数")
class AdminStatisticsUsageLogPageParams : PageParams() {
    @ApiModelProperty(value = "开始日期，格式 yyyy-MM-dd", required = false, example = "2026-06-01")
    @field:JsonFormat(pattern = "yyyy-MM-dd", timezone = "Asia/Shanghai")
    @field:DateTimeFormat(pattern = "yyyy-MM-dd")
    var startDate: Date? = null

    @ApiModelProperty(value = "结束日期，格式 yyyy-MM-dd", required = false, example = "2026-06-03")
    @field:JsonFormat(pattern = "yyyy-MM-dd", timezone = "Asia/Shanghai")
    @field:DateTimeFormat(pattern = "yyyy-MM-dd")
    var endDate: Date? = null

    @ApiModelProperty(value = "用户ID", required = false, example = "1001")
    var userId: Long? = null

    @ApiModelProperty(value = "用户关键字，匹配姓名/用户名/手机号/邮箱", required = false, example = "alice")
    var userKeyword: String? = null

    @ApiModelProperty(value = "模型ID", required = false, example = "1")
    var modelId: Long? = null

    @ApiModelProperty(value = "模型关键字，匹配模型别名/真实模型名/请求模型名", required = false, example = "gpt")
    var modelKeyword: String? = null

    @ApiModelProperty(value = "供应商ID", required = false, example = "1")
    var vendorId: Long? = null
}

@ApiModel("管理端-部门使用统计分页参数")
class AdminStatisticsDepartmentUsagePageParams : PageParams() {
    @ApiModelProperty(value = "部门名称，支持模糊查询", required = false, example = "研发")
    var deptName: String? = null
}

@ApiModel("管理端-用户使用统计分页参数")
class AdminStatisticsUserUsagePageParams : PageParams() {
    @ApiModelProperty(value = "用户昵称/姓名，支持模糊查询", required = false, example = "张三")
    var nickname: String? = null

    @ApiModelProperty(value = "用户账号，匹配手机号/用户名/邮箱", required = false, example = "alice")
    var accountKeyword: String? = null
}
