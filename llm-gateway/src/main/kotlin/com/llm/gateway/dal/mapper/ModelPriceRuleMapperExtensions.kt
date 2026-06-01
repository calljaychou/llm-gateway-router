/*
 * Auto-generated file. Created by MyBatis Generator
 * Generation date: 2026-05-31T14:53:40.613743+08:00
 */
package com.llm.gateway.dal.mapper

import com.llm.gateway.dal.mapper.ModelPriceRuleDynamicSqlSupport.ModelPriceRule
import com.llm.gateway.dal.mapper.ModelPriceRuleDynamicSqlSupport.ModelPriceRule.active
import com.llm.gateway.dal.mapper.ModelPriceRuleDynamicSqlSupport.ModelPriceRule.chargeItem
import com.llm.gateway.dal.mapper.ModelPriceRuleDynamicSqlSupport.ModelPriceRule.createdAt
import com.llm.gateway.dal.mapper.ModelPriceRuleDynamicSqlSupport.ModelPriceRule.currency
import com.llm.gateway.dal.mapper.ModelPriceRuleDynamicSqlSupport.ModelPriceRule.id
import com.llm.gateway.dal.mapper.ModelPriceRuleDynamicSqlSupport.ModelPriceRule.modelId
import com.llm.gateway.dal.mapper.ModelPriceRuleDynamicSqlSupport.ModelPriceRule.priceCnyPerMillion
import com.llm.gateway.dal.mapper.ModelPriceRuleDynamicSqlSupport.ModelPriceRule.updatedAt
import com.llm.gateway.dal.mapper.ModelPriceRuleDynamicSqlSupport.ModelPriceRule.vendorId
import com.llm.gateway.dal.model.ModelPriceRuleRecord
import org.mybatis.dynamic.sql.SqlBuilder.isEqualTo
import org.mybatis.dynamic.sql.insert.render.MultiRowInsertStatementProvider
import org.mybatis.dynamic.sql.util.kotlin.*
import org.mybatis.dynamic.sql.util.kotlin.mybatis3.*

fun ModelPriceRuleMapper.count(completer: CountCompleter) =
    countFrom(this::count, ModelPriceRule, completer)

fun ModelPriceRuleMapper.delete(completer: DeleteCompleter) =
    deleteFrom(this::delete, ModelPriceRule, completer)

fun ModelPriceRuleMapper.deleteByPrimaryKey(id_: Long) =
    delete {
        where(id, isEqualTo(id_))
    }

fun ModelPriceRuleMapper.insertMultipleHelper(multipleInsertStatement: MultiRowInsertStatementProvider<ModelPriceRuleRecord>) =
    insertMultiple(multipleInsertStatement.insertStatement, multipleInsertStatement.records)

fun ModelPriceRuleMapper.insert(record: ModelPriceRuleRecord) =
    insert(this::insert, record, ModelPriceRule) {
        map(modelId).toProperty("modelId")
        map(vendorId).toProperty("vendorId")
        map(chargeItem).toProperty("chargeItem")
        map(priceCnyPerMillion).toProperty("priceCnyPerMillion")
        map(currency).toProperty("currency")
        map(active).toProperty("active")
        map(createdAt).toProperty("createdAt")
        map(updatedAt).toProperty("updatedAt")
    }

fun ModelPriceRuleMapper.insertMultiple(records: Collection<ModelPriceRuleRecord>) =
    insertMultiple(this::insertMultipleHelper, records, ModelPriceRule) {
        map(modelId).toProperty("modelId")
        map(vendorId).toProperty("vendorId")
        map(chargeItem).toProperty("chargeItem")
        map(priceCnyPerMillion).toProperty("priceCnyPerMillion")
        map(currency).toProperty("currency")
        map(active).toProperty("active")
        map(createdAt).toProperty("createdAt")
        map(updatedAt).toProperty("updatedAt")
    }

fun ModelPriceRuleMapper.insertMultiple(vararg records: ModelPriceRuleRecord) =
    insertMultiple(records.toList())

fun ModelPriceRuleMapper.insertSelective(record: ModelPriceRuleRecord) =
    insert(this::insert, record, ModelPriceRule) {
        map(modelId).toPropertyWhenPresent("modelId", record::modelId)
        map(vendorId).toPropertyWhenPresent("vendorId", record::vendorId)
        map(chargeItem).toPropertyWhenPresent("chargeItem", record::chargeItem)
        map(priceCnyPerMillion).toPropertyWhenPresent("priceCnyPerMillion", record::priceCnyPerMillion)
        map(currency).toPropertyWhenPresent("currency", record::currency)
        map(active).toPropertyWhenPresent("active", record::active)
        map(createdAt).toPropertyWhenPresent("createdAt", record::createdAt)
        map(updatedAt).toPropertyWhenPresent("updatedAt", record::updatedAt)
    }

private val columnList = listOf(id, modelId, vendorId, chargeItem, priceCnyPerMillion, currency, active, createdAt, updatedAt)

fun ModelPriceRuleMapper.selectOne(completer: SelectCompleter) =
    selectOne(this::selectOne, columnList, ModelPriceRule, completer)

fun ModelPriceRuleMapper.select(completer: SelectCompleter) =
    selectList(this::selectMany, columnList, ModelPriceRule, completer)

fun ModelPriceRuleMapper.selectDistinct(completer: SelectCompleter) =
    selectDistinct(this::selectMany, columnList, ModelPriceRule, completer)

fun ModelPriceRuleMapper.selectByPrimaryKey(id_: Long) =
    selectOne {
        where(id, isEqualTo(id_))
    }

fun ModelPriceRuleMapper.update(completer: UpdateCompleter) =
    update(this::update, ModelPriceRule, completer)

fun KotlinUpdateBuilder.updateAllColumns(record: ModelPriceRuleRecord) =
    apply {
        set(modelId).equalTo(record::modelId)
        set(vendorId).equalTo(record::vendorId)
        set(chargeItem).equalTo(record::chargeItem)
        set(priceCnyPerMillion).equalTo(record::priceCnyPerMillion)
        set(currency).equalTo(record::currency)
        set(active).equalTo(record::active)
        set(createdAt).equalTo(record::createdAt)
        set(updatedAt).equalTo(record::updatedAt)
    }

fun KotlinUpdateBuilder.updateSelectiveColumns(record: ModelPriceRuleRecord) =
    apply {
        set(modelId).equalToWhenPresent(record::modelId)
        set(vendorId).equalToWhenPresent(record::vendorId)
        set(chargeItem).equalToWhenPresent(record::chargeItem)
        set(priceCnyPerMillion).equalToWhenPresent(record::priceCnyPerMillion)
        set(currency).equalToWhenPresent(record::currency)
        set(active).equalToWhenPresent(record::active)
        set(createdAt).equalToWhenPresent(record::createdAt)
        set(updatedAt).equalToWhenPresent(record::updatedAt)
    }

fun ModelPriceRuleMapper.updateByPrimaryKey(record: ModelPriceRuleRecord) =
    update {
        set(modelId).equalTo(record::modelId)
        set(vendorId).equalTo(record::vendorId)
        set(chargeItem).equalTo(record::chargeItem)
        set(priceCnyPerMillion).equalTo(record::priceCnyPerMillion)
        set(currency).equalTo(record::currency)
        set(active).equalTo(record::active)
        set(createdAt).equalTo(record::createdAt)
        set(updatedAt).equalTo(record::updatedAt)
        where(id, isEqualTo(record::id))
    }

fun ModelPriceRuleMapper.updateByPrimaryKeySelective(record: ModelPriceRuleRecord) =
    update {
        set(modelId).equalToWhenPresent(record::modelId)
        set(vendorId).equalToWhenPresent(record::vendorId)
        set(chargeItem).equalToWhenPresent(record::chargeItem)
        set(priceCnyPerMillion).equalToWhenPresent(record::priceCnyPerMillion)
        set(currency).equalToWhenPresent(record::currency)
        set(active).equalToWhenPresent(record::active)
        set(createdAt).equalToWhenPresent(record::createdAt)
        set(updatedAt).equalToWhenPresent(record::updatedAt)
        where(id, isEqualTo(record::id))
    }