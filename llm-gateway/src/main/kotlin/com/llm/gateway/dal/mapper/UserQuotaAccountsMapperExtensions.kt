/*
 * Auto-generated file. Created by MyBatis Generator
 * Generation date: 2026-05-21T18:47:35.933976+08:00
 */
package com.llm.gateway.dal.mapper

import com.llm.gateway.dal.mapper.UserQuotaAccountsDynamicSqlSupport.UserQuotaAccounts
import com.llm.gateway.dal.mapper.UserQuotaAccountsDynamicSqlSupport.UserQuotaAccounts.allowTransferOut
import com.llm.gateway.dal.mapper.UserQuotaAccountsDynamicSqlSupport.UserQuotaAccounts.availableTokens
import com.llm.gateway.dal.mapper.UserQuotaAccountsDynamicSqlSupport.UserQuotaAccounts.createdTime
import com.llm.gateway.dal.mapper.UserQuotaAccountsDynamicSqlSupport.UserQuotaAccounts.currentQuotaTokens
import com.llm.gateway.dal.mapper.UserQuotaAccountsDynamicSqlSupport.UserQuotaAccounts.earliestExpireAt
import com.llm.gateway.dal.mapper.UserQuotaAccountsDynamicSqlSupport.UserQuotaAccounts.expiredTokens
import com.llm.gateway.dal.mapper.UserQuotaAccountsDynamicSqlSupport.UserQuotaAccounts.id
import com.llm.gateway.dal.mapper.UserQuotaAccountsDynamicSqlSupport.UserQuotaAccounts.transferredInTokens
import com.llm.gateway.dal.mapper.UserQuotaAccountsDynamicSqlSupport.UserQuotaAccounts.transferredOutTokens
import com.llm.gateway.dal.mapper.UserQuotaAccountsDynamicSqlSupport.UserQuotaAccounts.updatedTime
import com.llm.gateway.dal.mapper.UserQuotaAccountsDynamicSqlSupport.UserQuotaAccounts.usedTokens
import com.llm.gateway.dal.mapper.UserQuotaAccountsDynamicSqlSupport.UserQuotaAccounts.userId
import com.llm.gateway.dal.model.UserQuotaAccountsRecord
import org.mybatis.dynamic.sql.SqlBuilder.isEqualTo
import org.mybatis.dynamic.sql.insert.render.MultiRowInsertStatementProvider
import org.mybatis.dynamic.sql.util.kotlin.*
import org.mybatis.dynamic.sql.util.kotlin.mybatis3.*

fun UserQuotaAccountsMapper.count(completer: CountCompleter) =
    countFrom(this::count, UserQuotaAccounts, completer)

fun UserQuotaAccountsMapper.delete(completer: DeleteCompleter) =
    deleteFrom(this::delete, UserQuotaAccounts, completer)

fun UserQuotaAccountsMapper.deleteByPrimaryKey(id_: Long) =
    delete {
        where(id, isEqualTo(id_))
    }

fun UserQuotaAccountsMapper.insertMultipleHelper(multipleInsertStatement: MultiRowInsertStatementProvider<UserQuotaAccountsRecord>) =
    insertMultiple(multipleInsertStatement.insertStatement, multipleInsertStatement.records)

fun UserQuotaAccountsMapper.insert(record: UserQuotaAccountsRecord) =
    insert(this::insert, record, UserQuotaAccounts) {
        map(userId).toProperty("userId")
        map(currentQuotaTokens).toProperty("currentQuotaTokens")
        map(usedTokens).toProperty("usedTokens")
        map(expiredTokens).toProperty("expiredTokens")
        map(transferredInTokens).toProperty("transferredInTokens")
        map(transferredOutTokens).toProperty("transferredOutTokens")
        map(availableTokens).toProperty("availableTokens")
        map(allowTransferOut).toProperty("allowTransferOut")
        map(earliestExpireAt).toProperty("earliestExpireAt")
        map(updatedTime).toProperty("updatedTime")
        map(createdTime).toProperty("createdTime")
    }

fun UserQuotaAccountsMapper.insertMultiple(records: Collection<UserQuotaAccountsRecord>) =
    insertMultiple(this::insertMultipleHelper, records, UserQuotaAccounts) {
        map(userId).toProperty("userId")
        map(currentQuotaTokens).toProperty("currentQuotaTokens")
        map(usedTokens).toProperty("usedTokens")
        map(expiredTokens).toProperty("expiredTokens")
        map(transferredInTokens).toProperty("transferredInTokens")
        map(transferredOutTokens).toProperty("transferredOutTokens")
        map(availableTokens).toProperty("availableTokens")
        map(allowTransferOut).toProperty("allowTransferOut")
        map(earliestExpireAt).toProperty("earliestExpireAt")
        map(updatedTime).toProperty("updatedTime")
        map(createdTime).toProperty("createdTime")
    }

fun UserQuotaAccountsMapper.insertMultiple(vararg records: UserQuotaAccountsRecord) =
    insertMultiple(records.toList())

fun UserQuotaAccountsMapper.insertSelective(record: UserQuotaAccountsRecord) =
    insert(this::insert, record, UserQuotaAccounts) {
        map(userId).toPropertyWhenPresent("userId", record::userId)
        map(currentQuotaTokens).toPropertyWhenPresent("currentQuotaTokens", record::currentQuotaTokens)
        map(usedTokens).toPropertyWhenPresent("usedTokens", record::usedTokens)
        map(expiredTokens).toPropertyWhenPresent("expiredTokens", record::expiredTokens)
        map(transferredInTokens).toPropertyWhenPresent("transferredInTokens", record::transferredInTokens)
        map(transferredOutTokens).toPropertyWhenPresent("transferredOutTokens", record::transferredOutTokens)
        map(availableTokens).toPropertyWhenPresent("availableTokens", record::availableTokens)
        map(allowTransferOut).toPropertyWhenPresent("allowTransferOut", record::allowTransferOut)
        map(earliestExpireAt).toPropertyWhenPresent("earliestExpireAt", record::earliestExpireAt)
        map(updatedTime).toPropertyWhenPresent("updatedTime", record::updatedTime)
        map(createdTime).toPropertyWhenPresent("createdTime", record::createdTime)
    }

private val columnList = listOf(id, userId, currentQuotaTokens, usedTokens, expiredTokens, transferredInTokens, transferredOutTokens, availableTokens, allowTransferOut, earliestExpireAt, updatedTime, createdTime)

fun UserQuotaAccountsMapper.selectOne(completer: SelectCompleter) =
    selectOne(this::selectOne, columnList, UserQuotaAccounts, completer)

fun UserQuotaAccountsMapper.select(completer: SelectCompleter) =
    selectList(this::selectMany, columnList, UserQuotaAccounts, completer)

fun UserQuotaAccountsMapper.selectDistinct(completer: SelectCompleter) =
    selectDistinct(this::selectMany, columnList, UserQuotaAccounts, completer)

fun UserQuotaAccountsMapper.selectByPrimaryKey(id_: Long) =
    selectOne {
        where(id, isEqualTo(id_))
    }

fun UserQuotaAccountsMapper.update(completer: UpdateCompleter) =
    update(this::update, UserQuotaAccounts, completer)

fun KotlinUpdateBuilder.updateAllColumns(record: UserQuotaAccountsRecord) =
    apply {
        set(userId).equalTo(record::userId)
        set(currentQuotaTokens).equalTo(record::currentQuotaTokens)
        set(usedTokens).equalTo(record::usedTokens)
        set(expiredTokens).equalTo(record::expiredTokens)
        set(transferredInTokens).equalTo(record::transferredInTokens)
        set(transferredOutTokens).equalTo(record::transferredOutTokens)
        set(availableTokens).equalTo(record::availableTokens)
        set(allowTransferOut).equalTo(record::allowTransferOut)
        set(earliestExpireAt).equalTo(record::earliestExpireAt)
        set(updatedTime).equalTo(record::updatedTime)
        set(createdTime).equalTo(record::createdTime)
    }

fun KotlinUpdateBuilder.updateSelectiveColumns(record: UserQuotaAccountsRecord) =
    apply {
        set(userId).equalToWhenPresent(record::userId)
        set(currentQuotaTokens).equalToWhenPresent(record::currentQuotaTokens)
        set(usedTokens).equalToWhenPresent(record::usedTokens)
        set(expiredTokens).equalToWhenPresent(record::expiredTokens)
        set(transferredInTokens).equalToWhenPresent(record::transferredInTokens)
        set(transferredOutTokens).equalToWhenPresent(record::transferredOutTokens)
        set(availableTokens).equalToWhenPresent(record::availableTokens)
        set(allowTransferOut).equalToWhenPresent(record::allowTransferOut)
        set(earliestExpireAt).equalToWhenPresent(record::earliestExpireAt)
        set(updatedTime).equalToWhenPresent(record::updatedTime)
        set(createdTime).equalToWhenPresent(record::createdTime)
    }

fun UserQuotaAccountsMapper.updateByPrimaryKey(record: UserQuotaAccountsRecord) =
    update {
        set(userId).equalTo(record::userId)
        set(currentQuotaTokens).equalTo(record::currentQuotaTokens)
        set(usedTokens).equalTo(record::usedTokens)
        set(expiredTokens).equalTo(record::expiredTokens)
        set(transferredInTokens).equalTo(record::transferredInTokens)
        set(transferredOutTokens).equalTo(record::transferredOutTokens)
        set(availableTokens).equalTo(record::availableTokens)
        set(allowTransferOut).equalTo(record::allowTransferOut)
        set(earliestExpireAt).equalTo(record::earliestExpireAt)
        set(updatedTime).equalTo(record::updatedTime)
        set(createdTime).equalTo(record::createdTime)
        where(id, isEqualTo(record::id))
    }

fun UserQuotaAccountsMapper.updateByPrimaryKeySelective(record: UserQuotaAccountsRecord) =
    update {
        set(userId).equalToWhenPresent(record::userId)
        set(currentQuotaTokens).equalToWhenPresent(record::currentQuotaTokens)
        set(usedTokens).equalToWhenPresent(record::usedTokens)
        set(expiredTokens).equalToWhenPresent(record::expiredTokens)
        set(transferredInTokens).equalToWhenPresent(record::transferredInTokens)
        set(transferredOutTokens).equalToWhenPresent(record::transferredOutTokens)
        set(availableTokens).equalToWhenPresent(record::availableTokens)
        set(allowTransferOut).equalToWhenPresent(record::allowTransferOut)
        set(earliestExpireAt).equalToWhenPresent(record::earliestExpireAt)
        set(updatedTime).equalToWhenPresent(record::updatedTime)
        set(createdTime).equalToWhenPresent(record::createdTime)
        where(id, isEqualTo(record::id))
    }