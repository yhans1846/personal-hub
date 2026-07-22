# 日记链接配图 + 清空数据 实现计划

> **面向 AI 代理的工作者：** 逐任务实现；步骤用复选框跟踪。规格：`docs/superpowers/specs/2026-07-23-diary-image-url-and-data-purge-design.md`

**目标：** 日记配图支持 URL 下载；设置·数据「清空数据」：字符图验证码 → 先备份 → 清空业务（保留账号/资料/layout）→ 只留该备份并下载。

**架构：** 日记复用 `ResourceResolver` HTTP 拉取写入 `DiaryFileService`；新建 `ImageCaptchaService`（Redis）+ `DataPurgeService`（调 `createStoredBackup` 后按域清理，保留 `user_layout` 与快照备份）。

**技术栈：** Spring Boot 3、Redis、Vue 3、现有 backup/diary API。

---

## 文件结构

| 文件 | 职责 |
|------|------|
| `ph-system/.../ImageCaptchaService.java` + Impl + Controller | 字符图验证码 |
| `ph-biz/.../DataPurgeService.java` + Impl + Controller | 清空编排 |
| `ph-biz/.../BackupDataMapper.java` (+XML 若需) | 增删通知/审计 SQL；purge 不清 layout |
| `DiaryFileService(+Impl)` / `DiaryEntryController` | from-url |
| `DiaryImageFromUrlDTO.java` | body |
| `DataManagement.vue` / `DiaryImagePanel.vue` / `api.ts` | UI |
| `docs/API.md` `PAGE_SPEC.md` `CHANGELOG.md` | 文档 |

### 任务 1：日记 from-url 后端 + 前端
### 任务 2：字符图验证码 API
### 任务 3：DataPurgeService + API
### 任务 4：DataManagement 清空 UI
### 任务 5：文档同步 + 验证

---

### 任务 1：日记配图 from-url

**文件：** `DiaryFileService.java`、`DiaryFileServiceImpl.java`、`DiaryEntryController.java`、`DiaryImageFromUrlDTO.java`、`api.ts`、`DiaryImagePanel.vue`

- [ ] `uploadImageFromUrl(diaryId, userId, url)`：`ResourceResolver.resolve(url, null)`；失败抛 BusinessException；校验图片扩展名（png/jpg/jpeg/gif/webp）；`storeBytes` + 追加 `image_files`
- [ ] `POST /{id}/images/from-url`
- [ ] 前端按钮 + Prompt/对话框输入 URL
- [ ] Commit：`feat(diary): 配图支持从链接下载`

### 任务 2：字符图验证码

- [ ] `ImageCaptchaService`：生成 4 位字符、BufferedImage、Redis `image-captcha:{id}` TTL 120s、`verifyAndConsume` 忽略大小写
- [ ] `GET /api/security/image-captcha`（需登录，勿 permitAll）
- [ ] Commit：`feat(security): 危险操作字符图验证码`

### 任务 3：清空数据

- [ ] `DataPurgeService.purge(userId, captchaId, code)`：验码 → `createStoredBackup(MANUAL)` → DB 清（复用 BackupDataMapper 顺序但**跳过 deleteLayouts**；另清 notification、audit_log）→ 删磁盘 notes/diaries/uploads（用户相关）→ 删其它 backup 只留快照 → 返回 VO
- [ ] `POST /api/data/purge`
- [ ] Commit：`feat(data): 验证码通过后备份并清空业务数据`

### 任务 4：设置 UI

- [ ] `DataManagement.vue` 危险区 + 对话框（图/输入/刷新）→ purge → downloadBackup → 刷新列表
- [ ] Commit：`feat(settings): 数据管理清空数据入口`

### 任务 5：文档

- [ ] 更新 API / PAGE_SPEC / CHANGELOG
- [ ] Commit：`docs: 同步日记链接配图与清空数据`
