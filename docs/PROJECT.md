# Personal Hub - 项目介绍

## 项目定位

面向个人使用的学习与知识管理系统。前后端分离，围绕个人学习场景设计。

## 设计理念

> **Notion 的简洁 + Linear 的精致 + Raycast 的现代感 + Apple 的克制设计**

Personal Hub **不是企业后台**，而是一个长期陪伴自己的个人知识管理系统。

- 简洁、专注、温暖、高级 — 不追求炫酷动画和复杂视觉
- 长时间使用不疲劳 — 大量留白、舒适阅读体验、极低学习成本
- 不像企业 ERP、OA 系统或传统 Admin Template

详细 UI 规范见 `STYLE_GUIDE.md`。

## 开发目标

构建个人知识管理平台，练习 Spring Boot 3 + Vue 3 前后端分离开发，项目长期维护，功能围绕个人场景。

## 系统架构

```
Vue 3 + Vite + TypeScript → Axios → RESTful API → Spring Boot 3 → MyBatis-Plus → MySQL
```

## 项目目录

```
personal-hub
├── personal-hub-server/  # Spring Boot 后端
├── personal-hub-web/     # Vue 3 前端
├── sql/                  # 数据库脚本
├── docs/                 # 项目文档
└── README.md
```

## 功能清单

### 已实现
- **用户认证**: JWT 登录/退出、个人信息、修改密码
- **笔记**: Markdown 编辑器、分类/标签、搜索/收藏/回收站
- **学习记录**: 时长/主题/内容 CRUD、日周月统计、连续天数
- **待办**: 优先级、截止日期、完成切换、拖拽排序、已完成折叠
- **文件管理**: 上传/下载/预览、分类管理、搜索
- **日记**: Markdown 编辑、月视图、心情/天气/地点/配图
- **收藏夹**: 网址 CRUD、分类、搜索
- **学习计划**: 进度跟踪、时间管理、剩余天数、超期标记、关联学习记录
- **阅读记录**: 章节进度、评分(1-5星)、阅读时长、阅读笔记
- **统一标签系统**: 全模块关联、颜色标记、使用次数统计
- **Dashboard**: 统计卡片、ECharts 趋势图（学习/笔记/Todo/阅读）
- **全局搜索**: 跨 8 模块、关键词高亮、结果分组
- **通知系统**: 自动检测（超期/截止/完成）、定时任务、Bell 红标下拉
- **UI**: 响应式、深色模式、5 种强调色、Command Palette、侧边栏三段分组
- **统一 UI 组件库**: UiDialog/UiInput/UiSelect/UiButton/UiSection/UiCard 等 8 个封装组件
- **CRUD 交互模式**: 全部模块使用 Dialog/Drawer（替代路由跳转表单页）
- **统一分类系统**: 笔记/收藏夹/文件分类合并为单表管理（Tab 切换）
- **工作台自定义**: 菜单/Dashboard 卡片显示隐藏、拖拽排序、每个用户独立布局

### 后续可扩展
AI 总结/问答 / OCR / PDF 预览 / MD 导出 PDF/HTML / 数据备份 / WebDAV / Docker

## 领域模块设计

当前后端按业务表拆分为 14 个模块（ph-auth/ph-note/ph-study/...），在**单体 Spring Boot + 单数据库**背景下偏细。

推荐按**业务领域**聚合为 6+1 模块，前后端保持一致：

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

```
personal-hub-web/src/modules/
├── system/          # 登录
├── knowledge/       # note / diary / study / reading / tag
├── planning/        # todo / studyplan
├── resource/        # bookmark / file
├── dashboard/       # Dashboard 首页
└── search/           # 全局搜索
```

### 领域划分原则

- **按业务领域聚合，不按数据库表划分模块** — 避免"每张表一个 Module"的数据库思维
- **模块职责内聚**：一个领域内的增删改查放在同一模块
- **跨模块引用**：通过 ph-common 或 Service 调用，不直接跨模块依赖 Entity
- **持续演进**：当前仍保留 14 个独立模块，后续重构时逐步合并到领域模块
- **Dashboard 仅聚合**：只负责跨模块查询，不保存业务数据

## 模块依赖关系

```
ph-system（用户/认证/通知）
    │
    ├── ph-knowledge（笔记 / 日记 / 学习记录 / 阅读记录 / 标签）
    ├── ph-planning（Todo / 学习计划）
    ├── ph-resource（收藏夹 / 文件管理）
    │
    └── ph-dashboard（聚合查询 + 全局搜索 + 趋势统计）
```

## 项目原则

1. **可读性优先** — 代码是人读的，不是写给机器看的
2. **可维护性优先** — 结构统一，命名一致，降低心智负担
3. **按领域组织，不按表组织** — 避免数据库思维，前后端统一领域模型
4. **不重复代码** — 公共能力沉淀到 shared/common，不各自造轮子
5. **不随意引入依赖** — 评估体积和必要性，优先社区标准方案
6. **前后端命名一致** — API 路径、模块名、领域模型名称前后端对齐
7. **接口风格统一** — RESTful 规范，统一返回格式/分页/错误码
8. **数据库设计一致** — 主键/时间/逻辑删除/索引策略统一
9. **纯函数优先** — 无副作用逻辑抽为纯函数，方便测试和复用
10. **新增功能更新文档** — 改代码先改文档，铁律
