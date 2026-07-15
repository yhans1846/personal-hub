# Personal Hub 优化路线图 实现计划

> **面向 AI 代理的工作者：** 必需子技能：使用 superpowers:subagent-driven-development（推荐）或 superpowers:executing-plans 逐任务实现此计划。步骤使用复选框（`- [ ]`）语法来跟踪进度。

**目标：** 按 P0→P1→P2 修复回收站/密钥/JWT，消除列表 N+1，补齐 excerpt/Redis/限流/健康检查与测试，再做包体与索引。

**架构：** 后端 Spring Boot 多模块；清理任务改「仅孤立目录」；prod 启动校验；JWT query 路径白名单；Service 层批量填充 + Cacheable；登录 Redis 限流 + Actuator。

**技术栈：** Spring Boot 3 / MyBatis-Plus / Redis / Vue 3 / Vite / MySQL 8

**规格：** `docs/superpowers/specs/2026-07-14-optimization-roadmap-design.md`

---

## 文件职责

| 文件 | 职责 |
|------|------|
| `NoteResourceCleanupTask.java` | 仅清理 DB 无行的孤立目录 |
| `ProdSecretsValidator.java`（新建） | prod 密钥失败即停 |
| `application-prod.yml` | 去掉弱默认密钥 |
| `JwtAuthenticationFilter.java` | query token 仅 notes 资源路径 |
| `NoteServiceImpl.java` | 批量填充分类/标签 + excerpt |
| `TagServiceImpl.java` / `TagRelMapper` | GROUP BY usage |
| `FileResourceServiceImpl.java` | 分类 map 外提 |
| `Note` / `NoteVO` / `sql/migration-*.sql` | excerpt 列 |
| `RedisConfig.java` + Category/Tag Service | 真实缓存 |
| `LoginRateLimitFilter.java`（新建） | 登录限流 |
| `ph-boot` Actuator + Dockerfile + nginx | 健康检查 / limit_req |
| 测试 + Vite + docs | 验证与文档 |

---

### 任务 1：P0 修复回收站资源清理

**文件：**
- 修改：`personal-hub-server/ph-knowledge/.../NoteResourceCleanupTask.java`

- [x] **步骤 1：** 将条件改为仅 `note == null` 时删除；更新类注释
- [x] **步骤 2：** 确认与 `permanentDelete`（删库+目录）分工正确

---

### 任务 2：P0 生产密钥校验

**文件：**
- 创建：`personal-hub-server/ph-boot/.../config/ProdSecretsValidator.java`
- 修改：`application-prod.yml`

- [x] **步骤 1：** YAML 中 `MYSQL_PASSWORD`/`REDIS_PASSWORD`/`JWT_SECRET` 去掉弱默认
- [x] **步骤 2：** `@Profile("prod")` 组件校验非空且不在拒绝列表

---

### 任务 3：P0 JWT query 白名单

**文件：**
- 修改：`JwtAuthenticationFilter.java`

- [x] **步骤 1：** query token 仅当 URI 匹配 `/api/notes/{id}/images|attachments/...` 时生效

---

### 任务 4：P1 消除 N+1（笔记/标签/文件）

**文件：**
- `NoteServiceImpl.java`、`TagServiceImpl.java`、`TagRelMapper.java`（及 XML 如需）、`FileResourceServiceImpl.java`

- [x] **步骤 1：** 笔记 list/recent/recycle 批量分类 + `getTagsMap`
- [x] **步骤 2：** `listByUser` 一次聚合 usageCount
- [x] **步骤 3：** 文件列表 category map 提到循环外

---

### 任务 5：P1 笔记 excerpt

**文件：**
- `sql/migration-20260714-note-excerpt.sql`、`init.sql`、`Note.java`、`NoteVO`、`NoteServiceImpl`、前端 List 如需要

- [x] **步骤 1：** 加列 `excerpt VARCHAR(500)`
- [x] **步骤 2：** create/update 时从 content 生成纯文本截断；列表填 excerpt

---

### 任务 6：P1 Redis 真实缓存

**文件：**
- `RedisConfig.java`、`CategoryServiceImpl`、`TagServiceImpl`

- [x] **步骤 1：** CacheManager + 收紧多态反序列化白名单
- [x] **步骤 2：** 分类/标签列表 Cacheable + 写时 Evict

---

### 任务 7：P1 登录限流 + Actuator 健康检查

**文件：**
- 新建限流 Filter、`SecurityConfig`、`ph-boot/pom.xml`、`application-prod.yml`、`deploy/backend/Dockerfile`、`deploy/nginx/default.conf`

- [x] **步骤 1：** Redis 计数限流 login
- [x] **步骤 2：** actuator health + Docker HEALTHCHECK
- [x] **步骤 3：** nginx `limit_req` 保护 login

---

### 任务 8：P1 关键测试

**文件：**
- `ph-boot` 或 `ph-knowledge` 测试：清理任务逻辑、JWT filter 白名单、ProdSecrets（如可单测）

- [x] **步骤 1：** 单元测试覆盖清理条件与 JWT query 路径规则

---

### 任务 9：P2 前端拆包 + 索引 + 迁移文档

**文件：**
- `vite.config.ts`、`sql/migration-*.sql`、`docs/DEPLOYMENT.md`、`DATABASE.md`、`CHANGELOG.md`

- [x] **步骤 1：** manualChunks
- [x] **步骤 2：** 复合索引 migration
- [x] **步骤 3：** 同步文档

---

## 验证命令

```bash
cd personal-hub-server && mvn -B -pl ph-boot -am test
cd personal-hub-web && pnpm build
```

## 提交策略

用户未明确要求提交前，不自动 `git commit`；全部完成后汇总变更并询问是否提交。
