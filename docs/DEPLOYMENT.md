# Personal Hub 部署指南

> 职责：Ubuntu 自托管部署与运维。版本 → `TECH_STACK.md`；初始化 DDL → `sql/init.sql`。

面向 **Ubuntu 22.04** VM：Docker Compose + GitHub Actions **Self-hosted Runner**（可出网、无公网入站）。

---

## 0. 首次部署（完整流程）

按顺序做完即可上线。假设部署用户有 `sudo`，仓库克隆到 `~/personal-hub`。

### 0.1 机器与网络


| 项   | 要求                                      |
| --- | --------------------------------------- |
| OS  | Ubuntu 22.04                            |
| 规格  | 2+ CPU · 4GB+ RAM · 40GB+ 磁盘            |
| 网络  | 能访问 Docker Hub、GitHub（拉镜像 / Runner 取任务） |
| 访问  | 内网浏览器打开 `http://<VM内网IP>/`（默认 80）       |


### 0.2 宿主机要装的环境（必读）

走 **Docker Compose 部署** 时，Linux **只需**装下面几样；**不要**在宿主机装 JDK / Maven / Node / MySQL / Redis（它们都在容器里）。


| 软件                                      | 是否必须 | 用途                      |
| --------------------------------------- | ---- | ----------------------- |
| **Git**                                 | 必须   | `clone` / `pull` 代码     |
| **Docker Engine（docker-ce）**            | 必须   | 跑容器                     |
| **Docker Compose 插件**（`docker compose`） | 必须   | 编排前后端 + MySQL + Redis   |
| **curl**                                | 建议   | 健康检查、排障                 |
| **openssl**                             | 建议   | 生成 `JWT_SECRET`（一般系统自带） |
| **GitHub Actions Runner**               | 可选   | 仅在要做「push 自动部署」时装（§0.7） |
| **mysql-client / redis-tools**          | 可选   | 仅当要在 SSH 里用命令行连库时       |


**宿主机不必装：** JDK 21 · Maven · Node.js · pnpm · 本机 MySQL · 本机 Redis。

#### 0.2.1 基础工具

```bash
sudo apt update
sudo apt install -y git curl ca-certificates openssl
```

#### 0.2.2 安装 Docker Engine + Compose（Ubuntu 官方源）

按 [Docker 官方 Ubuntu 安装文档](https://docs.docker.com/engine/install/ubuntu/) 整理，可直接复制执行：

```bash
# （可选）卸掉冲突的旧包
sudo apt remove -y docker.io docker-compose docker-compose-v2 docker-doc podman-docker containerd runc 2>/dev/null || true

# 添加 Docker 官方 GPG 与 apt 源
sudo apt update
sudo apt install -y ca-certificates curl
sudo install -m 0755 -d /etc/apt/keyrings
sudo curl -fsSL https://download.docker.com/linux/ubuntu/gpg -o /etc/apt/keyrings/docker.asc
sudo chmod a+r /etc/apt/keyrings/docker.asc

sudo tee /etc/apt/sources.list.d/docker.sources <<EOF
Types: deb
URIs: https://download.docker.com/linux/ubuntu
Suites: $(. /etc/os-release && echo "${UBUNTU_CODENAME:-$VERSION_CODENAME}")
Components: stable
Architectures: $(dpkg --print-architecture)
Signed-By: /etc/apt/keyrings/docker.asc
EOF

sudo apt update

# 安装引擎 + Compose 插件
sudo apt install -y docker-ce docker-ce-cli containerd.io docker-buildx-plugin docker-compose-plugin

# 开机自启（一般已启用）
sudo systemctl enable --now docker

# 当前用户免 sudo 跑 docker（装完必须重新登录才生效）
sudo usermod -aG docker "$USER"
```

然后 **退出 SSH 再登录**（或执行 `newgrp docker`），再验证：

```bash
git --version
docker version
docker compose version
sudo docker run --rm hello-world
```

看到 `Hello from Docker!` 即安装成功。  
若仍提示 `permission denied`，说明还没重新登录，或用户未进 `docker` 组：`groups` 应包含 `docker`。

> 国内网络若拉不动 `download.docker.com` / Docker Hub，需自备镜像加速或代理后再装。

### 0.3 克隆仓库与数据目录

```bash
cd ~
git clone <你的仓库SSH或HTTPS> personal-hub
cd ~/personal-hub
git checkout main

sudo mkdir -p /opt/personal-hub /data/personal-hub/uploads /data/personal-hub/logs
sudo chmod 775 /data/personal-hub/uploads /data/personal-hub/logs
```

### 0.4 配置密钥（必做）

```bash
sudo cp ~/personal-hub/deploy/.env.example /opt/personal-hub/.env
# 用任意编辑器改密钥，例如：
sudo vim /opt/personal-hub/.env
# 或：sudo vi /opt/personal-hub/.env
# Windows 也可用 WinSCP / VS Code Remote 打开该文件编辑
```

**必须改掉示例值**（弱密钥时 `ProdSecretsValidator` 会拒启后端）：


| 变量                    | 说明                           |
| --------------------- | ---------------------------- |
| `MYSQL_ROOT_PASSWORD` | MySQL root                   |
| `MYSQL_PASSWORD`      | 应用库用户密码（与 `MYSQL_USER` 对应）   |
| `REDIS_PASSWORD`      | Redis                        |
| `JWT_SECRET`          | 建议：`openssl rand -base64 48` |


可选：`HTTP_PORT`（默认 `80`；冲突可改 `8088`）· `MYSQL_PUBLISH`（默认 `3306`）· `REDIS_PUBLISH`（默认 `6379`）。  
`.env` 只放 `/opt/personal-hub/.env`，**勿提交 Git**。

### 0.4.1 本机 / IDE 直连 MySQL、Redis

Compose 已映射宿主机端口（`.env` 中 `MYSQL_PUBLISH` / `REDIS_PUBLISH`）。改端口后需 `$COMPOSE up -d`。


| 服务    | 连接地址                          | 账号                                                          |
| ----- | ----------------------------- | ----------------------------------------------------------- |
| MySQL | `主机:MYSQL_PUBLISH`（默认 `3306`） | 库 `personal_hub`；用户 `MYSQL_USER` / `MYSQL_PASSWORD`（或 root） |
| Redis | `主机:REDIS_PUBLISH`（默认 `6379`） | 需密码 `REDIS_PASSWORD`                                        |


**Docker 跑在本机时：**

```bash
mysql -h 127.0.0.1 -P 3306 -u al_user -p personal_hub
redis-cli -h 127.0.0.1 -p 6379 -a "$REDIS_PASSWORD" ping
```

IDE（DataGrip / Cursor MySQL MCP 等）：Host=`127.0.0.1`，Port=`3306` / `6379`，密码与 `.env` 一致。

**Docker 在远程 VM、你在 Windows 上连：**

- Host 填 VM **内网 IP**，端口同上；确保防火墙放行 `3306`/`6379`（仅内网）。
- 更安全：不开放端口，用 SSH 隧道：
  ```bash
  ssh -L 3306:127.0.0.1:3306 -L 6379:127.0.0.1:6379 user@<VM>
  ```
  然后本机仍连 `127.0.0.1:3306` / `6379`。
- 仅允许本机访问时，把 compose 端口改成 `127.0.0.1:3306:3306` / `127.0.0.1:6379:6379`（改后需重建对应服务）。

注意：容器内应用仍用 `MYSQL_HOST=mysql`、`REDIS_HOST=redis`，**不要**改成 `127.0.0.1`。

### 0.4.2 构建时网络（国内）

前端用 **npmmirror**、后端 Maven 用 **阿里云**，构建期**不走** Clash，避免容器访问 `host.docker.internal:7897` 被拒绝。

若 `.env` 里已有 `BUILD_HTTP_PROXY`，可删掉或注释掉（当前前端/后端 Dockerfile 会清空代理环境变量）。

`docker pull` 基础镜像若仍超时，继续用守护进程代理 `127.0.0.1:7897`（只影响拉镜像，不影响 Dockerfile 内 npm/maven）。

### 0.5 首次启动

在仓库根目录：

```bash
export COMPOSE="docker compose --env-file /opt/personal-hub/.env -f deploy/docker-compose.yml"
cd ~/personal-hub
$COMPOSE up -d --build
```

首次会构建前后端镜像，并拉起 `mysql` / `redis` / `backend` / `frontend`。  
`sql/init.sql` 仅在 **MySQL 数据卷第一次创建** 时执行（含默认用户等种子数据）。

等待就绪（约 1–3 分钟）：

```bash
$COMPOSE ps
curl -fsS http://127.0.0.1:8080/actuator/health   # 后端（容器网络内由 compose 暴露给健康检查）
curl -fsS -o /dev/null "http://127.0.0.1:${HTTP_PORT:-80}/" && echo Frontend OK
```

浏览器访问：`http://<VM内网IP>/`（若改了 `HTTP_PORT` 则带端口）。

登录使用 `init.sql` 中的初始账号（默认用户名 `admin`；密码以你库内种子 / 本地约定为准，首次登录后建议立即修改）。

### 0.6 验收清单

- 首页可打开，登录 + 书架验证码通过
- 笔记创建/预览正常（上传目录可写）
- 设置 → 高级 → 立即备份可下载 ZIP
- `$COMPOSE logs -f backend` 无持续 ERROR

### 0.7（可选）接入 CI/CD

仅在需要「推送 `main` 自动发布」时配置：

1. GitHub → Settings → Actions → Runners → New（Linux x64）
2. VM 上安装到 `~/actions-runner`：`config.sh` → `sudo ./svc.sh install && start`
3. Runner 用户须在 `docker` 组；机器须能出网拉 Action 任务
4. 之后 `push main` 或 `workflow_dispatch` 会执行 `.github/workflows/deploy.yml`（要求 `/opt/personal-hub/.env` 已存在）

**首次部署不依赖 Runner**：第 0.5 节手动 `up -d --build` 即可。

---

## 1. 前提（摘要）

Ubuntu 22.04 · 2+ CPU · 4GB+ RAM · 40GB+ · sudo · 默认分支 `main` · Docker Hub/GitHub 可达。  
宿主机软件清单见 **§0.2**（必装 Git + Docker + Compose）。

## 2. Compose 速览

服务：`frontend`（Nginx+静态）· `backend` · `mysql` · `redis`。

```bash
COMPOSE="docker compose --env-file /opt/personal-hub/.env -f deploy/docker-compose.yml"
$COMPOSE ps | logs -f backend | stop | start
```

目录：`deploy/`（compose、`.env.example`、nginx、Dockerfile）· `.github/workflows/deploy.yml`  
密钥：`/opt/personal-hub/.env`（勿进 Runner checkout）· 上传：`/data/personal-hub/uploads` · 日志：`/data/personal-hub/logs`

## 3. 宿主机环境

见 **§0.2**（Git · Docker · Compose；可选 curl / Runner / 客户端工具）。

## 4. 环境变量

见 §0.4。`deploy/.env.example` → `/opt/personal-hub/.env`。

## 5. 日常发布 / 重建

```bash
cd ~/personal-hub
git pull
$COMPOSE up -d --build
```

`sql/init.sql` **不会**在已有 MySQL 卷上再次执行。清库（会丢数据，先备份）：

```bash
$COMPOSE down
docker volume rm personal-hub_mysql_data   # 卷名以 docker volume ls 为准
$COMPOSE up -d --build
```

迁移脚本 Compose **不**自动跑。已有库按日期执行 `sql/alter_*.sql`：

```bash
set -a && . /opt/personal-hub/.env && set +a
$COMPOSE exec -T mysql mysql -u"$MYSQL_USER" -p"$MYSQL_PASSWORD" "$MYSQL_DATABASE" < sql/alter_2026-07-18_….sql
```

## 6. Self-hosted Runner

见 §0.7。

## 7. CI/CD

`push main` / `workflow_dispatch` → checkout → `compose up -d --build` → 轮询首页。须先完成 Docker、`.env`、数据目录（及可选 Runner）。

## 8. 运维


| 操作  | 命令/路径                                                              |
| --- | ------------------------------------------------------------------ |
| 日志  | `/data/personal-hub/logs/` · `$COMPOSE logs -f backend`            |
| 重启  | `$COMPOSE restart backend` / `up -d --build backend`               |
| 备份  | 运维：`mysqldump` + tar uploads；应用内：设置→高级→数据管理（`/api/backup`，按用户 ZIP） |
| 回滚  | `git checkout` 旧 commit 后 rebuild，或 Actions Re-run                 |


## 9. 排障 / 安全


| 现象                     | 处理                                                   |
| ---------------------- | ---------------------------------------------------- |
| Runner Offline         | `svc.sh status`；出网；restart                           |
| 缺 .env / docker denied | 路径与 docker 组                                         |
| 后端不健康 / 502            | logs；等 MySQL；反代；检查 JWT/密码是否仍为 change-me              |
| 改 .env 无效              | `up -d`；**MySQL 密码仅建卷时生效**                           |
| 80 占用                  | `HTTP_PORT=8088`                                     |
| 3306/6379 占用           | `.env` 改 `MYSQL_PUBLISH` / `REDIS_PUBLISH` 后 `up -d` |
| 外网连不上库                 | 查防火墙；或只用 SSH 隧道；勿对公网裸奔 MySQL/Redis                   |


勿提交 `.env`；生产强口令；内网 HTTP，公网需 HTTPS；Runner 勿对 fork PR 开放；数据库端口优先内网或 SSH 隧道。