#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
UserQuotaController API 回归脚本。

执行流程:
1) 登录管理员、转出用户、转入用户。
2) 使用管理员接口给转出用户发放两个不同过期时间的配额批次。
3) 使用用户端接口验证 current、transactions、expired-grants、transfer。

说明:
- 管理端调额要求 expiresAt > now，因此本脚本只直接造未来过期批次。
- expired-grants 用例验证接口可调用和响应结构；如需验证非空过期数据，需要先通过过期任务或数据库准备 EXPIRED 批次。
"""

from __future__ import annotations

import argparse
from datetime import datetime, timedelta
import json
import os
import sys
import urllib.error
import urllib.parse
import urllib.request
from typing import Any, Dict, List, Optional, Tuple


def load_json(path: str) -> dict:
    with open(path, "r", encoding="utf-8") as f:
        return json.load(f)


def pretty_json(value: Any) -> str:
    return json.dumps(value, ensure_ascii=False, indent=2, sort_keys=False)


def replace_placeholders(value: Any, variables: Dict[str, Any]) -> Any:
    if isinstance(value, str):
        text = value
        for key, raw in variables.items():
            text = text.replace("{" + key + "}", str(raw))
        if text.isdigit():
            return int(text)
        return text
    if isinstance(value, list):
        return [replace_placeholders(item, variables) for item in value]
    if isinstance(value, dict):
        return {key: replace_placeholders(item, variables) for key, item in value.items()}
    return value


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
    timeout: int = 30,
    print_log: bool = True,
    step: str = "",
) -> Tuple[int, Dict[str, str], str]:
    headers = {"Content-Type": "application/json"}
    if bearer_token:
        headers["Authorization"] = f"Bearer {bearer_token}"

    payload = None
    if body is not None:
        payload = json.dumps(body, ensure_ascii=False).encode("utf-8")

    if print_log:
        print("---------- REQUEST ----------")
        print(f"STEP: {step}")
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
            resp_headers = {k.lower(): v for k, v in resp.headers.items()}
            status = resp.status
    except urllib.error.HTTPError as e:
        text = e.read().decode("utf-8", errors="replace")
        resp_headers = {k.lower(): v for k, v in e.headers.items()}
        status = e.code

    if print_log:
        print("---------- RESPONSE ----------")
        print(f"STATUS: {status}")
        print("HEADERS:")
        print(pretty_json(resp_headers))
        print("BODY:")
        try:
            print(pretty_json(json.loads(text)))
        except Exception:
            print(text)

    return status, resp_headers, text


def parse_json(text: str) -> dict:
    try:
        return json.loads(text)
    except Exception:
        return {}


def dotted_get(obj: dict, path: str) -> Any:
    cur: Any = obj
    for part in path.split("."):
        if not isinstance(cur, dict):
            return None
        cur = cur.get(part)
    return cur


def assert_http_200(step: str, status: int, body: str) -> dict:
    if status != 200:
        print(f"FAIL: {step} HTTP={status} body={body}")
        sys.exit(1)
    payload = parse_json(body)
    if payload.get("code") != 200 or payload.get("success") is not True:
        print(f"FAIL: {step} code={payload.get('code')} message={payload.get('message')} body={body}")
        sys.exit(1)
    return payload


def assert_case_response(step: str, status: int, body: str, expected_status: int, expected_success: bool) -> dict:
    if status != expected_status:
        print(f"FAIL: {step} expected HTTP={expected_status}, actual HTTP={status}, body={body}")
        sys.exit(1)
    payload = parse_json(body)
    if payload.get("success") is not expected_success:
        print(f"FAIL: {step} expected success={expected_success}, body={body}")
        sys.exit(1)
    return payload


def login(base_url: str, username: str, password: str, label: str) -> Tuple[str, int]:
    status, _, body = request_json(
        "POST",
        build_url(base_url, "/api/auth/login"),
        {"username": username, "password": password},
        print_log=True,
        step=f"login {label}",
    )
    payload = assert_http_200(f"登录 {label}", status, body)
    token = dotted_get(payload, "data.token")
    user_id = dotted_get(payload, "data.userId")
    if not token or user_id is None:
        print(f"FAIL: 登录 {label} 未返回 token/userId body={body}")
        sys.exit(1)
    return str(token), int(user_id)


def future_time(days: int) -> str:
    return (datetime.now() + timedelta(days=days)).strftime("%Y-%m-%d %H:%M:%S")


def grant_quota(base_url: str, admin_token: str, user_id: int, grant_case: dict, variables: Dict[str, Any]) -> dict:
    resolved = replace_placeholders(grant_case, variables)
    body = {
        "adjustTokens": int(resolved["adjustTokens"]),
        "expiresAt": future_time(int(resolved["expiresInDays"])),
        "remark": str(resolved.get("remark") or ""),
    }
    status, _, text = request_json(
        "POST",
        build_url(base_url, f"/admin/users/{user_id}/quota/adjustments"),
        body,
        bearer_token=admin_token,
        print_log=True,
        step=f"grant quota: {resolved.get('name')}",
    )
    return assert_http_200(f"管理员发放配额 {resolved.get('name')}", status, text)


def validate_account_payload(step: str, payload: dict) -> dict:
    data = payload.get("data")
    if not isinstance(data, dict):
        print(f"FAIL: {step} data不是对象 payload={pretty_json(payload)}")
        sys.exit(1)
    required = ["userId", "currentQuotaTokens", "availableTokens", "usedTokens", "expiredTokens", "activeGrants"]
    missing = [key for key in required if key not in data]
    if missing:
        print(f"FAIL: {step} 缺少字段 {missing}, data={pretty_json(data)}")
        sys.exit(1)
    return data


def validate_page_payload(step: str, payload: dict) -> dict:
    data = payload.get("data")
    if not isinstance(data, dict):
        print(f"FAIL: {step} data不是对象 payload={pretty_json(payload)}")
        sys.exit(1)
    required = ["pageNum", "pageSize", "total", "records"]
    missing = [key for key in required if key not in data]
    if missing:
        print(f"FAIL: {step} 缺少分页字段 {missing}, data={pretty_json(data)}")
        sys.exit(1)
    if not isinstance(data.get("records"), list):
        print(f"FAIL: {step} records不是数组 data={pretty_json(data)}")
        sys.exit(1)
    return data


def actor_token(case: dict, from_token: str, target_token: str) -> str:
    actor = str(case.get("actor") or "from_user")
    if actor == "from_user":
        return from_token
    if actor == "target_user":
        return target_token
    print(f"FAIL: 未知actor={actor}")
    sys.exit(1)


def run_api_case(base_url: str, case: dict, variables: Dict[str, Any], from_token: str, target_token: str) -> dict:
    resolved = replace_placeholders(case, variables)
    query = resolved.get("query") or {}
    body = resolved.get("body")
    status, _, text = request_json(
        str(resolved["method"]),
        build_url(base_url, str(resolved["path"]), query),
        body if isinstance(body, dict) else None,
        bearer_token=actor_token(resolved, from_token, target_token),
        print_log=True,
        step=str(resolved.get("name")),
    )
    payload = assert_case_response(
        str(resolved.get("name")),
        status,
        text,
        int(resolved.get("expected_status", 200)),
        bool(resolved.get("expected_success", True)),
    )

    path = str(resolved["path"])
    if path.endswith("/current"):
        validate_account_payload(str(resolved.get("name")), payload)
    if path.endswith("/transactions") or path.endswith("/expired-grants"):
        validate_page_payload(str(resolved.get("name")), payload)
    if path.endswith("/transfer"):
        data = payload.get("data") or {}
        for field in ["fromUserId", "targetUserId", "transferTokens", "fromAccount", "targetAccount"]:
            if field not in data:
                print(f"FAIL: 转配响应缺少字段 {field}, data={pretty_json(data)}")
                sys.exit(1)
    print(f"PASS: {resolved.get('name')}")
    return payload


def main() -> None:
    parser = argparse.ArgumentParser(description="回归验证 UserQuotaController API")
    parser.add_argument(
        "--cases",
        default=os.path.join(os.path.dirname(__file__), "user_quota_regression_cases.json"),
        help="测试场景JSON路径",
    )
    parser.add_argument("--base-url", default=None, help="覆盖JSON中的base_url")
    args = parser.parse_args()

    config = load_json(args.cases)
    variables: Dict[str, Any] = dict(config.get("variables") or {})
    if args.base_url:
        variables["base_url"] = args.base_url

    base_url = str(variables["base_url"])
    print("========== USER QUOTA REGRESSION START ==========")
    print(f"BASE_URL: {base_url}")

    admin_token, admin_user_id = login(
        base_url,
        str(variables["admin_username"]),
        str(variables["admin_password"]),
        "admin",
    )
    from_token, from_user_id = login(
        base_url,
        str(variables["from_username"]),
        str(variables["from_password"]),
        "from_user",
    )
    target_token, target_user_id = login(
        base_url,
        str(variables["target_username"]),
        str(variables["target_password"]),
        "target_user",
    )
    variables.update(
        {
            "admin_user_id": admin_user_id,
            "from_user_id": from_user_id,
            "target_user_id": target_user_id,
        }
    )

    print("========== PREPARE QUOTA GRANTS ==========")
    expected_granted = 0
    for grant_case in config.get("grant_cases") or []:
        resolved = replace_placeholders(grant_case, variables)
        expected_granted += int(resolved["adjustTokens"])
        grant_quota(base_url, admin_token, from_user_id, grant_case, variables)

    print("========== VERIFY USER QUOTA API ==========")
    responses: Dict[str, dict] = {}
    for api_case in config.get("api_cases") or []:
        name = str(api_case.get("name"))
        responses[name] = run_api_case(base_url, api_case, variables, from_token, target_token)

    current = responses.get("from_user_current_quota", {}).get("data") or {}
    if int(current.get("availableTokens", 0)) < expected_granted:
        print(
            "FAIL: 发放后转出用户可用额度小于本次发放额度, "
            f"available={current.get('availableTokens')}, expected_at_least={expected_granted}"
        )
        sys.exit(1)

    target_current = responses.get("target_user_current_quota_after_transfer", {}).get("data") or {}
    transfer_tokens = int(variables["transfer_tokens"])
    if int(target_current.get("availableTokens", 0)) < transfer_tokens:
        print(
            "FAIL: 转入用户可用额度小于本次转配额度, "
            f"available={target_current.get('availableTokens')}, expected_at_least={transfer_tokens}"
        )
        sys.exit(1)

    print("========== USER QUOTA REGRESSION PASS ==========")


if __name__ == "__main__":
    main()
