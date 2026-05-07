package com.llm.gateway.config

import com.llm.gateway.common.exceptions.BizException
import com.llm.gateway.common.logger
import com.llm.gateway.model.ApiResult
import io.swagger.annotations.ApiOperation
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.http.HttpStatus
import org.springframework.validation.BindException
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.method.HandlerMethod

@RestControllerAdvice
class GlobalControllerExceptionHandler {

    private val log = logger()

    private fun getExtLogInfo(handlerMethod: HandlerMethod): String {
        val apiOperation = handlerMethod.getMethodAnnotation(ApiOperation::class.java)
        val exiLogInfo = if (apiOperation != null) {
            "接口[${apiOperation.value}], "
        } else "接口[${handlerMethod.method.declaringClass.typeName}.${handlerMethod.method.name}], "
        return exiLogInfo
    }

    @ExceptionHandler(MethodArgumentNotValidException::class)
    @ResponseStatus(value = HttpStatus.OK)
    fun methodArgumentNotValidExceptionHandler(
        exception: MethodArgumentNotValidException,
        handlerMethod: HandlerMethod,
    ): Any? {
        log.error("${getExtLogInfo(handlerMethod)}全局参数异常捕获", exception)
        return ApiResult.fail<Unit>(
            400,
            exception.bindingResult.fieldErrors.firstOrNull()?.defaultMessage ?: "参数异常"
        )
    }

    @ExceptionHandler(BindException::class)
    @ResponseStatus(value = HttpStatus.OK)
    fun bindExceptionHandler(exception: BindException, handlerMethod: HandlerMethod): Any? {
        log.error("${getExtLogInfo(handlerMethod)}全局参数异常捕获", exception)
        return ApiResult.fail<Unit>(
            400,
            exception.bindingResult.fieldErrors.firstOrNull()?.defaultMessage ?: "参数异常"
        )
    }

    @ExceptionHandler(Exception::class)
    @ResponseStatus(value = HttpStatus.OK)
    fun exceptionHandler(exception: Exception, handlerMethod: HandlerMethod): Any? {
        log.error("${getExtLogInfo(handlerMethod)}全局异常捕获", exception)
        return ApiResult.fail<Unit>(500, "系统繁忙，请稍后重试")
    }

    @ExceptionHandler(IllegalArgumentException::class)
    @ResponseStatus(value = HttpStatus.OK)
    fun argumentExceptionHandler(exception: IllegalArgumentException, handlerMethod: HandlerMethod): Any? {
        log.error("${getExtLogInfo(handlerMethod)}参数异常捕获", exception)
        return ApiResult.fail<Unit>(400, exception.message)
    }

    @ExceptionHandler(BizException::class)
    @ResponseStatus(value = HttpStatus.OK)
    fun bizExceptionHandler(exception: BizException, handlerMethod: HandlerMethod): Any? {
        log.error("${getExtLogInfo(handlerMethod)}业务异常捕获", exception)
        return ApiResult.fail<Unit>(exception.code, exception.message)
    }


    @ExceptionHandler(IllegalStateException::class)
    @ResponseStatus(value = HttpStatus.OK)
    fun illegalStateExceptionHandler(exception: IllegalStateException, handlerMethod: HandlerMethod): Any? {
        log.error("${getExtLogInfo(handlerMethod)}数据校验异常", exception)
        return ApiResult.fail<Unit>(400, exception.message)
    }

    @ExceptionHandler(DataIntegrityViolationException::class)
    @ResponseStatus(value = HttpStatus.OK)
    fun dataIntegrityViolationExceptionHandler(
        exception: DataIntegrityViolationException,
        handlerMethod: HandlerMethod,
    ): Any? {
        log.error("${getExtLogInfo(handlerMethod)}数据保存异常", exception)
        return ApiResult.fail<Unit>(
            500,
            "数据保存异常" + if (exception.message?.contains("Data too long for column") == true) ",数据超长" else ""
        )
    }

}