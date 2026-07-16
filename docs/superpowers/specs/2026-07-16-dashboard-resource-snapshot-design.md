# 首页资源盘点小统计 — 设计规格

**日期：** 2026-07-16  
**状态：** 已批准（对话确认）

## 背景

首页曾与「数据统计」页重叠展示 KPI / 趋势 / 活动，已剔除。独立「待办任务」卡片与「今日任务」中的今日待办重复。需要一块不重复的小统计填补 Bento 信息密度。

## 目标

1. 从首页移除 `pending_todos`（待办任务卡片）及相关实现。
2. 新增 `resource_snapshot`（资源盘点）整行条带，展示与统计页 KPI **不重复**的库视角指标。
3. 保持配置驱动（`layoutStore.visibleDashboardCards`），兼容旧本地/后端布局。

## 非目标

- 不改统计页（`/stats`）任何卡片或图表。
- 不新增后端 API；复用 `GET /dashboard/stats`（`DashboardStats`）。
- 不做趋势图、环比、完成率、连续学习天数（这些属于统计页）。
- 不移除「今日任务」内的今日待办勾选（仅删独立待办卡片）。

## 指标定义

| 展示标签 | 字段 | 点击跳转 |
|---------|------|---------|
| 本月日记 | `diaryCountThisMonth` | `/diaries` |
| 收藏 | `bookmarkCount` | `/bookmarks` |
| 文件 | `fileCount` | `/files` |
| 在读中 | `readingInProgress` | `/readings` |

空值按 `0` 显示。

## 布局与配置

- **code：** `resource_snapshot`
- **默认标题：** 资源盘点
- **默认：** `visible: true`，`order` 介于快捷操作与最近列表之间（建议 order=3，其后 recent_* 顺延）
- **栅格：** `DASHBOARD_CARD_SPAN.resource_snapshot = 12`（整行）
- **位置语义：** 渲染顺序由 order 决定；默认落在今日任务 + 快捷操作下方、最近列表上方
- **移除：** `pending_todos` 加入 `REMOVED_DASHBOARD_CODES`；删除 `PendingTodosWidget.vue`
- **本地存储 key：** 升为 `layout-dashboard-v4`，启动时 `ensureDashboardCards` 剔除废弃 code 并补齐新 code

## UI / 组件

- 新组件：`widgets/ResourceSnapshotWidget.vue`
- 沿用 `DashCard`；正文为 4 列等分格子（数字 + 标签）
- 每格可点击跳转；无图表、无环比文案
- 桌面 4 列；窄屏可 2×2（与现有 dashboard 断点风格一致即可）
- `Dashboard.vue`：从 `stats` 传入上述四字段；去掉 `pendingTodos` / `getTodoList`（仅服务于已删卡片时）及 `PendingTodosWidget` 映射；保留今日任务用的 `getTodayTodos` 与 `handleToggleDone`

## 设置页

- `DashboardManager` 随 `DEFAULT_DASHBOARD_ITEMS` 自动出现「资源盘点」开关；不再出现「待办任务」

## 文档

实现完成后更新：

- `docs/CHANGELOG.md`
- `docs/STYLE_GUIDE.md`（Dashboard Bento 默认 code 列表）
- 如有必要微调 `docs/PROJECT.md` 一行描述

## 验收

1. 首页无独立「待办任务」卡片；设置里也无该项。
2. 可见「资源盘点」整行：四指标正确，点击进对应模块。
3. 与 `/stats` KPI（笔记/阅读时长/Todo 完成率/连续学习）视觉与语义均不重复。
4. 旧 `layout-dashboard-v3` / 后端含 `pending_todos` 的配置加载后被清理且补上 `resource_snapshot`。
