# Cursor 浏览器巡检方法

Cursor Agent 做**全功能 / 跳转 / 日志对照**测试的标准做法（2026-07-15 实战）。规则：`.cursor/rules/browser-qa-testing.mdc`。

---

## 1. 目标

| 目标 | 说明 |
|------|------|
| 功能 | 各模块列表可开、核心交互可用 |
| 跳转 | 侧栏、「查看全部」、工作台条目、通知深链、快捷创建 |
| 根因 | 区分前端路由 vs 后端 API / 日志 |

| 项 | 值 |
|----|-----|
| 前端 | http://localhost:3000 |
| 后端 | http://localhost:8080 |
| 演示账号 | `admin` / `123456` |
| 应用日志 | `logs/personal-hub.log` |
| 错误日志 | `logs/personal-hub-error.log` |

---

## 2. 工具组合（三件套）

```
Browser MCP（复核/截图/Console）
    ↓
Playwright 批量脚本（路由/深链/API≥400）
    ↓
后端双日志对照（error.log + app.log）
```

| 工具 | 适用 | 产物 |
|------|------|------|
| **cursor-ide-browser** | 关键路径、弹窗、通知 | 截图 + Console |
| **Playwright** `scripts/qa/full_feature_test.py` | 16+ 路由、深链 | `logs/qa-artifacts/full_feature_result.json` |
| **日志** | 后端故障判定 | ERROR / Exception |

---

## 3. Cursor Browser MCP 标准流程

### 3.1 调用顺序（必须遵守）

1. `browser_tabs`（list）
2. `browser_navigate`
3. `browser_lock`
4. `browser_snapshot` → 拿 `ref`
5. `browser_click` / `browser_fill` / …
6. `browser_wait_for`（参数为**秒**）
7. 可选：`browser_console_messages`、`browser_network_requests`
8. `browser_unlock`（**结束前必须**）

### 3.2 实操要点

- **先 snapshot 再 click**；ref 过期 → 重新 snapshot。
- **SPA 标题**：正常 `{模块} | Personal Hub`；仅 `Personal Hub` → 子路由未匹配（空白）。
- **Router 警告** `No match found for location with path "/xxx/..."` → 前端深链，**通常不进** `personal-hub-error.log`。
- **Vditor**：可能空白 1–3s，`browser_wait_for` 后再判失败。
- **侧栏 click 偶发延迟**：重要断言优先**直访 URL**。

### 3.3 推荐复核清单

| 步骤 | URL / 操作 | 期望 |
|------|------------|------|
| 登录后首页 | `/dashboard` | 问候语、卡片、角标 |
| 侧栏模块 | `/notes` … `/stats` | 标题含模块名 |
| 「查看全部」 | `/todos`、`/notes` 等 | 列表页 |
| 深链弹窗 | `/study-plans?edit=1` | 编辑弹窗 |
| 旧路径 | `/study-plans/1/edit` | redirect + 弹窗 |
| 笔记编辑 | `/notes/{id}/edit` | 骨架后正文 |
| 通知 | 点铃铛 | 面板可开、跳转不空白 |

---

## 4. Playwright 批量巡检

### 4.1 脚本位置

| 路径 | 说明 |
|------|------|
| `scripts/qa/full_feature_test.py` | 巡检脚本 |
| `logs/qa-artifacts/full_feature_result.json` | 结果（gitignore） |
| `logs/qa-artifacts/screenshots/` | 截图（gitignore） |

### 4.2 Windows 启动方式

```powershell
$env:PLAYWRIGHT_CHROMIUM_USE_HEADLESS_SHELL = '0'
python scripts/qa/full_feature_test.py
```

策略：`channel="chrome"` · `wait_until="domcontentloaded"`（通知轮询致 `networkidle` 超时）· 每步 `save()` 写 JSON。

### 4.3 脚本覆盖范围（与路由表对齐）

1. 登录 `admin` / `123456`
2. 直访 dashboard / notes / diaries / readings / study-records / todos / study-plans / bookmarks / files / tags / categories / settings / stats / search / recycle
3. 侧栏点击 · 工作台「查看全部」与 `.list-item`
4. 可疑深链 `/xxx/new`、`/xxx/:id/edit`
5. JWT 调 API 取真实 id 验编辑深链
6. 汇总 `api_errors` / `console_errors` / `summary`

### 4.4 依赖

```bash
pip show playwright
cd personal-hub-web && pnpm test   # 前端单测，与浏览器巡检独立
```

---

## 5. 后端日志对照

### 5.1 先看文件再搜关键字

```powershell
Get-Item logs\personal-hub-error.log, logs\personal-hub.log |
  Format-Table Name, Length, LastWriteTime
Select-String -Path logs\personal-hub.log -Pattern "ERROR|Exception|WARN" |
  Select-Object -Last 40
```

### 5.2 判读

| 现象 | 结论 |
|------|------|
| `personal-hub-error.log` 0 字节 | 无 ERROR 落盘 |
| app.log 仅 JWT DEBUG / INFO | 后端正常 |
| 前端空白 + Router warn、无 ERROR | **前端路由/深链** |
| app.log ERROR + API 500 | **后端**优先修 |

前端空白**未必**伴随后端错误（2026-07-15 案例）。

---

## 6. 深链与弹窗规范（测试关注点）

Dialog/Drawer 模块**不得**依赖未注册独立编辑页。正确形态：

| 动作 | URL |
|------|-----|
| 编辑 | `/todos?edit=10`、`/study-plans?edit=2` |
| 新建 | `/study-records?create=1`、`/diaries?create=1` |
| 旧路径 | `/todos/10/edit` → redirect |

实现：`src/utils/deepLink.ts` · `useDeepLinkDialog.ts` · `router/index.ts` · `docs/STYLE_GUIDE.md`

笔记独立页：`/notes/:id/edit`、`/notes/:id/preview`。

---

## 7. 报告产出规范

每次正式巡检：

1. `docs/qa/YYYY-MM-DD-功能巡检报告.md`
2. `logs/qa-artifacts/full_feature_result.json`
3. `logs/qa-artifacts/screenshots/`
4. 有修复 → 报告「修复回写」+ `docs/CHANGELOG.md`

最少：结论、日志对照、路由表、失败深链、根因、优先级。样例：`docs/qa/2026-07-15-功能巡检报告.md`。

---

## 8. Agent 检查清单（复制用）

```markdown
- [ ] 前端 3000 / 后端 8080 可达
- [ ] Browser：navigate → lock → 关键路径 snapshot
- [ ] Playwright：跑 full_feature_test.py，看 summary
- [ ] 读 personal-hub-error.log 与 app.log ERROR/Exception
- [ ] Console 是否有 Vue Router No match
- [ ] 深链：?edit= / 旧 /:id/edit redirect / 笔记编辑页
- [ ] 写 docs/qa 报告（含证据路径）
- [ ] browser_unlock
```

---

## 9. 相关文件索引

| 路径 | 说明 |
|------|------|
| `.cursor/rules/browser-qa-testing.mdc` | Agent 规则 |
| `docs/qa/CURSOR_BROWSER_TESTING.md` | 本文 |
| `docs/qa/2026-07-15-功能巡检报告.md` | 首次全量报告 |
| `scripts/qa/full_feature_test.py` | 批量脚本 |
| `personal-hub-web/src/router/index.ts` | 路由与 redirect |
| `personal-hub-web/src/utils/deepLink.ts` | 深链工具 |
