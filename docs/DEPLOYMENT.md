# Personal Hub 部署指南

面向 **Ubuntu 22.04** VM：Docker Compose + GitHub Actions **Self-hosted Runner**。VM 可出网，**无公网入站**。

---

## 1. 环境与前提

| 项 | 要求 |
|----|------|
| 系统 | Ubuntu 22.04 · 2+ CPU · 4GB+ RAM · 40GB+ 盘 |
| 账号 | sudo 用户 · 仓库默认分支 `main` |
| 网络 | Docker Hub / GitHub 可达 |

栈：JDK 21 · Spring Boot 3 · Vue 3 · MySQL 8 · Redis · Nginx。

---

## 2. Docker 速览（首次使用）

镜像→容器；Compose 编排；Volume 持久化。服务：`frontend`（Nginx+静态）· `backend` · `mysql` · `redis`。

```bash
COMPOSE="docker compose --env-file /opt/personal-hub/.env -f deploy/docker-compose.yml"
$COMPOSE ps | logs -f backend | stop | start
```

---

## 3. 目录与组件

```
deploy/  # docker-compose.yml · .env.example · nginx/ · Dockerfiles
.github/workflows/deploy.yml · sql/init.sql · docs/DEPLOYMENT.md
```

密钥：`/opt/personal-hub/.env`（勿放 Runner checkout 内）。上传：`/data/personal-hub/uploads`。

---

## 4. 安装 Docker

按 [Docker 官方 Ubuntu 文档](https://docs.docker.com/engine/install/ubuntu/) 安装 `docker-ce` + compose 插件；`usermod -aG docker $USER` 后重登。验证：`docker version` · `docker compose version`。

---

## 5. 配置环境变量

```bash
sudo mkdir -p /opt/personal-hub /data/personal-hub/uploads
sudo cp deploy/.env.example /opt/personal-hub/.env   # 再编辑
```

**必改：** `MYSQL_*_PASSWORD` · `REDIS_PASSWORD` · `JWT_SECRET`（`openssl rand -base64 48`）  
**可选：** `HTTP_PORT`（默认 80）

---

## 6. 首次手动部署

```bash
cd ~/personal-hub
$COMPOSE up -d --build    # 访问 http://<内网IP>/
```

`sql/init.sql` 仅 MySQL 卷**首次**执行。清库重装：`down` → `docker volume rm personal-hub_mysql_data`（先备份）→ 再 `up`。

### 已有库升级（migration）

Compose **不**自动跑 `sql/migration*.sql`：

```bash
$COMPOSE exec -T mysql mysql -u"$MYSQL_USER" -p"$MYSQL_PASSWORD" "$MYSQL_DATABASE" < sql/migration-….sql
```

弱密钥时 `ProdSecretsValidator` 拒绝启动。健康：`http://127.0.0.1:8080/actuator/health`。

---

## 7. 安装 GitHub Self-hosted Runner

### 7.1 在 GitHub 获取注册令牌
Settings → Actions → Runners → New self-hosted runner（Linux x64）。

### 7.2 在 VM 上安装
`~/actions-runner`：按页面 download → tar → `./config.sh`。

### 7.3 安装为系统服务（开机自启）
`sudo ./svc.sh install && start && status`。用户须在 `docker` 组。GitHub 显示 Idle。

### 7.4 为何不用托管 Runner？
托管在公网进不了内网 VM；自托管由 VM **主动出网**拉任务。

---

## 8. CI/CD 工作流

`.github/workflows/deploy.yml`：`push main` 或 `workflow_dispatch` → checkout → 确保 uploads → `compose up -d --build` → 轮询首页。

首次须完成 §4–7（缺 `.env` 会失败）。

---

## 9. 日常运维

### 日志
`/data/personal-hub/logs/personal-hub.log`（及按天 / error）；`LOG_PATH` 可覆盖。  
`$COMPOSE logs -f backend` · `sudo tail -f …/personal-hub.log`

### 重启 / 重建
`$COMPOSE restart backend` · `$COMPOSE up -d --build backend`

### 备份
`mysqldump` + `tar` uploads；密码取自 `.env`。

### 回滚
`git checkout <旧 commit>` → `$COMPOSE up -d --build`，或 Actions Re-run。

---

## 10. 常见问题

| 现象 | 处理 |
|------|------|
| Runner Offline | `svc.sh status`；出网；restart |
| 缺 .env | 确认 `/opt/personal-hub/.env` |
| docker denied | 加 docker 组后重登 |
| 后端不健康 | logs；密码/MySQL 未就绪 |
| 改 .env 无效 | `up -d`；MySQL 密码仅首次建卷生效 |
| 80 占用 | `HTTP_PORT=8088` |
| 502 | 等 backend；查反代 |
| 构建失败 | 中央仓库 / npm 可达性 |

---

## 11. 安全提示

1. 勿提交 `.env`  
2. 生产强口令 JWT/DB/Redis  
3. 内网 HTTP；公网需反代 HTTPS  
4. Runner 权限大，仅可信仓，勿对 fork PR 开放  

---

## 相关文档

- [TECH_STACK.md](./TECH_STACK.md) · [PROJECT.md](./PROJECT.md)
