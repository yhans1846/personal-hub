# 自动备份 + 历史列表 实现计划

> **面向 AI 代理：** 用 executing-plans 执行。规格：`docs/superpowers/specs/2026-07-20-auto-backup-history-design.md`

**目标：** 服务端备份历史、定时、设置频率、从历史恢复。

**架构：** 扩展 `DataBackupService`：`createStoredBackup` 落盘 + `user_backup`；Controller 增 list/download/restore/delete/settings；`BackupScheduleJob` 02:00；前端 `DataManagement` 历史表。

---

### 任务 1：表 + Entity/Mapper
- [ ] `sql/init.sql` 加 `user_backup`；`sql/alter-20260720-user-backup.sql` 补表
- [ ] Entity/Mapper/VO/DTO 在 `ph-biz/.../backup/`

### 任务 2：服务
- [ ] `createStoredBackup` / prune / list / download / restore / delete / settings（layout_type=backup）
- [ ] `exportZip` 确保不打包 `backups/`
- [ ] `POST /now` 改走 createStoredBackup 后下载
- [ ] 单测：prune 保留逻辑

### 任务 3：定时 + Controller API
- [ ] `BackupScheduleJob`
- [ ] 扩展 `BackupController`

### 任务 4：前端 + 文档
- [ ] api + DataManagement UI
- [ ] DATABASE/API/PAGE_SPEC/PROJECT/CHANGELOG
- [ ] commit（不 push）
