#!/usr/bin/env bash
set -euo pipefail

BASE_URL="${1:-http://127.0.0.1:8080}"
USERNAME="${2:-admin}"
PASSWORD="${3:-123456}"
DEPT_ID="${4:-1}"
OPENAI_BASE_URL="${5:-https://api.deepseek.com}"
MASTER_API_KEY="${6:-sk-72e12834312c4731a1fd9c9d5444cd9b}"
REAL_MODEL_NAME="${7:-deepseek-v4-flash}"

if [[ -z "${MASTER_API_KEY}" ]]; then
  echo "ERROR: 第6个参数 MASTER_API_KEY 不能为空。"
  echo "用法: $0 [baseUrl] [username] [password] [deptId] [openaiBaseUrl] [masterApiKey] [realModelName]"
  exit 1
fi

SUFFIX="$(date +%s)"
MODEL_ALIAS="gpt-4-enterprise-${SUFFIX}"
VENDOR_NAME="OpenAI-US011-${SUFFIX}"
VKEY_NAME="US011-vkey-${SUFFIX}"

echo "==> Base URL: ${BASE_URL}"
echo "==> Username: ${USERNAME}"
echo "==> Dept ID: ${DEPT_ID}"
echo "==> Model Alias: ${MODEL_ALIAS}"
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

send_json() {
  local method="$1"
  local url="$2"
  local body="$3"
  local auth_header="${4:-}"

  local body_file header_file status_code
  body_file="$(mktemp)"
  header_file="$(mktemp)"

  if [[ -n "${auth_header}" ]]; then
    status_code="$(curl -sS -X "${method}" "${url}" \
      -H "Content-Type: application/json" \
      -H "Authorization: ${auth_header}" \
      -d "${body}" \
      -D "${header_file}" \
      -o "${body_file}" \
      -w "%{http_code}")"
  else
    status_code="$(curl -sS -X "${method}" "${url}" \
      -H "Content-Type: application/json" \
      -d "${body}" \
      -D "${header_file}" \
      -o "${body_file}" \
      -w "%{http_code}")"
  fi

  RESP_STATUS="${status_code}"
  RESP_HEADERS="$(cat "${header_file}")"
  RESP_BODY="$(cat "${body_file}")"
  rm -f "${body_file}" "${header_file}"
}

echo "==> 1) 登录获取管理员 Token"
LOGIN_BODY="$(cat <<JSON
{"username":"${USERNAME}","password":"${PASSWORD}"}
JSON
)"
send_json "POST" "${BASE_URL}/api/auth/login" "${LOGIN_BODY}"
echo "HTTP=${RESP_STATUS}"
echo "响应=${RESP_BODY}"
if [[ "${RESP_STATUS}" != "200" ]]; then
  echo "ERROR: 登录失败"
  exit 1
fi
ADMIN_TOKEN="$(extract_json_value "${RESP_BODY}" "data.token")"
if [[ -z "${ADMIN_TOKEN}" ]]; then
  echo "ERROR: admin token 提取失败"
  exit 1
fi
echo

echo "==> 2) 创建供应商"
CREATE_VENDOR_BODY="$(cat <<JSON
{"name":"${VENDOR_NAME}","baseUrl":"${OPENAI_BASE_URL}","status":1}
JSON
)"
send_json "POST" "${BASE_URL}/admin/vendors" "${CREATE_VENDOR_BODY}" "Bearer ${ADMIN_TOKEN}"
echo "HTTP=${RESP_STATUS}"
echo "响应=${RESP_BODY}"
VENDOR_ID="$(extract_json_value "${RESP_BODY}" "data.vendorId")"
if [[ -z "${VENDOR_ID}" ]]; then
  echo "ERROR: vendorId 提取失败"
  exit 1
fi
echo

echo "==> 3) 创建主密钥"
CREATE_MASTER_KEY_BODY="$(cat <<JSON
{"vendorId":${VENDOR_ID},"apiKey":"${MASTER_API_KEY}","weight":10,"status":1}
JSON
)"
send_json "POST" "${BASE_URL}/admin/master-keys" "${CREATE_MASTER_KEY_BODY}" "Bearer ${ADMIN_TOKEN}"
echo "HTTP=${RESP_STATUS}"
echo "响应=${RESP_BODY}"
echo

echo "==> 4) 创建模型映射"
CREATE_MODEL_BODY="$(cat <<JSON
{"modelAlias":"${MODEL_ALIAS}","realModelName":"${REAL_MODEL_NAME}","vendorId":${VENDOR_ID},"billingType":"PAID","active":true}
JSON
)"
send_json "POST" "${BASE_URL}/admin/models" "${CREATE_MODEL_BODY}" "Bearer ${ADMIN_TOKEN}"
echo "HTTP=${RESP_STATUS}"
echo "响应=${RESP_BODY}"
echo

echo "==> 5) 给部门授权模型"
ASSIGN_PERM_BODY="$(cat <<JSON
{"items":[{"modelAlias":"${MODEL_ALIAS}","scope":"SUBTREE"}]}
JSON
)"
send_json "PUT" "${BASE_URL}/admin/departments/${DEPT_ID}/permissions" "${ASSIGN_PERM_BODY}" "Bearer ${ADMIN_TOKEN}"
echo "HTTP=${RESP_STATUS}"
echo "响应=${RESP_BODY}"
echo

echo "==> 6) 创建虚拟 API Key"
CREATE_VKEY_BODY="$(cat <<JSON
{"name":"${VKEY_NAME}"}
JSON
)"
send_json "POST" "${BASE_URL}/admin/user/keys" "${CREATE_VKEY_BODY}" "Bearer ${ADMIN_TOKEN}"
echo "HTTP=${RESP_STATUS}"
echo "响应=${RESP_BODY}"
VIRTUAL_KEY="$(extract_json_value "${RESP_BODY}" "data.apiKey")"
if [[ -z "${VIRTUAL_KEY}" ]]; then
  echo "ERROR: virtual api key 提取失败"
  exit 1
fi
echo
