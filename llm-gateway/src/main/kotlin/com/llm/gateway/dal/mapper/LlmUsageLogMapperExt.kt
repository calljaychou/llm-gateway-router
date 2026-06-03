package com.llm.gateway.dal.mapper

import com.llm.gateway.model.dto.UserUsageHourlyCountDto
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
            m.model_alias AS model_name,
            m.vendor_id AS vendor_id,
            COUNT(1) AS usage_count
        FROM llm_usage_log l
        LEFT JOIN models m ON m.id = l.model_id
        WHERE l.user_id = #{userId}
          AND l.model_id IS NOT NULL
        GROUP BY l.model_id
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
}
