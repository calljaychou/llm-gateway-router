package com.llm.gateway.service

import com.llm.gateway.common.enums.NormalStatus
import com.llm.gateway.dal.mapper.ModelsDynamicSqlSupport
import com.llm.gateway.dal.mapper.ModelsMapper
import com.llm.gateway.dal.mapper.UsageLogsMapper
import com.llm.gateway.dal.mapper.UsersDynamicSqlSupport
import com.llm.gateway.dal.mapper.UsersMapper
import com.llm.gateway.dal.mapper.insertSelective
import com.llm.gateway.dal.mapper.selectOne
import com.llm.gateway.dal.model.UsageLogsRecord
import com.llm.gateway.model.dto.UsageLogRecordCommand
import java.util.Date
import org.apache.commons.math3.fitting.leastsquares.LeastSquaresFactory.model
import org.springframework.stereotype.Service

@Service
class UsageLogService(
    private val usageLogsMapper: UsageLogsMapper,
    private val usersMapper: UsersMapper,
) {
    fun record(command: UsageLogRecordCommand) {
        val user = usersMapper.selectOne {
            where { UsersDynamicSqlSupport.Users.id isEqualTo command.userId }
            and { UsersDynamicSqlSupport.Users.status isEqualTo NormalStatus }
            and { UsersDynamicSqlSupport.Users.delFlag isEqualTo false }
        }
        usageLogsMapper.insertSelective(
            UsageLogsRecord(
                requestId = command.requestId,
                userId = command.userId,
                deptId = user?.deptId ?: 0L,
                apiKeyId = null,
                vendorId = command.vendorId,
                modelId = null,
                endpoint = command.endpoint,
                useStream = command.stream,
                reservedTokens = command.reservedTokens,
                promptTokens = command.usage.promptTokens,
                completionTokens = command.usage.completionTokens,
                totalTokens = command.usage.totalTokens,
                latencyMs = command.latencyMs,
                statusCode = command.statusCode,
                errorCode = command.errorCode,
                accountingStatus = command.accountingStatus,
                settledAt = Date(),
                retryCount = 0,
                calcSource = command.calcSource,
                createdAt = Date(),
            )
        )
    }
}
