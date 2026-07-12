# Personal Hub - 项目介绍

## 定位
个人知识管理系统，Spring Boot 3 + Vue 3 前后端分离。

## 设计理念
> **Notion 的简洁 + Linear 的精致 + Raycast 的现代感 + Apple 的克制**

不是企业后台，是长期陪伴的知识空间。简洁、专注、温暖、高级。详见 `STYLE_GUIDE.md`。

## 架构

```
Vue 3 + Vite + TS → Axios → RESTful → Spring Boot 3 → MyBatis-Plus → MySQL
```

## 目录

```
personal-hub
├── personal-hub-server/   # 后端（领域模块）
├── personal-hub-web/      # 前端（modules/）
├── sql/                   # init.sql
├── docs/                  # 项目文档
└── README.md
```

## 领域模块设计

```
后端 (ph-*)                       前端 (src/modules/)
──────────────────────────────────────────────────
ph-boot       # 启动入口
ph-common     # 公共能力            —
ph-system     # 用户/认证/通知/审计  system/
ph-knowledge  # 笔记/日记/学习/      knowledge/
              # 阅读/标签/分类       category/
ph-planning   # Todo/学习计划       planning/
ph-resource   # 收藏夹/文件         resource/
ph-dashboard  # 聚合查询/全局搜索   dashboard/
              —                     search/
```

### 划分原则
- **按领域聚合，不按表划分** — 避免数据库思维
- **跨模块引用** — 通过 Service 调用，不直接依赖 Entity
- **Dashboard 仅聚合** — 不保存业务数据

## 依赖关系

```
ph-system ─┬─ ph-knowledge
           ├─ ph-planning
           ├─ ph-resource
           └─ ph-dashboard（聚合查询）
```

## 项目原则

1. 可读性优先 — 代码是人读的
2. 统一命名/结构 — 降低心智负担
3. 按领域组织 — 前后端统一领域模型
4. 不重复代码 — 公共能力沉淀到 common
5. 不随意加依赖 — 评估体积和必要性
6. 前后端命名一致 — API/模块/模型对齐
7. 接口风格统一 — RESTful + Result/PageResult
8. 数据库设计一致 — 主键/时间/逻辑删除/索引
9. 纯函数优先 — 无副作用逻辑方便测试和复用
10. 改代码先改文档

---

# 开发路线图

## 已完成 ✅

| 阶段 | 内容 | 状态 |
|------|------|------|
| 一 | 用户认证、笔记、学习记录、待办、文件管理 | ✅ |
| 二 | 日记、收藏夹、学习计划、阅读记录 | ✅ |
| 三 | Dashboard、数据统计、全局搜索、统一标签 | ✅ |
| 四 | UI 优化、功能增强、通知系统 | ✅ |
| 五 | 工作台自定义布局 | ✅ |
| 六 | CRUD UI 优化（Ui 组件库 + Dialog/Drawer）+ 分类合并（category 表） | ✅ |
| 七 | 代码清理（死代码删除）+ 配置文件分层（dev/prod） | ✅ |
| 八 | 笔记回收站改造 + 统一审计日志 | ✅ |
| 九 | 预览页阅读体验优化 | ✅ |
| 十 | 笔记编辑器 UI 重构（沉浸式编辑/自动保存/预览/专注模式） | ✅ |

## 后续可扩展

| 功能 | 优先级 | 说明 |
|------|--------|------|
| AI 笔记总结/问答 | 低 | 接入大模型，编辑器内 AI 助手（总结/润色/翻译/问答） |
| OCR 图片识别 | 低 | 文字提取 |
| PDF 在线预览 | 中 | 浏览器端 |
| MD 导出 PDF/HTML | 中 | |
| 数据备份 | 中 | 定时备份 |
| WebDAV 同步 | 低 | 文件同步 |
| Docker 部署 | 中 | 容器化 |
| Mermaid/PlantUML 图表 | 低 | 编辑器内渲染 UML 和流程图 |
| 数学公式 KaTeX | 低 | 行内/块级公式渲染 |
| 双向链接（Backlink） | 低 | 笔记间引用关系 |
| 附件管理 | 低 | 笔记内附件上传与预览 |

---

# 开发历史

| 步骤 | 内容 | 阶段 |
|------|------|------|
| 1-4 | 后端/前端脚手架 + 数据库初始化 + Swagger | 一 |
| 5-6 | Markdown 笔记 + 学习记录 | 一 |
| 7 | Maven 多模块拆分 | 一 |
| 8 | 代码质量统一（PageParam + ResultCode + @Slf4j） | 一 |
| 9-10 | Todo 模块 + 文件管理 | 一 |
| 11-12 | 日记 + 收藏夹 | 二 |
| 13-14 | 学习计划 + 阅读记录 | 二 |
| 15 | 统一标签系统（ph-tag + tag/tag_rel 表） | 三 |
| 16-18 | Dashboard + 趋势图（ECharts）+ 全局搜索 | 三 |
| 19 | 通知系统（sys_notification） | 四 |
| 20-21 | UI 优化（设计令牌/侧边栏/共享组件/Command Palette）+ 功能增强 | 四 |
| 22 | 工作台自定义布局（user_layout + 数据驱动 + 设置页） | 五 |
| 23-24 | CRUD UI 优化（Ui 组件库 + Dialog/Drawer）+ 分类系统合并 | 六 |
| 25-27 | 代码清理 + 配置分层（dev/prod）+ Swagger 修复 | 七 |
| 28 | 笔记回收站改造 + 审计日志（deleted_at/audit_log/预览/回收站重构） | 八 |
| 29 | 预览页阅读体验重构（DocLayout + markdown-prose + 阅读设置 + 主题 + 目录导航） | 九 |
