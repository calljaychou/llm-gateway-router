/*
 * Auto-generated file. Created by MyBatis Generator
 * Generation date: 2026-05-08T17:25:34.942384+08:00
 */
package com.llm.gateway.dal.mapper

import com.llm.gateway.dal.mapper.DepartmentDynamicSqlSupport.Department
import com.llm.gateway.dal.mapper.DepartmentDynamicSqlSupport.Department.createdTime
import com.llm.gateway.dal.mapper.DepartmentDynamicSqlSupport.Department.delFlag
import com.llm.gateway.dal.mapper.DepartmentDynamicSqlSupport.Department.deptName
import com.llm.gateway.dal.mapper.DepartmentDynamicSqlSupport.Department.id
import com.llm.gateway.dal.mapper.DepartmentDynamicSqlSupport.Department.leaderUserId
import com.llm.gateway.dal.mapper.DepartmentDynamicSqlSupport.Department.orderNum
import com.llm.gateway.dal.mapper.DepartmentDynamicSqlSupport.Department.parentId
import com.llm.gateway.dal.mapper.DepartmentDynamicSqlSupport.Department.status
import com.llm.gateway.dal.mapper.DepartmentDynamicSqlSupport.Department.tel
import com.llm.gateway.dal.mapper.DepartmentDynamicSqlSupport.Department.updatedTime
import com.llm.gateway.dal.model.DepartmentRecord
import org.mybatis.dynamic.sql.SqlBuilder.isEqualTo
import org.mybatis.dynamic.sql.insert.render.MultiRowInsertStatementProvider
import org.mybatis.dynamic.sql.util.kotlin.*
import org.mybatis.dynamic.sql.util.kotlin.mybatis3.*

fun DepartmentMapper.count(completer: CountCompleter) =
    countFrom(this::count, Department, completer)

fun DepartmentMapper.delete(completer: DeleteCompleter) =
    deleteFrom(this::delete, Department, completer)

fun DepartmentMapper.deleteByPrimaryKey(id_: Long) =
    delete {
        where(id, isEqualTo(id_))
    }

fun DepartmentMapper.insertMultipleHelper(multipleInsertStatement: MultiRowInsertStatementProvider<DepartmentRecord>) =
    insertMultiple(multipleInsertStatement.insertStatement, multipleInsertStatement.records)

fun DepartmentMapper.insert(record: DepartmentRecord) =
    insert(this::insert, record, Department) {
        map(parentId).toProperty("parentId")
        map(deptName).toProperty("deptName")
        map(orderNum).toProperty("orderNum")
        map(leaderUserId).toProperty("leaderUserId")
        map(tel).toProperty("tel")
        map(status).toProperty("status")
        map(delFlag).toProperty("delFlag")
        map(createdTime).toProperty("createdTime")
        map(updatedTime).toProperty("updatedTime")
    }

fun DepartmentMapper.insertMultiple(records: Collection<DepartmentRecord>) =
    insertMultiple(this::insertMultipleHelper, records, Department) {
        map(parentId).toProperty("parentId")
        map(deptName).toProperty("deptName")
        map(orderNum).toProperty("orderNum")
        map(leaderUserId).toProperty("leaderUserId")
        map(tel).toProperty("tel")
        map(status).toProperty("status")
        map(delFlag).toProperty("delFlag")
        map(createdTime).toProperty("createdTime")
        map(updatedTime).toProperty("updatedTime")
    }

fun DepartmentMapper.insertMultiple(vararg records: DepartmentRecord) =
    insertMultiple(records.toList())

fun DepartmentMapper.insertSelective(record: DepartmentRecord) =
    insert(this::insert, record, Department) {
        map(parentId).toPropertyWhenPresent("parentId", record::parentId)
        map(deptName).toPropertyWhenPresent("deptName", record::deptName)
        map(orderNum).toPropertyWhenPresent("orderNum", record::orderNum)
        map(leaderUserId).toPropertyWhenPresent("leaderUserId", record::leaderUserId)
        map(tel).toPropertyWhenPresent("tel", record::tel)
        map(status).toPropertyWhenPresent("status", record::status)
        map(delFlag).toPropertyWhenPresent("delFlag", record::delFlag)
        map(createdTime).toPropertyWhenPresent("createdTime", record::createdTime)
        map(updatedTime).toPropertyWhenPresent("updatedTime", record::updatedTime)
    }

private val columnList = listOf(id, parentId, deptName, orderNum, leaderUserId, tel, status, delFlag, createdTime, updatedTime)

fun DepartmentMapper.selectOne(completer: SelectCompleter) =
    selectOne(this::selectOne, columnList, Department, completer)

fun DepartmentMapper.select(completer: SelectCompleter) =
    selectList(this::selectMany, columnList, Department, completer)

fun DepartmentMapper.selectDistinct(completer: SelectCompleter) =
    selectDistinct(this::selectMany, columnList, Department, completer)

fun DepartmentMapper.selectByPrimaryKey(id_: Long) =
    selectOne {
        where(id, isEqualTo(id_))
    }

fun DepartmentMapper.update(completer: UpdateCompleter) =
    update(this::update, Department, completer)

fun KotlinUpdateBuilder.updateAllColumns(record: DepartmentRecord) =
    apply {
        set(parentId).equalTo(record::parentId)
        set(deptName).equalTo(record::deptName)
        set(orderNum).equalTo(record::orderNum)
        set(leaderUserId).equalTo(record::leaderUserId)
        set(tel).equalTo(record::tel)
        set(status).equalTo(record::status)
        set(delFlag).equalTo(record::delFlag)
        set(createdTime).equalTo(record::createdTime)
        set(updatedTime).equalTo(record::updatedTime)
    }

fun KotlinUpdateBuilder.updateSelectiveColumns(record: DepartmentRecord) =
    apply {
        set(parentId).equalToWhenPresent(record::parentId)
        set(deptName).equalToWhenPresent(record::deptName)
        set(orderNum).equalToWhenPresent(record::orderNum)
        set(leaderUserId).equalToWhenPresent(record::leaderUserId)
        set(tel).equalToWhenPresent(record::tel)
        set(status).equalToWhenPresent(record::status)
        set(delFlag).equalToWhenPresent(record::delFlag)
        set(createdTime).equalToWhenPresent(record::createdTime)
        set(updatedTime).equalToWhenPresent(record::updatedTime)
    }

fun DepartmentMapper.updateByPrimaryKey(record: DepartmentRecord) =
    update {
        set(parentId).equalTo(record::parentId)
        set(deptName).equalTo(record::deptName)
        set(orderNum).equalTo(record::orderNum)
        set(leaderUserId).equalTo(record::leaderUserId)
        set(tel).equalTo(record::tel)
        set(status).equalTo(record::status)
        set(delFlag).equalTo(record::delFlag)
        set(createdTime).equalTo(record::createdTime)
        set(updatedTime).equalTo(record::updatedTime)
        where(id, isEqualTo(record::id))
    }

fun DepartmentMapper.updateByPrimaryKeySelective(record: DepartmentRecord) =
    update {
        set(parentId).equalToWhenPresent(record::parentId)
        set(deptName).equalToWhenPresent(record::deptName)
        set(orderNum).equalToWhenPresent(record::orderNum)
        set(leaderUserId).equalToWhenPresent(record::leaderUserId)
        set(tel).equalToWhenPresent(record::tel)
        set(status).equalToWhenPresent(record::status)
        set(delFlag).equalToWhenPresent(record::delFlag)
        set(createdTime).equalToWhenPresent(record::createdTime)
        set(updatedTime).equalToWhenPresent(record::updatedTime)
        where(id, isEqualTo(record::id))
    }