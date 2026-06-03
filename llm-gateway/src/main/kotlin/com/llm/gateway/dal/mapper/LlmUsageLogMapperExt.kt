package com.llm.gateway.dal.mapper

import com.llm.gateway.model.dto.UserUsageHourlyCountDto
import com.llm.gateway.model.dto.UserUsageLogListItemDto
import com.llm.gateway.model.dto.UserUsageModelCountDto
import com.llm.gateway.model.dto.AdminStatisticsDepartmentUsageItemDto
import com.llm.gateway.model.dto.AdminStatisticsUsageLogItemDto
import com.llm.gateway.model.dto.AdminStatisticsUserUsageItemDto
import java.util.Date
import org.apache.ibatis.annotations.Mapper
import org.apache.ibatis.annotations.Param
import org.apache.ibatis.annotations.Result
import org.apache.ibatis.annotations.Results
import org.apache.ibatis.annotations.Select
import org.apache.ibatis.type.JdbcType

@Mapper
interface LlmUsageLogMapperExt : LlmUsageLogMapper {

    @Select(
        """
        SELECT
            DATE_FORMAT(created_at, '%Y-%m-%d') AS stat_date,
            HOUR(created_at) AS stat_hour,
            COUNT(1) AS request_count
        FROM llm_usage_log
        WHERE user_id = #{userId}
          AND created_at >= #{startTime}
          AND created_at < #{endTime}
        GROUP BY stat_date, stat_hour
        ORDER BY stat_date ASC, stat_hour ASC
        """
    )
    @Results(
        id = "UserUsageHourlyCountDtoResult",
        value = [
            Result(column = "stat_date", property = "statDate", jdbcType = JdbcType.VARCHAR),
            Result(column = "stat_hour", property = "statHour", jdbcType = JdbcType.INTEGER),
            Result(column = "request_count", property = "requestCount", jdbcType = JdbcType.BIGINT),
        ]
    )
    fun countHourlyUsage(
        @Param("userId") userId: Long,
        @Param("startTime") startTime: Date,
        @Param("endTime") endTime: Date,
    ): List<UserUsageHourlyCountDto>

    @Select(
        """
        SELECT
            l.model_id AS model_id,
            COALESCE(m.model_alias, l.resolved_model, l.request_model, '') AS model_name,
            COALESCE(m.vendor_id, l.vendor_id, 0) AS vendor_id,
            COUNT(1) AS usage_count
        FROM llm_usage_log l
        LEFT JOIN models m ON m.id = l.model_id
        WHERE l.user_id = #{userId}
          AND l.model_id IS NOT NULL
        GROUP BY l.model_id, model_name, vendor_id
        ORDER BY usage_count DESC, l.model_id ASC
        """
    )
    @Results(
        id = "UserUsageModelCountDtoResult",
        value = [
            Result(column = "model_id", property = "modelId", jdbcType = JdbcType.BIGINT),
            Result(column = "model_name", property = "modelName", jdbcType = JdbcType.VARCHAR),
            Result(column = "vendor_id", property = "vendorId", jdbcType = JdbcType.BIGINT),
            Result(column = "usage_count", property = "usageCount", jdbcType = JdbcType.BIGINT),
        ]
    )
    fun countModelUsage(
        @Param("userId") userId: Long,
    ): List<UserUsageModelCountDto>

    @Select(
        """
        SELECT
            id AS usage_log_id,
            request_id AS request_id,
            vendor_id AS vendor_id,
            model_id AS model_id,
            request_model AS request_model,
            resolved_model AS resolved_model,
            COALESCE(input_tokens, 0) AS input_tokens,
            COALESCE(output_tokens, 0) AS output_tokens,
            COALESCE(total_tokens, 0) AS total_tokens,
            COALESCE(request_started_at, created_at) AS used_at,
            COALESCE(latency_ms, 0) AS latency_ms
        FROM llm_usage_log
        WHERE user_id = #{userId}
          AND COALESCE(request_started_at, created_at) >= #{startTime}
          AND COALESCE(request_started_at, created_at) < #{endTime}
        ORDER BY used_at DESC, id DESC
        """
    )
    @Results(
        id = "UserUsageLogListItemDtoResult",
        value = [
            Result(column = "usage_log_id", property = "usageLogId", jdbcType = JdbcType.BIGINT),
            Result(column = "request_id", property = "requestId", jdbcType = JdbcType.VARCHAR),
            Result(column = "vendor_id", property = "vendorId", jdbcType = JdbcType.BIGINT),
            Result(column = "model_id", property = "modelId", jdbcType = JdbcType.BIGINT),
            Result(column = "request_model", property = "requestModel", jdbcType = JdbcType.VARCHAR),
            Result(column = "resolved_model", property = "resolvedModel", jdbcType = JdbcType.VARCHAR),
            Result(column = "input_tokens", property = "inputTokens", jdbcType = JdbcType.INTEGER),
            Result(column = "output_tokens", property = "outputTokens", jdbcType = JdbcType.INTEGER),
            Result(column = "total_tokens", property = "totalTokens", jdbcType = JdbcType.INTEGER),
            Result(column = "used_at", property = "usedAt", jdbcType = JdbcType.TIMESTAMP),
            Result(column = "latency_ms", property = "latencyMs", jdbcType = JdbcType.INTEGER),
        ]
    )
    fun listUsageLogs(
        @Param("userId") userId: Long,
        @Param("startTime") startTime: Date,
        @Param("endTime") endTime: Date,
    ): List<UserUsageLogListItemDto>

    @Select(
        """
        <script>
        SELECT
            l.id AS usage_log_id,
            l.request_id AS request_id,
            l.user_id AS user_id,
            COALESCE(u.name, '') AS user_name,
            COALESCE(u.username, u.mobile, u.email, '') AS user_account,
            COALESCE(d.dept_name, '') AS dept_name,
            l.vendor_id AS vendor_id,
            COALESCE(v.name, '') AS vendor_name,
            l.model_id AS model_id,
            COALESCE(m.model_alias, m.real_model_name, l.resolved_model, l.request_model, '') AS model_name,
            COALESCE(l.input_tokens, 0) AS input_tokens,
            COALESCE(l.output_tokens, 0) AS output_tokens,
            COALESCE(l.total_tokens, 0) AS total_tokens,
            COALESCE(l.request_started_at, l.created_at) AS used_at,
            COALESCE(l.latency_ms, 0) AS latency_ms
        FROM llm_usage_log l
        LEFT JOIN users u ON u.id = l.user_id AND u.del_flag = 0
        LEFT JOIN department d ON d.id = COALESCE(l.dept_id, u.dept_id) AND d.del_flag = 0
        LEFT JOIN vendors v ON v.id = l.vendor_id
        LEFT JOIN models m ON m.id = l.model_id
        WHERE 1 = 1
        <if test="startTime != null">
          AND COALESCE(l.request_started_at, l.created_at) <![CDATA[>=]]> #{startTime}
        </if>
        <if test="endTime != null">
          AND COALESCE(l.request_started_at, l.created_at) <![CDATA[<]]> #{endTime}
        </if>
        <if test="userId != null">
          AND l.user_id = #{userId}
        </if>
        <if test="userKeyword != null and userKeyword != ''">
          AND (
            u.name LIKE CONCAT('%', #{userKeyword}, '%')
            OR u.username LIKE CONCAT('%', #{userKeyword}, '%')
            OR u.mobile LIKE CONCAT('%', #{userKeyword}, '%')
            OR u.email LIKE CONCAT('%', #{userKeyword}, '%')
          )
        </if>
        <if test="modelId != null">
          AND l.model_id = #{modelId}
        </if>
        <if test="modelKeyword != null and modelKeyword != ''">
          AND (
            m.model_alias LIKE CONCAT('%', #{modelKeyword}, '%')
            OR m.real_model_name LIKE CONCAT('%', #{modelKeyword}, '%')
            OR l.resolved_model LIKE CONCAT('%', #{modelKeyword}, '%')
            OR l.request_model LIKE CONCAT('%', #{modelKeyword}, '%')
          )
        </if>
        <if test="vendorId != null">
          AND l.vendor_id = #{vendorId}
        </if>
        ORDER BY used_at DESC, l.id DESC
        </script>
        """
    )
    @Results(
        id = "AdminStatisticsUsageLogItemDtoResult",
        value = [
            Result(column = "usage_log_id", property = "usageLogId", jdbcType = JdbcType.BIGINT),
            Result(column = "request_id", property = "requestId", jdbcType = JdbcType.VARCHAR),
            Result(column = "user_id", property = "userId", jdbcType = JdbcType.BIGINT),
            Result(column = "user_name", property = "userName", jdbcType = JdbcType.VARCHAR),
            Result(column = "user_account", property = "userAccount", jdbcType = JdbcType.VARCHAR),
            Result(column = "dept_name", property = "deptName", jdbcType = JdbcType.VARCHAR),
            Result(column = "vendor_id", property = "vendorId", jdbcType = JdbcType.BIGINT),
            Result(column = "vendor_name", property = "vendorName", jdbcType = JdbcType.VARCHAR),
            Result(column = "model_id", property = "modelId", jdbcType = JdbcType.BIGINT),
            Result(column = "model_name", property = "modelName", jdbcType = JdbcType.VARCHAR),
            Result(column = "input_tokens", property = "inputTokens", jdbcType = JdbcType.INTEGER),
            Result(column = "output_tokens", property = "outputTokens", jdbcType = JdbcType.INTEGER),
            Result(column = "total_tokens", property = "totalTokens", jdbcType = JdbcType.INTEGER),
            Result(column = "used_at", property = "usedAt", jdbcType = JdbcType.TIMESTAMP),
            Result(column = "latency_ms", property = "latencyMs", jdbcType = JdbcType.INTEGER),
        ]
    )
    fun listAdminUsageLogs(
        @Param("startTime") startTime: Date?,
        @Param("endTime") endTime: Date?,
        @Param("userId") userId: Long?,
        @Param("userKeyword") userKeyword: String?,
        @Param("modelId") modelId: Long?,
        @Param("modelKeyword") modelKeyword: String?,
        @Param("vendorId") vendorId: Long?,
    ): List<AdminStatisticsUsageLogItemDto>

    @Select(
        """
        <script>
        SELECT
            d.id AS dept_id,
            COALESCE(d.dept_name, '') AS dept_name,
            COALESCE(s.usage_count, 0) AS usage_count,
            COALESCE(s.total_tokens, 0) AS total_tokens
        FROM department d
        LEFT JOIN (
            SELECT
                COALESCE(l.dept_id, u.dept_id) AS dept_id,
                COUNT(1) AS usage_count,
                COALESCE(SUM(l.total_tokens), 0) AS total_tokens
            FROM llm_usage_log l
            LEFT JOIN users u ON u.id = l.user_id AND u.del_flag = 0
            GROUP BY COALESCE(l.dept_id, u.dept_id)
        ) s ON s.dept_id = d.id
        WHERE d.del_flag = 0
        <if test="deptName != null and deptName != ''">
          AND d.dept_name LIKE CONCAT('%', #{deptName}, '%')
        </if>
        ORDER BY total_tokens DESC, usage_count DESC, dept_id ASC
        </script>
        """
    )
    @Results(
        id = "AdminStatisticsDepartmentUsageItemDtoResult",
        value = [
            Result(column = "dept_id", property = "deptId", jdbcType = JdbcType.BIGINT),
            Result(column = "dept_name", property = "deptName", jdbcType = JdbcType.VARCHAR),
            Result(column = "usage_count", property = "usageCount", jdbcType = JdbcType.BIGINT),
            Result(column = "total_tokens", property = "totalTokens", jdbcType = JdbcType.BIGINT),
        ]
    )
    fun listAdminDepartmentUsageStats(
        @Param("deptName") deptName: String?,
    ): List<AdminStatisticsDepartmentUsageItemDto>

    @Select(
        """
        <script>
        SELECT
            u.id AS user_id,
            COALESCE(u.name, '') AS user_name,
            u.username AS username,
            u.mobile AS mobile,
            u.email AS email,
            COALESCE(d.dept_name, '') AS dept_name,
            COALESCE(a.available_amount, 0) AS balance,
            COALESCE(SUM(l.total_tokens), 0) AS total_tokens,
            COUNT(l.id) AS usage_count
        FROM users u
        LEFT JOIN department d ON d.id = u.dept_id AND d.del_flag = 0
        LEFT JOIN user_quota_accounts a ON a.user_id = u.id
        LEFT JOIN llm_usage_log l ON l.user_id = u.id
        WHERE u.del_flag = 0
        <if test="nickname != null and nickname != ''">
          AND u.name LIKE CONCAT('%', #{nickname}, '%')
        </if>
        <if test="accountKeyword != null and accountKeyword != ''">
          AND (
            u.mobile LIKE CONCAT('%', #{accountKeyword}, '%')
            OR u.username LIKE CONCAT('%', #{accountKeyword}, '%')
            OR u.email LIKE CONCAT('%', #{accountKeyword}, '%')
          )
        </if>
        GROUP BY u.id, u.name, u.username, u.mobile, u.email, d.dept_name, a.available_amount
        ORDER BY total_tokens DESC, usage_count DESC, u.id DESC
        </script>
        """
    )
    @Results(
        id = "AdminStatisticsUserUsageItemDtoResult",
        value = [
            Result(column = "user_id", property = "userId", jdbcType = JdbcType.BIGINT),
            Result(column = "user_name", property = "userName", jdbcType = JdbcType.VARCHAR),
            Result(column = "username", property = "username", jdbcType = JdbcType.VARCHAR),
            Result(column = "mobile", property = "mobile", jdbcType = JdbcType.VARCHAR),
            Result(column = "email", property = "email", jdbcType = JdbcType.VARCHAR),
            Result(column = "dept_name", property = "deptName", jdbcType = JdbcType.VARCHAR),
            Result(column = "balance", property = "balance", jdbcType = JdbcType.DECIMAL),
            Result(column = "total_tokens", property = "totalTokens", jdbcType = JdbcType.BIGINT),
            Result(column = "usage_count", property = "usageCount", jdbcType = JdbcType.BIGINT),
        ]
    )
    fun listAdminUserUsageStats(
        @Param("nickname") nickname: String?,
        @Param("accountKeyword") accountKeyword: String?,
    ): List<AdminStatisticsUserUsageItemDto>
}
