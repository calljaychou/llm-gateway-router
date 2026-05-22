#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
用户配额扣减与 usage_logs 统计链路验收脚本。

流程:
1) 管理员登录，为调用用户发放足够额度。
2) 调用用户创建虚拟 API Key。
3) 使用虚拟 API Key 调用非流式 /v1/chat/completions。
4) 查询用户配额账户和流水，验证出现 USAGE_RESERVE，以及 USAGE_SETTLE 或 USAGE_REFUND。

前置:
- 服务已启动。
- 管理员、调用用户、模型、部门权限、供应商主密钥已准备好。
- 若上游不可用，脚本会验证失败退款链路，仍要求出现 USAGE_RESERVE 和 USAGE_REFUND。
"""

from __future__ import annotations

import argparse
from datetime import datetime, timedelta
import json
import sys
import time
import urllib.error
import urllib.parse
import urllib.request
from typing import Any, Dict, Optional, Tuple


def pretty_json(value: Any) -> str:
    return json.dumps(value, ensure_ascii=False, indent=2, sort_keys=False)


def request_json(
    method: str,
    url: str,
    body: Optional[dict] = None,
    bearer_token: Optional[str] = None,
    timeout: int = 60,
    step: str = "",
) -> Tuple[int, Dict[str, str], str]:
    headers = {"Content-Type": "application/json"}
    if bearer_token:
        headers["Authorization"] = f"Bearer {bearer_token}"
    payload = json.dumps(body, ensure_ascii=False).encode("utf-8") if body is not None else None

    print("========== REQUEST ==========")
    print(f"STEP: {step}")
    print(f"METHOD: {method.upper()}")
    print(f"URL: {url}")
    print("BODY:")
    print(pretty_json(body))

    req = urllib.request.Request(url=url, method=method.upper(), data=payload, headers=headers)
    try:
        with urllib.request.urlopen(req, timeout=timeout) as resp:
            text = resp.read().decode("utf-8", errors="replace")
            return resp.status, {k.lower(): v for k, v in resp.headers.items()}, text
    except urllib.error.HTTPError as e:
        text = e.read().decode("utf-8", errors="replace")
        return e.code, {k.lower(): v for k, v in e.headers.items()}, text


def print_response(status: int, headers: Dict[str, str], body: str) -> None:
    print("========== RESPONSE ==========")
    print(f"STATUS: {status}")
    print("HEADERS:")
    print(pretty_json(headers))
    print("BODY:")
    try:
        print(pretty_json(json.loads(body)))
    except Exception:
        print(body)


def parse_json(text: str) -> dict:
    try:
        return json.loads(text)
    except Exception:
        return {}


def api_success(step: str, status: int, body: str) -> dict:
    if status != 200:
        print(f"FAIL: {step} HTTP={status} body={body}")
        sys.exit(1)
    payload = parse_json(body)
    if payload.get("code") != 200 or payload.get("success") is not True:
        print(f"FAIL: {step} code={payload.get('code')} message={payload.get('message')}")
        sys.exit(1)
    return payload


def base_join(base_url: str, path: str, query: Optional[dict] = None) -> str:
    url = base_url.rstrip("/") + "/" + path.lstrip("/")
    if query:
        url += "?" + urllib.parse.urlencode(query)
    return url


def login(base_url: str, username: str, password: str) -> Tuple[str, int]:
    status, headers, body = request_json(
        "POST",
        base_join(base_url, "/api/auth/login"),
        {"username": username, "password": password},
        step=f"login {username}",
    )
    print_response(status, headers, body)
    payload = api_success(f"login {username}", status, body)
    return str(payload["data"]["token"]), int(payload["data"]["userId"])


def future_time(days: int) -> str:
    return (datetime.now() + timedelta(days=days)).strftime("%Y-%m-%d %H:%M:%S")


def grant_quota(base_url: str, admin_token: str, user_id: int, tokens: int) -> None:
    status, headers, body = request_json(
        "POST",
        base_join(base_url, f"/admin/users/{user_id}/quota/adjustments"),
        {"adjustTokens": tokens, "expiresAt": future_time(30), "remark": "quota usage acceptance"},
        bearer_token=admin_token,
        step="admin grant quota",
    )
    print_response(status, headers, body)
    api_success("admin grant quota", status, body)


def create_virtual_key(base_url: str, user_token: str) -> str:
    status, headers, body = request_json(
        "POST",
        base_join(base_url, "/admin/user/keys"),
        {"name": f"quota-usage-{int(time.time())}"},
        bearer_token=user_token,
        step="create virtual api key",
    )
    print_response(status, headers, body)
    payload = api_success("create virtual api key", status, body)
    return str(payload["data"]["apiKey"])


def get_current_quota(base_url: str, user_token: str) -> dict:
    status, headers, body = request_json(
        "GET",
        base_join(base_url, "/admin/user/quotas/current"),
        bearer_token=user_token,
        step="get current quota",
    )
    print_response(status, headers, body)
    return api_success("get current quota", status, body)["data"]


def list_transactions(base_url: str, user_token: str) -> list:
    status, headers, body = request_json(
        "GET",
        base_join(base_url, "/admin/user/quotas/transactions", {"pageNum": 1, "pageSize": 50}),
        bearer_token=user_token,
        step="list quota transactions",
    )
    print_response(status, headers, body)
    data = api_success("list quota transactions", status, body)["data"]
    return data.get("records") or data.get("list") or []


def call_chat(base_url: str, virtual_key: str, model_alias: str, max_tokens: int) -> int:
    status, headers, body = request_json(
        "POST",
        base_join(base_url, "/v1/chat/completions"),
        {
            "model": model_alias,
            "stream": False,
            "max_tokens": max_tokens,
            "messages": [{"role": "user", "content": "quota usage acceptance test"}],
        },
        bearer_token=virtual_key,
        step="chat completions quota usage",
    )
    print_response(status, headers, body)
    if status not in (200, 400, 401, 402, 403, 429, 500, 502, 503, 504):
        print(f"FAIL: unexpected chat status={status}")
        sys.exit(1)
    return status


def require_transaction(records: list, change_type: str) -> None:
    if not any(record.get("changeType") == change_type for record in records):
        print(f"FAIL: missing quota transaction changeType={change_type}")
        print(pretty_json(records))
        sys.exit(1)


def main() -> None:
    parser = argparse.ArgumentParser(description="验收用户配额扣减与统计链路")
    parser.add_argument("--base-url", default="http://127.0.0.1:8080")
    parser.add_argument("--admin-username", default="admin")
    parser.add_argument("--admin-password", default="123456")
    parser.add_argument("--user-username", required=True)
    parser.add_argument("--user-password", required=True)
    parser.add_argument("--model-alias", required=True)
    parser.add_argument("--grant-tokens", type=int, default=20000)
    parser.add_argument("--max-tokens", type=int, default=64)
    args = parser.parse_args()

    admin_token, _ = login(args.base_url, args.admin_username, args.admin_password)
    user_token, user_id = login(args.base_url, args.user_username, args.user_password)

    before = get_current_quota(args.base_url, user_token)
    grant_quota(args.base_url, admin_token, user_id, args.grant_tokens)
    after_grant = get_current_quota(args.base_url, user_token)
    if int(after_grant["availableTokens"]) < int(before["availableTokens"]) + args.grant_tokens:
        print("FAIL: grant quota did not increase availableTokens as expected")
        sys.exit(1)

    virtual_key = create_virtual_key(args.base_url, user_token)
    call_chat(args.base_url, virtual_key, args.model_alias, args.max_tokens)

    after_call = get_current_quota(args.base_url, user_token)
    records = list_transactions(args.base_url, user_token)
    require_transaction(records, "USAGE_RESERVE")
    if not any(record.get("changeType") in ("USAGE_SETTLE", "USAGE_REFUND") for record in records):
        print("FAIL: missing USAGE_SETTLE or USAGE_REFUND transaction")
        print(pretty_json(records))
        sys.exit(1)

    print("========== ASSERTION SUMMARY ==========")
    print(f"available before={before.get('availableTokens')}")
    print(f"available after grant={after_grant.get('availableTokens')}")
    print(f"available after call={after_call.get('availableTokens')}")
    print("PASS: user quota usage acceptance completed")


if __name__ == "__main__":
    main()
