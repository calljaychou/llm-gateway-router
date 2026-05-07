/*
 * Auto-generated file. Created by MyBatis Generator
 * Generation date: 2026-04-24T09:36:04.321+08:00
 */
package com.llm.gateway.dal.mapper

import com.llm.gateway.dal.mapper.RolesDynamicSqlSupport.Roles
import com.llm.gateway.dal.mapper.RolesDynamicSqlSupport.Roles.createdBy
import com.llm.gateway.dal.mapper.RolesDynamicSqlSupport.Roles.createdTime
import com.llm.gateway.dal.mapper.RolesDynamicSqlSupport.Roles.id
import com.llm.gateway.dal.mapper.RolesDynamicSqlSupport.Roles.roleKey
import com.llm.gateway.dal.mapper.RolesDynamicSqlSupport.Roles.roleName
import com.llm.gateway.dal.mapper.RolesDynamicSqlSupport.Roles.roleSort
import com.llm.gateway.dal.mapper.RolesDynamicSqlSupport.Roles.updatedBy
import com.llm.gateway.dal.mapper.RolesDynamicSqlSupport.Roles.updatedTime
import com.llm.gateway.dal.model.RolesRecord
import org.mybatis.dynamic.sql.SqlBuilder.isEqualTo
import org.mybatis.dynamic.sql.insert.render.MultiRowInsertStatementProvider
import org.mybatis.dynamic.sql.util.kotlin.*
import org.mybatis.dynamic.sql.util.kotlin.mybatis3.*

fun RolesMapper.count(completer: CountCompleter) =
    countFrom(this::count, Roles, completer)

fun RolesMapper.delete(completer: DeleteCompleter) =
    deleteFrom(this::delete, Roles, completer)

fun RolesMapper.deleteByPrimaryKey(id_: Long) =
    delete {
        where(id, isEqualTo(id_))
    }

fun RolesMapper.insertMultipleHelper(multipleInsertStatement: MultiRowInsertStatementProvider<RolesRecord>) =
    insertMultiple(multipleInsertStatement.insertStatement, multipleInsertStatement.records)

fun RolesMapper.insert(record: RolesRecord) =
    insert(this::insert, record, Roles) {
        map(roleName).toProperty("roleName")
        map(roleKey).toProperty("roleKey")
        map(roleSort).toProperty("roleSort")
        map(createdBy).toProperty("createdBy")
        map(createdTime).toProperty("createdTime")
        map(updatedBy).toProperty("updatedBy")
        map(updatedTime).toProperty("updatedTime")
    }

fun RolesMapper.insertMultiple(records: Collection<RolesRecord>) =
    insertMultiple(this::insertMultipleHelper, records, Roles) {
        map(roleName).toProperty("roleName")
        map(roleKey).toProperty("roleKey")
        map(roleSort).toProperty("roleSort")
        map(createdBy).toProperty("createdBy")
        map(createdTime).toProperty("createdTime")
        map(updatedBy).toProperty("updatedBy")
        map(updatedTime).toProperty("updatedTime")
    }

fun RolesMapper.insertMultiple(vararg records: RolesRecord) =
    insertMultiple(records.toList())

fun RolesMapper.insertSelective(record: RolesRecord) =
    insert(this::insert, record, Roles) {
        map(roleName).toPropertyWhenPresent("roleName", record::roleName)
        map(roleKey).toPropertyWhenPresent("roleKey", record::roleKey)
        map(roleSort).toPropertyWhenPresent("roleSort", record::roleSort)
        map(createdBy).toPropertyWhenPresent("createdBy", record::createdBy)
        map(createdTime).toPropertyWhenPresent("createdTime", record::createdTime)
        map(updatedBy).toPropertyWhenPresent("updatedBy", record::updatedBy)
        map(updatedTime).toPropertyWhenPresent("updatedTime", record::updatedTime)
    }

private val columnList = listOf(id, roleName, roleKey, roleSort, createdBy, createdTime, updatedBy, updatedTime)

fun RolesMapper.selectOne(completer: SelectCompleter) =
    selectOne(this::selectOne, columnList, Roles, completer)

fun RolesMapper.select(completer: SelectCompleter) =
    selectList(this::selectMany, columnList, Roles, completer)

fun RolesMapper.selectDistinct(completer: SelectCompleter) =
    selectDistinct(this::selectMany, columnList, Roles, completer)

fun RolesMapper.selectByPrimaryKey(id_: Long) =
    selectOne {
        where(id, isEqualTo(id_))
    }

fun RolesMapper.update(completer: UpdateCompleter) =
    update(this::update, Roles, completer)

fun KotlinUpdateBuilder.updateAllColumns(record: RolesRecord) =
    apply {
        set(roleName).equalTo(record::roleName)
        set(roleKey).equalTo(record::roleKey)
        set(roleSort).equalTo(record::roleSort)
        set(createdBy).equalTo(record::createdBy)
        set(createdTime).equalTo(record::createdTime)
        set(updatedBy).equalTo(record::updatedBy)
        set(updatedTime).equalTo(record::updatedTime)
    }

fun KotlinUpdateBuilder.updateSelectiveColumns(record: RolesRecord) =
    apply {
        set(roleName).equalToWhenPresent(record::roleName)
        set(roleKey).equalToWhenPresent(record::roleKey)
        set(roleSort).equalToWhenPresent(record::roleSort)
        set(createdBy).equalToWhenPresent(record::createdBy)
        set(createdTime).equalToWhenPresent(record::createdTime)
        set(updatedBy).equalToWhenPresent(record::updatedBy)
        set(updatedTime).equalToWhenPresent(record::updatedTime)
    }

fun RolesMapper.updateByPrimaryKey(record: RolesRecord) =
    update {
        set(roleName).equalTo(record::roleName)
        set(roleKey).equalTo(record::roleKey)
        set(roleSort).equalTo(record::roleSort)
        set(createdBy).equalTo(record::createdBy)
        set(createdTime).equalTo(record::createdTime)
        set(updatedBy).equalTo(record::updatedBy)
        set(updatedTime).equalTo(record::updatedTime)
        where(id, isEqualTo(record::id))
    }

fun RolesMapper.updateByPrimaryKeySelective(record: RolesRecord) =
    update {
        set(roleName).equalToWhenPresent(record::roleName)
        set(roleKey).equalToWhenPresent(record::roleKey)
        set(roleSort).equalToWhenPresent(record::roleSort)
        set(createdBy).equalToWhenPresent(record::createdBy)
        set(createdTime).equalToWhenPresent(record::createdTime)
        set(updatedBy).equalToWhenPresent(record::updatedBy)
        set(updatedTime).equalToWhenPresent(record::updatedTime)
        where(id, isEqualTo(record::id))
    }