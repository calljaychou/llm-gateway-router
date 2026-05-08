/*
 * Auto-generated file. Created by MyBatis Generator
 * Generation date: 2026-05-08T17:25:34.950845+08:00
 */
package com.llm.gateway.dal.mapper

import com.llm.gateway.dal.mapper.ModelsDynamicSqlSupport.Models
import com.llm.gateway.dal.mapper.ModelsDynamicSqlSupport.Models.active
import com.llm.gateway.dal.mapper.ModelsDynamicSqlSupport.Models.billingType
import com.llm.gateway.dal.mapper.ModelsDynamicSqlSupport.Models.createdTime
import com.llm.gateway.dal.mapper.ModelsDynamicSqlSupport.Models.id
import com.llm.gateway.dal.mapper.ModelsDynamicSqlSupport.Models.modelAlias
import com.llm.gateway.dal.mapper.ModelsDynamicSqlSupport.Models.realModelName
import com.llm.gateway.dal.mapper.ModelsDynamicSqlSupport.Models.updatedTime
import com.llm.gateway.dal.mapper.ModelsDynamicSqlSupport.Models.vendorId
import com.llm.gateway.dal.model.ModelsRecord
import org.mybatis.dynamic.sql.SqlBuilder.isEqualTo
import org.mybatis.dynamic.sql.insert.render.MultiRowInsertStatementProvider
import org.mybatis.dynamic.sql.util.kotlin.*
import org.mybatis.dynamic.sql.util.kotlin.mybatis3.*

fun ModelsMapper.count(completer: CountCompleter) =
    countFrom(this::count, Models, completer)

fun ModelsMapper.delete(completer: DeleteCompleter) =
    deleteFrom(this::delete, Models, completer)

fun ModelsMapper.deleteByPrimaryKey(id_: Long) =
    delete {
        where(id, isEqualTo(id_))
    }

fun ModelsMapper.insertMultipleHelper(multipleInsertStatement: MultiRowInsertStatementProvider<ModelsRecord>) =
    insertMultiple(multipleInsertStatement.insertStatement, multipleInsertStatement.records)

fun ModelsMapper.insert(record: ModelsRecord) =
    insert(this::insert, record, Models) {
        map(modelAlias).toProperty("modelAlias")
        map(realModelName).toProperty("realModelName")
        map(vendorId).toProperty("vendorId")
        map(billingType).toProperty("billingType")
        map(active).toProperty("active")
        map(createdTime).toProperty("createdTime")
        map(updatedTime).toProperty("updatedTime")
    }

fun ModelsMapper.insertMultiple(records: Collection<ModelsRecord>) =
    insertMultiple(this::insertMultipleHelper, records, Models) {
        map(modelAlias).toProperty("modelAlias")
        map(realModelName).toProperty("realModelName")
        map(vendorId).toProperty("vendorId")
        map(billingType).toProperty("billingType")
        map(active).toProperty("active")
        map(createdTime).toProperty("createdTime")
        map(updatedTime).toProperty("updatedTime")
    }

fun ModelsMapper.insertMultiple(vararg records: ModelsRecord) =
    insertMultiple(records.toList())

fun ModelsMapper.insertSelective(record: ModelsRecord) =
    insert(this::insert, record, Models) {
        map(modelAlias).toPropertyWhenPresent("modelAlias", record::modelAlias)
        map(realModelName).toPropertyWhenPresent("realModelName", record::realModelName)
        map(vendorId).toPropertyWhenPresent("vendorId", record::vendorId)
        map(billingType).toPropertyWhenPresent("billingType", record::billingType)
        map(active).toPropertyWhenPresent("active", record::active)
        map(createdTime).toPropertyWhenPresent("createdTime", record::createdTime)
        map(updatedTime).toPropertyWhenPresent("updatedTime", record::updatedTime)
    }

private val columnList = listOf(id, modelAlias, realModelName, vendorId, billingType, active, createdTime, updatedTime)

fun ModelsMapper.selectOne(completer: SelectCompleter) =
    selectOne(this::selectOne, columnList, Models, completer)

fun ModelsMapper.select(completer: SelectCompleter) =
    selectList(this::selectMany, columnList, Models, completer)

fun ModelsMapper.selectDistinct(completer: SelectCompleter) =
    selectDistinct(this::selectMany, columnList, Models, completer)

fun ModelsMapper.selectByPrimaryKey(id_: Long) =
    selectOne {
        where(id, isEqualTo(id_))
    }

fun ModelsMapper.update(completer: UpdateCompleter) =
    update(this::update, Models, completer)

fun KotlinUpdateBuilder.updateAllColumns(record: ModelsRecord) =
    apply {
        set(modelAlias).equalTo(record::modelAlias)
        set(realModelName).equalTo(record::realModelName)
        set(vendorId).equalTo(record::vendorId)
        set(billingType).equalTo(record::billingType)
        set(active).equalTo(record::active)
        set(createdTime).equalTo(record::createdTime)
        set(updatedTime).equalTo(record::updatedTime)
    }

fun KotlinUpdateBuilder.updateSelectiveColumns(record: ModelsRecord) =
    apply {
        set(modelAlias).equalToWhenPresent(record::modelAlias)
        set(realModelName).equalToWhenPresent(record::realModelName)
        set(vendorId).equalToWhenPresent(record::vendorId)
        set(billingType).equalToWhenPresent(record::billingType)
        set(active).equalToWhenPresent(record::active)
        set(createdTime).equalToWhenPresent(record::createdTime)
        set(updatedTime).equalToWhenPresent(record::updatedTime)
    }

fun ModelsMapper.updateByPrimaryKey(record: ModelsRecord) =
    update {
        set(modelAlias).equalTo(record::modelAlias)
        set(realModelName).equalTo(record::realModelName)
        set(vendorId).equalTo(record::vendorId)
        set(billingType).equalTo(record::billingType)
        set(active).equalTo(record::active)
        set(createdTime).equalTo(record::createdTime)
        set(updatedTime).equalTo(record::updatedTime)
        where(id, isEqualTo(record::id))
    }

fun ModelsMapper.updateByPrimaryKeySelective(record: ModelsRecord) =
    update {
        set(modelAlias).equalToWhenPresent(record::modelAlias)
        set(realModelName).equalToWhenPresent(record::realModelName)
        set(vendorId).equalToWhenPresent(record::vendorId)
        set(billingType).equalToWhenPresent(record::billingType)
        set(active).equalToWhenPresent(record::active)
        set(createdTime).equalToWhenPresent(record::createdTime)
        set(updatedTime).equalToWhenPresent(record::updatedTime)
        where(id, isEqualTo(record::id))
    }