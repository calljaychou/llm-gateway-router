# Token 计算、计费与落库改造任务书

## 1. 改造目标

本任务书用于指导后续 `llm-gateway` 中 Token 计算、Token 计费和用量落库的系统性改造。后续开发必须严格按本文顺序推进，每个阶段完成并验证后，再进入下一阶段。

本次改造不以当前 `usage_logs` 表为中心做局部修补，而是重新建立一套面向 LLM 调用计量的结构：

- Token 计算负责解析不同协议的请求、响应、流式事件，生成可解释的 Token 结果。
- Token 计费负责根据 Token 明细和模型价格规则逐项计算金额。
- Token 落库负责保存调用主日志、Token 明细、金额明细，支持审计、统计、对账和问题排查。

## 2. 总体架构

```text
OpenAiForwardFacade / 后续其他协议入口
  -> TokenCalcService
      -> ProviderEstimator
      -> TokenCounter
      -> TokenUsageNormalizer
      -> TokenDetailBuilder
  -> TokenBillingService
      -> ModelPriceRuleResolver
      -> TokenChargeItemCalculator
  -> UsageAccountingService
      -> 配额预占
      -> 上游转发
      -> 配额结算/补偿
      -> UsageLogWriteService
          -> llm_usage_log
          -> llm_usage_token_detail
          -> llm_usage_billing_detail
```

模块边界：

- `tokencalc`: 只负责 Token 计算，不引用数据库、配额、计费服务。
- `billing`: 只负责金额计算，不解析上游协议。
- `usage log`: 只负责落库，不重新计算 Token 或金额。
- `forward/accounting`: 编排请求生命周期、配额预占、上游转发、结算和补偿。

## 3. 数据库改造设计

### 3.1 调用主日志表 `llm_usage_log`

保存一次 LLM 调用的业务上下文、最终 Token 汇总、最终金额和状态。

```sql
CREATE TABLE llm_usage_log (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键',
    request_id VARCHAR(64) NOT NULL COMMENT '请求ID',

    user_id BIGINT NOT NULL COMMENT '用户ID',
    dept_id BIGINT NULL COMMENT '部门ID',
    api_key_id BIGINT NULL COMMENT '虚拟Key ID',
    vendor_id BIGINT NOT NULL COMMENT '供应商ID',
    model_id BIGINT NULL COMMENT '模型ID',

    endpoint VARCHAR(64) NOT NULL COMMENT '接口类型: chat.completions/responses/messages',
    token_protocol VARCHAR(64) NOT NULL COMMENT 'Token协议',
    use_stream TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否流式',

    request_model VARCHAR(128) NULL COMMENT '请求模型名或模型别名',
    upstream_model VARCHAR(128) NULL COMMENT '上游真实模型名',
    resolved_model VARCHAR(128) NULL COMMENT 'Token计算解析模型名',
    model_encoding VARCHAR(64) NULL COMMENT 'Token计算encoding',

    token_calc_source VARCHAR(32) NOT NULL COMMENT 'REPORTED_USAGE/LOCAL_ESTIMATE/MERGED/UNSUPPORTED',
    token_calc_supported TINYINT(1) NOT NULL DEFAULT 1 COMMENT '是否支持Token计算',
    token_calc_note TEXT NULL COMMENT 'Token计算说明',

    input_tokens INT NOT NULL DEFAULT 0 COMMENT '输入Token总数',
    output_tokens INT NOT NULL DEFAULT 0 COMMENT '输出Token总数',
    total_tokens INT NOT NULL DEFAULT 0 COMMENT '总Token数',

    billable_input_tokens INT NOT NULL DEFAULT 0 COMMENT '参与计费的输入Token数',
    billable_output_tokens INT NOT NULL DEFAULT 0 COMMENT '参与计费的输出Token数',

    reserved_amount_cny DECIMAL(18, 8) NOT NULL DEFAULT 0 COMMENT '预占金额',
    amount_cny DECIMAL(18, 8) NOT NULL DEFAULT 0 COMMENT '最终结算金额',

    billing_strategy VARCHAR(64) NOT NULL COMMENT '计费策略',
    billing_currency VARCHAR(16) NOT NULL DEFAULT 'CNY' COMMENT '计费币种',

    latency_ms INT NULL COMMENT '耗时毫秒',
    status_code INT NOT NULL COMMENT 'HTTP状态码',
    error_code VARCHAR(64) NULL COMMENT '错误码',
    accounting_status VARCHAR(32) NOT NULL COMMENT '结算状态',

    request_started_at DATETIME NULL COMMENT '请求开始时间',
    settled_at DATETIME NULL COMMENT '结算时间',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',

    token_calc_detail JSON NULL COMMENT 'Token计算过程快照',
    billing_detail JSON NULL COMMENT '金额计算过程快照',

    UNIQUE KEY uk_request_id (request_id),
    KEY idx_user_created (user_id, created_at),
    KEY idx_vendor_model_created (vendor_id, model_id, created_at),
    KEY idx_accounting_status (accounting_status)
) COMMENT='LLM调用用量主日志';
```

### 3.2 Token 明细表 `llm_usage_token_detail`

保存输入、输出、缓存、音频、推理、预测等 Token 明细。金额计算必须基于此表对应的数据结构，而不是只基于 `input_tokens/output_tokens`。

```sql
CREATE TABLE llm_usage_token_detail (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键',
    usage_log_id BIGINT NOT NULL COMMENT '用量日志ID',
    request_id VARCHAR(64) NOT NULL COMMENT '请求ID',

    token_direction VARCHAR(16) NOT NULL COMMENT 'INPUT/OUTPUT',
    token_type VARCHAR(64) NOT NULL COMMENT 'TEXT/AUDIO/IMAGE/FILE/REASONING/TOOL/PREDICTION/OTHER',
    cache_type VARCHAR(32) NOT NULL DEFAULT 'NONE' COMMENT 'NONE/CACHE_HIT/CACHE_MISS/CACHE_WRITE',

    tokens INT NOT NULL DEFAULT 0 COMMENT 'Token数量',
    billable_tokens INT NOT NULL DEFAULT 0 COMMENT '参与计费的Token数量',

    source VARCHAR(32) NOT NULL COMMENT 'REPORTED/LOCAL/MERGED/DERIVED',
    provider_field VARCHAR(128) NULL COMMENT '上游原始字段名',
    note VARCHAR(512) NULL COMMENT '说明',

    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',

    KEY idx_usage_log_id (usage_log_id),
    KEY idx_request_id (request_id),
    KEY idx_direction_type_cache (token_direction, token_type, cache_type)
) COMMENT='LLM Token用量明细';
```

### 3.3 金额明细表 `llm_usage_billing_detail`

保存每个计费项的单价快照和金额，便于审计和复算。

```sql
CREATE TABLE llm_usage_billing_detail (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键',
    usage_log_id BIGINT NOT NULL COMMENT '用量日志ID',
    request_id VARCHAR(64) NOT NULL COMMENT '请求ID',

    charge_item VARCHAR(64) NOT NULL COMMENT '计费项',
    token_direction VARCHAR(16) NOT NULL COMMENT 'INPUT/OUTPUT',
    token_type VARCHAR(64) NOT NULL COMMENT 'TEXT/AUDIO/IMAGE/FILE/REASONING/TOOL/PREDICTION/OTHER',
    cache_type VARCHAR(32) NOT NULL DEFAULT 'NONE' COMMENT 'NONE/CACHE_HIT/CACHE_MISS/CACHE_WRITE',

    tokens INT NOT NULL DEFAULT 0 COMMENT 'Token数量',
    price_cny_per_million DECIMAL(18, 8) NOT NULL DEFAULT 0 COMMENT '百万Token单价快照',
    amount_cny DECIMAL(18, 8) NOT NULL DEFAULT 0 COMMENT '本计费项金额',

    pricing_rule VARCHAR(128) NULL COMMENT '命中的价格规则',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',

    KEY idx_usage_log_id (usage_log_id),
    KEY idx_request_id (request_id),
    KEY idx_charge_item (charge_item)
) COMMENT='LLM Token计费明细';
```

### 3.4 模型价格规则表 `model_price_rule`

替代单一输入价、输出价的计费方式，支持缓存命中、缓存写入、音频、推理等差异化计费。

```sql
CREATE TABLE model_price_rule (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键',
    model_id BIGINT NOT NULL COMMENT '模型ID',
    vendor_id BIGINT NOT NULL COMMENT '供应商ID',

    charge_item VARCHAR(64) NOT NULL COMMENT '计费项',
    price_cny_per_million DECIMAL(18, 8) NOT NULL COMMENT '百万Token单价',
    currency VARCHAR(16) NOT NULL DEFAULT 'CNY' COMMENT '币种',

    active TINYINT(1) NOT NULL DEFAULT 1 COMMENT '是否启用',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',

    UNIQUE KEY uk_model_charge_item (model_id, charge_item),
    KEY idx_vendor_model (vendor_id, model_id)
) COMMENT='模型Token价格规则';
```

## 4. 枚举与字段语义

### 4.1 Token 方向

```text
INPUT
OUTPUT
```

### 4.2 Token 类型

```text
TEXT
AUDIO
IMAGE
FILE
REASONING
TOOL
PREDICTION
OTHER
```

### 4.3 缓存类型

```text
NONE
CACHE_HIT
CACHE_MISS
CACHE_WRITE
```

### 4.4 Token 计算来源

```text
REPORTED_USAGE   上游usage完整，直接使用
LOCAL_ESTIMATE   上游没有usage，本地估算
MERGED           上游usage不完整，本地补齐
UNSUPPORTED      协议或载荷不支持计算
```

### 4.5 Token 明细来源

```text
REPORTED   上游明确返回
LOCAL      本地估算
MERGED     上游和本地合并
DERIVED    根据其他字段推导，例如 cache miss = prompt - cached
```

### 4.6 计费项

第一阶段至少支持：

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

## 5. DTO 与模块设计

### 5.1 Token 计算结果

```kotlin
data class TokenEstimateResult(
    val usage: TokenUsageSummaryDto,
    val tokenDetails: List<TokenDetailDto>,
    val resolvedModel: String,
    val encoding: String,
    val source: TokenEstimateSource,
    val supported: Boolean,
    val note: String,
    val promptTextLen: Int,
    val completionTextLen: Int,
    val calcDetail: String,
)
```

`TokenUsageSummaryDto` 只保存汇总：

```kotlin
data class TokenUsageSummaryDto(
    val inputTokens: Int,
    val outputTokens: Int,
    val totalTokens: Int,
)
```

`TokenDetailDto` 保存可计费维度：

```kotlin
data class TokenDetailDto(
    val direction: TokenDirection,
    val tokenType: TokenType,
    val cacheType: TokenCacheType,
    val tokens: Int,
    val billableTokens: Int,
    val source: TokenDetailSource,
    val providerField: String?,
    val note: String?,
)
```

### 5.2 计费结果

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

## 6. 改造任务分解

### 阶段 1：数据库与生成代码准备

目标：先建立新表和模型价格规则，不接入业务流程。

任务：

1. 新增 SQL 迁移文件。
2. 创建 `llm_usage_log`。
3. 创建 `llm_usage_token_detail`。
4. 创建 `llm_usage_billing_detail`。
5. 创建 `model_price_rule`。
6. 更新 `generatorConfig.xml`，纳入新表。
7. 重新生成 MyBatis Record、Mapper、DynamicSqlSupport、MapperExtensions。

产出文件：

- `llm-gateway/sql/*_create_llm_usage_accounting_tables.sql`
- `llm-gateway/src/main/resources/generatorConfig.xml`
- `llm-gateway/src/main/kotlin/com/llm/gateway/dal/model/*`
- `llm-gateway/src/main/kotlin/com/llm/gateway/dal/mapper/*`

验收标准：

- SQL 可在本地数据库执行成功。
- MyBatis 生成文件编译通过。
- 不影响现有接口运行。

顺序要求：

- 本阶段完成前，不允许改造 `OpenAiForwardFacade`。

### 阶段 2：Token 计算基础模型

目标：建立协议无关的 Token 计算数据结构和枚举。

任务：

1. 新增 `com.llm.gateway.tokencalc` 包。
2. 新增 Token 协议、方向、类型、缓存类型、计算来源、明细来源枚举。
3. 新增 `TokenEstimateParams`。
4. 新增 `TokenEstimateResult`。
5. 新增 `TokenUsageSummaryDto`。
6. 新增 `TokenDetailDto`。
7. 新增 `TokenUsageNormalizer`。

产出文件：

- `tokencalc/model/TokenCalcDtos.kt`
- `tokencalc/model/TokenCalcEnums.kt`
- `tokencalc/TokenUsageNormalizer.kt`

验收标准：

- 枚举值与本任务书保持一致。
- `TokenUsageNormalizer` 覆盖负数归零、total 修正、明细汇总。
- 单元测试覆盖归一化规则。

### 阶段 3：OpenAI Chat Token 计算实现

目标：先覆盖当前网关实际使用的 `/v1/chat/completions`。

任务：

1. 新增 `TokenCalcService` 接口。
2. 新增 `DefaultTokenCalcService`。
3. 新增 `TokenProviderEstimator`。
4. 新增 `OpenAiChatEstimator`。
5. 支持请求体 `messages/system/tools/response_format` 提取。
6. 支持响应体 `usage` 提取。
7. 支持 OpenAI usage details：
   - `prompt_tokens_details.cached_tokens`
   - `prompt_cache_hit_tokens`
   - `prompt_cache_miss_tokens`
   - `prompt_tokens_details.audio_tokens`
   - `completion_tokens_details.reasoning_tokens`
   - `completion_tokens_details.audio_tokens`
   - `completion_tokens_details.accepted_prediction_tokens`
   - `completion_tokens_details.rejected_prediction_tokens`
8. 当未返回 cache miss 时，按 `promptTokens - cachedTokens` 推导。
9. 生成 `TokenDetailDto` 列表。

产出文件：

- `tokencalc/TokenCalcService.kt`
- `tokencalc/DefaultTokenCalcService.kt`
- `tokencalc/provider/TokenProviderEstimator.kt`
- `tokencalc/provider/OpenAiChatEstimator.kt`
- `tokencalc/text/TokenTextBuilder.kt`

验收标准：

- 响应带完整 usage 时，来源为 `REPORTED_USAGE`。
- 响应不带 usage 时，来源为 `LOCAL_ESTIMATE`。
- usage 不完整时，来源为 `MERGED`。
- Token 明细能区分 `INPUT/TEXT/CACHE_HIT`、`INPUT/TEXT/CACHE_MISS`、`OUTPUT/REASONING` 等。
- 不依赖数据库、配额或计费服务。

### 阶段 4：Token 计费架构

目标：金额计算从 `promptTokens/completionTokens` 改为基于 Token 明细逐项计费。

任务：

1. 新增 `TokenBillingService`。
2. 新增 `ModelPriceRuleResolver`。
3. 新增 `TokenChargeItemResolver`。
4. 新增 `TokenBillingResult`。
5. 新增 `TokenBillingDetailDto`。
6. 从 `model_price_rule` 读取计费项价格。
7. 支持默认兼容规则：
   - 缺少 `INPUT_CACHE_MISS` 时回退 `INPUT_TEXT`
   - 缺少 `INPUT_CACHE_HIT` 时可配置为 0 或回退 `INPUT_TEXT`
   - 缺少 `OUTPUT_REASONING` 时回退 `OUTPUT_TEXT`
8. 生成每个计费项金额明细。

产出文件：

- `billing/TokenBillingService.kt`
- `billing/DefaultTokenBillingService.kt`
- `billing/ModelPriceRuleResolver.kt`
- `billing/TokenChargeItemResolver.kt`
- `billing/TokenBillingDtos.kt`

验收标准：

- 同一请求可以拆出多个计费项。
- 最终金额等于所有计费明细金额之和。
- 价格使用数据库快照，不依赖前端展示价格。
- 金额计算保留 8 位存储，展示层再做格式化。

### 阶段 5：用量日志落库服务

目标：把 Token 计算结果和金额计算结果稳定写入三张日志表。

任务：

1. 新增 `UsageLogWriteService`。
2. 新增 `UsageLogRecordCommand` 新版本，不复用旧 `UsageLogRecordCommand`。
3. 写入 `llm_usage_log` 主表。
4. 批量写入 `llm_usage_token_detail`。
5. 批量写入 `llm_usage_billing_detail`。
6. 保证主表 ID 回填后再写明细。
7. 保证同一 `request_id` 幂等，避免重复插入。

产出文件：

- `service/UsageLogWriteService.kt`
- `model/dto/LlmUsageLogDtos.kt`

验收标准：

- 成功请求写入 1 条主表、多条 Token 明细、多条金额明细。
- 失败请求也写入主表，Token 和金额可为空或为 0。
- 重复 requestId 不产生重复账单。

### 阶段 6：非流式请求接入

目标：先改造非流式 Chat Completions 请求，完成从计算到计费再到落库的闭环。

任务：

1. 在 `OpenAiForwardFacade.chatCompletionsJsonCore()` 中保留原有配额预占流程。
2. 成功响应后调用 `TokenCalcService.estimate()`。
3. 使用 `TokenBillingService.calculate()` 计算实际金额。
4. 使用 `UserQuotaUsageService.settle()` 结算。
5. 使用 `UsageLogWriteService` 写新日志表。
6. 失败响应或异常时写失败日志。
7. 暂时保留旧 `usage_logs` 写入，或通过配置开关控制双写。

产出文件：

- `service/OpenAiForwardFacade.kt`
- 必要配置项

验收标准：

- 非流式成功请求完成新表落库。
- 上游 usage details 中缓存字段能进入 Token 明细和金额明细。
- 原有配额扣减语义不变。
- 如果开启双写，旧表结果与新表主表汇总结果一致。

### 阶段 7：预占金额改造

目标：预占额度不再只依赖 `max_tokens`，需要考虑输入 Token。

任务：

1. 请求转发前调用 `TokenCalcService` 只计算 prompt。
2. completion 预估使用 `max_tokens`、`max_completion_tokens` 或系统默认值。
3. 构造预估 Token 明细。
4. 使用 `TokenBillingService` 计算预占金额。
5. 预占金额写入 `llm_usage_log.reserved_amount_cny`。

验收标准：

- 长 prompt 请求不会因为 `max_tokens` 小而预占明显不足。
- 缓存命中在预估阶段如果无法确定，按 cache miss 或普通 input 价格保守预占。

### 阶段 8：流式请求接入

目标：支持流式请求的 Token 统计、金额计算和落库。

任务：

1. 新增 `StreamingTokenCounter`。
2. 在 `forwardStream()` 旁路消费上游 chunk。
3. 收集 SSE `data: {...}` 和 `[DONE]`。
4. 流结束时生成最终 `TokenEstimateResult`。
5. 完成实际金额计算。
6. 完成配额结算或失败补偿。
7. 写入三张新日志表。

验收标准：

- 正常流式完成后可以落库。
- 客户端中断时有明确失败或补偿记录。
- 上游最后返回 usage 时优先使用上游 usage。
- 无 usage 时可以使用本地累计估算。

### 阶段 9：旧表下线与查询迁移

目标：新表稳定后，迁移查询和前端展示。

任务：

1. 新增用量日志查询接口。
2. 支持按用户、模型、供应商、时间、状态查询。
3. 支持查看 Token 明细。
4. 支持查看金额明细。
5. 前端用量日志页面改读新表。
6. 关闭旧表双写。

验收标准：

- 管理端能查看一次请求的 Token 拆分和金额拆分。
- 用户端能看到消费金额和基础 Token 汇总。
- 旧 `usage_logs` 不再作为新账单来源。

## 7. 开发顺序硬约束

后续开发必须遵守：

1. 先建表和生成 DAL，再写业务代码。
2. 先完成 OpenAI Chat 非流式，再扩展其他协议。
3. 先完成 Token 计算，再完成金额计算。
4. 金额计算必须基于 Token 明细，不允许再只按 prompt/completion 两个总数字段计算。
5. 落库服务只接收计算结果，不重新解析 request/response。
6. 流式请求最后改造，不能和非流式首版混在一次提交里。
7. 每个阶段都必须有对应测试或回归脚本。

## 8. 测试计划

### 8.1 单元测试

- `TokenUsageNormalizerTest`
- `OpenAiChatEstimatorTest`
- `TokenChargeItemResolverTest`
- `DefaultTokenBillingServiceTest`
- `UsageLogWriteServiceTest`

### 8.2 回归测试

需要覆盖：

- 响应带完整 usage。
- 响应不带 usage，本地估算。
- usage 不完整，合并本地估算。
- 输入缓存命中 token。
- 输入缓存未命中 token。
- 输出推理 token。
- 输出音频 token。
- 缓存命中使用独立价格。
- 缺少缓存价格时回退默认价格。
- 失败请求退款并落失败日志。

### 8.3 数据一致性校验

每条成功日志必须满足：

```text
llm_usage_log.total_tokens = input_tokens + output_tokens
llm_usage_log.amount_cny = SUM(llm_usage_billing_detail.amount_cny)
llm_usage_log.input_tokens = SUM(INPUT token detail)
llm_usage_log.output_tokens = SUM(OUTPUT token detail)
```

## 9. 风险与处理策略

### 9.1 tokenizer 精度风险

本地估算不一定等于上游真实 usage。处理策略：

- 上游 usage 完整时直接使用。
- 本地估算只作为兜底。
- `token_calc_source` 和 `token_calc_note` 必须落库。

### 9.2 缓存字段供应商差异

不同供应商缓存字段不一致。处理策略：

- provider estimator 内部做协议适配。
- 标准化输出到 `TokenDetailDto`。
- 原始字段名保存到 `provider_field`。

### 9.3 价格规则缺失

缺少某个计费项价格会导致金额错误。处理策略：

- 首版使用明确回退规则。
- 计费详情记录 `pricingRule`。
- 后续管理端增加价格规则校验。

### 9.4 双写期间数据不一致

处理策略：

- 双写期间以旧表保持线上兼容。
- 新表用于校验。
- 稳定后切换查询和结算审计口径。

## 10. 第一阶段执行清单

下一步只执行第一阶段：

1. 新增建表 SQL。
2. 更新 `generatorConfig.xml`。
3. 生成新表 DAL。
4. 编译验证。

第一阶段不修改：

- `OpenAiForwardFacade`
- `OpenAiForwardService`
- `AmountCalculator`
- `UsageLogService`
- 配额扣减逻辑

完成第一阶段后，再进入第二阶段 Token 计算基础模型开发。
