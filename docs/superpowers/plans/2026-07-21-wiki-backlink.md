# 双向链接 v1 实现计划

> **面向 AI 代理的工作者：** 按任务顺序实现；步骤用复选框跟踪。

**目标：** `[[标题]]` 补全、预览跳转、回链列表；实验开关可开默认关。

**架构：** 前端 `wikiLinkUtils` 解析/预处理；Vditor 旁浮层补全；后端扫 `note.md` 提供 backlinks；无新表。

**技术栈：** Vue 3 · Vditor · Spring Boot · Vitest / JUnit（可选）

---

## 文件

| 文件 | 职责 |
|------|------|
| `wikiLinkUtils.ts` + test | 解析、预处理为 HTML/Markdown 链接 |
| `useWikiLinkAutocomplete.ts` | `[[` 补全浮层 |
| `NoteVditor.vue` / Preview / Editor | 接线 |
| `NoteBacklinks.vue` | 回链列表 UI |
| `NoteController` + `NoteService` | `GET /{id}/backlinks` |
| `featureFlagStore` | available + 文案 |
| `API.md` / `PAGE_SPEC` / `PROJECT` / `CHANGELOG` | 文档 |

### 任务 1：wikiLinkUtils + 单测

### 任务 2：后端 backlinks

### 任务 3：开关 + API 客户端

### 任务 4：编辑器补全

### 任务 5：预览转换 + 回链面板

### 任务 6：文档与提交
