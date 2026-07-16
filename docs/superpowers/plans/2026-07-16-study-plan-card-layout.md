# 学习计划卡片布局调整 实现计划

> **面向 AI 代理的工作者：** 必需子技能：使用 superpowers:subagent-driven-development（推荐）或 superpowers:executing-plans 逐任务实现此计划。步骤使用复选框（`- [ ]`）语法来跟踪进度。

**目标：** 将学习计划卡片视图改为规格中的 B 方案信息层级（软色标签最多 2 个 +`+N`、来源·作者合并、点分日期、相对更新时间底栏）。

**架构：** 仅改前端 `List.vue` 卡片 template 与若干纯展示格式函数；表格视图与 API 不动。相对时间与日期格式为本地 helper，不抽独立包。

**技术栈：** Vue 3 + Element Plus（`el-progress`）+ 现有 soft-tag 样式

**规格：** `docs/superpowers/specs/2026-07-16-study-plan-card-layout-design.md`

---

## 文件

| 文件 | 职责 |
|------|------|
| 修改：`personal-hub-web/src/modules/planning/studyplan/List.vue` | 卡片 template、格式函数、卡片内部 CSS |
| 可选文档：`docs/CHANGELOG.md` | 记一条 UI 调整（若项目惯例要求提交前同步） |

---

### 任务 1：展示格式 helper

**文件：**
- 修改：`personal-hub-web/src/modules/planning/studyplan/List.vue`（script）

- [ ] **步骤 1：替换 / 新增格式函数**

将 `formatDate` 改为点分格式；新增 `formatDateRange`、`formatRelativeUpdated`、`sourceAuthorLine`、`visibleTags`：

```ts
function formatDate(d: string | null) {
  if (!d) return '—'
  return d.slice(0, 10).replace(/-/g, '.')
}

function formatDateRange(start: string | null, end: string | null) {
  return `${formatDate(start)} ~ ${formatDate(end)}`
}

function formatRelativeUpdated(d: string | null | undefined) {
  if (!d) return '更新于 —'
  const t = new Date(d.replace(' ', 'T')).getTime()
  if (Number.isNaN(t)) return `更新于 ${formatDate(d)}`
  const diff = Date.now() - t
  const m = Math.floor(diff / 60000)
  if (m < 1) return '更新于 刚刚'
  if (m < 60) return `更新于 ${m} 分钟前`
  const h = Math.floor(m / 60)
  if (h < 24) return `更新于 ${h} 小时前`
  const days = Math.floor(h / 24)
  if (days < 7) return `更新于 ${days} 天前`
  return `更新于 ${formatDate(d)}`
}

function sourceAuthorLine(source: string | null, author: string | null) {
  const src = sourceDisplay(source)
  const parts: string[] = []
  if (src) parts.push(`${src.icon} ${src.text}`)
  if (author?.trim()) parts.push(author.trim())
  if (parts.length === 0) return null
  return parts.join(' · ')
}

function visibleTags(tags: StudyPlanVO['tags']) {
  const list = tags || []
  return { shown: list.slice(0, 2), more: Math.max(0, list.length - 2) }
}
```

删除仅被旧卡片使用的绝对时间展示依赖（`formatUpdated` 若表格仍用则保留）。

- [ ] **步骤 2：手动核对**

在浏览器控制台或临时调用确认：
- `formatDate('2025-10-20')` → `2025.10.20`
- `formatRelativeUpdated` 对「2 天前」量级合理

---

### 任务 2：重写卡片 template

**文件：**
- 修改：`personal-hub-web/src/modules/planning/studyplan/List.vue`（`.card-grid` 内 `plan-card`）

- [ ] **步骤 1：按规格顺序替换卡片内容**

结构要点：

```vue
<div class="plan-card" @click="openEdit(plan.id)">
  <div class="card-top">
    <div class="name-title">{{ plan.name }}</div>
    <span class="status-dot-label">…</span>
  </div>
  <div v-if="plan.tags?.length" class="card-tags">
    <span v-for="t in visibleTags(plan.tags).shown" … class="soft-tag">{{ t.name }}</span>
    <span v-if="visibleTags(plan.tags).more" class="tag-more">+{{ visibleTags(plan.tags).more }}</span>
  </div>
  <div class="card-source-line">
    <template v-if="sourceAuthorLine(plan.source, plan.author)">
      {{ sourceAuthorLine(plan.source, plan.author) }}
    </template>
    <span v-else class="muted">来源 —</span>
  </div>
  <el-progress … class="card-progress" />
  <div class="card-dates">{{ formatDateRange(plan.startDate, plan.endDate) }}</div>
  <el-tooltip v-if="plan.remark" …>
    <div class="card-remark">{{ plan.remark }}</div>
  </el-tooltip>
  <div v-else class="card-remark card-remark--empty" />
  <div class="card-footer" @click.stop>
    <span class="card-updated">{{ formatRelativeUpdated(plan.updatedAt) }}</span>
    <div class="card-actions">…链接 + ⋯…</div>
  </div>
</div>
```

注意：循环内避免多次调用 `visibleTags`——可用内联计算或在模板用一次赋值；若不便，接受两次调用（tags 很短）。

- [ ] **步骤 2：调整 CSS**

- `.card-footer`：`justify-content: space-between`（左时间右操作）
- `.card-source-line` / `.card-dates`：次要文字色、单行
- `.tag-more`：与 soft-tag 对齐的小号灰色文字
- 去掉旧的分列 `card-meta-row` / `card-author` / 双行 `card-meta` 若不再使用

---

### 任务 3：浏览器验证

**文件：** 无

- [ ] **步骤 1：** 打开 `http://localhost:3000/study-plans`，切到卡片视图
- [ ] **步骤 2：** 确认 5×2 仍铺满；卡片顺序与规格一致
- [ ] **步骤 3：** 找多标签计划（或临时改数据）确认只显示 2 个 + `+N`
- [ ] **步骤 4：** 确认底栏相对时间与外链/⋯ 可用；表格视图未破坏

---

### 任务 4：文档（提交前）

**文件：**
- 修改：`docs/CHANGELOG.md`

- [ ] **步骤 1：** 在当日条目追加：学习计划卡片视图信息层级调整（软色标签截断、来源合并、相对更新时间）
- [ ] **步骤 2：** **仅在用户要求 commit 时**再提交；本计划不自动 commit

---

## 规格覆盖自检

| 规格项 | 任务 |
|--------|------|
| 标题+状态 | 任务 2 |
| 软色标签最多 2 +`+N` | 任务 1 `visibleTags` + 任务 2 |
| 来源 · 作者 | 任务 1 `sourceAuthorLine` + 任务 2 |
| 进度条 | 任务 2（沿用） |
| 点分日期范围 | 任务 1 + 2 |
| 备注单行 | 任务 2 |
| 相对更新 + 右操作 | 任务 1 + 2 |
| 5×2 / 交互不变 | 任务 3 验证 |
| 表格/API 不改 | 范围约束 |
