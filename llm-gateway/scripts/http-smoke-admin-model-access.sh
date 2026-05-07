#!/usr/bin/env bash
set -euo pipefail

BASE_URL="${1:-http://127.0.0.1:8080}"
USERNAME="${2:-admin}"
PASSWORD="${3:-123456}"
VENDOR_NAME="${4:-OpenAI-SMOKE-$(date +%s)}"
VENDOR_BASE_URL="${5:-https://api.openai.com}"
MASTER_KEY_PLAIN="${6:-sk-smoke-test-1234567890}"
MODEL_ALIAS="${7:-gpt-4-enterprise-smoke-$(date +%s)}"
REAL_MODEL_NAME="${8:-gpt-4o}"

echo "==> Base URL: ${BASE_URL}"
echo "==> Username: ${USERNAME}"
echo

extract_json_value() {
  local json="$1"
  local expr="$2"
  JSON_PAYLOAD="${json}" python3 - "$expr" <<'PY'
import json
import os
import sys

expr = sys.argv[1]
raw = os.environ.get("JSON_PAYLOAD", "").strip()
obj = json.loads(raw)
for part in expr.split("."):
    if isinstance(obj, dict):
        obj = obj.get(part)
    else:
        obj = None
        break
if obj is None:
    print("")
else:
    print(obj)
PY
}

echo "==> 1) 登录获取 token"
LOGIN_BODY="$(cat <<JSON
{"username":"${USERNAME}","password":"${PASSWORD}"}
JSON
)"
LOGIN_RESP="$(curl -sS -X POST "${BASE_URL}/api/auth/login" \
  -H "Content-Type: application/json" \
  -d "${LOGIN_BODY}")"
echo "登录响应: ${LOGIN_RESP}"

TOKEN="$(extract_json_value "${LOGIN_RESP}" "data.token")"
if [[ -z "${TOKEN}" ]]; then
  TOKEN="$(extract_json_value "${LOGIN_RESP}" "token")"
fi
if [[ -z "${TOKEN}" ]]; then
  echo "ERROR: token 提取失败，请检查登录响应。"
  exit 1
fi
echo "token 提取成功。"
echo

echo "==> 2) 创建供应商"
CREATE_VENDOR_BODY="$(cat <<JSON
{"name":"${VENDOR_NAME}","baseUrl":"${VENDOR_BASE_URL}","status":1}
JSON
)"
VENDOR_RESP="$(curl -sS -X POST "${BASE_URL}/admin/vendors" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer ${TOKEN}" \
  -d "${CREATE_VENDOR_BODY}")"
echo "供应商响应: ${VENDOR_RESP}"

VENDOR_ID="$(extract_json_value "${VENDOR_RESP}" "data.vendorId")"
if [[ -z "${VENDOR_ID}" ]]; then
  echo "ERROR: vendorId 提取失败，无法继续。"
  exit 1
fi
echo "vendorId=${VENDOR_ID}"
echo

echo "==> 3) 创建主密钥"
CREATE_MASTER_KEY_BODY="$(cat <<JSON
{"vendorId":${VENDOR_ID},"apiKey":"${MASTER_KEY_PLAIN}","weight":10,"status":1}
JSON
)"
MASTER_KEY_RESP="$(curl -sS -X POST "${BASE_URL}/admin/master-keys" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer ${TOKEN}" \
  -d "${CREATE_MASTER_KEY_BODY}")"
echo "主密钥响应: ${MASTER_KEY_RESP}"
echo

echo "==> 4) 创建模型映射"
CREATE_MODEL_BODY="$(cat <<JSON
{"modelAlias":"${MODEL_ALIAS}","realModelName":"${REAL_MODEL_NAME}","vendorId":${VENDOR_ID},"billingType":"PAID","active":true}
JSON
)"
MODEL_RESP="$(curl -sS -X POST "${BASE_URL}/admin/models" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer ${TOKEN}" \
  -d "${CREATE_MODEL_BODY}")"
echo "模型响应: ${MODEL_RESP}"
echo

echo "==> 测试结束"
echo "提示: 若返回 403，请确认登录账号拥有 admin 或 llm-lead 角色。"
