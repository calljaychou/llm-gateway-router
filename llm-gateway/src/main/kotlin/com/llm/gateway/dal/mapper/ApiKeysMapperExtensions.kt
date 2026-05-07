/*
 * Auto-generated file. Created by MyBatis Generator
 * Generation date: 2026-05-07T18:46:02.172983+08:00
 */
package com.llm.gateway.dal.mapper

import com.llm.gateway.dal.mapper.ApiKeysDynamicSqlSupport.ApiKeys
import com.llm.gateway.dal.mapper.ApiKeysDynamicSqlSupport.ApiKeys.apiKeyHash
import com.llm.gateway.dal.mapper.ApiKeysDynamicSqlSupport.ApiKeys.apiKeyPrefix
import com.llm.gateway.dal.mapper.ApiKeysDynamicSqlSupport.ApiKeys.createdTime
import com.llm.gateway.dal.mapper.ApiKeysDynamicSqlSupport.ApiKeys.expiresAt
import com.llm.gateway.dal.mapper.ApiKeysDynamicSqlSupport.ApiKeys.id
import com.llm.gateway.dal.mapper.ApiKeysDynamicSqlSupport.ApiKeys.name
import com.llm.gateway.dal.mapper.ApiKeysDynamicSqlSupport.ApiKeys.status
import com.llm.gateway.dal.mapper.ApiKeysDynamicSqlSupport.ApiKeys.updatedTime
import com.llm.gateway.dal.mapper.ApiKeysDynamicSqlSupport.ApiKeys.userId
import com.llm.gateway.dal.model.ApiKeysRecord
import org.mybatis.dynamic.sql.SqlBuilder.isEqualTo
import org.mybatis.dynamic.sql.insert.render.MultiRowInsertStatementProvider
import org.mybatis.dynamic.sql.util.kotlin.*
import org.mybatis.dynamic.sql.util.kotlin.mybatis3.*

fun ApiKeysMapper.count(completer: CountCompleter) =
    countFrom(this::count, ApiKeys, completer)

fun ApiKeysMapper.delete(completer: DeleteCompleter) =
    deleteFrom(this::delete, ApiKeys, completer)

fun ApiKeysMapper.deleteByPrimaryKey(id_: Long) =
    delete {
        where(id, isEqualTo(id_))
    }

fun ApiKeysMapper.insertMultipleHelper(multipleInsertStatement: MultiRowInsertStatementProvider<ApiKeysRecord>) =
    insertMultiple(multipleInsertStatement.insertStatement, multipleInsertStatement.records)

fun ApiKeysMapper.insert(record: ApiKeysRecord) =
    insert(this::insert, record, ApiKeys) {
        map(userId).toProperty("userId")
        map(name).toProperty("name")
        map(apiKeyHash).toProperty("apiKeyHash")
        map(apiKeyPrefix).toProperty("apiKeyPrefix")
        map(status).toProperty("status")
        map(expiresAt).toProperty("expiresAt")
        map(createdTime).toProperty("createdTime")
        map(updatedTime).toProperty("updatedTime")
    }

fun ApiKeysMapper.insertMultiple(records: Collection<ApiKeysRecord>) =
    insertMultiple(this::insertMultipleHelper, records, ApiKeys) {
        map(userId).toProperty("userId")
        map(name).toProperty("name")
        map(apiKeyHash).toProperty("apiKeyHash")
        map(apiKeyPrefix).toProperty("apiKeyPrefix")
        map(status).toProperty("status")
        map(expiresAt).toProperty("expiresAt")
        map(createdTime).toProperty("createdTime")
        map(updatedTime).toProperty("updatedTime")
    }

fun ApiKeysMapper.insertMultiple(vararg records: ApiKeysRecord) =
    insertMultiple(records.toList())

fun ApiKeysMapper.insertSelective(record: ApiKeysRecord) =
    insert(this::insert, record, ApiKeys) {
        map(userId).toPropertyWhenPresent("userId", record::userId)
        map(name).toPropertyWhenPresent("name", record::name)
        map(apiKeyHash).toPropertyWhenPresent("apiKeyHash", record::apiKeyHash)
        map(apiKeyPrefix).toPropertyWhenPresent("apiKeyPrefix", record::apiKeyPrefix)
        map(status).toPropertyWhenPresent("status", record::status)
        map(expiresAt).toPropertyWhenPresent("expiresAt", record::expiresAt)
        map(createdTime).toPropertyWhenPresent("createdTime", record::createdTime)
        map(updatedTime).toPropertyWhenPresent("updatedTime", record::updatedTime)
    }

private val columnList = listOf(id, userId, name, apiKeyHash, apiKeyPrefix, status, expiresAt, createdTime, updatedTime)

fun ApiKeysMapper.selectOne(completer: SelectCompleter) =
    selectOne(this::selectOne, columnList, ApiKeys, completer)

fun ApiKeysMapper.select(completer: SelectCompleter) =
    selectList(this::selectMany, columnList, ApiKeys, completer)

fun ApiKeysMapper.selectDistinct(completer: SelectCompleter) =
    selectDistinct(this::selectMany, columnList, ApiKeys, completer)

fun ApiKeysMapper.selectByPrimaryKey(id_: Long) =
    selectOne {
        where(id, isEqualTo(id_))
    }

fun ApiKeysMapper.update(completer: UpdateCompleter) =
    update(this::update, ApiKeys, completer)

fun KotlinUpdateBuilder.updateAllColumns(record: ApiKeysRecord) =
    apply {
        set(userId).equalTo(record::userId)
        set(name).equalTo(record::name)
        set(apiKeyHash).equalTo(record::apiKeyHash)
        set(apiKeyPrefix).equalTo(record::apiKeyPrefix)
        set(status).equalTo(record::status)
        set(expiresAt).equalTo(record::expiresAt)
        set(createdTime).equalTo(record::createdTime)
        set(updatedTime).equalTo(record::updatedTime)
    }

fun KotlinUpdateBuilder.updateSelectiveColumns(record: ApiKeysRecord) =
    apply {
        set(userId).equalToWhenPresent(record::userId)
        set(name).equalToWhenPresent(record::name)
        set(apiKeyHash).equalToWhenPresent(record::apiKeyHash)
        set(apiKeyPrefix).equalToWhenPresent(record::apiKeyPrefix)
        set(status).equalToWhenPresent(record::status)
        set(expiresAt).equalToWhenPresent(record::expiresAt)
        set(createdTime).equalToWhenPresent(record::createdTime)
        set(updatedTime).equalToWhenPresent(record::updatedTime)
    }

fun ApiKeysMapper.updateByPrimaryKey(record: ApiKeysRecord) =
    update {
        set(userId).equalTo(record::userId)
        set(name).equalTo(record::name)
        set(apiKeyHash).equalTo(record::apiKeyHash)
        set(apiKeyPrefix).equalTo(record::apiKeyPrefix)
        set(status).equalTo(record::status)
        set(expiresAt).equalTo(record::expiresAt)
        set(createdTime).equalTo(record::createdTime)
        set(updatedTime).equalTo(record::updatedTime)
        where(id, isEqualTo(record::id))
    }

fun ApiKeysMapper.updateByPrimaryKeySelective(record: ApiKeysRecord) =
    update {
        set(userId).equalToWhenPresent(record::userId)
        set(name).equalToWhenPresent(record::name)
        set(apiKeyHash).equalToWhenPresent(record::apiKeyHash)
        set(apiKeyPrefix).equalToWhenPresent(record::apiKeyPrefix)
        set(status).equalToWhenPresent(record::status)
        set(expiresAt).equalToWhenPresent(record::expiresAt)
        set(createdTime).equalToWhenPresent(record::createdTime)
        set(updatedTime).equalToWhenPresent(record::updatedTime)
        where(id, isEqualTo(record::id))
    }