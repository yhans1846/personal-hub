# 笔记首页壳 实现计划

> **面向 AI 代理的工作者：** 按任务顺序实现；步骤用复选框跟踪。

**目标：** `/notes` 默认首页；侧栏夹+笔记；点夹保留卡片/表格列表。

**规格：** `docs/superpowers/specs/2026-07-22-note-home-shell-design.md`

## 文件

| 文件 | 职责 |
|------|------|
| `NoteFolderVO` / `NoteFolderTreeVO` / Service | 树节点带 `notes`；未分类笔记列表 |
| `types/note.ts` · `api.ts` | 类型与解包 |
| `NoteHome.vue` | 首页 UI |
| `NoteFolderTree*.vue` | 笔记子节点、首页选中 |
| `List.vue` | `home\|list` 切换 |
| `docs/API.md` · `PAGE_SPEC.md` · `CHANGELOG.md` | 契约 |

### 任务 1：后端树带笔记摘要
- [x] VO 增加 `NoteFolderNoteItem`；tree 查询未删除笔记填入各夹与 uncategorizedNotes
- [x] 单测或编译通过

### 任务 2：前端类型与树展示笔记
- [x] types/API；树展开渲染笔记；点击 emit 打开笔记

### 任务 3：NoteHome + List 壳切换
- [x] NoteHome；List 默认 home；点夹 list；点首页 home

### 任务 4：文档
- [x] API / PAGE_SPEC / CHANGELOG
