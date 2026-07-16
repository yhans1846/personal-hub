# 首页六宫格 + 外部快捷 实现计划

> **面向 AI 代理的工作者：** 必需子技能：使用 superpowers:subagent-driven-development（推荐）或 superpowers:executing-plans 逐任务实现此计划。步骤使用复选框（`- [ ]`）语法来跟踪进度。

**目标：** 首页改为等宽等高 2×3 六宫格；用收藏夹「展示到首页」书签填充「外部快捷」；资源盘点并入 Hero；系统设置 Dashboard 布局同步新卡片清单。

**架构：** DB 字段 `show_on_dashboard` + `GET /api/bookmarks/dashboard`；前端 `external_links` widget + `defaultLayouts`/`layout-dashboard-v5`；`DashboardManager` 无硬编码，随 store 默认项自动更新。

**技术栈：** Spring Boot 3 + MyBatis-Plus、Vue 3、Vitest、现有 DashCard / layoutStore

**规格：** `docs/superpowers/specs/2026-07-16-dashboard-equal-grid-external-links-design.md`

---

## 文件结构

| 文件 | 职责 |
|------|------|
| `sql/migration-20260716-bookmark-dashboard.sql` | ALTER 加字段 |
| `sql/init.sql` / `docs/DATABASE.md` | 表结构同步 |
| `BookmarkUrl.java` + DTO/VO + Service/Impl + Controller | 字段与 dashboard 列表 API |
| `docs/API.md` | 接口文档 |
| `personal-hub-web/src/types/bookmark.ts` | 类型 |
| `personal-hub-web/src/modules/resource/api.ts` | `getDashboardBookmarks` |
| `BookmarkDialog.vue` / `List.vue` | 开关 + 角标 |
| `defaultLayouts.ts` + ensure 单测 | 六卡默认、移除 resource_snapshot |
| `layoutStore.ts` | key v5 |
| `ExternalLinksWidget.vue` | 首页外部快捷 |
| `Dashboard.vue` | 六宫格 + Hero 资源链；删 ResourceSnapshot |
| `docs/CHANGELOG.md` / `STYLE_GUIDE.md` | 文档 |

Commit 步骤仅在用户要求提交时执行。

---

### 任务 1：DB + 后端 API

**文件：**
- 创建：`sql/migration-20260716-bookmark-dashboard.sql`
- 修改：`sql/init.sql`、`BookmarkUrl`、`BookmarkCreateDTO`、`BookmarkVO`、Service/Impl、Controller
- 修改：`docs/DATABASE.md`、`docs/API.md`

- [ ] **步骤 1：写 migration**

```sql
ALTER TABLE `bookmark_url`
  ADD COLUMN `show_on_dashboard` tinyint NOT NULL DEFAULT 0 COMMENT '展示到首页外部快捷' AFTER `category_id`;
```

同步 `init.sql` 建表语句加入同名字段；`DATABASE.md` 表 9 增加一行。

- [ ] **步骤 2：实体与 DTO/VO**

`BookmarkUrl` 增加 `private Integer showOnDashboard;`  
`BookmarkCreateDTO` 增加 `private Integer showOnDashboard;`（0/1，可空则默认 0）  
`BookmarkVO` + `from()` 映射该字段。

- [ ] **步骤 3：Service 方法**

```java
List<BookmarkVO> listForDashboard(Long userId, int limit);
```

实现：`eq userId`、`eq show_on_dashboard 1`、未删除、`orderByDesc updatedAt`、`last("LIMIT " + limit)`（limit 钳制 1～20，默认 8）。create/update 写入 `showOnDashboard`（null→0）。

- [ ] **步骤 4：Controller — 必须写在 `/{id}` 之前**

```java
@GetMapping("/dashboard")
public Result<List<BookmarkVO>> dashboardList(
    Authentication authentication,
    @RequestParam(defaultValue = "8") int limit) { ... }
```

- [ ] **步骤 5：本机执行 migration**（MySQL）

按项目实际库连接执行 `migration-20260716-bookmark-dashboard.sql`。

- [ ] **步骤 6：API.md**

在「收藏夹」节增加：`GET /api/bookmarks/dashboard?limit=8`；Create/Update body 增加 `showOnDashboard`。

---

### 任务 2：前端类型、API、收藏夹 UI

**文件：**
- `types/bookmark.ts`、`modules/resource/api.ts`
- `BookmarkDialog.vue`、`List.vue`

- [ ] **步骤 1：类型与 API**

```ts
// BookmarkVO / BookmarkCreateDTO 增加 showOnDashboard?: number
export function getDashboardBookmarks(limit = 8) {
  return request.get<Result<BookmarkVO[]>>('/bookmarks/dashboard', { params: { limit } })
}
```

- [ ] **步骤 2：BookmarkDialog**

表单增加 `el-switch`：「展示到首页」；加载详情时回填；提交写入 `showOnDashboard: form.showOnDashboard ? 1 : 0`。

- [ ] **步骤 3：List 角标**

标题旁：`v-if="item.showOnDashboard === 1"` 显示小号 tag「首页」。

---

### 任务 3：布局默认值 + ensure 单测（TDD）

**文件：**
- `defaultLayouts.ts`、`layoutStore.ts`
- `__tests__/ensureDashboardCards.test.ts`

- [ ] **步骤 1：扩展/改写测试**

断言：

- `resource_snapshot` 在 `REMOVED` 中且 ensure 结果不含  
- 含 `external_links`，`DASHBOARD_CARD_SPAN` 六码均为 4  
- `DEFAULT` 恰好 6 项且无 `resource_snapshot`

- [ ] **步骤 2：跑测期望失败**

```bash
cd personal-hub-web && pnpm exec vitest run src/modules/dashboard/__tests__/ensureDashboardCards.test.ts
```

- [ ] **步骤 3：改 defaultLayouts**

```ts
DEFAULT_DASHBOARD_ITEMS = [
  { code: 'today_plan', title: '今日任务', order: 1, visible: true },
  { code: 'quick_actions', title: '快捷操作', order: 2, visible: true },
  { code: 'external_links', title: '外部快捷', order: 3, visible: true },
  { code: 'recent_notes', title: '最近编辑', order: 4, visible: true },
  { code: 'recent_studies', title: '最近学习', order: 5, visible: true },
  { code: 'recent_reading', title: '最近阅读', order: 6, visible: true },
]
// REMOVED 加入 'resource_snapshot'
// DASHBOARD_CARD_SPAN：六码均为 4
```

- [ ] **步骤 4：** `STORAGE_KEY_DASHBOARD = 'layout-dashboard-v5'`

- [ ] **步骤 5：测试通过**

- [ ] **步骤 6：核对设置页数据源**

确认 `DashboardManager` 仅读 `layoutStore.dashboardCards`（无需改模板）；重置后列表为上表 6 项。手工验收记在任务 5。

---

### 任务 4：Dashboard UI

**文件：**
- 创建：`widgets/ExternalLinksWidget.vue`
- 修改：`Dashboard.vue`
- 删除：`widgets/ResourceSnapshotWidget.vue`

- [ ] **步骤 1：ExternalLinksWidget**

Props：`links: BookmarkVO[]`  
DashCard title「外部快捷」、`more-to="/bookmarks"`  
列表：favicon 或 Globe 图标 + title + 域名；`<a :href="url" target="_blank" rel="noopener">`  
空态：「去收藏夹勾选「展示到首页」」+ link。

- [ ] **步骤 2：Dashboard.vue**

- `widgetMap.external_links = ExternalLinksWidget`  
- 去掉 `resource_snapshot` / ResourceSnapshotWidget  
- `onMounted` 调 `getDashboardBookmarks(8)`  
- `widgetProps('external_links')` → `{ links }`  
- Hero：四资源可点链接（diaryCountThisMonth / bookmarkCount / fileCount / readingInProgress）  
- skeleton 可改为 6 个等宽块  

- [ ] **步骤 3：删除 ResourceSnapshotWidget.vue**

- [ ] **步骤 4：**

```bash
cd personal-hub-web && pnpm test --run
```

预期全部 PASS。

---

### 任务 5：文档 + 验收

- [ ] **步骤 1：** CHANGELOG / STYLE_GUIDE（六宫格 + external_links；设置页随默认项）  
- [ ] **步骤 2：** 浏览器核对  
  1. 首页六卡等宽等高  
  2. 勾选书签出现在外部快捷  
  3. **系统设置 → Dashboard 布局** 有「外部快捷」、无「资源盘点」；隐藏/排序生效  
  4. Hero 资源链可点  

---

## 自检

| 规格项 | 任务 |
|--------|------|
| 六宫格 span=4 等高 | 3 + 4 |
| external_links + 收藏开关 | 1 + 2 + 4 |
| 移除 resource_snapshot → Hero | 3 + 4 |
| 设置页 Dashboard 布局 | 3（数据）+ 5（验收） |
| DB/API 文档 | 1 + 5 |
