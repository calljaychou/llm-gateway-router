package com.llm.gateway.dal.mapper

import com.llm.gateway.model.dto.UserUsageHourlyCountDto
import com.llm.gateway.model.dto.UserUsageLogListItemDto
import com.llm.gateway.model.dto.UserUsageModelCountDto
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
}
