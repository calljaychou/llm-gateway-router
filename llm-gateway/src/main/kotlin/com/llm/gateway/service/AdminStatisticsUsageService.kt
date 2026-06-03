package com.llm.gateway.service

import cn.hutool.core.date.DateUtil
import com.github.pagehelper.PageInfo
import com.github.pagehelper.page.PageMethod
import com.llm.gateway.dal.mapper.LlmUsageLogMapperExt
import com.llm.gateway.dal.mapper.LlmUsageTokenDetailDynamicSqlSupport
import com.llm.gateway.dal.mapper.LlmUsageTokenDetailMapper
import com.llm.gateway.dal.mapper.select
import com.llm.gateway.model.PageResult
import com.llm.gateway.model.dto.AdminStatisticsDepartmentUsageItemDto
import com.llm.gateway.model.dto.AdminStatisticsUsageLogItemDto
import com.llm.gateway.model.dto.AdminStatisticsUserUsageItemDto
import com.llm.gateway.model.params.AdminStatisticsDepartmentUsagePageParams
import com.llm.gateway.model.params.AdminStatisticsUsageLogPageParams
import com.llm.gateway.model.params.AdminStatisticsUserUsagePageParams
import com.llm.gateway.model.results.AdminStatisticsDepartmentUsageItemResult
import com.llm.gateway.model.results.AdminStatisticsUsageLogItemResult
import com.llm.gateway.model.results.AdminStatisticsUserUsageItemResult
import com.llm.gateway.tokencalc.model.TokenCacheType
import com.llm.gateway.tokencalc.model.TokenDirection
import java.util.Date
import org.springframework.stereotype.Service

@Service
class AdminStatisticsUsageService(
    private val llmUsageLogMapperExt: LlmUsageLogMapperExt,
    private val llmUsageTokenDetailMapper: LlmUsageTokenDetailMapper,
) {

    companion object {
        private const val RECENT_MONTH_DAYS = 30
    }

    fun listUsageLogs(params: AdminStatisticsUsageLogPageParams): PageResult<AdminStatisticsUsageLogItemResult> {
        val endDate = DateUtil.beginOfDay(params.endDate ?: Date())
        val startDate = DateUtil.beginOfDay(params.startDate ?: DateUtil.offsetDay(endDate, -(RECENT_MONTH_DAYS - 1)))
        val endTime = DateUtil.offsetDay(endDate, 1)

        PageMethod.startPage<AdminStatisticsUsageLogItemDto>(params.pageNum, params.pageSize)
        val records = llmUsageLogMapperExt.listAdminUsageLogs(
            startTime = startDate,
            endTime = endTime,
            userId = params.userId,
            userKeyword = normalizeKeyword(params.userKeyword),
            modelId = params.modelId,
            modelKeyword = normalizeKeyword(params.modelKeyword),
            vendorId = params.vendorId,
        )
        val pageInfo = PageInfo.of(records)
        val cachedTokenMap = listCachedTokenCounts(records)

        return PageResult(
            pageNum = pageInfo.pageNum,
            pageSize = pageInfo.pageSize,
            total = pageInfo.total,
            list = records.map { buildUsageLogItemResult(it, cachedTokenMap) },
        )
    }

    fun listDepartmentUsageStats(
        params: AdminStatisticsDepartmentUsagePageParams,
    ): PageResult<AdminStatisticsDepartmentUsageItemResult> {
        PageMethod.startPage<AdminStatisticsDepartmentUsageItemDto>(params.pageNum, params.pageSize)
        val records = llmUsageLogMapperExt.listAdminDepartmentUsageStats(normalizeKeyword(params.deptName))
        val pageInfo = PageInfo.of(records)

        return PageResult(
            pageNum = pageInfo.pageNum,
            pageSize = pageInfo.pageSize,
            total = pageInfo.total,
            list = records.map { buildDepartmentUsageItemResult(it) },
        )
    }

    fun listUserUsageStats(params: AdminStatisticsUserUsagePageParams): PageResult<AdminStatisticsUserUsageItemResult> {
        PageMethod.startPage<AdminStatisticsUserUsageItemDto>(params.pageNum, params.pageSize)
        val records = llmUsageLogMapperExt.listAdminUserUsageStats(
            nickname = normalizeKeyword(params.nickname),
            accountKeyword = normalizeKeyword(params.accountKeyword),
        )
        val pageInfo = PageInfo.of(records)

        return PageResult(
            pageNum = pageInfo.pageNum,
            pageSize = pageInfo.pageSize,
            total = pageInfo.total,
            list = records.map { buildUserUsageItemResult(it) },
        )
    }

    /**
     * 清洗分页筛选关键字。
     */
    private fun normalizeKeyword(keyword: String?): String? {
        return keyword?.trim()?.ifBlank { null }
    }

    /**
     * 构建管理端使用日志列表项。
     */
    private fun buildUsageLogItemResult(
        row: AdminStatisticsUsageLogItemDto,
        cachedTokenMap: Map<Long, Int>,
    ): AdminStatisticsUsageLogItemResult {
        return AdminStatisticsUsageLogItemResult(
            usageLogId = row.usageLogId,
            requestId = row.requestId,
            userId = row.userId,
            userName = row.userName,
            userAccount = row.userAccount,
            deptName = row.deptName,
            vendorId = row.vendorId,
            vendorName = row.vendorName,
            modelId = row.modelId,
            modelName = row.modelName,
            inputTokens = row.inputTokens,
            outputTokens = row.outputTokens,
            totalTokens = row.totalTokens,
            cachedTokens = cachedTokenMap[row.usageLogId] ?: 0,
            usedAt = row.usedAt,
            latencyMs = row.latencyMs,
        )
    }

    /**
     * 构建部门使用统计列表项。
     */
    private fun buildDepartmentUsageItemResult(
        row: AdminStatisticsDepartmentUsageItemDto,
    ): AdminStatisticsDepartmentUsageItemResult {
        return AdminStatisticsDepartmentUsageItemResult(
            deptId = row.deptId,
            deptName = row.deptName,
            usageCount = row.usageCount,
            totalTokens = row.totalTokens,
        )
    }

    /**
     * 构建用户使用统计列表项。
     */
    private fun buildUserUsageItemResult(row: AdminStatisticsUserUsageItemDto): AdminStatisticsUserUsageItemResult {
        return AdminStatisticsUserUsageItemResult(
            userId = row.userId,
            userName = row.userName,
            username = row.username,
            mobile = row.mobile,
            email = row.email,
            deptName = row.deptName,
            balance = row.balance,
            totalTokens = row.totalTokens,
            usageCount = row.usageCount,
        )
    }

    /**
     * 批量查询使用日志缓存Token。
     */
    private fun listCachedTokenCounts(records: List<AdminStatisticsUsageLogItemDto>): Map<Long, Int> {
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
