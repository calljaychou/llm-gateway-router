/*
 * Auto-generated file. Created by MyBatis Generator
 * Generation date: 2026-04-24T09:36:04.321+08:00
 */
package com.llm.gateway.dal.mapper

import com.llm.gateway.dal.mapper.UserPostRelDynamicSqlSupport.UserPostRel
import com.llm.gateway.dal.mapper.UserPostRelDynamicSqlSupport.UserPostRel.postId
import com.llm.gateway.dal.mapper.UserPostRelDynamicSqlSupport.UserPostRel.userId
import com.llm.gateway.dal.model.UserPostRelRecord
import org.mybatis.dynamic.sql.SqlBuilder.isEqualTo
import org.mybatis.dynamic.sql.util.kotlin.*
import org.mybatis.dynamic.sql.util.kotlin.mybatis3.*

fun UserPostRelMapper.count(completer: CountCompleter) =
    countFrom(this::count, UserPostRel, completer)

fun UserPostRelMapper.delete(completer: DeleteCompleter) =
    deleteFrom(this::delete, UserPostRel, completer)

fun UserPostRelMapper.deleteByPrimaryKey(userId_: Long, postId_: Long) =
    delete {
        where(userId, isEqualTo(userId_))
        and(postId, isEqualTo(postId_))
    }

fun UserPostRelMapper.insert(record: UserPostRelRecord) =
    insert(this::insert, record, UserPostRel) {
        map(userId).toProperty("userId")
        map(postId).toProperty("postId")
    }

fun UserPostRelMapper.insertMultiple(records: Collection<UserPostRelRecord>) =
    insertMultiple(this::insertMultiple, records, UserPostRel) {
        map(userId).toProperty("userId")
        map(postId).toProperty("postId")
    }

fun UserPostRelMapper.insertMultiple(vararg records: UserPostRelRecord) =
    insertMultiple(records.toList())

fun UserPostRelMapper.insertSelective(record: UserPostRelRecord) =
    insert(this::insert, record, UserPostRel) {
        map(userId).toPropertyWhenPresent("userId", record::userId)
        map(postId).toPropertyWhenPresent("postId", record::postId)
    }

private val columnList = listOf(userId, postId)

fun UserPostRelMapper.selectOne(completer: SelectCompleter) =
    selectOne(this::selectOne, columnList, UserPostRel, completer)

fun UserPostRelMapper.select(completer: SelectCompleter) =
    selectList(this::selectMany, columnList, UserPostRel, completer)

fun UserPostRelMapper.selectDistinct(completer: SelectCompleter) =
    selectDistinct(this::selectMany, columnList, UserPostRel, completer)

fun UserPostRelMapper.update(completer: UpdateCompleter) =
    update(this::update, UserPostRel, completer)

fun KotlinUpdateBuilder.updateAllColumns(record: UserPostRelRecord) =
    apply {
        set(userId).equalTo(record::userId)
        set(postId).equalTo(record::postId)
    }

fun KotlinUpdateBuilder.updateSelectiveColumns(record: UserPostRelRecord) =
    apply {
        set(userId).equalToWhenPresent(record::userId)
        set(postId).equalToWhenPresent(record::postId)
    }