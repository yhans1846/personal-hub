# Personal Hub 部署指南

> 职责：Ubuntu 自托管部署与运维。版本 → `TECH_STACK.md`；DDL → `sql/init.sql`。

面向 **Ubuntu 22.04** VM：Docker Compose（frontend / backend / MySQL / Redis），可选 GitHub Actions Self-hosted Runner。

约定（全文通用）：

```bash
# 仓库根目录 ~/personal-hub；密钥 /opt/personal-hub/.env
export COMPOSE="docker compose --env-file /opt/personal-hub/.env -f deploy/docker-compose.yml"
cd ~/personal-hub
```

| 路径 | 用途 |
|------|------|
| `~/personal-hub` | 代码（git） |
| `/opt/personal-hub/.env` | 密钥（勿提交 Git） |
| `/data/personal-hub/uploads` | 上传文件 |
| `/data/personal-hub/logs` | 应用日志 |

服务：`mysql` · `redis` · `backend` · `frontend`（Nginx，对外 `HTTP_PORT`）。

---

## 选哪条方案

| 场景 | 章节 |
|------|------|
| 看状态 / 启停 / 日志等常用命令 | **常用命令速查** |
| 全新机器第一次上线 | **方案一 · 首次部署** |
| 已上线，拉代码发新版 | **方案二 · 日常更新** |
| push `main` 自动发布 | **方案三 · CI/CD（可选）** |
| 连库、回滚、改密、清库 | **方案四 · 运维手册** |
| 装 Docker / 改 `.env` / 排障 | **附录** |

---

## 常用命令速查

以下均在仓库根目录执行，并先设好文首的 `$COMPOSE`。服务名：`mysql` · `redis` · `backend` · `frontend`。

### 看状态

```bash
$COMPOSE ps                    # 各服务状态（Up / healthy / Exit）
$COMPOSE ps -a                 # 含已退出容器
docker ps --filter name=personal-hub   # 本项目相关容器
```

健康检查（后端 8080 未映射到宿主机）：

```bash
$COMPOSE exec backend curl -sf http://127.0.0.1:8080/actuator/health
curl -fsS -o /dev/null "http://127.0.0.1:${HTTP_PORT:-80}/" && echo Frontend OK
```

### 启动

```bash
$COMPOSE up -d                 # 后台启动全部服务（不重建镜像）
$COMPOSE up -d --build         # 构建镜像后再启动（改代码 / Dockerfile 后用）
$COMPOSE start                 # 启动已创建但处于 stopped 的容器
$COMPOSE start backend         # 只启动某一个服务
```

### 停止

```bash
$COMPOSE stop                  # 停止全部（容器保留，数据卷保留）
$COMPOSE stop backend          # 只停后端
$COMPOSE down                  # 停止并删除容器与网络（**数据卷默认保留**）
$COMPOSE down -v               # 连同数据卷删除（会清空 MySQL/Redis，危险）
```

### 重启

```bash
$COMPOSE restart               # 重启全部
$COMPOSE restart backend       # 只重启后端（不重建镜像）
$COMPOSE up -d --build backend # 重建后端镜像并启动
```

### 日志

```bash
$COMPOSE logs -f               # 全部服务，跟随输出
$COMPOSE logs -f backend       # 只看后端
$COMPOSE logs --tail 100 backend
$COMPOSE logs --since 10m backend
```

应用落盘日志：`/data/personal-hub/logs/`。

### 进容器 / 资源

```bash
$COMPOSE exec backend sh       # 进后端容器（Alpine/JRE 可用 sh）
$COMPOSE exec mysql bash       # 进 MySQL 容器
$COMPOSE top                   # 各容器进程
$COMPOSE images                # 本 compose 用到的镜像
docker stats                   # CPU / 内存实时占用
```

### 其它常用

```bash
$COMPOSE pull                  # 拉取 compose 中的基础镜像（mysql/redis 等）
$COMPOSE config                # 检查合并后的 compose 配置是否合法
docker volume ls | grep personal-hub
docker system df               # 磁盘占用概览
# docker system prune          # 清悬空资源（慎用，勿加 -a/-v 除非明确要清）
```

**启停对照：** 临时维护用 `stop` → `start`；要下线编排用 `down`（库数据仍在卷里）；真正清库才 `down -v` 或手动 `volume rm`（见方案四）。

---

## 方案一 · 首次部署

按顺序执行，全部完成后浏览器可访问。

### 1. 确认机器

| 项 | 要求 |
|----|------|
| OS | Ubuntu 22.04 |
| 规格 | 2+ CPU · 4GB+ RAM · 40GB+ 磁盘 |
| 网络 | 能访问 Docker Hub、GitHub |
| 权限 | 当前用户有 `sudo` |
| 访问 | 内网 `http://<VM内网IP>/`（默认 80） |

宿主机**只需** Git + Docker + Compose；**不要**装 JDK / Maven / Node / 本机 MySQL / Redis。安装步骤见 [附录 A](#附录-a--安装-docker)。

### 2. 克隆与数据目录

```bash
cd ~
git clone <仓库SSH或HTTPS> personal-hub
cd ~/personal-hub
git checkout main

sudo mkdir -p /opt/personal-hub /data/personal-hub/uploads /data/personal-hub/logs
sudo chmod 775 /data/personal-hub/uploads /data/personal-hub/logs
```

### 3. 配置 `.env`

```bash
sudo cp ~/personal-hub/deploy/.env.example /opt/personal-hub/.env
sudo vim /opt/personal-hub/.env
```

**必须改掉**示例弱密钥（否则 prod 拒启）：

| 变量 | 说明                               |
|------|----------------------------------|
| `MYSQL_ROOT_PASSWORD` | Root的密码                          |
| `MYSQL_USER`  | mysql用户（**勿填 `root`**）           |
| `MYSQL_PASSWORD` | 上面用户的密码                          |
| `REDIS_PASSWORD` | Redis密码                          |
| `JWT_SECRET` | `可以命令生成 openssl rand -base64 48` |

**端口规则（易错）：**

| 变量 | 含义          | 取值 |
|------|-------------|------|
| `MYSQL_PORT` / `REDIS_PORT` | 容器内，给后端连不要改 | **必须** `3306` / `6379` |
| `MYSQL_PUBLISH` / `REDIS_PUBLISH` | 宿主机映射，给 IDE | 默认 `3306`/`6379`；冲突可改 `3307`/`6380` |
| `HTTP_PORT` | 网站          | 默认 `80` |

`MYSQL_HOST`/`REDIS_HOST` 固定为 `mysql`/`redis`。完整说明见 [附录 B](#附录-b--环境变量)。

国内构建：前端 npmmirror、后端阿里云 Maven，一般**不必**设 `BUILD_HTTP_PROXY`。`docker pull` 超时可给 Docker 守护进程配代理。

### 4. 首次启动

```bash
export COMPOSE="docker compose --env-file /opt/personal-hub/.env -f deploy/docker-compose.yml"
cd ~/personal-hub
$COMPOSE up -d --build
```

- 首次构建镜像约数分钟。
- `sql/init.sql` **仅在 MySQL 数据卷第一次创建时**执行。

### 5. 验收

```bash
$COMPOSE ps
$COMPOSE exec backend curl -sf http://127.0.0.1:8080/actuator/health
curl -fsS -o /dev/null "http://127.0.0.1:${HTTP_PORT:-80}/" && echo Frontend OK
$COMPOSE exec backend date   # 应为 CST / Asia/Shanghai
```

- 浏览器：`http://<VM内网IP>/`（改了 `HTTP_PORT` 则带端口）。
- 登录：`init.sql` 种子用户（默认 `admin`，首次登录后改密）。
- 抽查：登录、笔记、设置→高级→立即备份；后端日志无持续 Connection refused。

首次部署到此结束。需要自动发布再做方案三。

---

## 方案二 · 日常更新

已上线机器发新版本。按顺序：

### 1. 拉代码

```bash
cd ~/personal-hub
git pull origin main
```

### 2. （如有）执行库迁移

Compose **不会**自动跑 `sql/alter_*.sql`。有新 alter 时：

```bash
set -a && . /opt/personal-hub/.env && set +a
$COMPOSE exec -T mysql mysql -u"$MYSQL_USER" -p"$MYSQL_PASSWORD" "$MYSQL_DATABASE" < sql/alter_YYYY-MM-DD_….sql
```

无 alter 则跳过。`init.sql` **不会**在已有卷上再执行。

### 3. 重建并启动

```bash
export COMPOSE="docker compose --env-file /opt/personal-hub/.env -f deploy/docker-compose.yml"
$COMPOSE up -d --build
```

仅改后端时也可：`$COMPOSE up -d --build backend`。

### 4. 验收

```bash
$COMPOSE ps
$COMPOSE exec backend curl -sf http://127.0.0.1:8080/actuator/health
curl -fsS -o /dev/null "http://127.0.0.1:${HTTP_PORT:-80}/" && echo Frontend OK
$COMPOSE logs --tail 50 backend
```

出问题：见方案四「回滚」。

---

## 方案三 · CI/CD（可选）

仅在需要「push `main` 自动部署」时配置。**首次上线不依赖本方案**。

### 1. 注册 Runner

1. GitHub → Settings → Actions → Runners → New（Linux x64）
2. VM 安装到 `~/actions-runner`：`config.sh` → `sudo ./svc.sh install && start`
3. Runner 用户须在 `docker` 组；机器能出网取任务

### 2. 前置条件

- `/opt/personal-hub/.env` 已存在且密钥有效
- `/data/personal-hub/{uploads,logs}` 已创建
- 方案一已成功手动跑通过至少一次

### 3. 触发

- `push` 到 `main`，或 Actions 里 `workflow_dispatch`
- 流程：checkout → `compose up -d --build` → 轮询首页（见 `.github/workflows/deploy.yml`）

---

## 方案四 · 运维手册

以下命令均先设好 `$COMPOSE`（见文首）。

### 4.1 连接 MySQL / Redis

**A. 进容器（推荐，不依赖宿主机端口）**

```bash
set -a && . /opt/personal-hub/.env && set +a
$COMPOSE exec mysql mysql -u"$MYSQL_USER" -p"$MYSQL_PASSWORD" "$MYSQL_DATABASE"
$COMPOSE exec redis redis-cli -a "$REDIS_PASSWORD" ping
```

**B. 宿主机端口**（可选 `apt install mysql-client redis-tools`）

```bash
mysql -h 127.0.0.1 -P "${MYSQL_PUBLISH:-3306}" -u "$MYSQL_USER" -p"$MYSQL_PASSWORD" "$MYSQL_DATABASE"
redis-cli -h 127.0.0.1 -p "${REDIS_PUBLISH:-6379}" -a "$REDIS_PASSWORD" ping
```

IDE：Host=`127.0.0.1` 或 VM IP，Port=`MYSQL_PUBLISH` / `REDIS_PUBLISH`。

**C. Windows → VM（SSH 隧道）**

```bash
# PUBLISH 默认时：
ssh -L 3306:127.0.0.1:3306 -L 6379:127.0.0.1:6379 user@<VM>
# PUBLISH=3307/6380 时：
ssh -L 3307:127.0.0.1:3307 -L 6380:127.0.0.1:6380 user@<VM>
```

勿对公网裸奔数据库端口。

### 4.2 日志 / 重启

启停、状态、日志完整命令见文首 **常用命令速查**。运维常用：

| 操作 | 命令 |
|------|------|
| 状态 | `$COMPOSE ps` |
| 停全部 / 启全部 | `$COMPOSE stop` / `$COMPOSE start` |
| 下线编排（保留卷） | `$COMPOSE down` |
| 应用日志文件 | `/data/personal-hub/logs/` |
| 容器日志 | `$COMPOSE logs -f backend` |
| 软重启 | `$COMPOSE restart backend` |
| 重建镜像 | `$COMPOSE up -d --build backend` |

### 4.3 备份

| 方式 | 做法 |
|------|------|
| 应用内 | 设置 → 高级 → 数据管理 → 立即备份（按用户 ZIP） |
| 运维 | `mysqldump` + `tar` uploads（自行脚本） |

### 4.4 回滚

```bash
cd ~/personal-hub
git log --oneline -10
git checkout <旧commit>
$COMPOSE up -d --build
# 恢复跟踪 main：git checkout main && git pull
```

或 Actions 对旧成功 run 再跑一次（若仍指向当前 commit，需先回退代码）。

### 4.5 修改 `.env` / 密码

```bash
sudo vim /opt/personal-hub/.env
$COMPOSE up -d          # 多数变量重启即生效
```

**MySQL 密码仅在数据卷首次初始化时写入。** 改密后仍用旧密码 → 要么容器内 `ALTER USER`，要么删卷重建（见下，会丢库）。

### 4.6 清库重建（危险）

会删除全部业务数据，先备份：

```bash
$COMPOSE down
docker volume ls | grep mysql
docker volume rm personal-hub_mysql_data   # 名称以 ls 为准
$COMPOSE up -d --build                     # 重新执行 init.sql
```

### 4.7 时区

生产容器固定 `Asia/Shanghai`（TZ + JVM + Logback）。日志仍慢 8 小时：

```bash
git pull
$COMPOSE up -d --build backend
$COMPOSE exec backend date
```

---

## 附录 A · 安装 Docker

```bash
sudo apt update
sudo apt install -y git curl ca-certificates openssl

# （可选）卸旧包
sudo apt remove -y docker.io docker-compose docker-compose-v2 docker-doc podman-docker containerd runc 2>/dev/null || true

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
sudo apt install -y docker-ce docker-ce-cli containerd.io docker-buildx-plugin docker-compose-plugin
sudo systemctl enable --now docker
sudo usermod -aG docker "$USER"
```

**退出 SSH 再登录**（或 `newgrp docker`），再验证：

```bash
docker version && docker compose version
sudo docker run --rm hello-world
```

拉不动 `download.docker.com` / Hub 时自备镜像加速或代理。可选：`mysql-client` / `redis-tools` 仅 SSH 连库需要。

---

## 附录 B · 环境变量

模板：`deploy/.env.example` → `/opt/personal-hub/.env`。

| 变量 | 说明 |
|------|------|
| `MYSQL_*` | 见方案一 §3；`PORT`≠`PUBLISH` |
| `REDIS_*` | 同上 |
| `JWT_SECRET` / `JWT_EXPIRATION` | 生产强密钥 |
| `STORAGE_LOCATION` / `STORAGE_MAX_SIZE` | 容器内上传路径与上限 |
| `LOG_PATH` | 日志目录 |
| `HTTP_PORT` | 站点端口 |
| `BUILD_HTTP_PROXY` | 一般不需要；若用须 Allow LAN，勿填 `127.0.0.1` |

时区由 Compose / 镜像固定，无需在 `.env` 设 `TZ`。

---

## 附录 C · 排障

| 现象 | 处理 |
|------|------|
| docker permission denied | 用户进 `docker` 组并重新登录 |
| 缺 `.env` | 按方案一复制并填写 |
| 后端不健康 / 502 | `$COMPOSE logs backend`；等 mysql/redis healthy；查弱密钥 |
| DataSource/Redis Connection refused | `MYSQL_PORT=3306`、`REDIS_PORT=6379`（勿写成 PUBLISH） |
| `MYSQL_USER=root` 非法 | 改为普通用户；必要时删卷重建 |
| 改 `.env` 密码无效 | MySQL 密码随卷固化；`ALTER USER` 或删卷 |
| 80 占用 | `HTTP_PORT=8088` |
| 3306/6379 占用 | 改 `MYSQL_PUBLISH` / `REDIS_PUBLISH` |
| 日志慢 8 小时 | `--build backend`；`exec backend date` |
| 宿主机 curl :8080 失败 | 正常（未 publish）；用 `exec backend curl …/health` |
| Runner Offline | `svc.sh status`；出网；restart |
| 外网连不上库 | 防火墙 / SSH 隧道；勿公网裸奔 |

安全：勿提交 `.env`；生产强口令；内网 HTTP，公网需 HTTPS；Runner 勿对 fork PR 开放。
