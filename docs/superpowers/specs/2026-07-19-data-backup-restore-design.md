# 数据备份与恢复（手动完整包）设计

> 日期：2026-07-19 · 已批准  
> 范围：本版仅手动「立即备份」+「导入覆盖」；不含自动备份、备份历史表、分模块勾选导出。

## 1. 目标

为当前登录用户提供可下载的完整数据快照（业务库表 + 对应磁盘文件），并支持用备份包**全量覆盖**恢复到当前账号。

成功标准：

- 备份后换机/清库，用同一账号导入，业务数据与笔记正文/配图/附件/文件库文件可用
- 恢复失败不留下半成品业务数据（库事务回滚；文件经临时区再替换）
- 密码、登录名、通知、审计不受影响

## 2. 范围

### 2.1 备份（含）

| 表 / 项 | 说明 |
|---------|------|
| `note_note` | 含回收站 |
| `note_category_rel` | |
| `category` | 全部 type |
| `tag` / `tag_rel` | |
| `diary_entry` | |
| `todo_task` | |
| `bookmark_url` | |
| `study_record` / `study_plan` | |
| `reading_record` | |
| `file_resource` | |
| `user_layout` | menu / dashboard / preview |
| `sys_user` 资料字段 | nickname、avatar、email、gender、birthday、phone、地区、website、github、bio（**不含** id/username/password） |

磁盘（相对 storage 根，打入 ZIP `files/`）：

- `notes/{id}/**`
- `diaries/{id}/**`
- `uploads/**` 中属于该用户 `file_resource.path` 的文件
- `avatars/{filename}`（由 avatar URL `/api/files/avatar/{filename}` 解析）

缺文件：写入 `manifest.warnings`，仍生成包（不中断）。

### 2.2 不备份

| 项 | 说明 |
|----|------|
| `sys_notification` | 整表 |
| `audit_log` | 整表 |
| `sys_user.username` / `password` | 登录身份与密码不迁移；恢复后仍是当前登录账号 |
| Redis / 仅前端 localStorage | |

### 2.3 恢复语义

对**当前登录用户**：先清空其业务数据与上列文件树，再写入备份内容；`user_id` 一律改为当前用户；**保留包内主键 ID**（便于 `notes/{id}` 路径对齐）。

## 3. 架构

- 包格式：`personal-hub-backup-yyyyMMdd-HHmmss.zip`
- 同步请求；不落 `user_backup` 表；ZIP 经临时文件生成后流式下载
- 实现落在 **ph-biz**（聚合业务表）；删除 ph-system 占位 `BackupController`
- 编排：`DataBackupService`；存储复用 `StorageService` / `StoragePaths`
- 默认包大小上限：512MB（可配置）；超限明确错误

```
manifest.json
data/
  notes.json, note_folders.json, categories.json, tags.json, tag_rels.json,
  note_category_rels.json, diaries.json, todos.json,
  bookmarks.json, study_records.json, study_plans.json,
  readings.json, files.json, layouts.json, profile.json
files/
  notes/...
  diaries/...
  uploads/...
  avatars/...
```

`manifest.json` 字段：`schemaVersion`（1）、`app`（personal-hub）、`createdAt`、`sourceUserId`（仅提示）、`modules[]`、`warnings[]`。

## 4. API

| 方法 | 路径 | 行为 |
|------|------|------|
| `POST` | `/api/backup/now` | 生成 ZIP，`Content-Disposition` 附件下载 |
| `POST` | `/api/backup/import` | `multipart file`；校验后覆盖恢复；成功 `Result.success()` |
| — | `/api/backup/list`、`/download/{id}` | **本版删除**（无历史） |

鉴权：需登录。导入前校验：ZIP、`manifest.schemaVersion==1`、`app==personal-hub`。

## 5. 导出 / 导入流程

### 5.1 导出

1. 按用户查询各表 → 写 `data/*.json`（保留原 id，含 `user_id` 原值仅作存档）
2. 收集路径 → 拷入 `files/`；缺失记 warning
3. 写 manifest → 打 ZIP → 响应流；删临时文件

### 5.2 导入（覆盖）

1. 解压到临时目录，读 manifest，校验
2. **DB 事务内**：按依赖倒序删除当前用户业务行（rel → 实体 → category/tag → layout）
3. 按依赖正序插入（显式主键 + `user_id=当前用户`）
4. 事务提交后：用临时 `files/` **替换**对应目录（先删该用户相关 `notes/{ids}`、`diaries/{ids}` 及旧 `file_resource` 路径，再拷入）；失败则记错误并尽量清理临时区（库已提交时需文档标明「文件失败可重导」——本版策略：**先落文件到 staging，事务成功后再 swap**；若 swap 失败返回 500 并提示重新导入）

推荐顺序：

- 删：`tag_rel` → `note_category_rel` → 各业务实体 → `category`/`tag` → `user_layout`
- 插：`category`/`tag` → 实体 → `note_category_rel`/`tag_rel` → `user_layout`
- 文件：staging → 替换

## 6. 前端

`DataManagement.vue`：

- 「立即备份」：请求 `blob` 下载（`triggerBlobDownload`），不再依赖 `downloadUrl` JSON
- 「导入恢复」：仅接受 `.zip`；成功后提示刷新/重新进入业务页
- 去掉对 list API 的任何依赖

## 7. 安全与限制

- 路径穿越：ZIP 内条目必须落在约定前缀下（`notes/`、`diaries/`、`uploads/`）
- 仅操作用户自己的数据
- 导入覆盖二次确认（已有 UI）
- 审计：可选写一条 `BACKUP`/`RESTORE`（本版建议写 `audit_log`，与「不备份审计」不冲突）

## 8. 非本版

自动备份、频率配置、备份列表、`.json.gz`、分模块导出、跨用户合并。

## 9. 测试要点

- 空用户备份可下载；导入到另一干净用户后数据可见
- 有笔记正文+配图：恢复后预览正常
- 含回收站笔记：恢复后仍在回收站
- 坏 ZIP / 错误 schema → 400，数据未清空
- 缺单个附件文件：仍能导出，manifest 有 warning
