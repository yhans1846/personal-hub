# 自动备份 + 历史列表 + 可回滚

> 日期：2026-07-20 · 已批准  
> 前置：`2026-07-19-data-backup-restore-design`（ZIP 格式与 import/export 语义不变）  
> 范围：服务端落盘、定时、历史 CRUD、从历史恢复。不含增量、选模块、云存储、邮件通知。

## 1. 目标

- 手动「立即备份」：生成与现网一致的 ZIP → **写入服务端历史** → **同时**浏览器下载。
- 按用户频率自动备份；历史列表可下载 / 恢复 / 删除；每用户最多保留 **7** 份成功备份。
- 从历史恢复前自动再打一份快照，降低误恢复风险。

成功标准：

- 关闭浏览器后，设置页仍能看到历史并下载/恢复。
- 定时任务按 `off|daily|weekly` 行为正确；超 7 份删最旧成功包。
- 恢复语义与现 `importZip` 一致；**不**删除 `user_backup` 行与 `backups/` 目录。

## 2. 数据模型

### 2.1 表 `user_backup`

| 列 | 类型 | 说明 |
|----|------|------|
| id | BIGINT PK AI | |
| user_id | BIGINT NOT NULL | 索引 |
| file_path | VARCHAR(512) NOT NULL | 相对 storage 根，如 `backups/{userId}/{id}.zip` |
| file_size | BIGINT NOT NULL | 字节 |
| trigger_type | VARCHAR(16) NOT NULL | `MANUAL` \| `AUTO` |
| status | VARCHAR(16) NOT NULL | `OK` \| `FAILED` |
| error_message | VARCHAR(500) NULL | 失败原因 |
| created_at | DATETIME | |

软删：本表 **物理删除**（删行即删文件）；不做 `is_deleted`。

同步：`sql/init.sql` + `docs/DATABASE.md`；提供可重复执行的 alter/补表脚本（若项目惯例有 migration 目录则一并）。

### 2.2 频率配置

`user_layout.layout_type = 'backup'`，`layout_json`：

```json
{ "frequency": "daily" }
```

取值：`off` | `daily` | `weekly`。缺省按 **daily**。  
不新增 `sys_user` 列。

### 2.3 磁盘

```
{storage.location}/
  backups/{userId}/{backupId}.zip
  notes/ ...（现有，打入 ZIP；ZIP 内不含 backups/）
```

`exportZip` **不得**把 `backups/` 打进包（避免递归膨胀）。`importZip` **不得**清空或覆盖 `backups/` 与 `user_backup`。

## 3. API

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | `/api/backup/now` | 生成 ZIP → 落盘 `MANUAL/OK` → 流式下载（兼容现客户端） |
| GET | `/api/backup/list` | 当前用户历史，按 `created_at` 降序；可含 FAILED |
| GET | `/api/backup/{id}/download` | 仅本人且 status=OK；流式 ZIP |
| POST | `/api/backup/{id}/restore` | 仅本人 OK：先 `createStoredBackup(MANUAL)` 快照，再 `importZip` 该文件 |
| DELETE | `/api/backup/{id}` | 删文件 + 行；仅本人 |
| GET | `/api/backup/settings` | `{ frequency }` |
| PUT | `/api/backup/settings` | body `{ frequency }` |

归属：一律校验 `user_id`；越权 → 404。  
现有 `POST /api/backup/import`（上传覆盖）保留不变。

## 4. 服务逻辑

### 4.1 `createStoredBackup(userId, triggerType)`

1. 调现有打包逻辑得到 `byte[]`（或先写临时文件再落盘，避免大包占双倍堆；优先临时文件）。
2. 插入 `user_backup`（可先插拿 id，或先写临时名再 update path）。
3. 写入 `backups/{userId}/{id}.zip`。
4. 更新 `file_size`、`status=OK`；失败则 `FAILED` + `error_message`，无有效文件则 `file_path` 可空串或占位。
5. `pruneBackups(userId)`：成功包（`status=OK`）按时间降序保留 **7**；多余删文件+行。`FAILED` 最多保留 **3** 条，其余删。

### 4.2 `POST /now`

`createStoredBackup(MANUAL)` 成功后，将同一 ZIP 作为响应体下载（文件名仍 `personal-hub-backup-yyyyMMdd-HHmmss.zip`）。失败不下载。

### 4.3 定时任务

- `@Scheduled`：每日 **02:00**（应用时区，与现通知任务一致用系统/容器时区）。
- 扫描有 `sys_user` 的用户（或仅有 layout/业务数据的用户）：读 backup frequency。
  - `off`：跳过  
  - `daily`：执行 AUTO  
  - `weekly`：仅当当天为周一执行  
- 单用户失败不影响其他；打日志。
- 跳过条件（可选防抖）：若该用户在过去 20 小时内已有 `AUTO/OK`，则跳过（避免重启重复打）。

### 4.4 从历史恢复

1. 校验记录 OK 且文件存在。  
2. `createStoredBackup(MANUAL)` 作为恢复前快照（失败则中止恢复并报错）。  
3. 读历史 ZIP bytes → `importZip`。  
4. 审计：`BACKUP` / `RESTORE`（可注明 fromBackupId）。

## 5. 前端

`DataManagement.vue`（设置 · 高级 · 缓存与数据）：

- 频率：`el-select` 关闭 / 每天 / 每周；变更即 `PUT settings`。
- 「立即备份」：保持；成功后刷新历史列表。
- 「导入恢复」：保持上传 ZIP。
- 历史表/列表：时间、来源(手动/自动)、大小、状态；操作：下载、恢复（二次确认全量覆盖）、删除（确认）。
- 空态：`EmptyState` 文案「暂无服务器备份」。

## 6. 文档

实现时更新：`DATABASE.md`、`API.md`、`PAGE_SPEC.md`、`PROJECT.md`（该项标 ✅）、`CHANGELOG.md`、`DEPLOYMENT.md`（若需挂载说明 `backups/` 已含在 storage 卷内则一句带过）。

## 7. 测试要点

- now：历史多一条 + 浏览器可下；磁盘有文件  
- list / download / delete 归属  
- restore：恢复前多一条 MANUAL；数据回到历史点  
- prune：第 8 次成功后仅剩 7  
- frequency off 不跑；weekly 非周一不跑  
- exportZip 内容不含 `backups/`
