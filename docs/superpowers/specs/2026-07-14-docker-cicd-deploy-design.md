# Personal Hub Docker + GitHub Actions 自托管部署设计

**日期：** 2026-07-14  
**环境：** Ubuntu 22.04.5 LTS 本机虚拟机（可出网、不可入站）  
**状态：** 已批准

## 目标

在单台内网 VM 上，用 Docker Compose 运行前后端与依赖，通过 GitHub Actions **Self-hosted Runner** 实现 push 后自动构建与发布。面向首次使用 Docker 的个人部署。

## 约束

| 项 | 结论 |
|----|------|
| 网络 | VM 可访问外网，外网无法主动连入 → 必须自托管 Runner |
| 栈 | JDK 21 · Spring Boot 3 · Vue 3 · MySQL 8 · Redis · Nginx |
| 前端 API | Axios `baseURL: '/api'` → Nginx 同域反代 |
| 文件存储 | 容器内 `/data/personal-hub/uploads`，宿主机 bind mount |
| 本期不做 | HTTPS / 域名、K8s、多机 |

## 架构

```
push → main / workflow_dispatch
        │
        ▼（Runner 主动拉取）
Ubuntu VM · Self-hosted Runner
        │
        ▼
Docker Compose
  nginx:80  → 静态资源 + /api → backend:8080
  backend   → mysql / redis
  mysql:8 · redis
  volumes: mysql_data · redis_data · /data/personal-hub/uploads
```

## 组件

| 服务 | 镜像 | 职责 |
|------|------|------|
| frontend | 自建（Node 22 构建 + nginx） | 托管 `dist`，反代 `/api` |
| backend | 自建（Maven 构建 + JRE 21） | Spring Boot，`prod` profile |
| mysql | `mysql:8` | 库 `personal_hub`，挂载 `sql/init.sql` 首次初始化 |
| redis | `redis:7`（兼容 5+ 协议） | 密码认证 |

## 仓库布局

```
deploy/
  docker-compose.yml
  .env.example
  nginx/default.conf
  backend/Dockerfile
  frontend/Dockerfile
.github/workflows/deploy.yml
docs/DEPLOYMENT.md
```

敏感配置：`/opt/personal-hub/.env`（**在 Runner workspace 之外**），避免 `actions/checkout` 清理导致丢失。

## CI/CD

1. `actions/checkout`
2. `docker compose --env-file /opt/personal-hub/.env -f deploy/docker-compose.yml up -d --build`
3. 健康检查：HTTP 访问首页与后端端口

触发：`push` 到 `main`；`workflow_dispatch` 手动。

## 配置约定

- Compose 通过环境变量覆盖数据源 / Redis / JWT（`application-prod.yml` 使用 `${VAR:default}`）
- MySQL 用户与 `application-prod` 对齐（默认用户名 `al_user`）
- 生产密码与 JWT 必须在 `.env` 中修改，不得提交仓库

## 运维

- 日志：`docker compose logs -f`
- 备份：MySQL volume + `/data/personal-hub/uploads`
- 回滚：检出旧 commit 后重新跑 workflow，或手动 `compose up` 旧代码树

## 验收

- [ ] `docker compose up` 后浏览器可打开站点并登录
- [ ] push 后 Runner 自动完成构建与容器更新
- [ ] 重启 VM 后 Compose 服务与 Runner 可恢复（systemd）
