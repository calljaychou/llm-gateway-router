/*
 * Auto-generated file. Created by MyBatis Generator
 * Generation date: 2026-04-24T09:36:04.322+08:00
 */
package com.llm.gateway.dal.mapper

import com.llm.gateway.dal.mapper.UserRoleRelDynamicSqlSupport.UserRoleRel
import com.llm.gateway.dal.mapper.UserRoleRelDynamicSqlSupport.UserRoleRel.roleId
import com.llm.gateway.dal.mapper.UserRoleRelDynamicSqlSupport.UserRoleRel.userId
import com.llm.gateway.dal.model.UserRoleRelRecord
import org.mybatis.dynamic.sql.SqlBuilder.isEqualTo
import org.mybatis.dynamic.sql.util.kotlin.*
import org.mybatis.dynamic.sql.util.kotlin.mybatis3.*

fun UserRoleRelMapper.count(completer: CountCompleter) =
    countFrom(this::count, UserRoleRel, completer)

fun UserRoleRelMapper.delete(completer: DeleteCompleter) =
    deleteFrom(this::delete, UserRoleRel, completer)

fun UserRoleRelMapper.deleteByPrimaryKey(userId_: Long, roleId_: Long) =
    delete {
        where(userId, isEqualTo(userId_))
        and(roleId, isEqualTo(roleId_))
    }

fun UserRoleRelMapper.insert(record: UserRoleRelRecord) =
    insert(this::insert, record, UserRoleRel) {
        map(userId).toProperty("userId")
        map(roleId).toProperty("roleId")
    }

fun UserRoleRelMapper.insertMultiple(records: Collection<UserRoleRelRecord>) =
    insertMultiple(this::insertMultiple, records, UserRoleRel) {
        map(userId).toProperty("userId")
        map(roleId).toProperty("roleId")
    }

fun UserRoleRelMapper.insertMultiple(vararg records: UserRoleRelRecord) =
    insertMultiple(records.toList())

fun UserRoleRelMapper.insertSelective(record: UserRoleRelRecord) =
    insert(this::insert, record, UserRoleRel) {
        map(userId).toPropertyWhenPresent("userId", record::userId)
        map(roleId).toPropertyWhenPresent("roleId", record::roleId)
    }

private val columnList = listOf(userId, roleId)

fun UserRoleRelMapper.selectOne(completer: SelectCompleter) =
    selectOne(this::selectOne, columnList, UserRoleRel, completer)

fun UserRoleRelMapper.select(completer: SelectCompleter) =
    selectList(this::selectMany, columnList, UserRoleRel, completer)

fun UserRoleRelMapper.selectDistinct(completer: SelectCompleter) =
    selectDistinct(this::selectMany, columnList, UserRoleRel, completer)

fun UserRoleRelMapper.update(completer: UpdateCompleter) =
    update(this::update, UserRoleRel, completer)

fun KotlinUpdateBuilder.updateAllColumns(record: UserRoleRelRecord) =
    apply {
        set(userId).equalTo(record::userId)
        set(roleId).equalTo(record::roleId)
    }

fun KotlinUpdateBuilder.updateSelectiveColumns(record: UserRoleRelRecord) =
    apply {
        set(userId).equalToWhenPresent(record::userId)
        set(roleId).equalToWhenPresent(record::roleId)
    }