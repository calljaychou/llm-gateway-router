package com.llm.gateway.service

import com.llm.gateway.common.enums.NormalStatus
import com.llm.gateway.common.exceptions.BizException
import com.llm.gateway.common.logger
import com.llm.gateway.dal.mapper.DepartmentDynamicSqlSupport
import com.llm.gateway.dal.mapper.DepartmentMapper
import com.llm.gateway.dal.mapper.UsersDynamicSqlSupport
import com.llm.gateway.dal.mapper.UsersMapper
import com.llm.gateway.dal.mapper.count
import com.llm.gateway.dal.mapper.insert
import com.llm.gateway.dal.mapper.select
import com.llm.gateway.dal.mapper.selectOne
import com.llm.gateway.dal.mapper.update
import com.llm.gateway.dal.mapper.updateByPrimaryKeySelective
import com.llm.gateway.dal.model.DepartmentRecord
import com.llm.gateway.model.params.DepartmentCreateParams
import com.llm.gateway.model.results.DepartmentCreateResult
import com.llm.gateway.model.results.DepartmentDeleteResult
import com.llm.gateway.model.results.DepartmentTreeResult
import java.util.Date
import java.util.concurrent.TimeUnit
import org.redisson.api.RedissonClient
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class DepartmentService(
    private val departmentMapper: DepartmentMapper,
    private val usersMapper: UsersMapper,
    private val redissonClient: RedissonClient,
) {

    private val log = logger()

    companion object {
        private const val DEPT_PATH_CACHE_PREFIX = "gateway:dept:path:"
        private const val ANCESTOR_PATH_CACHE_TTL_MINUTES = 30L
        private const val ROOT_PARENT_ID = 0L
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
    fun createDepartment(record: DepartmentRecord): Long {
        departmentMapper.insert(record)
        val newDeptId = record.id ?: throw BizException(BizException.SYSTEM_FAILED, "创建部门失败")
        evictAncestorPathCache(newDeptId)
        return newDeptId
    }

    /**
     * 新建一级部门，一级部门 parentId 固定为0。
     */
    @Transactional(rollbackFor = [Exception::class])
    fun createRootDepartment(params: DepartmentCreateParams): DepartmentCreateResult {
        return createDepartmentCore(ROOT_PARENT_ID, params)
    }

    /**
     * 在指定父部门下新增子部门。
     */
    @Transactional(rollbackFor = [Exception::class])
    fun createChildDepartment(parentDeptId: Long, params: DepartmentCreateParams): DepartmentCreateResult {
        val parentDepartment = ensureDeptExists(parentDeptId)
        if (parentDepartment.status != NormalStatus) {
            throw BizException(BizException.BUSINESS_FAILED, "父部门状态异常，无法新增子部门")
        }
        return createDepartmentCore(parentDeptId, params)
    }

    /**
     * 查询部门树，仅返回未删除且状态正常的部门。
     */
    fun getDepartmentTree(): List<DepartmentTreeResult> {
        val departments = getAllActiveDepartments()
            .sortedWith(compareBy<DepartmentRecord> { it.orderNum ?: 0 }.thenBy { it.id ?: Long.MAX_VALUE })
        val childrenMap = departments.groupBy { it.parentId ?: ROOT_PARENT_ID }

        return buildDepartmentTree(ROOT_PARENT_ID, childrenMap, mutableSetOf())
    }

    /**
     * 删除部门；存在子部门时禁止删除，存在用户时先解除用户部门归属。
     */
    @Transactional(rollbackFor = [Exception::class])
    fun deleteDepartment(deptId: Long): DepartmentDeleteResult {
        ensureDeptExists(deptId)
        checkDepartmentHasNoChildren(deptId)

        val now = Date()
        val unboundUserCount = unbindUsersFromDepartment(deptId, now)
        departmentMapper.update {
            set(DepartmentDynamicSqlSupport.Department.delFlag).equalTo(true)
            set(DepartmentDynamicSqlSupport.Department.updatedTime).equalTo(now)
            where { DepartmentDynamicSqlSupport.Department.id isEqualTo deptId }
            and { DepartmentDynamicSqlSupport.Department.delFlag isEqualTo false }
        }
        evictAncestorPathCache(deptId)
        log.info("删除部门成功 deptId={}, unboundUserCount={}", deptId, unboundUserCount)

        return DepartmentDeleteResult(
            deptId = deptId,
            deleted = true,
            unboundUserCount = unboundUserCount,
        )
    }

    /**
     * 查询指定部门及其所有后代部门ID。
     */
    fun listSubtreeDeptIds(deptId: Long): List<Long> {
        ensureDeptExists(deptId)
        return (listOf(deptId) + findDescendantDeptIds(deptId)).distinct()
    }

    /**
     * 校验部门下不存在未删除子部门。
     */
    private fun checkDepartmentHasNoChildren(deptId: Long) {
        val childCount = departmentMapper.count {
            where { DepartmentDynamicSqlSupport.Department.parentId isEqualTo deptId }
            and { DepartmentDynamicSqlSupport.Department.delFlag isEqualTo false }
        }
        if (childCount > 0L) {
            throw BizException(BizException.BUSINESS_FAILED, "部门下存在子部门，无法删除")
        }
    }

    /**
     * 解除用户与部门的归属关系，返回受影响用户数量。
     */
    private fun unbindUsersFromDepartment(deptId: Long, updatedTime: Date): Long {
        val userCount = usersMapper.count {
            where { UsersDynamicSqlSupport.Users.deptId isEqualTo deptId }
        }
        if (userCount == 0L) return 0L

        usersMapper.update {
            set(UsersDynamicSqlSupport.Users.deptId).equalToNull()
            set(UsersDynamicSqlSupport.Users.updatedTime).equalTo(updatedTime)
            where { UsersDynamicSqlSupport.Users.deptId isEqualTo deptId }
        }
        return userCount
    }

    /**
     * 创建部门核心逻辑，统一处理同级重名校验、默认字段与异常转换。
     */
    private fun createDepartmentCore(parentId: Long, params: DepartmentCreateParams): DepartmentCreateResult {
        val deptName = params.deptName.trim()
        checkSiblingDeptNameUnique(parentId, deptName)

        val now = Date()
        val record = DepartmentRecord(
            parentId = parentId,
            deptName = deptName,
            orderNum = params.orderNum ?: 0,
            leaderName = params.leaderName,
            tel = params.tel?.trim()?.takeIf { it.isNotBlank() },
            status = NormalStatus,
            delFlag = false,
            createdTime = now,
            updatedTime = now,
        )

        try {
            createDepartment(record)
        } catch (e: DataIntegrityViolationException) {
            log.warn("创建部门数据冲突 parentId={}, deptName={}", parentId, deptName, e)
            throw BizException(BizException.BUSINESS_FAILED, "同级部门名称已存在")
        }

        val deptId = record.id ?: throw BizException(BizException.SYSTEM_FAILED, "创建部门失败")
        log.info("创建部门成功 deptId={}, parentId={}, deptName={}", deptId, parentId, deptName)
        return DepartmentCreateResult(
            deptId = deptId,
            parentId = parentId,
            deptName = deptName,
        )
    }

    /**
     * 校验同一父部门下是否存在同名未删除部门。
     */
    private fun checkSiblingDeptNameUnique(parentId: Long, deptName: String) {
        val existed = if (parentId == ROOT_PARENT_ID) {
            getAllRootDepartmentCandidates().firstOrNull { it.deptName == deptName }
        } else {
            departmentMapper.selectOne {
                where { DepartmentDynamicSqlSupport.Department.parentId isEqualTo parentId }
                and { DepartmentDynamicSqlSupport.Department.deptName isEqualTo deptName }
                and { DepartmentDynamicSqlSupport.Department.delFlag isEqualTo false }
            }
        }
        if (existed != null) {
            throw BizException(BizException.BUSINESS_FAILED, "同级部门名称已存在")
        }
    }

    /**
     * 基于 parentId 分组递归构建部门树，并防御脏数据导致的循环引用。
     */
    private fun buildDepartmentTree(
        parentId: Long,
        childrenMap: Map<Long, List<DepartmentRecord>>,
        visitedDeptIds: MutableSet<Long>,
    ): List<DepartmentTreeResult> {
        return childrenMap[parentId].orEmpty().mapNotNull { department ->
            val deptId = department.id ?: return@mapNotNull null
            if (!visitedDeptIds.add(deptId)) {
                log.warn("部门树存在循环引用 deptId={}, parentId={}", deptId, parentId)
                return@mapNotNull null
            }

            DepartmentTreeResult(
                id = deptId,
                name = department.deptName.orEmpty(),
                parentId = department.parentId ?: ROOT_PARENT_ID,
                orderNum = department.orderNum ?: 0,
                leaderName= department.leaderName,
                tel = department.tel,
                status = department.status ?: NormalStatus,
                children = buildDepartmentTree(deptId, childrenMap, visitedDeptIds),
            ).also {
                visitedDeptIds.remove(deptId)
            }
        }
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

    /**
     * 删除指定部门的祖先路径缓存。
     */
    private fun evictAncestorPathCache(deptId: Long) {
        redissonClient.getBucket<String>("$DEPT_PATH_CACHE_PREFIX$deptId").delete()
    }

    /**
     * 查询所有未删除且状态正常的部门。
     */
    private fun getAllActiveDepartments(): List<DepartmentRecord> {
        return departmentMapper.select {
            where { DepartmentDynamicSqlSupport.Department.delFlag isEqualTo false }
            and { DepartmentDynamicSqlSupport.Department.status isEqualTo NormalStatus }
        }
    }

    /**
     * 查询一级部门候选数据，兼容历史 parentId 为空或为0的根部门记录。
     */
    private fun getAllRootDepartmentCandidates(): List<DepartmentRecord> {
        return departmentMapper.select {
            where { DepartmentDynamicSqlSupport.Department.delFlag isEqualTo false }
        }.filter { (it.parentId ?: ROOT_PARENT_ID) == ROOT_PARENT_ID }
    }

    /**
     * 解析缓存中的部门路径字符串。
     */
    private fun parsePathValue(pathValue: String?): List<Long> {
        if (pathValue.isNullOrBlank()) return emptyList()
        return pathValue.split("/").mapNotNull { it.toLongOrNull() }
    }
}
