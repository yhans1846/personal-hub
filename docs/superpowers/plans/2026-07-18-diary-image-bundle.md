# 日记配图资源包实现计划

> **面向 AI 代理的工作者：** 使用 executing-plans 或 subagent-driven-development 逐任务实现。步骤用 `- [ ]` 跟踪。

**目标：** 日记配图存 `diaries/{id}/images/`，库字段 `image_files`（文件名 JSON）；不进 `file_resource`；文件页只管本页上传。

**架构：** 仿 `NoteFileService` 新增 `DiaryFileService`；CRUD 字段改为 `List<String> imageFiles`；前端有 diaryId 后才上传。日记删除时删盘（日记无回收站，与笔记软删不同）。

**技术栈：** Spring Boot 3 · MyBatis-Plus · Vue 3 · StorageService

---

## 文件结构

| 文件 | 职责 |
|------|------|
| `sql/alter_2026-07-18_diary_image_files.sql` | 列改名 |
| `sql/init.sql` | 对齐 |
| `DiaryEntry.java` / DTO / VO | `imageFiles` |
| `DiaryFileService` + Impl | 上传/读/删单图 + 更新列表 |
| `DiaryEntryController` | 图片 3 个端点 |
| `DiaryEntryServiceImpl` | JSON 序列化；delete 时 `storageService.delete("diaries/"+id)` |
| `DiaryDialog.vue` / `List.vue` / `api.ts` / `types/diary.ts` | 新 API |
| `docs/DATABASE.md` / `API.md` / `CHANGELOG.md` | 文档 |

---

### 任务 1：DB + Entity/DTO/VO

- [ ] 改名列并更新 Entity 解析为 `List<String>`
- [ ] DTO/VO 字段替换
- [ ] 本地执行 ALTER

### 任务 2：DiaryFileService + Controller

- [ ] 上传写入 `diaries/{id}/images/{uuid}.ext`，append 到 `image_files`
- [ ] GET / DELETE 端点；filename 禁止 `..` 与 `/`
- [ ] 删日记时删 `diaries/{id}` 目录

### 任务 3：前端

- [ ] 新建：先保存拿 id，再允许传图
- [ ] 预览 URL：`/api/diaries/{id}/images/{filename}`（blob + JWT）
- [ ] 去掉对 `uploadFile` / `getFilePreviewUrl(fileId)` 的日记依赖

### 任务 4：文档 + 规格状态

- [ ] 更新 DATABASE / API / CHANGELOG；规格状态改为已实现

---

## 新建日记 UX（写死）

1. 用户点保存 → `POST /api/diaries`（可无图）→ 得到 `id`
2. 弹窗切为编辑态（保留打开）→ 启用上传
3. 上传每张立即 `POST .../images`；排序/删图后 `PUT` 同步 `imageFiles`（或删图走 DELETE 接口已更新库，排序只 PUT）
