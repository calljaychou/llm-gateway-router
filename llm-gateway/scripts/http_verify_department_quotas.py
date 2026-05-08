#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
US-004 部门配额管理接口验证脚本。

验证点:
1) 分页查询 GET /admin/department-quotas
2) 按部门查询 GET /admin/department-quotas/{deptId}
3) 新增 POST /admin/department-quotas
4) 更新 PUT /admin/department-quotas/{deptId}
5) 状态更新 PUT /admin/department-quotas/{deptId}/status
"""

from __future__ import annotations

import argparse
from datetime import datetime, timedelta, timezone
import json
import random
import sys
import time
import urllib.error
import urllib.request
from typing import Dict, Optional, Tuple


def request_json(
    method: str,
    url: str,
    body: Optional[dict] = None,
    bearer_token: Optional[str] = None,
    timeout: int = 20,
) -> Tuple[int, Dict[str, str], str]:
    payload = None
    headers = {"Content-Type": "application/json"}
    if bearer_token:
        headers["Authorization"] = f"Bearer {bearer_token}"
    if body is not None:
        payload = json.dumps(body, ensure_ascii=False).encode("utf-8")

    req = urllib.request.Request(url=url, method=method.upper(), data=payload, headers=headers)
    try:
        with urllib.request.urlopen(req, timeout=timeout) as resp:
            return resp.status, {k.lower(): v for k, v in resp.headers.items()}, resp.read().decode("utf-8", "replace")
    except urllib.error.HTTPError as e:
        return e.code, {k.lower(): v for k, v in e.headers.items()}, e.read().decode("utf-8", "replace")


def parse_json(text: str) -> dict:
    try:
        return json.loads(text)
    except Exception:
        return {}


def assert_http_200(step: str, status: int, body: str) -> None:
    if status != 200:
        print(f"FAIL: {step} HTTP={status} body={body}")
        sys.exit(1)


def assert_api_success(step: str, payload: dict) -> None:
    if payload.get("code") != 200 or payload.get("success") is not True:
        print(f"FAIL: {step} code={payload.get('code')} message={payload.get('message')}")
        sys.exit(1)


def normalize_expected_updated_at(raw_value: object) -> int:
    """将 updatedAt 归一化为毫秒时间戳，兼容 int/float/"yyyy-MM-dd HH:mm:ss"/ISO8601。"""
    if isinstance(raw_value, int):
        return raw_value
    if isinstance(raw_value, float):
        return int(raw_value)
    if isinstance(raw_value, str):
        text = raw_value.strip()
        if text.isdigit():
            return int(text)
        # 服务端 JsonFormat 配置的常见格式
        try:
            dt = datetime.strptime(text, "%Y-%m-%d %H:%M:%S").replace(tzinfo=timezone(timedelta(hours=8)))
            return int(dt.timestamp() * 1000)
        except ValueError:
            pass
        # 兜底 ISO8601
        try:
            dt = datetime.fromisoformat(text.replace("Z", "+00:00"))
            return int(dt.timestamp() * 1000)
        except ValueError:
            pass
    print(f"FAIL: 无法解析updatedAt={raw_value!r}")
    sys.exit(1)


def login(base_url: str, username: str, password: str) -> str:
    status, _, body = request_json(
        "POST",
        f"{base_url}/api/auth/login",
        {"username": username, "password": password},
    )
    assert_http_200("管理员登录", status, body)
    payload = parse_json(body)
    assert_api_success("管理员登录", payload)
    token = payload.get("data", {}).get("token")
    if not token:
        print(f"FAIL: 管理员登录未返回token body={body}")
        sys.exit(1)
    return str(token)


def get_quota_detail(base_url: str, admin_token: str, dept_id: int) -> Optional[dict]:
    status, _, body = request_json("GET", f"{base_url}/admin/department-quotas/{dept_id}", bearer_token=admin_token)
    assert_http_200("查询部门配额详情", status, body)
    payload = parse_json(body)
    if payload.get("code") == 200 and payload.get("success") is True:
        return payload.get("data") or {}
    return None


def main() -> None:
    parser = argparse.ArgumentParser(description="验证 US-004 部门配额管理接口")
    parser.add_argument("--base-url", default="http://127.0.0.1:8080")
    parser.add_argument("--admin-username", default="admin")
    parser.add_argument("--admin-password", default="123456")
    parser.add_argument("--dept-id", type=int, default=1)
    args = parser.parse_args()

    print("==> 1) 管理员登录")
    admin_token = login(args.base_url, args.admin_username, args.admin_password)
    print("登录成功")

    print("==> 2) 分页查询")
    status, _, body = request_json(
        "GET",
        f"{args.base_url}/admin/department-quotas?deptId={args.dept_id}&pageNum=1&pageSize=10",
        bearer_token=admin_token,
    )
    assert_http_200("分页查询", status, body)
    payload = parse_json(body)
    assert_api_success("分页查询", payload)
    print(f"分页查询成功, total={payload.get('data', {}).get('total')}")

    print("==> 3) 查询/创建策略")
    detail = get_quota_detail(args.base_url, admin_token, args.dept_id)
    if detail is None:
        create_body = {
            "deptId": args.dept_id,
            "quotaTokens": random.randint(10000, 20000),
            "period": "MONTHLY",
            "status": 1,
            "remark": f"auto-create-{int(time.time())}",
        }
        status, _, body = request_json(
            "POST",
            f"{args.base_url}/admin/department-quotas",
            body=create_body,
            bearer_token=admin_token,
        )
        assert_http_200("创建策略", status, body)
        payload = parse_json(body)
        assert_api_success("创建策略", payload)
        print("创建策略成功")
        detail = get_quota_detail(args.base_url, admin_token, args.dept_id)
        if detail is None:
            print("FAIL: 创建后仍无法查询到策略")
            sys.exit(1)
    else:
        print("策略已存在，跳过创建")

    print("==> 4) 更新策略")
    expected_updated_at = normalize_expected_updated_at(detail.get("updatedAt"))
    update_body = {
        "quotaTokens": random.randint(20001, 30000),
        "period": "FOREVER" if detail.get("period") == "MONTHLY" else "MONTHLY",
        "status": 1,
        "expectedUpdatedAt": expected_updated_at,
        "remark": f"auto-update-{int(time.time())}",
    }
    status, _, body = request_json(
        "PUT",
        f"{args.base_url}/admin/department-quotas/{args.dept_id}",
        body=update_body,
        bearer_token=admin_token,
    )
    assert_http_200("更新策略", status, body)
    payload = parse_json(body)
    assert_api_success("更新策略", payload)
    latest_updated_at = normalize_expected_updated_at(payload.get("data", {}).get("updatedAt"))
    print("更新策略成功")

    print("==> 5) 更新策略状态")
    status_body = {
        "status": 0 if detail.get("status") == 1 else 1,
        "expectedUpdatedAt": latest_updated_at,
    }
    status, _, body = request_json(
        "PUT",
        f"{args.base_url}/admin/department-quotas/{args.dept_id}/status",
        body=status_body,
        bearer_token=admin_token,
    )
    assert_http_200("更新策略状态", status, body)
    payload = parse_json(body)
    assert_api_success("更新策略状态", payload)
    print("更新策略状态成功")

    print("PASS: US-004 部门配额管理接口验证通过")


if __name__ == "__main__":
    main()
