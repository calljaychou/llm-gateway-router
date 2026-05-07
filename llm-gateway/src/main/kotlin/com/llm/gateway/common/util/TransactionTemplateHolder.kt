package com.llm.gateway.common.util

import javax.annotation.PostConstruct
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import org.springframework.transaction.support.TransactionSynchronization
import org.springframework.transaction.support.TransactionSynchronizationManager
import org.springframework.transaction.support.TransactionTemplate

/**
 * @author JayCHou <a href="calljaychou@qq.com">Email</a>
 */

@Component
class TransactionTemplateHolder {
    @Autowired
    private lateinit var injectedTemplate: TransactionTemplate

    @PostConstruct
    fun init() {
        transactionTemplate = injectedTemplate
    }

    companion object {
        // 静态变量
        lateinit var transactionTemplate: TransactionTemplate
    }
}

/**
 * 手动事物执行
 * @param [block] 块
 */
fun transactionOperation(block: () -> Unit) {
    TransactionTemplateHolder.transactionTemplate.execute {
        block.invoke()
    }
}

/**
 * 事物提交后执行
 * @param [block] 块
 */
fun transactionAfterCommit(block: () -> Unit) = TransactionSynchronizationManager.registerSynchronization(object : TransactionSynchronization {
    override fun afterCommit() {
        block.invoke()
    }
})


/**
 * 事物提交后执行
 * @param [block] 块
 */
fun transactionActiveAfterCommit(block: () -> Unit) {
    if (TransactionSynchronizationManager.isSynchronizationActive()) {
        TransactionSynchronizationManager.registerSynchronization(object : TransactionSynchronization {
            override fun afterCommit() {
                block.invoke()
            }
        })
    } else {
        block.invoke()
    }
}