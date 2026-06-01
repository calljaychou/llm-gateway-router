#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
用户金额配额链路 API 验证脚本。

流程:
1) 管理员登录，为调用用户发放金额配额。
2) 调用用户查询当前配额，验证 availableAmount 增加。
3) 可选：创建虚拟 API Key 并调用 /v1/chat/completions。
4) 查询配额流水，验证金额字段和 USAGE_RESERVE/USAGE_SETTLE/USAGE_REFUND。

前置:
- 服务已启动。
- 管理员、调用用户、模型权限、供应商主密钥已准备好。
- 若开启 --skip-chat，则只验证调额、账户快照、流水字段。
"""

from __future__ import annotations

import argparse
from datetime import datetime, timedelta
from decimal import Decimal
import json
import sys
import time
import urllib.error
import urllib.parse
import urllib.request
from typing import Any, Dict, Optional, Tuple


def pretty_json(value: Any) -> str:
    return json.dumps(value, ensure_ascii=False, indent=2, sort_keys=False)


def mask_headers(headers: Dict[str, str]) -> Dict[str, str]:
    masked: Dict[str, str] = {}
    for key, value in headers.items():
        if key.lower() == "authorization":
            masked[key] = value[:18] + "..." if len(value) > 18 else "***"
        else:
            masked[key] = value
    return masked


def build_url(base_url: str, path: str, query: Optional[dict] = None) -> str:
    url = base_url.rstrip("/") + "/" + path.lstrip("/")
    if query:
        url += "?" + urllib.parse.urlencode(query)
    return url


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

    print("========== CASE START ==========")
    print(f"STEP: {step}")
    print("---------- REQUEST ----------")
    print(f"METHOD: {method.upper()}")
    print(f"URL: {url}")
    print("HEADERS:")
    print(pretty_json(mask_headers(headers)))
    print("BODY:")
    print(pretty_json(body))

    req = urllib.request.Request(url=url, method=method.upper(), data=payload, headers=headers)
    try:
        with urllib.request.urlopen(req, timeout=timeout) as resp:
            text = resp.read().decode("utf-8", errors="replace")
            response_headers = {k.lower(): v for k, v in resp.headers.items()}
            status = resp.status
    except urllib.error.HTTPError as exc:
        text = exc.read().decode("utf-8", errors="replace")
        response_headers = {k.lower(): v for k, v in exc.headers.items()}
        status = exc.code

    print("---------- RESPONSE ----------")
    print(f"STATUS: {status}")
    print("HEADERS:")
    print(pretty_json(response_headers))
    print("BODY:")
    try:
        print(pretty_json(json.loads(text)))
    except Exception:
        print(text)
    print("========== CASE END ==========")
    return status, response_headers, text


def parse_json(text: str) -> dict:
    try:
        parsed = json.loads(text)
        return parsed if isinstance(parsed, dict) else {}
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


def decimal_value(value: Any) -> Decimal:
    if value is None:
        return Decimal("0")
    return Decimal(str(value))


def login(base_url: str, username: str, password: str) -> Tuple[str, int]:
    status, _, body = request_json(
        "POST",
        build_url(base_url, "/api/auth/login"),
        {"username": username, "password": password},
        step=f"login {username}",
    )
    payload = api_success(f"login {username}", status, body)
    return str(payload["data"]["token"]), int(payload["data"]["userId"])


def future_time(days: int) -> str:
    return (datetime.now() + timedelta(days=days)).strftime("%Y-%m-%d %H:%M:%S")


def grant_quota_amount(base_url: str, admin_token: str, user_id: int, amount: Decimal) -> None:
    status, _, body = request_json(
        "POST",
        build_url(base_url, f"/admin/users/{user_id}/quota/adjustments"),
        {
            "adjustAmount": str(amount),
            "expiresAt": future_time(30),
            "remark": "quota amount acceptance",
        },
        bearer_token=admin_token,
        step="admin grant quota amount",
    )
    api_success("admin grant quota amount", status, body)


def create_virtual_key(base_url: str, user_token: str) -> str:
    status, _, body = request_json(
        "POST",
        build_url(base_url, "/admin/user/keys"),
        {"name": f"quota-amount-{int(time.time())}"},
        bearer_token=user_token,
        step="create virtual api key",
    )
    payload = api_success("create virtual api key", status, body)
    return str(payload["data"]["apiKey"])


def get_current_quota(base_url: str, user_token: str) -> dict:
    status, _, body = request_json(
        "GET",
        build_url(base_url, "/admin/user/quotas/current"),
        bearer_token=user_token,
        step="get current quota",
    )
    data = api_success("get current quota", status, body)["data"]
    require_fields(
        "current quota",
        data,
        ["currentQuotaAmount", "availableAmount", "usedAmount", "expiredAmount", "activeGrants"],
    )
    return data


def list_transactions(base_url: str, user_token: str) -> list:
    status, _, body = request_json(
        "GET",
        build_url(base_url, "/admin/user/quotas/transactions", {"pageNum": 1, "pageSize": 50}),
        bearer_token=user_token,
        step="list quota transactions",
    )
    data = api_success("list quota transactions", status, body)["data"]
    records = data.get("list")
    if not isinstance(records, list):
        print(f"FAIL: quota transactions response missing list, data={pretty_json(data)}")
        sys.exit(1)
    return records


def call_chat(base_url: str, virtual_key: str, model_alias: str, max_tokens: int) -> int:
    status, _, _ = request_json(
        "POST",
        build_url(base_url, "/v1/chat/completions"),
        {
            "model": model_alias,
            "stream": False,
            "max_tokens": max_tokens,
            "messages": [{"role": "user", "content": "quota amount acceptance test"}],
        },
        bearer_token=virtual_key,
        step="chat completions quota amount",
    )
    if status not in (200, 400, 401, 402, 403, 429, 500, 502, 503, 504):
        print(f"FAIL: unexpected chat status={status}")
        sys.exit(1)
    return status


def require_fields(step: str, value: dict, fields: list[str]) -> None:
    missing = [field for field in fields if field not in value]
    if missing:
        print(f"FAIL: {step} missing fields={missing}, value={pretty_json(value)}")
        sys.exit(1)


def require_transaction(records: list, change_type: str) -> None:
    if not any(record.get("changeType") == change_type for record in records):
        print(f"FAIL: missing quota transaction changeType={change_type}")
        print(pretty_json(records))
        sys.exit(1)


def require_amount_transaction_fields(records: list) -> None:
    required = ["deltaAmount", "quotaBeforeAmount", "quotaAfterAmount", "availableBeforeAmount", "availableAfterAmount"]
    for record in records:
        require_fields(f"transaction {record.get('transactionId')}", record, required)


def main() -> None:
    parser = argparse.ArgumentParser(description="验收用户金额配额链路")
    parser.add_argument("--base-url", default="http://127.0.0.1:8080")
    parser.add_argument("--admin-username", default="admin")
    parser.add_argument("--admin-password", default="123456")
    parser.add_argument("--user-username", required=True)
    parser.add_argument("--user-password", required=True)
    parser.add_argument("--model-alias", default="")
    parser.add_argument("--grant-amount", default="100.000000")
    parser.add_argument("--max-tokens", type=int, default=64)
    parser.add_argument("--skip-chat", action="store_true")
    args = parser.parse_args()

    grant_amount = Decimal(args.grant_amount)
    admin_token, _ = login(args.base_url, args.admin_username, args.admin_password)
    user_token, user_id = login(args.base_url, args.user_username, args.user_password)

    before = get_current_quota(args.base_url, user_token)
    grant_quota_amount(args.base_url, admin_token, user_id, grant_amount)
    after_grant = get_current_quota(args.base_url, user_token)

    before_available = decimal_value(before.get("availableAmount"))
    after_available = decimal_value(after_grant.get("availableAmount"))
    if after_available < before_available + grant_amount:
        print(
            "FAIL: grant quota did not increase availableAmount as expected, "
            f"before={before_available}, after={after_available}, grant={grant_amount}"
        )
        sys.exit(1)

    if not args.skip_chat:
        if not args.model_alias:
            print("FAIL: --model-alias is required when --skip-chat is not set")
            sys.exit(1)
        virtual_key = create_virtual_key(args.base_url, user_token)
        call_chat(args.base_url, virtual_key, args.model_alias, args.max_tokens)

    records = list_transactions(args.base_url, user_token)
    require_amount_transaction_fields(records)
    require_transaction(records, "ADMIN_GRANT")
    if not args.skip_chat:
        require_transaction(records, "USAGE_RESERVE")
        if not any(record.get("changeType") in ("USAGE_SETTLE", "USAGE_REFUND") for record in records):
            print("FAIL: missing USAGE_SETTLE or USAGE_REFUND transaction")
            print(pretty_json(records))
            sys.exit(1)

    print("========== ASSERTION SUMMARY ==========")
    print(f"available before={before_available}")
    print(f"available after grant={after_available}")
    print(f"grant amount={grant_amount}")
    print("PASS: user quota amount acceptance completed")


if __name__ == "__main__":
    main()
