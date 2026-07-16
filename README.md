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
├── knowledge/       知识模块       笔记 · 日记 · 学习 · 阅读 · 标签
├── planning/        规划模块       Todo · 学习计划
├── resource/        资源模块       收藏夹 · 文件管理
├── category/        分类管理       笔记 / 收藏夹 / 文件 统一分类
├── search/          全局搜索       跨 8 模块全文检索
├── stats/           数据统计       ECharts 可视化
└── system/          系统模块       登录 · 工作台设置
```

---

## 🚀 功能矩阵

| 模块 | 核心能力 |
|------|----------|
| **📝 笔记** | Markdown 写作 · 分类/标签 · 回收站/审计日志 · 导出 `.md` · 阅读主题/字号/行高/图片比例可调（系统设置持久化）· 目录大纲导航 · **沉浸式创作模式**（全屏/分栏预览/滚动同步/阅读模式/Focus Mode/底部状态栏/自动保存/状态持久化） |
| **📖 学习记录** | 主题/时长追踪 · 日/周/月统计 · 连续学习天数 · 关联学习计划 |
| **✅ 待办** | 优先级 · 截止日期 · 拖拽排序 · Tab 分组导航（今天/本周/以后/已完成/已超期）· 信息卡片布局 · 相对时间徽章 · 完成时间追踪 |
| **📅 日记** | 日历月视图 · 心情/天气/地点 · 配图上传 · Markdown 正文 |
| **🔖 收藏夹** | 网址收藏 · 分类/标签 · 域名提取 · 模糊搜索 |
| **📚 阅读记录** | 章节进度 · 1-5 星评分 · 阅读时长 · 笔记沉淀 |
| **🎯 学习计划** | 目标/进度百分比 · 开始/截止日期 · 超期标记 · 学习记录关联 |
| **📁 文件管理** | 上传/下载/预览 · MIME 类型识别 · 分类管理 · 多来源追踪 |
| **🏷 多态标签** | 全模块统一关联 · 颜色标记 · 使用频次统计 |
| **📊 Dashboard** | 7 项可配置统计卡片 · 面积/柱状/环图/横向柱状 · 时间范围 3/7/15/30/90 天 · 可拖拽排序显隐 |
| **🔍 全局搜索** | 跨 8 模块检索 · 关键词高亮 · 结果分组展示 |
| **🔔 通知中心** | 待办超期 · 计划截止 · 完成提醒 · Bell 红标下拉 |
| **👤 个人资料** | 头像上传 · 基本信息/联系方式/个人简介 · 4 层级联地区选择 · 左侧抽屉展示 |
| **🎨 主题系统** | 浅色/深色 · 9 种强调色（蓝/紫/粉/红/橙/绿/teal/天蓝/靛蓝）· CSS 变量驱动 · 跟随系统 |
| **⌨️ 效率工具** | Command Palette（Ctrl+K）· 响应式布局 · 骨架屏加载 |

---

## 🎯 设计系统亮点

- **回收站增强** — 删除时间/原因追踪 · 分类标签保留 · 审计日志可追溯
- **沉浸式阅读器** — 4 套阅读主题 · 字号/行高/宽度/图片比例可调 · 设置持久化到服务器 · 目录大纲可折叠拖拽 · 代码复制/图片放大/标题锚点
- **统一排版系统** — `.markdown-prose` 全局 CSS 类，笔记预览/帮助文档/README 复用同一套排版规范
- **统一 UI 组件库** — 8 个 Ui-* 基础组件 + DocLayout 文档布局封装，全局一致
- **CRUD 交互规范** — Dialog / Drawer 替代页面跳转，新增编辑共用组件
- **CSS 设计令牌** — 全站 `--accent` / `--bg-card` / `--text-primary` 驱动
- **空状态设计** — 9 套 SVG 插画 + 引导文案，拒绝"暂无数据"
- **侧边栏三段分组** — 工作区 / 管理 / 统计，数据驱动可自定义

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
| [`PROJECT.md`](docs/PROJECT.md) | 项目定位 · 架构设计 · 核心原则 |
| [`TECH_STACK.md`](docs/TECH_STACK.md) | 技术栈版本 · 环境配置 |
| [`DEPLOYMENT.md`](docs/DEPLOYMENT.md) | Ubuntu 部署 · Docker Compose · GitHub Actions |
| [`DATABASE.md`](docs/DATABASE.md) | 16 张表结构 · ER 图 · 索引策略 |
| [`API.md`](docs/API.md) | RESTful 接口规范 · 请求/响应示例 |
| [`STYLE_GUIDE.md`](docs/STYLE_GUIDE.md) | 编码规范 · UI 设计系统 · 共享组件 |
| [`LIST_PAGE_SPEC.md`](docs/LIST_PAGE_SPEC.md) | Product 列表页（Table/Card）开发规范 |
| [`PREVIEW_PAGE_SPEC.md`](docs/PREVIEW_PAGE_SPEC.md) | 预览页开发规范 · 排版系统 · 注意事项 |
| [`CHANGELOG.md`](docs/CHANGELOG.md) | 版本变更记录 |
| [`qa/CURSOR_BROWSER_TESTING.md`](docs/qa/CURSOR_BROWSER_TESTING.md) | Cursor 浏览器巡检方法（MCP + Playwright + 日志） |

---

<p align="center">
  <sub>Built with care. Not an enterprise dashboard — a personal space that grows with you.</sub>
</p>
