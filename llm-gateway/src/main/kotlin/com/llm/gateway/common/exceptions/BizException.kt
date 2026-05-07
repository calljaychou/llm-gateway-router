package com.llm.gateway.common.exceptions

/**
 * @author JayCHou <a href="calljaychou@qq.com">Email</a>
 */
class BizException : RuntimeException {

    var code: Int

    constructor(message: String) : super(message) {
        this.code = 500
    }

    constructor(code: Int, message: String) : super(message) {
        this.code = code
    }

    constructor(code: Int, message: String, cause: Throwable) : super(message, cause) {
        this.code = code
    }

    companion object {
        const val SYSTEM_FAILED = 500
        const val BUSINESS_FAILED = 400
        const val UNAUTHORIZED = 401
    }

}