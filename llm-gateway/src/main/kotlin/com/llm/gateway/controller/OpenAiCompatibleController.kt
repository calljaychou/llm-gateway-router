package com.llm.gateway.controller

import com.llm.gateway.common.annotation.ExcludeResultHandler
import com.llm.gateway.security.CustomUserDetails
import com.llm.gateway.service.OpenAiForwardFacade
import io.swagger.annotations.Api
import io.swagger.annotations.ApiOperation
import io.swagger.annotations.ApiParam
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse
import org.springframework.security.core.Authentication
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter

@Api(tags = ["OpenAI兼容接口"])
@RestController
@RequestMapping("/v1")
class OpenAiCompatibleController(
    private val openAiForwardFacade: OpenAiForwardFacade,
) {

    @ExcludeResultHandler
    @ApiOperation("Chat Completions（OpenAI协议转发）")
    @PostMapping("/chat/completions")
    fun chatCompletions(
        @ApiParam("OpenAI Chat Completions 请求体（透传扩展字段）")
        @RequestBody payload: Map<String, Any?>,
        authentication: Authentication,
        request: HttpServletRequest,
        response: HttpServletResponse,
    ): Any {
        val authorization = request.getHeader("Authorization").orEmpty().trim()
        if (!authorization.startsWith("Bearer sk-vkey-")) {
            return openAiError("无效API密钥", "VIRTUAL_API_KEY_REQUIRED")
        }
        val principal = authentication.principal as? CustomUserDetails
            ?: return openAiError("未认证", "UNAUTHORIZED")
        val user = principal.getUser()
        val userId = user.id
            ?: return openAiError("用户ID缺失", "UNAUTHORIZED")
        val virtualApiKey = authorization.removePrefix("Bearer ").trim()

        val result = openAiForwardFacade.chatCompletions(userId, user.deptId, payload, virtualApiKey)
        if (result is SseEmitter) {
            response.contentType = MediaType.TEXT_EVENT_STREAM_VALUE
            response.characterEncoding = Charsets.UTF_8.name()
        }
        return result
    }

    /** 生成 OpenAI 协议格式的统一错误响应。 */
    private fun openAiError(message: String, code: String): ResponseEntity<Any?> {
        val body = mapOf(
            "error" to mapOf(
                "message" to message,
                "type" to "invalid_api_key",
                "code" to code,
            )
        )
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
            .contentType(MediaType.APPLICATION_JSON)
            .body(body)
    }
}
