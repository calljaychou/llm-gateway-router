#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
用于验证网关五个限流维度的脚本:
- api_key
- user
- model
- vendor
- global

说明:
- 脚本会自动创建供应商、模型、测试用户、虚拟 API Key，并执行并发请求。
- 判定标准为: 至少出现一条 `HTTP 429 + X-RateLimit-Source=gateway + X-RateLimit-Scope=<expected>`。
- 若上游 OpenAI 返回 429，会被统计为 upstream，不作为 scope 判定依据。
"""

from __future__ import annotations

import argparse
import json
import math
import os
import sys
import time
import urllib.error
import urllib.request
from concurrent.futures import ThreadPoolExecutor, as_completed
from dataclasses import dataclass
from typing import Dict, List, Optional, Tuple


def now_suffix() -> str:
    return str(int(time.time()))


def request_json(
    method: str,
    url: str,
    body: Optional[dict] = None,
    bearer_token: Optional[str] = None,
    timeout: int = 30,
) -> Tuple[int, Dict[str, str], str]:
    """发送 JSON HTTP 请求并返回 (status, headers_lower, body_text)。"""
    payload = None
    headers = {"Content-Type": "application/json"}
    if bearer_token:
        headers["Authorization"] = f"Bearer {bearer_token}"
    if body is not None:
        payload = json.dumps(body, ensure_ascii=False).encode("utf-8")

    req = urllib.request.Request(url=url, method=method.upper(), data=payload, headers=headers)
    try:
        with urllib.request.urlopen(req, timeout=timeout) as resp:
            resp_body = resp.read().decode("utf-8", errors="replace")
            resp_headers = {k.lower(): v for k, v in resp.headers.items()}
            return resp.status, resp_headers, resp_body
    except urllib.error.HTTPError as e:
        body_text = e.read().decode("utf-8", errors="replace")
        resp_headers = {k.lower(): v for k, v in e.headers.items()}
        return e.code, resp_headers, body_text


def parse_json(text: str) -> dict:
    """解析 JSON 字符串，失败返回空 dict。"""
    try:
        return json.loads(text)
    except Exception:
        return {}


def require_200(step: str, status: int, body: str) -> None:
    """校验 HTTP=200，不满足则直接退出。"""
    if status != 200:
        print(f"ERROR: {step} 失败，HTTP={status}\n响应={body}")
        sys.exit(1)


def dotted_get(obj: dict, path: str) -> Optional[object]:
    """按点路径读取字典字段，如 data.token。"""
    cur: object = obj
    for part in path.split("."):
        if isinstance(cur, dict):
            cur = cur.get(part)
        else:
            return None
    return cur


def login(base_url: str, username: str, password: str) -> str:
    """登录并返回 token。"""
    status, _, body = request_json(
        "POST",
        f"{base_url}/api/auth/login",
        {"username": username, "password": password},
    )
    require_200(f"登录 {username}", status, body)
    token = dotted_get(parse_json(body), "data.token")
    if not token:
        print(f"ERROR: 登录 {username} 未返回 token")
        sys.exit(1)
    return str(token)


def create_vendor(base_url: str, admin_token: str, name: str, openai_base_url: str) -> int:
    """创建供应商并返回 vendorId。"""
    status, _, body = request_json(
        "POST",
        f"{base_url}/admin/vendors",
        {"name": name, "baseUrl": openai_base_url, "status": 1},
        admin_token,
    )
    require_200(f"创建供应商 {name}", status, body)
    vendor_id = dotted_get(parse_json(body), "data.vendorId")
    if vendor_id is None:
        print(f"ERROR: 供应商 {name} 未返回 vendorId")
        sys.exit(1)
    return int(vendor_id)


def create_master_key(base_url: str, admin_token: str, vendor_id: int, master_api_key: str) -> None:
    """为供应商创建主密钥。"""
    status, _, body = request_json(
        "POST",
        f"{base_url}/admin/master-keys",
        {"vendorId": vendor_id, "apiKey": master_api_key, "weight": 10, "status": 1},
        admin_token,
    )
    require_200(f"创建主密钥 vendor={vendor_id}", status, body)


def create_model(base_url: str, admin_token: str, alias: str, real_model_name: str, vendor_id: int) -> None:
    """创建模型映射。"""
    status, _, body = request_json(
        "POST",
        f"{base_url}/admin/models",
        {
            "modelAlias": alias,
            "realModelName": real_model_name,
            "vendorId": vendor_id,
            "billingType": "PAID",
            "active": True,
        },
        admin_token,
    )
    require_200(f"创建模型 {alias}", status, body)


def create_user(base_url: str, admin_token: str, dept_id: int, username: str, email: str, password: str) -> None:
    """创建测试用户。"""
    status, _, body = request_json(
        "POST",
        f"{base_url}/admin/users",
        {
            "username": username,
            "email": email,
            "deptId": dept_id,
            "roleKeys": ["common"],
            "password": password,
            "forcePasswordChange": False,
        },
        admin_token,
    )
    require_200(f"创建用户 {username}", status, body)


def create_user_vkey(base_url: str, user_token: str, name: str) -> str:
    """为用户创建虚拟 API Key。"""
    status, _, body = request_json(
        "POST",
        f"{base_url}/admin/user/keys",
        {"name": name},
        user_token,
    )
    require_200(f"创建虚拟Key {name}", status, body)
    key = dotted_get(parse_json(body), "data.apiKey")
    if not key:
        print(f"ERROR: 虚拟Key {name} 未返回 apiKey")
        sys.exit(1)
    return str(key)


def grant_dept_permissions(base_url: str, admin_token: str, dept_id: int, aliases: List[str]) -> None:
    """把模型授权到部门。"""
    items = [{"modelAlias": a, "scope": "SUBTREE"} for a in aliases]
    status, _, body = request_json(
        "PUT",
        f"{base_url}/admin/departments/{dept_id}/permissions",
        {"items": items},
        admin_token,
    )
    require_200("部门授权模型", status, body)


def post_chat(base_url: str, virtual_key: str, model_alias: str, timeout: int = 45) -> Tuple[int, Dict[str, str], str]:
    """调用 /v1/chat/completions。"""
    return request_json(
        "POST",
        f"{base_url}/v1/chat/completions",
        {
            "model": model_alias,
            "temperature": 0.1,
            "messages": [{"role": "user", "content": "rate limit scope test"}],
        },
        bearer_token=virtual_key,
        timeout=timeout,
    )


@dataclass
class ScopeCase:
    name: str
    expected_scope: str
    plan: List[Tuple[str, str, int]]  # (virtual_key, model_alias, repeat)


def run_scope_case(base_url: str, case: ScopeCase) -> bool:
    """并发执行单个 scope 用例并判断是否命中目标维度。"""
    tasks: List[Tuple[str, str]] = []
    for key, model, repeat in case.plan:
        for _ in range(max(0, repeat)):
            tasks.append((key, model))

    if not tasks:
        print(f"[{case.name}] FAIL: 计划为空")
        return False

    gateway_429 = 0
    upstream_429 = 0
    expected_hit = 0
    total = len(tasks)

    with ThreadPoolExecutor(max_workers=min(120, max(8, total))) as pool:
        futures = [pool.submit(post_chat, base_url, key, model) for key, model in tasks]
        for f in as_completed(futures):
            try:
                status, headers, _ = f.result()
            except Exception:
                continue
            source = headers.get("x-ratelimit-source", "").strip().lower()
            scope = headers.get("x-ratelimit-scope", "").strip().lower()
            if status == 429 and source == "gateway":
                gateway_429 += 1
                if scope == case.expected_scope:
                    expected_hit += 1
            if status == 429 and source == "upstream":
                upstream_429 += 1

    print(
        f"[{case.name}] total={total} gateway429={gateway_429} "
        f"upstream429={upstream_429} expectedScopeHit={expected_hit}"
    )
    ok = expected_hit > 0
    print(f"[{case.name}] {'PASS' if ok else 'FAIL'}")
    return ok


def distribute(total: int, buckets: int) -> List[int]:
    """把 total 尽量均匀分配到 buckets 个桶。"""
    base = total // buckets
    rem = total % buckets
    arr = [base] * buckets
    for i in range(rem):
        arr[i] += 1
    return arr


def main() -> None:
    parser = argparse.ArgumentParser(description="验证网关限流五个 scope")
    parser.add_argument("--base-url", default="http://127.0.0.1:8080")
    parser.add_argument("--admin-username", default="admin")
    parser.add_argument("--admin-password", default="123456")
    parser.add_argument("--dept-id", type=int, default=1)
    parser.add_argument("--openai-base-url", default="https://api.openai.com")
    parser.add_argument("--master-api-key", required=True)
    parser.add_argument("--real-model-name", default="gpt-4o-mini")
    args = parser.parse_args()

    # 可通过环境变量覆盖阈值，默认与 application-dev.yaml 一致。
    rl_api_key = int(os.getenv("RL_API_KEY_PER_SECOND", "3"))
    rl_user = int(os.getenv("RL_USER_PER_SECOND", "5"))
    rl_model = int(os.getenv("RL_MODEL_PER_SECOND", "20"))
    rl_vendor = int(os.getenv("RL_VENDOR_PER_SECOND", "50"))
    rl_global = int(os.getenv("RL_GLOBAL_PER_SECOND", "100"))

    if min(rl_api_key, rl_user, rl_model, rl_vendor, rl_global) <= 0:
        print("ERROR: 限流阈值必须为正整数")
        sys.exit(1)
    if rl_model <= 1:
        print("ERROR: RL_MODEL_PER_SECOND 需大于 1，才能构造不触发 model 的 vendor/global 用例")
        sys.exit(1)

    suffix = now_suffix()
    print("==> 限流 Scope 验证(Python)")
    print(f"BASE_URL={args.base_url}")
    print(
        "阈值(api_key/user/model/vendor/global)="
        f"({rl_api_key}/{rl_user}/{rl_model}/{rl_vendor}/{rl_global})"
    )

    print("\n==> 1) 管理员登录")
    admin_token = login(args.base_url, args.admin_username, args.admin_password)
    print("管理员登录成功")

    # 为了避免 vendor/global 用例先触发 model，动态计算每个 vendor 需要的模型数。
    total_vendor_target = rl_vendor + 6
    total_global_target = rl_global + 12
    per_vendor_for_global = math.ceil(total_global_target / 3)
    models_per_vendor = max(
        3,
        math.ceil(total_vendor_target / (rl_model - 1)),
        math.ceil(per_vendor_for_global / (rl_model - 1)),
    )

    print(f"\n==> 2) 创建供应商与模型 (vendors=3, models_per_vendor={models_per_vendor})")
    vendor_ids: List[int] = []
    models_by_vendor: Dict[int, List[str]] = {}
    all_models: List[str] = []
    for vi in range(1, 4):
        vendor_name = f"RL-Vendor-{suffix}-{vi}"
        vendor_id = create_vendor(args.base_url, admin_token, vendor_name, args.openai_base_url)
        create_master_key(args.base_url, admin_token, vendor_id, args.master_api_key)
        vendor_ids.append(vendor_id)
        models = []
        for mi in range(1, models_per_vendor + 1):
            alias = f"rl-model-v{vi}-m{mi}-{suffix}"
            create_model(args.base_url, admin_token, alias, args.real_model_name, vendor_id)
            models.append(alias)
            all_models.append(alias)
        models_by_vendor[vendor_id] = models
    print("供应商与模型创建完成")

    # 每用户最大可压请求数（不触发 user/api_key）
    keys_per_user = max(3, math.ceil(rl_user / rl_api_key))
    per_user_capacity = min(rl_user, rl_api_key * keys_per_user)
    user_count = max(8, math.ceil((total_global_target + 5) / max(1, per_user_capacity)))

    print(f"\n==> 3) 创建测试用户与虚拟Key (users={user_count}, keys_per_user={keys_per_user})")
    usernames: List[str] = []
    passwords: List[str] = []
    user_tokens: List[str] = []
    user_keys: Dict[int, List[str]] = {}

    for i in range(1, user_count + 1):
        username = f"rl_user_{suffix}_{i}"
        password = "Temp@123456"
        email = f"rl_user_{suffix}_{i}@example.com"
        create_user(args.base_url, admin_token, args.dept_id, username, email, password)
        usernames.append(username)
        passwords.append(password)

    grant_dept_permissions(args.base_url, admin_token, args.dept_id, all_models)

    for idx, (username, password) in enumerate(zip(usernames, passwords)):
        token = login(args.base_url, username, password)
        user_tokens.append(token)
        keys = []
        for k in range(1, keys_per_user + 1):
            key_name = f"rl-key-u{idx + 1}-k{k}-{suffix}"
            keys.append(create_user_vkey(args.base_url, token, key_name))
        user_keys[idx] = keys
    print("测试用户与虚拟Key创建完成")

    # 快捷引用
    v1, v2, v3 = vendor_ids
    models_v1 = models_by_vendor[v1]
    models_v2 = models_by_vendor[v2]
    models_v3 = models_by_vendor[v3]

    # 用例1: api_key（单 key 直接超）
    case_api_key = ScopeCase(
        name="scope_api_key",
        expected_scope="api_key",
        plan=[(user_keys[0][0], models_v1[0], rl_api_key + 3)],
    )

    # 用例2: user（同用户多 key 汇总超）
    user_target = rl_user + 3
    remaining = user_target
    user_plan: List[Tuple[str, str, int]] = []
    for key in user_keys[0]:
        if remaining <= 0:
            break
        cnt = min(rl_api_key, remaining)
        user_plan.append((key, models_v1[0], cnt))
        remaining -= cnt
    case_user = ScopeCase("scope_user", "user", user_plan)

    # 用例3: model（多用户同模型，总量超 model，单用户不超 user，单 key 不超 api_key）
    model_target = rl_model + 6
    model_plan: List[Tuple[str, str, int]] = []
    left = model_target
    uidx = 0
    while left > 0 and uidx < user_count:
        per_user_take = min(per_user_capacity, left)
        remain_for_user = per_user_take
        for key in user_keys[uidx]:
            if remain_for_user <= 0:
                break
            c = min(rl_api_key, remain_for_user)
            model_plan.append((key, models_v1[0], c))
            remain_for_user -= c
        left -= per_user_take
        uidx += 1
    case_model = ScopeCase("scope_model", "model", model_plan)

    # 用例4: vendor（同 vendor 多模型，总量超 vendor，每模型控制在 model 阈值内）
    vendor_target = rl_vendor + 6
    vendor_plan: List[Tuple[str, str, int]] = []
    vendor_model_distribution = distribute(vendor_target, len(models_v1))
    cursor_user = 0
    for midx, cnt_for_model in enumerate(vendor_model_distribution):
        left_cnt = cnt_for_model
        while left_cnt > 0 and cursor_user < user_count:
            take_user = min(per_user_capacity, left_cnt)
            remain_for_user = take_user
            for key in user_keys[cursor_user]:
                if remain_for_user <= 0:
                    break
                c = min(rl_api_key, remain_for_user)
                vendor_plan.append((key, models_v1[midx], c))
                remain_for_user -= c
            left_cnt -= take_user
            cursor_user += 1
    case_vendor = ScopeCase("scope_vendor", "vendor", vendor_plan)

    # 用例5: global（跨 vendor+model 打散，总量超 global，尽量不触发 vendor/model）
    global_target = rl_global + 12
    all_models_round_robin = models_v1 + models_v2 + models_v3
    global_distribution = distribute(global_target, len(all_models_round_robin))
    global_plan: List[Tuple[str, str, int]] = []
    cursor_user = 0
    for midx, cnt_for_model in enumerate(global_distribution):
        left_cnt = cnt_for_model
        while left_cnt > 0 and cursor_user < user_count:
            take_user = min(per_user_capacity, left_cnt)
            remain_for_user = take_user
            for key in user_keys[cursor_user]:
                if remain_for_user <= 0:
                    break
                c = min(rl_api_key, remain_for_user)
                global_plan.append((key, all_models_round_robin[midx], c))
                remain_for_user -= c
            left_cnt -= take_user
            cursor_user += 1
    case_global = ScopeCase("scope_global", "global", global_plan)

    print("\n==> 4) 开始逐个 scope 验证")
    cases = [case_api_key, case_user, case_model, case_vendor, case_global]
    failed = 0
    for c in cases:
        ok = run_scope_case(args.base_url, c)
        if not ok:
            failed += 1

    print()
    if failed > 0:
        print(f"==> 结果: 失败 {failed} 个用例")
        print("建议: 确认 RL_* 环境变量与服务端限流配置一致，并只关注 X-RateLimit-Source=gateway 的429。")
        sys.exit(1)
    print("==> 结果: 五个 scope 全部验证通过")


if __name__ == "__main__":
    main()
