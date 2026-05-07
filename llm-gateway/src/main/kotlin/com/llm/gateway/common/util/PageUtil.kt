package com.llm.gateway.common.util

import com.github.pagehelper.PageInfo
import com.github.pagehelper.page.PageMethod

/**
 * @author JayCHou <a href="calljaychou@qq.com">Email</a>
 */

/**
 * 分页运行
 * @param [pageSize] 页面大小
 * @param [supplier] 数据提供者（DB查询）
 * @param [consumer] 数据消费者
 */
inline fun <E, reified T> T.pageConsume(
    pageSize: Int,
    supplier: () -> List<E>,
    consumer: (List<E>) -> Unit,
) {
    var pageNum = 1
    var hasNextPage: Boolean
    do {
        PageMethod.startPage<E>(pageNum++, pageSize)
        val data = supplier.invoke()
        if (data.isNotEmpty()) consumer.invoke(data)
        hasNextPage = PageInfo.of(data).isHasNextPage
    } while (hasNextPage)
}

/**
 * 分页运行
 * @param [pageSize] 页面大小
 * @param [supplier] 数据提供者（DB查询）
 * @param [action] 对每个元素执行给定的操作
 */
inline fun <E, reified T> T.iterablePageConsume(
    pageSize: Int,
    supplier: () -> List<E>,
    action: (E) -> Unit,
) {
    var pageNum = 1
    var hasNextPage: Boolean
    do {
        PageMethod.startPage<E>(pageNum++, pageSize)
        val data = supplier.invoke()
        // 判空
        if (data.isNotEmpty()) data.forEach(action)
        hasNextPage = PageInfo.of(data).isHasNextPage
    } while (hasNextPage)
}

/**
 * 分页运行
 * @param [pageSize] 页面大小
 * @param [maxSize]  限制大小
 * @param [supplier] 数据提供者（DB查询）
 * @param [consumer] 数据消费者
 */
inline fun <E, reified T> T.pageConsume(
    pageSize: Int,
    maxSize: Int,
    supplier: () -> List<E>,
    consumer: (List<E>) -> Unit,
) {
    var pageNum = 1
    var hasNextPage: Boolean
    do {
        PageMethod.startPage<E>(pageNum++, pageSize)
        val data = supplier.invoke()
        if (data.isNotEmpty()) consumer.invoke(data)
        hasNextPage = (pageNum - 1) * pageSize < maxSize && PageInfo.of(data).isHasNextPage
    } while (hasNextPage)
}

/**
 * 分页运行
 * @param [pageSize] 页面大小
 * @param [supplier] 数据提供者
 * @param [consumer] 数据消费者
 */
inline fun <reified T> pageConsume(
    pageSize: Int,
    supplier: (Int, Int) -> Pair<Long, List<T>>,
    consumer: (List<T>) -> Unit,
) {
    var pageNum = 1
    var hasNextPage: Boolean
    do {
        val (total, list) = supplier.invoke(pageNum++, pageSize)
        if (list.isNotEmpty()) consumer.invoke(list)
        hasNextPage = ((pageNum - 1 * pageSize) < total) && list.isNotEmpty()
    } while (hasNextPage)
}

inline fun nextDelete(deleter: () -> Int) {
    do {
        val deletedRows = deleter.invoke()
    } while (deletedRows > 0)
}

inline fun nextUpdate(updater: () -> Int) {
    do {
        val updateRows = updater.invoke()
    } while (updateRows > 0)
}