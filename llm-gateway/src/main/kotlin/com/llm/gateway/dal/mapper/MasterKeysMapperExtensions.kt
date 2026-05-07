/*
 * Auto-generated file. Created by MyBatis Generator
 * Generation date: 2026-05-07T18:46:02.171456+08:00
 */
package com.llm.gateway.dal.mapper

import com.llm.gateway.dal.mapper.MasterKeysDynamicSqlSupport.MasterKeys
import com.llm.gateway.dal.mapper.MasterKeysDynamicSqlSupport.MasterKeys.apiKeyEncrypted
import com.llm.gateway.dal.mapper.MasterKeysDynamicSqlSupport.MasterKeys.createdTime
import com.llm.gateway.dal.mapper.MasterKeysDynamicSqlSupport.MasterKeys.errorCount
import com.llm.gateway.dal.mapper.MasterKeysDynamicSqlSupport.MasterKeys.id
import com.llm.gateway.dal.mapper.MasterKeysDynamicSqlSupport.MasterKeys.lastCheckedAt
import com.llm.gateway.dal.mapper.MasterKeysDynamicSqlSupport.MasterKeys.status
import com.llm.gateway.dal.mapper.MasterKeysDynamicSqlSupport.MasterKeys.updatedTime
import com.llm.gateway.dal.mapper.MasterKeysDynamicSqlSupport.MasterKeys.vendorId
import com.llm.gateway.dal.mapper.MasterKeysDynamicSqlSupport.MasterKeys.weight
import com.llm.gateway.dal.model.MasterKeysRecord
import org.mybatis.dynamic.sql.SqlBuilder.isEqualTo
import org.mybatis.dynamic.sql.insert.render.MultiRowInsertStatementProvider
import org.mybatis.dynamic.sql.util.kotlin.*
import org.mybatis.dynamic.sql.util.kotlin.mybatis3.*

fun MasterKeysMapper.count(completer: CountCompleter) =
    countFrom(this::count, MasterKeys, completer)

fun MasterKeysMapper.delete(completer: DeleteCompleter) =
    deleteFrom(this::delete, MasterKeys, completer)

fun MasterKeysMapper.deleteByPrimaryKey(id_: Long) =
    delete {
        where(id, isEqualTo(id_))
    }

fun MasterKeysMapper.insertMultipleHelper(multipleInsertStatement: MultiRowInsertStatementProvider<MasterKeysRecord>) =
    insertMultiple(multipleInsertStatement.insertStatement, multipleInsertStatement.records)

fun MasterKeysMapper.insert(record: MasterKeysRecord) =
    insert(this::insert, record, MasterKeys) {
        map(vendorId).toProperty("vendorId")
        map(weight).toProperty("weight")
        map(status).toProperty("status")
        map(errorCount).toProperty("errorCount")
        map(lastCheckedAt).toProperty("lastCheckedAt")
        map(createdTime).toProperty("createdTime")
        map(updatedTime).toProperty("updatedTime")
        map(apiKeyEncrypted).toProperty("apiKeyEncrypted")
    }

fun MasterKeysMapper.insertMultiple(records: Collection<MasterKeysRecord>) =
    insertMultiple(this::insertMultipleHelper, records, MasterKeys) {
        map(vendorId).toProperty("vendorId")
        map(weight).toProperty("weight")
        map(status).toProperty("status")
        map(errorCount).toProperty("errorCount")
        map(lastCheckedAt).toProperty("lastCheckedAt")
        map(createdTime).toProperty("createdTime")
        map(updatedTime).toProperty("updatedTime")
        map(apiKeyEncrypted).toProperty("apiKeyEncrypted")
    }

fun MasterKeysMapper.insertMultiple(vararg records: MasterKeysRecord) =
    insertMultiple(records.toList())

fun MasterKeysMapper.insertSelective(record: MasterKeysRecord) =
    insert(this::insert, record, MasterKeys) {
        map(vendorId).toPropertyWhenPresent("vendorId", record::vendorId)
        map(weight).toPropertyWhenPresent("weight", record::weight)
        map(status).toPropertyWhenPresent("status", record::status)
        map(errorCount).toPropertyWhenPresent("errorCount", record::errorCount)
        map(lastCheckedAt).toPropertyWhenPresent("lastCheckedAt", record::lastCheckedAt)
        map(createdTime).toPropertyWhenPresent("createdTime", record::createdTime)
        map(updatedTime).toPropertyWhenPresent("updatedTime", record::updatedTime)
        map(apiKeyEncrypted).toPropertyWhenPresent("apiKeyEncrypted", record::apiKeyEncrypted)
    }

private val columnList = listOf(id, vendorId, weight, status, errorCount, lastCheckedAt, createdTime, updatedTime, apiKeyEncrypted)

fun MasterKeysMapper.selectOne(completer: SelectCompleter) =
    selectOne(this::selectOne, columnList, MasterKeys, completer)

fun MasterKeysMapper.select(completer: SelectCompleter) =
    selectList(this::selectMany, columnList, MasterKeys, completer)

fun MasterKeysMapper.selectDistinct(completer: SelectCompleter) =
    selectDistinct(this::selectMany, columnList, MasterKeys, completer)

fun MasterKeysMapper.selectByPrimaryKey(id_: Long) =
    selectOne {
        where(id, isEqualTo(id_))
    }

fun MasterKeysMapper.update(completer: UpdateCompleter) =
    update(this::update, MasterKeys, completer)

fun KotlinUpdateBuilder.updateAllColumns(record: MasterKeysRecord) =
    apply {
        set(vendorId).equalTo(record::vendorId)
        set(weight).equalTo(record::weight)
        set(status).equalTo(record::status)
        set(errorCount).equalTo(record::errorCount)
        set(lastCheckedAt).equalTo(record::lastCheckedAt)
        set(createdTime).equalTo(record::createdTime)
        set(updatedTime).equalTo(record::updatedTime)
        set(apiKeyEncrypted).equalTo(record::apiKeyEncrypted)
    }

fun KotlinUpdateBuilder.updateSelectiveColumns(record: MasterKeysRecord) =
    apply {
        set(vendorId).equalToWhenPresent(record::vendorId)
        set(weight).equalToWhenPresent(record::weight)
        set(status).equalToWhenPresent(record::status)
        set(errorCount).equalToWhenPresent(record::errorCount)
        set(lastCheckedAt).equalToWhenPresent(record::lastCheckedAt)
        set(createdTime).equalToWhenPresent(record::createdTime)
        set(updatedTime).equalToWhenPresent(record::updatedTime)
        set(apiKeyEncrypted).equalToWhenPresent(record::apiKeyEncrypted)
    }

fun MasterKeysMapper.updateByPrimaryKey(record: MasterKeysRecord) =
    update {
        set(vendorId).equalTo(record::vendorId)
        set(weight).equalTo(record::weight)
        set(status).equalTo(record::status)
        set(errorCount).equalTo(record::errorCount)
        set(lastCheckedAt).equalTo(record::lastCheckedAt)
        set(createdTime).equalTo(record::createdTime)
        set(updatedTime).equalTo(record::updatedTime)
        set(apiKeyEncrypted).equalTo(record::apiKeyEncrypted)
        where(id, isEqualTo(record::id))
    }

fun MasterKeysMapper.updateByPrimaryKeySelective(record: MasterKeysRecord) =
    update {
        set(vendorId).equalToWhenPresent(record::vendorId)
        set(weight).equalToWhenPresent(record::weight)
        set(status).equalToWhenPresent(record::status)
        set(errorCount).equalToWhenPresent(record::errorCount)
        set(lastCheckedAt).equalToWhenPresent(record::lastCheckedAt)
        set(createdTime).equalToWhenPresent(record::createdTime)
        set(updatedTime).equalToWhenPresent(record::updatedTime)
        set(apiKeyEncrypted).equalToWhenPresent(record::apiKeyEncrypted)
        where(id, isEqualTo(record::id))
    }