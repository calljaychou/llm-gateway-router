package com.llm.gateway.service

import com.llm.gateway.common.enums.NormalStatus
import com.llm.gateway.common.exceptions.BizException
import com.llm.gateway.dal.mapper.DepartmentDynamicSqlSupport
import com.llm.gateway.dal.mapper.DepartmentMapper
import com.llm.gateway.dal.mapper.insert
import com.llm.gateway.dal.mapper.select
import com.llm.gateway.dal.mapper.selectOne
import com.llm.gateway.dal.mapper.updateByPrimaryKeySelective
import com.llm.gateway.dal.model.DepartmentRecord
import java.util.concurrent.TimeUnit
import org.redisson.api.RedissonClient
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class DepartmentService(
    private val departmentMapper: DepartmentMapper,
    private val redissonClient: RedissonClient,
) {

    companion object {
        private const val DEPT_PATH_CACHE_PREFIX = "gateway:dept:path:"
        private const val ANCESTOR_PATH_CACHE_TTL_MINUTES = 30L
    }

    /**
     * 校验部门是否存在且未删除，不满足条件抛出业务异常。
     */
    fun ensureDeptExists(deptId: Long): DepartmentRecord {
        return departmentMapper.selectOne {
            where { DepartmentDynamicSqlSupport.Department.id isEqualTo deptId }
            and { DepartmentDynamicSqlSupport.Department.delFlag isEqualTo false }
        } ?: throw BizException(BizException.BUSINESS_FAILED, "部门不存在")
    }

    /**
     * 获取从根到当前部门的祖先路径，优先读取部门路径缓存。
     * 缓存格式：key=deptId, value=1/100/200（根到当前的ID路径）。
     */
    fun getAncestorPath(deptId: Long): List<Long> {
        if (deptId <= 0L) return emptyList()

        val cacheKey = "$DEPT_PATH_CACHE_PREFIX$deptId"
        val cachedPath = parsePathValue(redissonClient.getBucket<String>(cacheKey).get())
        if (cachedPath.isNotEmpty()) {
            return cachedPath
        }

        val allDepartments = getAllActiveDepartments()
        val path = resolveAncestorPathFromMemory(deptId, allDepartments)
        if (path.isNotEmpty()) {
            redissonClient.getBucket<String>(cacheKey).set(
                path.joinToString("/"), ANCESTOR_PATH_CACHE_TTL_MINUTES, TimeUnit.MINUTES
            )
        }
        return path
    }

    /**
     * 创建部门并处理相关缓存失效。
     */
    @Transactional(rollbackFor = [Exception::class])
    fun createDepartment(record: DepartmentRecord): Long {
        departmentMapper.insert(record)
        val newDeptId = record.id ?: throw BizException(BizException.SYSTEM_FAILED, "创建部门失败")
        evictAncestorPathCache(newDeptId)
        return newDeptId
    }

    /**
     * 更新部门并按子树范围失效祖先路径缓存。
     */
    @Transactional(rollbackFor = [Exception::class])
    fun updateDepartment(record: DepartmentRecord) {
        val deptId = record.id ?: throw BizException(BizException.BUSINESS_FAILED, "部门ID不能为空")
        ensureDeptExists(deptId)
        departmentMapper.updateByPrimaryKeySelective(record)
        evictAncestorPathCacheForSubtree(deptId)
    }

    /**
     * 供外部在部门结构变更后主动触发缓存失效。
     */
    fun evictAncestorPathCacheForSubtree(deptId: Long) {
        val descendantIds = findDescendantDeptIds(deptId) + deptId
        descendantIds.distinct().forEach { evictAncestorPathCache(it) }
    }

    /**
     * 使用 parent 索引在内存中回溯父节点，组装根到当前节点路径。
     */
    private fun resolveAncestorPathFromMemory(startDeptId: Long, allDepartments: List<DepartmentRecord>): List<Long> {
        val parentByDeptId = allDepartments.mapNotNull { dept ->
            val deptId = dept.id ?: return@mapNotNull null
            deptId to (dept.parentId ?: 0L)
        }.toMap()
        if (parentByDeptId.isEmpty()) return emptyList()

        val path = ArrayDeque<Long>()
        val visited = mutableSetOf<Long>()
        var cursor: Long? = startDeptId

        while (cursor != null && cursor > 0L && visited.add(cursor)) {
            val currentDeptId = cursor
            if (!parentByDeptId.containsKey(currentDeptId)) break
            path.addFirst(currentDeptId)
            cursor = parentByDeptId[currentDeptId]
        }
        return path.toList()
    }

    /**
     * 找到指定部门的所有后代部门ID，用于缓存批量失效。
     */
    private fun findDescendantDeptIds(deptId: Long): List<Long> {
        val allDepartments = getAllActiveDepartments()
        if (allDepartments.isEmpty()) return emptyList()

        val childrenMap = allDepartments.groupBy { it.parentId ?: 0L }

        val descendants = mutableListOf<Long>()
        val queue = ArrayDeque<Long>()
        queue.add(deptId)

        while (queue.isNotEmpty()) {
            val current = queue.removeFirst()
            val children = childrenMap[current].orEmpty()
            children.forEach { child ->
                val childId = child.id ?: return@forEach
                descendants.add(childId)
                queue.add(childId)
            }
        }
        return descendants
    }

    private fun evictAncestorPathCache(deptId: Long) {
        redissonClient.getBucket<String>("$DEPT_PATH_CACHE_PREFIX$deptId").delete()
    }

    private fun getAllActiveDepartments(): List<DepartmentRecord> {
        return departmentMapper.select {
            where { DepartmentDynamicSqlSupport.Department.delFlag isEqualTo false }
            and { DepartmentDynamicSqlSupport.Department.status isEqualTo NormalStatus }
        }
    }

    private fun parsePathValue(pathValue: String?): List<Long> {
        if (pathValue.isNullOrBlank()) return emptyList()
        return pathValue.split("/").mapNotNull { it.toLongOrNull() }
    }
}
