# 一屏铺满（列表 + Dashboard）实现计划

> **面向 AI 代理的工作者：** 必需子技能：使用 superpowers:subagent-driven-development（推荐）或 superpowers:executing-plans 逐任务实现此计划。步骤使用复选框（`- [ ]`）语法来跟踪进度。

**目标：** Dashboard 外锁内滚；笔记 / 日记 / 学习记录 / 待办 Table+Card（或清单）一屏铺满，默认 PAGE_SIZE=10、矮屏降 8/6，超出分页。

**架构：** 抽出纯函数 `resolveFillPageSize` + composable `useFillPageSize` / `useMainContentFill`；四列表对齐学习计划骨架；待办增加后端 `dueScope` 以支持 Tab 服务端分页（规格中的硬缺口）；Dashboard 挂 fill + DashCard body 内滚。

**技术栈：** Vue 3 · TypeScript · Vitest · Spring Boot（仅 TodoQuery）· 现有 `ListPagination` / `main-content--fill`

**规格：** `docs/superpowers/specs/2026-07-17-viewport-fit-lists-dashboard-design.md`

---

## 文件结构

| 文件 | 职责 |
|------|------|
| 创建 `personal-hub-web/src/composables/resolveFillPageSize.ts` | 高度 → 10/8/6 纯函数 |
| 创建 `personal-hub-web/src/composables/useFillPageSize.ts` | resize 防抖 + 响应式 pageSize |
| 创建 `personal-hub-web/src/composables/useMainContentFill.ts` | 挂载/卸载 `main-content--fill` |
| 创建 `personal-hub-web/src/composables/__tests__/resolveFillPageSize.test.ts` | 断点单测 |
| 修改 `personal-hub-web/src/modules/knowledge/note/List.vue` | fill + pageSize + 槽位 |
| 修改 `personal-hub-web/src/modules/knowledge/diary/List.vue` | 列表模式 fill（日历模式可保留自身滚动） |
| 修改 `personal-hub-web/src/modules/knowledge/study/List.vue` | fill + pageSize |
| 修改 `personal-hub-server/ph-biz/.../dto/TodoQueryDTO.java` | 新增 `dueScope` |
| 修改 `personal-hub-server/ph-biz/.../service/impl/TodoTaskServiceImpl.java` | dueScope 条件 |
| 修改 `personal-hub-web/src/types/todo.ts` | `dueScope?` |
| 修改 `personal-hub-web/src/modules/planning/todo/List.vue` | 服务端分页 + fill |
| 修改 `personal-hub-web/src/modules/dashboard/Dashboard.vue` | fill + 布局锁高 |
| 修改 `personal-hub-web/src/modules/dashboard/widgets/DashCard.vue` | body `overflow-y:auto` |
| 修改 `docs/LIST_PAGE_SPEC.md` / `docs/CHANGELOG.md` / `docs/API.md`（若有 Todo 查询节） | 文档同步 |

---

### 任务 1：resolveFillPageSize + 单测

**文件：**
- 创建：`personal-hub-web/src/composables/resolveFillPageSize.ts`
- 创建：`personal-hub-web/src/composables/__tests__/resolveFillPageSize.test.ts`

- [ ] **步骤 1：编写失败的测试**

```ts
import { describe, it, expect } from 'vitest'
import { resolveFillPageSize } from '../resolveFillPageSize'

describe('resolveFillPageSize', () => {
  it('≥640 → 10', () => {
    expect(resolveFillPageSize(640)).toBe(10)
    expect(resolveFillPageSize(900)).toBe(10)
  })
  it('520–639 → 8', () => {
    expect(resolveFillPageSize(520)).toBe(8)
    expect(resolveFillPageSize(639)).toBe(8)
  })
  it('<520 → 6', () => {
    expect(resolveFillPageSize(519)).toBe(6)
    expect(resolveFillPageSize(0)).toBe(6)
  })
})
```

- [ ] **步骤 2：运行测试确认失败**

```bash
cd personal-hub-web
pnpm test -- src/composables/__tests__/resolveFillPageSize.test.ts
```

预期：FAIL（模块不存在）

- [ ] **步骤 3：实现纯函数**

```ts
/** 按主内容区可视高度决定一屏条数（LIST_PAGE_SPEC 矮屏降档） */
export function resolveFillPageSize(contentHeightPx: number): number {
  if (contentHeightPx >= 640) return 10
  if (contentHeightPx >= 520) return 8
  return 6
}
```

- [ ] **步骤 4：运行测试确认通过**

```bash
pnpm test -- src/composables/__tests__/resolveFillPageSize.test.ts
```

预期：PASS

- [ ] **步骤 5：Commit**

```bash
git add personal-hub-web/src/composables/resolveFillPageSize.ts personal-hub-web/src/composables/__tests__/resolveFillPageSize.test.ts
git commit -m "feat: 新增 resolveFillPageSize 矮屏降档纯函数"
```

---

### 任务 2：useFillPageSize + useMainContentFill

**文件：**
- 创建：`personal-hub-web/src/composables/useFillPageSize.ts`
- 创建：`personal-hub-web/src/composables/useMainContentFill.ts`

- [ ] **步骤 1：实现 useMainContentFill**

```ts
import { onMounted, onUnmounted } from 'vue'

const FILL = 'main-content--fill'

export function useMainContentFill() {
  onMounted(() => {
    document.querySelector('.main-content')?.classList.add(FILL)
  })
  onUnmounted(() => {
    document.querySelector('.main-content')?.classList.remove(FILL)
  })
}
```

- [ ] **步骤 2：实现 useFillPageSize**

```ts
import { ref, onMounted, onUnmounted, watch } from 'vue'
import { resolveFillPageSize } from './resolveFillPageSize'

export function useFillPageSize(onSizeChange?: (size: number) => void) {
  const pageSize = ref(10)
  let timer: ReturnType<typeof setTimeout> | null = null

  function measure() {
    const el = document.querySelector('.main-content') as HTMLElement | null
    const h = el?.clientHeight ?? window.innerHeight
    const next = resolveFillPageSize(h)
    if (next !== pageSize.value) {
      pageSize.value = next
      onSizeChange?.(next)
    }
  }

  function onResize() {
    if (timer) clearTimeout(timer)
    timer = setTimeout(measure, 150)
  }

  onMounted(() => {
    measure()
    window.addEventListener('resize', onResize)
  })
  onUnmounted(() => {
    window.removeEventListener('resize', onResize)
    if (timer) clearTimeout(timer)
  })

  return { pageSize }
}
```

说明：调用方在 `onSizeChange` 里把 `query.size = size`、`query.page = 1` 并 `fetchList()`。也可用 `watch(pageSize, ...)`，二选一，页面内保持一种。

- [ ] **步骤 3：Typecheck / 快速编译**

```bash
cd personal-hub-web
pnpm exec vue-tsc -b --pretty false 2>&1 | Select-Object -First 30
```

预期：无与新文件相关的错误（或全绿）

- [ ] **步骤 4：Commit**

```bash
git add personal-hub-web/src/composables/useFillPageSize.ts personal-hub-web/src/composables/useMainContentFill.ts
git commit -m "feat: 新增 useFillPageSize / useMainContentFill"
```

---

### 任务 3：笔记列表接入 fill

**文件：**
- 修改：`personal-hub-web/src/modules/knowledge/note/List.vue`

参考：`personal-hub-web/src/modules/planning/studyplan/List.vue`（`plan-page` / 空槽 / fill）

- [ ] **步骤 1：接入 composable，替换固定 size:20**

在 `<script setup>`：

```ts
import { useMainContentFill } from '@/composables/useMainContentFill'
import { useFillPageSize } from '@/composables/useFillPageSize'

useMainContentFill()

const query = ref<NoteQuery>({ page: 1, size: 10, keyword: '' })

const { pageSize } = useFillPageSize((size) => {
  query.value.size = size
  query.value.page = 1
  fetchList()
})
```

将模板根节点改为：

```html
<div class="plan-page">
  <div class="plan-top">...</div>
  <div class="plan-middle">...</div>
  <div class="plan-foot">
    <ListPagination :total="total" :page="query.page" :size="pageSize" @change="onPageChange" />
  </div>
</div>
```

- [ ] **步骤 2：卡片区均分 + 空槽**

卡片容器使用与学习计划类似的 grid（`height:100%`、`grid-auto-rows:1fr`），并：

```html
<div
  v-for="n in Math.max(0, pageSize - list.length)"
  :key="'pad-' + n"
  class="note-card note-card--pad"
  aria-hidden="true"
/>
```

CSS 要点（对齐 studyplan）：

```css
.plan-page {
  display: flex;
  flex-direction: column;
  height: 100%;
  overflow: hidden;
}
.plan-middle {
  flex: 1;
  min-height: 0;
  overflow: hidden;
}
.note-card--pad {
  visibility: hidden;
  pointer-events: none;
}
```

- [ ] **步骤 3：本地确认**

```bash
cd personal-hub-web
pnpm test
```

预期：既有测试仍通过。手动打开 `/notes`：无整页滚动，分页 size 随高度为 10/8/6。

- [ ] **步骤 4：Commit**

```bash
git add personal-hub-web/src/modules/knowledge/note/List.vue
git commit -m "feat(notes): 列表一屏铺满与矮屏降档"
```

---

### 任务 4：日记列表接入 fill

**文件：**
- 修改：`personal-hub-web/src/modules/knowledge/diary/List.vue`

- [ ] **步骤 1：列表模式 fill**

同任务 3：`useMainContentFill` + `useFillPageSize`；`query.size` 从 20 改为跟随 `pageSize`。

日历模式（`showCalendar === true`）：可不强制 `plan-middle` 锁死——若日历需滚动，仅在列表模式给根节点 `plan-page`；或日历放在 middle 内 `overflow:auto`，**页面外层仍不滚**。

- [ ] **步骤 2：空槽与 ListPagination 的 size 绑定 pageSize**

与笔记相同模式。

- [ ] **步骤 3：Commit**

```bash
git add personal-hub-web/src/modules/knowledge/diary/List.vue
git commit -m "feat(diary): 列表一屏铺满与矮屏降档"
```

---

### 任务 5：学习记录列表接入 fill

**文件：**
- 修改：`personal-hub-web/src/modules/knowledge/study/List.vue`

- [ ] **步骤 1：接入 fill + pageSize**

同任务 3；时间线 / 卡片布局改为 middle 内均分或 `overflow:hidden` + 空槽；`query.size` 跟随 `pageSize`。

- [ ] **步骤 2：Commit**

```bash
git add personal-hub-web/src/modules/knowledge/study/List.vue
git commit -m "feat(study): 列表一屏铺满与矮屏降档"
```

---

### 任务 6：待办 dueScope 后端（硬缺口）

**文件：**
- 修改：`personal-hub-server/ph-biz/src/main/java/com/personalhub/planning/dto/TodoQueryDTO.java`
- 修改：`personal-hub-server/ph-biz/src/main/java/com/personalhub/planning/service/impl/TodoTaskServiceImpl.java`
- 可选单测：新建 `TodoTaskServiceImpl` 纯逻辑不便测则用 Mapper 集成测；最低要求 `mvn -pl ph-biz -am compile`

- [ ] **步骤 1：DTO 增加字段**

```java
@Schema(description = "日期范围：all|overdue|today|week|later|done；空同 all")
private String dueScope;
```

- [ ] **步骤 2：Service 筛选逻辑（在 isDone 条件之后）**

```java
String scope = query.getDueScope();
if (StringUtils.hasText(scope) && !"all".equalsIgnoreCase(scope)) {
    LocalDate today = LocalDate.now();
    switch (scope.toLowerCase()) {
        case "done" -> wrapper.eq(TodoTask::getIsDone, 1);
        case "overdue" -> wrapper.eq(TodoTask::getIsDone, 0)
                .isNotNull(TodoTask::getDueDate)
                .lt(TodoTask::getDueDate, today);
        case "today" -> wrapper.eq(TodoTask::getIsDone, 0)
                .eq(TodoTask::getDueDate, today);
        case "week" -> wrapper.eq(TodoTask::getIsDone, 0)
                .isNotNull(TodoTask::getDueDate)
                .gt(TodoTask::getDueDate, today)
                .le(TodoTask::getDueDate, today.plusDays(7));
        case "later" -> wrapper.eq(TodoTask::getIsDone, 0)
                .and(w -> w.isNull(TodoTask::getDueDate)
                        .or()
                        .gt(TodoTask::getDueDate, today.plusDays(7)));
        default -> { /* ignore unknown */ }
    }
}
```

注意：当 `dueScope=done` 时不要再与 `isDone=false` 冲突；前端只传 `dueScope`，可不传 `isDone`。

- [ ] **步骤 3：编译**

```bash
cd personal-hub-server
mvn -q -pl ph-biz -am compile
```

预期：BUILD SUCCESS

- [ ] **步骤 4：Commit**

```bash
git add personal-hub-server/ph-biz/src/main/java/com/personalhub/planning/dto/TodoQueryDTO.java \
  personal-hub-server/ph-biz/src/main/java/com/personalhub/planning/service/impl/TodoTaskServiceImpl.java
git commit -m "feat(todos): 查询支持 dueScope 服务端筛选分页"
```

---

### 任务 7：待办前端 fill + 服务端分页

**文件：**
- 修改：`personal-hub-web/src/types/todo.ts`
- 修改：`personal-hub-web/src/modules/planning/todo/List.vue`

- [ ] **步骤 1：类型**

```ts
export interface TodoQuery {
  page?: number
  size?: number
  keyword?: string
  priority?: number
  isDone?: boolean
  /** all|overdue|today|week|later|done */
  dueScope?: string
}
```

- [ ] **步骤 2：改造 List.vue 数据流**

删除「一次 size:9999 + 前端 groups 过滤驱动列表」；改为：

```ts
useMainContentFill()
const total = ref(0)
const query = ref<TodoQuery>({ page: 1, size: 10, keyword: '', dueScope: 'all' })
const currentTab = ref<TabKey>('all')

const { pageSize } = useFillPageSize((size) => {
  query.value.size = size
  query.value.page = 1
  fetchList()
})

async function fetchList() {
  loading.value = true
  try {
    const res = await getTodoList(query.value)
    list.value = res.data.data.records
    total.value = res.data.data.total
  } finally {
    loading.value = false
  }
}

function onTabChange(key: TabKey) {
  currentTab.value = key
  query.value.dueScope = key
  query.value.page = 1
  fetchList()
}
```

模板：根 `plan-page`；列表渲染 `list`（当前页）；底加 `ListPagination`；`PageHeader` 可用 `共 {{ total }} 项`。

Tab 角标：首期仅当前 tab 显示 `total`，其它 tab 不显示 count（避免再拉全量）。或角标省略。

拖拽：仅当前页 `list`；注释或文案不暗示跨页排序。

- [ ] **步骤 3：样式 fill**

列表容器在 `plan-middle` 内 `overflow:hidden` 或条目均分高度，保证整页不滚。

- [ ] **步骤 4：Commit**

```bash
git add personal-hub-web/src/types/todo.ts personal-hub-web/src/modules/planning/todo/List.vue
git commit -m "feat(todos): 服务端分页与一屏铺满"
```

---

### 任务 8：Dashboard 外锁内滚

**文件：**
- 修改：`personal-hub-web/src/modules/dashboard/Dashboard.vue`
- 修改：`personal-hub-web/src/modules/dashboard/widgets/DashCard.vue`

- [ ] **步骤 1：Dashboard 挂 fill + 布局**

```ts
import { useMainContentFill } from '@/composables/useMainContentFill'
useMainContentFill()
```

```css
.dashboard {
  height: 100%;
  overflow: hidden;
  display: flex;
  flex-direction: column;
  min-height: 0;
}
.hero {
  flex-shrink: 0;
}
.bento {
  flex: 1;
  min-height: 0;
  overflow: hidden;
  /* 保留现有 12 列 grid；行高改为自适应剩余空间 */
  align-content: stretch;
}
.bento-cell {
  min-height: 0; /* 覆盖原 min-height:220px 在 fill 下的撑破 */
  height: 100%;
  display: flex;
  flex-direction: column;
}
```

若 grid 行数导致仍溢出：给 `.bento` 使用 `grid-auto-rows: minmax(0, 1fr)`（按实际行数调试）。

- [ ] **步骤 2：DashCard body 内滚**

```css
.dash-card-body {
  flex: 1;
  min-height: 0;
  overflow-y: auto;
}
```

快捷 / 外链 / 今日 widget：内容过长时自然出现块内滚动，无需改业务请求。

- [ ] **步骤 3：手工确认 `/` 无整页滚动**

列表类卡片内容多时仅卡片内出现滚动条。

- [ ] **步骤 4：Commit**

```bash
git add personal-hub-web/src/modules/dashboard/Dashboard.vue \
  personal-hub-web/src/modules/dashboard/widgets/DashCard.vue
git commit -m "feat(dashboard): 一屏锁高与卡片内滚动"
```

---

### 任务 9：文档同步

**文件：**
- 修改：`docs/LIST_PAGE_SPEC.md`
- 修改：`docs/CHANGELOG.md`
- 修改：`docs/API.md`（待办查询参数增加 `dueScope`）

- [ ] **步骤 1：LIST_PAGE_SPEC**

将「一屏铺满（可选）」改为默认约定（本批页面 + 学习计划/阅读）；写入矮屏降档表与 `useFillPageSize` / `useMainContentFill` 名称。

- [ ] **步骤 2：CHANGELOG**

```markdown
### 2026-07-17 一屏铺满
- Dashboard 外锁内滚；笔记/日记/学习记录/待办 fill + 矮屏降档 10/8/6
- 待办查询新增 dueScope（all|overdue|today|week|later|done）
```

- [ ] **步骤 3：API.md**

在 Todo 列表查询参数表增加 `dueScope`。

- [ ] **步骤 4：Commit**

```bash
git add docs/LIST_PAGE_SPEC.md docs/CHANGELOG.md docs/API.md
git commit -m "docs: 同步一屏铺满与 dueScope 约定"
```

---

### 任务 10：终验

- [ ] **步骤 1：前端测试**

```bash
cd personal-hub-web
pnpm test
```

预期：PASS

- [ ] **步骤 2：后端编译/测试**

```bash
cd personal-hub-server
mvn -q test
```

预期：BUILD SUCCESS

- [ ] **步骤 3：手工清单**

| 路由 | 检查 |
|------|------|
| `/` | 无整页滚动；列表 widget 块内可滚 |
| `/notes` | Table/Card 或卡片网格一屏；分页 |
| `/diaries` | 列表模式一屏 |
| `/study-records` | 一屏 |
| `/todos` | Tab 切换重拉；分页；一屏 |
| `/study-plans` | 回归 fill 仍正常 |

---

## 自检对照规格

| 规格项 | 任务 |
|--------|------|
| resolveFillPageSize 10/8/6 | 1 |
| useFillPageSize / useMainContentFill | 2 |
| 笔记/日记/学习 | 3–5 |
| 待办服务端分页 + Tab | 6–7（含 dueScope 缺口） |
| Dashboard 外锁内滚 | 8 |
| LIST_PAGE_SPEC / CHANGELOG | 9 |
| 验证 | 10 |

---

## 执行交接

计划保存于 `docs/superpowers/plans/2026-07-17-viewport-fit-lists-dashboard.md`。

**两种执行方式：**

1. **子代理驱动（推荐）** — 每任务新子代理 + 审查  
2. **内联执行** — 本会话 executing-plans  

选哪种？
