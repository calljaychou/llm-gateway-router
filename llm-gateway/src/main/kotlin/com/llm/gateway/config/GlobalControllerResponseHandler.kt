package com.llm.gateway.config

import com.llm.gateway.common.annotation.ExcludeResultHandler
import com.llm.gateway.model.ApiResult
import org.springframework.core.MethodParameter
import org.springframework.http.MediaType
import org.springframework.http.converter.HttpMessageConverter
import org.springframework.http.server.ServerHttpRequest
import org.springframework.http.server.ServerHttpResponse
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice

@RestControllerAdvice(basePackages = ["com.llm.gateway"])
class GlobalControllerResponseHandler : ResponseBodyAdvice<Any?> {

    override fun supports(methodParameter: MethodParameter, aClass: Class<out HttpMessageConverter<*>>): Boolean {
        return methodParameter.method?.isAnnotationPresent(ExcludeResultHandler::class.java) == false
    }

    override fun beforeBodyWrite(
        body: Any?,
        returnType: MethodParameter,
        selectedContentType: MediaType,
        selectedConverterType: Class<out HttpMessageConverter<*>>,
        request: ServerHttpRequest,
        response: ServerHttpResponse,
    ): Any? {
        return if (body is String || body is ApiResult<*>) body else ApiResult.success(body)
    }
}