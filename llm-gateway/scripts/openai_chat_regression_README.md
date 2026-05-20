# OpenAI Chat Completions 回归脚本（Python）

用于回放验证网关接口 `POST /v1/chat/completions` 的 JSON 与 SSE（stream=true）行为，适合替代或补充 MockMvc。

## 1. 准备

- 确保服务已启动（默认脚本访问 `http://127.0.0.1:8080`）
- 准备一个可用的虚拟 API Key（以 `sk-vkey-` 开头）
- 确保该虚拟 Key 对应用户有权限调用你填写的 `model_alias`

## 2. 配置用例

编辑同目录下的 `openai_chat_regression_cases.json`：

- `variables.virtual_api_key`
- `variables.model_alias`

## 3. 执行

运行全部用例：

```bash
python3 llm-gateway/scripts/openai_chat_regression.py
```

覆盖 baseUrl / vkey / model：

```bash
python3 llm-gateway/scripts/openai_chat_regression.py \
  --base-url http://127.0.0.1:8080 \
  --vkey sk-vkey-EX5nYfigBhzYlzV8FzZA2WFurm6er255 \
  --model-alias deepseek-v4-flash
```

仅运行某一个 case：

```bash
python3 llm-gateway/scripts/openai_chat_regression.py --case chat_completions_stream_true
```

## 4. 判定标准

- stream 用例：状态码=200、`Content-Type` 包含 `text/event-stream`、输出包含 `[DONE]`
- json 用例：状态码=200、`Content-Type` 包含 `application/json`

