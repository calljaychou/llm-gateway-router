/*
 * Auto-generated file. Created by MyBatis Generator
 * Generation date: 2026-05-31T14:53:40.612787+08:00
 */
package com.llm.gateway.dal.mapper

import com.llm.gateway.dal.mapper.LlmUsageBillingDetailDynamicSqlSupport.LlmUsageBillingDetail
import com.llm.gateway.dal.mapper.LlmUsageBillingDetailDynamicSqlSupport.LlmUsageBillingDetail.amountCny
import com.llm.gateway.dal.mapper.LlmUsageBillingDetailDynamicSqlSupport.LlmUsageBillingDetail.cacheType
import com.llm.gateway.dal.mapper.LlmUsageBillingDetailDynamicSqlSupport.LlmUsageBillingDetail.chargeItem
import com.llm.gateway.dal.mapper.LlmUsageBillingDetailDynamicSqlSupport.LlmUsageBillingDetail.createdAt
import com.llm.gateway.dal.mapper.LlmUsageBillingDetailDynamicSqlSupport.LlmUsageBillingDetail.id
import com.llm.gateway.dal.mapper.LlmUsageBillingDetailDynamicSqlSupport.LlmUsageBillingDetail.priceCnyPerMillion
import com.llm.gateway.dal.mapper.LlmUsageBillingDetailDynamicSqlSupport.LlmUsageBillingDetail.pricingRule
import com.llm.gateway.dal.mapper.LlmUsageBillingDetailDynamicSqlSupport.LlmUsageBillingDetail.requestId
import com.llm.gateway.dal.mapper.LlmUsageBillingDetailDynamicSqlSupport.LlmUsageBillingDetail.tokenDirection
import com.llm.gateway.dal.mapper.LlmUsageBillingDetailDynamicSqlSupport.LlmUsageBillingDetail.tokenType
import com.llm.gateway.dal.mapper.LlmUsageBillingDetailDynamicSqlSupport.LlmUsageBillingDetail.tokens
import com.llm.gateway.dal.mapper.LlmUsageBillingDetailDynamicSqlSupport.LlmUsageBillingDetail.usageLogId
import com.llm.gateway.dal.model.LlmUsageBillingDetailRecord
import org.mybatis.dynamic.sql.SqlBuilder.isEqualTo
import org.mybatis.dynamic.sql.insert.render.MultiRowInsertStatementProvider
import org.mybatis.dynamic.sql.util.kotlin.*
import org.mybatis.dynamic.sql.util.kotlin.mybatis3.*

fun LlmUsageBillingDetailMapper.count(completer: CountCompleter) =
    countFrom(this::count, LlmUsageBillingDetail, completer)

fun LlmUsageBillingDetailMapper.delete(completer: DeleteCompleter) =
    deleteFrom(this::delete, LlmUsageBillingDetail, completer)

fun LlmUsageBillingDetailMapper.deleteByPrimaryKey(id_: Long) =
    delete {
        where(id, isEqualTo(id_))
    }

fun LlmUsageBillingDetailMapper.insertMultipleHelper(multipleInsertStatement: MultiRowInsertStatementProvider<LlmUsageBillingDetailRecord>) =
    insertMultiple(multipleInsertStatement.insertStatement, multipleInsertStatement.records)

fun LlmUsageBillingDetailMapper.insert(record: LlmUsageBillingDetailRecord) =
    insert(this::insert, record, LlmUsageBillingDetail) {
        map(usageLogId).toProperty("usageLogId")
        map(requestId).toProperty("requestId")
        map(chargeItem).toProperty("chargeItem")
        map(tokenDirection).toProperty("tokenDirection")
        map(tokenType).toProperty("tokenType")
        map(cacheType).toProperty("cacheType")
        map(tokens).toProperty("tokens")
        map(priceCnyPerMillion).toProperty("priceCnyPerMillion")
        map(amountCny).toProperty("amountCny")
        map(pricingRule).toProperty("pricingRule")
        map(createdAt).toProperty("createdAt")
    }

fun LlmUsageBillingDetailMapper.insertMultiple(records: Collection<LlmUsageBillingDetailRecord>) =
    insertMultiple(this::insertMultipleHelper, records, LlmUsageBillingDetail) {
        map(usageLogId).toProperty("usageLogId")
        map(requestId).toProperty("requestId")
        map(chargeItem).toProperty("chargeItem")
        map(tokenDirection).toProperty("tokenDirection")
        map(tokenType).toProperty("tokenType")
        map(cacheType).toProperty("cacheType")
        map(tokens).toProperty("tokens")
        map(priceCnyPerMillion).toProperty("priceCnyPerMillion")
        map(amountCny).toProperty("amountCny")
        map(pricingRule).toProperty("pricingRule")
        map(createdAt).toProperty("createdAt")
    }

fun LlmUsageBillingDetailMapper.insertMultiple(vararg records: LlmUsageBillingDetailRecord) =
    insertMultiple(records.toList())

fun LlmUsageBillingDetailMapper.insertSelective(record: LlmUsageBillingDetailRecord) =
    insert(this::insert, record, LlmUsageBillingDetail) {
        map(usageLogId).toPropertyWhenPresent("usageLogId", record::usageLogId)
        map(requestId).toPropertyWhenPresent("requestId", record::requestId)
        map(chargeItem).toPropertyWhenPresent("chargeItem", record::chargeItem)
        map(tokenDirection).toPropertyWhenPresent("tokenDirection", record::tokenDirection)
        map(tokenType).toPropertyWhenPresent("tokenType", record::tokenType)
        map(cacheType).toPropertyWhenPresent("cacheType", record::cacheType)
        map(tokens).toPropertyWhenPresent("tokens", record::tokens)
        map(priceCnyPerMillion).toPropertyWhenPresent("priceCnyPerMillion", record::priceCnyPerMillion)
        map(amountCny).toPropertyWhenPresent("amountCny", record::amountCny)
        map(pricingRule).toPropertyWhenPresent("pricingRule", record::pricingRule)
        map(createdAt).toPropertyWhenPresent("createdAt", record::createdAt)
    }

private val columnList = listOf(id, usageLogId, requestId, chargeItem, tokenDirection, tokenType, cacheType, tokens, priceCnyPerMillion, amountCny, pricingRule, createdAt)

fun LlmUsageBillingDetailMapper.selectOne(completer: SelectCompleter) =
    selectOne(this::selectOne, columnList, LlmUsageBillingDetail, completer)

fun LlmUsageBillingDetailMapper.select(completer: SelectCompleter) =
    selectList(this::selectMany, columnList, LlmUsageBillingDetail, completer)

fun LlmUsageBillingDetailMapper.selectDistinct(completer: SelectCompleter) =
    selectDistinct(this::selectMany, columnList, LlmUsageBillingDetail, completer)

fun LlmUsageBillingDetailMapper.selectByPrimaryKey(id_: Long) =
    selectOne {
        where(id, isEqualTo(id_))
    }

fun LlmUsageBillingDetailMapper.update(completer: UpdateCompleter) =
    update(this::update, LlmUsageBillingDetail, completer)

fun KotlinUpdateBuilder.updateAllColumns(record: LlmUsageBillingDetailRecord) =
    apply {
        set(usageLogId).equalTo(record::usageLogId)
        set(requestId).equalTo(record::requestId)
        set(chargeItem).equalTo(record::chargeItem)
        set(tokenDirection).equalTo(record::tokenDirection)
        set(tokenType).equalTo(record::tokenType)
        set(cacheType).equalTo(record::cacheType)
        set(tokens).equalTo(record::tokens)
        set(priceCnyPerMillion).equalTo(record::priceCnyPerMillion)
        set(amountCny).equalTo(record::amountCny)
        set(pricingRule).equalTo(record::pricingRule)
        set(createdAt).equalTo(record::createdAt)
    }

fun KotlinUpdateBuilder.updateSelectiveColumns(record: LlmUsageBillingDetailRecord) =
    apply {
        set(usageLogId).equalToWhenPresent(record::usageLogId)
        set(requestId).equalToWhenPresent(record::requestId)
        set(chargeItem).equalToWhenPresent(record::chargeItem)
        set(tokenDirection).equalToWhenPresent(record::tokenDirection)
        set(tokenType).equalToWhenPresent(record::tokenType)
        set(cacheType).equalToWhenPresent(record::cacheType)
        set(tokens).equalToWhenPresent(record::tokens)
        set(priceCnyPerMillion).equalToWhenPresent(record::priceCnyPerMillion)
        set(amountCny).equalToWhenPresent(record::amountCny)
        set(pricingRule).equalToWhenPresent(record::pricingRule)
        set(createdAt).equalToWhenPresent(record::createdAt)
    }

fun LlmUsageBillingDetailMapper.updateByPrimaryKey(record: LlmUsageBillingDetailRecord) =
    update {
        set(usageLogId).equalTo(record::usageLogId)
        set(requestId).equalTo(record::requestId)
        set(chargeItem).equalTo(record::chargeItem)
        set(tokenDirection).equalTo(record::tokenDirection)
        set(tokenType).equalTo(record::tokenType)
        set(cacheType).equalTo(record::cacheType)
        set(tokens).equalTo(record::tokens)
        set(priceCnyPerMillion).equalTo(record::priceCnyPerMillion)
        set(amountCny).equalTo(record::amountCny)
        set(pricingRule).equalTo(record::pricingRule)
        set(createdAt).equalTo(record::createdAt)
        where(id, isEqualTo(record::id))
    }

fun LlmUsageBillingDetailMapper.updateByPrimaryKeySelective(record: LlmUsageBillingDetailRecord) =
    update {
        set(usageLogId).equalToWhenPresent(record::usageLogId)
        set(requestId).equalToWhenPresent(record::requestId)
        set(chargeItem).equalToWhenPresent(record::chargeItem)
        set(tokenDirection).equalToWhenPresent(record::tokenDirection)
        set(tokenType).equalToWhenPresent(record::tokenType)
        set(cacheType).equalToWhenPresent(record::cacheType)
        set(tokens).equalToWhenPresent(record::tokens)
        set(priceCnyPerMillion).equalToWhenPresent(record::priceCnyPerMillion)
        set(amountCny).equalToWhenPresent(record::amountCny)
        set(pricingRule).equalToWhenPresent(record::pricingRule)
        set(createdAt).equalToWhenPresent(record::createdAt)
        where(id, isEqualTo(record::id))
    }