# Personal Hub 部署指南

> 职责：Ubuntu 自托管部署与运维。版本 → `TECH_STACK.md`；初始化 DDL → `sql/init.sql`。

面向 **Ubuntu 22.04** VM：Docker Compose + GitHub Actions **Self-hosted Runner**（可出网、无公网入站）。

## 1. 前提

Ubuntu 22.04 · 2+ CPU · 4GB+ RAM · 40GB+ · sudo · 默认分支 `main` · Docker Hub/GitHub 可达。

## 2. Compose 速览

服务：`frontend`（Nginx+静态）· `backend` · `mysql` · `redis`。

```bash
COMPOSE="docker compose --env-file /opt/personal-hub/.env -f deploy/docker-compose.yml"
$COMPOSE ps | logs -f backend | stop | start
```

目录：`deploy/`（compose、`.env.example`、nginx、Dockerfile）· `.github/workflows/deploy.yml`  
密钥：`/opt/personal-hub/.env`（勿进 Runner checkout）· 上传：`/data/personal-hub/uploads`

## 3. 安装 Docker

按 [官方 Ubuntu 文档](https://docs.docker.com/engine/install/ubuntu/) 装 `docker-ce` + compose；用户进 `docker` 组后重登。

## 4. 环境变量

```bash
sudo mkdir -p /opt/personal-hub /data/personal-hub/uploads
sudo cp deploy/.env.example /opt/personal-hub/.env   # 再编辑
```

必改：`MYSQL_*_PASSWORD` · `REDIS_PASSWORD` · `JWT_SECRET`（`openssl rand -base64 48`）。可选 `HTTP_PORT`（默认 80）。

## 5. 首次部署

```bash
cd ~/personal-hub
$COMPOSE up -d --build    # http://<内网IP>/
```

`sql/init.sql` 仅 MySQL 卷**首次**执行。清库：`down` → `docker volume rm personal-hub_mysql_data`（先备份）→ 再 `up`。

迁移脚本 Compose **不**自动跑。已有库按日期执行 `sql/alter_*.sql`（例：日记定位、未用字段清理、`image_files` 改名）：

```bash
$COMPOSE exec -T mysql mysql -u"$MYSQL_USER" -p"$MYSQL_PASSWORD" "$MYSQL_DATABASE" < sql/alter_2026-07-18_….sql
```

弱密钥时 `ProdSecretsValidator` 拒启。健康：`http://127.0.0.1:8080/actuator/health`。

## 6. Self-hosted Runner

GitHub → Settings → Actions → Runners → New（Linux x64）。  
VM：`~/actions-runner` 按页面 download → `config.sh` → `sudo ./svc.sh install && start`。须在 `docker` 组。托管 Runner 进不了内网，故用自托管出网拉任务。

## 7. CI/CD

`push main` / `workflow_dispatch` → checkout → `compose up -d --build` → 轮询首页。须先完成 §3–6。

## 8. 运维

| 操作 | 命令/路径 |
|------|-----------|
| 日志 | `/data/personal-hub/logs/` · `$COMPOSE logs -f backend` |
| 重启 | `$COMPOSE restart backend` / `up -d --build backend` |
| 备份 | 运维：`mysqldump` + tar uploads；应用内：设置→高级→数据管理（`/api/backup`，按用户 ZIP） |
| 回滚 | `git checkout` 旧 commit 后 rebuild，或 Actions Re-run |

## 9. 排障 / 安全

| 现象 | 处理 |
|------|------|
| Runner Offline | `svc.sh status`；出网；restart |
| 缺 .env / docker denied | 路径与 docker 组 |
| 后端不健康 / 502 | logs；等 MySQL；反代 |
| 改 .env 无效 | `up -d`；MySQL 密码仅建卷时生效 |
| 80 占用 | `HTTP_PORT=8088` |

勿提交 `.env`；生产强口令；内网 HTTP，公网需 HTTPS；Runner 勿对 fork PR 开放。
