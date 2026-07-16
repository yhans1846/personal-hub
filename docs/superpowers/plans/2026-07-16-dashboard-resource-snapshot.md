# 首页资源盘点小统计 实现计划

> **面向 AI 代理的工作者：** 必需子技能：使用 superpowers:subagent-driven-development（推荐）或 superpowers:executing-plans 逐任务实现此计划。步骤使用复选框（`- [ ]`）语法来跟踪进度。

**目标：** 删除首页独立「待办任务」卡片，新增整行「资源盘点」小统计（本月日记 / 收藏 / 文件 / 在读中），且不与 `/stats` KPI 重复。

**架构：** 沿用 Bento + `layoutStore.visibleDashboardCards`。新组件 `ResourceSnapshotWidget` 消费已有 `DashboardStats` 四字段；`pending_todos` 进入 `REMOVED_DASHBOARD_CODES`；本地 key 升至 `layout-dashboard-v4`。

**技术栈：** Vue 3 + TypeScript + Pinia + Vitest + 现有 `DashCard` / `getDashboardStats`

**规格：** `docs/superpowers/specs/2026-07-16-dashboard-resource-snapshot-design.md`

---

## 文件结构

| 文件 | 职责 |
|------|------|
| `personal-hub-web/src/modules/dashboard/defaultLayouts.ts` | 默认卡片、span、REMOVED、ensure |
| `personal-hub-web/src/modules/dashboard/__tests__/ensureDashboardCards.test.ts` | ensure / 剔除逻辑单测 |
| `personal-hub-web/src/modules/dashboard/widgets/ResourceSnapshotWidget.vue` | 资源盘点 UI |
| `personal-hub-web/src/modules/dashboard/widgets/PendingTodosWidget.vue` | **删除** |
| `personal-hub-web/src/modules/dashboard/Dashboard.vue` | 挂载新 widget、去掉 pending |
| `personal-hub-web/src/store/layoutStore.ts` | `layout-dashboard-v4` |
| `docs/CHANGELOG.md` / `docs/STYLE_GUIDE.md` / `docs/PROJECT.md` | 文档同步 |

---

### 任务 1：布局默认值 + ensure 单测（TDD）

**文件：**
- 修改：`personal-hub-web/src/modules/dashboard/defaultLayouts.ts`
- 创建：`personal-hub-web/src/modules/dashboard/__tests__/ensureDashboardCards.test.ts`
- 修改：`personal-hub-web/src/store/layoutStore.ts`（仅 STORAGE_KEY）

- [ ] **步骤 1：编写失败的测试**

```ts
import { describe, it, expect } from 'vitest'
import {
  DEFAULT_DASHBOARD_ITEMS,
  REMOVED_DASHBOARD_CODES,
  ensureDashboardCards,
  DASHBOARD_CARD_SPAN,
} from '../defaultLayouts'

describe('ensureDashboardCards', () => {
  it('剔除 pending_todos 并补齐 resource_snapshot', () => {
    const result = ensureDashboardCards([
      { code: 'today_plan', title: '今日任务', order: 1, visible: true },
      { code: 'pending_todos', title: '待办任务', order: 9, visible: true },
    ])
    expect(result.some(c => c.code === 'pending_todos')).toBe(false)
    expect(result.some(c => c.code === 'resource_snapshot')).toBe(true)
    expect(REMOVED_DASHBOARD_CODES.has('pending_todos')).toBe(true)
  })

  it('resource_snapshot 默认 span 为 12 且默认可见', () => {
    expect(DASHBOARD_CARD_SPAN.resource_snapshot).toBe(12)
    const item = DEFAULT_DASHBOARD_ITEMS.find(c => c.code === 'resource_snapshot')
    expect(item?.visible).toBe(true)
  })
})
```

- [ ] **步骤 2：运行测试验证失败**

```bash
cd personal-hub-web && pnpm exec vitest run src/modules/dashboard/__tests__/ensureDashboardCards.test.ts
```

预期：FAIL（尚无 `resource_snapshot` / `pending_todos` 未进 REMOVED）

- [ ] **步骤 3：更新 defaultLayouts.ts**

将 `DEFAULT_DASHBOARD_ITEMS` 改为：

```ts
export const DEFAULT_DASHBOARD_ITEMS: CardItem[] = [
  { code: 'today_plan', title: '今日任务', order: 1, visible: true },
  { code: 'quick_actions', title: '快捷操作', order: 2, visible: true },
  { code: 'resource_snapshot', title: '资源盘点', order: 3, visible: true },
  { code: 'recent_notes', title: '最近编辑', order: 4, visible: true },
  { code: 'recent_studies', title: '最近学习', order: 5, visible: true },
  { code: 'recent_reading', title: '最近阅读', order: 6, visible: true },
]
```

`REMOVED_DASHBOARD_CODES` 加入 `'pending_todos'`（保留已有 kpi_strip / weekly_trend / recent_activity）。

`DASHBOARD_CARD_SPAN`：删除 `pending_todos`，增加 `resource_snapshot: 12`。

- [ ] **步骤 4：layoutStore 升 key**

`STORAGE_KEY_DASHBOARD = 'layout-dashboard-v4'`

- [ ] **步骤 5：运行测试验证通过**

```bash
cd personal-hub-web && pnpm exec vitest run src/modules/dashboard/__tests__/ensureDashboardCards.test.ts
```

预期：PASS

- [ ] **步骤 6：Commit**（仅当用户要求提交时执行；否则跳过）

```bash
git add personal-hub-web/src/modules/dashboard/defaultLayouts.ts \
  personal-hub-web/src/modules/dashboard/__tests__/ensureDashboardCards.test.ts \
  personal-hub-web/src/store/layoutStore.ts
git commit -m "$(cat <<'EOF'
feat(dashboard): 资源盘点布局默认值与 ensure 兼容

EOF
)"
```

---

### 任务 2：ResourceSnapshotWidget + 接入 Dashboard

**文件：**
- 创建：`personal-hub-web/src/modules/dashboard/widgets/ResourceSnapshotWidget.vue`
- 修改：`personal-hub-web/src/modules/dashboard/Dashboard.vue`
- 删除：`personal-hub-web/src/modules/dashboard/widgets/PendingTodosWidget.vue`

- [ ] **步骤 1：创建 ResourceSnapshotWidget.vue**

Props：

```ts
defineProps<{
  diaryCountThisMonth: number
  bookmarkCount: number
  fileCount: number
  readingInProgress: number
}>()
```

结构：`DashCard` title=`资源盘点`，icon 用 `Boxes`（lucide-vue-next）；body 为 4 格 grid，每格 `router-link`：

| 标签 | 值 | to |
|------|-----|-----|
| 本月日记 | diaryCountThisMonth | `/diaries` |
| 收藏 | bookmarkCount | `/bookmarks` |
| 文件 | fileCount | `/files` |
| 在读中 | readingInProgress | `/readings` |

样式要点：`grid-template-columns: repeat(4, 1fr)`；`@media (max-width: 640px) { repeat(2, 1fr) }`；数字用 `var(--text-xl)` / `font-weight: 600`；标签 `var(--text-xs)` / `var(--text-tertiary)`；格子 hover 用 `var(--bg-hover)`。

- [ ] **步骤 2：改 Dashboard.vue**

1. 删除 `PendingTodosWidget` import / `widgetMap.pending_todos` / `pendingTodos` ref / `getTodoList` 调用 / `widgetProps` 的 `pending_todos` 分支。
2. 保留 `getTodayTodos`、`todayTodos`、`handleToggleDone`（今日任务仍需要）。
3. 增加：

```ts
import ResourceSnapshotWidget from './widgets/ResourceSnapshotWidget.vue'
// widgetMap:
resource_snapshot: ResourceSnapshotWidget,
```

```ts
case 'resource_snapshot':
  return {
    diaryCountThisMonth: stats.value?.diaryCountThisMonth ?? 0,
    bookmarkCount: stats.value?.bookmarkCount ?? 0,
    fileCount: stats.value?.fileCount ?? 0,
    readingInProgress: stats.value?.readingInProgress ?? 0,
  }
```

4. 若 `handleToggleDone` 里仍更新 `pendingTodos`，一并删掉对该数组的操作。

- [ ] **步骤 3：删除 PendingTodosWidget.vue**

- [ ] **步骤 4：全量单测**

```bash
cd personal-hub-web && pnpm test --run
```

预期：全部 PASS

- [ ] **步骤 5：Commit**（仅当用户要求时）

```bash
git commit -m "$(cat <<'EOF'
feat(dashboard): 资源盘点小统计替换独立待办卡片

EOF
)"
```

---

### 任务 3：文档同步

**文件：**
- `docs/CHANGELOG.md`
- `docs/STYLE_GUIDE.md`（Dashboard Bento 默认 code）
- `docs/PROJECT.md`（若 22b 描述仍写「待办」则微调）

- [ ] **步骤 1：CHANGELOG 追加一条**

说明：移除首页独立待办卡片；新增资源盘点（本月日记/收藏/文件/在读）；layout key v4。

- [ ] **步骤 2：STYLE_GUIDE 更新默认 code 列表**

含 `resource_snapshot`；注明 `pending_todos` 已移除。

- [ ] **步骤 3：浏览器或手工核对**（开发服已开时）

1. `/dashboard`：有资源盘点整行，无独立待办卡  
2. 四格点击跳转正确  
3. `/settings` 首页卡片管理：有资源盘点、无待办任务  
4. `/stats` KPI 不变  

- [ ] **步骤 4：Commit**（仅当用户要求时）

---

## 自检

| 规格项 | 任务 |
|--------|------|
| 删 pending_todos + 组件 | 任务 1 + 2 |
| resource_snapshot 四指标与跳转 | 任务 2 |
| span 12 / order / ensure / v4 | 任务 1 |
| 不撞 stats KPI | 指标选型已在规格锁定 |
| 文档 | 任务 3 |
