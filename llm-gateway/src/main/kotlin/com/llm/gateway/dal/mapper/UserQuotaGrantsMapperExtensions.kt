/*
 * Auto-generated file. Created by MyBatis Generator
 * Generation date: 2026-05-29T14:51:25.344652+08:00
 */
package com.llm.gateway.dal.mapper

import com.llm.gateway.dal.mapper.UserQuotaGrantsDynamicSqlSupport.UserQuotaGrants
import com.llm.gateway.dal.mapper.UserQuotaGrantsDynamicSqlSupport.UserQuotaGrants.consumedAmount
import com.llm.gateway.dal.mapper.UserQuotaGrantsDynamicSqlSupport.UserQuotaGrants.createdTime
import com.llm.gateway.dal.mapper.UserQuotaGrantsDynamicSqlSupport.UserQuotaGrants.expiredAmount
import com.llm.gateway.dal.mapper.UserQuotaGrantsDynamicSqlSupport.UserQuotaGrants.expiresAt
import com.llm.gateway.dal.mapper.UserQuotaGrantsDynamicSqlSupport.UserQuotaGrants.grantedAmount
import com.llm.gateway.dal.mapper.UserQuotaGrantsDynamicSqlSupport.UserQuotaGrants.grantedBy
import com.llm.gateway.dal.mapper.UserQuotaGrantsDynamicSqlSupport.UserQuotaGrants.id
import com.llm.gateway.dal.mapper.UserQuotaGrantsDynamicSqlSupport.UserQuotaGrants.remainingAmount
import com.llm.gateway.dal.mapper.UserQuotaGrantsDynamicSqlSupport.UserQuotaGrants.remark
import com.llm.gateway.dal.mapper.UserQuotaGrantsDynamicSqlSupport.UserQuotaGrants.sourceGrantId
import com.llm.gateway.dal.mapper.UserQuotaGrantsDynamicSqlSupport.UserQuotaGrants.sourceType
import com.llm.gateway.dal.mapper.UserQuotaGrantsDynamicSqlSupport.UserQuotaGrants.sourceUserId
import com.llm.gateway.dal.mapper.UserQuotaGrantsDynamicSqlSupport.UserQuotaGrants.status
import com.llm.gateway.dal.mapper.UserQuotaGrantsDynamicSqlSupport.UserQuotaGrants.updatedTime
import com.llm.gateway.dal.mapper.UserQuotaGrantsDynamicSqlSupport.UserQuotaGrants.userId
import com.llm.gateway.dal.model.UserQuotaGrantsRecord
import org.mybatis.dynamic.sql.SqlBuilder.isEqualTo
import org.mybatis.dynamic.sql.insert.render.MultiRowInsertStatementProvider
import org.mybatis.dynamic.sql.util.kotlin.*
import org.mybatis.dynamic.sql.util.kotlin.mybatis3.*

fun UserQuotaGrantsMapper.count(completer: CountCompleter) =
    countFrom(this::count, UserQuotaGrants, completer)

fun UserQuotaGrantsMapper.delete(completer: DeleteCompleter) =
    deleteFrom(this::delete, UserQuotaGrants, completer)

fun UserQuotaGrantsMapper.deleteByPrimaryKey(id_: Long) =
    delete {
        where(id, isEqualTo(id_))
    }

fun UserQuotaGrantsMapper.insertMultipleHelper(multipleInsertStatement: MultiRowInsertStatementProvider<UserQuotaGrantsRecord>) =
    insertMultiple(multipleInsertStatement.insertStatement, multipleInsertStatement.records)

fun UserQuotaGrantsMapper.insert(record: UserQuotaGrantsRecord) =
    insert(this::insert, record, UserQuotaGrants) {
        map(userId).toProperty("userId")
        map(sourceType).toProperty("sourceType")
        map(sourceUserId).toProperty("sourceUserId")
        map(sourceGrantId).toProperty("sourceGrantId")
        map(grantedAmount).toProperty("grantedAmount")
        map(remainingAmount).toProperty("remainingAmount")
        map(consumedAmount).toProperty("consumedAmount")
        map(expiredAmount).toProperty("expiredAmount")
        map(expiresAt).toProperty("expiresAt")
        map(status).toProperty("status")
        map(grantedBy).toProperty("grantedBy")
        map(remark).toProperty("remark")
        map(createdTime).toProperty("createdTime")
        map(updatedTime).toProperty("updatedTime")
    }

fun UserQuotaGrantsMapper.insertMultiple(records: Collection<UserQuotaGrantsRecord>) =
    insertMultiple(this::insertMultipleHelper, records, UserQuotaGrants) {
        map(userId).toProperty("userId")
        map(sourceType).toProperty("sourceType")
        map(sourceUserId).toProperty("sourceUserId")
        map(sourceGrantId).toProperty("sourceGrantId")
        map(grantedAmount).toProperty("grantedAmount")
        map(remainingAmount).toProperty("remainingAmount")
        map(consumedAmount).toProperty("consumedAmount")
        map(expiredAmount).toProperty("expiredAmount")
        map(expiresAt).toProperty("expiresAt")
        map(status).toProperty("status")
        map(grantedBy).toProperty("grantedBy")
        map(remark).toProperty("remark")
        map(createdTime).toProperty("createdTime")
        map(updatedTime).toProperty("updatedTime")
    }

fun UserQuotaGrantsMapper.insertMultiple(vararg records: UserQuotaGrantsRecord) =
    insertMultiple(records.toList())

fun UserQuotaGrantsMapper.insertSelective(record: UserQuotaGrantsRecord) =
    insert(this::insert, record, UserQuotaGrants) {
        map(userId).toPropertyWhenPresent("userId", record::userId)
        map(sourceType).toPropertyWhenPresent("sourceType", record::sourceType)
        map(sourceUserId).toPropertyWhenPresent("sourceUserId", record::sourceUserId)
        map(sourceGrantId).toPropertyWhenPresent("sourceGrantId", record::sourceGrantId)
        map(grantedAmount).toPropertyWhenPresent("grantedAmount", record::grantedAmount)
        map(remainingAmount).toPropertyWhenPresent("remainingAmount", record::remainingAmount)
        map(consumedAmount).toPropertyWhenPresent("consumedAmount", record::consumedAmount)
        map(expiredAmount).toPropertyWhenPresent("expiredAmount", record::expiredAmount)
        map(expiresAt).toPropertyWhenPresent("expiresAt", record::expiresAt)
        map(status).toPropertyWhenPresent("status", record::status)
        map(grantedBy).toPropertyWhenPresent("grantedBy", record::grantedBy)
        map(remark).toPropertyWhenPresent("remark", record::remark)
        map(createdTime).toPropertyWhenPresent("createdTime", record::createdTime)
        map(updatedTime).toPropertyWhenPresent("updatedTime", record::updatedTime)
    }

private val columnList = listOf(id, userId, sourceType, sourceUserId, sourceGrantId, grantedAmount, remainingAmount, consumedAmount, expiredAmount, expiresAt, status, grantedBy, remark, createdTime, updatedTime)

fun UserQuotaGrantsMapper.selectOne(completer: SelectCompleter) =
    selectOne(this::selectOne, columnList, UserQuotaGrants, completer)

fun UserQuotaGrantsMapper.select(completer: SelectCompleter) =
    selectList(this::selectMany, columnList, UserQuotaGrants, completer)

fun UserQuotaGrantsMapper.selectDistinct(completer: SelectCompleter) =
    selectDistinct(this::selectMany, columnList, UserQuotaGrants, completer)

fun UserQuotaGrantsMapper.selectByPrimaryKey(id_: Long) =
    selectOne {
        where(id, isEqualTo(id_))
    }

fun UserQuotaGrantsMapper.update(completer: UpdateCompleter) =
    update(this::update, UserQuotaGrants, completer)

fun KotlinUpdateBuilder.updateAllColumns(record: UserQuotaGrantsRecord) =
    apply {
        set(userId).equalTo(record::userId)
        set(sourceType).equalTo(record::sourceType)
        set(sourceUserId).equalTo(record::sourceUserId)
        set(sourceGrantId).equalTo(record::sourceGrantId)
        set(grantedAmount).equalTo(record::grantedAmount)
        set(remainingAmount).equalTo(record::remainingAmount)
        set(consumedAmount).equalTo(record::consumedAmount)
        set(expiredAmount).equalTo(record::expiredAmount)
        set(expiresAt).equalTo(record::expiresAt)
        set(status).equalTo(record::status)
        set(grantedBy).equalTo(record::grantedBy)
        set(remark).equalTo(record::remark)
        set(createdTime).equalTo(record::createdTime)
        set(updatedTime).equalTo(record::updatedTime)
    }

fun KotlinUpdateBuilder.updateSelectiveColumns(record: UserQuotaGrantsRecord) =
    apply {
        set(userId).equalToWhenPresent(record::userId)
        set(sourceType).equalToWhenPresent(record::sourceType)
        set(sourceUserId).equalToWhenPresent(record::sourceUserId)
        set(sourceGrantId).equalToWhenPresent(record::sourceGrantId)
        set(grantedAmount).equalToWhenPresent(record::grantedAmount)
        set(remainingAmount).equalToWhenPresent(record::remainingAmount)
        set(consumedAmount).equalToWhenPresent(record::consumedAmount)
        set(expiredAmount).equalToWhenPresent(record::expiredAmount)
        set(expiresAt).equalToWhenPresent(record::expiresAt)
        set(status).equalToWhenPresent(record::status)
        set(grantedBy).equalToWhenPresent(record::grantedBy)
        set(remark).equalToWhenPresent(record::remark)
        set(createdTime).equalToWhenPresent(record::createdTime)
        set(updatedTime).equalToWhenPresent(record::updatedTime)
    }

fun UserQuotaGrantsMapper.updateByPrimaryKey(record: UserQuotaGrantsRecord) =
    update {
        set(userId).equalTo(record::userId)
        set(sourceType).equalTo(record::sourceType)
        set(sourceUserId).equalTo(record::sourceUserId)
        set(sourceGrantId).equalTo(record::sourceGrantId)
        set(grantedAmount).equalTo(record::grantedAmount)
        set(remainingAmount).equalTo(record::remainingAmount)
        set(consumedAmount).equalTo(record::consumedAmount)
        set(expiredAmount).equalTo(record::expiredAmount)
        set(expiresAt).equalTo(record::expiresAt)
        set(status).equalTo(record::status)
        set(grantedBy).equalTo(record::grantedBy)
        set(remark).equalTo(record::remark)
        set(createdTime).equalTo(record::createdTime)
        set(updatedTime).equalTo(record::updatedTime)
        where(id, isEqualTo(record::id))
    }

fun UserQuotaGrantsMapper.updateByPrimaryKeySelective(record: UserQuotaGrantsRecord) =
    update {
        set(userId).equalToWhenPresent(record::userId)
        set(sourceType).equalToWhenPresent(record::sourceType)
        set(sourceUserId).equalToWhenPresent(record::sourceUserId)
        set(sourceGrantId).equalToWhenPresent(record::sourceGrantId)
        set(grantedAmount).equalToWhenPresent(record::grantedAmount)
        set(remainingAmount).equalToWhenPresent(record::remainingAmount)
        set(consumedAmount).equalToWhenPresent(record::consumedAmount)
        set(expiredAmount).equalToWhenPresent(record::expiredAmount)
        set(expiresAt).equalToWhenPresent(record::expiresAt)
        set(status).equalToWhenPresent(record::status)
        set(grantedBy).equalToWhenPresent(record::grantedBy)
        set(remark).equalToWhenPresent(record::remark)
        set(createdTime).equalToWhenPresent(record::createdTime)
        set(updatedTime).equalToWhenPresent(record::updatedTime)
        where(id, isEqualTo(record::id))
    }