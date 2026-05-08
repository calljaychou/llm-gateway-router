/*
 * Auto-generated file. Created by MyBatis Generator
 * Generation date: 2026-05-08T17:25:34.951476+08:00
 */
package com.llm.gateway.dal.mapper

import com.llm.gateway.dal.mapper.DepartmentModelPermissionsDynamicSqlSupport.DepartmentModelPermissions
import com.llm.gateway.dal.mapper.DepartmentModelPermissionsDynamicSqlSupport.DepartmentModelPermissions.createdBy
import com.llm.gateway.dal.mapper.DepartmentModelPermissionsDynamicSqlSupport.DepartmentModelPermissions.createdTime
import com.llm.gateway.dal.mapper.DepartmentModelPermissionsDynamicSqlSupport.DepartmentModelPermissions.deptId
import com.llm.gateway.dal.mapper.DepartmentModelPermissionsDynamicSqlSupport.DepartmentModelPermissions.id
import com.llm.gateway.dal.mapper.DepartmentModelPermissionsDynamicSqlSupport.DepartmentModelPermissions.modelId
import com.llm.gateway.dal.mapper.DepartmentModelPermissionsDynamicSqlSupport.DepartmentModelPermissions.scope
import com.llm.gateway.dal.mapper.DepartmentModelPermissionsDynamicSqlSupport.DepartmentModelPermissions.status
import com.llm.gateway.dal.mapper.DepartmentModelPermissionsDynamicSqlSupport.DepartmentModelPermissions.updatedBy
import com.llm.gateway.dal.mapper.DepartmentModelPermissionsDynamicSqlSupport.DepartmentModelPermissions.updatedTime
import com.llm.gateway.dal.model.DepartmentModelPermissionsRecord
import org.mybatis.dynamic.sql.SqlBuilder.isEqualTo
import org.mybatis.dynamic.sql.insert.render.MultiRowInsertStatementProvider
import org.mybatis.dynamic.sql.util.kotlin.*
import org.mybatis.dynamic.sql.util.kotlin.mybatis3.*

fun DepartmentModelPermissionsMapper.count(completer: CountCompleter) =
    countFrom(this::count, DepartmentModelPermissions, completer)

fun DepartmentModelPermissionsMapper.delete(completer: DeleteCompleter) =
    deleteFrom(this::delete, DepartmentModelPermissions, completer)

fun DepartmentModelPermissionsMapper.deleteByPrimaryKey(id_: Long) =
    delete {
        where(id, isEqualTo(id_))
    }

fun DepartmentModelPermissionsMapper.insertMultipleHelper(multipleInsertStatement: MultiRowInsertStatementProvider<DepartmentModelPermissionsRecord>) =
    insertMultiple(multipleInsertStatement.insertStatement, multipleInsertStatement.records)

fun DepartmentModelPermissionsMapper.insert(record: DepartmentModelPermissionsRecord) =
    insert(this::insert, record, DepartmentModelPermissions) {
        map(deptId).toProperty("deptId")
        map(modelId).toProperty("modelId")
        map(scope).toProperty("scope")
        map(status).toProperty("status")
        map(createdBy).toProperty("createdBy")
        map(createdTime).toProperty("createdTime")
        map(updatedBy).toProperty("updatedBy")
        map(updatedTime).toProperty("updatedTime")
    }

fun DepartmentModelPermissionsMapper.insertMultiple(records: Collection<DepartmentModelPermissionsRecord>) =
    insertMultiple(this::insertMultipleHelper, records, DepartmentModelPermissions) {
        map(deptId).toProperty("deptId")
        map(modelId).toProperty("modelId")
        map(scope).toProperty("scope")
        map(status).toProperty("status")
        map(createdBy).toProperty("createdBy")
        map(createdTime).toProperty("createdTime")
        map(updatedBy).toProperty("updatedBy")
        map(updatedTime).toProperty("updatedTime")
    }

fun DepartmentModelPermissionsMapper.insertMultiple(vararg records: DepartmentModelPermissionsRecord) =
    insertMultiple(records.toList())

fun DepartmentModelPermissionsMapper.insertSelective(record: DepartmentModelPermissionsRecord) =
    insert(this::insert, record, DepartmentModelPermissions) {
        map(deptId).toPropertyWhenPresent("deptId", record::deptId)
        map(modelId).toPropertyWhenPresent("modelId", record::modelId)
        map(scope).toPropertyWhenPresent("scope", record::scope)
        map(status).toPropertyWhenPresent("status", record::status)
        map(createdBy).toPropertyWhenPresent("createdBy", record::createdBy)
        map(createdTime).toPropertyWhenPresent("createdTime", record::createdTime)
        map(updatedBy).toPropertyWhenPresent("updatedBy", record::updatedBy)
        map(updatedTime).toPropertyWhenPresent("updatedTime", record::updatedTime)
    }

private val columnList = listOf(id, deptId, modelId, scope, status, createdBy, createdTime, updatedBy, updatedTime)

fun DepartmentModelPermissionsMapper.selectOne(completer: SelectCompleter) =
    selectOne(this::selectOne, columnList, DepartmentModelPermissions, completer)

fun DepartmentModelPermissionsMapper.select(completer: SelectCompleter) =
    selectList(this::selectMany, columnList, DepartmentModelPermissions, completer)

fun DepartmentModelPermissionsMapper.selectDistinct(completer: SelectCompleter) =
    selectDistinct(this::selectMany, columnList, DepartmentModelPermissions, completer)

fun DepartmentModelPermissionsMapper.selectByPrimaryKey(id_: Long) =
    selectOne {
        where(id, isEqualTo(id_))
    }

fun DepartmentModelPermissionsMapper.update(completer: UpdateCompleter) =
    update(this::update, DepartmentModelPermissions, completer)

fun KotlinUpdateBuilder.updateAllColumns(record: DepartmentModelPermissionsRecord) =
    apply {
        set(deptId).equalTo(record::deptId)
        set(modelId).equalTo(record::modelId)
        set(scope).equalTo(record::scope)
        set(status).equalTo(record::status)
        set(createdBy).equalTo(record::createdBy)
        set(createdTime).equalTo(record::createdTime)
        set(updatedBy).equalTo(record::updatedBy)
        set(updatedTime).equalTo(record::updatedTime)
    }

fun KotlinUpdateBuilder.updateSelectiveColumns(record: DepartmentModelPermissionsRecord) =
    apply {
        set(deptId).equalToWhenPresent(record::deptId)
        set(modelId).equalToWhenPresent(record::modelId)
        set(scope).equalToWhenPresent(record::scope)
        set(status).equalToWhenPresent(record::status)
        set(createdBy).equalToWhenPresent(record::createdBy)
        set(createdTime).equalToWhenPresent(record::createdTime)
        set(updatedBy).equalToWhenPresent(record::updatedBy)
        set(updatedTime).equalToWhenPresent(record::updatedTime)
    }

fun DepartmentModelPermissionsMapper.updateByPrimaryKey(record: DepartmentModelPermissionsRecord) =
    update {
        set(deptId).equalTo(record::deptId)
        set(modelId).equalTo(record::modelId)
        set(scope).equalTo(record::scope)
        set(status).equalTo(record::status)
        set(createdBy).equalTo(record::createdBy)
        set(createdTime).equalTo(record::createdTime)
        set(updatedBy).equalTo(record::updatedBy)
        set(updatedTime).equalTo(record::updatedTime)
        where(id, isEqualTo(record::id))
    }

fun DepartmentModelPermissionsMapper.updateByPrimaryKeySelective(record: DepartmentModelPermissionsRecord) =
    update {
        set(deptId).equalToWhenPresent(record::deptId)
        set(modelId).equalToWhenPresent(record::modelId)
        set(scope).equalToWhenPresent(record::scope)
        set(status).equalToWhenPresent(record::status)
        set(createdBy).equalToWhenPresent(record::createdBy)
        set(createdTime).equalToWhenPresent(record::createdTime)
        set(updatedBy).equalToWhenPresent(record::updatedBy)
        set(updatedTime).equalToWhenPresent(record::updatedTime)
        where(id, isEqualTo(record::id))
    }