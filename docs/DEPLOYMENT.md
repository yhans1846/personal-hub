# Personal Hub 部署指南

面向 **Ubuntu 22.04** 本机虚拟机：Docker Compose 运行全栈，GitHub Actions **Self-hosted Runner** 自动发布。

适用网络：**虚拟机可访问外网，外网无法主动连入**（只出不进）。

---

## 1. 环境与前提

| 项 | 要求 |
|----|------|
| 系统 | Ubuntu 22.04 LTS（已在本机 VM 验证系列） |
| 资源建议 | 2+ CPU · 4 GB+ 内存 · 40 GB+ 磁盘 |
| 账号 | 有 `sudo` 的普通用户（如 `yhans`） |
| 代码 | GitHub 仓库，默认分支 `main` |
| 网络 | 能访问 Docker Hub / GitHub；**无需**公网入站 |

技术栈：JDK 21 · Spring Boot 3 · Vue 3 · MySQL 8 · Redis · Nginx（均在容器内）。

---

## 2. Docker 速览（首次使用）

| 概念 | 一句话 |
|------|--------|
| **镜像 (Image)** | 只读模板，类似「安装包」 |
| **容器 (Container)** | 镜像跑起来的进程实例 |
| **Compose** | 用一个 YAML 同时管多个容器 |
| **Volume / 挂载** | 把数据留在宿主机，容器删了数据还在 |

本项目四个服务：`frontend`（Nginx + 静态页）、`backend`（Spring Boot）、`mysql`、`redis`。

常用命令（在仓库根目录）：

```bash
# 查看状态
docker compose --env-file /opt/personal-hub/.env -f deploy/docker-compose.yml ps

# 看日志
docker compose --env-file /opt/personal-hub/.env -f deploy/docker-compose.yml logs -f backend

# 停止 / 启动
docker compose --env-file /opt/personal-hub/.env -f deploy/docker-compose.yml stop
docker compose --env-file /opt/personal-hub/.env -f deploy/docker-compose.yml start
```

下文用 `COMPOSE` 代指：

```bash
docker compose --env-file /opt/personal-hub/.env -f deploy/docker-compose.yml
```

---

## 3. 目录与组件

```
personal-hub/
├── deploy/
│   ├── docker-compose.yml    # 编排
│   ├── .env.example          # 环境变量模板
│   ├── nginx/default.conf    # 前端 Nginx：SPA + /api 反代
│   ├── backend/Dockerfile
│   └── frontend/Dockerfile
├── .github/workflows/deploy.yml
├── sql/init.sql              # MySQL 首次初始化
└── docs/DEPLOYMENT.md        # 本文档
```

**密钥文件位置（重要）：**

```
/opt/personal-hub/.env
```

放在 Runner 工作区之外，避免 `actions/checkout` 清理 workspace 时丢掉密码。

宿主机文件上传目录：

```
/data/personal-hub/uploads
```

---

## 4. 安装 Docker

```bash
sudo apt-get update
sudo apt-get install -y ca-certificates curl gnupg

sudo install -m 0755 -d /etc/apt/keyrings
curl -fsSL https://download.docker.com/linux/ubuntu/gpg \
  | sudo gpg --dearmor -o /etc/apt/keyrings/docker.gpg
sudo chmod a+r /etc/apt/keyrings/docker.gpg

echo "deb [arch=$(dpkg --print-architecture) signed-by=/etc/apt/keyrings/docker.gpg] \
https://download.docker.com/linux/ubuntu jammy stable" \
  | sudo tee /etc/apt/sources.list.d/docker.list > /dev/null

sudo apt-get update
sudo apt-get install -y docker-ce docker-ce-cli containerd.io docker-buildx-plugin docker-compose-plugin

# 当前用户免 sudo 使用 docker（重新登录后生效）
sudo usermod -aG docker "$USER"
```

验证（重新登录后）：

```bash
docker version
docker compose version
```

若虚拟机拉取镜像较慢，可自行配置 registry 镜像加速（按你的网络环境选择）。

---

## 5. 配置环境变量

```bash
sudo mkdir -p /opt/personal-hub /data/personal-hub/uploads
sudo chmod 777 /data/personal-hub/uploads

# 先把仓库克隆到任意目录，例如 ~/personal-hub
cd ~/personal-hub
sudo cp deploy/.env.example /opt/personal-hub/.env
sudo nano /opt/personal-hub/.env
```

**必须修改：**

- `MYSQL_ROOT_PASSWORD` / `MYSQL_PASSWORD`
- `REDIS_PASSWORD`
- `JWT_SECRET`（足够长的随机串）

可选：`HTTP_PORT`（默认 `80`）。

生成随机密钥示例：

```bash
openssl rand -base64 48
```

---

## 6. 首次手动部署

```bash
cd ~/personal-hub   # 仓库根目录

# 构建并启动（首次会较久：拉基础镜像 + Maven/pnpm 构建）
docker compose --env-file /opt/personal-hub/.env -f deploy/docker-compose.yml up -d --build
```

查看状态：

```bash
docker compose --env-file /opt/personal-hub/.env -f deploy/docker-compose.yml ps
docker compose --env-file /opt/personal-hub/.env -f deploy/docker-compose.yml logs -f backend
```

浏览器访问（同一内网机器或 VM 端口转发后）：

```
http://<虚拟机内网IP>/
```

`sql/init.sql` 仅在 **MySQL 数据卷第一次创建** 时执行。若要重新初始化库：

```bash
docker compose --env-file /opt/personal-hub/.env -f deploy/docker-compose.yml down
docker volume ls | grep mysql   # 确认卷名，一般为 personal-hub_mysql_data
docker volume rm personal-hub_mysql_data
# 再 up -d --build
```

> 清卷会删库，生产数据先备份。

### 已有库升级（migration）

Compose **不会**自动执行 `sql/migration*.sql`。升级已有数据卷时，按版本手工执行对应脚本，例如：

```bash
# 示例：笔记 excerpt + 复合索引
docker compose --env-file /opt/personal-hub/.env -f deploy/docker-compose.yml exec -T mysql \
  mysql -u"$MYSQL_USER" -p"$MYSQL_PASSWORD" "$MYSQL_DATABASE" < sql/migration-20260714-note-excerpt-index.sql
```

也可用宿主机客户端连接容器映射的 MySQL 端口执行。执行前请备份。

**生产密钥**：`JWT_SECRET`、`MYSQL_PASSWORD`、`REDIS_PASSWORD` 必须在 `/opt/personal-hub/.env` 中配置为强口令；缺失或使用示例弱值时，`ProdSecretsValidator` 会拒绝启动后端。

**健康检查**：后端 Docker HEALTHCHECK 探测 `http://127.0.0.1:8080/actuator/health`。

---

## 7. 安装 GitHub Self-hosted Runner

### 7.1 在 GitHub 获取注册令牌

仓库 → **Settings** → **Actions** → **Runners** → **New self-hosted runner** → 选 Linux x64，按页面复制命令。

### 7.2 在 VM 上安装

```bash
mkdir -p ~/actions-runner && cd ~/actions-runner
# 使用 GitHub 页面提供的 download / config 命令，例如：
# curl -o actions-runner-linux-x64-*.tar.gz -L https://github.com/actions/runner/releases/download/...
# tar xzf ./actions-runner-linux-x64-*.tar.gz
# ./config.sh --url https://github.com/<owner>/<repo> --token <TOKEN>
```

`config.sh` 提示：

- Runner group：默认即可  
- Runner name：如 `personal-hub-vm`  
- Labels：可留空（workflow 使用 `runs-on: self-hosted`）  
- Work folder：默认 `_work`

### 7.3 安装为系统服务（开机自启）

```bash
cd ~/actions-runner
sudo ./svc.sh install
sudo ./svc.sh start
sudo ./svc.sh status
```

Runner 用户需已在 `docker` 组内，且能执行 `sudo mkdir`（workflow 里会创建上传目录）。若不想给 sudo，可事先建好 `/data/personal-hub/uploads` 并去掉 workflow 中的 sudo 步骤。

在 GitHub Runners 页面应显示 **Idle（绿色）**。

### 7.4 为何不用托管 Runner？

托管 Runner 在公网，无法 SSH 进你的内网 VM。自托管 Runner 由 VM **主动连出** GitHub 拉任务，适合「只出不进」。

---

## 8. CI/CD 工作流

文件：`.github/workflows/deploy.yml`

| 触发 | 行为 |
|------|------|
| `push` 到 `main` | 自动构建镜像并 `compose up -d` |
| `workflow_dispatch` | 在 Actions 页手动运行 |

步骤摘要：

1. `checkout` 代码  
2. 确保 `/data/personal-hub/uploads` 存在  
3. `docker compose ... up -d --build`  
4. 轮询 `http://127.0.0.1:$HTTP_PORT/` 做健康检查  

**发布流程：**

```text
本机改代码 → git push origin main → VM 上 Runner 自动部署 → 浏览器刷新验证
```

首次请先完成本文档第 4～7 节，再 push，否则 Job 会因缺少 `/opt/personal-hub/.env` 失败。

---

## 9. 日常运维

### 日志

```bash
COMPOSE="docker compose --env-file /opt/personal-hub/.env -f deploy/docker-compose.yml"
$COMPOSE logs -f --tail=200
$COMPOSE logs -f backend
```

### 重启某一服务

```bash
$COMPOSE restart backend
```

### 仅重建后端

```bash
$COMPOSE up -d --build backend
```

### 备份

```bash
# 数据库
docker exec -i $(docker ps -qf name=mysql) \
  mysqldump -u root -p"$MYSQL_ROOT_PASSWORD" personal_hub > backup-$(date +%F).sql

# 上传文件
sudo tar -czf uploads-$(date +%F).tar.gz -C /data/personal-hub uploads
```

`MYSQL_ROOT_PASSWORD` 从 `/opt/personal-hub/.env` 读取。

### 回滚

```bash
cd ~/personal-hub
git fetch && git checkout <旧 commit 或 tag>
docker compose --env-file /opt/personal-hub/.env -f deploy/docker-compose.yml up -d --build
```

或在 GitHub 对旧 commit 使用 **Re-run jobs**（需该 commit 仍含 deploy 文件）。

---

## 10. 常见问题

| 现象 | 处理 |
|------|------|
| Runner Offline | `sudo ~/actions-runner/svc.sh status`；检查出网；必要时 `svc.sh stop && svc.sh start` |
| Job 报缺少 `.env` | 确认 `/opt/personal-hub/.env` 存在且可读 |
| `permission denied` 操作 docker | 用户加入 `docker` 组后重新登录 |
| 后端一直不健康 | `$COMPOSE logs backend`；常见：密码与 `.env` 不一致、MySQL 未就绪 |
| 改了 `.env` 不生效 | `$COMPOSE up -d` 重建/重建容器；MySQL 初始化密码只在首次建卷时生效 |
| 80 端口占用 | 改 `.env` 中 `HTTP_PORT=8088` 后重新 `up -d` |
| 页面开了但接口 502 | 等 backend 健康；检查 Nginx 反代与 `docker compose ps` |
| Maven / pnpm 构建失败 | 看 build 日志；确认 VM 能访问中央仓库与 npm registry |

---

## 11. 安全提示

1. **不要**把 `/opt/personal-hub/.env` 提交到 Git（已在 `.gitignore` 忽略 `.env`）。  
2. 生产务必更换 JWT 与数据库密码。  
3. 当前仅内网 HTTP；若以后要从公网访问，再单独加反向代理与 HTTPS。  
4. Self-hosted Runner 拥有本机权限，仅在可信仓库启用，勿对 fork PR 开启。

---

## 相关文档

- [TECH_STACK.md](./TECH_STACK.md) — 版本与环境  
- [PROJECT.md](./PROJECT.md) — 架构概览  
- 设计说明：[superpowers/specs/2026-07-14-docker-cicd-deploy-design.md](./superpowers/specs/2026-07-14-docker-cicd-deploy-design.md)
