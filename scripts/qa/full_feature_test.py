"""Personal Hub smoke test — resilient, incremental results."""
from __future__ import annotations

import json
import os
import re
import time
from datetime import datetime
from pathlib import Path

from playwright.sync_api import sync_playwright, TimeoutError as PwTimeout

os.environ.setdefault("PLAYWRIGHT_CHROMIUM_USE_HEADLESS_SHELL", "0")

BASE = "http://localhost:3000"
REPO_ROOT = Path(__file__).resolve().parents[2]
OUT = REPO_ROOT / "logs" / "qa-artifacts"
SHOT = OUT / "screenshots"
SHOT.mkdir(parents=True, exist_ok=True)
RESULT = OUT / "full_feature_result.json"

ROUTES = [
    "/dashboard",
    "/notes",
    "/notes/new",
    "/notes/recycle",
    "/diaries",
    "/readings",
    "/study-records",
    "/todos",
    "/study-plans",
    "/bookmarks",
    "/files",
    "/tags",
    "/categories",
    "/settings",
    "/stats",
    "/search",
]

SIDEBAR = [
    ("首页", "/dashboard"),
    ("笔记", "/notes"),
    ("日记", "/diaries"),
    ("阅读记录", "/readings"),
    ("学习记录", "/study-records"),
    ("待办任务", "/todos"),
    ("学习计划", "/study-plans"),
    ("收藏夹", "/bookmarks"),
    ("文件管理", "/files"),
    ("标签管理", "/tags"),
    ("分类管理", "/categories"),
    ("系统设置", "/settings"),
    ("回收站", "/notes/recycle"),
    ("数据统计", "/stats"),
]

SUSPECT = [
    "/diaries/new",
    "/study-records/new",
    "/todos/1/edit",
    "/study-plans/1/edit",
    "/study-records/1/edit",
    "/readings/1/edit",
]

MODULE_KEYS = (
    "工作台", "笔记", "待办", "日记", "收藏", "学习", "阅读", "文件",
    "标签", "分类", "统计", "搜索", "设置", "回收", "预览", "登录",
)


def save(report: dict) -> None:
    report["updated_at"] = datetime.now().isoformat(timespec="seconds")
    RESULT.write_text(json.dumps(report, ensure_ascii=False, indent=2), encoding="utf-8")


def goto(page, url: str, wait: float = 0.8) -> None:
    page.goto(url, wait_until="domcontentloaded", timeout=20000)
    try:
        page.wait_for_load_state("networkidle", timeout=8000)
    except PwTimeout:
        pass
    time.sleep(wait)


def has_module_title(title: str) -> bool:
    return any(k in title for k in MODULE_KEYS)


def main() -> None:
    report = {
        "started_at": datetime.now().isoformat(timespec="seconds"),
        "base_url": BASE,
        "login": {},
        "routes": [],
        "sidebar": [],
        "dashboard_view_all": [],
        "dashboard_items": [],
        "note_links": [],
        "suspect_paths": [],
        "api_errors": [],
        "console_errors": [],
        "page_errors": [],
        "summary": {},
    }
    save(report)

    api_errs: list[dict] = []
    console_errs: list[str] = []
    page_errs: list[str] = []

    with sync_playwright() as p:
        browser = p.chromium.launch(headless=True, channel="chrome")
        context = browser.new_context(viewport={"width": 1440, "height": 900})
        page = context.new_page()

        page.on(
            "console",
            lambda m: console_errs.append(f"[{m.type}] {m.text[:400]}")
            if m.type == "error"
            else None,
        )
        page.on("pageerror", lambda e: page_errs.append(str(e)[:400]))

        def on_resp(resp):
            if "/api/" in resp.url and resp.status >= 400:
                api_errs.append(
                    {
                        "status": resp.status,
                        "method": resp.request.method,
                        "url": resp.url,
                    }
                )

        page.on("response", on_resp)

        # login
        try:
            goto(page, f"{BASE}/login", 0.5)
            page.fill('input[placeholder="用户名"]', "admin")
            page.fill('input[placeholder="密码"]', "123456")
            page.click("button:has-text('登录')")
            page.wait_for_url(re.compile(r"dashboard"), timeout=15000)
            report["login"] = {"ok": True, "url": page.url, "title": page.title()}
            page.screenshot(path=str(SHOT / "login-ok.png"))
        except Exception as e:
            report["login"] = {"ok": False, "error": str(e)[:300], "url": page.url}
            save(report)
            browser.close()
            print(json.dumps(report["login"], ensure_ascii=False))
            return
        save(report)

        # routes
        for path in ROUTES:
            item = {"path": path}
            try:
                goto(page, f"{BASE}{path}")
                item["url"] = page.url
                item["title"] = page.title()
                text = page.locator("body").inner_text(timeout=5000)
                item["content_len"] = len(text.strip())
                item["ok"] = has_module_title(page.title()) and item["content_len"] > 30
                if not item["ok"]:
                    item["issue"] = "标题未匹配模块或内容过少"
                page.screenshot(path=str(SHOT / f"route-{path.strip('/').replace('/', '_') or 'root'}.png"))
            except Exception as e:
                item["ok"] = False
                item["error"] = str(e)[:300]
            report["routes"].append(item)
            save(report)

        # sidebar
        for label, expect in SIDEBAR:
            item = {"label": label, "expect": expect}
            try:
                goto(page, f"{BASE}/dashboard", 0.4)
                page.get_by_role("link", name=label, exact=True).click(timeout=5000)
                try:
                    page.wait_for_load_state("networkidle", timeout=8000)
                except PwTimeout:
                    pass
                time.sleep(0.5)
                item["landed"] = page.url
                item["title"] = page.title()
                item["ok"] = expect in page.url and has_module_title(page.title())
                if not item["ok"]:
                    item["issue"] = "侧栏跳转目标不符"
            except Exception as e:
                item["ok"] = False
                item["error"] = str(e)[:300]
            report["sidebar"].append(item)
            save(report)

        # dashboard 查看全部
        try:
            goto(page, f"{BASE}/dashboard")
            hrefs = page.eval_on_selector_all(
                "a.card-more, a:has-text('查看全部')",
                "els => els.map(e => ({href: e.getAttribute('href'), text: (e.textContent||'').trim()}))",
            )
            for h in hrefs:
                item = {"href": h.get("href"), "text": h.get("text")}
                try:
                    goto(page, f"{BASE}{h['href']}" if h.get("href", "").startswith("/") else h["href"])
                    item["landed"] = page.url
                    item["title"] = page.title()
                    item["ok"] = has_module_title(page.title()) and "/login" not in page.url
                except Exception as e:
                    item["ok"] = False
                    item["error"] = str(e)[:200]
                report["dashboard_view_all"].append(item)
                save(report)
        except Exception as e:
            report["dashboard_view_all_error"] = str(e)[:200]

        # dashboard list items — capture target URLs via click
        try:
            goto(page, f"{BASE}/dashboard")
            count = page.locator(".list-item").count()
            for i in range(min(count, 10)):
                item = {"index": i}
                try:
                    goto(page, f"{BASE}/dashboard", 0.5)
                    el = page.locator(".list-item").nth(i)
                    item["text"] = el.inner_text().strip().replace("\n", " ")[:100]
                    el.click()
                    try:
                        page.wait_for_load_state("networkidle", timeout=8000)
                    except PwTimeout:
                        pass
                    time.sleep(0.6)
                    item["landed"] = page.url
                    item["title"] = page.title()
                    generic = not has_module_title(page.title()) or page.title() == "Personal Hub"
                    item["ok"] = not generic and "/login" not in page.url
                    if not item["ok"]:
                        item["issue"] = "点击后路由标题异常（可能无匹配前端路由）"
                    page.screenshot(path=str(SHOT / f"dash-item-{i}.png"))
                except Exception as e:
                    item["ok"] = False
                    item["error"] = str(e)[:200]
                report["dashboard_items"].append(item)
                save(report)
        except Exception as e:
            report["dashboard_items_error"] = str(e)[:200]

        # note list links
        try:
            goto(page, f"{BASE}/notes")
            hrefs = page.eval_on_selector_all(
                "a[href*='/notes/']",
                "els => [...new Set(els.map(e => e.getAttribute('href')).filter(Boolean))]",
            )
            for href in hrefs[:8]:
                if href in ("/notes", "/notes/new", "/notes/recycle") or href.endswith("/notes"):
                    continue
                item = {"href": href}
                try:
                    target = href if href.startswith("http") else f"{BASE}{href}"
                    goto(page, target)
                    item["landed"] = page.url
                    item["title"] = page.title()
                    item["ok"] = has_module_title(page.title())
                except Exception as e:
                    item["ok"] = False
                    item["error"] = str(e)[:200]
                report["note_links"].append(item)
                save(report)
        except Exception as e:
            report["note_links_error"] = str(e)[:200]

        # suspect deep links
        for path in SUSPECT:
            item = {"path": path}
            try:
                goto(page, f"{BASE}{path}")
                item["url"] = page.url
                item["title"] = page.title()
                text = page.locator("body").inner_text(timeout=5000)
                item["content_len"] = len(text.strip())
                # Vue unmatched nested route: title often just "Personal Hub"
                item["ok"] = has_module_title(page.title()) and page.title() != "Personal Hub"
                if not item["ok"]:
                    item["issue"] = "路由未在 router/index.ts 注册，跳转无页面组件"
                page.screenshot(path=str(SHOT / f"suspect-{path.strip('/').replace('/', '_')}.png"))
            except Exception as e:
                item["ok"] = False
                item["error"] = str(e)[:200]
            report["suspect_paths"].append(item)
            save(report)

        # pick real ids from API if token available
        token = page.evaluate("() => localStorage.getItem('token')")
        if token:
            report["api_id_checks"] = []
            import urllib.request

            def api_get(path: str):
                req = urllib.request.Request(
                    f"http://localhost:8080{path}",
                    headers={"Authorization": f"Bearer {token}"},
                )
                with urllib.request.urlopen(req, timeout=10) as r:
                    return json.loads(r.read().decode("utf-8"))

            for list_path, id_key, edit_tpl in [
                ("/api/study-plans", "id", "/study-plans/{}/edit"),
                ("/api/study-records", "id", "/study-records/{}/edit"),
                ("/api/readings", "id", "/readings/{}/edit"),
                ("/api/todos", "id", "/todos/{}/edit"),
                ("/api/notes?page=1&size=1", "id", "/notes/{}/edit"),
                ("/api/notes?page=1&size=1", "id", "/notes/{}/preview"),
            ]:
                try:
                    data = api_get(list_path)
                    rows = data.get("data") if isinstance(data, dict) else None
                    if isinstance(rows, dict) and "records" in rows:
                        rows = rows["records"]
                    if isinstance(rows, list) and rows:
                        rid = rows[0].get(id_key)
                        path = edit_tpl.format(rid)
                        goto(page, f"{BASE}{path}")
                        item = {
                            "path": path,
                            "title": page.title(),
                            "url": page.url,
                            "ok": has_module_title(page.title()) and page.title() != "Personal Hub",
                        }
                        if not item["ok"]:
                            item["issue"] = "使用真实 ID 的编辑深链仍无路由匹配"
                        report["api_id_checks"].append(item)
                        save(report)
                except Exception as e:
                    report["api_id_checks"].append({"list_path": list_path, "error": str(e)[:200]})

        report["api_errors"] = api_errs
        report["console_errors"] = console_errs[-50:]
        report["page_errors"] = page_errs[-50:]

        def fails(items):
            return [x for x in items if not x.get("ok", True)]

        report["summary"] = {
            "login_ok": report["login"].get("ok"),
            "routes_ok": sum(1 for r in report["routes"] if r.get("ok")),
            "routes_fail": len(fails(report["routes"])),
            "sidebar_fail": len(fails(report["sidebar"])),
            "view_all_fail": len(fails(report["dashboard_view_all"])),
            "dash_item_fail": len(fails(report["dashboard_items"])),
            "suspect_fail": len(fails(report["suspect_paths"])),
            "api_http_errors": len(api_errs),
            "console_errors": len(console_errs),
            "page_errors": len(page_errs),
            "finished_at": datetime.now().isoformat(timespec="seconds"),
        }
        save(report)
        browser.close()
        print(json.dumps(report["summary"], ensure_ascii=False, indent=2))
        print(f"Wrote {RESULT}")


if __name__ == "__main__":
    main()
