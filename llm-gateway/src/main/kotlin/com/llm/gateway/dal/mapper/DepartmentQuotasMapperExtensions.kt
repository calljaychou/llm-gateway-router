/*
 * Auto-generated file. Created by MyBatis Generator
 * Generation date: 2026-05-07T18:46:02.17348+08:00
 */
package com.llm.gateway.dal.mapper

import com.llm.gateway.dal.mapper.DepartmentQuotasDynamicSqlSupport.DepartmentQuotas
import com.llm.gateway.dal.mapper.DepartmentQuotasDynamicSqlSupport.DepartmentQuotas.createdAt
import com.llm.gateway.dal.mapper.DepartmentQuotasDynamicSqlSupport.DepartmentQuotas.deptId
import com.llm.gateway.dal.mapper.DepartmentQuotasDynamicSqlSupport.DepartmentQuotas.id
import com.llm.gateway.dal.mapper.DepartmentQuotasDynamicSqlSupport.DepartmentQuotas.lastResetAt
import com.llm.gateway.dal.mapper.DepartmentQuotasDynamicSqlSupport.DepartmentQuotas.period
import com.llm.gateway.dal.mapper.DepartmentQuotasDynamicSqlSupport.DepartmentQuotas.totalTokens
import com.llm.gateway.dal.mapper.DepartmentQuotasDynamicSqlSupport.DepartmentQuotas.updatedAt
import com.llm.gateway.dal.mapper.DepartmentQuotasDynamicSqlSupport.DepartmentQuotas.usedTokens
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
        map(totalTokens).toProperty("totalTokens")
        map(usedTokens).toProperty("usedTokens")
        map(period).toProperty("period")
        map(lastResetAt).toProperty("lastResetAt")
        map(createdAt).toProperty("createdAt")
        map(updatedAt).toProperty("updatedAt")
    }

fun DepartmentQuotasMapper.insertMultiple(records: Collection<DepartmentQuotasRecord>) =
    insertMultiple(this::insertMultipleHelper, records, DepartmentQuotas) {
        map(deptId).toProperty("deptId")
        map(totalTokens).toProperty("totalTokens")
        map(usedTokens).toProperty("usedTokens")
        map(period).toProperty("period")
        map(lastResetAt).toProperty("lastResetAt")
        map(createdAt).toProperty("createdAt")
        map(updatedAt).toProperty("updatedAt")
    }

fun DepartmentQuotasMapper.insertMultiple(vararg records: DepartmentQuotasRecord) =
    insertMultiple(records.toList())

fun DepartmentQuotasMapper.insertSelective(record: DepartmentQuotasRecord) =
    insert(this::insert, record, DepartmentQuotas) {
        map(deptId).toPropertyWhenPresent("deptId", record::deptId)
        map(totalTokens).toPropertyWhenPresent("totalTokens", record::totalTokens)
        map(usedTokens).toPropertyWhenPresent("usedTokens", record::usedTokens)
        map(period).toPropertyWhenPresent("period", record::period)
        map(lastResetAt).toPropertyWhenPresent("lastResetAt", record::lastResetAt)
        map(createdAt).toPropertyWhenPresent("createdAt", record::createdAt)
        map(updatedAt).toPropertyWhenPresent("updatedAt", record::updatedAt)
    }

private val columnList = listOf(id, deptId, totalTokens, usedTokens, period, lastResetAt, createdAt, updatedAt)

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
        set(totalTokens).equalTo(record::totalTokens)
        set(usedTokens).equalTo(record::usedTokens)
        set(period).equalTo(record::period)
        set(lastResetAt).equalTo(record::lastResetAt)
        set(createdAt).equalTo(record::createdAt)
        set(updatedAt).equalTo(record::updatedAt)
    }

fun KotlinUpdateBuilder.updateSelectiveColumns(record: DepartmentQuotasRecord) =
    apply {
        set(deptId).equalToWhenPresent(record::deptId)
        set(totalTokens).equalToWhenPresent(record::totalTokens)
        set(usedTokens).equalToWhenPresent(record::usedTokens)
        set(period).equalToWhenPresent(record::period)
        set(lastResetAt).equalToWhenPresent(record::lastResetAt)
        set(createdAt).equalToWhenPresent(record::createdAt)
        set(updatedAt).equalToWhenPresent(record::updatedAt)
    }

fun DepartmentQuotasMapper.updateByPrimaryKey(record: DepartmentQuotasRecord) =
    update {
        set(deptId).equalTo(record::deptId)
        set(totalTokens).equalTo(record::totalTokens)
        set(usedTokens).equalTo(record::usedTokens)
        set(period).equalTo(record::period)
        set(lastResetAt).equalTo(record::lastResetAt)
        set(createdAt).equalTo(record::createdAt)
        set(updatedAt).equalTo(record::updatedAt)
        where(id, isEqualTo(record::id))
    }

fun DepartmentQuotasMapper.updateByPrimaryKeySelective(record: DepartmentQuotasRecord) =
    update {
        set(deptId).equalToWhenPresent(record::deptId)
        set(totalTokens).equalToWhenPresent(record::totalTokens)
        set(usedTokens).equalToWhenPresent(record::usedTokens)
        set(period).equalToWhenPresent(record::period)
        set(lastResetAt).equalToWhenPresent(record::lastResetAt)
        set(createdAt).equalToWhenPresent(record::createdAt)
        set(updatedAt).equalToWhenPresent(record::updatedAt)
        where(id, isEqualTo(record::id))
    }