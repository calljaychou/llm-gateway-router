package com.llm.gateway.service

import com.github.pagehelper.PageInfo
import com.github.pagehelper.page.PageMethod
import com.llm.gateway.common.enums.NormalStatus
import com.llm.gateway.common.exceptions.BizException
import com.llm.gateway.common.logger
import com.llm.gateway.common.util.transactionActiveAfterCommit
import com.llm.gateway.dal.mapper.DepartmentQuotasDynamicSqlSupport
import com.llm.gateway.dal.mapper.DepartmentQuotasMapper
import com.llm.gateway.dal.mapper.insert
import com.llm.gateway.dal.mapper.select
import com.llm.gateway.dal.mapper.selectOne
import com.llm.gateway.dal.mapper.update
import com.llm.gateway.dal.model.DepartmentQuotasRecord
import com.llm.gateway.model.params.AdminDepartmentQuotaCreateParams
import com.llm.gateway.model.params.AdminDepartmentQuotaPageParams
import com.llm.gateway.model.params.AdminDepartmentQuotaStatusUpdateParams
import com.llm.gateway.model.params.AdminDepartmentQuotaUpdateParams
import com.llm.gateway.model.results.AdminDepartmentQuotaItemResult
import com.llm.gateway.model.results.AdminDepartmentQuotaOperateResult
import com.llm.gateway.model.results.AdminDepartmentQuotaPageResult
import java.util.Date
import org.redisson.api.RedissonClient
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class AdminDepartmentQuotaService(
    private val departmentQuotasMapper: DepartmentQuotasMapper,
    private val departmentService: DepartmentService,
    private val redissonClient: RedissonClient,
) {
    private val log = logger()

    companion object {
        private const val QUOTA_CACHE_KEY_PREFIX = "quota:dept:"
    }

    fun pageDepartmentQuotas(
        params: AdminDepartmentQuotaPageParams,
    ): AdminDepartmentQuotaPageResult {
        PageMethod.startPage<DepartmentQuotasRecord>(params.pageNum, params.pageSize)
        val records = departmentQuotasMapper.select {
            where { DepartmentQuotasDynamicSqlSupport.DepartmentQuotas.deptId isEqualToWhenPresent params.deptId }
            and { DepartmentQuotasDynamicSqlSupport.DepartmentQuotas.period isEqualToWhenPresent params.period?.value }
            and { DepartmentQuotasDynamicSqlSupport.DepartmentQuotas.status isEqualToWhenPresent params.status }
            orderBy(
                DepartmentQuotasDynamicSqlSupport.DepartmentQuotas.updatedAt.descending(),
                DepartmentQuotasDynamicSqlSupport.DepartmentQuotas.id.descending()
            )
        }
        val pageInfo = PageInfo.of(records)
        return AdminDepartmentQuotaPageResult(
            pageNum = pageInfo.pageNum,
            pageSize = pageInfo.pageSize,
            total = pageInfo.total,
            records = pageInfo.list.map { it.toResult() },
        )
    }

    fun getDepartmentQuotaByDeptId(deptId: Long): AdminDepartmentQuotaItemResult {
        departmentService.ensureDeptExists(deptId)
        val record = departmentQuotasMapper.selectOne {
            where { DepartmentQuotasDynamicSqlSupport.DepartmentQuotas.deptId isEqualTo deptId }
        } ?: throw BizException(BizException.BUSINESS_FAILED, "策略不存在")
        return record.toResult()
    }

    @Transactional(rollbackFor = [Exception::class])
    fun createDepartmentQuota(params: AdminDepartmentQuotaCreateParams): AdminDepartmentQuotaOperateResult {
        val deptId = params.deptId
        departmentService.ensureDeptExists(deptId)
        val status = params.status ?: NormalStatus

        val existed = departmentQuotasMapper.selectOne {
            where { DepartmentQuotasDynamicSqlSupport.DepartmentQuotas.deptId isEqualTo deptId }
        }
        if (existed != null) {
            throw BizException(BizException.BUSINESS_FAILED, "部门策略已存在，禁止重复创建")
        }

        val now = Date()
        val record = DepartmentQuotasRecord(
            deptId = deptId,
            quotaTokens = params.quotaTokens,
            period = params.period.value,
            status = status,
            remark = params.remark?.trim(),
            createdAt = now,
            updatedAt = now,
        )
        try {
            departmentQuotasMapper.insert(record)
        } catch (e: DataIntegrityViolationException) {
            throw mapInsertException(e)
        }
        evictQuotaCacheSafelyAfterCommit(deptId)
        // 写后回读，确保返回数据库最终值（如DB时间精度处理后的updatedAt）
        val latest = loadQuotaByDeptIdOrThrow(deptId)
        return AdminDepartmentQuotaOperateResult(
            deptId = deptId,
            id = latest.id!!,
            updatedAt = latest.updatedAt,
        )
    }

    @Transactional(rollbackFor = [Exception::class])
    fun updateDepartmentQuota(
        deptId: Long,
        params: AdminDepartmentQuotaUpdateParams,
    ): AdminDepartmentQuotaOperateResult {
        departmentService.ensureDeptExists(deptId)
        val existed = departmentQuotasMapper.selectOne {
            where { DepartmentQuotasDynamicSqlSupport.DepartmentQuotas.deptId isEqualTo deptId }
        } ?: throw BizException(BizException.BUSINESS_FAILED, "策略不存在")

        if (existed.updatedAt != params.expectedUpdatedAt) {
            throw BizException(BizException.BUSINESS_FAILED, "并发更新冲突，请刷新后重试")
        }

        val now = Date()
        val updatedRows = departmentQuotasMapper.update {
            set(DepartmentQuotasDynamicSqlSupport.DepartmentQuotas.quotaTokens) equalTo params.quotaTokens
            set(DepartmentQuotasDynamicSqlSupport.DepartmentQuotas.period) equalTo params.period.value
            set(DepartmentQuotasDynamicSqlSupport.DepartmentQuotas.status) equalTo params.status
            set(DepartmentQuotasDynamicSqlSupport.DepartmentQuotas.remark) equalTo params.remark?.trim()
            set(DepartmentQuotasDynamicSqlSupport.DepartmentQuotas.updatedAt) equalTo now
            where { DepartmentQuotasDynamicSqlSupport.DepartmentQuotas.deptId isEqualTo deptId }
            and { DepartmentQuotasDynamicSqlSupport.DepartmentQuotas.updatedAt isEqualTo params.expectedUpdatedAt }
        }
        if (updatedRows == 0) {
            throw BizException(BizException.BUSINESS_FAILED, "并发更新冲突，请刷新后重试")
        }

        evictQuotaCacheSafelyAfterCommit(deptId)
        // 写后回读，避免直接返回应用侧时间导致并发版本不一致
        val latest = loadQuotaByDeptIdOrThrow(deptId)
        return AdminDepartmentQuotaOperateResult(
            deptId = deptId,
            id = latest.id!!,
            updatedAt = latest.updatedAt,
        )
    }

    @Transactional(rollbackFor = [Exception::class])
    fun updateDepartmentQuotaStatus(
        deptId: Long,
        params: AdminDepartmentQuotaStatusUpdateParams,
    ): AdminDepartmentQuotaOperateResult {
        departmentService.ensureDeptExists(deptId)
        val existed = departmentQuotasMapper.selectOne {
            where { DepartmentQuotasDynamicSqlSupport.DepartmentQuotas.deptId isEqualTo deptId }
        } ?: throw BizException(BizException.BUSINESS_FAILED, "策略不存在")

        if (existed.updatedAt != params.expectedUpdatedAt) {
            throw BizException(BizException.BUSINESS_FAILED, "并发更新冲突，请刷新后重试")
        }

        val now = Date()
        val updatedRows = departmentQuotasMapper.update {
            set(DepartmentQuotasDynamicSqlSupport.DepartmentQuotas.status).equalTo(params.status)
            set(DepartmentQuotasDynamicSqlSupport.DepartmentQuotas.updatedAt).equalTo(now)
            where { DepartmentQuotasDynamicSqlSupport.DepartmentQuotas.deptId isEqualTo deptId }
            and { DepartmentQuotasDynamicSqlSupport.DepartmentQuotas.updatedAt isEqualTo params.expectedUpdatedAt }
        }
        if (updatedRows == 0) {
            throw BizException(BizException.BUSINESS_FAILED, "并发更新冲突，请刷新后重试")
        }

        evictQuotaCacheSafelyAfterCommit(deptId)
        // 写后回读，统一以数据库记录作为返回与后续并发控制基准
        val latest = loadQuotaByDeptIdOrThrow(deptId)

        return AdminDepartmentQuotaOperateResult(
            deptId = deptId,
            id = latest.id!!,
            updatedAt = latest.updatedAt,
        )
    }

    /**
     * 将唯一键冲突映射为 4102，其他异常统一视为系统异常。
     */
    private fun mapInsertException(e: DataIntegrityViolationException): BizException {
        val message = e.message.orEmpty().lowercase()
        return if (message.contains("uk_dept_id") || message.contains("duplicate")) {
            BizException(BizException.SYSTEM_FAILED, "部门策略已存在，禁止重复创建")
        } else {
            BizException(BizException.SYSTEM_FAILED, "创建部门配额失败")
        }
    }

    /**
     * 配额策略变更后删除部门缓存，保证下一次请求读取到新配置。
     */
    private fun evictQuotaCacheSafelyAfterCommit(deptId: Long) {
        transactionActiveAfterCommit {
            runCatching {
                redissonClient.getBucket<String>("$QUOTA_CACHE_KEY_PREFIX$deptId").delete()
            }.onFailure { ex ->
                log.warn("删除部门配额缓存失败, deptId={}", deptId, ex)
            }
        }
    }

    private fun loadQuotaByDeptIdOrThrow(deptId: Long): DepartmentQuotasRecord {
        return departmentQuotasMapper.selectOne {
            where { DepartmentQuotasDynamicSqlSupport.DepartmentQuotas.deptId isEqualTo deptId }
        } ?: throw BizException(BizException.SYSTEM_FAILED, "策略不存在")
    }

    /**
     * 把数据库记录映射为返回对象，统一转换时间戳字段。
     */
    private fun DepartmentQuotasRecord.toResult(): AdminDepartmentQuotaItemResult {
        return AdminDepartmentQuotaItemResult(
            id = id ?: 0L,
            deptId = deptId ?: 0L,
            quotaTokens = quotaTokens ?: 0L,
            period = period.orEmpty(),
            status = status ?: 0,
            remark = remark,
            createdAt = createdAt,
            updatedAt = updatedAt,
        )
    }
}
