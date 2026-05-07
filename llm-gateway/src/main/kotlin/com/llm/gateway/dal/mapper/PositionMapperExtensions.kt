/*
 * Auto-generated file. Created by MyBatis Generator
 * Generation date: 2026-04-24T09:36:04.32+08:00
 */
package com.llm.gateway.dal.mapper

import com.llm.gateway.dal.mapper.PositionDynamicSqlSupport.Position
import com.llm.gateway.dal.mapper.PositionDynamicSqlSupport.Position.createdBy
import com.llm.gateway.dal.mapper.PositionDynamicSqlSupport.Position.createdTime
import com.llm.gateway.dal.mapper.PositionDynamicSqlSupport.Position.id
import com.llm.gateway.dal.mapper.PositionDynamicSqlSupport.Position.postCode
import com.llm.gateway.dal.mapper.PositionDynamicSqlSupport.Position.postName
import com.llm.gateway.dal.mapper.PositionDynamicSqlSupport.Position.postSort
import com.llm.gateway.dal.mapper.PositionDynamicSqlSupport.Position.status
import com.llm.gateway.dal.mapper.PositionDynamicSqlSupport.Position.updatedBy
import com.llm.gateway.dal.mapper.PositionDynamicSqlSupport.Position.updatedTime
import com.llm.gateway.dal.model.PositionRecord
import org.mybatis.dynamic.sql.SqlBuilder.isEqualTo
import org.mybatis.dynamic.sql.insert.render.MultiRowInsertStatementProvider
import org.mybatis.dynamic.sql.util.kotlin.*
import org.mybatis.dynamic.sql.util.kotlin.mybatis3.*

fun PositionMapper.count(completer: CountCompleter) =
    countFrom(this::count, Position, completer)

fun PositionMapper.delete(completer: DeleteCompleter) =
    deleteFrom(this::delete, Position, completer)

fun PositionMapper.deleteByPrimaryKey(id_: Long) =
    delete {
        where(id, isEqualTo(id_))
    }

fun PositionMapper.insertMultipleHelper(multipleInsertStatement: MultiRowInsertStatementProvider<PositionRecord>) =
    insertMultiple(multipleInsertStatement.insertStatement, multipleInsertStatement.records)

fun PositionMapper.insert(record: PositionRecord) =
    insert(this::insert, record, Position) {
        map(postCode).toProperty("postCode")
        map(postName).toProperty("postName")
        map(postSort).toProperty("postSort")
        map(status).toProperty("status")
        map(createdBy).toProperty("createdBy")
        map(updatedBy).toProperty("updatedBy")
        map(createdTime).toProperty("createdTime")
        map(updatedTime).toProperty("updatedTime")
    }

fun PositionMapper.insertMultiple(records: Collection<PositionRecord>) =
    insertMultiple(this::insertMultipleHelper, records, Position) {
        map(postCode).toProperty("postCode")
        map(postName).toProperty("postName")
        map(postSort).toProperty("postSort")
        map(status).toProperty("status")
        map(createdBy).toProperty("createdBy")
        map(updatedBy).toProperty("updatedBy")
        map(createdTime).toProperty("createdTime")
        map(updatedTime).toProperty("updatedTime")
    }

fun PositionMapper.insertMultiple(vararg records: PositionRecord) =
    insertMultiple(records.toList())

fun PositionMapper.insertSelective(record: PositionRecord) =
    insert(this::insert, record, Position) {
        map(postCode).toPropertyWhenPresent("postCode", record::postCode)
        map(postName).toPropertyWhenPresent("postName", record::postName)
        map(postSort).toPropertyWhenPresent("postSort", record::postSort)
        map(status).toPropertyWhenPresent("status", record::status)
        map(createdBy).toPropertyWhenPresent("createdBy", record::createdBy)
        map(updatedBy).toPropertyWhenPresent("updatedBy", record::updatedBy)
        map(createdTime).toPropertyWhenPresent("createdTime", record::createdTime)
        map(updatedTime).toPropertyWhenPresent("updatedTime", record::updatedTime)
    }

private val columnList = listOf(id, postCode, postName, postSort, status, createdBy, updatedBy, createdTime, updatedTime)

fun PositionMapper.selectOne(completer: SelectCompleter) =
    selectOne(this::selectOne, columnList, Position, completer)

fun PositionMapper.select(completer: SelectCompleter) =
    selectList(this::selectMany, columnList, Position, completer)

fun PositionMapper.selectDistinct(completer: SelectCompleter) =
    selectDistinct(this::selectMany, columnList, Position, completer)

fun PositionMapper.selectByPrimaryKey(id_: Long) =
    selectOne {
        where(id, isEqualTo(id_))
    }

fun PositionMapper.update(completer: UpdateCompleter) =
    update(this::update, Position, completer)

fun KotlinUpdateBuilder.updateAllColumns(record: PositionRecord) =
    apply {
        set(postCode).equalTo(record::postCode)
        set(postName).equalTo(record::postName)
        set(postSort).equalTo(record::postSort)
        set(status).equalTo(record::status)
        set(createdBy).equalTo(record::createdBy)
        set(updatedBy).equalTo(record::updatedBy)
        set(createdTime).equalTo(record::createdTime)
        set(updatedTime).equalTo(record::updatedTime)
    }

fun KotlinUpdateBuilder.updateSelectiveColumns(record: PositionRecord) =
    apply {
        set(postCode).equalToWhenPresent(record::postCode)
        set(postName).equalToWhenPresent(record::postName)
        set(postSort).equalToWhenPresent(record::postSort)
        set(status).equalToWhenPresent(record::status)
        set(createdBy).equalToWhenPresent(record::createdBy)
        set(updatedBy).equalToWhenPresent(record::updatedBy)
        set(createdTime).equalToWhenPresent(record::createdTime)
        set(updatedTime).equalToWhenPresent(record::updatedTime)
    }

fun PositionMapper.updateByPrimaryKey(record: PositionRecord) =
    update {
        set(postCode).equalTo(record::postCode)
        set(postName).equalTo(record::postName)
        set(postSort).equalTo(record::postSort)
        set(status).equalTo(record::status)
        set(createdBy).equalTo(record::createdBy)
        set(updatedBy).equalTo(record::updatedBy)
        set(createdTime).equalTo(record::createdTime)
        set(updatedTime).equalTo(record::updatedTime)
        where(id, isEqualTo(record::id))
    }

fun PositionMapper.updateByPrimaryKeySelective(record: PositionRecord) =
    update {
        set(postCode).equalToWhenPresent(record::postCode)
        set(postName).equalToWhenPresent(record::postName)
        set(postSort).equalToWhenPresent(record::postSort)
        set(status).equalToWhenPresent(record::status)
        set(createdBy).equalToWhenPresent(record::createdBy)
        set(updatedBy).equalToWhenPresent(record::updatedBy)
        set(createdTime).equalToWhenPresent(record::createdTime)
        set(updatedTime).equalToWhenPresent(record::updatedTime)
        where(id, isEqualTo(record::id))
    }