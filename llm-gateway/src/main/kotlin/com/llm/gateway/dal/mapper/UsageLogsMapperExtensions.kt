/*
 * Auto-generated file. Created by MyBatis Generator
 * Generation date: 2026-05-07T18:46:02.174083+08:00
 */
package com.llm.gateway.dal.mapper

import com.llm.gateway.dal.mapper.UsageLogsDynamicSqlSupport.UsageLogs
import com.llm.gateway.dal.mapper.UsageLogsDynamicSqlSupport.UsageLogs.apiKeyId
import com.llm.gateway.dal.mapper.UsageLogsDynamicSqlSupport.UsageLogs.completionTokens
import com.llm.gateway.dal.mapper.UsageLogsDynamicSqlSupport.UsageLogs.createdAt
import com.llm.gateway.dal.mapper.UsageLogsDynamicSqlSupport.UsageLogs.deptId
import com.llm.gateway.dal.mapper.UsageLogsDynamicSqlSupport.UsageLogs.endpoint
import com.llm.gateway.dal.mapper.UsageLogsDynamicSqlSupport.UsageLogs.errorCode
import com.llm.gateway.dal.mapper.UsageLogsDynamicSqlSupport.UsageLogs.id
import com.llm.gateway.dal.mapper.UsageLogsDynamicSqlSupport.UsageLogs.isStream
import com.llm.gateway.dal.mapper.UsageLogsDynamicSqlSupport.UsageLogs.latencyMs
import com.llm.gateway.dal.mapper.UsageLogsDynamicSqlSupport.UsageLogs.modelId
import com.llm.gateway.dal.mapper.UsageLogsDynamicSqlSupport.UsageLogs.promptTokens
import com.llm.gateway.dal.mapper.UsageLogsDynamicSqlSupport.UsageLogs.requestId
import com.llm.gateway.dal.mapper.UsageLogsDynamicSqlSupport.UsageLogs.statusCode
import com.llm.gateway.dal.mapper.UsageLogsDynamicSqlSupport.UsageLogs.totalTokens
import com.llm.gateway.dal.mapper.UsageLogsDynamicSqlSupport.UsageLogs.userId
import com.llm.gateway.dal.mapper.UsageLogsDynamicSqlSupport.UsageLogs.vendorId
import com.llm.gateway.dal.model.UsageLogsRecord
import org.mybatis.dynamic.sql.SqlBuilder.isEqualTo
import org.mybatis.dynamic.sql.insert.render.MultiRowInsertStatementProvider
import org.mybatis.dynamic.sql.util.kotlin.*
import org.mybatis.dynamic.sql.util.kotlin.mybatis3.*

fun UsageLogsMapper.count(completer: CountCompleter) =
    countFrom(this::count, UsageLogs, completer)

fun UsageLogsMapper.delete(completer: DeleteCompleter) =
    deleteFrom(this::delete, UsageLogs, completer)

fun UsageLogsMapper.deleteByPrimaryKey(id_: Long) =
    delete {
        where(id, isEqualTo(id_))
    }

fun UsageLogsMapper.insertMultipleHelper(multipleInsertStatement: MultiRowInsertStatementProvider<UsageLogsRecord>) =
    insertMultiple(multipleInsertStatement.insertStatement, multipleInsertStatement.records)

fun UsageLogsMapper.insert(record: UsageLogsRecord) =
    insert(this::insert, record, UsageLogs) {
        map(requestId).toProperty("requestId")
        map(userId).toProperty("userId")
        map(deptId).toProperty("deptId")
        map(apiKeyId).toProperty("apiKeyId")
        map(vendorId).toProperty("vendorId")
        map(modelId).toProperty("modelId")
        map(endpoint).toProperty("endpoint")
        map(isStream).toProperty("isStream")
        map(promptTokens).toProperty("promptTokens")
        map(completionTokens).toProperty("completionTokens")
        map(totalTokens).toProperty("totalTokens")
        map(latencyMs).toProperty("latencyMs")
        map(statusCode).toProperty("statusCode")
        map(errorCode).toProperty("errorCode")
        map(createdAt).toProperty("createdAt")
    }

fun UsageLogsMapper.insertMultiple(records: Collection<UsageLogsRecord>) =
    insertMultiple(this::insertMultipleHelper, records, UsageLogs) {
        map(requestId).toProperty("requestId")
        map(userId).toProperty("userId")
        map(deptId).toProperty("deptId")
        map(apiKeyId).toProperty("apiKeyId")
        map(vendorId).toProperty("vendorId")
        map(modelId).toProperty("modelId")
        map(endpoint).toProperty("endpoint")
        map(isStream).toProperty("isStream")
        map(promptTokens).toProperty("promptTokens")
        map(completionTokens).toProperty("completionTokens")
        map(totalTokens).toProperty("totalTokens")
        map(latencyMs).toProperty("latencyMs")
        map(statusCode).toProperty("statusCode")
        map(errorCode).toProperty("errorCode")
        map(createdAt).toProperty("createdAt")
    }

fun UsageLogsMapper.insertMultiple(vararg records: UsageLogsRecord) =
    insertMultiple(records.toList())

fun UsageLogsMapper.insertSelective(record: UsageLogsRecord) =
    insert(this::insert, record, UsageLogs) {
        map(requestId).toPropertyWhenPresent("requestId", record::requestId)
        map(userId).toPropertyWhenPresent("userId", record::userId)
        map(deptId).toPropertyWhenPresent("deptId", record::deptId)
        map(apiKeyId).toPropertyWhenPresent("apiKeyId", record::apiKeyId)
        map(vendorId).toPropertyWhenPresent("vendorId", record::vendorId)
        map(modelId).toPropertyWhenPresent("modelId", record::modelId)
        map(endpoint).toPropertyWhenPresent("endpoint", record::endpoint)
        map(isStream).toPropertyWhenPresent("isStream", record::isStream)
        map(promptTokens).toPropertyWhenPresent("promptTokens", record::promptTokens)
        map(completionTokens).toPropertyWhenPresent("completionTokens", record::completionTokens)
        map(totalTokens).toPropertyWhenPresent("totalTokens", record::totalTokens)
        map(latencyMs).toPropertyWhenPresent("latencyMs", record::latencyMs)
        map(statusCode).toPropertyWhenPresent("statusCode", record::statusCode)
        map(errorCode).toPropertyWhenPresent("errorCode", record::errorCode)
        map(createdAt).toPropertyWhenPresent("createdAt", record::createdAt)
    }

private val columnList = listOf(id, requestId, userId, deptId, apiKeyId, vendorId, modelId, endpoint, isStream, promptTokens, completionTokens, totalTokens, latencyMs, statusCode, errorCode, createdAt)

fun UsageLogsMapper.selectOne(completer: SelectCompleter) =
    selectOne(this::selectOne, columnList, UsageLogs, completer)

fun UsageLogsMapper.select(completer: SelectCompleter) =
    selectList(this::selectMany, columnList, UsageLogs, completer)

fun UsageLogsMapper.selectDistinct(completer: SelectCompleter) =
    selectDistinct(this::selectMany, columnList, UsageLogs, completer)

fun UsageLogsMapper.selectByPrimaryKey(id_: Long) =
    selectOne {
        where(id, isEqualTo(id_))
    }

fun UsageLogsMapper.update(completer: UpdateCompleter) =
    update(this::update, UsageLogs, completer)

fun KotlinUpdateBuilder.updateAllColumns(record: UsageLogsRecord) =
    apply {
        set(requestId).equalTo(record::requestId)
        set(userId).equalTo(record::userId)
        set(deptId).equalTo(record::deptId)
        set(apiKeyId).equalTo(record::apiKeyId)
        set(vendorId).equalTo(record::vendorId)
        set(modelId).equalTo(record::modelId)
        set(endpoint).equalTo(record::endpoint)
        set(isStream).equalTo(record::isStream)
        set(promptTokens).equalTo(record::promptTokens)
        set(completionTokens).equalTo(record::completionTokens)
        set(totalTokens).equalTo(record::totalTokens)
        set(latencyMs).equalTo(record::latencyMs)
        set(statusCode).equalTo(record::statusCode)
        set(errorCode).equalTo(record::errorCode)
        set(createdAt).equalTo(record::createdAt)
    }

fun KotlinUpdateBuilder.updateSelectiveColumns(record: UsageLogsRecord) =
    apply {
        set(requestId).equalToWhenPresent(record::requestId)
        set(userId).equalToWhenPresent(record::userId)
        set(deptId).equalToWhenPresent(record::deptId)
        set(apiKeyId).equalToWhenPresent(record::apiKeyId)
        set(vendorId).equalToWhenPresent(record::vendorId)
        set(modelId).equalToWhenPresent(record::modelId)
        set(endpoint).equalToWhenPresent(record::endpoint)
        set(isStream).equalToWhenPresent(record::isStream)
        set(promptTokens).equalToWhenPresent(record::promptTokens)
        set(completionTokens).equalToWhenPresent(record::completionTokens)
        set(totalTokens).equalToWhenPresent(record::totalTokens)
        set(latencyMs).equalToWhenPresent(record::latencyMs)
        set(statusCode).equalToWhenPresent(record::statusCode)
        set(errorCode).equalToWhenPresent(record::errorCode)
        set(createdAt).equalToWhenPresent(record::createdAt)
    }

fun UsageLogsMapper.updateByPrimaryKey(record: UsageLogsRecord) =
    update {
        set(requestId).equalTo(record::requestId)
        set(userId).equalTo(record::userId)
        set(deptId).equalTo(record::deptId)
        set(apiKeyId).equalTo(record::apiKeyId)
        set(vendorId).equalTo(record::vendorId)
        set(modelId).equalTo(record::modelId)
        set(endpoint).equalTo(record::endpoint)
        set(isStream).equalTo(record::isStream)
        set(promptTokens).equalTo(record::promptTokens)
        set(completionTokens).equalTo(record::completionTokens)
        set(totalTokens).equalTo(record::totalTokens)
        set(latencyMs).equalTo(record::latencyMs)
        set(statusCode).equalTo(record::statusCode)
        set(errorCode).equalTo(record::errorCode)
        set(createdAt).equalTo(record::createdAt)
        where(id, isEqualTo(record::id))
    }

fun UsageLogsMapper.updateByPrimaryKeySelective(record: UsageLogsRecord) =
    update {
        set(requestId).equalToWhenPresent(record::requestId)
        set(userId).equalToWhenPresent(record::userId)
        set(deptId).equalToWhenPresent(record::deptId)
        set(apiKeyId).equalToWhenPresent(record::apiKeyId)
        set(vendorId).equalToWhenPresent(record::vendorId)
        set(modelId).equalToWhenPresent(record::modelId)
        set(endpoint).equalToWhenPresent(record::endpoint)
        set(isStream).equalToWhenPresent(record::isStream)
        set(promptTokens).equalToWhenPresent(record::promptTokens)
        set(completionTokens).equalToWhenPresent(record::completionTokens)
        set(totalTokens).equalToWhenPresent(record::totalTokens)
        set(latencyMs).equalToWhenPresent(record::latencyMs)
        set(statusCode).equalToWhenPresent(record::statusCode)
        set(errorCode).equalToWhenPresent(record::errorCode)
        set(createdAt).equalToWhenPresent(record::createdAt)
        where(id, isEqualTo(record::id))
    }