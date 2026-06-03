package com.llm.gateway.service

import cn.hutool.core.date.DateUtil
import com.llm.gateway.dal.mapper.LlmUsageLogMapperExt
import com.llm.gateway.model.dto.UserUsageHourlyCountDto
import com.llm.gateway.model.results.UserUsageHourlyHeatmapDayResult
import com.llm.gateway.model.results.UserUsageHourlyHeatmapHourResult
import com.llm.gateway.model.results.UserUsageHourlyHeatmapResult
import java.util.Date
import org.springframework.stereotype.Service

@Service
class UserUsageService(
    private val llmUsageLogMapperExt: LlmUsageLogMapperExt,
) {

    companion object {
        private const val RECENT_MONTH_DAYS = 30
        private const val HOURS_PER_DAY = 24
        private const val DATE_PATTERN = "yyyy-MM-dd"
    }

    fun getRecentMonthHourlyHeatmap(userId: Long): UserUsageHourlyHeatmapResult {
        val endDate = DateUtil.beginOfDay(Date())
        val startDate = DateUtil.offsetDay(endDate, -(RECENT_MONTH_DAYS - 1))
        val endTime = DateUtil.offsetDay(endDate, 1)
        val countMap = llmUsageLogMapperExt.countHourlyUsage(userId, startDate, endTime)
            .associateBy { buildCountKey(it.statDate, it.statHour) }

        val days = (0 until RECENT_MONTH_DAYS).map { dayIndex ->
            val currentDate = DateUtil.offsetDay(startDate, dayIndex)
            buildHeatmapDay(DateUtil.format(currentDate, DATE_PATTERN), countMap)
        }
        val totalCount = days.sumOf { it.totalCount }
        val maxHourlyCount = days.flatMap { it.hours }.maxOfOrNull { it.requestCount } ?: 0L

        return UserUsageHourlyHeatmapResult(
            startDate = DateUtil.format(startDate, DATE_PATTERN),
            endDate = DateUtil.format(endDate, DATE_PATTERN),
            totalCount = totalCount,
            maxHourlyCount = maxHourlyCount,
            days = days,
        )
    }

    /**
     * 构建单日24小时热图数据。
     */
    private fun buildHeatmapDay(
        date: String,
        countMap: Map<String, UserUsageHourlyCountDto>,
    ): UserUsageHourlyHeatmapDayResult {
        val hours = (0 until HOURS_PER_DAY).map { hour ->
            UserUsageHourlyHeatmapHourResult(
                hour = hour,
                requestCount = countMap[buildCountKey(date, hour)]?.requestCount ?: 0L,
            )
        }
        return UserUsageHourlyHeatmapDayResult(
            date = date,
            totalCount = hours.sumOf { it.requestCount },
            hours = hours,
        )
    }

    /**
     * 构建日期小时聚合查询结果的索引键。
     */
    private fun buildCountKey(date: String, hour: Int): String {
        return "$date:$hour"
    }
}
