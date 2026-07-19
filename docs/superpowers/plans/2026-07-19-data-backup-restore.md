# 数据备份与恢复 实现计划

> **面向 AI 代理的工作者：** 必需子技能：使用 superpowers:subagent-driven-development（推荐）或 superpowers:executing-plans 逐任务实现此计划。步骤使用复选框（`- [ ]`）语法来跟踪进度。

**目标：** 实现当前用户手动 ZIP 全量备份与覆盖恢复（库表 + 文件），替换占位 `BackupController`。

**架构：** ph-biz 内 `DataBackupService` 编排导出/导入；ZIP = manifest + data/*.json + files/**；导入先 staging 文件再事务写库后 swap；不落备份历史表。

**技术栈：** Spring Boot 3 · MyBatis-Plus · Jackson · `java.util.zip` · Vue3 设置页 `DataManagement.vue`

**规格：** `docs/superpowers/specs/2026-07-19-data-backup-restore-design.md`

---

## 文件结构

| 路径 | 职责 |
|------|------|
| `ph-biz/.../backup/DataBackupService.java` | 接口：`exportZip(userId)` / `importZip(userId, bytes)` |
| `ph-biz/.../backup/DataBackupServiceImpl.java` | 编排 |
| `ph-biz/.../backup/BackupManifest.java` | manifest DTO |
| `ph-biz/.../backup/BackupZipSupport.java` | ZIP 读写、路径安全校验 |
| `ph-biz/.../backup/BackupController.java` | `POST /now` 流下载、`POST /import` |
| 删除 `ph-system/.../BackupController.java` | 去掉占位 |
| `personal-hub-web/.../system/api.ts` | blob 备份 + import |
| `DataManagement.vue` | 接线下载与 `.zip` |
| `docs/API.md` / `CHANGELOG.md` / `PAGE_SPEC.md` | 文档同步 |

---

### 任务 1：Manifest + ZIP 工具 + 路径安全

**文件：**
- 创建：`personal-hub-server/ph-biz/src/main/java/com/personalhub/backup/BackupManifest.java`
- 创建：`personal-hub-server/ph-biz/src/main/java/com/personalhub/backup/BackupZipSupport.java`
- 测试：`personal-hub-server/ph-biz/src/test/java/com/personalhub/backup/BackupZipSupportTest.java`

- [ ] **步骤 1：编写路径校验失败测试**

```java
@Test
void rejectPathTraversal() {
    assertThrows(BusinessException.class,
        () -> BackupZipSupport.assertSafeEntryName("../etc/passwd"));
}
```

- [ ] **步骤 2：实现 `assertSafeEntryName`：仅允许 `manifest.json`、`data/`、`files/notes|diaries|uploads/` 前缀，禁止 `..`**
- [ ] **步骤 3：测试通过后 commit**

```bash
git add personal-hub-server/ph-biz/src/main/java/com/personalhub/backup \
  personal-hub-server/ph-biz/src/test/java/com/personalhub/backup
git commit -m "feat(backup): ZIP 条目路径安全校验"
```

---

### 任务 2：导出 ZIP（库 + 文件）

**文件：**
- 创建：`DataBackupService.java` / `DataBackupServiceImpl.java`
- 注入各 Mapper + `StorageService` + `ObjectMapper`

- [ ] **步骤 1：按用户查出规格所列实体，序列化为 `data/*.json`**
- [ ] **步骤 2：收集 `notes/{id}`、`diaries/{id}`、`file_resource.path`，存在则写入 `files/`，缺失记 `warnings`**
- [ ] **步骤 3：写 `manifest.json`（schemaVersion=1），返回 `byte[]`；超可配置上限抛业务异常**
- [ ] **步骤 4：单元/集成测试：空用户导出 ZIP 含 manifest**
- [ ] **步骤 5：Commit** `feat(backup): 导出用户数据 ZIP`

---

### 任务 3：导入覆盖

**文件：** 同 `DataBackupServiceImpl`

- [ ] **步骤 1：解压到临时目录，校验 manifest**
- [ ] **步骤 2：文件先落到 staging；校验 ZIP 内路径**
- [ ] **步骤 3：`@Transactional` 内按规格顺序删当前用户业务行，再按原 id 插入且 `userId=当前用户`**
- [ ] **步骤 4：事务后 swap 文件目录；写审计 `BACKUP`/`RESTORE`（可选但推荐）**
- [ ] **步骤 5：坏 manifest 测试应失败且不删数据（可用 @SpringBootTest 或纯校验单测）**
- [ ] **步骤 6：Commit** `feat(backup): 导入 ZIP 全量覆盖恢复`

---

### 任务 4：Controller 迁移 + 前端

**文件：**
- 创建 ph-biz `BackupController`；删除 ph-system 占位
- 修改 `system/api.ts`、`DataManagement.vue`

- [ ] **步骤 1：`POST /api/backup/now` → `ResponseEntity<byte[]>` 附件下载（对齐笔记 export）**
- [ ] **步骤 2：`POST /api/backup/import` multipart；去掉 list/download**
- [ ] **步骤 3：前端 `backupNow` 用 `responseType: 'blob'` + `triggerBlobDownload`；accept 仅 `.zip`**
- [ ] **步骤 4：Commit** `feat(backup): 接线设置页备份/恢复`

---

### 任务 5：文档

- [ ] 更新 `docs/API.md`、`CHANGELOG.md`；必要时 `PAGE_SPEC` / `PROJECT` 备份条目
- [ ] Commit** `docs: 同步备份/恢复 API 与 changelog`

---

## 执行交接

规格：`docs/superpowers/specs/2026-07-19-data-backup-restore-design.md`  
计划：本文件。

用户已指示开始实现 → 本会话采用 **内联执行**。
