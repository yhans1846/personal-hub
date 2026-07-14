# Docker + CI/CD 部署 实现计划

> **面向 AI 代理的工作者：** 必需子技能：使用 superpowers:subagent-driven-development（推荐）或 superpowers:executing-plans 逐任务实现此计划。步骤使用复选框（`- [ ]`）语法来跟踪进度。

**目标：** 落地 Docker Compose 部署骨架、GitHub Actions 自托管发布流水线，以及面向 Ubuntu 22.04 VM 的部署文档。

**架构：** 自托管 Runner 在 VM 上拉取任务；本机构建前后端镜像并用 Compose 更新；`.env` 放在 `/opt/personal-hub/.env`。

**技术栈：** Docker Compose · GitHub Actions · Nginx · MySQL 8 · Redis · Eclipse Temurin 21 · Node 22 / pnpm

---

### 任务 1：生产配置支持环境变量

**文件：**
- 修改：`personal-hub-server/ph-boot/src/main/resources/application-prod.yml`

- [x] **步骤 1：** `MYSQL_*` / `REDIS_*` / `JWT_SECRET` / 存储路径支持 `${}` 占位
- [x] **步骤 2：** 本地默认值保持可开发机直连 localhost

### 任务 2：deploy/ 骨架

**文件：**
- 创建：`deploy/docker-compose.yml`、`.env.example`、`nginx/default.conf`、`backend/Dockerfile`、`frontend/Dockerfile`、`.dockerignore`（根目录）

- [x] **步骤 1：** 编写 Compose 四服务与 volume
- [x] **步骤 2：** 多阶段 Dockerfile（后端 Maven、前端 pnpm）
- [x] **步骤 3：** Nginx SPA + `/api` 反代

### 任务 3：GitHub Actions

**文件：**
- 创建：`.github/workflows/deploy.yml`

- [x] **步骤 1：** `self-hosted` + `--env-file /opt/personal-hub/.env` 部署

### 任务 4：文档与索引

**文件：**
- 创建：`docs/DEPLOYMENT.md`
- 修改：`README.md`、`CLAUDE.md`、`.gitignore`、`docs/CHANGELOG.md`

- [x] **步骤 1：** 写完整部署文档（含 Docker 速览与 Runner 安装）
- [x] **步骤 2：** 同步文档索引与忽略 `.env`
