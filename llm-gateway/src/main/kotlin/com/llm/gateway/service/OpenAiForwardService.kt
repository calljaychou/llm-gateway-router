package com.llm.gateway.service

import com.alibaba.fastjson2.JSON
import com.llm.gateway.common.enums.NormalStatus
import com.llm.gateway.common.exceptions.BizException
import com.llm.gateway.dal.mapper.MasterKeysDynamicSqlSupport
import com.llm.gateway.dal.mapper.MasterKeysMapper
import com.llm.gateway.dal.mapper.ModelsDynamicSqlSupport
import com.llm.gateway.dal.mapper.ModelsMapper
import com.llm.gateway.dal.mapper.VendorsDynamicSqlSupport
import com.llm.gateway.dal.mapper.VendorsMapper
import com.llm.gateway.dal.mapper.selectOne
import com.llm.gateway.model.dto.ForwardContextDto
import java.nio.charset.StandardCharsets
import java.security.MessageDigest
import java.time.Duration
import java.util.Base64
import javax.crypto.Cipher
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.SecretKeySpec
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

@Service
class OpenAiForwardService(
    private val modelsMapper: ModelsMapper,
    private val vendorsMapper: VendorsMapper,
    private val masterKeysMapper: MasterKeysMapper,
    private val adminUserPermissionService: AdminUserPermissionService,
    private val webClientBuilder: WebClient.Builder,
    @Value("\${gateway.aes-secret}") private val aesSecret: String,
    @Value("\${gateway.forward-timeout-seconds}") private val forwardTimeoutSeconds: Long,
) {
    fun buildForwardContext(userId: Long, payload: Map<String, Any?>): ForwardContextDto {
        val modelAlias = payload["model"]?.toString()?.trim().orEmpty()
        if (modelAlias.isBlank()) {
            throw BizException(BizException.BUSINESS_FAILED, "model 不能为空")
        }

        adminUserPermissionService.assertUserCanAccessModel(userId, modelAlias)

        val modelRecord = modelsMapper.selectOne {
            where { ModelsDynamicSqlSupport.Models.modelAlias isEqualTo modelAlias }
            and { ModelsDynamicSqlSupport.Models.active isEqualTo true }
        } ?: throw BizException(BizException.BUSINESS_FAILED, "模型不存在或未启用")

        val vendorId = modelRecord.vendorId ?: throw BizException(BizException.SYSTEM_FAILED, "模型供应商配置缺失")
        val vendorRecord = vendorsMapper.selectOne {
            where { VendorsDynamicSqlSupport.Vendors.id isEqualTo vendorId }
            and { VendorsDynamicSqlSupport.Vendors.status isEqualTo NormalStatus }
        } ?: throw BizException(BizException.BUSINESS_FAILED, "供应商不存在或已停用")

        val masterKey = masterKeysMapper.selectOne {
            where { MasterKeysDynamicSqlSupport.MasterKeys.vendorId isEqualTo vendorId }
            and { MasterKeysDynamicSqlSupport.MasterKeys.status isEqualTo NormalStatus }
            orderBy(
                MasterKeysDynamicSqlSupport.MasterKeys.weight.descending(),
                MasterKeysDynamicSqlSupport.MasterKeys.id
            )
            limit(1)
        } ?: throw BizException(BizException.BUSINESS_FAILED, "主密钥不可用")

        val apiKey =
            decryptApiKey(masterKey.apiKeyEncrypted ?: throw BizException(BizException.SYSTEM_FAILED, "主密钥缺失"))
        val realModelName = modelRecord.realModelName?.trim()
            ?: throw BizException(BizException.SYSTEM_FAILED, "模型真实名称未配置")
        val forwardPayload = LinkedHashMap(payload).apply { this["model"] = realModelName }

        val stream = payload["stream"]?.toString()?.equals("true", ignoreCase = true) ?: false

        return ForwardContextDto(
            targetUrl = vendorRecord.baseUrl!!.trimEnd('/'),
            apiKey = apiKey,
            payload = forwardPayload,
            stream = stream,
            modelAlias = modelAlias,
            vendorId = vendorId,
        )
    }

    fun forwardJson(context: ForwardContextDto): Mono<ResponseEntity<*>> {
        return webClientBuilder.build().post()
            .uri(context.targetUrl)
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON)
            .headers { headers -> headers.setBearerAuth(context.apiKey) }
            .bodyValue(context.payload)
            .exchange()
            .flatMap { response ->
                response.bodyToMono(String::class.java)
                    .defaultIfEmpty("")
                    .map { body ->
                        val upstreamHeaders = response.headers().asHttpHeaders()
                        val builder = ResponseEntity.status(response.statusCode())
                            .contentType(resolveContentType(upstreamHeaders.contentType, body))
                        if (response.statusCode() == HttpStatus.TOO_MANY_REQUESTS) {
                            builder.header("X-RateLimit-Source", "upstream")
                            upstreamHeaders["Retry-After"]?.firstOrNull()?.let { builder.header("Retry-After", it) }
                        }
                        builder.body(parseBody(body))
                    }
            }.timeout(Duration.ofSeconds(forwardTimeoutSeconds)) as Mono<ResponseEntity<*>>
    }

    fun forwardStream(context: ForwardContextDto): ResponseEntity<*> {
        val emitter = SseEmitter(forwardTimeoutSeconds * 1000)
        webClientBuilder.build().post()
            .uri(context.targetUrl)
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.TEXT_EVENT_STREAM, MediaType.APPLICATION_JSON)
            .headers { headers -> headers.setBearerAuth(context.apiKey) }
            .bodyValue(context.payload)
            .exchange()
            .flatMapMany { response ->
                if (response.statusCode().is2xxSuccessful) {
                    response.bodyToFlux(String::class.java)
                } else {
                    response.bodyToMono(String::class.java)
                        .defaultIfEmpty("")
                        .flatMapMany { body ->
                            Flux.error(
                                BizException(
                                    response.statusCode().value(),
                                    "流式转发失败: ${body.take(200)}"
                                )
                            )
                        }
                }
            }
            .timeout(Duration.ofSeconds(forwardTimeoutSeconds))
            .subscribe(
                { chunk ->
                    runCatching { emitter.send(chunk) }.onFailure { emitter.completeWithError(it) }
                },
                { error ->
                    val errorPayload = mapOf(
                        "error" to mapOf(
                            "message" to (error.message ?: "流式响应异常"),
                            "type" to "server_error",
                            "code" to "STREAM_FORWARD_FAILED",
                        )
                    )
                    runCatching {
                        emitter.send("data: ${JSON.toJSONString(errorPayload)}\n\n")
                        emitter.send("data: [DONE]\n\n")
                    }
                    emitter.complete()
                },
                {
                    emitter.complete()
                }
            )
        val headers = HttpHeaders()
        headers.contentType = MediaType.TEXT_EVENT_STREAM
        return ResponseEntity.status(HttpStatus.OK)
            .headers(headers)
            .body(emitter)
    }

    /**
     * 对主密钥密文进行 AES-GCM 解密并返回明文 API Key。
     */
    private fun decryptApiKey(encryptedValue: String): String {
        val segments = encryptedValue.split(":")
        if (segments.size != 2) {
            throw BizException(BizException.SYSTEM_FAILED, "主密钥密文格式非法")
        }
        val iv = Base64.getDecoder().decode(segments[0])
        val encrypted = Base64.getDecoder().decode(segments[1])
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.DECRYPT_MODE, buildAesKey(), GCMParameterSpec(128, iv))
        val plainBytes = cipher.doFinal(encrypted)
        return String(plainBytes, StandardCharsets.UTF_8)
    }

    /** 根据配置密钥派生固定长度 AES 对称密钥。 */
    private fun buildAesKey(): SecretKeySpec {
        val digest = MessageDigest.getInstance("SHA-256")
            .digest(aesSecret.toByteArray(StandardCharsets.UTF_8))
        return SecretKeySpec(digest, "AES")
    }

    /** 尝试将上游响应体解析为 JSON，失败时回退为原始字符串。 */
    private fun parseBody(body: String): Any {
        if (body.isBlank()) return emptyMap<String, Any>()
        return try {
            JSON.parse(body)
        } catch (_: Exception) {
            body
        }
    }

    /** 优先沿用上游 Content-Type，缺失时根据响应体内容推断。 */
    private fun resolveContentType(contentType: MediaType?, body: String): MediaType {
        if (contentType != null) {
            return contentType
        }
        return if (body.trim().startsWith("{") || body.trim().startsWith("[")) {
            MediaType.APPLICATION_JSON
        } else {
            MediaType.TEXT_PLAIN
        }
    }

}
