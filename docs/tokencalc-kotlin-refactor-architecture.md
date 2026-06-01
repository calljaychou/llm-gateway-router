# tokencalc Kotlin 重构架构与实现文档

## 1. 背景与目标

本文基于 `https://github.com/xy200303/tokencalc` 当前 Go 实现整理，用于后续在 `llm-gateway` 中使用 Kotlin/Spring 重构 token 计算与统计能力。

`tokencalc` 的职责边界非常明确：只负责不同 LLM 协议下的 token 统计、usage 提取、usage 归一化、本地兜底估算和流式聚合，不负责网关转发、计费、落库、鉴权、限流或供应商路由。

结合当前 `llm-gateway` 项目，建议 Kotlin 重构后的模块定位为网关内部的 token 计算基础能力，由转发结算流程调用，并逐步替换当前 `OpenAiForwardFacade.extractTokenUsage()` 和 `estimateReserveTokens()` 中较简单的 usage 处理逻辑。

## 2. Go 项目能力总览

### 2.1 支持协议

- `openai_chat`: OpenAI Chat Completions 协议。
- `openai_responses`: OpenAI Responses API 协议。
- `anthropic_messages`: Anthropic Messages 协议。
- `gemini_contents`: Gemini Contents 协议。

### 2.2 核心能力

- 根据模型名解析 tokenizer encoding。
- 从请求体或响应体自动识别模型。
- 从响应体或流式事件中提取上游 usage。
- usage 完整时直接使用上游 usage。
- usage 缺失或不完整时，使用本地文本估算补齐。
- 对 prompt 和 completion 分别统计 token。
- 支持多模态内容的占位 token 策略。
- 支持 SSE/JSONL 流式响应聚合。
- 支持批量 `CountTexts`、`EstimateBatch`。
- 支持自定义协议 estimator 和自定义 stream collector 注册。

## 3. 总体架构

Go 项目采用“对外 Service + 内部协议解析 + tokenizer 计数 + 流式聚合”的分层结构。

```text
tokencalc
├─ Service / BatchService              对外统一入口
├─ Registry                            自定义协议与流式收集器注册
├─ Model Resolver                      模型名到 encoding 的映射
├─ Codec                               文本 token 计数
│  └─ TiktokenCodec                    基于 tiktoken-go
├─ Provider Estimator                  协议解析与 usage 提取
│  ├─ OpenAIChat
│  ├─ OpenAIResponses
│  ├─ Anthropic
│  └─ Gemini
├─ Text Builder                        文本扁平化、多模态占位、备注聚合
└─ Stream
   ├─ StreamCollector                  收集原始流式响应
   ├─ StreamingCounter                 增量计算累计 usage 与 delta
   └─ StreamAccumulator                协议级流式事件解析
```

建议 Kotlin 重构时保持该分层，不要把协议 JSON 解析逻辑散落到 `OpenAiForwardFacade` 或计费服务中。

## 4. 核心计算流程

### 4.1 非流式 Estimate 流程

```text
EstimateRequest
  -> 根据协议获取 Estimator
  -> 解析模型名
     1. UpstreamModel
     2. RequestModel
     3. 请求体 model 字段
     4. 响应体 model 字段
  -> 根据模型解析 encoding
  -> 读取调用方传入 ReportedUsage
  -> 若 usage 完整，直接返回 reported_usage
  -> 若协议不支持：
     - 有 usage 则原样返回
     - 无 usage 则返回 unsupported
  -> 从响应体提取上游 usage
  -> 若 usage 完整，直接返回 reported_usage
  -> 提取 prompt 文本
  -> 提取 completion 文本
  -> 使用 tokenizer 计算本地 usage
  -> 合并 reported usage 与 local estimate
  -> NormalizeUsage
  -> 返回 EstimateResult
```

### 4.2 usage 来源优先级

usage 来源由高到低：

1. 调用方显式传入的 `ReportedUsage`。
2. 响应体或流式事件中的上游 usage。
3. 本地根据请求和响应文本估算。

如果上游 usage 完整，即 `prompt_tokens`、`completion_tokens`、`total_tokens` 都大于 0，则直接使用。若只缺少部分字段，则使用本地估算补齐缺失字段，并将来源标记为 `merged`。

### 4.3 usage 归一化规则

Go 实现中的 `NormalizeUsage` 规则如下：

- 负数 token 统一修正为 0。
- 当 `total_tokens = 0` 且 prompt + completion 大于 0 时，`total_tokens = prompt + completion`。
- 当 `total_tokens < prompt + completion` 时，将 `total_tokens` 修正为两者之和。

合并规则 `MergeUsage(reported, estimated)`：

- 先分别归一化 reported 和 estimated。
- reported 中为 0 的字段使用 estimated 补齐。
- 最后再次归一化。

## 5. 核心数据结构

Kotlin 重构建议使用 data class 和 enum class 表达同等语义。

```kotlin
enum class TokenProtocol(val value: String) {
    OPENAI_CHAT("openai_chat"),
    OPENAI_RESPONSES("openai_responses"),
    ANTHROPIC_MESSAGES("anthropic_messages"),
    GEMINI_CONTENTS("gemini_contents"),
}

data class TokenUsageDto(
    val promptTokens: Int = 0,
    val completionTokens: Int = 0,
    val totalTokens: Int = 0,
    val promptCachedTokens: Int = 0,
    val promptCacheMissTokens: Int = 0,
    val promptAudioTokens: Int = 0,
    val completionReasoningTokens: Int = 0,
    val completionAudioTokens: Int = 0,
    val completionAcceptedPredictionTokens: Int = 0,
    val completionRejectedPredictionTokens: Int = 0,
)

enum class TokenEstimateSource(val value: String) {
    REPORTED_USAGE("reported_usage"),
    LOCAL_ESTIMATE("local_estimate"),
    MERGED("merged"),
    UNSUPPORTED("unsupported"),
}

data class TokenEstimateParams(
    val protocol: TokenProtocol,
    val requestModel: String? = null,
    val upstreamModel: String? = null,
    val stream: Boolean = false,
    val requestBody: String? = null,
    val responseBody: String? = null,
    val reportedUsage: TokenUsageDto? = null,
)

data class TokenEstimateResult(
    val usage: TokenUsageDto = TokenUsageDto(),
    val resolvedModel: String = "",
    val source: TokenEstimateSource = TokenEstimateSource.UNSUPPORTED,
    val encoding: String = "",
    val supported: Boolean = false,
    val note: String = "",
    val promptTextLen: Int = 0,
    val completionTextLen: Int = 0,
)
```

当前项目已有 `TokenUsageDto` 和 `TokenCalcSource`。重构时不建议重复创建业务日志 DTO，可以新增 token 计算模块内部的 `TokenEstimateSource`，最终落库时再映射到现有 `TokenCalcSource.UPSTREAM`、`TokenCalcSource.LOCAL_ESTIMATE`、`TokenCalcSource.STREAM`。

## 6. 模块设计建议

建议在当前项目中新增包：

```text
com.llm.gateway.tokencalc
├─ TokenCalcService.kt
├─ DefaultTokenCalcService.kt
├─ TokenUsageNormalizer.kt
├─ TokenModelResolver.kt
├─ TokenPlaceholderPolicy.kt
├─ codec/
│  ├─ TokenCounter.kt
│  └─ ApproxTokenCounter.kt
├─ provider/
│  ├─ TokenProviderEstimator.kt
│  ├─ BaseProviderSupport.kt
│  ├─ OpenAiChatEstimator.kt
│  ├─ OpenAiResponsesEstimator.kt
│  ├─ AnthropicMessagesEstimator.kt
│  └─ GeminiContentsEstimator.kt
├─ stream/
│  ├─ StreamCollector.kt
│  ├─ SseEventParser.kt
│  ├─ StreamingTokenCounter.kt
│  └─ StreamAccumulator.kt
└─ text/
   └─ TokenTextBuilder.kt
```

### 6.1 `TokenCalcService`

职责：

- 对外提供 `estimate()`、`countText()`。
- 根据协议路由到对应 estimator。
- 控制 reported usage、本地估算、合并和归一化逻辑。
- 返回统一 `TokenEstimateResult`。

接口示例：

```kotlin
interface TokenCalcService {
    fun estimate(params: TokenEstimateParams): TokenEstimateResult

    fun countText(model: String?, text: String): Pair<Int, String>
}
```

### 6.2 `TokenProviderEstimator`

职责只放在协议解析，不负责计费、落库或用户业务。

```kotlin
interface TokenProviderEstimator {
    fun extractPrompt(requestBody: String?): ExtractTokenTextResult

    fun extractCompletion(responseBody: String?, stream: Boolean): ExtractTokenTextResult

    fun extractReportedUsage(responseBody: String?, stream: Boolean): ReportedUsageResult

    fun extractRequestModel(requestBody: String?): String?

    fun extractResponseModel(responseBody: String?, stream: Boolean): String?
}

data class ExtractTokenTextResult(
    val text: String = "",
    val extraTokens: Int = 0,
    val supported: Boolean = true,
    val note: String = "",
)

data class ReportedUsageResult(
    val usage: TokenUsageDto = TokenUsageDto(),
    val note: String = "",
)
```

### 6.3 `TokenCounter`

Go 版本使用 `github.com/pkoukk/tiktoken-go`。Kotlin 侧需要选择 tokenizer 实现：

- 优先方案：引入 JVM 可用的 tiktoken 实现，保证 `o200k_base`、`cl100k_base` 可用。
- 过渡方案：先实现 `ApproxTokenCounter`，使用字符/词粒度估算，用于额度预冻结，不作为最终精确结算来源。
- 长期方案：将 tokenizer 封装为接口，允许后续替换为更准确实现，不影响 provider 和 service。

接口示例：

```kotlin
interface TokenCounter {
    fun count(encoding: String, text: String): Int
}
```

## 7. 模型到 encoding 的映射

Go 项目当前规则：

- 精确匹配：
  - `gpt-4o`、`gpt-4o-mini`、`gpt-4.1`、`gpt-4.1-mini`、`gpt-4.1-nano`
  - `gpt-5`、`gpt-5-mini`、`gpt-5-nano`
  - `o1`、`o1-mini`、`o3`、`o3-mini`、`o4-mini`
  - `chatgpt-4o-latest`
  - 均映射到 `o200k_base`
- 前缀匹配：
  - `gpt-5`、`gpt-4o`、`gpt-4.1`、`o1`、`o3`、`o4`、`qwen` -> `o200k_base`
  - `gpt-4`、`gpt-3.5`、`text-embedding-3`、`claude`、`gemini` -> `cl100k_base`
- 默认值：`cl100k_base`

Kotlin 侧建议用对象常量维护表驱动规则：

```kotlin
object TokenModelResolver {
    private const val DEFAULT_ENCODING = "cl100k_base"

    private val exactModels = mapOf(
        "gpt-4o" to "o200k_base",
        "gpt-4o-mini" to "o200k_base",
        "gpt-4.1" to "o200k_base",
        "gpt-5" to "o200k_base",
        "o1" to "o200k_base",
        "o3" to "o200k_base",
        "o4-mini" to "o200k_base",
    )

    private val prefixModels = listOf(
        "gpt-5" to "o200k_base",
        "gpt-4o" to "o200k_base",
        "qwen" to "o200k_base",
        "gpt-4" to "cl100k_base",
        "gpt-3.5" to "cl100k_base",
        "claude" to "cl100k_base",
        "gemini" to "cl100k_base",
    )

    fun resolve(model: String?): String {
        val normalized = model?.trim()?.lowercase().orEmpty()
        if (normalized.isBlank()) return DEFAULT_ENCODING
        exactModels[normalized]?.let { return it }
        return prefixModels.firstOrNull { normalized.startsWith(it.first) }?.second ?: DEFAULT_ENCODING
    }
}
```

## 8. 协议解析规则

### 8.1 通用文本扁平化

Go 实现通过 `text.Builder` 将复杂 JSON 内容统一展开为文本：

- 字符串直接加入文本。
- 数组递归处理每个元素。
- Map 根据 `type`、`text`、`content`、`messages`、`parts`、`tool_calls` 等字段递归提取。
- `tools`、`response_format`、`text` 配置等结构化字段按 JSON 字符串加入。
- 多模态内容不读取真实文件，只按占位策略增加 token：
  - image: 256
  - audio: 128
  - file: 64

Kotlin 侧建议基于 `fastjson2` 的 `JSONObject`、`JSONArray` 实现递归处理，保持与当前项目 JSON 技术栈一致。

### 8.2 OpenAI Chat

请求 prompt 提取：

- `messages`
- `system`
- `tools` 按 JSON
- `response_format` 按 JSON

响应 completion 提取：

- 非流式：`choices`，兼容顶层 `message`
- 流式：逐个 SSE event 中的 `choices`

usage 提取：

- `usage.prompt_tokens`
- `usage.completion_tokens`
- `usage.total_tokens`

### 8.3 OpenAI Responses

请求 prompt 提取：

- `instructions`
- `input`
- `tools` 按 JSON
- `text` 配置按 JSON

响应 completion 提取：

- `output`
- `output_text`
- `response`

流式事件：

- `response.output_text.delta`: 读取 `delta` 或 `text`
- `response.output_text.done`: 读取 `text`
- `response.completed`: 读取 `response`
- 其他事件走通用文本扁平化

usage 提取：

- 优先 `usage.input_tokens`、`usage.output_tokens`、`usage.total_tokens`
- 兼容 `prompt_tokens`、`completion_tokens`

### 8.4 Anthropic Messages

请求 prompt 提取：

- `system`
- `messages`
- `tools` 按 JSON

响应 completion 提取：

- 非流式：`content`
- 流式：
  - `content_block_delta`: 读取 `delta`
  - `content_block_start`: 读取 `content_block`
  - 其他事件走通用文本扁平化

usage 提取：

- `usage.input_tokens`
- `usage.output_tokens`
- `usage.total_tokens`

### 8.5 Gemini Contents

请求 prompt 提取：

- `systemInstruction`
- `contents`
- `tools` 按 JSON

响应 completion 提取：

- 非流式：`candidates`
- 流式：事件整体走通用文本扁平化

usage 提取：

- `usageMetadata.promptTokenCount`
- `usageMetadata.candidatesTokenCount`
- `usageMetadata.totalTokenCount`

## 9. 流式统计设计

Go 项目有两种流式能力：

1. `StreamCollector`: 简单收集所有 chunk，最终拼成 `ResponseBody` 后调用 `Estimate`。
2. `StreamingCounter`: 每次 `AddChunk` 后增量计算当前累计 usage 和本次 delta。

当前 `llm-gateway` 的 `OpenAiForwardService.forwardStream()` 只负责透传 SSE，没有落 usage，也没有结算流式请求。若后续要支持流式结算，建议引入 `StreamingTokenCounter`：

```kotlin
interface StreamingTokenCounter {
    fun addChunk(chunk: String): StreamEstimateUpdate

    fun finalResult(): StreamEstimateUpdate

    fun finalBody(): String
}

data class StreamEstimateUpdate(
    val result: TokenEstimateResult = TokenEstimateResult(),
    val delta: TokenUsageDto = TokenUsageDto(),
    val updated: Boolean = false,
)
```

事件解析规则：

- 支持标准 SSE 行：`data: {...}`。
- 忽略 `event:` 行。
- 忽略 `data: [DONE]`。
- 支持 JSONL：每行以 `{` 或 `[` 开头即作为事件。
- 非 final 阶段遇到不完整 JSON 不报错，等待下一 chunk。
- final 阶段仍不完整则返回错误，便于记录异常流。

## 10. 与当前 llm-gateway 的集成方案

### 10.1 非流式请求

当前成功结算逻辑在 `OpenAiForwardFacade.settleAndRecordSuccess()` 中：

```kotlin
val upstreamUsage = extractTokenUsage(upstreamResponse.body)
val usage = upstreamUsage ?: TokenUsageDto(...)
```

建议替换为：

```kotlin
val estimateResult = tokenCalcService.estimate(
    TokenEstimateParams(
        protocol = TokenProtocol.OPENAI_CHAT,
        requestModel = context.modelAlias,
        upstreamModel = context.payload["model"]?.toString(),
        stream = false,
        requestBody = JSON.toJSONString(payload),
        responseBody = JSON.toJSONString(upstreamResponse.body),
    )
)
val usage = estimateResult.usage
```

落库 `calcSource` 映射建议：

- `REPORTED_USAGE` -> `TokenCalcSource.UPSTREAM.value`
- `LOCAL_ESTIMATE` -> `TokenCalcSource.LOCAL_ESTIMATE.value`
- `MERGED` -> `TokenCalcSource.LOCAL_ESTIMATE.value`，同时建议在 `amountCalcDetail` 或后续新增字段记录 `merged`
- 流式场景 -> `TokenCalcSource.STREAM.value`

### 10.2 预冻结额度

当前预冻结使用：

```kotlin
private fun estimateReserveTokens(payload: Map<String, Any?>): Long {
    val maxTokens = payload["max_tokens"]?.toString()?.toLongOrNull()
    return maxTokens?.coerceAtLeast(1L) ?: 1024L
}
```

建议后续改为：

- prompt token 使用 `tokenCalcService.estimate()` 只传 requestBody 得到。
- completion 预估仍用 `max_tokens`、`max_completion_tokens` 或默认值。
- reservedTokens = promptTokens + completionReserveTokens。

这样能避免长 prompt 但 `max_tokens` 较小导致预冻结不足。

### 10.3 流式请求

建议后续将 `forwardStream()` 从纯转发调整为“转发 + 旁路统计”：

- 创建 `StreamingTokenCounter`。
- 每收到上游 chunk：
  - 先发送给客户端。
  - 同步喂给 counter。
- 完成时调用 `finalResult()`。
- 使用最终 usage 结算并记录 usage log。
- 若异常则按当前补偿逻辑退款并记录失败。

该改造会影响响应生命周期、配额冻结和异常补偿，建议作为独立迭代，不要和非流式 token 重构混在一次提交里。

## 11. Kotlin 实现顺序建议

1. 新增 token 计算基础 DTO、枚举和 `TokenUsageNormalizer`。
2. 新增 `TokenModelResolver` 和 `TokenCounter` 接口。
3. 先实现 `ApproxTokenCounter`，打通调用链；随后替换为真实 tokenizer。
4. 实现 `TokenTextBuilder` 和通用 JSON 扁平化逻辑。
5. 实现 `OpenAiChatEstimator`，优先覆盖当前网关实际使用的 `/v1/chat/completions`。
6. 实现 `DefaultTokenCalcService.estimate()`，完成 usage 优先级、合并、归一化。
7. 将非流式 `settleAndRecordSuccess()` 接入 `TokenCalcService`。
8. 补 OpenAI Chat 回归测试。
9. 再逐步扩展 OpenAI Responses、Anthropic、Gemini。
10. 最后实现 `StreamingTokenCounter` 和流式结算。

## 12. 测试用例清单

建议先复制 Go 项目的 `testdata` 思路，在 Kotlin 测试资源中维护协议样本。

核心用例：

- OpenAI Chat 请求 + 响应无 usage，本地估算 prompt/completion。
- OpenAI Chat 响应带完整 usage，直接使用上游 usage。
- 调用方传入不完整 usage，仅有 promptTokens，本地估算 completionTokens 后合并。
- 请求体没有 model，响应体有 model，能自动识别。
- 未知 model 回退到默认 encoding。
- 负数 usage 被归零。
- totalTokens 小于 prompt + completion 时自动修正。
- 多模态 image/audio/file 按占位 token 累加。
- SSE `data: {...}` 能提取 completion。
- SSE 最后一段带 usage 时切换为上游 usage。
- 不支持协议且无 usage 返回 unsupported。

## 13. 关键注意事项

- Service 层不要重复做 JSR-380 入参基础校验，只保留业务处理和容错。
- provider 只解析协议，不要引用计费、用户、数据库或网关转发类。
- token 计算模块应保持无数据库依赖，便于单元测试。
- 对于 unknown model，必须可解释地回退默认 encoding，并在 `note` 中体现。
- 多模态 token 是估算策略，不是精确值，需要通过配置项控制。
- `MERGED` 不等于纯上游 usage，日志中应保留来源说明，方便排查账单差异。
- 流式结算需要考虑客户端断开、上游异常、已发送但未结算等边界，建议独立设计补偿流程。

## 14. 与 Go 实现的主要差异建议

- Go 版本是独立库；Kotlin 版本建议作为 `llm-gateway` 内部 Spring Bean。
- Go 版本使用 `[]byte`；Kotlin 项目可统一使用 `String` + `fastjson2`。
- Go 版本 tokenizer 已接入 tiktoken；Kotlin 初期可以接口化，先用近似估算打通，后续替换真实 tokenizer。
- 当前项目已有更丰富的 `TokenUsageDto` 明细字段，Kotlin 版本应保留这些字段，OpenAI Chat 上游 usage 明细仍由解析器填充。
- 当前项目已有 `TokenCalcSource`，不建议直接替换为 Go 的 source 枚举，应在结算落库时做映射。
