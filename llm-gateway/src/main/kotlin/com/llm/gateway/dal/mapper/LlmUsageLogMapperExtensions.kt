/*
 * Auto-generated file. Created by MyBatis Generator
 * Generation date: 2026-05-31T14:53:40.609887+08:00
 */
package com.llm.gateway.dal.mapper

import com.llm.gateway.dal.mapper.LlmUsageLogDynamicSqlSupport.LlmUsageLog
import com.llm.gateway.dal.mapper.LlmUsageLogDynamicSqlSupport.LlmUsageLog.accountingStatus
import com.llm.gateway.dal.mapper.LlmUsageLogDynamicSqlSupport.LlmUsageLog.amountCny
import com.llm.gateway.dal.mapper.LlmUsageLogDynamicSqlSupport.LlmUsageLog.apiKeyId
import com.llm.gateway.dal.mapper.LlmUsageLogDynamicSqlSupport.LlmUsageLog.billableInputTokens
import com.llm.gateway.dal.mapper.LlmUsageLogDynamicSqlSupport.LlmUsageLog.billableOutputTokens
import com.llm.gateway.dal.mapper.LlmUsageLogDynamicSqlSupport.LlmUsageLog.billingCurrency
import com.llm.gateway.dal.mapper.LlmUsageLogDynamicSqlSupport.LlmUsageLog.billingDetail
import com.llm.gateway.dal.mapper.LlmUsageLogDynamicSqlSupport.LlmUsageLog.billingStrategy
import com.llm.gateway.dal.mapper.LlmUsageLogDynamicSqlSupport.LlmUsageLog.createdAt
import com.llm.gateway.dal.mapper.LlmUsageLogDynamicSqlSupport.LlmUsageLog.deptId
import com.llm.gateway.dal.mapper.LlmUsageLogDynamicSqlSupport.LlmUsageLog.endpoint
import com.llm.gateway.dal.mapper.LlmUsageLogDynamicSqlSupport.LlmUsageLog.errorCode
import com.llm.gateway.dal.mapper.LlmUsageLogDynamicSqlSupport.LlmUsageLog.id
import com.llm.gateway.dal.mapper.LlmUsageLogDynamicSqlSupport.LlmUsageLog.inputTokens
import com.llm.gateway.dal.mapper.LlmUsageLogDynamicSqlSupport.LlmUsageLog.latencyMs
import com.llm.gateway.dal.mapper.LlmUsageLogDynamicSqlSupport.LlmUsageLog.modelEncoding
import com.llm.gateway.dal.mapper.LlmUsageLogDynamicSqlSupport.LlmUsageLog.modelId
import com.llm.gateway.dal.mapper.LlmUsageLogDynamicSqlSupport.LlmUsageLog.outputTokens
import com.llm.gateway.dal.mapper.LlmUsageLogDynamicSqlSupport.LlmUsageLog.requestId
import com.llm.gateway.dal.mapper.LlmUsageLogDynamicSqlSupport.LlmUsageLog.requestModel
import com.llm.gateway.dal.mapper.LlmUsageLogDynamicSqlSupport.LlmUsageLog.requestStartedAt
import com.llm.gateway.dal.mapper.LlmUsageLogDynamicSqlSupport.LlmUsageLog.reservedAmountCny
import com.llm.gateway.dal.mapper.LlmUsageLogDynamicSqlSupport.LlmUsageLog.resolvedModel
import com.llm.gateway.dal.mapper.LlmUsageLogDynamicSqlSupport.LlmUsageLog.settledAt
import com.llm.gateway.dal.mapper.LlmUsageLogDynamicSqlSupport.LlmUsageLog.statusCode
import com.llm.gateway.dal.mapper.LlmUsageLogDynamicSqlSupport.LlmUsageLog.tokenCalcDetail
import com.llm.gateway.dal.mapper.LlmUsageLogDynamicSqlSupport.LlmUsageLog.tokenCalcNote
import com.llm.gateway.dal.mapper.LlmUsageLogDynamicSqlSupport.LlmUsageLog.tokenCalcSource
import com.llm.gateway.dal.mapper.LlmUsageLogDynamicSqlSupport.LlmUsageLog.tokenCalcSupported
import com.llm.gateway.dal.mapper.LlmUsageLogDynamicSqlSupport.LlmUsageLog.tokenProtocol
import com.llm.gateway.dal.mapper.LlmUsageLogDynamicSqlSupport.LlmUsageLog.totalTokens
import com.llm.gateway.dal.mapper.LlmUsageLogDynamicSqlSupport.LlmUsageLog.upstreamModel
import com.llm.gateway.dal.mapper.LlmUsageLogDynamicSqlSupport.LlmUsageLog.useStream
import com.llm.gateway.dal.mapper.LlmUsageLogDynamicSqlSupport.LlmUsageLog.userId
import com.llm.gateway.dal.mapper.LlmUsageLogDynamicSqlSupport.LlmUsageLog.vendorId
import com.llm.gateway.dal.model.LlmUsageLogRecord
import org.mybatis.dynamic.sql.SqlBuilder.isEqualTo
import org.mybatis.dynamic.sql.insert.render.MultiRowInsertStatementProvider
import org.mybatis.dynamic.sql.util.kotlin.*
import org.mybatis.dynamic.sql.util.kotlin.mybatis3.*

fun LlmUsageLogMapper.count(completer: CountCompleter) =
    countFrom(this::count, LlmUsageLog, completer)

fun LlmUsageLogMapper.delete(completer: DeleteCompleter) =
    deleteFrom(this::delete, LlmUsageLog, completer)

fun LlmUsageLogMapper.deleteByPrimaryKey(id_: Long) =
    delete {
        where(id, isEqualTo(id_))
    }

fun LlmUsageLogMapper.insertMultipleHelper(multipleInsertStatement: MultiRowInsertStatementProvider<LlmUsageLogRecord>) =
    insertMultiple(multipleInsertStatement.insertStatement, multipleInsertStatement.records)

fun LlmUsageLogMapper.insert(record: LlmUsageLogRecord) =
    insert(this::insert, record, LlmUsageLog) {
        map(requestId).toProperty("requestId")
        map(userId).toProperty("userId")
        map(deptId).toProperty("deptId")
        map(apiKeyId).toProperty("apiKeyId")
        map(vendorId).toProperty("vendorId")
        map(modelId).toProperty("modelId")
        map(endpoint).toProperty("endpoint")
        map(tokenProtocol).toProperty("tokenProtocol")
        map(useStream).toProperty("useStream")
        map(requestModel).toProperty("requestModel")
        map(upstreamModel).toProperty("upstreamModel")
        map(resolvedModel).toProperty("resolvedModel")
        map(modelEncoding).toProperty("modelEncoding")
        map(tokenCalcSource).toProperty("tokenCalcSource")
        map(tokenCalcSupported).toProperty("tokenCalcSupported")
        map(inputTokens).toProperty("inputTokens")
        map(outputTokens).toProperty("outputTokens")
        map(totalTokens).toProperty("totalTokens")
        map(billableInputTokens).toProperty("billableInputTokens")
        map(billableOutputTokens).toProperty("billableOutputTokens")
        map(reservedAmountCny).toProperty("reservedAmountCny")
        map(amountCny).toProperty("amountCny")
        map(billingStrategy).toProperty("billingStrategy")
        map(billingCurrency).toProperty("billingCurrency")
        map(latencyMs).toProperty("latencyMs")
        map(statusCode).toProperty("statusCode")
        map(errorCode).toProperty("errorCode")
        map(accountingStatus).toProperty("accountingStatus")
        map(requestStartedAt).toProperty("requestStartedAt")
        map(settledAt).toProperty("settledAt")
        map(createdAt).toProperty("createdAt")
        map(tokenCalcNote).toProperty("tokenCalcNote")
        map(tokenCalcDetail).toProperty("tokenCalcDetail")
        map(billingDetail).toProperty("billingDetail")
    }

fun LlmUsageLogMapper.insertMultiple(records: Collection<LlmUsageLogRecord>) =
    insertMultiple(this::insertMultipleHelper, records, LlmUsageLog) {
        map(requestId).toProperty("requestId")
        map(userId).toProperty("userId")
        map(deptId).toProperty("deptId")
        map(apiKeyId).toProperty("apiKeyId")
        map(vendorId).toProperty("vendorId")
        map(modelId).toProperty("modelId")
        map(endpoint).toProperty("endpoint")
        map(tokenProtocol).toProperty("tokenProtocol")
        map(useStream).toProperty("useStream")
        map(requestModel).toProperty("requestModel")
        map(upstreamModel).toProperty("upstreamModel")
        map(resolvedModel).toProperty("resolvedModel")
        map(modelEncoding).toProperty("modelEncoding")
        map(tokenCalcSource).toProperty("tokenCalcSource")
        map(tokenCalcSupported).toProperty("tokenCalcSupported")
        map(inputTokens).toProperty("inputTokens")
        map(outputTokens).toProperty("outputTokens")
        map(totalTokens).toProperty("totalTokens")
        map(billableInputTokens).toProperty("billableInputTokens")
        map(billableOutputTokens).toProperty("billableOutputTokens")
        map(reservedAmountCny).toProperty("reservedAmountCny")
        map(amountCny).toProperty("amountCny")
        map(billingStrategy).toProperty("billingStrategy")
        map(billingCurrency).toProperty("billingCurrency")
        map(latencyMs).toProperty("latencyMs")
        map(statusCode).toProperty("statusCode")
        map(errorCode).toProperty("errorCode")
        map(accountingStatus).toProperty("accountingStatus")
        map(requestStartedAt).toProperty("requestStartedAt")
        map(settledAt).toProperty("settledAt")
        map(createdAt).toProperty("createdAt")
        map(tokenCalcNote).toProperty("tokenCalcNote")
        map(tokenCalcDetail).toProperty("tokenCalcDetail")
        map(billingDetail).toProperty("billingDetail")
    }

fun LlmUsageLogMapper.insertMultiple(vararg records: LlmUsageLogRecord) =
    insertMultiple(records.toList())

fun LlmUsageLogMapper.insertSelective(record: LlmUsageLogRecord) =
    insert(this::insert, record, LlmUsageLog) {
        map(requestId).toPropertyWhenPresent("requestId", record::requestId)
        map(userId).toPropertyWhenPresent("userId", record::userId)
        map(deptId).toPropertyWhenPresent("deptId", record::deptId)
        map(apiKeyId).toPropertyWhenPresent("apiKeyId", record::apiKeyId)
        map(vendorId).toPropertyWhenPresent("vendorId", record::vendorId)
        map(modelId).toPropertyWhenPresent("modelId", record::modelId)
        map(endpoint).toPropertyWhenPresent("endpoint", record::endpoint)
        map(tokenProtocol).toPropertyWhenPresent("tokenProtocol", record::tokenProtocol)
        map(useStream).toPropertyWhenPresent("useStream", record::useStream)
        map(requestModel).toPropertyWhenPresent("requestModel", record::requestModel)
        map(upstreamModel).toPropertyWhenPresent("upstreamModel", record::upstreamModel)
        map(resolvedModel).toPropertyWhenPresent("resolvedModel", record::resolvedModel)
        map(modelEncoding).toPropertyWhenPresent("modelEncoding", record::modelEncoding)
        map(tokenCalcSource).toPropertyWhenPresent("tokenCalcSource", record::tokenCalcSource)
        map(tokenCalcSupported).toPropertyWhenPresent("tokenCalcSupported", record::tokenCalcSupported)
        map(inputTokens).toPropertyWhenPresent("inputTokens", record::inputTokens)
        map(outputTokens).toPropertyWhenPresent("outputTokens", record::outputTokens)
        map(totalTokens).toPropertyWhenPresent("totalTokens", record::totalTokens)
        map(billableInputTokens).toPropertyWhenPresent("billableInputTokens", record::billableInputTokens)
        map(billableOutputTokens).toPropertyWhenPresent("billableOutputTokens", record::billableOutputTokens)
        map(reservedAmountCny).toPropertyWhenPresent("reservedAmountCny", record::reservedAmountCny)
        map(amountCny).toPropertyWhenPresent("amountCny", record::amountCny)
        map(billingStrategy).toPropertyWhenPresent("billingStrategy", record::billingStrategy)
        map(billingCurrency).toPropertyWhenPresent("billingCurrency", record::billingCurrency)
        map(latencyMs).toPropertyWhenPresent("latencyMs", record::latencyMs)
        map(statusCode).toPropertyWhenPresent("statusCode", record::statusCode)
        map(errorCode).toPropertyWhenPresent("errorCode", record::errorCode)
        map(accountingStatus).toPropertyWhenPresent("accountingStatus", record::accountingStatus)
        map(requestStartedAt).toPropertyWhenPresent("requestStartedAt", record::requestStartedAt)
        map(settledAt).toPropertyWhenPresent("settledAt", record::settledAt)
        map(createdAt).toPropertyWhenPresent("createdAt", record::createdAt)
        map(tokenCalcNote).toPropertyWhenPresent("tokenCalcNote", record::tokenCalcNote)
        map(tokenCalcDetail).toPropertyWhenPresent("tokenCalcDetail", record::tokenCalcDetail)
        map(billingDetail).toPropertyWhenPresent("billingDetail", record::billingDetail)
    }

private val columnList = listOf(id, requestId, userId, deptId, apiKeyId, vendorId, modelId, endpoint, tokenProtocol, useStream, requestModel, upstreamModel, resolvedModel, modelEncoding, tokenCalcSource, tokenCalcSupported, inputTokens, outputTokens, totalTokens, billableInputTokens, billableOutputTokens, reservedAmountCny, amountCny, billingStrategy, billingCurrency, latencyMs, statusCode, errorCode, accountingStatus, requestStartedAt, settledAt, createdAt, tokenCalcNote, tokenCalcDetail, billingDetail)

fun LlmUsageLogMapper.selectOne(completer: SelectCompleter) =
    selectOne(this::selectOne, columnList, LlmUsageLog, completer)

fun LlmUsageLogMapper.select(completer: SelectCompleter) =
    selectList(this::selectMany, columnList, LlmUsageLog, completer)

fun LlmUsageLogMapper.selectDistinct(completer: SelectCompleter) =
    selectDistinct(this::selectMany, columnList, LlmUsageLog, completer)

fun LlmUsageLogMapper.selectByPrimaryKey(id_: Long) =
    selectOne {
        where(id, isEqualTo(id_))
    }

fun LlmUsageLogMapper.update(completer: UpdateCompleter) =
    update(this::update, LlmUsageLog, completer)

fun KotlinUpdateBuilder.updateAllColumns(record: LlmUsageLogRecord) =
    apply {
        set(requestId).equalTo(record::requestId)
        set(userId).equalTo(record::userId)
        set(deptId).equalTo(record::deptId)
        set(apiKeyId).equalTo(record::apiKeyId)
        set(vendorId).equalTo(record::vendorId)
        set(modelId).equalTo(record::modelId)
        set(endpoint).equalTo(record::endpoint)
        set(tokenProtocol).equalTo(record::tokenProtocol)
        set(useStream).equalTo(record::useStream)
        set(requestModel).equalTo(record::requestModel)
        set(upstreamModel).equalTo(record::upstreamModel)
        set(resolvedModel).equalTo(record::resolvedModel)
        set(modelEncoding).equalTo(record::modelEncoding)
        set(tokenCalcSource).equalTo(record::tokenCalcSource)
        set(tokenCalcSupported).equalTo(record::tokenCalcSupported)
        set(inputTokens).equalTo(record::inputTokens)
        set(outputTokens).equalTo(record::outputTokens)
        set(totalTokens).equalTo(record::totalTokens)
        set(billableInputTokens).equalTo(record::billableInputTokens)
        set(billableOutputTokens).equalTo(record::billableOutputTokens)
        set(reservedAmountCny).equalTo(record::reservedAmountCny)
        set(amountCny).equalTo(record::amountCny)
        set(billingStrategy).equalTo(record::billingStrategy)
        set(billingCurrency).equalTo(record::billingCurrency)
        set(latencyMs).equalTo(record::latencyMs)
        set(statusCode).equalTo(record::statusCode)
        set(errorCode).equalTo(record::errorCode)
        set(accountingStatus).equalTo(record::accountingStatus)
        set(requestStartedAt).equalTo(record::requestStartedAt)
        set(settledAt).equalTo(record::settledAt)
        set(createdAt).equalTo(record::createdAt)
        set(tokenCalcNote).equalTo(record::tokenCalcNote)
        set(tokenCalcDetail).equalTo(record::tokenCalcDetail)
        set(billingDetail).equalTo(record::billingDetail)
    }

fun KotlinUpdateBuilder.updateSelectiveColumns(record: LlmUsageLogRecord) =
    apply {
        set(requestId).equalToWhenPresent(record::requestId)
        set(userId).equalToWhenPresent(record::userId)
        set(deptId).equalToWhenPresent(record::deptId)
        set(apiKeyId).equalToWhenPresent(record::apiKeyId)
        set(vendorId).equalToWhenPresent(record::vendorId)
        set(modelId).equalToWhenPresent(record::modelId)
        set(endpoint).equalToWhenPresent(record::endpoint)
        set(tokenProtocol).equalToWhenPresent(record::tokenProtocol)
        set(useStream).equalToWhenPresent(record::useStream)
        set(requestModel).equalToWhenPresent(record::requestModel)
        set(upstreamModel).equalToWhenPresent(record::upstreamModel)
        set(resolvedModel).equalToWhenPresent(record::resolvedModel)
        set(modelEncoding).equalToWhenPresent(record::modelEncoding)
        set(tokenCalcSource).equalToWhenPresent(record::tokenCalcSource)
        set(tokenCalcSupported).equalToWhenPresent(record::tokenCalcSupported)
        set(inputTokens).equalToWhenPresent(record::inputTokens)
        set(outputTokens).equalToWhenPresent(record::outputTokens)
        set(totalTokens).equalToWhenPresent(record::totalTokens)
        set(billableInputTokens).equalToWhenPresent(record::billableInputTokens)
        set(billableOutputTokens).equalToWhenPresent(record::billableOutputTokens)
        set(reservedAmountCny).equalToWhenPresent(record::reservedAmountCny)
        set(amountCny).equalToWhenPresent(record::amountCny)
        set(billingStrategy).equalToWhenPresent(record::billingStrategy)
        set(billingCurrency).equalToWhenPresent(record::billingCurrency)
        set(latencyMs).equalToWhenPresent(record::latencyMs)
        set(statusCode).equalToWhenPresent(record::statusCode)
        set(errorCode).equalToWhenPresent(record::errorCode)
        set(accountingStatus).equalToWhenPresent(record::accountingStatus)
        set(requestStartedAt).equalToWhenPresent(record::requestStartedAt)
        set(settledAt).equalToWhenPresent(record::settledAt)
        set(createdAt).equalToWhenPresent(record::createdAt)
        set(tokenCalcNote).equalToWhenPresent(record::tokenCalcNote)
        set(tokenCalcDetail).equalToWhenPresent(record::tokenCalcDetail)
        set(billingDetail).equalToWhenPresent(record::billingDetail)
    }

fun LlmUsageLogMapper.updateByPrimaryKey(record: LlmUsageLogRecord) =
    update {
        set(requestId).equalTo(record::requestId)
        set(userId).equalTo(record::userId)
        set(deptId).equalTo(record::deptId)
        set(apiKeyId).equalTo(record::apiKeyId)
        set(vendorId).equalTo(record::vendorId)
        set(modelId).equalTo(record::modelId)
        set(endpoint).equalTo(record::endpoint)
        set(tokenProtocol).equalTo(record::tokenProtocol)
        set(useStream).equalTo(record::useStream)
        set(requestModel).equalTo(record::requestModel)
        set(upstreamModel).equalTo(record::upstreamModel)
        set(resolvedModel).equalTo(record::resolvedModel)
        set(modelEncoding).equalTo(record::modelEncoding)
        set(tokenCalcSource).equalTo(record::tokenCalcSource)
        set(tokenCalcSupported).equalTo(record::tokenCalcSupported)
        set(inputTokens).equalTo(record::inputTokens)
        set(outputTokens).equalTo(record::outputTokens)
        set(totalTokens).equalTo(record::totalTokens)
        set(billableInputTokens).equalTo(record::billableInputTokens)
        set(billableOutputTokens).equalTo(record::billableOutputTokens)
        set(reservedAmountCny).equalTo(record::reservedAmountCny)
        set(amountCny).equalTo(record::amountCny)
        set(billingStrategy).equalTo(record::billingStrategy)
        set(billingCurrency).equalTo(record::billingCurrency)
        set(latencyMs).equalTo(record::latencyMs)
        set(statusCode).equalTo(record::statusCode)
        set(errorCode).equalTo(record::errorCode)
        set(accountingStatus).equalTo(record::accountingStatus)
        set(requestStartedAt).equalTo(record::requestStartedAt)
        set(settledAt).equalTo(record::settledAt)
        set(createdAt).equalTo(record::createdAt)
        set(tokenCalcNote).equalTo(record::tokenCalcNote)
        set(tokenCalcDetail).equalTo(record::tokenCalcDetail)
        set(billingDetail).equalTo(record::billingDetail)
        where(id, isEqualTo(record::id))
    }

fun LlmUsageLogMapper.updateByPrimaryKeySelective(record: LlmUsageLogRecord) =
    update {
        set(requestId).equalToWhenPresent(record::requestId)
        set(userId).equalToWhenPresent(record::userId)
        set(deptId).equalToWhenPresent(record::deptId)
        set(apiKeyId).equalToWhenPresent(record::apiKeyId)
        set(vendorId).equalToWhenPresent(record::vendorId)
        set(modelId).equalToWhenPresent(record::modelId)
        set(endpoint).equalToWhenPresent(record::endpoint)
        set(tokenProtocol).equalToWhenPresent(record::tokenProtocol)
        set(useStream).equalToWhenPresent(record::useStream)
        set(requestModel).equalToWhenPresent(record::requestModel)
        set(upstreamModel).equalToWhenPresent(record::upstreamModel)
        set(resolvedModel).equalToWhenPresent(record::resolvedModel)
        set(modelEncoding).equalToWhenPresent(record::modelEncoding)
        set(tokenCalcSource).equalToWhenPresent(record::tokenCalcSource)
        set(tokenCalcSupported).equalToWhenPresent(record::tokenCalcSupported)
        set(inputTokens).equalToWhenPresent(record::inputTokens)
        set(outputTokens).equalToWhenPresent(record::outputTokens)
        set(totalTokens).equalToWhenPresent(record::totalTokens)
        set(billableInputTokens).equalToWhenPresent(record::billableInputTokens)
        set(billableOutputTokens).equalToWhenPresent(record::billableOutputTokens)
        set(reservedAmountCny).equalToWhenPresent(record::reservedAmountCny)
        set(amountCny).equalToWhenPresent(record::amountCny)
        set(billingStrategy).equalToWhenPresent(record::billingStrategy)
        set(billingCurrency).equalToWhenPresent(record::billingCurrency)
        set(latencyMs).equalToWhenPresent(record::latencyMs)
        set(statusCode).equalToWhenPresent(record::statusCode)
        set(errorCode).equalToWhenPresent(record::errorCode)
        set(accountingStatus).equalToWhenPresent(record::accountingStatus)
        set(requestStartedAt).equalToWhenPresent(record::requestStartedAt)
        set(settledAt).equalToWhenPresent(record::settledAt)
        set(createdAt).equalToWhenPresent(record::createdAt)
        set(tokenCalcNote).equalToWhenPresent(record::tokenCalcNote)
        set(tokenCalcDetail).equalToWhenPresent(record::tokenCalcDetail)
        set(billingDetail).equalToWhenPresent(record::billingDetail)
        where(id, isEqualTo(record::id))
    }