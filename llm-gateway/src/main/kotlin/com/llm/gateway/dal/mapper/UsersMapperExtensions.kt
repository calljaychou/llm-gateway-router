/*
 * Auto-generated file. Created by MyBatis Generator
 * Generation date: 2026-05-08T17:25:34.946873+08:00
 */
package com.llm.gateway.dal.mapper

import com.llm.gateway.dal.mapper.UsersDynamicSqlSupport.Users
import com.llm.gateway.dal.mapper.UsersDynamicSqlSupport.Users.avatarUrl
import com.llm.gateway.dal.mapper.UsersDynamicSqlSupport.Users.createdTime
import com.llm.gateway.dal.mapper.UsersDynamicSqlSupport.Users.delFlag
import com.llm.gateway.dal.mapper.UsersDynamicSqlSupport.Users.deptId
import com.llm.gateway.dal.mapper.UsersDynamicSqlSupport.Users.email
import com.llm.gateway.dal.mapper.UsersDynamicSqlSupport.Users.gender
import com.llm.gateway.dal.mapper.UsersDynamicSqlSupport.Users.id
import com.llm.gateway.dal.mapper.UsersDynamicSqlSupport.Users.mobile
import com.llm.gateway.dal.mapper.UsersDynamicSqlSupport.Users.password
import com.llm.gateway.dal.mapper.UsersDynamicSqlSupport.Users.passwordChanged
import com.llm.gateway.dal.mapper.UsersDynamicSqlSupport.Users.remark
import com.llm.gateway.dal.mapper.UsersDynamicSqlSupport.Users.status
import com.llm.gateway.dal.mapper.UsersDynamicSqlSupport.Users.updatedTime
import com.llm.gateway.dal.mapper.UsersDynamicSqlSupport.Users.username
import com.llm.gateway.dal.model.UsersRecord
import org.mybatis.dynamic.sql.SqlBuilder.isEqualTo
import org.mybatis.dynamic.sql.insert.render.MultiRowInsertStatementProvider
import org.mybatis.dynamic.sql.util.kotlin.*
import org.mybatis.dynamic.sql.util.kotlin.mybatis3.*

fun UsersMapper.count(completer: CountCompleter) =
    countFrom(this::count, Users, completer)

fun UsersMapper.delete(completer: DeleteCompleter) =
    deleteFrom(this::delete, Users, completer)

fun UsersMapper.deleteByPrimaryKey(id_: Long) =
    delete {
        where(id, isEqualTo(id_))
    }

fun UsersMapper.insertMultipleHelper(multipleInsertStatement: MultiRowInsertStatementProvider<UsersRecord>) =
    insertMultiple(multipleInsertStatement.insertStatement, multipleInsertStatement.records)

fun UsersMapper.insert(record: UsersRecord) =
    insert(this::insert, record, Users) {
        map(deptId).toProperty("deptId")
        map(username).toProperty("username")
        map(email).toProperty("email")
        map(mobile).toProperty("mobile")
        map(gender).toProperty("gender")
        map(avatarUrl).toProperty("avatarUrl")
        map(password).toProperty("password")
        map(passwordChanged).toProperty("passwordChanged")
        map(remark).toProperty("remark")
        map(status).toProperty("status")
        map(delFlag).toProperty("delFlag")
        map(createdTime).toProperty("createdTime")
        map(updatedTime).toProperty("updatedTime")
    }

fun UsersMapper.insertMultiple(records: Collection<UsersRecord>) =
    insertMultiple(this::insertMultipleHelper, records, Users) {
        map(deptId).toProperty("deptId")
        map(username).toProperty("username")
        map(email).toProperty("email")
        map(mobile).toProperty("mobile")
        map(gender).toProperty("gender")
        map(avatarUrl).toProperty("avatarUrl")
        map(password).toProperty("password")
        map(passwordChanged).toProperty("passwordChanged")
        map(remark).toProperty("remark")
        map(status).toProperty("status")
        map(delFlag).toProperty("delFlag")
        map(createdTime).toProperty("createdTime")
        map(updatedTime).toProperty("updatedTime")
    }

fun UsersMapper.insertMultiple(vararg records: UsersRecord) =
    insertMultiple(records.toList())

fun UsersMapper.insertSelective(record: UsersRecord) =
    insert(this::insert, record, Users) {
        map(deptId).toPropertyWhenPresent("deptId", record::deptId)
        map(username).toPropertyWhenPresent("username", record::username)
        map(email).toPropertyWhenPresent("email", record::email)
        map(mobile).toPropertyWhenPresent("mobile", record::mobile)
        map(gender).toPropertyWhenPresent("gender", record::gender)
        map(avatarUrl).toPropertyWhenPresent("avatarUrl", record::avatarUrl)
        map(password).toPropertyWhenPresent("password", record::password)
        map(passwordChanged).toPropertyWhenPresent("passwordChanged", record::passwordChanged)
        map(remark).toPropertyWhenPresent("remark", record::remark)
        map(status).toPropertyWhenPresent("status", record::status)
        map(delFlag).toPropertyWhenPresent("delFlag", record::delFlag)
        map(createdTime).toPropertyWhenPresent("createdTime", record::createdTime)
        map(updatedTime).toPropertyWhenPresent("updatedTime", record::updatedTime)
    }

private val columnList = listOf(id, deptId, username, email, mobile, gender, avatarUrl, password, passwordChanged, remark, status, delFlag, createdTime, updatedTime)

fun UsersMapper.selectOne(completer: SelectCompleter) =
    selectOne(this::selectOne, columnList, Users, completer)

fun UsersMapper.select(completer: SelectCompleter) =
    selectList(this::selectMany, columnList, Users, completer)

fun UsersMapper.selectDistinct(completer: SelectCompleter) =
    selectDistinct(this::selectMany, columnList, Users, completer)

fun UsersMapper.selectByPrimaryKey(id_: Long) =
    selectOne {
        where(id, isEqualTo(id_))
    }

fun UsersMapper.update(completer: UpdateCompleter) =
    update(this::update, Users, completer)

fun KotlinUpdateBuilder.updateAllColumns(record: UsersRecord) =
    apply {
        set(deptId).equalTo(record::deptId)
        set(username).equalTo(record::username)
        set(email).equalTo(record::email)
        set(mobile).equalTo(record::mobile)
        set(gender).equalTo(record::gender)
        set(avatarUrl).equalTo(record::avatarUrl)
        set(password).equalTo(record::password)
        set(passwordChanged).equalTo(record::passwordChanged)
        set(remark).equalTo(record::remark)
        set(status).equalTo(record::status)
        set(delFlag).equalTo(record::delFlag)
        set(createdTime).equalTo(record::createdTime)
        set(updatedTime).equalTo(record::updatedTime)
    }

fun KotlinUpdateBuilder.updateSelectiveColumns(record: UsersRecord) =
    apply {
        set(deptId).equalToWhenPresent(record::deptId)
        set(username).equalToWhenPresent(record::username)
        set(email).equalToWhenPresent(record::email)
        set(mobile).equalToWhenPresent(record::mobile)
        set(gender).equalToWhenPresent(record::gender)
        set(avatarUrl).equalToWhenPresent(record::avatarUrl)
        set(password).equalToWhenPresent(record::password)
        set(passwordChanged).equalToWhenPresent(record::passwordChanged)
        set(remark).equalToWhenPresent(record::remark)
        set(status).equalToWhenPresent(record::status)
        set(delFlag).equalToWhenPresent(record::delFlag)
        set(createdTime).equalToWhenPresent(record::createdTime)
        set(updatedTime).equalToWhenPresent(record::updatedTime)
    }

fun UsersMapper.updateByPrimaryKey(record: UsersRecord) =
    update {
        set(deptId).equalTo(record::deptId)
        set(username).equalTo(record::username)
        set(email).equalTo(record::email)
        set(mobile).equalTo(record::mobile)
        set(gender).equalTo(record::gender)
        set(avatarUrl).equalTo(record::avatarUrl)
        set(password).equalTo(record::password)
        set(passwordChanged).equalTo(record::passwordChanged)
        set(remark).equalTo(record::remark)
        set(status).equalTo(record::status)
        set(delFlag).equalTo(record::delFlag)
        set(createdTime).equalTo(record::createdTime)
        set(updatedTime).equalTo(record::updatedTime)
        where(id, isEqualTo(record::id))
    }

fun UsersMapper.updateByPrimaryKeySelective(record: UsersRecord) =
    update {
        set(deptId).equalToWhenPresent(record::deptId)
        set(username).equalToWhenPresent(record::username)
        set(email).equalToWhenPresent(record::email)
        set(mobile).equalToWhenPresent(record::mobile)
        set(gender).equalToWhenPresent(record::gender)
        set(avatarUrl).equalToWhenPresent(record::avatarUrl)
        set(password).equalToWhenPresent(record::password)
        set(passwordChanged).equalToWhenPresent(record::passwordChanged)
        set(remark).equalToWhenPresent(record::remark)
        set(status).equalToWhenPresent(record::status)
        set(delFlag).equalToWhenPresent(record::delFlag)
        set(createdTime).equalToWhenPresent(record::createdTime)
        set(updatedTime).equalToWhenPresent(record::updatedTime)
        where(id, isEqualTo(record::id))
    }