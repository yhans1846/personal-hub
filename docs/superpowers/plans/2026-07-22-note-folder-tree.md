# 笔记文件夹树 实现计划

> **面向 AI 代理的工作者：** 必需子技能：使用 superpowers:subagent-driven-development（推荐）或 superpowers:executing-plans 逐任务实现此计划。步骤使用复选框（`- [ ]`）语法来跟踪进度。

**目标：** 笔记列表左侧文件夹树（CRUD + 拖拽），笔记单归属 `folder_id`；删夹笔记归未分类。

**架构：** 新表 `note_folder` + `note_note.folder_id`；`/api/note-folders` 管树；笔记列表/创建/移动扩展筛选与归属。前端仅 `List.vue` 左树，不进编辑器。

**技术栈：** Spring Boot 3 · MyBatis-Plus · Vue 3 · 原生 HTML5 DnD（对齐现有列表交互）

**规格：** `docs/superpowers/specs/2026-07-22-note-folder-tree-design.md`

---

## 文件结构

| 文件 | 职责 |
|------|------|
| `sql/init.sql` + `sql/alter-20260722-note-folder.sql` | 表与列 |
| `entity/NoteFolder.java` · `mapper/NoteFolderMapper.java` | 文件夹持久化 |
| `entity/Note.java` · `vo/NoteVO` · DTO | `folderId` 字段 |
| `dto/NoteQueryDTO` · `NoteMapper.xml` | 列表按夹筛选 |
| `service/NoteFolderService(+Impl)` · `controller/NoteFolderController` | 树 CRUD / move / delete |
| `NoteService` · `NoteController` | create/update/`PATCH .../folder` |
| `backup` 相关 | 导出/导入含 `note_folder`（若现有按表枚举） |
| `web/.../api.ts` · `types` | 前端 API |
| `NoteFolderTree.vue` · `List.vue` | 左树 + 筛选 + DnD |
| `docs/DATABASE.md` · `API.md` · `PAGE_SPEC.md` · `CHANGELOG.md` | 契约同步 |

---

### 任务 1：数据库

**文件：** `sql/init.sql` · `sql/alter-20260722-note-folder.sql` · `docs/DATABASE.md`

- [x] **步骤 1：** `init.sql` 增加 `note_folder` 表；`note_note` 增加 `folder_id` + 索引
- [x] **步骤 2：** 写 alter 脚本供存量库
- [x] **步骤 3：** 更新 `DATABASE.md`
- [ ] **步骤 4：** Commit `feat(db): note_folder 表与 note.folder_id`

### 任务 2：后端文件夹服务（TDD 环检测 / 删除）

**文件：** `NoteFolder*` 全套 · 单测 `NoteFolderServiceTest`

- [x] **步骤 1：** 写失败测试：move 成环抛业务异常；delete 后笔记 folderId 清空
- [x] **步骤 2：** 实现 Entity/Mapper/Service/Controller（`/api/note-folders`）
- [x] **步骤 3：** 测试通过 · Commit `feat(notes): note-folders API`

### 任务 3：笔记列表与归属

**文件：** `Note.java` · `NoteVO` · `NoteCreateDTO`/`UpdateDTO` · `NoteQueryDTO` · `NoteMapper.xml` · `NoteService` · `NoteController`

- [x] **步骤 1：** 实体/DTO/VO 增加 `folderId`；Query 用 `String folderId`（`none` | 数字 | 缺省=全部）
- [x] **步骤 2：** XML 筛选；create/update 写 folderId；`PATCH /api/notes/{id}/folder`
- [ ] **步骤 3：** Commit `feat(notes): 列表与笔记支持 folderId`

### 任务 4：备份纳入文件夹（若改动面小）

**文件：** `DataBackupServiceImpl` 等

- [x] **步骤 1：** 备份/恢复序列包含 `note_folder` 与笔记 `folder_id`
- [ ] **步骤 2：** Commit `feat(backup): 纳入 note_folder`

### 任务 5：前端 API + 树组件

**文件：** `knowledge/api.ts` · `types` · `NoteFolderTree.vue`

- [x] **步骤 1：** API 封装 tree/create/rename/move/delete、`updateNoteFolder`
- [x] **步骤 2：** `NoteFolderTree`：全部/未分类/树、展开、CRUD 菜单、接受 drop
- [ ] **步骤 3：** Commit `feat(notes): NoteFolderTree 组件`

### 任务 6：List 接线 + DnD

**文件：** `note/List.vue` · Overlay 创建传 folderId

- [x] **步骤 1：** plan-middle 左树右列表；选中驱动 `folderId` 查询
- [x] **步骤 2：** 卡片/行 draggable → 树；树节点拖到树；新建笔记带 folderId
- [ ] **步骤 3：** ≤768 树抽屉 · Commit `feat(notes): 列表文件夹树与拖拽`

### 任务 7：文档收口

**文件：** `API.md` · `PAGE_SPEC.md` · `CHANGELOG.md` · `PROJECT.md`

- [x] **步骤 1：** 同步契约与 changelog
- [ ] **步骤 2：** Commit `docs: 笔记文件夹树契约`

---

## 验收对照规格 §8

跑通规格验收清单后再宣称完成。
