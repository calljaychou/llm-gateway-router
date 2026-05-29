/*
 * Auto-generated file. Created by MyBatis Generator
 * Generation date: 2026-05-29T14:51:25.345572+08:00
 */
package com.llm.gateway.dal.mapper

import com.llm.gateway.dal.mapper.UserQuotaTransactionsDynamicSqlSupport.UserQuotaTransactions
import com.llm.gateway.dal.mapper.UserQuotaTransactionsDynamicSqlSupport.UserQuotaTransactions.availableAfterAmount
import com.llm.gateway.dal.mapper.UserQuotaTransactionsDynamicSqlSupport.UserQuotaTransactions.availableBeforeAmount
import com.llm.gateway.dal.mapper.UserQuotaTransactionsDynamicSqlSupport.UserQuotaTransactions.bizNo
import com.llm.gateway.dal.mapper.UserQuotaTransactionsDynamicSqlSupport.UserQuotaTransactions.changeType
import com.llm.gateway.dal.mapper.UserQuotaTransactionsDynamicSqlSupport.UserQuotaTransactions.counterpartyUserId
import com.llm.gateway.dal.mapper.UserQuotaTransactionsDynamicSqlSupport.UserQuotaTransactions.createdTime
import com.llm.gateway.dal.mapper.UserQuotaTransactionsDynamicSqlSupport.UserQuotaTransactions.deltaAmount
import com.llm.gateway.dal.mapper.UserQuotaTransactionsDynamicSqlSupport.UserQuotaTransactions.grantId
import com.llm.gateway.dal.mapper.UserQuotaTransactionsDynamicSqlSupport.UserQuotaTransactions.id
import com.llm.gateway.dal.mapper.UserQuotaTransactionsDynamicSqlSupport.UserQuotaTransactions.operatorUserId
import com.llm.gateway.dal.mapper.UserQuotaTransactionsDynamicSqlSupport.UserQuotaTransactions.quotaAfterAmount
import com.llm.gateway.dal.mapper.UserQuotaTransactionsDynamicSqlSupport.UserQuotaTransactions.quotaBeforeAmount
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
        map(deltaAmount).toProperty("deltaAmount")
        map(quotaBeforeAmount).toProperty("quotaBeforeAmount")
        map(quotaAfterAmount).toProperty("quotaAfterAmount")
        map(availableBeforeAmount).toProperty("availableBeforeAmount")
        map(availableAfterAmount).toProperty("availableAfterAmount")
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
        map(deltaAmount).toProperty("deltaAmount")
        map(quotaBeforeAmount).toProperty("quotaBeforeAmount")
        map(quotaAfterAmount).toProperty("quotaAfterAmount")
        map(availableBeforeAmount).toProperty("availableBeforeAmount")
        map(availableAfterAmount).toProperty("availableAfterAmount")
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
        map(deltaAmount).toPropertyWhenPresent("deltaAmount", record::deltaAmount)
        map(quotaBeforeAmount).toPropertyWhenPresent("quotaBeforeAmount", record::quotaBeforeAmount)
        map(quotaAfterAmount).toPropertyWhenPresent("quotaAfterAmount", record::quotaAfterAmount)
        map(availableBeforeAmount).toPropertyWhenPresent("availableBeforeAmount", record::availableBeforeAmount)
        map(availableAfterAmount).toPropertyWhenPresent("availableAfterAmount", record::availableAfterAmount)
        map(counterpartyUserId).toPropertyWhenPresent("counterpartyUserId", record::counterpartyUserId)
        map(requestId).toPropertyWhenPresent("requestId", record::requestId)
        map(operatorUserId).toPropertyWhenPresent("operatorUserId", record::operatorUserId)
        map(remark).toPropertyWhenPresent("remark", record::remark)
        map(createdTime).toPropertyWhenPresent("createdTime", record::createdTime)
    }

private val columnList = listOf(id, bizNo, userId, grantId, changeType, deltaAmount, quotaBeforeAmount, quotaAfterAmount, availableBeforeAmount, availableAfterAmount, counterpartyUserId, requestId, operatorUserId, remark, createdTime)

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
        set(deltaAmount).equalTo(record::deltaAmount)
        set(quotaBeforeAmount).equalTo(record::quotaBeforeAmount)
        set(quotaAfterAmount).equalTo(record::quotaAfterAmount)
        set(availableBeforeAmount).equalTo(record::availableBeforeAmount)
        set(availableAfterAmount).equalTo(record::availableAfterAmount)
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
        set(deltaAmount).equalToWhenPresent(record::deltaAmount)
        set(quotaBeforeAmount).equalToWhenPresent(record::quotaBeforeAmount)
        set(quotaAfterAmount).equalToWhenPresent(record::quotaAfterAmount)
        set(availableBeforeAmount).equalToWhenPresent(record::availableBeforeAmount)
        set(availableAfterAmount).equalToWhenPresent(record::availableAfterAmount)
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
        set(deltaAmount).equalTo(record::deltaAmount)
        set(quotaBeforeAmount).equalTo(record::quotaBeforeAmount)
        set(quotaAfterAmount).equalTo(record::quotaAfterAmount)
        set(availableBeforeAmount).equalTo(record::availableBeforeAmount)
        set(availableAfterAmount).equalTo(record::availableAfterAmount)
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
        set(deltaAmount).equalToWhenPresent(record::deltaAmount)
        set(quotaBeforeAmount).equalToWhenPresent(record::quotaBeforeAmount)
        set(quotaAfterAmount).equalToWhenPresent(record::quotaAfterAmount)
        set(availableBeforeAmount).equalToWhenPresent(record::availableBeforeAmount)
        set(availableAfterAmount).equalToWhenPresent(record::availableAfterAmount)
        set(counterpartyUserId).equalToWhenPresent(record::counterpartyUserId)
        set(requestId).equalToWhenPresent(record::requestId)
        set(operatorUserId).equalToWhenPresent(record::operatorUserId)
        set(remark).equalToWhenPresent(record::remark)
        set(createdTime).equalToWhenPresent(record::createdTime)
        where(id, isEqualTo(record::id))
    }