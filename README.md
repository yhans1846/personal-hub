<p align="center">
  <h1 align="center">Personal Hub</h1>
  <p align="center">
    个人知识管理系统 · 简洁、专注、长期陪伴
  </p>
</p>

---

## ✨ 设计哲学

> **Notion 的简洁 × Linear 的精致 × Raycast 的现代感 × Apple 的克制**

不是企业后台，是属于自己的知识空间——安静、高效、审美在线。

---

## 🛠 技术架构

| 层级 | 技术选型 |
|------|----------|
| **后端** | Spring Boot 3.3 · Java 21 · MyBatis-Plus · MySQL 8 · Redis 5 |
| **前端** | Vue 3 · TypeScript · Vite · Element Plus · Pinia |
| **认证** | Spring Security + JWT（无状态会话） |
| **文档** | SpringDoc OpenAPI 2.6（Swagger） |
| **可视化** | ECharts（Dashboard 趋势分析） |
| **编辑器** | Vditor（Markdown 写作 / WYSIWYG） |

---

## 🧩 项目结构

### 后端 · 4 模块（包内按领域划分）

```
personal-hub-server/
├── ph-boot          启动入口
├── ph-common        公共能力 + 存储 SPI（Result / JWT / Security / StorageService）
├── ph-system        用户系统（登录 / 通知 / 布局 / 审计）
└── ph-biz           业务合集
                     · knowledge   笔记 / 日记 / 学习 / 阅读 / 标签 / 分类
                     · planning    Todo / 学习计划
                     · resource    收藏夹 / 文件 / 笔记附件
                     · dashboard   统计 / 搜索 / 趋势聚合
```

### 前端 · 领域模块（Composition API）

```
personal-hub-web/src/modules/
├── dashboard/       首页仪表盘     8 项统计卡片 + 趋势图
├── knowledge/       知识模块       笔记 · 日记 · 学习 · 阅读 · 标签 · 分类
├── planning/        规划模块       Todo · 学习计划
├── resource/        资源模块       收藏夹 · 文件管理
├── search/          全局搜索       跨 8 模块全文检索
├── stats/           数据统计       ECharts 可视化
└── system/          系统模块       登录 · 工作台设置
```

---

## 🚀 功能矩阵

| 模块 | 核心能力 |
|------|----------|
| **📝 笔记** | Markdown · Table/Card · 分类/标签 · 回收站 fill · 单条导出 · 沉浸编辑 / 预览阅读体验 |
| **📖 学习记录** | 时间线 · 主题/时长 · 关联计划 · fill |
| **✅ 待办** | 优先级 · 截止 · 拖拽 · **dueScope 服务端 Tab 分页** · ListToolbar · lucide Tab · 两列卡片 · fill |
| **📅 日记** | 月历 · 心情/天气配色 · 地点 · 配图 · Markdown · fill |
| **🔖 收藏夹** | 网址 · 分类/标签 · 首页展示 · fill 卡片网格 |
| **📚 阅读记录** | Product Table/Card · 进度/评分 · XLSX 导出 · fill + 矮屏 10/8/6 |
| **🎯 学习计划** | Product Table/Card · 进度/状态 · XLSX 导出 · fill + 矮屏 10/8/6 |
| **📁 文件管理** | 上传/下载 · MIME · 分类 · fill |
| **🏷 多态标签** | 全模块统一关联 · 颜色 · 频次 |
| **📊 Dashboard** | Bento 外锁内滚 · 可配置卡片 · 窄屏可滚 |
| **🔍 全局搜索** | 跨模块检索 · Ctrl+K 直达 |
| **🔔 通知中心** | 超期/截止提醒 · Bell |
| **👤 个人资料** | 头像 · 资料抽屉 |
| **🎨 主题系统** | 浅/深色 · 强调色 · 密度/圆角 · CSS Token |
| **⚙️ 系统设置** | 工作台标签云 · 外观 · 高级折叠 |
| **⌨️ 效率工具** | Command Palette · 骨架屏 · 一屏铺满列表规范 |

---

## 🎯 设计系统亮点

- **一屏铺满列表** — `useMainContentFill` + 矮屏 10/8/6 · Product Table/Card · 统一 ListToolbar
- **回收站增强** — 删除时间/原因追踪 · 分类标签保留 · 审计日志可追溯 · fill 布局
- **沉浸式阅读器** — 4 套阅读主题 · 字号/行高/宽度/图片比例可调 · 设置持久化到服务器 · 目录大纲可折叠拖拽 · 代码复制/图片放大/标题锚点
- **统一排版系统** — `.markdown-prose` 全局 CSS 类，笔记预览/帮助文档/README 复用同一套排版规范
- **统一 UI 组件库** — 8 个 Ui-* 基础组件 + DocLayout 文档布局封装，全局一致
- **CRUD 交互规范** — Dialog 替代页面跳转，新增编辑共用组件
- **CSS 设计令牌** — 全站 `--accent` / `--bg-card` / `--text-primary` / 密度 Token 驱动
- **空状态设计** — 9 套 SVG 插画 + 引导文案，拒绝"暂无数据"
- **侧边栏三段分组** — 工作区 / 管理 / 统计，数据驱动可自定义 · 设置页标签云

---

## 🚦 快速开始

### 环境要求

**JDK 21** · **Maven 3.8+** · **MySQL 8.0+** · **Redis 5+** · **Node.js 22** · **pnpm**

### 后端

```bash
cd personal-hub-server
mysql -u root -p < ../sql/init.sql     # 首次初始化
mvn spring-boot:run -pl ph-boot -am    # 启动 → :8080
```

### 前端

```bash
cd personal-hub-web
pnpm install
pnpm dev                               # 启动 → :3000
```

### 环境配置

| Profile | Swagger | SQL 日志 | 适用场景 |
|---------|---------|----------|----------|
| `dev`（默认） | ✅ 开启 | ✅ 打印 | 本地开发 |
| `prod` | ❌ 关闭 | ❌ 关闭 | 生产部署 |

```bash
# 切换生产环境
mvn spring-boot:run -pl ph-boot -am -Dspring-boot.run.profiles=prod
```

### 生产部署（Docker + CI/CD）

Ubuntu 内网虚拟机可用 Docker Compose 一键运行，并配合 GitHub Actions Self-hosted Runner 自动发布。详见 [`docs/DEPLOYMENT.md`](docs/DEPLOYMENT.md)。

---

## 📚 文档索引

| 文档 | 说明 |
|------|------|
| [`PROJECT.md`](docs/PROJECT.md) | 定位 · 模块架构 · 路线图 |
| [`TECH_STACK.md`](docs/TECH_STACK.md) | 版本与环境 |
| [`DEPLOYMENT.md`](docs/DEPLOYMENT.md) | 部署运维 |
| [`DATABASE.md`](docs/DATABASE.md) | 表结构（对齐 init.sql） |
| [`API.md`](docs/API.md) | HTTP 契约 |
| [`STYLE_GUIDE.md`](docs/STYLE_GUIDE.md) | 编码 · Token · 组件目录 |
| [`PAGE_SPEC.md`](docs/PAGE_SPEC.md) | 页面形态与交互 |
| [`CHANGELOG.md`](docs/CHANGELOG.md) | 按日变更 |
| [`qa/CURSOR_BROWSER_TESTING.md`](docs/qa/CURSOR_BROWSER_TESTING.md) | 浏览器巡检 |

---

<p align="center">
  <sub>Built with care. Not an enterprise dashboard — a personal space that grows with you.</sub>
</p>
