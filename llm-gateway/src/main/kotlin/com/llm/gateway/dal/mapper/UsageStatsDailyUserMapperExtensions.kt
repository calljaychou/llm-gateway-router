/*
 * Auto-generated file. Created by MyBatis Generator
 * Generation date: 2026-05-07T18:46:02.175251+08:00
 */
package com.llm.gateway.dal.mapper

import com.llm.gateway.dal.mapper.UsageStatsDailyUserDynamicSqlSupport.UsageStatsDailyUser
import com.llm.gateway.dal.mapper.UsageStatsDailyUserDynamicSqlSupport.UsageStatsDailyUser.deptId
import com.llm.gateway.dal.mapper.UsageStatsDailyUserDynamicSqlSupport.UsageStatsDailyUser.errorCnt
import com.llm.gateway.dal.mapper.UsageStatsDailyUserDynamicSqlSupport.UsageStatsDailyUser.requestCnt
import com.llm.gateway.dal.mapper.UsageStatsDailyUserDynamicSqlSupport.UsageStatsDailyUser.statDate
import com.llm.gateway.dal.mapper.UsageStatsDailyUserDynamicSqlSupport.UsageStatsDailyUser.totalTokens
import com.llm.gateway.dal.mapper.UsageStatsDailyUserDynamicSqlSupport.UsageStatsDailyUser.updatedAt
import com.llm.gateway.dal.mapper.UsageStatsDailyUserDynamicSqlSupport.UsageStatsDailyUser.userId
import com.llm.gateway.dal.model.UsageStatsDailyUserRecord
import java.util.Date
import org.mybatis.dynamic.sql.SqlBuilder.isEqualTo
import org.mybatis.dynamic.sql.insert.render.MultiRowInsertStatementProvider
import org.mybatis.dynamic.sql.util.kotlin.*
import org.mybatis.dynamic.sql.util.kotlin.mybatis3.*

fun UsageStatsDailyUserMapper.count(completer: CountCompleter) =
    countFrom(this::count, UsageStatsDailyUser, completer)

fun UsageStatsDailyUserMapper.delete(completer: DeleteCompleter) =
    deleteFrom(this::delete, UsageStatsDailyUser, completer)

fun UsageStatsDailyUserMapper.deleteByPrimaryKey(statDate_: Date, userId_: Long) =
    delete {
        where(statDate, isEqualTo(statDate_))
        and(userId, isEqualTo(userId_))
    }

fun UsageStatsDailyUserMapper.insertMultipleHelper(multipleInsertStatement: MultiRowInsertStatementProvider<UsageStatsDailyUserRecord>) =
    insertMultiple(multipleInsertStatement.insertStatement, multipleInsertStatement.records)

fun UsageStatsDailyUserMapper.insert(record: UsageStatsDailyUserRecord) =
    insert(this::insert, record, UsageStatsDailyUser) {
        map(statDate).toProperty("statDate")
        map(userId).toProperty("userId")
        map(deptId).toProperty("deptId")
        map(totalTokens).toProperty("totalTokens")
        map(requestCnt).toProperty("requestCnt")
        map(errorCnt).toProperty("errorCnt")
        map(updatedAt).toProperty("updatedAt")
    }

fun UsageStatsDailyUserMapper.insertMultiple(records: Collection<UsageStatsDailyUserRecord>) =
    insertMultiple(this::insertMultipleHelper, records, UsageStatsDailyUser) {
        map(statDate).toProperty("statDate")
        map(userId).toProperty("userId")
        map(deptId).toProperty("deptId")
        map(totalTokens).toProperty("totalTokens")
        map(requestCnt).toProperty("requestCnt")
        map(errorCnt).toProperty("errorCnt")
        map(updatedAt).toProperty("updatedAt")
    }

fun UsageStatsDailyUserMapper.insertMultiple(vararg records: UsageStatsDailyUserRecord) =
    insertMultiple(records.toList())

fun UsageStatsDailyUserMapper.insertSelective(record: UsageStatsDailyUserRecord) =
    insert(this::insert, record, UsageStatsDailyUser) {
        map(statDate).toPropertyWhenPresent("statDate", record::statDate)
        map(userId).toPropertyWhenPresent("userId", record::userId)
        map(deptId).toPropertyWhenPresent("deptId", record::deptId)
        map(totalTokens).toPropertyWhenPresent("totalTokens", record::totalTokens)
        map(requestCnt).toPropertyWhenPresent("requestCnt", record::requestCnt)
        map(errorCnt).toPropertyWhenPresent("errorCnt", record::errorCnt)
        map(updatedAt).toPropertyWhenPresent("updatedAt", record::updatedAt)
    }

private val columnList = listOf(statDate, userId, deptId, totalTokens, requestCnt, errorCnt, updatedAt)

fun UsageStatsDailyUserMapper.selectOne(completer: SelectCompleter) =
    selectOne(this::selectOne, columnList, UsageStatsDailyUser, completer)

fun UsageStatsDailyUserMapper.select(completer: SelectCompleter) =
    selectList(this::selectMany, columnList, UsageStatsDailyUser, completer)

fun UsageStatsDailyUserMapper.selectDistinct(completer: SelectCompleter) =
    selectDistinct(this::selectMany, columnList, UsageStatsDailyUser, completer)

fun UsageStatsDailyUserMapper.selectByPrimaryKey(statDate_: Date, userId_: Long) =
    selectOne {
        where(statDate, isEqualTo(statDate_))
        and(userId, isEqualTo(userId_))
    }

fun UsageStatsDailyUserMapper.update(completer: UpdateCompleter) =
    update(this::update, UsageStatsDailyUser, completer)

fun KotlinUpdateBuilder.updateAllColumns(record: UsageStatsDailyUserRecord) =
    apply {
        set(statDate).equalTo(record::statDate)
        set(userId).equalTo(record::userId)
        set(deptId).equalTo(record::deptId)
        set(totalTokens).equalTo(record::totalTokens)
        set(requestCnt).equalTo(record::requestCnt)
        set(errorCnt).equalTo(record::errorCnt)
        set(updatedAt).equalTo(record::updatedAt)
    }

fun KotlinUpdateBuilder.updateSelectiveColumns(record: UsageStatsDailyUserRecord) =
    apply {
        set(statDate).equalToWhenPresent(record::statDate)
        set(userId).equalToWhenPresent(record::userId)
        set(deptId).equalToWhenPresent(record::deptId)
        set(totalTokens).equalToWhenPresent(record::totalTokens)
        set(requestCnt).equalToWhenPresent(record::requestCnt)
        set(errorCnt).equalToWhenPresent(record::errorCnt)
        set(updatedAt).equalToWhenPresent(record::updatedAt)
    }

fun UsageStatsDailyUserMapper.updateByPrimaryKey(record: UsageStatsDailyUserRecord) =
    update {
        set(deptId).equalTo(record::deptId)
        set(totalTokens).equalTo(record::totalTokens)
        set(requestCnt).equalTo(record::requestCnt)
        set(errorCnt).equalTo(record::errorCnt)
        set(updatedAt).equalTo(record::updatedAt)
        where(statDate, isEqualTo(record::statDate))
        and(userId, isEqualTo(record::userId))
    }

fun UsageStatsDailyUserMapper.updateByPrimaryKeySelective(record: UsageStatsDailyUserRecord) =
    update {
        set(deptId).equalToWhenPresent(record::deptId)
        set(totalTokens).equalToWhenPresent(record::totalTokens)
        set(requestCnt).equalToWhenPresent(record::requestCnt)
        set(errorCnt).equalToWhenPresent(record::errorCnt)
        set(updatedAt).equalToWhenPresent(record::updatedAt)
        where(statDate, isEqualTo(record::statDate))
        and(userId, isEqualTo(record::userId))
    }