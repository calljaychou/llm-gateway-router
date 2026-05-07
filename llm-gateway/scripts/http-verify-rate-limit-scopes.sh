#!/usr/bin/env bash
set -euo pipefail

BASE_URL="${1:-http://127.0.0.1:8080}"
ADMIN_USERNAME="${2:-admin}"
ADMIN_PASSWORD="${3:-123456}"
DEPT_ID="${4:-1}"
OPENAI_BASE_URL="${5:-https://api.openai.com}"
MASTER_API_KEY="${6:-sk-}"
REAL_MODEL_NAME="${7:-gpt-4o-mini}"

# 默认与 application-dev.yaml 对齐，可按需用环境变量覆盖。
RL_API_KEY_PER_SECOND="${RL_API_KEY_PER_SECOND:-3}"
RL_USER_PER_SECOND="${RL_USER_PER_SECOND:-5}"
RL_MODEL_PER_SECOND="${RL_MODEL_PER_SECOND:-20}"
RL_VENDOR_PER_SECOND="${RL_VENDOR_PER_SECOND:-50}"
RL_GLOBAL_PER_SECOND="${RL_GLOBAL_PER_SECOND:-100}"

if [[ -z "${MASTER_API_KEY}" ]]; then
  echo "ERROR: 第6个参数 MASTER_API_KEY 不能为空。"
  echo "用法: $0 [baseUrl] [adminUsername] [adminPassword] [deptId] [openaiBaseUrl] [masterApiKey] [realModelName]"
  exit 1
fi

SUFFIX="$(date +%s)"
TMP_DIR="$(mktemp -d)"
trap 'rm -rf "${TMP_DIR}"' EXIT

RESP_STATUS=""
RESP_HEADERS=""
RESP_BODY=""

declare -a TEST_USERS=()
declare -a TEST_PASSWORDS=()
declare -a TEST_TOKENS=()
declare -A USER_KEYS=()
declare -a VENDOR_IDS=()
declare -A MODELS_BY_VENDOR=()

extract_json_value() {
  local json="$1"
  local expr="$2"
  JSON_PAYLOAD="${json}" python3 - "$expr" <<'PY'
import json
import os
import sys

expr = sys.argv[1]
raw = os.environ.get("JSON_PAYLOAD", "").strip()
try:
    obj = json.loads(raw)
except Exception:
    print("")
    sys.exit(0)
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

json_escape() {
  python3 - "$1" <<'PY'
import json
import sys
print(json.dumps(sys.argv[1], ensure_ascii=False))
PY
}

must_success_200() {
  local step="$1"
  if [[ "${RESP_STATUS}" != "200" ]]; then
    echo "ERROR: ${step} 失败，HTTP=${RESP_STATUS}"
    echo "响应=${RESP_BODY}"
    exit 1
  fi
}

csv_get() {
  local csv="$1"
  local idx="$2"
  echo "${csv}" | awk -F',' -v i="$((idx+1))" '{print $i}'
}

append_csv() {
  local old="$1"
  local value="$2"
  if [[ -z "${old}" ]]; then
    echo "${value}"
  else
    echo "${old},${value}"
  fi
}

login_as() {
  local username="$1"
  local password="$2"
  local body
  body="$(cat <<JSON
{"username":"${username}","password":"${password}"}
JSON
)"
  send_json "POST" "${BASE_URL}/api/auth/login" "${body}"
  must_success_200 "登录 ${username}"
  local token
  token="$(extract_json_value "${RESP_BODY}" "data.token")"
  if [[ -z "${token}" ]]; then
    echo "ERROR: 登录 ${username} 未提取到 token"
    exit 1
  fi
  echo "${token}"
}

create_vendor_with_models() {
  local vendor_index="$1"
  local model_count="$2"
  local admin_bearer="$3"

  local vendor_name="RL-Vendor-${SUFFIX}-${vendor_index}"
  local body
  body="$(cat <<JSON
{"name":"${vendor_name}","baseUrl":"${OPENAI_BASE_URL}","status":1}
JSON
)"
  send_json "POST" "${BASE_URL}/admin/vendors" "${body}" "${admin_bearer}"
  must_success_200 "创建供应商 ${vendor_name}"
  local vendor_id
  vendor_id="$(extract_json_value "${RESP_BODY}" "data.vendorId")"
  if [[ -z "${vendor_id}" ]]; then
    echo "ERROR: 创建供应商未返回 vendorId"
    exit 1
  fi
  VENDOR_IDS+=("${vendor_id}")

  body="$(cat <<JSON
{"vendorId":${vendor_id},"apiKey":"${MASTER_API_KEY}","weight":10,"status":1}
JSON
)"
  send_json "POST" "${BASE_URL}/admin/master-keys" "${body}" "${admin_bearer}"
  must_success_200 "创建主密钥 vendor=${vendor_id}"

  local aliases=""
  local i
  for ((i=1; i<=model_count; i++)); do
    local alias="rl-model-v${vendor_index}-m${i}-${SUFFIX}"
    body="$(cat <<JSON
{"modelAlias":"${alias}","realModelName":"${REAL_MODEL_NAME}","vendorId":${vendor_id},"billingType":"PAID","active":true}
JSON
)"
    send_json "POST" "${BASE_URL}/admin/models" "${body}" "${admin_bearer}"
    must_success_200 "创建模型 ${alias}"
    aliases="$(append_csv "${aliases}" "${alias}")"
  done
  MODELS_BY_VENDOR["${vendor_id}"]="${aliases}"
}

create_test_user() {
  local idx="$1"
  local admin_bearer="$2"
  local username="rl_user_${SUFFIX}_${idx}"
  local password="Temp@123456"
  local email="rl_user_${SUFFIX}_${idx}@example.com"
  local body
  body="$(cat <<JSON
{"username":"${username}","email":"${email}","deptId":${DEPT_ID},"roleKeys":["user"],"password":"${password}","forcePasswordChange":false}
JSON
)"
  send_json "POST" "${BASE_URL}/admin/users" "${body}" "${admin_bearer}"
  must_success_200 "创建测试用户 ${username}"

  TEST_USERS+=("${username}")
  TEST_PASSWORDS+=("${password}")
}

grant_models_to_dept() {
  local admin_bearer="$1"
  local items_json=""
  local vendor_id
  for vendor_id in "${VENDOR_IDS[@]}"; do
    IFS=',' read -r -a aliases <<< "${MODELS_BY_VENDOR["${vendor_id}"]}"
    local alias
    for alias in "${aliases[@]}"; do
      local esc
      esc="$(json_escape "${alias}")"
      if [[ -n "${items_json}" ]]; then
        items_json+=","
      fi
      items_json+="{\"modelAlias\":${esc},\"scope\":\"SUBTREE\"}"
    done
  done

  local body
  body="{\"items\":[${items_json}]}"
  send_json "PUT" "${BASE_URL}/admin/departments/${DEPT_ID}/permissions" "${body}" "${admin_bearer}"
  must_success_200 "部门授权模型"
}

create_user_keys() {
  local keys_per_user="$1"
  local idx
  for ((idx=0; idx<${#TEST_USERS[@]}; idx++)); do
    local token
    token="$(login_as "${TEST_USERS[idx]}" "${TEST_PASSWORDS[idx]}")"
    TEST_TOKENS+=("${token}")
    local bearer="Bearer ${token}"
    local key_csv=""
    local k
    for ((k=1; k<=keys_per_user; k++)); do
      local key_name="rl-key-u$((idx+1))-k${k}-${SUFFIX}"
      local body
      body="$(cat <<JSON
{"name":"${key_name}"}
JSON
)"
      send_json "POST" "${BASE_URL}/admin/user/keys" "${body}" "${bearer}"
      must_success_200 "创建虚拟Key ${key_name}"
      local key
      key="$(extract_json_value "${RESP_BODY}" "data.apiKey")"
      if [[ -z "${key}" ]]; then
        echo "ERROR: 创建虚拟Key未返回apiKey"
        exit 1
      fi
      key_csv="$(append_csv "${key_csv}" "${key}")"
    done
    USER_KEYS["${idx}"]="${key_csv}"
  done
}

fire_async_request() {
  local req_id="$1"
  local virtual_key="$2"
  local model_alias="$3"
  local dir="$4"
  local payload
  payload="$(cat <<JSON
{"model":"${model_alias}","temperature":0.1,"messages":[{"role":"user","content":"rate limit scope test ${req_id}"}]}
JSON
)"

  (
    local h="${dir}/h_${req_id}.txt"
    local b="${dir}/b_${req_id}.txt"
    local s="${dir}/s_${req_id}.txt"
    local code
    code="$(curl -sS -X POST "${BASE_URL}/v1/chat/completions" \
      -H "Authorization: Bearer ${virtual_key}" \
      -H "Content-Type: application/json" \
      -d "${payload}" \
      -D "${h}" \
      -o "${b}" \
      -w "%{http_code}" || echo "000")"
    echo "${code}" > "${s}"
  ) &
}

run_scope_case() {
  local case_name="$1"
  local expected_scope="$2"
  local request_plan_file="$3"

  local case_dir="${TMP_DIR}/${case_name}"
  mkdir -p "${case_dir}"
  local req_id=0

  while IFS='|' read -r key model repeat_count; do
    [[ -z "${key}" ]] && continue
    local i
    for ((i=1; i<=repeat_count; i++)); do
      req_id=$((req_id + 1))
      fire_async_request "${req_id}" "${key}" "${model}" "${case_dir}"
    done
  done < "${request_plan_file}"

  wait

  local total=0
  local gateway_scope_hit=0
  local gateway_429=0
  local upstream_429=0
  local status_file
  for status_file in "${case_dir}"/s_*.txt; do
    [[ -f "${status_file}" ]] || continue
    total=$((total + 1))
    local id="${status_file##*/s_}"
    id="${id%.txt}"
    local code source scope
    code="$(cat "${status_file}")"
    source="$(grep -i '^X-RateLimit-Source:' "${case_dir}/h_${id}.txt" | tail -n1 | awk -F': ' '{print tolower($2)}' | tr -d '\r')"
    scope="$(grep -i '^X-RateLimit-Scope:' "${case_dir}/h_${id}.txt" | tail -n1 | awk -F': ' '{print tolower($2)}' | tr -d '\r')"
    if [[ "${code}" == "429" && "${source}" == "gateway" ]]; then
      gateway_429=$((gateway_429 + 1))
      if [[ "${scope}" == "${expected_scope}" ]]; then
        gateway_scope_hit=$((gateway_scope_hit + 1))
      fi
    fi
    if [[ "${code}" == "429" && "${source}" == "upstream" ]]; then
      upstream_429=$((upstream_429 + 1))
    fi
  done

  echo "[${case_name}] total=${total} gateway429=${gateway_429} upstream429=${upstream_429} expectedScopeHit=${gateway_scope_hit}"
  if [[ "${gateway_scope_hit}" -lt 1 ]]; then
    echo "[${case_name}] FAIL: 未命中 gateway ${expected_scope}。建议检查对应阈值配置或并发量。"
    return 1
  fi
  echo "[${case_name}] PASS"
  return 0
}

build_plan_file() {
  local path="$1"
  shift
  : > "${path}"
  while [[ $# -gt 0 ]]; do
    echo "$1" >> "${path}"
    shift
  done
}

echo "==> 限流 Scope 验证脚本"
echo "BASE_URL=${BASE_URL}"
echo "阈值(api_key/user/model/vendor/global)=(${RL_API_KEY_PER_SECOND}/${RL_USER_PER_SECOND}/${RL_MODEL_PER_SECOND}/${RL_VENDOR_PER_SECOND}/${RL_GLOBAL_PER_SECOND})"
echo

echo "==> 1) 管理员登录"
ADMIN_TOKEN="$(login_as "${ADMIN_USERNAME}" "${ADMIN_PASSWORD}")"
ADMIN_BEARER="Bearer ${ADMIN_TOKEN}"
echo "管理员登录成功"

echo "==> 2) 创建 3 个供应商，每个 3 个模型"
create_vendor_with_models 1 3 "${ADMIN_BEARER}"
create_vendor_with_models 2 3 "${ADMIN_BEARER}"
create_vendor_with_models 3 3 "${ADMIN_BEARER}"
echo "供应商与模型创建完成"

echo "==> 3) 创建 6 个测试用户并生成虚拟Key"
for i in 1 2 3 4 5 6; do
  create_test_user "${i}" "${ADMIN_BEARER}"
done
grant_models_to_dept "${ADMIN_BEARER}"
create_user_keys 3
echo "用户与虚拟Key创建完成"

V1="${VENDOR_IDS[0]}"
V2="${VENDOR_IDS[1]}"
V3="${VENDOR_IDS[2]}"
M1_1="$(csv_get "${MODELS_BY_VENDOR[${V1}]}" 0)"
M1_2="$(csv_get "${MODELS_BY_VENDOR[${V1}]}" 1)"
M1_3="$(csv_get "${MODELS_BY_VENDOR[${V1}]}" 2)"
M2_1="$(csv_get "${MODELS_BY_VENDOR[${V2}]}" 0)"
M2_2="$(csv_get "${MODELS_BY_VENDOR[${V2}]}" 1)"
M2_3="$(csv_get "${MODELS_BY_VENDOR[${V2}]}" 2)"
M3_1="$(csv_get "${MODELS_BY_VENDOR[${V3}]}" 0)"
M3_2="$(csv_get "${MODELS_BY_VENDOR[${V3}]}" 1)"
M3_3="$(csv_get "${MODELS_BY_VENDOR[${V3}]}" 2)"

U1K1="$(csv_get "${USER_KEYS[0]}" 0)"
U1K2="$(csv_get "${USER_KEYS[0]}" 1)"
U1K3="$(csv_get "${USER_KEYS[0]}" 2)"
U2K1="$(csv_get "${USER_KEYS[1]}" 0)"
U2K2="$(csv_get "${USER_KEYS[1]}" 1)"
U2K3="$(csv_get "${USER_KEYS[1]}" 2)"
U3K1="$(csv_get "${USER_KEYS[2]}" 0)"
U3K2="$(csv_get "${USER_KEYS[2]}" 1)"
U3K3="$(csv_get "${USER_KEYS[2]}" 2)"
U4K1="$(csv_get "${USER_KEYS[3]}" 0)"
U4K2="$(csv_get "${USER_KEYS[3]}" 1)"
U4K3="$(csv_get "${USER_KEYS[3]}" 2)"
U5K1="$(csv_get "${USER_KEYS[4]}" 0)"
U5K2="$(csv_get "${USER_KEYS[4]}" 1)"
U5K3="$(csv_get "${USER_KEYS[4]}" 2)"
U6K1="$(csv_get "${USER_KEYS[5]}" 0)"
U6K2="$(csv_get "${USER_KEYS[5]}" 1)"
U6K3="$(csv_get "${USER_KEYS[5]}" 2)"

echo "==> 4) 构建测试计划"
PLAN_API_KEY="${TMP_DIR}/plan_api_key.txt"
PLAN_USER="${TMP_DIR}/plan_user.txt"
PLAN_MODEL="${TMP_DIR}/plan_model.txt"
PLAN_VENDOR="${TMP_DIR}/plan_vendor.txt"
PLAN_GLOBAL="${TMP_DIR}/plan_global.txt"

build_plan_file "${PLAN_API_KEY}" \
  "${U1K1}|${M1_1}|$((RL_API_KEY_PER_SECOND + 3))"

# user 维度：同一用户三把 key，单 key 不超过 api_key 阈值，总量超过 user 阈值。
build_plan_file "${PLAN_USER}" \
  "${U1K1}|${M1_1}|${RL_API_KEY_PER_SECOND}" \
  "${U1K2}|${M1_1}|${RL_API_KEY_PER_SECOND}" \
  "${U1K3}|${M1_1}|$((RL_USER_PER_SECOND + 2 - RL_API_KEY_PER_SECOND * 2))"

# model 维度：多用户打同一模型，单用户总量 <= user 阈值。
build_plan_file "${PLAN_MODEL}" \
  "${U1K1}|${M1_1}|$((RL_USER_PER_SECOND - 1))" \
  "${U2K1}|${M1_1}|$((RL_USER_PER_SECOND - 1))" \
  "${U3K1}|${M1_1}|$((RL_USER_PER_SECOND - 1))" \
  "${U4K1}|${M1_1}|$((RL_USER_PER_SECOND - 1))" \
  "${U5K1}|${M1_1}|$((RL_USER_PER_SECOND - 1))" \
  "${U6K1}|${M1_1}|$((RL_USER_PER_SECOND - 1))"

# vendor 维度：同一 vendor 下多模型分摊，避免先触发 model。
build_plan_file "${PLAN_VENDOR}" \
  "${U1K1}|${M1_1}|$((RL_MODEL_PER_SECOND - 2))" \
  "${U2K1}|${M1_2}|$((RL_MODEL_PER_SECOND - 2))" \
  "${U3K1}|${M1_3}|$((RL_MODEL_PER_SECOND - 2))"

# global 维度：跨 vendor 多模型分摊，避免先触发 vendor/model。
build_plan_file "${PLAN_GLOBAL}" \
  "${U1K1}|${M1_1}|$((RL_VENDOR_PER_SECOND / 2))" \
  "${U2K1}|${M1_2}|$((RL_VENDOR_PER_SECOND / 2 - 1))" \
  "${U3K1}|${M2_1}|$((RL_VENDOR_PER_SECOND / 2))" \
  "${U4K1}|${M2_2}|$((RL_VENDOR_PER_SECOND / 2 - 1))" \
  "${U5K1}|${M3_1}|$((RL_VENDOR_PER_SECOND / 2))" \
  "${U6K1}|${M3_2}|$((RL_VENDOR_PER_SECOND / 2 - 1))"

echo "==> 5) 逐个 scope 压测验证"
FAIL_COUNT=0
run_scope_case "scope_api_key" "api_key" "${PLAN_API_KEY}" || FAIL_COUNT=$((FAIL_COUNT + 1))
run_scope_case "scope_user" "user" "${PLAN_USER}" || FAIL_COUNT=$((FAIL_COUNT + 1))
run_scope_case "scope_model" "model" "${PLAN_MODEL}" || FAIL_COUNT=$((FAIL_COUNT + 1))
run_scope_case "scope_vendor" "vendor" "${PLAN_VENDOR}" || FAIL_COUNT=$((FAIL_COUNT + 1))
run_scope_case "scope_global" "global" "${PLAN_GLOBAL}" || FAIL_COUNT=$((FAIL_COUNT + 1))

echo
if [[ "${FAIL_COUNT}" -gt 0 ]]; then
  echo "==> 结果: 部分 scope 未命中（失败数=${FAIL_COUNT}）"
  echo "建议：检查限流阈值配置是否与脚本环境变量一致；若上游429过多，可只关注 X-RateLimit-Source=gateway 的结果。"
  exit 1
fi

echo "==> 结果: 五个 scope 全部命中并验证通过"
