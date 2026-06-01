#!/usr/bin/env python3
from __future__ import annotations

import argparse
import json
import sys
import time
import urllib.error
import urllib.request
from typing import Any, Dict, Optional, Tuple


def pretty_json(value: Any) -> str:
    try:
        return json.dumps(value, ensure_ascii=False, indent=2, sort_keys=False)
    except Exception:
        return str(value)


def request_json(
    method: str,
    url: str,
    body: Optional[dict],
    bearer_token: str,
    timeout_seconds: int,
) -> Tuple[int, Dict[str, str], str]:
    headers = {
        "Authorization": f"Bearer {bearer_token}",
        "Content-Type": "application/json",
        "Accept": "application/json",
    }
    payload = json.dumps(body, ensure_ascii=False).encode("utf-8") if body is not None else None
    req = urllib.request.Request(url=url, method=method.upper(), data=payload, headers=headers)
    try:
        with urllib.request.urlopen(req, timeout=timeout_seconds) as resp:
            return resp.status, {k.lower(): v for k, v in resp.headers.items()}, resp.read().decode("utf-8")
    except urllib.error.HTTPError as e:
        return e.code, {k.lower(): v for k, v in e.headers.items()}, e.read().decode("utf-8")


def parse_json(text: str) -> dict:
    try:
        value = json.loads(text)
        return value if isinstance(value, dict) else {}
    except Exception:
        return {}


def get_int(value: Any) -> int:
    try:
        return max(int(value), 0)
    except Exception:
        return 0


def call_chat(base_url: str, virtual_key: str, model_alias: str, timeout_seconds: int) -> dict:
    body = {
        "model": model_alias,
        "stream": False,
        "temperature": 0.2,
        "max_tokens": 64,
        "messages": [
            {
                "role": "user",
                "content": "请用一句话回答：token usage detail test",
            }
        ],
    }
    url = f"{base_url.rstrip('/')}/v1/chat/completions"
    print("========== REQUEST ==========")
    print(f"POST {url}")
    print(pretty_json(body))
    status, headers, response_text = request_json("POST", url, body, virtual_key, timeout_seconds)
    print("========== RESPONSE ==========")
    print(f"STATUS: {status}")
    print(pretty_json(headers))
    print(pretty_json(parse_json(response_text) or response_text))
    if status != 200:
        raise RuntimeError(f"chat completions 请求失败 status={status}")
    response_body = parse_json(response_text)
    usage = response_body.get("usage")
    if not isinstance(usage, dict):
        raise RuntimeError("响应中没有 usage，无法校验 token 明细落库")
    return usage


def fetch_latest_usage_log(args: argparse.Namespace) -> Dict[str, Any]:
    try:
        import pymysql  # type: ignore
    except ImportError as exc:
        raise RuntimeError("缺少 pymysql，请先执行：pip install pymysql") from exc

    sql = """
        SELECT
            id,
            prompt_tokens,
            prompt_cached_tokens,
            prompt_cache_miss_tokens,
            prompt_audio_tokens,
            completion_tokens,
            completion_reasoning_tokens,
            completion_audio_tokens,
            completion_accepted_prediction_tokens,
            completion_rejected_prediction_tokens,
            total_tokens,
            calc_source,
            created_at
        FROM usage_logs
        WHERE endpoint = 'chat.completions'
          AND status_code = 200
        ORDER BY id DESC
        LIMIT 1
    """
    conn = pymysql.connect(
        host=args.mysql_host,
        port=args.mysql_port,
        user=args.mysql_user,
        password=args.mysql_password,
        database=args.mysql_database,
        charset="utf8mb4",
        cursorclass=pymysql.cursors.DictCursor,
    )
    try:
        with conn.cursor() as cursor:
            cursor.execute(sql)
            row = cursor.fetchone()
            if not row:
                raise RuntimeError("未查询到成功的 usage_logs 记录")
            return dict(row)
    finally:
        conn.close()


def expected_token_details(usage: dict) -> Dict[str, int]:
    prompt_details = usage.get("prompt_tokens_details")
    completion_details = usage.get("completion_tokens_details")
    if not isinstance(prompt_details, dict):
        prompt_details = {}
    if not isinstance(completion_details, dict):
        completion_details = {}
    prompt_tokens = get_int(usage.get("prompt_tokens"))
    completion_tokens = get_int(usage.get("completion_tokens"))
    prompt_cached_tokens = get_int(prompt_details.get("cached_tokens")) or get_int(usage.get("prompt_cache_hit_tokens"))
    explicit_miss_tokens = usage.get("prompt_cache_miss_tokens")
    return {
        "prompt_tokens": prompt_tokens,
        "prompt_cached_tokens": prompt_cached_tokens,
        "prompt_cache_miss_tokens": get_int(explicit_miss_tokens)
        if explicit_miss_tokens is not None
        else max(prompt_tokens - prompt_cached_tokens, 0),
        "prompt_audio_tokens": get_int(prompt_details.get("audio_tokens")),
        "completion_tokens": completion_tokens,
        "completion_reasoning_tokens": get_int(completion_details.get("reasoning_tokens")),
        "completion_audio_tokens": get_int(completion_details.get("audio_tokens")),
        "completion_accepted_prediction_tokens": get_int(completion_details.get("accepted_prediction_tokens")),
        "completion_rejected_prediction_tokens": get_int(completion_details.get("rejected_prediction_tokens")),
        "total_tokens": get_int(usage.get("total_tokens")) or prompt_tokens + completion_tokens,
    }


def assert_usage_log(row: Dict[str, Any], expected: Dict[str, int]) -> None:
    errors = []
    for field_name, expected_value in expected.items():
        actual_value = get_int(row.get(field_name))
        if actual_value != expected_value:
            errors.append(f"{field_name}: expected={expected_value}, actual={actual_value}")
    print("========== USAGE LOG ==========")
    print(pretty_json(row))
    if errors:
        print("========== RESULT ==========")
        print("FAIL")
        print(pretty_json(errors))
        raise SystemExit(1)
    print("========== RESULT ==========")
    print("PASS")


def main() -> None:
    parser = argparse.ArgumentParser(description="校验 chat completions token 明细是否完整写入 usage_logs")
    parser.add_argument("--base-url", default="http://127.0.0.1:8080")
    parser.add_argument("--vkey", required=True, help="虚拟 API Key，例如 sk-vkey-...")
    parser.add_argument("--model-alias", required=True)
    parser.add_argument("--timeout-seconds", type=int, default=60)
    parser.add_argument("--mysql-host", default="127.0.0.1")
    parser.add_argument("--mysql-port", type=int, default=3306)
    parser.add_argument("--mysql-user", default="root")
    parser.add_argument("--mysql-password", default="123456789")
    parser.add_argument("--mysql-database", default="llm-gateway")
    parser.add_argument("--wait-seconds", type=float, default=0.5)
    args = parser.parse_args()

    usage = call_chat(args.base_url, args.vkey, args.model_alias, args.timeout_seconds)
    time.sleep(args.wait_seconds)
    row = fetch_latest_usage_log(args)
    assert_usage_log(row, expected_token_details(usage))


if __name__ == "__main__":
    try:
        main()
    except Exception as e:
        print(f"FAIL: {e}", file=sys.stderr)
        raise SystemExit(1)
