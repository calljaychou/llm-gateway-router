package com.llm.gateway.service

import cn.hutool.core.date.DateUtil
import com.github.pagehelper.PageInfo
import com.github.pagehelper.page.PageMethod
import com.llm.gateway.dal.mapper.ModelsDynamicSqlSupport
import com.llm.gateway.dal.mapper.ModelsMapper
import com.llm.gateway.dal.mapper.LlmUsageTokenDetailDynamicSqlSupport
import com.llm.gateway.dal.mapper.LlmUsageTokenDetailMapper
import com.llm.gateway.dal.mapper.LlmUsageLogMapperExt
import com.llm.gateway.dal.mapper.VendorsDynamicSqlSupport
import com.llm.gateway.dal.mapper.VendorsMapper
import com.llm.gateway.dal.mapper.select
import com.llm.gateway.dal.model.ModelsRecord
import com.llm.gateway.dal.model.VendorsRecord
import com.llm.gateway.model.dto.UserUsageHourlyCountDto
import com.llm.gateway.model.dto.UserUsageLogListItemDto
import com.llm.gateway.model.dto.UserUsageModelCountDto
import com.llm.gateway.model.PageResult
import com.llm.gateway.model.params.UserUsageLogPageParams
import com.llm.gateway.model.results.UserUsageLogListItemResult
import com.llm.gateway.model.results.UserModelUsageCountResult
import com.llm.gateway.model.results.UserUsageHourlyHeatmapDayResult
import com.llm.gateway.model.results.UserUsageHourlyHeatmapHourResult
import com.llm.gateway.model.results.UserUsageHourlyHeatmapResult
import com.llm.gateway.tokencalc.model.TokenCacheType
import com.llm.gateway.tokencalc.model.TokenDirection
import java.util.Date
import org.springframework.stereotype.Service

@Service
class UserUsageService(
    private val llmUsageLogMapperExt: LlmUsageLogMapperExt,
    private val llmUsageTokenDetailMapper: LlmUsageTokenDetailMapper,
    private val modelsMapper: ModelsMapper,
    private val vendorsMapper: VendorsMapper,
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

    fun listModelUsageCounts(userId: Long): List<UserModelUsageCountResult> {
        return llmUsageLogMapperExt.countModelUsage(userId).map { buildModelUsageCountResult(it) }
    }

    fun listUsageLogs(userId: Long, params: UserUsageLogPageParams): PageResult<UserUsageLogListItemResult> {
        val endDate = DateUtil.beginOfDay(params.endDate ?: Date())
        val startDate = DateUtil.beginOfDay(params.startDate ?: DateUtil.offsetDay(endDate, -(RECENT_MONTH_DAYS - 1)))
        val endTime = DateUtil.offsetDay(endDate, 1)

        PageMethod.startPage<UserUsageLogListItemDto>(params.pageNum, params.pageSize)
        val records = llmUsageLogMapperExt.listUsageLogs(userId, startDate, endTime)
        val pageInfo = PageInfo.of(records)
        val modelMap = listModels(records)
        val vendorMap = listVendors(records)
        val cachedTokenMap = listCachedTokenCounts(records)

        return PageResult(
            pageNum = pageInfo.pageNum,
            pageSize = pageInfo.pageSize,
            total = pageInfo.total,
            list = records.map { buildUsageLogListItemResult(it, modelMap, vendorMap, cachedTokenMap) },
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

    /**
     * 构建模型维度使用次数统计结果。
     */
    private fun buildModelUsageCountResult(row: UserUsageModelCountDto): UserModelUsageCountResult {
        return UserModelUsageCountResult(
            modelId = row.modelId,
            modelName = row.modelName,
            vendorId = row.vendorId,
            usageCount = row.usageCount,
        )
    }

    /**
     * 构建使用日志列表项。
     */
    private fun buildUsageLogListItemResult(
        row: UserUsageLogListItemDto,
        modelMap: Map<Long, ModelsRecord>,
        vendorMap: Map<Long, VendorsRecord>,
        cachedTokenMap: Map<Long, Int>,
    ): UserUsageLogListItemResult {
        val model = row.modelId?.let { modelMap[it] }
        return UserUsageLogListItemResult(
            usageLogId = row.usageLogId,
            requestId = row.requestId,
            vendorName = row.vendorId?.let { vendorMap[it]?.name }.orEmpty(),
            modelName = model?.modelAlias ?: row.resolvedModel ?: row.requestModel.orEmpty(),
            inputTokens = row.inputTokens,
            outputTokens = row.outputTokens,
            totalTokens = row.totalTokens,
            cachedTokens = cachedTokenMap[row.usageLogId] ?: 0,
            usedAt = row.usedAt,
            latencyMs = row.latencyMs,
        )
    }

    /**
     * 批量查询使用日志关联模型。
     */
    private fun listModels(records: List<UserUsageLogListItemDto>): Map<Long, ModelsRecord> {
        val modelIds = records.mapNotNull { it.modelId }.distinct()
        if (modelIds.isEmpty()) return emptyMap()
        return modelsMapper.select {
            where { ModelsDynamicSqlSupport.Models.id isIn modelIds }
        }.mapNotNull { model -> model.id?.let { it to model } }.toMap()
    }

    /**
     * 批量查询使用日志关联供应商。
     */
    private fun listVendors(records: List<UserUsageLogListItemDto>): Map<Long, VendorsRecord> {
        val vendorIds = records.mapNotNull { it.vendorId }.distinct()
        if (vendorIds.isEmpty()) return emptyMap()
        return vendorsMapper.select {
            where { VendorsDynamicSqlSupport.Vendors.id isIn vendorIds }
        }.mapNotNull { vendor -> vendor.id?.let { it to vendor } }.toMap()
    }

    /**
     * 批量查询使用日志缓存Token。
     */
    private fun listCachedTokenCounts(records: List<UserUsageLogListItemDto>): Map<Long, Int> {
        val usageLogIds = records.map { it.usageLogId }.distinct()
        if (usageLogIds.isEmpty()) return emptyMap()
        return llmUsageTokenDetailMapper.select {
            where { LlmUsageTokenDetailDynamicSqlSupport.LlmUsageTokenDetail.usageLogId isIn usageLogIds }
            and { LlmUsageTokenDetailDynamicSqlSupport.LlmUsageTokenDetail.tokenDirection isEqualTo TokenDirection.INPUT.value }
            and { LlmUsageTokenDetailDynamicSqlSupport.LlmUsageTokenDetail.cacheType isEqualTo TokenCacheType.CACHE_HIT.value }
        }.groupBy { it.usageLogId!! }
            .mapValues { (_, details) -> details.sumOf { it.tokens ?: 0 } }
    }
}
