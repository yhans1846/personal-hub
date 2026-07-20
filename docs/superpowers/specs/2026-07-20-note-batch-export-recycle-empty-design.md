# 笔记批量 MD 导出 + 回收站清空

> 日期：2026-07-20 · 已批准  
> 范围：P0-A。日记导出、筛选/全部导出、`meta.json`、检索升级（P0-B）、快捷捕获（P0-C）不在本文。

## 1. 目标

- 用户在笔记列表勾选多篇笔记，一次下载含正文与资源的 ZIP，可在 Obsidian / Typora 等工具中直接打开。
- 用户可一键清空回收站（物理删除全部已删/已归档笔记及磁盘目录）。

成功标准：

- 勾选 N 篇（1≤N≤50）导出后，ZIP 内每篇独立目录，`note.md` 相对路径图片/附件可打开。
- 非本人 / 已删 / 不存在的 id 不进入包；若有效 id 为 0 则 400。
- 清空回收站后列表为空，对应 `notes/{id}/` 目录已删，不可恢复。

## 2. 批量导出

### 2.1 API

| 项 | 约定 |
|----|------|
| 方法路径 | `POST /api/notes/export` |
| Body | `{ "ids": number[] }`（必填，去重后 1–50） |
| 鉴权 | JWT；仅导出 `user_id` 匹配且 `is_deleted=0` 的笔记 |
| 成功 | `200`，`Content-Type: application/zip`，`Content-Disposition: attachment; filename="notes-export-yyyyMMdd-HHmm.zip"` |
| 失败 | ids 空/超限 → 400；无有效笔记 → 400「没有可导出的笔记」 |

实现：扩展 `NoteExportService`（复用单篇读盘与 `sanitizeFileName`），禁止前端 N 次单篇导出再拼包。

### 2.2 ZIP 布局

对齐 `2026-07-11-file-storage-design` §7.2：

```
notes-export-yyyyMMdd-HHmm.zip
  {安全标题}/
    note.md
    images/
    attachments/
  {安全标题} (123)/          ← 标题冲突时后缀 (id)
    note.md
    ...
```

- 正文：读 `notes/{id}/note.md`；缺失则写空 `note.md`，不中断整包。
- 资源：列出并写入 `images/`、`attachments/`；单文件失败打 warn 跳过。
- **不**写入标签/分类 `meta.json`（本版不做）。
- 单篇 `GET /api/notes/{id}/export` 保持现有扁平结构（`{title}.md` + `images/`），本版不改，避免破坏已有下载习惯。

### 2.3 前端

| 项 | 约定 |
|----|------|
| 入口 | 笔记列表：多选模式 + 工具栏「导出 ZIP」 |
| 启用条件 | 至少勾选 1 篇；超过 50 前端拦截提示 |
| 交互 | 点击后 loading；成功 `triggerBlobDownload`；失败 `handleApiError` |
| 范围 | **仅勾选**；不做「当前筛选 / 全部」 |

多选 UI：与现有列表 Token / `ListToolbar` 一致；退出多选清空勾选。不引入新路由。

### 2.4 不做

- 日记 / 收藏等其它模块 MD 导出
- 无勾选时导出筛选结果或全部
- 异步任务队列、进度 SSE
- 导出回收站内笔记

## 3. 回收站清空

### 3.1 API

| 项 | 约定 |
|----|------|
| 方法路径 | `DELETE /api/notes/recycle-bin` |
| 行为 | 当前用户所有 `is_deleted=1` 的笔记：复用 `permanentDelete` 语义（删库行、分类/标签关联、磁盘 `notes/{id}/`） |
| 成功 | `200` + `Result`，body 可含 `{ "deleted": n }` |
| 空站 | 仍 200，`deleted: 0` |

事务：库操作同事务；磁盘删除失败记 warn，不回滚已删库行（与现网单篇永久删除一致）。

### 3.2 前端

- 回收站页 `PageHeader` / 工具栏增加「清空回收站」（danger）。
- `ElMessageBox.confirm`：明确「将永久删除回收站内全部笔记及附件，不可恢复」。
- 成功提示删除数量并刷新；空站按钮可禁用或点击后提示「回收站已空」。

### 3.3 不做

- 按筛选条件清空、定时清空、清空前再导出。

## 4. 文档同步（实现时）

- `docs/API.md`：补充 `POST /api/notes/export`、`DELETE /api/notes/recycle-bin`
- `docs/PAGE_SPEC.md`：笔记列表多选导出；回收站清空
- `docs/PROJECT.md`：MD 批量导出标完成（或移出「后续」）
- `docs/CHANGELOG.md`：当日条目

## 5. 测试要点

- 导出 1 篇 / 多篇同标题 / 含图附件 / 混入他人 id 与已删 id
- ids 空、51 篇、全无效 id
- 清空：有数据 / 空站；清空后单篇恢复接口 404；磁盘目录不存在
