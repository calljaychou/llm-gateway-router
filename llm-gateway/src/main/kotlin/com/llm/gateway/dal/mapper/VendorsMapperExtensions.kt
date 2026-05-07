/*
 * Auto-generated file. Created by MyBatis Generator
 * Generation date: 2026-04-24T09:36:04.322+08:00
 */
package com.llm.gateway.dal.mapper

import com.llm.gateway.dal.mapper.VendorsDynamicSqlSupport.Vendors
import com.llm.gateway.dal.mapper.VendorsDynamicSqlSupport.Vendors.baseUrl
import com.llm.gateway.dal.mapper.VendorsDynamicSqlSupport.Vendors.createdTime
import com.llm.gateway.dal.mapper.VendorsDynamicSqlSupport.Vendors.id
import com.llm.gateway.dal.mapper.VendorsDynamicSqlSupport.Vendors.name
import com.llm.gateway.dal.mapper.VendorsDynamicSqlSupport.Vendors.status
import com.llm.gateway.dal.mapper.VendorsDynamicSqlSupport.Vendors.updatedTime
import com.llm.gateway.dal.model.VendorsRecord
import org.mybatis.dynamic.sql.SqlBuilder.isEqualTo
import org.mybatis.dynamic.sql.insert.render.MultiRowInsertStatementProvider
import org.mybatis.dynamic.sql.util.kotlin.*
import org.mybatis.dynamic.sql.util.kotlin.mybatis3.*

fun VendorsMapper.count(completer: CountCompleter) =
    countFrom(this::count, Vendors, completer)

fun VendorsMapper.delete(completer: DeleteCompleter) =
    deleteFrom(this::delete, Vendors, completer)

fun VendorsMapper.deleteByPrimaryKey(id_: Long) =
    delete {
        where(id, isEqualTo(id_))
    }

fun VendorsMapper.insertMultipleHelper(multipleInsertStatement: MultiRowInsertStatementProvider<VendorsRecord>) =
    insertMultiple(multipleInsertStatement.insertStatement, multipleInsertStatement.records)

fun VendorsMapper.insert(record: VendorsRecord) =
    insert(this::insert, record, Vendors) {
        map(name).toProperty("name")
        map(baseUrl).toProperty("baseUrl")
        map(status).toProperty("status")
        map(createdTime).toProperty("createdTime")
        map(updatedTime).toProperty("updatedTime")
    }

fun VendorsMapper.insertMultiple(records: Collection<VendorsRecord>) =
    insertMultiple(this::insertMultipleHelper, records, Vendors) {
        map(name).toProperty("name")
        map(baseUrl).toProperty("baseUrl")
        map(status).toProperty("status")
        map(createdTime).toProperty("createdTime")
        map(updatedTime).toProperty("updatedTime")
    }

fun VendorsMapper.insertMultiple(vararg records: VendorsRecord) =
    insertMultiple(records.toList())

fun VendorsMapper.insertSelective(record: VendorsRecord) =
    insert(this::insert, record, Vendors) {
        map(name).toPropertyWhenPresent("name", record::name)
        map(baseUrl).toPropertyWhenPresent("baseUrl", record::baseUrl)
        map(status).toPropertyWhenPresent("status", record::status)
        map(createdTime).toPropertyWhenPresent("createdTime", record::createdTime)
        map(updatedTime).toPropertyWhenPresent("updatedTime", record::updatedTime)
    }

private val columnList = listOf(id, name, baseUrl, status, createdTime, updatedTime)

fun VendorsMapper.selectOne(completer: SelectCompleter) =
    selectOne(this::selectOne, columnList, Vendors, completer)

fun VendorsMapper.select(completer: SelectCompleter) =
    selectList(this::selectMany, columnList, Vendors, completer)

fun VendorsMapper.selectDistinct(completer: SelectCompleter) =
    selectDistinct(this::selectMany, columnList, Vendors, completer)

fun VendorsMapper.selectByPrimaryKey(id_: Long) =
    selectOne {
        where(id, isEqualTo(id_))
    }

fun VendorsMapper.update(completer: UpdateCompleter) =
    update(this::update, Vendors, completer)

fun KotlinUpdateBuilder.updateAllColumns(record: VendorsRecord) =
    apply {
        set(name).equalTo(record::name)
        set(baseUrl).equalTo(record::baseUrl)
        set(status).equalTo(record::status)
        set(createdTime).equalTo(record::createdTime)
        set(updatedTime).equalTo(record::updatedTime)
    }

fun KotlinUpdateBuilder.updateSelectiveColumns(record: VendorsRecord) =
    apply {
        set(name).equalToWhenPresent(record::name)
        set(baseUrl).equalToWhenPresent(record::baseUrl)
        set(status).equalToWhenPresent(record::status)
        set(createdTime).equalToWhenPresent(record::createdTime)
        set(updatedTime).equalToWhenPresent(record::updatedTime)
    }

fun VendorsMapper.updateByPrimaryKey(record: VendorsRecord) =
    update {
        set(name).equalTo(record::name)
        set(baseUrl).equalTo(record::baseUrl)
        set(status).equalTo(record::status)
        set(createdTime).equalTo(record::createdTime)
        set(updatedTime).equalTo(record::updatedTime)
        where(id, isEqualTo(record::id))
    }

fun VendorsMapper.updateByPrimaryKeySelective(record: VendorsRecord) =
    update {
        set(name).equalToWhenPresent(record::name)
        set(baseUrl).equalToWhenPresent(record::baseUrl)
        set(status).equalToWhenPresent(record::status)
        set(createdTime).equalToWhenPresent(record::createdTime)
        set(updatedTime).equalToWhenPresent(record::updatedTime)
        where(id, isEqualTo(record::id))
    }