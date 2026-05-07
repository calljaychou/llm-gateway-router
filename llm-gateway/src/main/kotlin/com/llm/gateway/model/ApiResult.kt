package com.llm.gateway.model

import java.time.Instant

/**
 * @author JayCHou <a href="calljaychou@qq.com">Email</a>
 */
data class ApiResult<T>(
    var code: Int? = null,
    var success: Boolean? = null,
    var message: String? = null,
    var timestamp: Long? = null,
    var data: T? = null
) {
    companion object {
        fun <T> success(data: T? = null): ApiResult<T> {
            return ApiResult(200, true, "成功", Instant.now().toEpochMilli(), data)
        }

        fun <T> fail(): ApiResult<T> {
            return ApiResult(500, false, "系统异常，请稍后再试", Instant.now().toEpochMilli(), null)
        }

        fun <T> fail(code: Int, message: String?): ApiResult<T> {
            return ApiResult(code, false, message, Instant.now().toEpochMilli(), null)
        }

        fun <T> fail(message: String?): ApiResult<T> {
            return ApiResult(500, false, message, Instant.now().toEpochMilli(), null)
        }
    }
}