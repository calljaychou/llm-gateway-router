# Token 计算与 Token 金额计费技术实现方案

## 1. 背景与目标

- 模块一：Token 计算模块 `tokencalc`，负责把不同 LLM 协议的请求、响应和 usage 归一化为可审计的 Token 汇总与 Token 明细。
- 模块二：Token 金额计费模块 `billing`，负责基于 Token 明细和模型价格规则逐项计算金额，生成金额明细和金额快照。

两个模块不直接负责配额扣减、请求转发和三表落库。转发编排层只消费两个模块的结果，再交给配额和日志服务处理。

## 2. 总体架构

```text
OpenAiForwardFacade / 后续其他协议入口
  -> TokenCalcService
      -> TokenProviderEstimator
      -> TokenTextBuilder
      -> TokenUsageNormalizer
      -> TokenDetailBuilder
  -> TokenBillingService
      -> ModelPriceRuleResolver
      -> TokenChargeItemResolver
      -> TokenChargeItemCalculator
  -> UserQuotaUsageService
      -> prepare / settle / compensate
  -> UsageLogWriteService
      -> llm_usage_log
      -> llm_usage_token_detail
      -> llm_usage_billing_detail
```

模块依赖方向：

```text
forward/accounting
  -> tokencalc
  -> billing
  -> usage log

billing -> tokencalc.model
tokencalc 不依赖 billing / dal / quota / service
usage log 只消费 tokencalc 与 billing 的结果，不重新计算
```

核心原则：

- Token 计算和金额计费分离，计费必须基于 `TokenDetailDto`，不能只基于 `inputTokens/outputTokens` 汇总。
- 上游 usage 完整时优先使用上游 usage；上游 usage 缺失或不完整时，本地估算只作为兜底或补齐。
- 金额计算使用数据库价格规则快照，金额明细必须记录命中的价格规则或回退规则。
- 请求原文和响应原文不落入计费结果；日志只保存计算快照、字段来源、长度和明细。

## 3. 模块一：Token 计算模块

### 3.1 职责边界

Token 计算模块包路径建议保持：

```text
com.llm.gateway.tokencalc
├─ TokenCalcService.kt
├─ DefaultTokenCalcService.kt
├─ TokenUsageNormalizer.kt
├─ model/
│  ├─ TokenCalcDtos.kt
│  └─ TokenCalcEnums.kt
├─ provider/
│  ├─ TokenProviderEstimator.kt
│  └─ OpenAiChatEstimator.kt
└─ text/
   └─ TokenTextBuilder.kt
```

负责：

- 根据 `TokenProtocol` 选择协议估算器。
- 解析请求 prompt 文本和响应 completion 文本。
- 提取上游 `usage` 和 usage details。
- 处理 `REPORTED_USAGE`、`LOCAL_ESTIMATE`、`MERGED`、`UNSUPPORTED` 四类来源。
- 归一化 Token 汇总，修正负数和 `totalTokens`。
- 生成 `TokenDetailDto`，为后续计费提供最小计费输入。
- 生成 `calcDetail` 快照，便于审计和排查。

不负责：

- 不查询数据库。
- 不计算金额。
- 不预占或结算用户配额。
- 不写 `llm_usage_log`、`llm_usage_token_detail`、`llm_usage_billing_detail`。
- 不在 Service 层重复做 Controller 已通过 JSR-380 完成的基础参数校验。

### 3.2 计算流程

```text
TokenEstimateParams
  -> DefaultTokenCalcService 根据 protocol 选择 TokenProviderEstimator
  -> estimator 提取 promptText / completionText
  -> 提取显式 reportedUsage 或 response.usage
  -> 本地估算 estimatedUsage
  -> 判断 usage 来源
      完整上游 usage: REPORTED_USAGE
      无上游 usage: LOCAL_ESTIMATE
      上游 usage 不完整: MERGED
      无 estimator: UNSUPPORTED
  -> TokenUsageNormalizer.normalizeUsage / mergeUsage
  -> 构造 TokenDetailDto 列表
  -> 返回 TokenEstimateResult
```

usage 优先级：

1. `TokenEstimateParams.reportedUsage`
2. `responseBody.usage`
3. 本地估算结果

归一化规则：

- `inputTokens/outputTokens/totalTokens` 小于 0 时修正为 0。
- `totalTokens = 0` 且 `inputTokens + outputTokens > 0` 时，`totalTokens = inputTokens + outputTokens`。
- `totalTokens < inputTokens + outputTokens` 时，修正为两者之和。
- `MERGED` 场景下，上游字段为 0 或缺失时用本地估算补齐。

### 3.3 数据模型

入参：

```kotlin
data class TokenEstimateParams(
    val protocol: TokenProtocol,
    val requestModel: String? = null,
    val upstreamModel: String? = null,
    val stream: Boolean = false,
    val requestBody: String? = null,
    val responseBody: String? = null,
    val reportedUsage: TokenUsageSummaryDto? = null,
)
```

结果：

```kotlin
data class TokenEstimateResult(
    val usage: TokenUsageSummaryDto = TokenUsageSummaryDto(),
    val tokenDetails: List<TokenDetailDto> = emptyList(),
    val resolvedModel: String = "",
    val encoding: String = "",
    val source: TokenEstimateSource = TokenEstimateSource.UNSUPPORTED,
    val supported: Boolean = false,
    val note: String = "",
    val promptTextLen: Int = 0,
    val completionTextLen: Int = 0,
    val calcDetail: String = "",
)
```

汇总：

```kotlin
data class TokenUsageSummaryDto(
    val inputTokens: Int = 0,
    val outputTokens: Int = 0,
    val totalTokens: Int = 0,
)
```

明细：

```kotlin
data class TokenDetailDto(
    val direction: TokenDirection,
    val tokenType: TokenType,
    val cacheType: TokenCacheType = TokenCacheType.NONE,
    val tokens: Int,
    val billableTokens: Int = tokens,
    val source: TokenDetailSource,
    val providerField: String? = null,
    val note: String? = null,
)
```

关键枚举：

```text
TokenProtocol: OPENAI_CHAT / OPENAI_RESPONSES / ANTHROPIC_MESSAGES / GEMINI_CONTENTS
TokenDirection: INPUT / OUTPUT
TokenType: TEXT / AUDIO / IMAGE / FILE / REASONING / TOOL / PREDICTION / OTHER
TokenCacheType: NONE / CACHE_HIT / CACHE_MISS / CACHE_WRITE
TokenEstimateSource: REPORTED_USAGE / LOCAL_ESTIMATE / MERGED / UNSUPPORTED
TokenDetailSource: REPORTED / LOCAL / MERGED / DERIVED
```

### 3.4 OpenAI Chat 首版实现

首版优先覆盖当前网关实际使用的 `/v1/chat/completions`。

请求 prompt 提取：

- `messages`
- `system`
- `tools`
- `response_format`

响应 completion 提取：

- 非流式：`choices[*].message.content`
- 兼容顶层 `message`
- 流式后续独立实现，不和非流式首版混在一个提交中

usage 汇总字段：

- `usage.prompt_tokens` -> `inputTokens`
- `usage.completion_tokens` -> `outputTokens`
- `usage.total_tokens` -> `totalTokens`

usage details 字段：

- `prompt_tokens_details.cached_tokens`
- `prompt_cache_hit_tokens`
- `prompt_cache_miss_tokens`
- `prompt_tokens_details.audio_tokens`
- `completion_tokens_details.reasoning_tokens`
- `completion_tokens_details.audio_tokens`
- `completion_tokens_details.accepted_prediction_tokens`
- `completion_tokens_details.rejected_prediction_tokens`

cache miss 推导规则：

```text
如果上游存在 prompt_cache_miss_tokens:
  INPUT/TEXT/CACHE_MISS = prompt_cache_miss_tokens, source = REPORTED
否则:
  INPUT/TEXT/CACHE_MISS = prompt_tokens - cached_tokens - audio_tokens, source = DERIVED
```

本地估算策略：

- 当前可先使用轻量估算，例如 `ceil(text.length / 4.0)`。
- 后续应通过 `TokenCounter` 接口替换为真实 tokenizer。
- 本地估算只作为 usage 缺失或不完整时的兜底，不覆盖完整上游 usage。

### 3.5 边界场景

- `responseBody` 不是合法 JSON：返回本地估算结果，`note` 记录解析失败原因摘要，日志不打印原文。
- usage 字段为负数：归一化为 0。
- 上游只返回 `total_tokens`：使用本地估算补齐输入和输出，来源为 `MERGED`。
- 上游只返回输入或输出：缺失字段用本地估算补齐，来源为 `MERGED`。
- 未知模型：使用默认 encoding，`note` 或 `calcDetail` 标记回退。
- 多模态内容：不读取外部文件，只按占位 Token 策略估算，后续通过配置控制。
- 流式响应：首版不在非流式实现中混入，后续通过 `StreamingTokenCounter` 独立支持。

### 3.6 验收标准

- 响应带完整 usage 时，`source = REPORTED_USAGE`。
- 响应不带 usage 时，`source = LOCAL_ESTIMATE`。
- usage 不完整时，`source = MERGED`。
- `totalTokens >= inputTokens + outputTokens`。
- `tokenDetails` 能拆出缓存命中、缓存未命中、音频、推理、预测输出等明细。
- 模块不依赖数据库、配额和计费服务。
- 单元测试覆盖 normalizer、OpenAI Chat usage 提取、usage merge、cache miss 推导。

## 4. 模块二：Token 金额计费模块

### 4.1 职责边界

计费模块包路径建议保持：

```text
com.llm.gateway.billing
├─ TokenBillingService.kt
├─ DefaultTokenBillingService.kt
├─ TokenBillingDtos.kt
├─ ModelPriceRuleResolver.kt
└─ TokenChargeItemResolver.kt
```

负责：

- 从 `model_price_rule` 读取模型价格规则。
- 将 `TokenDetailDto` 映射为 `TokenChargeItem`。
- 按百万 Token 单价计算每个计费项金额。
- 生成 `TokenBillingDetailDto` 和 `TokenBillingResult`。
- 记录价格规则快照或回退规则，支持审计与复算。

不负责：

- 不解析 request/response。
- 不估算 Token。
- 不修改 Token 明细来源。
- 不执行配额扣减、退款或补偿。
- 不写日志三表。
- 不做展示层金额格式化。

### 4.2 计费流程

```text
TokenBillingParams
  -> normalizeDetails
  -> ModelPriceRuleResolver.resolve(vendorId, modelId, currency)
  -> TokenChargeItemResolver.resolve(detail)
  -> resolvePriceRule(chargeItem)
      命中直接价格
      缺失则按回退规则
      仍缺失则按 0 元并记录 missing
  -> amount = billableTokens * priceCnyPerMillion / 1_000_000
  -> 汇总 amountCny
  -> 汇总 billableInputTokens / billableOutputTokens
  -> 返回 TokenBillingResult
```

金额精度：

- 数据库存储：`DECIMAL(18, 8)`。
- 服务计算：统一 `setScale(8, RoundingMode.HALF_UP)`。
- 展示格式化由前端或查询接口处理，不在计费核心中做截断展示。

### 4.3 数据模型

入参：

```kotlin
data class TokenBillingParams(
    val vendorId: Long,
    val modelId: Long,
    val tokenDetails: List<TokenDetailDto>,
    val billingStrategy: String = TokenBillingStrategy.MODEL_PRICE_RULE,
    val currency: String = "CNY",
)
```

结果：

```kotlin
data class TokenBillingResult(
    val amountCny: BigDecimal,
    val billingStrategy: String,
    val currency: String,
    val billableInputTokens: Int,
    val billableOutputTokens: Int,
    val billingDetails: List<TokenBillingDetailDto>,
    val billingDetailJson: String,
)
```

金额明细：

```kotlin
data class TokenBillingDetailDto(
    val chargeItem: TokenChargeItem,
    val direction: TokenDirection,
    val tokenType: TokenType,
    val cacheType: TokenCacheType,
    val tokens: Int,
    val priceCnyPerMillion: BigDecimal,
    val amountCny: BigDecimal,
    val pricingRule: String?,
)
```

价格规则快照：

```kotlin
data class ModelPriceRuleSnapshot(
    val vendorId: Long,
    val modelId: Long,
    val currency: String,
    val rules: Map<TokenChargeItem, ModelPriceRuleItem>,
)
```

计费项：

```text
INPUT_TEXT
INPUT_CACHE_HIT
INPUT_CACHE_MISS
INPUT_CACHE_WRITE
INPUT_AUDIO
INPUT_IMAGE
OUTPUT_TEXT
OUTPUT_REASONING
OUTPUT_AUDIO
OUTPUT_ACCEPTED_PREDICTION
OUTPUT_REJECTED_PREDICTION
```

### 4.4 计费项映射规则

输入方向：

```text
INPUT + AUDIO -> INPUT_AUDIO
INPUT + IMAGE -> INPUT_IMAGE
INPUT + CACHE_HIT -> INPUT_CACHE_HIT
INPUT + CACHE_MISS -> INPUT_CACHE_MISS
INPUT + CACHE_WRITE -> INPUT_CACHE_WRITE
其他 INPUT -> INPUT_TEXT
```

输出方向：

```text
OUTPUT + REASONING -> OUTPUT_REASONING
OUTPUT + AUDIO -> OUTPUT_AUDIO
OUTPUT + PREDICTION + accepted 标记 -> OUTPUT_ACCEPTED_PREDICTION
OUTPUT + PREDICTION + rejected 标记 -> OUTPUT_REJECTED_PREDICTION
其他 OUTPUT -> OUTPUT_TEXT
```

prediction 标记来源：

- 优先通过 `TokenDetailDto.note` 判断 `accepted/rejected`。
- 其次通过 `providerField` 判断原始字段名。
- 无法识别时回退为 `OUTPUT_TEXT`。

### 4.5 价格规则与回退策略

价格表：

```sql
model_price_rule(
    id,
    model_id,
    vendor_id,
    charge_item,
    price_cny_per_million,
    currency,
    active,
    created_at,
    updated_at
)
```

查询条件：

```text
vendor_id = params.vendorId
model_id = params.modelId
active = true
```

首版回退规则：

```text
INPUT_CACHE_MISS -> INPUT_TEXT
INPUT_CACHE_HIT -> INPUT_TEXT
INPUT_CACHE_WRITE -> INPUT_TEXT
OUTPUT_REASONING -> OUTPUT_TEXT
OUTPUT_ACCEPTED_PREDICTION -> OUTPUT_TEXT
OUTPUT_REJECTED_PREDICTION -> OUTPUT_TEXT
```

缺失价格处理：

- 有直接价格：`pricingRule = model_price_rule:{id}:{chargeItem}`。
- 命中回退价格：`pricingRule = fallback:{from}->{to}:{originRule}`。
- 无直接价格且无回退价格：按 0 元计费，`pricingRule = missing:{chargeItem}`，并打印 `warn`。

是否按 0 元处理需要后续结合运营策略决定。若希望避免漏配价格导致少计费，可增加配置项：

```text
billing.price-missing-policy = ZERO | THROW
```

首版建议默认 `ZERO`，避免影响线上请求；管理端和巡检脚本补充价格规则缺失告警。

### 4.6 边界场景

- `billableTokens <= 0`：不生成金额明细。
- 价格为负数：按 0 处理。
- 价格规则存在未知 `charge_item`：忽略该规则并打印 `warn`。
- 同一计费项多条 Token 明细：可以分别生成多条金额明细，后续查询层再聚合。
- 币种非 `CNY`：当前字段保留 `currency`，但金额字段仍以 `amountCny` 为准；多币种折算不在首版范围。
- `modelId` 为空：计费模块不应自行猜测模型，应由编排层决定是否跳过计费、返回失败或使用默认模型价格。

### 4.7 验收标准

- 同一次请求可拆出多个计费项。
- `amountCny = SUM(billingDetails.amountCny)`。
- `billableInputTokens = SUM(INPUT detail.billableTokens)`。
- `billableOutputTokens = SUM(OUTPUT detail.billableTokens)`。
- 缓存命中、缓存未命中、推理 Token 能按独立计费项计算。
- 价格缺失时能按明确规则回退，并在 `pricingRule` 中可追踪。
- 单元测试覆盖计费项映射、价格回退、金额精度、0 Token 跳过。

## 5. 两模块集成方式

非流式成功响应：

```kotlin
val tokenEstimate = tokenCalcService.estimate(
    TokenEstimateParams(
        protocol = TokenProtocol.OPENAI_CHAT,
        requestModel = context.modelAlias,
        upstreamModel = context.upstreamModel,
        stream = false,
        requestBody = JSON.toJSONString(payload),
        responseBody = JSON.toJSONString(upstreamResponse.body),
    )
)

val billing = tokenBillingService.calculate(
    TokenBillingParams(
        vendorId = context.vendorId,
        modelId = context.modelId,
        tokenDetails = tokenEstimate.tokenDetails,
    )
)
```

后续由编排层处理：

```text
tokenEstimate + billing
  -> UserQuotaUsageService.settle(...)
  -> UsageLogWriteService.record(...)
```

失败响应：

- 如果没有上游响应或响应不可解析，可写入 `UNSUPPORTED` 或空 Token 结果。
- 金额按 0 处理。
- `UsageLogWriteService` 仍写主日志，用 `accountingStatus` 和 `errorCode` 标记失败。

预占金额：

- 请求转发前只计算 prompt 侧 Token。
- completion 预估使用 `max_tokens`、`max_completion_tokens` 或系统默认值。
- 构造预估 Token 明细后调用 `TokenBillingService.calculate()` 计算预占金额。
- 缓存命中无法提前确认时，按 `INPUT_TEXT` 或 `INPUT_CACHE_MISS` 保守预占。

## 6. 实施顺序

建议按以下顺序推进，避免局部改造破坏现有配额和转发流程：

1. 完成 `tokencalc` 基础模型、枚举和 `TokenUsageNormalizer`。
2. 完成 OpenAI Chat 非流式 Token 计算和单元测试。
3. 完成 `billing` DTO、计费项映射、价格规则读取和金额计算。
4. 完成计费模块单元测试。
5. 在非流式成功响应链路中接入 `TokenCalcService` 与 `TokenBillingService`。
6. 将结果交给 `UsageLogWriteService` 写新日志表。
7. 改造预占金额逻辑。
8. 独立迭代流式 Token 统计和流式结算。
9. 扩展 OpenAI Responses、Anthropic Messages、Gemini Contents。

