# Personal Hub 优化路线图 — 设计规格

> 日期：2026-07-14  
> 状态：已确认（用户要求按序规划并执行）

## 目标

按 P0 → P1 → P2 顺序消除正确性/安全风险，再提升列表性能与运维能力，最后做包体与索引等增强。

## 范围

| 批次 | 内容 |
|------|------|
| P0 | 回收站清理、生产密钥、JWT query 收口 |
| P1 | N+1、excerpt、Redis 实装、登录限流、Actuator、关键测试 |
| P2 | 前端拆包、复合索引、迁移进部署、文档同步 |

## 设计决策

### P0-1 孤立资源清理

- **只删除**「目录存在但 DB 中无对应笔记行」的资源（`selectById == null`）。
- **不删除**回收站笔记（`is_deleted=1`）目录。
- 可选后续：按 `deleted_at` 超期自动永久删除（本轮不做，避免扩大 scope）。

### P0-2 生产密钥

- `prod` profile 启动时校验：`JWT_SECRET`、`MYSQL_PASSWORD`、`REDIS_PASSWORD` 必须非空，且不得为已知弱默认值。
- YAML 去掉弱默认值（使用 `${VAR}` 无默认，或 validator 拒绝默认）。

### P0-3 JWT query

- 保留 `?token=` **仅**用于 `/api/notes/{id}/**` 静态资源路径（img 场景）。
- 其它路径忽略 query token，必须走 `Authorization`。

### P1 列表与缓存

- 笔记列表/最近/回收站：批量分类 + `TagService.getTagsMap`。
- 标签 `listByUser`：一次 GROUP BY 统计 usage。
- 文件列表：分类 map 缓存到 convert 外层。
- 笔记 `excerpt`：DB 列，创建/更新时截断纯文本写入；列表填充 excerpt。
- Redis：配置 `RedisCacheManager`；`@Cacheable` 分类列表与用户标签列表；写操作 `@CacheEvict`；移除 `activateDefaultTyping`。

### P1 安全与运维

- 登录：基于 Redis 的计数限流（IP，如 10 次/分钟）。
- Actuator：`/actuator/health` 对 Docker 开放；Dockerfile HEALTHCHECK 改指向它。
- Nginx：`limit_req` 保护 `/api/auth/login`。

### P2

- Vite `manualChunks` + Stats/Editor 按需。
- `note_note(user_id, is_deleted, updated_at)` 复合索引 + migration。
- 部署文档说明升级时跑 migration；收紧 upload 权限说明。
- 更新 CHANGELOG / DEPLOYMENT / DATABASE。

## 非目标（本轮不做）

- Dashboard 跨模块 Mapper 重构
- 前端 any 全量清理、超大 Vue 拆分
- AI / PDF / 备份等产品功能
