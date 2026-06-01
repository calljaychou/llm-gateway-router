/*
 * Auto-generated file. Created by MyBatis Generator
 * Generation date: 2026-05-31T14:53:40.61152+08:00
 */
package com.llm.gateway.dal.mapper

import com.llm.gateway.dal.mapper.LlmUsageTokenDetailDynamicSqlSupport.LlmUsageTokenDetail
import com.llm.gateway.dal.mapper.LlmUsageTokenDetailDynamicSqlSupport.LlmUsageTokenDetail.billableTokens
import com.llm.gateway.dal.mapper.LlmUsageTokenDetailDynamicSqlSupport.LlmUsageTokenDetail.cacheType
import com.llm.gateway.dal.mapper.LlmUsageTokenDetailDynamicSqlSupport.LlmUsageTokenDetail.createdAt
import com.llm.gateway.dal.mapper.LlmUsageTokenDetailDynamicSqlSupport.LlmUsageTokenDetail.id
import com.llm.gateway.dal.mapper.LlmUsageTokenDetailDynamicSqlSupport.LlmUsageTokenDetail.note
import com.llm.gateway.dal.mapper.LlmUsageTokenDetailDynamicSqlSupport.LlmUsageTokenDetail.providerField
import com.llm.gateway.dal.mapper.LlmUsageTokenDetailDynamicSqlSupport.LlmUsageTokenDetail.requestId
import com.llm.gateway.dal.mapper.LlmUsageTokenDetailDynamicSqlSupport.LlmUsageTokenDetail.source
import com.llm.gateway.dal.mapper.LlmUsageTokenDetailDynamicSqlSupport.LlmUsageTokenDetail.tokenDirection
import com.llm.gateway.dal.mapper.LlmUsageTokenDetailDynamicSqlSupport.LlmUsageTokenDetail.tokenType
import com.llm.gateway.dal.mapper.LlmUsageTokenDetailDynamicSqlSupport.LlmUsageTokenDetail.tokens
import com.llm.gateway.dal.mapper.LlmUsageTokenDetailDynamicSqlSupport.LlmUsageTokenDetail.usageLogId
import com.llm.gateway.dal.model.LlmUsageTokenDetailRecord
import org.mybatis.dynamic.sql.SqlBuilder.isEqualTo
import org.mybatis.dynamic.sql.insert.render.MultiRowInsertStatementProvider
import org.mybatis.dynamic.sql.util.kotlin.*
import org.mybatis.dynamic.sql.util.kotlin.mybatis3.*

fun LlmUsageTokenDetailMapper.count(completer: CountCompleter) =
    countFrom(this::count, LlmUsageTokenDetail, completer)

fun LlmUsageTokenDetailMapper.delete(completer: DeleteCompleter) =
    deleteFrom(this::delete, LlmUsageTokenDetail, completer)

fun LlmUsageTokenDetailMapper.deleteByPrimaryKey(id_: Long) =
    delete {
        where(id, isEqualTo(id_))
    }

fun LlmUsageTokenDetailMapper.insertMultipleHelper(multipleInsertStatement: MultiRowInsertStatementProvider<LlmUsageTokenDetailRecord>) =
    insertMultiple(multipleInsertStatement.insertStatement, multipleInsertStatement.records)

fun LlmUsageTokenDetailMapper.insert(record: LlmUsageTokenDetailRecord) =
    insert(this::insert, record, LlmUsageTokenDetail) {
        map(usageLogId).toProperty("usageLogId")
        map(requestId).toProperty("requestId")
        map(tokenDirection).toProperty("tokenDirection")
        map(tokenType).toProperty("tokenType")
        map(cacheType).toProperty("cacheType")
        map(tokens).toProperty("tokens")
        map(billableTokens).toProperty("billableTokens")
        map(source).toProperty("source")
        map(providerField).toProperty("providerField")
        map(note).toProperty("note")
        map(createdAt).toProperty("createdAt")
    }

fun LlmUsageTokenDetailMapper.insertMultiple(records: Collection<LlmUsageTokenDetailRecord>) =
    insertMultiple(this::insertMultipleHelper, records, LlmUsageTokenDetail) {
        map(usageLogId).toProperty("usageLogId")
        map(requestId).toProperty("requestId")
        map(tokenDirection).toProperty("tokenDirection")
        map(tokenType).toProperty("tokenType")
        map(cacheType).toProperty("cacheType")
        map(tokens).toProperty("tokens")
        map(billableTokens).toProperty("billableTokens")
        map(source).toProperty("source")
        map(providerField).toProperty("providerField")
        map(note).toProperty("note")
        map(createdAt).toProperty("createdAt")
    }

fun LlmUsageTokenDetailMapper.insertMultiple(vararg records: LlmUsageTokenDetailRecord) =
    insertMultiple(records.toList())

fun LlmUsageTokenDetailMapper.insertSelective(record: LlmUsageTokenDetailRecord) =
    insert(this::insert, record, LlmUsageTokenDetail) {
        map(usageLogId).toPropertyWhenPresent("usageLogId", record::usageLogId)
        map(requestId).toPropertyWhenPresent("requestId", record::requestId)
        map(tokenDirection).toPropertyWhenPresent("tokenDirection", record::tokenDirection)
        map(tokenType).toPropertyWhenPresent("tokenType", record::tokenType)
        map(cacheType).toPropertyWhenPresent("cacheType", record::cacheType)
        map(tokens).toPropertyWhenPresent("tokens", record::tokens)
        map(billableTokens).toPropertyWhenPresent("billableTokens", record::billableTokens)
        map(source).toPropertyWhenPresent("source", record::source)
        map(providerField).toPropertyWhenPresent("providerField", record::providerField)
        map(note).toPropertyWhenPresent("note", record::note)
        map(createdAt).toPropertyWhenPresent("createdAt", record::createdAt)
    }

private val columnList = listOf(id, usageLogId, requestId, tokenDirection, tokenType, cacheType, tokens, billableTokens, source, providerField, note, createdAt)

fun LlmUsageTokenDetailMapper.selectOne(completer: SelectCompleter) =
    selectOne(this::selectOne, columnList, LlmUsageTokenDetail, completer)

fun LlmUsageTokenDetailMapper.select(completer: SelectCompleter) =
    selectList(this::selectMany, columnList, LlmUsageTokenDetail, completer)

fun LlmUsageTokenDetailMapper.selectDistinct(completer: SelectCompleter) =
    selectDistinct(this::selectMany, columnList, LlmUsageTokenDetail, completer)

fun LlmUsageTokenDetailMapper.selectByPrimaryKey(id_: Long) =
    selectOne {
        where(id, isEqualTo(id_))
    }

fun LlmUsageTokenDetailMapper.update(completer: UpdateCompleter) =
    update(this::update, LlmUsageTokenDetail, completer)

fun KotlinUpdateBuilder.updateAllColumns(record: LlmUsageTokenDetailRecord) =
    apply {
        set(usageLogId).equalTo(record::usageLogId)
        set(requestId).equalTo(record::requestId)
        set(tokenDirection).equalTo(record::tokenDirection)
        set(tokenType).equalTo(record::tokenType)
        set(cacheType).equalTo(record::cacheType)
        set(tokens).equalTo(record::tokens)
        set(billableTokens).equalTo(record::billableTokens)
        set(source).equalTo(record::source)
        set(providerField).equalTo(record::providerField)
        set(note).equalTo(record::note)
        set(createdAt).equalTo(record::createdAt)
    }

fun KotlinUpdateBuilder.updateSelectiveColumns(record: LlmUsageTokenDetailRecord) =
    apply {
        set(usageLogId).equalToWhenPresent(record::usageLogId)
        set(requestId).equalToWhenPresent(record::requestId)
        set(tokenDirection).equalToWhenPresent(record::tokenDirection)
        set(tokenType).equalToWhenPresent(record::tokenType)
        set(cacheType).equalToWhenPresent(record::cacheType)
        set(tokens).equalToWhenPresent(record::tokens)
        set(billableTokens).equalToWhenPresent(record::billableTokens)
        set(source).equalToWhenPresent(record::source)
        set(providerField).equalToWhenPresent(record::providerField)
        set(note).equalToWhenPresent(record::note)
        set(createdAt).equalToWhenPresent(record::createdAt)
    }

fun LlmUsageTokenDetailMapper.updateByPrimaryKey(record: LlmUsageTokenDetailRecord) =
    update {
        set(usageLogId).equalTo(record::usageLogId)
        set(requestId).equalTo(record::requestId)
        set(tokenDirection).equalTo(record::tokenDirection)
        set(tokenType).equalTo(record::tokenType)
        set(cacheType).equalTo(record::cacheType)
        set(tokens).equalTo(record::tokens)
        set(billableTokens).equalTo(record::billableTokens)
        set(source).equalTo(record::source)
        set(providerField).equalTo(record::providerField)
        set(note).equalTo(record::note)
        set(createdAt).equalTo(record::createdAt)
        where(id, isEqualTo(record::id))
    }

fun LlmUsageTokenDetailMapper.updateByPrimaryKeySelective(record: LlmUsageTokenDetailRecord) =
    update {
        set(usageLogId).equalToWhenPresent(record::usageLogId)
        set(requestId).equalToWhenPresent(record::requestId)
        set(tokenDirection).equalToWhenPresent(record::tokenDirection)
        set(tokenType).equalToWhenPresent(record::tokenType)
        set(cacheType).equalToWhenPresent(record::cacheType)
        set(tokens).equalToWhenPresent(record::tokens)
        set(billableTokens).equalToWhenPresent(record::billableTokens)
        set(source).equalToWhenPresent(record::source)
        set(providerField).equalToWhenPresent(record::providerField)
        set(note).equalToWhenPresent(record::note)
        set(createdAt).equalToWhenPresent(record::createdAt)
        where(id, isEqualTo(record::id))
    }