/*
 * Auto-generated file. Created by MyBatis Generator
 * Generation date: 2026-05-07T18:46:02.174599+08:00
 */
package com.llm.gateway.dal.mapper

import com.llm.gateway.dal.mapper.UsageStatsDailyDepartmentDynamicSqlSupport.UsageStatsDailyDepartment
import com.llm.gateway.dal.mapper.UsageStatsDailyDepartmentDynamicSqlSupport.UsageStatsDailyDepartment.activeUsers
import com.llm.gateway.dal.mapper.UsageStatsDailyDepartmentDynamicSqlSupport.UsageStatsDailyDepartment.deptId
import com.llm.gateway.dal.mapper.UsageStatsDailyDepartmentDynamicSqlSupport.UsageStatsDailyDepartment.errorCnt
import com.llm.gateway.dal.mapper.UsageStatsDailyDepartmentDynamicSqlSupport.UsageStatsDailyDepartment.requestCnt
import com.llm.gateway.dal.mapper.UsageStatsDailyDepartmentDynamicSqlSupport.UsageStatsDailyDepartment.statDate
import com.llm.gateway.dal.mapper.UsageStatsDailyDepartmentDynamicSqlSupport.UsageStatsDailyDepartment.totalTokens
import com.llm.gateway.dal.mapper.UsageStatsDailyDepartmentDynamicSqlSupport.UsageStatsDailyDepartment.updatedAt
import com.llm.gateway.dal.model.UsageStatsDailyDepartmentRecord
import java.util.Date
import org.mybatis.dynamic.sql.SqlBuilder.isEqualTo
import org.mybatis.dynamic.sql.insert.render.MultiRowInsertStatementProvider
import org.mybatis.dynamic.sql.util.kotlin.*
import org.mybatis.dynamic.sql.util.kotlin.mybatis3.*

fun UsageStatsDailyDepartmentMapper.count(completer: CountCompleter) =
    countFrom(this::count, UsageStatsDailyDepartment, completer)

fun UsageStatsDailyDepartmentMapper.delete(completer: DeleteCompleter) =
    deleteFrom(this::delete, UsageStatsDailyDepartment, completer)

fun UsageStatsDailyDepartmentMapper.deleteByPrimaryKey(statDate_: Date, deptId_: Long) =
    delete {
        where(statDate, isEqualTo(statDate_))
        and(deptId, isEqualTo(deptId_))
    }

fun UsageStatsDailyDepartmentMapper.insertMultipleHelper(multipleInsertStatement: MultiRowInsertStatementProvider<UsageStatsDailyDepartmentRecord>) =
    insertMultiple(multipleInsertStatement.insertStatement, multipleInsertStatement.records)

fun UsageStatsDailyDepartmentMapper.insert(record: UsageStatsDailyDepartmentRecord) =
    insert(this::insert, record, UsageStatsDailyDepartment) {
        map(statDate).toProperty("statDate")
        map(deptId).toProperty("deptId")
        map(totalTokens).toProperty("totalTokens")
        map(requestCnt).toProperty("requestCnt")
        map(errorCnt).toProperty("errorCnt")
        map(activeUsers).toProperty("activeUsers")
        map(updatedAt).toProperty("updatedAt")
    }

fun UsageStatsDailyDepartmentMapper.insertMultiple(records: Collection<UsageStatsDailyDepartmentRecord>) =
    insertMultiple(this::insertMultipleHelper, records, UsageStatsDailyDepartment) {
        map(statDate).toProperty("statDate")
        map(deptId).toProperty("deptId")
        map(totalTokens).toProperty("totalTokens")
        map(requestCnt).toProperty("requestCnt")
        map(errorCnt).toProperty("errorCnt")
        map(activeUsers).toProperty("activeUsers")
        map(updatedAt).toProperty("updatedAt")
    }

fun UsageStatsDailyDepartmentMapper.insertMultiple(vararg records: UsageStatsDailyDepartmentRecord) =
    insertMultiple(records.toList())

fun UsageStatsDailyDepartmentMapper.insertSelective(record: UsageStatsDailyDepartmentRecord) =
    insert(this::insert, record, UsageStatsDailyDepartment) {
        map(statDate).toPropertyWhenPresent("statDate", record::statDate)
        map(deptId).toPropertyWhenPresent("deptId", record::deptId)
        map(totalTokens).toPropertyWhenPresent("totalTokens", record::totalTokens)
        map(requestCnt).toPropertyWhenPresent("requestCnt", record::requestCnt)
        map(errorCnt).toPropertyWhenPresent("errorCnt", record::errorCnt)
        map(activeUsers).toPropertyWhenPresent("activeUsers", record::activeUsers)
        map(updatedAt).toPropertyWhenPresent("updatedAt", record::updatedAt)
    }

private val columnList = listOf(statDate, deptId, totalTokens, requestCnt, errorCnt, activeUsers, updatedAt)

fun UsageStatsDailyDepartmentMapper.selectOne(completer: SelectCompleter) =
    selectOne(this::selectOne, columnList, UsageStatsDailyDepartment, completer)

fun UsageStatsDailyDepartmentMapper.select(completer: SelectCompleter) =
    selectList(this::selectMany, columnList, UsageStatsDailyDepartment, completer)

fun UsageStatsDailyDepartmentMapper.selectDistinct(completer: SelectCompleter) =
    selectDistinct(this::selectMany, columnList, UsageStatsDailyDepartment, completer)

fun UsageStatsDailyDepartmentMapper.selectByPrimaryKey(statDate_: Date, deptId_: Long) =
    selectOne {
        where(statDate, isEqualTo(statDate_))
        and(deptId, isEqualTo(deptId_))
    }

fun UsageStatsDailyDepartmentMapper.update(completer: UpdateCompleter) =
    update(this::update, UsageStatsDailyDepartment, completer)

fun KotlinUpdateBuilder.updateAllColumns(record: UsageStatsDailyDepartmentRecord) =
    apply {
        set(statDate).equalTo(record::statDate)
        set(deptId).equalTo(record::deptId)
        set(totalTokens).equalTo(record::totalTokens)
        set(requestCnt).equalTo(record::requestCnt)
        set(errorCnt).equalTo(record::errorCnt)
        set(activeUsers).equalTo(record::activeUsers)
        set(updatedAt).equalTo(record::updatedAt)
    }

fun KotlinUpdateBuilder.updateSelectiveColumns(record: UsageStatsDailyDepartmentRecord) =
    apply {
        set(statDate).equalToWhenPresent(record::statDate)
        set(deptId).equalToWhenPresent(record::deptId)
        set(totalTokens).equalToWhenPresent(record::totalTokens)
        set(requestCnt).equalToWhenPresent(record::requestCnt)
        set(errorCnt).equalToWhenPresent(record::errorCnt)
        set(activeUsers).equalToWhenPresent(record::activeUsers)
        set(updatedAt).equalToWhenPresent(record::updatedAt)
    }

fun UsageStatsDailyDepartmentMapper.updateByPrimaryKey(record: UsageStatsDailyDepartmentRecord) =
    update {
        set(totalTokens).equalTo(record::totalTokens)
        set(requestCnt).equalTo(record::requestCnt)
        set(errorCnt).equalTo(record::errorCnt)
        set(activeUsers).equalTo(record::activeUsers)
        set(updatedAt).equalTo(record::updatedAt)
        where(statDate, isEqualTo(record::statDate))
        and(deptId, isEqualTo(record::deptId))
    }

fun UsageStatsDailyDepartmentMapper.updateByPrimaryKeySelective(record: UsageStatsDailyDepartmentRecord) =
    update {
        set(totalTokens).equalToWhenPresent(record::totalTokens)
        set(requestCnt).equalToWhenPresent(record::requestCnt)
        set(errorCnt).equalToWhenPresent(record::errorCnt)
        set(activeUsers).equalToWhenPresent(record::activeUsers)
        set(updatedAt).equalToWhenPresent(record::updatedAt)
        where(statDate, isEqualTo(record::statDate))
        and(deptId, isEqualTo(record::deptId))
    }