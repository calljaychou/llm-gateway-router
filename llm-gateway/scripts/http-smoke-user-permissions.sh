#!/usr/bin/env bash
set -euo pipefail

BASE_URL="${1:-http://127.0.0.1:8080}"
USERNAME="${2:-admin}"
PASSWORD="${3:-123456}"
ROOT_DEPT_ID="${4:-1}"
CHILD_DEPT_ID="${5:-2}"
TARGET_MODEL_ALIAS="${6:-gpt-4-enterprise}"

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
  echo "ERROR: token 提取失败"
  exit 1
fi
echo

NEW_USER="user-perm-$(date +%s)"
NEW_MAIL="${NEW_USER}@company.com"

echo "==> 2) US-002 创建用户"
CREATE_USER_BODY="$(cat <<JSON
{"username":"${NEW_USER}","email":"${NEW_MAIL}","mobile":"13800138000","deptId":${CHILD_DEPT_ID},"roleKeys":["user"],"password":"Temp@123456","forcePasswordChange":true}
JSON
)"
CREATE_USER_RESP="$(curl -sS -X POST "${BASE_URL}/admin/users" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer ${TOKEN}" \
  -d "${CREATE_USER_BODY}")"
echo "创建用户响应: ${CREATE_USER_RESP}"
NEW_USER_ID="$(extract_json_value "${CREATE_USER_RESP}" "data.userId")"
if [[ -z "${NEW_USER_ID}" ]]; then
  echo "ERROR: userId 提取失败"
  exit 1
fi
echo

echo "==> 3) US-003 配置父部门权限(SUBTREE)"
ASSIGN_ROOT_BODY="$(cat <<JSON
{"mode":"REPLACE","items":[{"modelAlias":"${TARGET_MODEL_ALIAS}","scope":"SUBTREE"}]}
JSON
)"
ROOT_ASSIGN_RESP="$(curl -sS -X PUT "${BASE_URL}/admin/departments/${ROOT_DEPT_ID}/permissions" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer ${TOKEN}" \
  -d "${ASSIGN_ROOT_BODY}")"
echo "父部门权限响应: ${ROOT_ASSIGN_RESP}"
echo

echo "==> 4) 查询子部门生效权限"
EFFECTIVE_DEPT_RESP="$(curl -sS -X GET "${BASE_URL}/admin/departments/${CHILD_DEPT_ID}/permissions?view=effective" \
  -H "Authorization: Bearer ${TOKEN}")"
echo "子部门生效权限响应: ${EFFECTIVE_DEPT_RESP}"
echo

echo "==> 5) 查询新用户生效权限"
EFFECTIVE_USER_RESP="$(curl -sS -X GET "${BASE_URL}/admin/users/${NEW_USER_ID}/permissions/effective" \
  -H "Authorization: Bearer ${TOKEN}")"
echo "用户生效权限响应: ${EFFECTIVE_USER_RESP}"
echo

echo "==> 测试结束"
echo "提示: 若返回 403，请确认管理员账号具备 admin/llm-lead 角色，且目标模型已在 models 表中激活。"
