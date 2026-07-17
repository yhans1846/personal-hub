"""Personal Hub — 可靠版全功能/全按钮巡检（去重 + 点击后重采）。"""
from __future__ import annotations

import json
import os
import re
import time
import urllib.request
from datetime import datetime
from pathlib import Path

from playwright.sync_api import sync_playwright, TimeoutError as PwTimeout

os.environ.setdefault("PLAYWRIGHT_CHROMIUM_USE_HEADLESS_SHELL", "0")

BASE = "http://localhost:3000"
API = "http://localhost:8080"
REPO_ROOT = Path(__file__).resolve().parents[2]
OUT = REPO_ROOT / "logs" / "qa-artifacts"
SHOT = OUT / "screenshots"
SHOT.mkdir(parents=True, exist_ok=True)
RESULT = OUT / "full_button_scan_result.json"

ROUTES = [
    "/dashboard", "/notes", "/notes/new", "/notes/recycle",
    "/diaries", "/readings", "/study-records", "/todos", "/study-plans",
    "/bookmarks", "/files", "/tags", "/categories", "/settings", "/stats", "/search",
]

DEEP_LINKS = [
    "/todos?create=1", "/diaries?create=1", "/study-records?create=1",
    "/study-plans?create=1", "/readings?create=1", "/bookmarks?create=1",
    "/todos?edit=1", "/study-plans?edit=1", "/readings?edit=1",
]

PRIMARY_LABELS = [
    "新建", "新建笔记", "新建日记", "新建收藏", "新建待办", "新建计划", "添加书籍",
    "写日记", "记录学习", "上传", "导入", "导出", "搜索", "筛选", "重置",
    "列表", "卡片", "深色模式", "浅色模式", "通知", "分类管理", "标签管理",
    "保存", "编辑", "删除", "恢复", "彻底删除", "预览", "查看全部",
]

SKIP_RE = re.compile(r"^(登出|退出登录|退出|Logout|删除全部|清空回收站)$", re.I)
MODULE_KEYS = (
    "工作台", "笔记", "待办", "日记", "收藏", "学习", "阅读", "文件",
    "标签", "分类", "统计", "搜索", "设置", "回收", "预览", "登录",
)


def save(report: dict) -> None:
    report["updated_at"] = datetime.now().isoformat(timespec="seconds")
    RESULT.write_text(json.dumps(report, ensure_ascii=False, indent=2), encoding="utf-8")


def api_json(method: str, path: str, body: dict | None = None, token: str | None = None):
    data = None if body is None else json.dumps(body).encode("utf-8")
    headers = {"Content-Type": "application/json", "Accept": "application/json"}
    if token:
        headers["Authorization"] = f"Bearer {token}"
    req = urllib.request.Request(f"{API}{path}", data=data, method=method, headers=headers)
    with urllib.request.urlopen(req, timeout=15) as r:
        return json.loads(r.read().decode("utf-8"))


def login_via_api(username="admin", password="123456") -> dict:
    cap = api_json("GET", "/api/auth/captcha")
    data = cap["data"]
    captcha_id = data["captchaId"]
    shelf = data.get("shelfBooks") or []
    empty = next((i for i, b in enumerate(shelf) if not b), 0)
    login = api_json(
        "POST",
        "/api/auth/login",
        {"username": username, "password": password, "captchaId": captcha_id, "sliderX": empty},
    )
    token = login["data"]["token"]
    return {"token": token, "empty_slot": empty}


def goto(page, url: str, wait: float = 0.5) -> None:
    page.goto(url, wait_until="domcontentloaded", timeout=25000)
    try:
        page.wait_for_load_state("networkidle", timeout=5000)
    except PwTimeout:
        pass
    time.sleep(wait)


def has_module_title(title: str) -> bool:
    return any(k in title for k in MODULE_KEYS)


def dismiss(page) -> None:
    for sel in (
        "button:has-text('取消')",
        ".el-message-box__btns button:has-text('取消')",
        ".el-dialog__headerbtn",
        ".el-drawer__close-btn",
        "[aria-label='Close']",
        "button:has-text('关闭')",
    ):
        try:
            loc = page.locator(sel).first
            if loc.count() and loc.is_visible(timeout=150):
                loc.click(timeout=600)
                time.sleep(0.15)
        except Exception:
            pass
    try:
        page.keyboard.press("Escape")
    except Exception:
        pass
    time.sleep(0.1)


def list_actions(page) -> list[dict]:
    return page.evaluate(
        """(primary) => {
          const root = document.querySelector('.main-content, .app-main, main, #app') || document.body;
          const nodes = [...root.querySelectorAll('button, [role="button"], a.el-button, .el-button')];
          const out = [];
          const seen = new Set();
          for (const el of nodes) {
            if (!(el instanceof HTMLElement)) continue;
            if (el.closest('.sidebar, .side-nav, .el-menu, nav.sidebar, .app-sidebar, .el-pager')) continue;
            const style = getComputedStyle(el);
            if (style.display === 'none' || style.visibility === 'hidden') continue;
            const r = el.getBoundingClientRect();
            if (r.width < 2 || r.height < 2) continue;
            const text = (el.innerText || el.getAttribute('aria-label') || el.getAttribute('title') || '')
              .trim().replace(/\\s+/g, ' ').slice(0, 60);
            if (!text) continue; // 跳过纯图标无文案
            const disabled = !!(el.disabled || el.getAttribute('aria-disabled') === 'true' || el.classList.contains('is-disabled'));
            // 优先主操作；其余标签也收，但同文案只留第一次
            const key = text;
            if (seen.has(key)) continue;
            seen.add(key);
            const mark = 'qa-' + out.length;
            el.setAttribute('data-qa', mark);
            out.push({ mark, text, disabled, href: el.getAttribute('href'), primary: primary.some(p => text.includes(p)) });
          }
          // 主操作靠前
          out.sort((a,b) => Number(b.primary) - Number(a.primary));
          return out;
        }""",
        PRIMARY_LABELS,
    )


def main() -> None:
    report: dict = {
        "started_at": datetime.now().isoformat(timespec="seconds"),
        "base_url": BASE,
        "login": {},
        "routes": [],
        "deep_links": [],
        "buttons": [],
        "api_probes": [],
        "api_errors": [],
        "console_errors": [],
        "page_errors": [],
        "summary": {},
    }
    save(report)

    api_errs: list[dict] = []
    console_errs: list[str] = []
    page_errs: list[str] = []

    try:
        auth = login_via_api()
        report["login"] = {"ok": True, "via": "api+captcha", "empty_slot": auth["empty_slot"]}
    except Exception as e:
        report["login"] = {"ok": False, "error": str(e)[:400]}
        save(report)
        print(json.dumps(report["login"], ensure_ascii=False))
        return

    # API 探针：布局保存 / 头像错误 Content-Type / 备份
    token = auth["token"]
    probes = []
    try:
        # 布局：先 GET 再 PUT 相同内容，观察是否 500
        layout = api_json("GET", "/api/layout/menu", token=token)
        probes.append({"name": "GET /api/layout/menu", "ok": True, "data": bool(layout.get("data"))})
        try:
            body = {"layoutType": "menu", "layoutJson": json.dumps((layout.get("data") or {}).get("layoutJson") or {"items": []})}
            # 若 layoutJson 已是字符串则直接用
            lj = (layout.get("data") or {}).get("layoutJson")
            if isinstance(lj, str):
                body["layoutJson"] = lj
            elif lj is not None:
                body["layoutJson"] = json.dumps(lj, ensure_ascii=False)
            api_json("PUT", "/api/layout", body, token=token)
            probes.append({"name": "PUT /api/layout menu", "ok": True})
        except Exception as e:
            probes.append({"name": "PUT /api/layout menu", "ok": False, "error": str(e)[:300]})
    except Exception as e:
        probes.append({"name": "GET /api/layout/menu", "ok": False, "error": str(e)[:300]})

    # 错误地用 JSON POST avatar（复现 MultipartException）
    try:
        req = urllib.request.Request(
            f"{API}/api/user/avatar",
            data=b'{"file":"x"}',
            method="POST",
            headers={"Content-Type": "application/json", "Authorization": f"Bearer {token}"},
        )
        with urllib.request.urlopen(req, timeout=10) as r:
            probes.append({"name": "POST /api/user/avatar as JSON", "ok": True, "status": r.status})
    except urllib.error.HTTPError as e:
        probes.append({
            "name": "POST /api/user/avatar as JSON",
            "ok": False,
            "status": e.code,
            "error": e.read().decode("utf-8", errors="ignore")[:200],
            "expect": "应返回业务错误而非 500 MultipartException",
        })
    except Exception as e:
        probes.append({"name": "POST /api/user/avatar as JSON", "ok": False, "error": str(e)[:200]})

    report["api_probes"] = probes
    save(report)

    with sync_playwright() as p:
        browser = p.chromium.launch(headless=True, channel="chrome")
        context = browser.new_context(viewport={"width": 1440, "height": 900})
        page = context.new_page()
        page.on("console", lambda m: console_errs.append(f"[{m.type}] {m.text[:400]}") if m.type == "error" else None)
        page.on("pageerror", lambda e: page_errs.append(str(e)[:400]))

        def on_resp(resp):
            if "/api/" in resp.url and resp.status >= 400:
                api_errs.append({
                    "status": resp.status,
                    "method": resp.request.method,
                    "url": resp.url.split("?")[0],
                    "full": resp.url[:240],
                })

        page.on("response", on_resp)

        goto(page, f"{BASE}/login", 0.2)
        page.evaluate("(t) => localStorage.setItem('token', t)", token)
        goto(page, f"{BASE}/dashboard", 0.6)
        if "/login" in page.url:
            report["login"]["ok"] = False
            report["login"]["error"] = "token inject failed"
            save(report)
            browser.close()
            return
        report["login"]["landed"] = page.url
        report["login"]["title"] = page.title()
        page.screenshot(path=str(SHOT / "scan-dashboard.png"))
        save(report)

        for path in ROUTES:
            item = {"path": path}
            before = len(api_errs)
            try:
                goto(page, f"{BASE}{path}")
                item.update({
                    "url": page.url,
                    "title": page.title(),
                    "content_len": len(page.locator("body").inner_text(timeout=5000).strip()),
                })
                item["ok"] = has_module_title(page.title()) and item["content_len"] > 30 and "/login" not in page.url
                item["new_api_errors"] = api_errs[before:]
                if item["new_api_errors"]:
                    item["ok"] = False
                    item["issue"] = "页面加载 API≥400"
                elif not item["ok"]:
                    item["issue"] = "标题/内容异常"
                page.screenshot(path=str(SHOT / f"scan-route-{path.strip('/').replace('/', '_')}.png"))
            except Exception as e:
                item["ok"] = False
                item["error"] = str(e)[:300]
            report["routes"].append(item)
            save(report)

        for path in DEEP_LINKS:
            item = {"path": path}
            try:
                goto(page, f"{BASE}{path}", 0.8)
                dlg = page.locator(".el-dialog, .el-drawer, [role='dialog']").first
                item["url"] = page.url
                item["title"] = page.title()
                item["dialog_visible"] = dlg.count() > 0 and dlg.is_visible()
                # edit 深链若 id 不存在也可能不弹；create 必须弹
                if "create=1" in path:
                    item["ok"] = item["dialog_visible"]
                    if not item["ok"]:
                        item["issue"] = "create 深链未打开 Dialog/Drawer（可能未接 useDeepLinkDialog）"
                else:
                    item["ok"] = has_module_title(page.title())
                    item["note"] = "edit 深链：有数据才弹窗"
                dismiss(page)
            except Exception as e:
                item["ok"] = False
                item["error"] = str(e)[:300]
            report["deep_links"].append(item)
            save(report)

        # 每页按钮：同文案一次，最多 18 个，点击后重采
        for path in ROUTES:
            goto(page, f"{BASE}{path}", 0.5)
            dismiss(page)
            done: set[str] = set()
            rounds = 0
            while rounds < 18:
                rounds += 1
                actions = [a for a in list_actions(page) if a["text"] not in done]
                if not actions:
                    break
                a = actions[0]
                done.add(a["text"])
                btn = {"route": path, "text": a["text"], "disabled": a["disabled"], "primary": a.get("primary")}
                if a["disabled"] or SKIP_RE.match(a["text"]):
                    btn["ok"] = True
                    btn["skipped"] = True
                    report["buttons"].append(btn)
                    continue
                before = len(api_errs)
                before_url = page.url
                try:
                    page.locator(f'[data-qa="{a["mark"]}"]').first.click(timeout=3500)
                    time.sleep(0.45)
                    # 确认框取消
                    try:
                        c = page.locator(".el-message-box__btns button:has-text('取消')").first
                        if c.count() and c.is_visible(timeout=250):
                            c.click(timeout=600)
                            btn["handled"] = "cancel-confirm"
                    except Exception:
                        pass
                    after_url = page.url
                    title = page.title()
                    new_api = api_errs[before:]
                    blank = title == "Personal Hub"
                    kicked = "/login" in after_url
                    btn["landed"] = after_url
                    btn["title"] = title
                    btn["navigated"] = after_url != before_url
                    btn["new_api_errors"] = new_api
                    btn["ok"] = not blank and not kicked and not new_api
                    if not btn["ok"]:
                        btn["issue"] = "; ".join(
                            x for x in [
                                "空白路由" if blank else "",
                                "踢回登录" if kicked else "",
                                f"API≥400 x{len(new_api)}" if new_api else "",
                            ] if x
                        )
                    dismiss(page)
                    if page.url.split("?")[0].rstrip("/") != f"{BASE}{path}".rstrip("/"):
                        goto(page, f"{BASE}{path}", 0.4)
                        dismiss(page)
                except Exception as e:
                    btn["ok"] = False
                    btn["error"] = str(e)[:220]
                    dismiss(page)
                    try:
                        goto(page, f"{BASE}{path}", 0.35)
                    except Exception:
                        pass
                report["buttons"].append(btn)
                save(report)

        # 侧栏导航
        goto(page, f"{BASE}/dashboard", 0.4)
        for label, expect in [
            ("首页", "/dashboard"), ("笔记", "/notes"), ("日记", "/diaries"),
            ("阅读记录", "/readings"), ("学习记录", "/study-records"),
            ("待办任务", "/todos"), ("学习计划", "/study-plans"),
            ("收藏夹", "/bookmarks"), ("文件管理", "/files"),
            ("标签管理", "/tags"), ("分类管理", "/categories"),
            ("系统设置", "/settings"), ("回收站", "/notes/recycle"), ("数据统计", "/stats"),
        ]:
            item = {"kind": "sidebar", "text": label, "expect": expect}
            try:
                goto(page, f"{BASE}/dashboard", 0.3)
                page.get_by_role("link", name=label, exact=True).click(timeout=4000)
                time.sleep(0.45)
                item["landed"] = page.url
                item["title"] = page.title()
                item["ok"] = expect in page.url and has_module_title(page.title())
                if not item["ok"]:
                    item["issue"] = "侧栏跳转不符"
            except Exception as e:
                item["ok"] = False
                item["error"] = str(e)[:200]
            report["buttons"].append(item)
            save(report)

        report["api_errors"] = api_errs
        report["console_errors"] = console_errs[-60:]
        report["page_errors"] = page_errs[-40:]

        def fails(items):
            return [x for x in items if not x.get("ok", True) and not x.get("skipped")]

        api_agg: dict[str, int] = {}
        for a in api_errs:
            k = f"{a.get('status')} {a.get('method')} {a.get('url')}"
            api_agg[k] = api_agg.get(k, 0) + 1

        report["failed_routes"] = fails(report["routes"])
        report["failed_deep_links"] = fails(report["deep_links"])
        report["failed_buttons"] = fails(report["buttons"])
        report["failed_api_probes"] = fails(report["api_probes"])
        report["summary"] = {
            "login_ok": report["login"].get("ok"),
            "routes_ok": sum(1 for r in report["routes"] if r.get("ok")),
            "routes_fail": len(report["failed_routes"]),
            "deep_fail": len(report["failed_deep_links"]),
            "buttons_total": len(report["buttons"]),
            "buttons_fail": len(report["failed_buttons"]),
            "api_probe_fail": len(report["failed_api_probes"]),
            "api_http_errors": len(api_errs),
            "api_unique": api_agg,
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
