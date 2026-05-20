from __future__ import annotations

import argparse
import json
import os
import time
import urllib.error
import urllib.request
from typing import Any, Dict, Iterable, List, Optional, Tuple


def load_json(path: str) -> dict:
    with open(path, "r", encoding="utf-8") as f:
        return json.load(f)


def pretty_json(obj: Any) -> str:
    try:
        return json.dumps(obj, ensure_ascii=False, indent=2, sort_keys=False)
    except Exception:
        return str(obj)


def mask_headers(headers: Dict[str, str]) -> Dict[str, str]:
    out: Dict[str, str] = {}
    for k, v in headers.items():
        if k.lower() == "authorization":
            out[k] = v[:18] + "..." if len(v) > 18 else "***"
        else:
            out[k] = v
    return out


def replace_placeholders(value: Any, variables: Dict[str, str]) -> Any:
    if isinstance(value, str):
        out = value
        for k, v in variables.items():
            out = out.replace("{" + k + "}", str(v))
        return out
    if isinstance(value, list):
        return [replace_placeholders(x, variables) for x in value]
    if isinstance(value, dict):
        return {k: replace_placeholders(v, variables) for k, v in value.items()}
    return value


def merge_headers(default_headers: Dict[str, str], case_headers: Dict[str, str]) -> Dict[str, str]:
    merged = dict(default_headers)
    merged.update(case_headers or {})
    return merged


def build_url(base_url: str, path: str) -> str:
    if base_url.endswith("/") and path.startswith("/"):
        return base_url[:-1] + path
    if not base_url.endswith("/") and not path.startswith("/"):
        return base_url + "/" + path
    return base_url + path


def http_request(
    method: str,
    url: str,
    headers: Dict[str, str],
    body: Optional[dict],
    timeout_seconds: int,
) -> Tuple[int, Dict[str, str], bytes]:
    payload = None
    if body is not None:
        payload = json.dumps(body, ensure_ascii=False).encode("utf-8")

    req = urllib.request.Request(url=url, method=method.upper(), data=payload, headers=headers)
    try:
        with urllib.request.urlopen(req, timeout=timeout_seconds) as resp:
            resp_headers = {k.lower(): v for k, v in resp.headers.items()}
            return resp.status, resp_headers, resp.read()
    except urllib.error.HTTPError as e:
        resp_headers = {k.lower(): v for k, v in e.headers.items()}
        return e.code, resp_headers, e.read()


def stream_request(
    method: str,
    url: str,
    headers: Dict[str, str],
    body: Optional[dict],
    timeout_seconds: int,
    max_stream_seconds: int,
) -> Tuple[int, Dict[str, str], List[str]]:
    payload = None
    if body is not None:
        payload = json.dumps(body, ensure_ascii=False).encode("utf-8")

    req = urllib.request.Request(url=url, method=method.upper(), data=payload, headers=headers)
    start = time.time()
    lines: List[str] = []
    try:
        with urllib.request.urlopen(req, timeout=timeout_seconds) as resp:
            resp_headers = {k.lower(): v for k, v in resp.headers.items()}
            while True:
                if int(time.time() - start) >= max_stream_seconds:
                    break
                raw = resp.readline()
                if not raw:
                    break
                line = raw.decode("utf-8", errors="replace").rstrip("\n")
                lines.append(line)
                if "[DONE]" in line:
                    break
            return resp.status, resp_headers, lines
    except urllib.error.HTTPError as e:
        resp_headers = {k.lower(): v for k, v in e.headers.items()}
        body_text = e.read().decode("utf-8", errors="replace")
        return e.code, resp_headers, body_text.splitlines()


def assert_equals(name: str, actual: Any, expected: Any, errors: List[str]) -> None:
    if actual != expected:
        errors.append(f"{name}: expected={expected}, actual={actual}")


def assert_contains(name: str, haystack: str, needle: str, errors: List[str]) -> None:
    if needle not in haystack:
        errors.append(f"{name}: expected to contain {needle}")


def run_case(
    base_url: str,
    variables: Dict[str, str],
    default_headers: Dict[str, str],
    case: dict,
    timeout_seconds: int,
    max_stream_seconds: int,
) -> bool:
    resolved_case = replace_placeholders(case, variables)
    url = build_url(base_url, str(resolved_case["path"]))
    headers = merge_headers(replace_placeholders(default_headers, variables), resolved_case.get("headers") or {})
    body = resolved_case.get("body")
    is_stream = bool(body.get("stream") is True) if isinstance(body, dict) else False

    print("========== CASE START ==========")
    print(f"NAME: {resolved_case.get('name')}")
    print("---------- REQUEST ----------")
    print(f"METHOD: {resolved_case.get('method')}")
    print(f"URL: {url}")
    print("HEADERS:")
    print(pretty_json(mask_headers(headers)))
    print("BODY:")
    print(pretty_json(body))

    errors: List[str] = []
    if is_stream:
        status, resp_headers, lines = stream_request(
            method=str(resolved_case["method"]),
            url=url,
            headers=headers,
            body=body,
            timeout_seconds=timeout_seconds,
            max_stream_seconds=max_stream_seconds,
        )
        print("---------- RESPONSE ----------")
        print(f"STATUS: {status}")
        print("HEADERS:")
        print(pretty_json(resp_headers))
        print("STREAM:")
        for ln in lines:
            print(ln)

        if "expected_status" in resolved_case:
            assert_equals("status", status, int(resolved_case["expected_status"]), errors)
        if "expected_content_type_contains" in resolved_case:
            assert_contains(
                "content-type",
                str(resp_headers.get("content-type", "")),
                str(resolved_case["expected_content_type_contains"]),
                errors,
            )
        joined = "\n".join(lines)
        for frag in resolved_case.get("expected_stream_contains") or []:
            assert_contains("stream_contains", joined, str(frag), errors)
        if resolved_case.get("expected_stream_done") is True:
            assert_contains("stream_done", joined, "[DONE]", errors)
    else:
        status, resp_headers, resp_body_bytes = http_request(
            method=str(resolved_case["method"]),
            url=url,
            headers=headers,
            body=body,
            timeout_seconds=timeout_seconds,
        )
        resp_text = resp_body_bytes.decode("utf-8", errors="replace")
        print("---------- RESPONSE ----------")
        print(f"STATUS: {status}")
        print("HEADERS:")
        print(pretty_json(resp_headers))
        print("BODY:")
        try:
            print(pretty_json(json.loads(resp_text)))
        except Exception:
            print(resp_text)

        if "expected_status" in resolved_case:
            assert_equals("status", status, int(resolved_case["expected_status"]), errors)
        if "expected_content_type_contains" in resolved_case:
            assert_contains(
                "content-type",
                str(resp_headers.get("content-type", "")),
                str(resolved_case["expected_content_type_contains"]),
                errors,
            )

    ok = len(errors) == 0
    print("---------- RESULT ----------")
    print("PASS" if ok else "FAIL")
    if errors:
        print(pretty_json(errors))
    print("=========== CASE END ===========")
    return ok


def main() -> None:
    parser = argparse.ArgumentParser()
    parser.add_argument("--base-url", default="http://127.0.0.1:8080")
    parser.add_argument("--cases", default=os.path.join(os.path.dirname(__file__), "openai_chat_regression_cases.json"))
    parser.add_argument("--case", default="")
    parser.add_argument("--vkey", default="")
    parser.add_argument("--model-alias", default="")
    parser.add_argument("--timeout-seconds", type=int, default=60)
    parser.add_argument("--max-stream-seconds", type=int, default=30)
    args = parser.parse_args()

    data = load_json(args.cases)
    variables = {k: str(v) for k, v in (data.get("variables") or {}).items()}
    if args.vkey:
        variables["virtual_api_key"] = args.vkey
    if args.model_alias:
        variables["model_alias"] = args.model_alias

    default_headers = {k: str(v) for k, v in (data.get("default_headers") or {}).items()}
    cases = list(data.get("cases") or [])
    if args.case:
        cases = [c for c in cases if str(c.get("name")) == args.case]

    if not cases:
        print("no cases matched")
        return

    passed = 0
    for c in cases:
        if run_case(
            base_url=str(args.base_url),
            variables=variables,
            default_headers=default_headers,
            case=c,
            timeout_seconds=int(args.timeout_seconds),
            max_stream_seconds=int(args.max_stream_seconds),
        ):
            passed += 1

    print("========== SUMMARY ==========")
    print(f"TOTAL: {len(cases)}")
    print(f"PASS: {passed}")
    print(f"FAIL: {len(cases) - passed}")


if __name__ == "__main__":
    main()

