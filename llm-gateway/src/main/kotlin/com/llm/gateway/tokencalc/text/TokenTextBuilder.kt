package com.llm.gateway.tokencalc.text

import com.alibaba.fastjson2.JSON
import com.alibaba.fastjson2.JSONArray
import com.alibaba.fastjson2.JSONObject
import com.llm.gateway.common.logger

object TokenTextBuilder {

    private val log = logger()

    /**
     * 构造 Chat prompt 的可估算文本。
     * 当前本地估算不是最终 tokenizer，只保证把 messages、tools、response_format 等关键输入纳入估算范围。
     */
    fun buildOpenAiChatPromptText(requestBody: String?): String {
        val root = parseObject(requestBody) ?: return ""
        return buildString {
            appendMessages(root.getJSONArray("messages"))
            appendJsonField(root, "tools")
            appendJsonField(root, "response_format")
        }.trim()
    }

    /**
     * 构造 Chat completion 的可估算文本。
     * 非流式优先读取 choices[].message.content，兼容后续复用时的 choices[].delta.content。
     */
    fun buildOpenAiChatCompletionText(responseBody: String?): String {
        val root = parseObject(responseBody) ?: return ""
        val choices = root.getJSONArray("choices") ?: return ""
        return choices.mapNotNull { choice ->
            val choiceObject = choice as? JSONObject
            val message = choiceObject?.getJSONObject("message")
            message?.getString("content")
                ?: choiceObject?.getJSONObject("delta")?.getString("content")
        }.joinToString("\n")
    }

    /** 提取 OpenAI Chat messages 中会进入 prompt 的主要文本内容。 */
    private fun StringBuilder.appendMessages(messages: JSONArray?) {
        messages?.forEach { item ->
            val message = item as? JSONObject ?: return@forEach
            append(message.getString("role").orEmpty()).append('\n')
            appendContent(message["content"])
            appendJsonField(message, "tool_calls")
            appendJsonField(message, "function_call")
            append('\n')
        }
    }

    /** 兼容 content 为字符串、数组、多模态对象的情况。 */
    private fun StringBuilder.appendContent(content: Any?) {
        when (content) {
            null -> return
            is String -> append(content).append('\n')
            is JSONArray -> content.forEach { appendContent(it) }
            is JSONObject -> {
                val type = content.getString("type").orEmpty()
                val text = content.getString("text")
                    ?: content.getJSONObject("input_text")?.getString("text")
                if (text != null) append(text).append('\n')
                else append(type).append(' ').append(JSON.toJSONString(content)).append('\n')
            }
            else -> append(content.toString()).append('\n')
        }
    }

    /** 将结构化字段稳定转为文本，便于后续替换为真实 tokenizer 时复用同一输入。 */
    private fun StringBuilder.appendJsonField(root: JSONObject, fieldName: String) {
        val value = root[fieldName] ?: return
        append(fieldName).append(':').append(JSON.toJSONString(value)).append('\n')
    }

    /** 解析 JSON 字符串，失败时返回 null，调用方按空文本兜底。 */
    private fun parseObject(body: String?): JSONObject? {
        if (body.isNullOrBlank()) return null
        return runCatching { JSON.parseObject(body) }
            .onFailure {
                log.warn("Token文本提取JSON解析失败 bodyLength={}, error={}", body.length, it.message)
            }.getOrNull()
    }
}
