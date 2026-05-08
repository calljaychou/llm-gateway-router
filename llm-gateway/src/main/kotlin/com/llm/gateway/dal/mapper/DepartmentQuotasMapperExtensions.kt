/*
 * Auto-generated file. Created by MyBatis Generator
 * Generation date: 2026-05-08T17:25:34.952565+08:00
 */
package com.llm.gateway.dal.mapper

import com.llm.gateway.dal.mapper.DepartmentQuotasDynamicSqlSupport.DepartmentQuotas
import com.llm.gateway.dal.mapper.DepartmentQuotasDynamicSqlSupport.DepartmentQuotas.createdAt
import com.llm.gateway.dal.mapper.DepartmentQuotasDynamicSqlSupport.DepartmentQuotas.deptId
import com.llm.gateway.dal.mapper.DepartmentQuotasDynamicSqlSupport.DepartmentQuotas.id
import com.llm.gateway.dal.mapper.DepartmentQuotasDynamicSqlSupport.DepartmentQuotas.period
import com.llm.gateway.dal.mapper.DepartmentQuotasDynamicSqlSupport.DepartmentQuotas.quotaTokens
import com.llm.gateway.dal.mapper.DepartmentQuotasDynamicSqlSupport.DepartmentQuotas.remark
import com.llm.gateway.dal.mapper.DepartmentQuotasDynamicSqlSupport.DepartmentQuotas.status
import com.llm.gateway.dal.mapper.DepartmentQuotasDynamicSqlSupport.DepartmentQuotas.updatedAt
import com.llm.gateway.dal.model.DepartmentQuotasRecord
import org.mybatis.dynamic.sql.SqlBuilder.isEqualTo
import org.mybatis.dynamic.sql.insert.render.MultiRowInsertStatementProvider
import org.mybatis.dynamic.sql.util.kotlin.*
import org.mybatis.dynamic.sql.util.kotlin.mybatis3.*

fun DepartmentQuotasMapper.count(completer: CountCompleter) =
    countFrom(this::count, DepartmentQuotas, completer)

fun DepartmentQuotasMapper.delete(completer: DeleteCompleter) =
    deleteFrom(this::delete, DepartmentQuotas, completer)

fun DepartmentQuotasMapper.deleteByPrimaryKey(id_: Long) =
    delete {
        where(id, isEqualTo(id_))
    }

fun DepartmentQuotasMapper.insertMultipleHelper(multipleInsertStatement: MultiRowInsertStatementProvider<DepartmentQuotasRecord>) =
    insertMultiple(multipleInsertStatement.insertStatement, multipleInsertStatement.records)

fun DepartmentQuotasMapper.insert(record: DepartmentQuotasRecord) =
    insert(this::insert, record, DepartmentQuotas) {
        map(deptId).toProperty("deptId")
        map(quotaTokens).toProperty("quotaTokens")
        map(period).toProperty("period")
        map(status).toProperty("status")
        map(remark).toProperty("remark")
        map(createdAt).toProperty("createdAt")
        map(updatedAt).toProperty("updatedAt")
    }

fun DepartmentQuotasMapper.insertMultiple(records: Collection<DepartmentQuotasRecord>) =
    insertMultiple(this::insertMultipleHelper, records, DepartmentQuotas) {
        map(deptId).toProperty("deptId")
        map(quotaTokens).toProperty("quotaTokens")
        map(period).toProperty("period")
        map(status).toProperty("status")
        map(remark).toProperty("remark")
        map(createdAt).toProperty("createdAt")
        map(updatedAt).toProperty("updatedAt")
    }

fun DepartmentQuotasMapper.insertMultiple(vararg records: DepartmentQuotasRecord) =
    insertMultiple(records.toList())

fun DepartmentQuotasMapper.insertSelective(record: DepartmentQuotasRecord) =
    insert(this::insert, record, DepartmentQuotas) {
        map(deptId).toPropertyWhenPresent("deptId", record::deptId)
        map(quotaTokens).toPropertyWhenPresent("quotaTokens", record::quotaTokens)
        map(period).toPropertyWhenPresent("period", record::period)
        map(status).toPropertyWhenPresent("status", record::status)
        map(remark).toPropertyWhenPresent("remark", record::remark)
        map(createdAt).toPropertyWhenPresent("createdAt", record::createdAt)
        map(updatedAt).toPropertyWhenPresent("updatedAt", record::updatedAt)
    }

private val columnList = listOf(id, deptId, quotaTokens, period, status, remark, createdAt, updatedAt)

fun DepartmentQuotasMapper.selectOne(completer: SelectCompleter) =
    selectOne(this::selectOne, columnList, DepartmentQuotas, completer)

fun DepartmentQuotasMapper.select(completer: SelectCompleter) =
    selectList(this::selectMany, columnList, DepartmentQuotas, completer)

fun DepartmentQuotasMapper.selectDistinct(completer: SelectCompleter) =
    selectDistinct(this::selectMany, columnList, DepartmentQuotas, completer)

fun DepartmentQuotasMapper.selectByPrimaryKey(id_: Long) =
    selectOne {
        where(id, isEqualTo(id_))
    }

fun DepartmentQuotasMapper.update(completer: UpdateCompleter) =
    update(this::update, DepartmentQuotas, completer)

fun KotlinUpdateBuilder.updateAllColumns(record: DepartmentQuotasRecord) =
    apply {
        set(deptId).equalTo(record::deptId)
        set(quotaTokens).equalTo(record::quotaTokens)
        set(period).equalTo(record::period)
        set(status).equalTo(record::status)
        set(remark).equalTo(record::remark)
        set(createdAt).equalTo(record::createdAt)
        set(updatedAt).equalTo(record::updatedAt)
    }

fun KotlinUpdateBuilder.updateSelectiveColumns(record: DepartmentQuotasRecord) =
    apply {
        set(deptId).equalToWhenPresent(record::deptId)
        set(quotaTokens).equalToWhenPresent(record::quotaTokens)
        set(period).equalToWhenPresent(record::period)
        set(status).equalToWhenPresent(record::status)
        set(remark).equalToWhenPresent(record::remark)
        set(createdAt).equalToWhenPresent(record::createdAt)
        set(updatedAt).equalToWhenPresent(record::updatedAt)
    }

fun DepartmentQuotasMapper.updateByPrimaryKey(record: DepartmentQuotasRecord) =
    update {
        set(deptId).equalTo(record::deptId)
        set(quotaTokens).equalTo(record::quotaTokens)
        set(period).equalTo(record::period)
        set(status).equalTo(record::status)
        set(remark).equalTo(record::remark)
        set(createdAt).equalTo(record::createdAt)
        set(updatedAt).equalTo(record::updatedAt)
        where(id, isEqualTo(record::id))
    }

fun DepartmentQuotasMapper.updateByPrimaryKeySelective(record: DepartmentQuotasRecord) =
    update {
        set(deptId).equalToWhenPresent(record::deptId)
        set(quotaTokens).equalToWhenPresent(record::quotaTokens)
        set(period).equalToWhenPresent(record::period)
        set(status).equalToWhenPresent(record::status)
        set(remark).equalToWhenPresent(record::remark)
        set(createdAt).equalToWhenPresent(record::createdAt)
        set(updatedAt).equalToWhenPresent(record::updatedAt)
        where(id, isEqualTo(record::id))
    }