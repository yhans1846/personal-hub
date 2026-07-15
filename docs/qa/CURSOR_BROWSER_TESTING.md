# Cursor 浏览器巡检方法

本文记录本项目在 Cursor Agent 中做**全功能 / 跳转 / 日志对照**测试的标准做法（基于 2026-07-15 实战）。配套 Cursor 规则：`.cursor/rules/browser-qa-testing.mdc`。

---

## 1. 目标

| 目标 | 说明 |
|------|------|
| 功能是否正常 | 各模块列表页可打开、核心交互可用 |
| 超链接是否可跳转 | 侧栏、「查看全部」、工作台条目、通知深链、快捷创建 |
| 根因定位 | 区分前端路由问题 vs 后端 API / 日志错误 |

默认环境：

| 项 | 值 |
|----|-----|
| 前端 | http://localhost:3000 |
| 后端 | http://localhost:8080 |
| 演示账号 | `admin` / `123456`（Login.vue 预填） |
| 应用日志 | `logs/personal-hub.log` |
| 错误日志 | `logs/personal-hub-error.log` |

---

## 2. 工具组合（三件套）

不要只用一种手段：

```
┌─────────────────────┐
│ Cursor Browser MCP  │  人工复核、看截图、点弹窗、读 Console
└──────────┬──────────┘
           │
┌──────────▼──────────┐
│ Playwright 批量脚本 │  扫全路由 / 深链 / API≥400 / 摘要 JSON
└──────────┬──────────┘
           │
┌──────────▼──────────┐
│ 后端双日志对照      │  error.log 为空？app.log 有无 Exception？
└─────────────────────┘
```

| 工具 | 适用 | 产物 |
|------|------|------|
| **cursor-ide-browser** MCP | 关键路径复核、弹窗、通知角标 | 截图 + Console |
| **Playwright**（`scripts/qa/full_feature_test.py`） | 16+ 路由批量、深链、工作台条目 | `logs/qa-artifacts/full_feature_result.json` + screenshots |
| **日志** | 判定是否后端故障 | ERROR / Exception 摘录 |

---

## 3. Cursor Browser MCP 标准流程

### 3.1 调用顺序（必须遵守）

1. `browser_tabs`（list）— 看是否已有页签  
2. `browser_navigate` — 打开目标 URL（可截图）  
3. `browser_lock` — 锁定后再点选  
4. `browser_snapshot` — 拿可交互元素 `ref`  
5. `browser_click` / `browser_fill` / … — 用最新 ref  
6. `browser_wait_for` — 等文案或固定秒数（参数为**秒**）  
7. 需要时：`browser_console_messages`、`browser_network_requests`  
8. `browser_unlock` — **结束前必须解锁**

### 3.2 实操要点

- **先 snapshot 再 click**：ref 过期会报 `Stale element reference`，重新 snapshot 即可。  
- **SPA 标题是关键信号**：正常页为 `{模块} | Personal Hub`；仅 `Personal Hub` 往往表示**子路由未匹配**（主内容空白）。  
- **Vue Router 警告**：Console 出现  
  `No match found for location with path "/xxx/..."`  
  → 前端深链问题，通常**不会**写入 `personal-hub-error.log`。  
- **编辑器类页面**：Vditor 可能先空白 1–3 秒，应用 `browser_wait_for` 再判定失败。  
- **侧栏 click 偶发 URL 延迟**：重要断言优先用**直访 URL** + 标题/弹窗，再辅以侧栏点击。

### 3.3 推荐复核清单

| 步骤 | URL / 操作 | 期望 |
|------|------------|------|
| 登录后首页 | `/dashboard` | 问候语、卡片、角标 |
| 侧栏模块 | `/notes` … `/stats` | 标题含模块名 |
| 「查看全部」 | `/todos`、`/notes` 等 | 落到列表页 |
| 深链弹窗 | `/study-plans?edit=1` | 打开编辑弹窗 |
| 旧路径兼容 | `/study-plans/1/edit` | redirect → 列表 + 弹窗 |
| 笔记编辑 | `/notes/{id}/edit` | 标题可用，骨架后出现正文 |
| 通知 | 点铃铛 | 面板可开；条目跳转不空白 |

---

## 4. Playwright 批量巡检

### 4.1 脚本位置

| 路径 | 说明 |
|------|------|
| `scripts/qa/full_feature_test.py` | 巡检脚本（纳入版本库） |
| `logs/qa-artifacts/full_feature_result.json` | 运行结果（gitignore） |
| `logs/qa-artifacts/screenshots/` | 截图（gitignore） |

### 4.2 Windows 启动方式

本机若未安装 Playwright 自带 Chromium，使用系统 Chrome，并关闭 headless shell：

```powershell
$env:PLAYWRIGHT_CHROMIUM_USE_HEADLESS_SHELL = '0'
python scripts/qa/full_feature_test.py
```

脚本内关键策略：

- `chromium.launch(headless=True, channel="chrome")`
- `page.goto(..., wait_until="domcontentloaded")`  
  （本项目有通知轮询，`networkidle` 易超时）
- 每步 `save()` 写入 JSON，避免中途崩溃丢结果

### 4.3 脚本覆盖范围（与路由表对齐）

1. 登录 `admin` / `123456`  
2. 直访已注册路由：dashboard / notes / diaries / readings / study-records / todos / study-plans / bookmarks / files / tags / categories / settings / stats / search / recycle 等  
3. 侧栏文案点击  
4. 工作台「查看全部」与 `.list-item`  
5. 可疑深链：`/xxx/new`、`/xxx/:id/edit`  
6. 用 JWT 调后端列表 API 取真实 id，再验编辑深链  
7. 汇总 `api_errors` / `console_errors` / `summary`

### 4.4 依赖

```bash
# 仓库外环境已装 playwright 时
pip show playwright

# 前端单测（与浏览器巡检独立）
cd personal-hub-web && pnpm test
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
| `personal-hub-error.log` 为 0 字节 | 本会话无 ERROR 落盘 |
| app.log 仅 JWT DEBUG / 业务 INFO | 后端链路正常 |
| 前端空白 + Router warn、日志无 ERROR | **前端路由/深链**问题 |
| app.log 出现 ERROR + API 500 | **后端**问题，优先修服务端 |

巡检时前端空白**未必**伴随后端错误——2026-07-15 案例即如此。

---

## 6. 深链与弹窗规范（测试关注点）

Dialog/Drawer 类模块**不得**依赖未注册的独立编辑页路由。正确形态：

| 动作 | URL |
|------|-----|
| 编辑 | `/todos?edit=10`、`/study-plans?edit=2` … |
| 新建 | `/study-records?create=1`、`/diaries?create=1` |
| 旧路径 | `/todos/10/edit` → redirect 到上表 |

实现入口：

- `src/utils/deepLink.ts`
- `src/composables/useDeepLinkDialog.ts`
- `src/router/index.ts`（redirect）
- `docs/STYLE_GUIDE.md`（约定说明）

笔记仍为独立页：`/notes/:id/edit`、`/notes/:id/preview`。

---

## 7. 报告产出规范

每次正式巡检应留下：

1. **Markdown 报告** → `docs/qa/YYYY-MM-DD-功能巡检报告.md`  
2. **原始 JSON** → `logs/qa-artifacts/full_feature_result.json`  
3. **截图** → `logs/qa-artifacts/screenshots/`  
4. 若有修复 → 更新报告「修复回写」节 + `docs/CHANGELOG.md`

报告最少包含：结论摘要、日志对照、路由表、失败深链、根因、优先级。

参考样例：`docs/qa/2026-07-15-功能巡检报告.md`。

---

## 8. Agent 检查清单（复制用）

```markdown
- [ ] 前端 3000 / 后端 8080 可达
- [ ] Browser：navigate → lock → 关键路径 snapshot
- [ ] Playwright：跑 full_feature_test.py，看 summary
- [ ] 读 personal-hub-error.log（体积）与 app.log ERROR/Exception
- [ ] 核对 Console 是否有 Vue Router No match
- [ ] 深链：?edit= / 旧 /:id/edit redirect / 笔记编辑页
- [ ] 写 docs/qa 报告（含证据路径）
- [ ] browser_unlock
```

---

## 9. 相关文件索引

| 路径 | 说明 |
|------|------|
| `.cursor/rules/browser-qa-testing.mdc` | Cursor Agent 规则（本方法摘要） |
| `docs/qa/CURSOR_BROWSER_TESTING.md` | 本文（完整方法） |
| `docs/qa/2026-07-15-功能巡检报告.md` | 首次全量实战报告 |
| `scripts/qa/full_feature_test.py` | 批量巡检脚本 |
| `personal-hub-web/src/router/index.ts` | 路由与 redirect |
| `personal-hub-web/src/utils/deepLink.ts` | 深链工具 |
