package com.llm.gateway.tokencalc.text

import com.alibaba.fastjson2.JSON
import com.alibaba.fastjson2.JSONArray
import com.alibaba.fastjson2.JSONObject
import com.llm.gateway.common.logger

object TokenTextBuilder {

    private val log = logger()
    private const val IMAGE_TOKEN_COST = 256
    private const val AUDIO_TOKEN_COST = 128
    private const val FILE_TOKEN_COST = 64

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

    /** 构造 OpenAI Responses prompt 的可估算文本和多模态占位 Token。 */
    fun buildOpenAiResponsesPromptText(requestBody: String?): TokenTextExtractResult {
        val root = parseObject(requestBody) ?: return TokenTextExtractResult()
        return TokenTextExtractBuilder().apply {
            appendGenericContent(root["instructions"])
            if (root.containsKey("input")) appendGenericContent(root["input"])
            else addNote("input missing")
            appendJsonField(root, "tools")
            appendJsonField(root, "text")
        }.result()
    }

    /** 构造 OpenAI Responses completion 的可估算文本和多模态占位 Token。 */
    fun buildOpenAiResponsesCompletionText(responseBody: String?, stream: Boolean): TokenTextExtractResult {
        if (stream) return buildOpenAiResponsesStreamCompletionText(responseBody)
        val root = parseObject(responseBody) ?: return TokenTextExtractResult()
        return TokenTextExtractBuilder().apply {
            appendGenericContent(root["output"])
            appendGenericContent(root["output_text"])
            appendGenericContent(root["response"])
            if (text.isBlank() && extraTokens == 0) addNote("output missing")
        }.result()
    }

    /** 构造 Anthropic Messages prompt 的可估算文本和多模态占位 Token。 */
    fun buildAnthropicPromptText(requestBody: String?): TokenTextExtractResult {
        val root = parseObject(requestBody) ?: return TokenTextExtractResult()
        return TokenTextExtractBuilder().apply {
            appendGenericContent(root["system"])
            if (root.containsKey("messages")) appendGenericContent(root["messages"])
            else addNote("messages missing")
            appendJsonField(root, "tools")
        }.result()
    }

    /** 构造 Anthropic Messages completion 的可估算文本和多模态占位 Token。 */
    fun buildAnthropicCompletionText(responseBody: String?, stream: Boolean): TokenTextExtractResult {
        if (stream) return buildAnthropicStreamCompletionText(responseBody)
        val root = parseObject(responseBody) ?: return TokenTextExtractResult()
        return TokenTextExtractBuilder().apply {
            if (root.containsKey("content")) appendGenericContent(root["content"])
            else addNote("content missing")
        }.result()
    }

    /** 构造 Gemini Contents prompt 的可估算文本和多模态占位 Token。 */
    fun buildGeminiPromptText(requestBody: String?): TokenTextExtractResult {
        val root = parseObject(requestBody) ?: return TokenTextExtractResult()
        return TokenTextExtractBuilder().apply {
            appendGenericContent(root["systemInstruction"])
            if (root.containsKey("contents")) appendGenericContent(root["contents"])
            else addNote("contents missing")
            appendJsonField(root, "tools")
        }.result()
    }

    /** 构造 Gemini Contents completion 的可估算文本和多模态占位 Token。 */
    fun buildGeminiCompletionText(responseBody: String?, stream: Boolean): TokenTextExtractResult {
        if (stream) return buildGeminiStreamCompletionText(responseBody)
        val root = parseObject(responseBody) ?: return TokenTextExtractResult()
        return TokenTextExtractBuilder().apply {
            if (root.containsKey("candidates")) appendGenericContent(root["candidates"])
            else addNote("candidates missing")
        }.result()
    }

    /** 解析 responseBody 为 JSONObject；流式场景返回最后一个可解析事件对象。 */
    fun parseResponseObject(responseBody: String?, stream: Boolean): JSONObject? {
        if (!stream) return parseObject(responseBody)
        return parseStreamEvents(responseBody).lastOrNull()
    }

    /** 提取流式响应中的 JSON 事件，支持 SSE data 行和 JSONL。 */
    fun parseStreamEvents(responseBody: String?): List<JSONObject> {
        if (responseBody.isNullOrBlank()) return emptyList()
        return extractStreamEventPayloads(responseBody).mapNotNull { payload ->
            if (payload == "[DONE]") return@mapNotNull null
            runCatching { JSON.parseObject(payload) }
                .onFailure {
                    log.warn("Token流式事件JSON解析失败 payloadLength={}, error={}", payload.length, it.message)
                }.getOrNull()
        }
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

    /** 构造 OpenAI Responses 流式 completion 的可估算文本。 */
    private fun buildOpenAiResponsesStreamCompletionText(responseBody: String?): TokenTextExtractResult {
        return TokenTextExtractBuilder().apply {
            parseStreamEvents(responseBody).forEach { event ->
                when (event.getString("type").orEmpty().lowercase()) {
                    "response.output_text.delta" -> addText(event.firstString("delta", "text"))
                    "response.output_text.done" -> addText(event.firstString("text"))
                    "response.completed" -> appendGenericContent(event["response"])
                    else -> appendGenericContent(event)
                }
            }
            if (text.isBlank() && extraTokens == 0) addNote("stream contained no extractable responses deltas")
        }.result()
    }

    /** 构造 Anthropic Messages 流式 completion 的可估算文本。 */
    private fun buildAnthropicStreamCompletionText(responseBody: String?): TokenTextExtractResult {
        return TokenTextExtractBuilder().apply {
            parseStreamEvents(responseBody).forEach { event ->
                when (event.getString("type").orEmpty().lowercase()) {
                    "content_block_delta" -> appendGenericContent(event.getJSONObject("delta"))
                    "content_block_start" -> appendGenericContent(event.getJSONObject("content_block"))
                    else -> appendGenericContent(event)
                }
            }
            if (text.isBlank() && extraTokens == 0) addNote("stream contained no extractable anthropic deltas")
        }.result()
    }

    /** 构造 Gemini Contents 流式 completion 的可估算文本。 */
    private fun buildGeminiStreamCompletionText(responseBody: String?): TokenTextExtractResult {
        return TokenTextExtractBuilder().apply {
            parseStreamEvents(responseBody).forEach { appendGenericContent(it) }
            if (text.isBlank() && extraTokens == 0) addNote("stream contained no extractable gemini deltas")
        }.result()
    }

    /** 提取 SSE data 行或 JSONL 行中的事件 JSON 字符串。 */
    private fun extractStreamEventPayloads(body: String): List<String> {
        val lines = body.trim().lineSequence().map { it.trim() }.filter { it.isNotBlank() }.toList()
        if (lines.any { it.startsWith("data:") || it.startsWith("event:") }) {
            return lines.mapNotNull { line ->
                if (!line.startsWith("data:")) return@mapNotNull null
                line.removePrefix("data:").trim().takeIf { it.isNotBlank() }
            }
        }
        val jsonLines = lines.filter { it.startsWith("{") || it.startsWith("[") }
        return jsonLines.ifEmpty { listOf(body.trim()) }
    }

    /** 解析 JSON 字符串，失败时返回 null，调用方按空文本兜底。 */
    private fun parseObject(body: String?): JSONObject? {
        if (body.isNullOrBlank()) return null
        return runCatching { JSON.parseObject(body) }
            .onFailure {
                log.warn("Token文本提取JSON解析失败 bodyLength={}, error={}", body.length, it.message)
            }.getOrNull()
    }

    /** 提取对象中的第一个非空字符串字段。 */
    private fun JSONObject.firstString(vararg keys: String): String {
        return keys.firstNotNullOfOrNull { key -> getString(key)?.takeIf { it.isNotBlank() } }.orEmpty()
    }

    private class TokenTextExtractBuilder {
        private val builder = StringBuilder()
        private val notes = linkedSetOf<String>()
        private var imageTokens = 0
        private var audioTokens = 0
        private var fileTokens = 0

        val text: String
            get() = builder.toString()

        val extraTokens: Int
            get() = imageTokens + audioTokens + fileTokens

        /** 追加普通文本，自动过滤空白内容并按换行分隔片段。 */
        fun addText(value: String?) {
            val trimmed = value?.trim().orEmpty()
            if (trimmed.isBlank()) return
            if (builder.isNotEmpty()) builder.append('\n')
            builder.append(trimmed)
        }

        /** 追加结构化字段 JSON，保持工具定义等信息进入本地估算。 */
        fun appendJsonField(root: JSONObject, fieldName: String) {
            val value = root[fieldName] ?: return
            addText(JSON.toJSONString(value))
        }

        /** 递归展开任意协议内容节点。 */
        fun appendGenericContent(value: Any?) {
            when (value) {
                null -> return
                is String -> addText(value)
                is JSONArray -> value.forEach { appendGenericContent(it) }
                is JSONObject -> appendGenericMap(value)
                else -> addText(JSON.toJSONString(value))
            }
        }

        /** 追加说明，重复说明只保留一次。 */
        fun addNote(note: String) {
            note.trim().takeIf { it.isNotBlank() }?.let { notes.add(it) }
        }

        /** 返回本次文本提取结果。 */
        fun result(): TokenTextExtractResult {
            return TokenTextExtractResult(
                text = builder.toString(),
                imageTokens = imageTokens,
                audioTokens = audioTokens,
                fileTokens = fileTokens,
                note = notes.joinToString("; "),
            )
        }

        /** 按 Go tokencalc 的字段顺序递归展开 Map。 */
        private fun appendGenericMap(mapped: JSONObject) {
            var placeholderAdded = false
            when (mapped.getString("type").orEmpty().trim().lowercase()) {
                "image", "input_image", "image_url" -> {
                    addPlaceholder("image")
                    placeholderAdded = true
                }
                "audio", "input_audio" -> {
                    addPlaceholder("audio")
                    placeholderAdded = true
                }
                "file", "input_file" -> {
                    addPlaceholder("file")
                    placeholderAdded = true
                }
            }

            addText(mapped.firstString("text", "input_text", "output_text", "value", "delta"))
            listOf(
                "instructions",
                "message",
                "messages",
                "delta",
                "content",
                "content_block",
                "parts",
                "contents",
                "input",
                "output",
                "output_text",
                "response",
                "choices",
                "candidates",
                "candidate",
                "item",
                "tool_calls",
                "function_call",
                "function",
                "tool_call",
                "tool_result",
                "refusal",
                "arguments",
            ).forEach { key ->
                if (!mapped.containsKey(key)) return@forEach
                val value = mapped[key]
                if (key == "arguments") addText(value?.toString() ?: JSON.toJSONString(value))
                else appendGenericContent(value)
            }
            addText(mapped.firstString("name"))

            if (!placeholderAdded) addFieldPlaceholder(mapped)
        }

        /** 根据字段形态追加图片、音频、文件占位 Token。 */
        private fun addFieldPlaceholder(mapped: JSONObject) {
            when {
                mapped.containsKey("image_url") || mapped.containsKey("inlineData") || mapped.containsKey("inline_data") ->
                    addPlaceholder("image")
                mapped.containsKey("audio") -> addPlaceholder("audio")
                mapped.containsKey("fileData") || mapped.containsKey("file_data") -> addPlaceholder("file")
            }
        }

        /** 追加多模态占位 Token，并记录估算说明。 */
        private fun addPlaceholder(kind: String) {
            when (kind) {
                "image" -> {
                    imageTokens += IMAGE_TOKEN_COST
                    addNote("image parts counted by placeholder policy")
                }
                "audio" -> {
                    audioTokens += AUDIO_TOKEN_COST
                    addNote("audio parts counted by placeholder policy")
                }
                "file" -> {
                    fileTokens += FILE_TOKEN_COST
                    addNote("file parts counted by placeholder policy")
                }
            }
        }

        /** 提取对象中的第一个非空字符串字段。 */
        private fun JSONObject.firstString(vararg keys: String): String {
            return keys.firstNotNullOfOrNull { key -> getString(key)?.takeIf { it.isNotBlank() } }.orEmpty()
        }
    }
}

data class TokenTextExtractResult(
    val text: String = "",
    val imageTokens: Int = 0,
    val audioTokens: Int = 0,
    val fileTokens: Int = 0,
    val note: String = "",
) {
    val extraTokens: Int
        get() = imageTokens + audioTokens + fileTokens
}
