#!/usr/bin/env python3
import argparse
import json
import sys
from concurrent.futures import ThreadPoolExecutor, as_completed
from pathlib import Path
from typing import Any, Dict, Optional, Tuple
from urllib import error, request


SCRIPT_DIR = Path(__file__).resolve().parent
DEFAULT_CASE_FILE = SCRIPT_DIR / "user_quota_transfer_cases.json"


def load_test_cases(case_file: Path) -> Dict[str, Any]:
    with case_file.open("r", encoding="utf-8") as file:
        return json.load(file)


def build_url(base_url: str, path: str) -> str:
    return base_url.rstrip("/") + path


def pretty_json(value: Any) -> str:
    return json.dumps(value, ensure_ascii=False, indent=2)


def send_json(
    method: str,
    url: str,
    headers: Dict[str, str],
    body: Optional[Dict[str, Any]] = None,
) -> Tuple[int, Dict[str, str], str, Optional[Dict[str, Any]]]:
    payload = None if body is None else json.dumps(body).encode("utf-8")
    req = request.Request(url=url, data=payload, headers=headers, method=method.upper())
    try:
        with request.urlopen(req, timeout=30) as response:
            response_body = response.read().decode("utf-8")
            return response.status, dict(response.headers), response_body, parse_json(response_body)
    except error.HTTPError as exc:
        response_body = exc.read().decode("utf-8")
        return exc.code, dict(exc.headers), response_body, parse_json(response_body)


def parse_json(raw: str) -> Optional[Dict[str, Any]]:
    try:
        parsed = json.loads(raw)
        return parsed if isinstance(parsed, dict) else None
    except json.JSONDecodeError:
        return None


def print_request_log(name: str, method: str, url: str, headers: Dict[str, str], body: Optional[Dict[str, Any]]) -> None:
    print("========== CASE START ==========")
    print(f"case: {name}")
    print("---------- REQUEST ----------")
    print(f"{method.upper()} {url}")
    print(pretty_json(mask_headers(headers)))
    if body is not None:
        print(pretty_json(body))


def print_response_log(status: int, headers: Dict[str, str], raw_body: str, parsed_body: Optional[Dict[str, Any]]) -> None:
    print("---------- RESPONSE ----------")
    print(f"status: {status}")
    print(pretty_json(dict(headers)))
    print(pretty_json(parsed_body) if parsed_body is not None else raw_body)
    print("========== CASE END ==========")


def mask_headers(headers: Dict[str, str]) -> Dict[str, str]:
    masked = dict(headers)
    authorization = masked.get("Authorization")
    if authorization and len(authorization) > 20:
        masked["Authorization"] = authorization[:16] + "***"
    return masked


def login(base_url: str, default_headers: Dict[str, str], username: str, password: str) -> str:
    body = {"username": username, "password": password}
    url = build_url(base_url, "/api/auth/login")
    print_request_log("login", "POST", url, default_headers, body)
    status, headers, raw_body, parsed_body = send_json("POST", url, default_headers, body)
    print_response_log(status, headers, raw_body, parsed_body)
    if status != 200 or not parsed_body or parsed_body.get("success") is not True:
        raise RuntimeError("登录失败，请检查 username/password/base_url")
    token = ((parsed_body.get("data") or {}).get("token") or "").strip()
    if not token:
        raise RuntimeError("登录响应中没有 token")
    return token


def get_current_quota(base_url: str, headers: Dict[str, str]) -> Dict[str, Any]:
    url = build_url(base_url, "/admin/user/quotas/current")
    print_request_log("get_current_quota", "GET", url, headers, None)
    status, response_headers, raw_body, parsed_body = send_json("GET", url, headers)
    print_response_log(status, response_headers, raw_body, parsed_body)
    if status != 200 or not parsed_body or parsed_body.get("success") is not True:
        raise RuntimeError("查询当前用户配额失败")
    return parsed_body.get("data") or {}


def transfer_quota(
    base_url: str,
    headers: Dict[str, str],
    target_user_id: int,
    transfer_tokens: int,
    remark: str,
    case_name: str,
) -> Tuple[int, Optional[Dict[str, Any]]]:
    body = {
        "targetUserId": target_user_id,
        "transferTokens": transfer_tokens,
        "remark": remark,
    }
    url = build_url(base_url, "/admin/user/quotas/transfer")
    print_request_log(case_name, "POST", url, headers, body)
    status, response_headers, raw_body, parsed_body = send_json("POST", url, headers, body)
    print_response_log(status, response_headers, raw_body, parsed_body)
    return status, parsed_body


def assert_single_transfer(
    before: Dict[str, Any],
    after: Dict[str, Any],
    response_body: Optional[Dict[str, Any]],
    transfer_tokens: int,
) -> None:
    if not response_body or response_body.get("success") is not True:
        raise AssertionError("转配接口未返回成功")
    before_available = int(before.get("availableTokens") or 0)
    after_available = int(after.get("availableTokens") or 0)
    expected_after = before_available - transfer_tokens
    if after_available != expected_after:
        raise AssertionError(
            f"转出方剩余额度不符合预期 before={before_available}, after={after_available}, expected={expected_after}"
        )


def run_single_case(config: Dict[str, Any], token: str, case: Dict[str, Any]) -> None:
    variables = config["variables"]
    headers = dict(config.get("default_headers", {}))
    headers["Authorization"] = f"Bearer {token}"
    before = get_current_quota(variables["base_url"], headers)
    status, response_body = transfer_quota(
        variables["base_url"],
        headers,
        int(variables["target_user_id"]),
        int(variables["transfer_tokens"]),
        variables.get("remark", ""),
        case["name"],
    )
    if status != int(case.get("expected_status", 200)):
        raise AssertionError(f"HTTP 状态码不符合预期 status={status}")
    after = get_current_quota(variables["base_url"], headers)
    assert_single_transfer(before, after, response_body, int(variables["transfer_tokens"]))


def run_concurrent_case(config: Dict[str, Any], token: str, case: Dict[str, Any]) -> None:
    variables = config["variables"]
    headers = dict(config.get("default_headers", {}))
    headers["Authorization"] = f"Bearer {token}"
    before = get_current_quota(variables["base_url"], headers)
    before_available = int(before.get("availableTokens") or 0)
    transfer_tokens = int(variables["transfer_tokens"])
    concurrent_requests = int(variables.get("concurrent_requests", 2))

    with ThreadPoolExecutor(max_workers=concurrent_requests) as executor:
        futures = [
            executor.submit(
                transfer_quota,
                variables["base_url"],
                headers,
                int(variables["target_user_id"]),
                transfer_tokens,
                f"{variables.get('remark', '')} concurrent-{index + 1}",
                f"{case['name']}-{index + 1}",
            )
            for index in range(concurrent_requests)
        ]
        results = [future.result() for future in as_completed(futures)]

    after = get_current_quota(variables["base_url"], headers)
    after_available = int(after.get("availableTokens") or 0)
    success_count = sum(1 for _, body in results if body and body.get("success") is True)
    expected_min_available = max(before_available - success_count * transfer_tokens, 0)

    print("---------- SUMMARY ----------")
    print(f"before_available: {before_available}")
    print(f"after_available: {after_available}")
    print(f"success_count: {success_count}")
    print(f"expected_after_by_success_count: {expected_min_available}")

    if after_available != before_available - success_count * transfer_tokens:
        raise AssertionError("并发转配后转出方剩余额度与成功次数不一致，可能存在超扣或账户重建异常")
    if success_count * transfer_tokens > before_available:
        raise AssertionError("并发转配成功总额超过转出方原始剩余额度")


def main() -> int:
    parser = argparse.ArgumentParser(description="用户额度转配接口回归验证脚本")
    parser.add_argument("--case-file", default=str(DEFAULT_CASE_FILE), help="测试用例 JSON 文件路径")
    parser.add_argument("--case", choices=["normal_transfer", "concurrent_transfer_should_not_overdraw"], default="normal_transfer")
    args = parser.parse_args()

    config = load_test_cases(Path(args.case_file))
    variables = config["variables"]
    token = login(
        variables["base_url"],
        dict(config.get("default_headers", {})),
        variables["username"],
        variables["password"],
    )
    selected_case = next(case for case in config["cases"] if case["name"] == args.case)
    if selected_case["mode"] == "single":
        run_single_case(config, token, selected_case)
    else:
        run_concurrent_case(config, token, selected_case)
    print("PASS")
    return 0


if __name__ == "__main__":
    try:
        raise SystemExit(main())
    except Exception as exc:
        print(f"FAIL: {exc}", file=sys.stderr)
        raise SystemExit(1)
