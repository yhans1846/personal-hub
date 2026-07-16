# 首页六宫格 + 外部快捷（收藏夹）— 设计规格

**日期：** 2026-07-16  
**状态：** 已批准（对话确认）

## 背景

首页卡片尺寸不齐（7/5、整行资源盘点、底部 4+4+4）。用户要求等宽等高，并新增「外部快捷」卡片；入口数据来自收藏夹，在条目上用开关控制是否展示到首页。系统设置中的 Dashboard 布局管理需同步新卡片清单。

## 目标

1. 首页 Bento 改为 **2×3 六宫格**：六张卡 `span=4`、等高拉伸。
2. 新增 `external_links`（外部快捷）：展示收藏夹中 `show_on_dashboard=1` 的书签。
3. 收藏夹编辑增加「展示到首页」开关；列表可有轻量角标。
4. **系统设置 → Dashboard 布局**（`DashboardManager`）与默认卡片同步：出现「外部快捷」、移除「资源盘点」。
5. 资源盘点独立卡移除，四指标并入 Hero 可点摘要。

## 非目标

- 不改 `/stats`。
- 不新建独立「首页快捷」表。
- 不重做收藏夹列表大改版（仅开关 + 角标）。
- 不引入拖拽排序字段（本版按 `updated_at` 降序取前 N）。

## 卡片清单（默认）

| order | code | title | visible | span |
|------:|------|-------|---------|-----:|
| 1 | `today_plan` | 今日任务 | true | 4 |
| 2 | `quick_actions` | 快捷操作 | true | 4 |
| 3 | `external_links` | 外部快捷 | true | 4 |
| 4 | `recent_notes` | 最近编辑 | true | 4 |
| 5 | `recent_studies` | 最近学习 | true | 4 |
| 6 | `recent_reading` | 最近阅读 | true | 4 |

`REMOVED_DASHBOARD_CODES` 追加：`resource_snapshot`（保留既有 `kpi_strip` / `weekly_trend` / `recent_activity` / `pending_todos`）。

本地存储 key：`layout-dashboard-v5`。`ensureDashboardCards` 继续负责补齐/剔除。

### 系统设置 Dashboard 布局

- `DashboardManager` 绑定 `layoutStore.dashboardCards`，本身无硬编码卡片列表。
- 实现时确保：`DEFAULT_DASHBOARD_ITEMS` 变更后，设置页展示「外部快捷」、不再展示「资源盘点」；重置布局回到上表。
- 验收时必须打开 **系统设置 → 工作台/Dashboard 布局** 核对开关与排序项。

## 布局与等高

- `DASHBOARD_CARD_SPAN`：上述六码均为 `4`。
- 网格：`align-items: stretch`；`DashCard` 保持 `height: 100%`。
- 窄屏（≤900px）：现有规则可继续强制 `span 12` 单列。
- 卡片内容区滚动或截断，避免单卡撑破行高；行内六卡视觉同高。

## Hero 资源摘要

原 `resource_snapshot` 四指标迁入 Hero（`getDashboardStats`）：

- 本月日记 → `/diaries`
- 收藏 → `/bookmarks`
- 文件 → `/files`
- 在读中 → `/readings`

展示为紧凑可点链接/chip，不做大卡。

## 后端：收藏夹字段与查询

### Schema

`bookmark_url` 新增：

| 字段 | 类型 | 说明 |
|------|------|------|
| `show_on_dashboard` | TINYINT NOT NULL DEFAULT 0 | 1=展示到首页外部快捷 |

同步：`sql/init.sql`、增量 migration/说明、`docs/DATABASE.md`、实体 `BookmarkUrl`、DTO/VO、前端 `BookmarkVO` / `BookmarkCreateDTO`。

### API

- 创建/更新：`BookmarkCreateDTO` 增加 `showOnDashboard`（Boolean/Integer）。
- 列表：支持可选筛选 `showOnDashboard`；或新增轻量接口  
  `GET /bookmarks/dashboard?limit=8`  
  返回当前用户 `show_on_dashboard=1` 且未删除，按 `updated_at DESC`，最多 8 条。  
  **本规格采用专用 dashboard 列表接口**，避免污染通用分页语义。

## 前端

### 收藏夹

- `BookmarkDialog`：开关「展示到首页」。
- `List.vue`：已开启项显示小角标（如「首页」）。

### Dashboard

- 新组件 `ExternalLinksWidget.vue`：列表项标题 + 域名/favicon；点击新窗口打开 `url`；空态文案 + 链到 `/bookmarks`。
- `Dashboard.vue`：挂载 `external_links`；删除 `ResourceSnapshotWidget` 引用；Hero 接入四指标链接。
- 删除或停用 `ResourceSnapshotWidget.vue`（文件删除）。

## 文档

实现后更新：`CHANGELOG`、`STYLE_GUIDE`（Bento 默认 code）、`DATABASE.md`、`API.md`（若有书签接口章节）、`PROJECT.md`（如需）。

## 验收

1. 首页六张等宽等高；无独立资源盘点卡；Hero 有四资源入口。
2. 勾选「展示到首页」的书签出现在「外部快捷」；取消后消失。
3. **系统设置 → Dashboard 布局** 含「外部快捷」，无「资源盘点」；显隐/排序生效。
4. 旧布局 key / 含 `resource_snapshot` 的配置被清理并补齐 `external_links`。
5. 相关单测（ensureDashboardCards）与后端字段读写通过。
