package com.llm.gateway.common

import cn.hutool.crypto.digest.MD5
import com.alibaba.fastjson2.JSON
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.github.pagehelper.PageInfo
import com.llm.gateway.common.util.SpringContextUtil
import java.math.BigDecimal
import java.math.RoundingMode
import java.util.TimeZone
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.BeanUtils

/**
 * @author JayCHou <a href="calljaychou@qq.com">Email</a>
 */
fun jsonMapper(): ObjectMapper = jacksonObjectMapper()
    .disable(SerializationFeature.FAIL_ON_EMPTY_BEANS)
    .setSerializationInclusion(JsonInclude.Include.NON_NULL)
    .setTimeZone(TimeZone.getTimeZone("GMT+8:00"))
    .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)

inline fun <reified T> T.logger(): Logger = LoggerFactory.getLogger(T::class.java)

inline fun Boolean.then(block: () -> Unit): Boolean {
    if (this) block.invoke()
    return this
}

inline fun Boolean.otherwise(block: () -> Unit): Boolean {
    if (!this) block.invoke()
    return this
}

fun <T> paramHex(vararg elements: T): String? = MD5.create().digestHex(JSON.toJSONString(elements))

inline fun <reified T> T.selfBean(): T {
    return SpringContextUtil.applicationContext.getBean(this!!::class.java)
}

fun <T> getBean(clazz: Class<T>): T {
    return SpringContextUtil.applicationContext.getBean(clazz)
}

inline fun <T, reified R : Any> T.mapType(vararg ignores: String): R {
    val that = this
    return R::class.java.getDeclaredConstructor().apply { isAccessible = true }.newInstance().apply {
        BeanUtils.copyProperties(that!!, this, *ignores)
    }
}

inline fun <T : Any, reified R : Any> List<T>.mapType(): List<R> {
    return this.map { it.mapType() }
}

val oneHundred = BigDecimal(100)

fun Long?.fen2Yuan(): BigDecimal? = this?.let { BigDecimal.valueOf(this).divide(oneHundred, 2, RoundingMode.HALF_UP) }

fun BigDecimal?.yuan2fen(): Long? = this?.multiply(oneHundred)?.toLong()

fun <E, T> PageInfo<E>.convert(converter: (E) -> T): PageInfo<T> {
    val page = this
    return PageInfo.of(list.map { converter.invoke(it) }).apply {
        pageNum = page.pageNum
        pageSize = page.pageSize
        startRow = page.startRow
        endRow = page.endRow
        total = page.total
        pages = page.pages
        startRow = page.startRow
        endRow = page.endRow
    }
}