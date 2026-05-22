/*
 * Auto-generated file. Created by MyBatis Generator
 * Generation date: 2026-05-21T18:47:35.939833+08:00
 */
package com.llm.gateway.dal.mapper

import com.llm.gateway.dal.mapper.UserQuotaTransactionsDynamicSqlSupport.UserQuotaTransactions
import com.llm.gateway.dal.mapper.UserQuotaTransactionsDynamicSqlSupport.UserQuotaTransactions.availableAfter
import com.llm.gateway.dal.mapper.UserQuotaTransactionsDynamicSqlSupport.UserQuotaTransactions.availableBefore
import com.llm.gateway.dal.mapper.UserQuotaTransactionsDynamicSqlSupport.UserQuotaTransactions.bizNo
import com.llm.gateway.dal.mapper.UserQuotaTransactionsDynamicSqlSupport.UserQuotaTransactions.changeType
import com.llm.gateway.dal.mapper.UserQuotaTransactionsDynamicSqlSupport.UserQuotaTransactions.counterpartyUserId
import com.llm.gateway.dal.mapper.UserQuotaTransactionsDynamicSqlSupport.UserQuotaTransactions.createdTime
import com.llm.gateway.dal.mapper.UserQuotaTransactionsDynamicSqlSupport.UserQuotaTransactions.deltaTokens
import com.llm.gateway.dal.mapper.UserQuotaTransactionsDynamicSqlSupport.UserQuotaTransactions.grantId
import com.llm.gateway.dal.mapper.UserQuotaTransactionsDynamicSqlSupport.UserQuotaTransactions.id
import com.llm.gateway.dal.mapper.UserQuotaTransactionsDynamicSqlSupport.UserQuotaTransactions.operatorUserId
import com.llm.gateway.dal.mapper.UserQuotaTransactionsDynamicSqlSupport.UserQuotaTransactions.quotaAfter
import com.llm.gateway.dal.mapper.UserQuotaTransactionsDynamicSqlSupport.UserQuotaTransactions.quotaBefore
import com.llm.gateway.dal.mapper.UserQuotaTransactionsDynamicSqlSupport.UserQuotaTransactions.remark
import com.llm.gateway.dal.mapper.UserQuotaTransactionsDynamicSqlSupport.UserQuotaTransactions.requestId
import com.llm.gateway.dal.mapper.UserQuotaTransactionsDynamicSqlSupport.UserQuotaTransactions.userId
import com.llm.gateway.dal.model.UserQuotaTransactionsRecord
import org.mybatis.dynamic.sql.SqlBuilder.isEqualTo
import org.mybatis.dynamic.sql.insert.render.MultiRowInsertStatementProvider
import org.mybatis.dynamic.sql.util.kotlin.*
import org.mybatis.dynamic.sql.util.kotlin.mybatis3.*

fun UserQuotaTransactionsMapper.count(completer: CountCompleter) =
    countFrom(this::count, UserQuotaTransactions, completer)

fun UserQuotaTransactionsMapper.delete(completer: DeleteCompleter) =
    deleteFrom(this::delete, UserQuotaTransactions, completer)

fun UserQuotaTransactionsMapper.deleteByPrimaryKey(id_: Long) =
    delete {
        where(id, isEqualTo(id_))
    }

fun UserQuotaTransactionsMapper.insertMultipleHelper(multipleInsertStatement: MultiRowInsertStatementProvider<UserQuotaTransactionsRecord>) =
    insertMultiple(multipleInsertStatement.insertStatement, multipleInsertStatement.records)

fun UserQuotaTransactionsMapper.insert(record: UserQuotaTransactionsRecord) =
    insert(this::insert, record, UserQuotaTransactions) {
        map(bizNo).toProperty("bizNo")
        map(userId).toProperty("userId")
        map(grantId).toProperty("grantId")
        map(changeType).toProperty("changeType")
        map(deltaTokens).toProperty("deltaTokens")
        map(quotaBefore).toProperty("quotaBefore")
        map(quotaAfter).toProperty("quotaAfter")
        map(availableBefore).toProperty("availableBefore")
        map(availableAfter).toProperty("availableAfter")
        map(counterpartyUserId).toProperty("counterpartyUserId")
        map(requestId).toProperty("requestId")
        map(operatorUserId).toProperty("operatorUserId")
        map(remark).toProperty("remark")
        map(createdTime).toProperty("createdTime")
    }

fun UserQuotaTransactionsMapper.insertMultiple(records: Collection<UserQuotaTransactionsRecord>) =
    insertMultiple(this::insertMultipleHelper, records, UserQuotaTransactions) {
        map(bizNo).toProperty("bizNo")
        map(userId).toProperty("userId")
        map(grantId).toProperty("grantId")
        map(changeType).toProperty("changeType")
        map(deltaTokens).toProperty("deltaTokens")
        map(quotaBefore).toProperty("quotaBefore")
        map(quotaAfter).toProperty("quotaAfter")
        map(availableBefore).toProperty("availableBefore")
        map(availableAfter).toProperty("availableAfter")
        map(counterpartyUserId).toProperty("counterpartyUserId")
        map(requestId).toProperty("requestId")
        map(operatorUserId).toProperty("operatorUserId")
        map(remark).toProperty("remark")
        map(createdTime).toProperty("createdTime")
    }

fun UserQuotaTransactionsMapper.insertMultiple(vararg records: UserQuotaTransactionsRecord) =
    insertMultiple(records.toList())

fun UserQuotaTransactionsMapper.insertSelective(record: UserQuotaTransactionsRecord) =
    insert(this::insert, record, UserQuotaTransactions) {
        map(bizNo).toPropertyWhenPresent("bizNo", record::bizNo)
        map(userId).toPropertyWhenPresent("userId", record::userId)
        map(grantId).toPropertyWhenPresent("grantId", record::grantId)
        map(changeType).toPropertyWhenPresent("changeType", record::changeType)
        map(deltaTokens).toPropertyWhenPresent("deltaTokens", record::deltaTokens)
        map(quotaBefore).toPropertyWhenPresent("quotaBefore", record::quotaBefore)
        map(quotaAfter).toPropertyWhenPresent("quotaAfter", record::quotaAfter)
        map(availableBefore).toPropertyWhenPresent("availableBefore", record::availableBefore)
        map(availableAfter).toPropertyWhenPresent("availableAfter", record::availableAfter)
        map(counterpartyUserId).toPropertyWhenPresent("counterpartyUserId", record::counterpartyUserId)
        map(requestId).toPropertyWhenPresent("requestId", record::requestId)
        map(operatorUserId).toPropertyWhenPresent("operatorUserId", record::operatorUserId)
        map(remark).toPropertyWhenPresent("remark", record::remark)
        map(createdTime).toPropertyWhenPresent("createdTime", record::createdTime)
    }

private val columnList = listOf(id, bizNo, userId, grantId, changeType, deltaTokens, quotaBefore, quotaAfter, availableBefore, availableAfter, counterpartyUserId, requestId, operatorUserId, remark, createdTime)

fun UserQuotaTransactionsMapper.selectOne(completer: SelectCompleter) =
    selectOne(this::selectOne, columnList, UserQuotaTransactions, completer)

fun UserQuotaTransactionsMapper.select(completer: SelectCompleter) =
    selectList(this::selectMany, columnList, UserQuotaTransactions, completer)

fun UserQuotaTransactionsMapper.selectDistinct(completer: SelectCompleter) =
    selectDistinct(this::selectMany, columnList, UserQuotaTransactions, completer)

fun UserQuotaTransactionsMapper.selectByPrimaryKey(id_: Long) =
    selectOne {
        where(id, isEqualTo(id_))
    }

fun UserQuotaTransactionsMapper.update(completer: UpdateCompleter) =
    update(this::update, UserQuotaTransactions, completer)

fun KotlinUpdateBuilder.updateAllColumns(record: UserQuotaTransactionsRecord) =
    apply {
        set(bizNo).equalTo(record::bizNo)
        set(userId).equalTo(record::userId)
        set(grantId).equalTo(record::grantId)
        set(changeType).equalTo(record::changeType)
        set(deltaTokens).equalTo(record::deltaTokens)
        set(quotaBefore).equalTo(record::quotaBefore)
        set(quotaAfter).equalTo(record::quotaAfter)
        set(availableBefore).equalTo(record::availableBefore)
        set(availableAfter).equalTo(record::availableAfter)
        set(counterpartyUserId).equalTo(record::counterpartyUserId)
        set(requestId).equalTo(record::requestId)
        set(operatorUserId).equalTo(record::operatorUserId)
        set(remark).equalTo(record::remark)
        set(createdTime).equalTo(record::createdTime)
    }

fun KotlinUpdateBuilder.updateSelectiveColumns(record: UserQuotaTransactionsRecord) =
    apply {
        set(bizNo).equalToWhenPresent(record::bizNo)
        set(userId).equalToWhenPresent(record::userId)
        set(grantId).equalToWhenPresent(record::grantId)
        set(changeType).equalToWhenPresent(record::changeType)
        set(deltaTokens).equalToWhenPresent(record::deltaTokens)
        set(quotaBefore).equalToWhenPresent(record::quotaBefore)
        set(quotaAfter).equalToWhenPresent(record::quotaAfter)
        set(availableBefore).equalToWhenPresent(record::availableBefore)
        set(availableAfter).equalToWhenPresent(record::availableAfter)
        set(counterpartyUserId).equalToWhenPresent(record::counterpartyUserId)
        set(requestId).equalToWhenPresent(record::requestId)
        set(operatorUserId).equalToWhenPresent(record::operatorUserId)
        set(remark).equalToWhenPresent(record::remark)
        set(createdTime).equalToWhenPresent(record::createdTime)
    }

fun UserQuotaTransactionsMapper.updateByPrimaryKey(record: UserQuotaTransactionsRecord) =
    update {
        set(bizNo).equalTo(record::bizNo)
        set(userId).equalTo(record::userId)
        set(grantId).equalTo(record::grantId)
        set(changeType).equalTo(record::changeType)
        set(deltaTokens).equalTo(record::deltaTokens)
        set(quotaBefore).equalTo(record::quotaBefore)
        set(quotaAfter).equalTo(record::quotaAfter)
        set(availableBefore).equalTo(record::availableBefore)
        set(availableAfter).equalTo(record::availableAfter)
        set(counterpartyUserId).equalTo(record::counterpartyUserId)
        set(requestId).equalTo(record::requestId)
        set(operatorUserId).equalTo(record::operatorUserId)
        set(remark).equalTo(record::remark)
        set(createdTime).equalTo(record::createdTime)
        where(id, isEqualTo(record::id))
    }

fun UserQuotaTransactionsMapper.updateByPrimaryKeySelective(record: UserQuotaTransactionsRecord) =
    update {
        set(bizNo).equalToWhenPresent(record::bizNo)
        set(userId).equalToWhenPresent(record::userId)
        set(grantId).equalToWhenPresent(record::grantId)
        set(changeType).equalToWhenPresent(record::changeType)
        set(deltaTokens).equalToWhenPresent(record::deltaTokens)
        set(quotaBefore).equalToWhenPresent(record::quotaBefore)
        set(quotaAfter).equalToWhenPresent(record::quotaAfter)
        set(availableBefore).equalToWhenPresent(record::availableBefore)
        set(availableAfter).equalToWhenPresent(record::availableAfter)
        set(counterpartyUserId).equalToWhenPresent(record::counterpartyUserId)
        set(requestId).equalToWhenPresent(record::requestId)
        set(operatorUserId).equalToWhenPresent(record::operatorUserId)
        set(remark).equalToWhenPresent(record::remark)
        set(createdTime).equalToWhenPresent(record::createdTime)
        where(id, isEqualTo(record::id))
    }