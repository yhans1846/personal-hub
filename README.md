# Personal Hub

个人知识管理系统 — Spring Boot 3 + Vue 3 前后端分离。

**设计理念**：Notion 的简洁 + Linear 的精致 + Raycast 的现代感 + Apple 的克制。不是企业后台，是长期陪伴自己的知识空间。

## 技术栈

| 端 | 技术 |
|------|------|
| 后端 | Spring Boot 3.3.5 / Java 21 / MyBatis-Plus / MySQL 8 / Redis 5 |
| 前端 | Vue 3 / Vite / TypeScript / Element Plus / Pinia / ECharts |
| 认证 | JWT + Spring Security |
| 文档 | SpringDoc OpenAPI (Swagger) |

## 项目结构

```
personal-hub
├── personal-hub-server/  # Spring Boot 后端（6+1 领域模块）
├── personal-hub-web/     # Vue 3 前端
├── sql/                  # 数据库脚本
├── docs/                 # 项目文档
└── README.md
```

### 后端领域模块

```
personal-hub-server/
├── ph-boot          # 启动入口
├── ph-common        # 公共能力（JWT/异常/Result/配置）
├── ph-system        # 用户 / 认证(JWT) / 通知
├── ph-knowledge     # 笔记 / 日记 / 学习记录 / 阅读记录 / 标签
├── ph-planning      # Todo / 学习计划
├── ph-resource      # 收藏夹 / 文件管理
└── ph-dashboard     # Dashboard 聚合 + 全局搜索
```

### 前端领域模块

```
personal-hub-web/src/modules/
├── system/          # 登录
├── knowledge/       # note / diary / study / reading / tag
├── planning/        # todo / studyplan
├── resource/        # bookmark / file
├── dashboard/       # Dashboard 首页
└── search/          # 全局搜索
```

## 功能

| 模块 | 功能 |
|------|------|
| **用户认证** | JWT 登录/退出、个人信息、修改密码 |
| **笔记** | Markdown 编辑器（md-editor-v3）、分类/标签、搜索/收藏/回收站、导出 Markdown |
| **学习记录** | 时长/主题 CRUD、日周月统计、连续学习天数 |
| **待办** | 优先级/截止日期、拖拽排序（sortablejs）、已完成折叠 |
| **文件管理** | 上传/下载/预览、分类管理、搜索 |
| **日记** | Markdown 编辑、月视图、心情/天气/地点/配图 |
| **收藏夹** | 网址 CRUD、分类管理、搜索 |
| **学习计划** | 进度跟踪、剩余天数/超期标记、关联学习记录 |
| **阅读记录** | 章节进度、评分（1-5星）、阅读时长、笔记 |
| **统一标签系统** | 全模块多态关联、颜色标记、使用次数统计 |
| **Dashboard** | 8 项统计卡片、ECharts 趋势图（7/30/90 天） |
| **全局搜索** | 跨 8 模块统一搜索、关键词高亮、结果分组 |
| **通知系统** | 自动检测（待办超期/计划截止/完成）、定时任务、Bell 红标下拉面板 |
| **UI 优化** | 响应式布局、深色模式、5 种强调色切换、Command Palette（Ctrl+K）、侧边栏三段分组 |

## 快速启动

### 前置要求

- JDK 21 LTS、Maven 3.8+
- MySQL 8.0+、Redis 5.0+
- Node.js 22 LTS、pnpm

### 启动后端

```bash
cd personal-hub-server
# 初始化数据库（首次）
mysql -u root -p < ../sql/init.sql
# 启动
mvn spring-boot:run -pl ph-boot -am
```

### 启动前端

```bash
cd personal-hub-web
pnpm install
pnpm dev
```

### 访问

- 应用地址：`http://localhost:5173`
- API 地址：`http://localhost:8080`
- API 文档：`http://localhost:8080/swagger-ui.html`

## 文档

| 文档 | 内容 |
|------|------|
| [PROJECT.md](docs/PROJECT.md) | 项目定位、领域模块设计、架构原则 |
| [TECH_STACK.md](docs/TECH_STACK.md) | 技术栈版本与环境要求 |
| [DATABASE.md](docs/DATABASE.md) | 表结构、ER、索引 |
| [API.md](docs/API.md) | RESTful 接口规范 |
| [STYLE_GUIDE.md](docs/STYLE_GUIDE.md) | 编码规范、UI 设计系统、共享组件 |
| [ROADMAP.md](docs/ROADMAP.md) | 开发路线与后续规划 |
| [CHANGELOG.md](docs/CHANGELOG.md) | 版本变更记录 |
| [DEVELOPMENT_PLAN.md](docs/DEVELOPMENT_PLAN.md) | 分步开发计划 |
