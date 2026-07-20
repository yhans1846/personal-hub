# 笔记批量 MD 导出 + 回收站清空 实现计划

> **面向 AI 代理的工作者：** 必需子技能：使用 superpowers:executing-plans 或 subagent-driven-development 逐任务实现。步骤使用复选框跟踪。

**目标：** 笔记列表勾选导出 ZIP（≤50）；回收站一键清空。

**架构：** 扩展 `NoteExportService` 批量打 ZIP（每篇一目录）；`NoteService.emptyRecycleBin` 复用 `permanentDelete`；前端列表多选 + 回收站按钮。

**技术栈：** Spring Boot 3 · MyBatis-Plus · Vue 3 · JUnit 5

**规格：** `docs/superpowers/specs/2026-07-20-note-batch-export-recycle-empty-design.md`

---

## 文件

| 文件 | 职责 |
|------|------|
| `ph-biz/.../dto/NoteBatchExportDTO.java` | ids 校验 |
| `ph-biz/.../service/NoteExportService.java` | `exportNotes` |
| `ph-biz/.../controller/NoteController.java` | POST `/export`、DELETE `/recycle-bin` |
| `ph-biz/.../service/NoteService.java` + Impl | `emptyRecycleBin` |
| `ph-biz/.../vo/RecycleEmptyVO.java` | `{ deleted }` |
| `ph-biz/src/test/.../NoteExportServiceBatchTest.java` | 校验与目录名纯逻辑 / mock 导出 |
| `personal-hub-web/.../knowledge/api.ts` | `exportNotesBatch`、`emptyRecycleBin` |
| `personal-hub-web/.../note/List.vue` | 多选 + 导出 |
| `personal-hub-web/.../note/RecycleBin.vue` | 清空按钮 |
| `docs/API.md` · `PAGE_SPEC.md` · `PROJECT.md` · `CHANGELOG.md` | 同步 |

---

### 任务 1：后端批量导出

- [ ] 写 `NoteBatchExportDTO`（`@NotEmpty` `@Size(max=50)` `List<Long> ids`）
- [ ] 写失败测试：空 ids / >50 / 目录名冲突后缀（可测 `sanitize`/`uniqueFolder` 包内方法）
- [ ] 实现 `exportNotes(List<Long> ids, Long userId)`：去重→查归属且未删→无有效则 400→ZIP 每篇 `{title}/note.md|+images|+attachments`，冲突加 ` (id)`
- [ ] Controller `POST /api/notes/export` 返回 zip 流
- [ ] 运行测试通过并 commit

### 任务 2：后端清空回收站

- [ ] `emptyRecycleBin(userId)`：查出该用户 `is_deleted=1` 全部 id，逐个 `permanentDelete`，返回 deleted 数
- [ ] `DELETE /api/notes/recycle-bin` → `Result<RecycleEmptyVO>`
- [ ] commit

### 任务 3：前端

- [ ] api：`exportNotesBatch(ids)` blob POST；`emptyRecycleBin()`
- [ ] List：多选模式、勾选、导出 ZIP（≤50）、`triggerBlobDownload`
- [ ] RecycleBin：「清空回收站」二次确认
- [ ] commit

### 任务 4：文档

- [ ] 更新 API / PAGE_SPEC / PROJECT / CHANGELOG
- [ ] commit（不 push）
