# 日记配图链接下载 + 设置清空数据

> 日期：2026-07-23 · 已批准（对话确认）  
> 前置：`2026-07-20-auto-backup-history-design`、日记配图 `DiaryFileService`、导入侧 `ResourceResolver`  
> 范围：日记面板「从链接添加」配图；设置·数据「清空数据」+ 字符图验证码。  
> 非目标：笔记编辑器链接配图、粘贴 URL 自动识别、批量 URL、第二个「清空文件数据」按钮、登录密码二次确认、复用书架验证码。

## 1. 目标与成功标准

### 1.1 日记配图链接

- 已保存日记的配图区可输入 http(s) 图片 URL，服务端下载并写入现有 `diaries/{id}/images/`，行为与本地上传一致（缩略图、排序、封面）。
- 成功标准：合法外链可插入；内网/非图片/过大/不可达有明确错误且不污染 `image_files`。

### 1.2 清空数据

- 设置 → 数据管理仅保留一个危险按钮「清空数据」。
- 流程：字符图验证码通过 → **先打一份备份** → 清空业务与附属数据 → **备份历史只留刚打的那条** → 前端触发本地下载。
- 成功标准：账号/密码/资料/`user_layout` 仍在；业务数据与通知、审计、旧备份消失；历史可见且可下载/恢复刚留的那条 ZIP。

## 2. 日记配图：从链接添加

### 2.1 UI

- 位置：`DiaryImagePanel` 现有 dropzone 旁（或下方）「从链接添加」。
- 条件：与本地上传相同，`canUpload`（日记已保存）。
- 交互：弹输入框填 URL → 确认 → loading → 成功刷新网格；失败 toast。
- v1 单次一个 URL；可多次点击添加多张。

### 2.2 API

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | `/api/diaries/{id}/images/from-url` | body `{ "url" }` → `{ "name": "<filename>" }` |

- 鉴权 + 日记归属校验，同现有上传。
- 复用 `ResourceResolver` 的 HTTP 拉取语义：仅 http/https；禁内网/本机；不跟随重定向；≤20MB。
- 额外要求：内容为图片（Content-Type 与/或扩展名）；写入 `StoragePaths.diaryImage`，UUID 文件名，追加 `image_files`。
- 错误：400 + 可读 message（无效 URL、非图片、过大、下载失败、日记未保存等）。

### 2.3 实现要点

- `DiaryFileService` 新增 `uploadImageFromUrl`；控制器挂在现有日记配图分组。
- 前端 `uploadDiaryImageFromUrl`；不改笔记模块。

## 3. 清空数据 + 字符图验证码

### 3.1 保留 / 删除范围

**保留**

| 项 | 说明 |
|----|------|
| 登录账号 / 密码 | `sys_user.username` / 密码哈希 |
| 用户资料 | nickname、avatar、email、gender、birthday、phone、地区、website、github、bio 等 |
| 用户配置 | 全部 `user_layout`（含 data 备份频率等） |
| 清空前快照 | 刚生成的 1 条 `user_backup`（OK）+ 对应 ZIP |
| 头像文件 | `avatars/` 中当前用户头像（随资料保留） |

**删除（当前用户）**

| 域 | 说明 |
|----|------|
| 笔记 | `note_note`、`note_folder`、关联表；磁盘 `notes/{id}/` |
| 日记 | `diary_entry`；磁盘 `diaries/{id}/` |
| 待办 / 收藏 / 学习 / 阅读 | 对应业务表及关联 |
| 文件库 | `file_resource`；磁盘 `uploads/`（用户相关） |
| 标签 / 分类 | `tag`、`category`、`tag_rel`、`note_category_rel` 等 |
| 通知 | `sys_notification`（用户相关） |
| 审计 | `audit_log`（用户相关） |
| 旧备份 | 除快照外的 `user_backup` 行与 ZIP |

### 3.2 字符图验证码

- 独立于登录书架验证码。
- `GET /api/security/image-captcha`（需登录）→ `{ captchaId, imageBase64 }`（或等价 data URL）。
- 服务端生成 4–6 位易辨字符（大小写不敏感匹配），图中可加轻微干扰线；答案存 Redis，TTL 120s，一次性。
- 校验失败：作废该 id，前端刷新新图。
- 仅用于危险操作；清空接口必须 JWT。

### 3.3 清空 API

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/api/security/image-captcha` | 取图 |
| POST | `/api/data/purge` | body `{ captchaId, captchaCode }` → `{ backupId, fileSize, createdAt }` |

`trigger_type`：快照记为 `MANUAL`（与立即备份一致；日志标明 purge 来源即可，避免破现有枚举查询）。

### 3.4 服务流程

1. 校验验证码；失败立即返回，不改数据。
2. `createStoredBackup(userId, MANUAL)`（与立即备份同打包语义；ZIP **不含** `backups/`）。
3. 执行用户域清空（DB + 磁盘业务目录）；**不得**删除 `sys_user` 账号字段、`user_layout`、头像、步骤 2 的备份文件。
4. 删除该用户其余 `user_backup`（文件+行），**仅保留步骤 2 的 id**。
5. 返回该备份元数据；前端调用现有 `GET /api/backup/{id}/download` 触发下载，并刷新历史列表。

**失败策略**

- 步骤 2 失败：整体失败，数据未清。
- 步骤 3 失败：返回错误，**保留步骤 2 备份**（可恢复）；尽量按模块顺序清理并记录，避免静默半清空。
- 步骤 4 失败：业务已清，补删旧备份可重试或记日志；快照必须仍在。

### 3.5 UI（`DataManagement.vue`）

- 危险区：「清空数据」按钮（危险样式）。
- 对话框：后果说明（不可撤销、将先备份再清空）+ 验证码图 + 输入框 + 刷新验证码 + 确认/取消。
- 确认后禁用按钮防重复；成功 toast + 自动下载 + 刷新备份列表（应仅 1 条）。
- 现有缓存清理 / 立即备份 / 频率 / 历史 / 导入 ZIP 行为不变。

## 4. 文档与测试

实现时同步：`docs/API.md`、`docs/PAGE_SPEC.md`、`docs/CHANGELOG.md`；本功能无新表则可不改 `DATABASE.md`。

建议验证：

- 日记：外链图成功；内网 URL 拒绝；非图片拒绝。
- 清空：验证码错误不备份；正确则备份→清空→历史仅 1 条且可下载恢复；登录与 layout/外观仍在。

## 5. 决策摘要

| 项 | 结论 |
|----|------|
| 验证 | 仅字符图验证码（非密码、非书架） |
| 清空按钮 | 只保留「清空数据」一个 |
| 备份 | 验码通过后先备份；清后历史只留该条 + 本地下载 |
| 配图 | 日记面板「从链接添加」，非笔记编辑器 |
