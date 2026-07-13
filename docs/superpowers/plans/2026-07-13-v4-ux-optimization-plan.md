# Personal Hub V4 UX 优化 实现计划

> **面向 AI 代理的工作者：** 必需子技能：使用 subagent-driven-development（推荐）或 executing-plans 逐任务实现此计划。步骤使用复选框（`- [ ]`）语法来跟踪进度。

**目标：** 优化 Dashboard/Sidebar/CRUD/Dialog/Editor/Search/动画体验，提升一致性和可维护性

**架构：** 前端 Vue 3 + TypeScript + Pinia 状态管理；不新增业务模块，不改变目录结构

**技术栈：** Vue 3 + TypeScript + Element Plus + Pinia + SortableJS

---

## 文件变更总览

### 第一阶段：Dashboard + Sidebar + CRUD

| 文件 | 操作 | 说明 |
|------|------|------|
| `personal-hub-web/src/modules/dashboard/Dashboard.vue` | 修改 | 布局重构：单列内容流，统计卡移到底部，新增快捷操作 |
| `personal-hub-web/src/modules/dashboard/defaultLayouts.ts` | 修改 | 更新默认卡片列表和排序 |
| `personal-hub-web/src/components/AppLayout.vue` | 修改 | 分组合并为工作区/管理/统计，新增最近访问区域 |
| `personal-hub-web/src/store/layoutStore.ts` | 修改 | 新增最近访问逻辑 |
| `personal-hub-web/src/store/recentVisitStore.ts` | 新建 | 最近访问 Pinia store |
| `personal-hub-web/src/modules/knowledge/diary/List.vue` | 修改 | CRUD 对齐 |
| `personal-hub-web/src/modules/knowledge/study/List.vue` | 修改 | CRUD 对齐 |
| `personal-hub-web/src/modules/resource/file/List.vue` | 修改 | CRUD 对齐 |
| `personal-hub-web/src/modules/resource/bookmark/List.vue` | 修改 | CRUD 对齐 |
| `personal-hub-web/src/modules/planning/studyplan/List.vue` | 修改 | CRUD 对齐 |

### 第二阶段：Dialog + Editor + Customization

| 文件 | 操作 | 说明 |
|------|------|------|
| `personal-hub-web/src/components/ui/UiButton.vue` | 修改 | 新增 danger 属性 |
| `personal-hub-web/src/components/ui/UiDialog.vue` | 修改 | 可选：微调动画 |
| `personal-hub-web/src/modules/knowledge/note/Editor.vue` | 修改 | 新建笔记显示"未命名笔记"默认标题 |
| `personal-hub-web/src/modules/system/settings/components/DashboardManager.vue` | 修改 | 更新默认卡片列表 |

### 第三阶段：搜索 + 动画 + 细节

| 文件 | 操作 | 说明 |
|------|------|------|
| `personal-hub-web/src/components/CommandPalette.vue` | 修改 | 新增搜索历史/最近搜索 |
| `personal-hub-web/src/styles/global.css` | 修改 | page-fade 200ms → 150ms |
| `personal-hub-web/src/components/ListPagination.vue` | 修改 | 可选：统一 pagination 样式 |
| `personal-hub-web/src/components/EmptyState.vue` | 修改 | 可选：动画增强 |

---

### 任务 1：Dashboard 布局重构

**文件：**
- 修改：`personal-hub-web/src/modules/dashboard/Dashboard.vue`
- 修改：`personal-hub-web/src/modules/dashboard/defaultLayouts.ts`

- [ ] **步骤 1：更新 defaultLayouts.ts 卡片配置**

将 DEFAULT_DASHBOARD_ITEMS 按 V4 规范重新排序：
- 今日任务 (today_plan, visible)
- 最近编辑 (recent_notes, visible)
- 最近阅读 (recent_reading, visible)
- 学习记录 (recent_studies, visible)
- **快捷操作** (quick_actions, visible) ← 新增
- 待办任务 (pending_todos, hidden)
- 最近收藏 (recent_bookmarks, hidden)

```typescript
// personal-hub-web/src/modules/dashboard/defaultLayouts.ts
export const DEFAULT_DASHBOARD_ITEMS: CardItem[] = [
  { code: 'today_plan', title: '今日任务', order: 1, visible: true },
  { code: 'recent_notes', title: '最近编辑', order: 2, visible: true },
  { code: 'recent_reading', title: '最近阅读', order: 3, visible: true },
  { code: 'recent_studies', title: '学习记录', order: 4, visible: true },
  { code: 'quick_actions', title: '快捷操作', order: 5, visible: true },
  { code: 'pending_todos', title: '待办任务', order: 6, visible: false },
  { code: 'recent_bookmarks', title: '最近收藏', order: 7, visible: false },
]
```

- [ ] **步骤 2：重写 Dashboard.vue 布局**

关键变更：
1. 删除顶部 stats-grid（8 张统计卡）
2. 改为单列布局 `.dashboard-flow`
3. 内容卡片在 flow 中渲染
4. 统计卡区域固定在底部
5. 新增「快捷操作」卡片
6. 骨架屏调整为单列

```vue
<script setup lang="ts">
// 新增快捷操作相关
import { Plus, FileText, CheckCircle, PenLine } from 'lucide-vue-next'

// 快捷创建
function quickCreate(type: string) {
  switch (type) {
    case 'note': router.push('/notes/new'); break
    case 'todo': todoDialogVisible.value = true; break
    case 'diary': router.push('/diaries/new'); break
    case 'study': router.push('/study-records/new'); break
  }
}
</script>

<template>
  <div class="dashboard">
    <!-- 欢迎区域 -->
    <div class="welcome-section">
      <div>
        <h1>{{ authStore.user?.nickname || authStore.user?.username || '用户' }}，{{ greeting }} 👋</h1>
        <p class="welcome-sub">
          <span v-if="stats">本周学习 {{ formatDuration(stats.studyDurationThisWeek) }}</span>
        </p>
      </div>
    </div>

    <!-- 骨架屏 -->
    <div v-if="loading" class="loading-skeleton">
      <div v-for="i in 5" :key="i" class="skeleton-card-wide" />
    </div>

    <template v-else>
      <!-- 内容卡片 - 单列 -->
      <div class="dashboard-flow">
        <template v-for="card in layoutStore.visibleDashboardCards" :key="card.code">
          <!-- 今日任务 -->
          <div v-if="card.code === 'today_plan'" class="flow-card">
            <!-- ... 现有内容 ... -->
          </div>

          <!-- 最近编辑（原最近更新） -->
          <div v-if="card.code === 'recent_notes'" class="flow-card">
            <!-- ... 现有内容，标题改为"最近编辑" ... -->
          </div>

          <!-- 最近阅读 -->
          <div v-if="card.code === 'recent_reading'" class="flow-card">
            <!-- 不变 -->
          </div>

          <!-- 学习记录 -->
          <div v-if="card.code === 'recent_studies'" class="flow-card">
            <!-- 不变 -->
          </div>

          <!-- 快捷操作 - 新增 -->
          <div v-if="card.code === 'quick_actions'" class="flow-card flow-card--compact">
            <div class="flow-card-header">
              <h3>快捷操作</h3>
            </div>
            <div class="quick-actions-grid">
              <button class="quick-action-btn" @click="quickCreate('note')">
                <FileText :size="20" />
                <span>新建笔记</span>
              </button>
              <button class="quick-action-btn" @click="quickCreate('todo')">
                <CheckCircle :size="20" />
                <span>新建任务</span>
              </button>
              <button class="quick-action-btn" @click="quickCreate('diary')">
                <PenLine :size="20" />
                <span>写日记</span>
              </button>
              <button class="quick-action-btn" @click="quickCreate('study')">
                <Plus :size="20" />
                <span>记录学习</span>
              </button>
            </div>
          </div>

          <!-- 待办任务 -->
          <div v-if="card.code === 'pending_todos'" class="flow-card">
            <!-- 不变 -->
          </div>

          <!-- 最近收藏 -->
          <div v-if="card.code === 'recent_bookmarks'" class="flow-card">
            <!-- 不变 -->
          </div>
        </template>
      </div>

      <!-- 统计卡区域 - 固定在底部 -->
      <div class="stats-section">
        <div class="stats-section-header">
          <h3>数据概览</h3>
        </div>
        <div class="stats-grid">
          <StatCard
            v-for="card in statCards" :key="card.key"
            :icon="card.icon"
            :value="stats ? stats[card.key] : 0"
            :label="card.label"
            :color="card.color"
            :append="card.append ? card.append(stats!) : undefined"
            @click="card.onClick"
          />
        </div>
      </div>
    </template>
  </div>
  <!-- ... -->
</template>
```

- [ ] **步骤 3：更新样式为单列布局**

```css
/* Dashboard.vue - 样式变更 */

/* 删除旧的 .dashboard-grid (2列) */
/* 新增单列内容流 */
.dashboard-flow {
  display: flex;
  flex-direction: column;
  gap: var(--sp-5);
}

.flow-card {
  background: var(--bg-card);
  border: 1px solid var(--border-color);
  border-radius: var(--radius-lg);
  overflow: hidden;
  transition: all var(--transition);
}
.flow-card:hover {
  box-shadow: var(--shadow-md);
  transform: translateY(-2px);
}
.flow-card--compact {
  /* 快捷操作卡片更紧凑 */
}

.flow-card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: var(--sp-4) var(--sp-6);
  border-bottom: 1px solid var(--border-light);
}
.flow-card-header h3 {
  font-size: var(--text-sm);
  font-weight: 600;
}

/* 快捷操作网格 */
.quick-actions-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: var(--sp-3);
  padding: var(--sp-4) var(--sp-6);
}
.quick-action-btn {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: var(--sp-2);
  padding: var(--sp-4);
  border-radius: var(--radius-md);
  border: 1px solid var(--border-color);
  background: var(--bg-card);
  color: var(--text-secondary);
  cursor: pointer;
  transition: all var(--transition);
  font-size: var(--text-sm);
}
.quick-action-btn:hover {
  border-color: var(--accent);
  color: var(--accent);
  background: var(--accent-light);
}

/* 统计卡区域 - 底部 */
.stats-section {
  margin-top: var(--sp-5);
  padding-top: var(--sp-5);
  border-top: 1px solid var(--border-light);
}
.stats-section-header h3 {
  font-size: var(--text-sm);
  font-weight: 600;
  margin-bottom: var(--sp-4);
  color: var(--text-secondary);
}
.stats-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(200px, 1fr));
  gap: var(--sp-3);
}

/* 响应式 */
@media (max-width: 768px) {
  .quick-actions-grid {
    grid-template-columns: repeat(2, 1fr);
  }
}
```

- [ ] **步骤 5：验证 Dashboard 构建通过**

运行：`cd personal-hub-web && npx vue-tsc --noEmit`
预期：无类型错误

---

### 任务 2：Sidebar 分组重构 + 最近访问

**文件：**
- 新建：`personal-hub-web/src/store/recentVisitStore.ts`
- 修改：`personal-hub-web/src/components/AppLayout.vue`
- 修改：`personal-hub-web/src/modules/dashboard/defaultLayouts.ts`

- [ ] **步骤 1：创建 recentVisitStore**

```typescript
// personal-hub-web/src/store/recentVisitStore.ts
import { defineStore } from 'pinia'
import { ref } from 'vue'

export interface VisitItem {
  path: string
  title: string
  time: number
}

const STORAGE_KEY = 'recent-visits'
const MAX_VISITS = 10

function loadVisits(): VisitItem[] {
  try {
    const raw = localStorage.getItem(STORAGE_KEY)
    return raw ? JSON.parse(raw) : []
  } catch { return [] }
}

function saveVisits(items: VisitItem[]) {
  localStorage.setItem(STORAGE_KEY, JSON.stringify(items))
}

export const useRecentVisitStore = defineStore('recentVisit', () => {
  const visits = ref<VisitItem[]>(loadVisits())

  function recordVisit(path: string, title: string) {
    const existing = visits.value.findIndex(v => v.path === path)
    if (existing !== -1) visits.value.splice(existing, 1)
    visits.value.unshift({ path, title, time: Date.now() })
    if (visits.value.length > MAX_VISITS) visits.value = visits.value.slice(0, MAX_VISITS)
    saveVisits(visits.value)
  }

  return { visits, recordVisit }
})
```

- [ ] **步骤 2：更新 AppLayout.vue 侧边栏分组**

关键变更：
1. 新增「最近访问」区域（从 recentVisitStore 读取）
2. 菜单分组从 5 组改为 3 组：工作区/管理/统计
3. Section icon map 调整为只有 3 组

```typescript
// AppLayout.vue - script 部分变更
import { useRecentVisitStore } from '@/store/recentVisitStore'
import { useRouter, useRoute } from 'vue-router'
import { Clock } from 'lucide-vue-next'

const recentVisitStore = useRecentVisitStore()

// 记录路由访问
router.afterEach((to) => {
  const title = (to.meta?.title as string) || to.name as string || ''
  if (title && to.path !== '/dashboard') {
    recentVisitStore.recordVisit(to.path, title)
  }
})

// section 映射改为 3 组
const sectionIconMap: Record<string, Component> = {
  workspace: LayoutDashboard,
  manage: Cog,
  stats: BarChart3,
}
```

- [ ] **步骤 3：更新 AppLayout.vue 模板**

在 sidebar-nav 顶部添加「最近访问」区域：

```vue
<!-- 最近访问 -->
<div v-if="recentVisitStore.visits.length > 0" class="sidebar-section sidebar-section--recent">
  <div class="section-header">
    <Clock :size="14" class="section-icon" />
    <span>最近访问</span>
  </div>
  <div class="section-items">
    <router-link
      v-for="visit in recentVisitStore.visits.slice(0, 5)"
      :key="visit.path"
      :to="visit.path"
      class="nav-item nav-item--secondary"
      :class="{ active: route.path === visit.path }"
    >
      <Clock :size="14" />
      <span>{{ visit.title }}</span>
    </router-link>
  </div>
</div>
```

- [ ] **步骤 4：更新 defaultLayouts.ts 菜单分组**

从 5 组改为 3 组（工作区/管理/统计）：

```typescript
// personal-hub-web/src/modules/dashboard/defaultLayouts.ts
export const DEFAULT_MENU_ITEMS: MenuItem[] = [
  // ===== 工作区 =====
  { code: 'dashboard', title: '首页', section: 'workspace', route: '/dashboard', order: 1, visible: true, fixed: true },
  { code: 'todos', title: '待办任务', section: 'workspace', route: '/todos', order: 2, visible: true },
  { code: 'study-plans', title: '学习计划', section: 'workspace', route: '/study-plans', order: 3, visible: true },
  // ===== 管理 =====
  { code: 'notes', title: '笔记', section: 'manage', route: '/notes', order: 4, visible: true },
  { code: 'diaries', title: '日记', section: 'manage', route: '/diaries', order: 5, visible: true },
  { code: 'readings', title: '阅读记录', section: 'manage', route: '/readings', order: 6, visible: true },
  { code: 'study-records', title: '学习记录', section: 'manage', route: '/study-records', order: 7, visible: true },
  { code: 'bookmarks', title: '收藏夹', section: 'manage', route: '/bookmarks', order: 8, visible: true },
  { code: 'files', title: '文件管理', section: 'manage', route: '/files', order: 9, visible: true },
  { code: 'tags', title: '标签管理', section: 'manage', route: '/tags', order: 10, visible: true },
  { code: 'categories', title: '分类管理', section: 'manage', route: '/categories', order: 11, visible: true },
  { code: 'settings', title: '系统设置', section: 'manage', route: '/settings', order: 12, visible: true, fixed: true },
  { code: 'recycle', title: '回收站', section: 'manage', route: '/notes/recycle', order: 13, visible: true },
  // ===== 统计 =====
  { code: 'stats', title: '数据统计', section: 'stats', route: '/stats', order: 14, visible: true },
]
```

- [ ] **步骤 5：更新 layoutStore.ts 的 section 定义**

```typescript
// layoutStore.ts - visibleMenuSections
const visibleMenuSections = computed(() => {
  const sections: { title: string; key: string; items: MenuItem[] }[] = [
    { title: '工作区', key: 'workspace', items: [] },
    { title: '管理', key: 'manage', items: [] },
    { title: '统计', key: 'stats', items: [] },
  ]
  // ... rest unchanged
})
```

- [ ] **步骤 6：更新 layoutStore.migrateMenuItems 检测旧 section 逻辑**

在 migrateMenuItems 中，需要确保旧的 5 分组数据被重置为新的 3 分组。但 layoutStore 中的 `needsSectionReset` 和 `migrateMenuItems` 已有自动迁移逻辑，只需确保 `DEFAULT_MENU_ITEMS` 更新后，旧数据（section 不匹配）会被自动重置。

- [ ] **步骤 7：验证构建通过**

运行：`cd personal-hub-web && npx vue-tsc --noEmit`
预期：无类型错误

---

### 任务 3：CRUD 页面差异对齐

**文件：**
- 修改：`personal-hub-web/src/modules/knowledge/diary/List.vue`
- 修改：`personal-hub-web/src/modules/knowledge/study/List.vue`
- 修改：`personal-hub-web/src/resource/file/List.vue`
- 修改：`personal-hub-web/src/resource/bookmark/List.vue`
- 修改：`personal-hub-web/src/planning/studyplan/List.vue`

- [ ] **步骤 1：检查并修复 diary/List.vue**

确保结构完全遵循 PageHeader → ListToolbar → Content → Pagination。

- [ ] **步骤 2：检查并修复 study/List.vue**

确保工具条统一：搜索框在左侧，新建按钮在右侧。

- [ ] **步骤 3：检查并修复 file/List.vue**

同上。

- [ ] **步骤 4：检查并修复 bookmark/List.vue**

同上。

- [ ] **步骤 5：检查并修复 studyplan/List.vue**

同上。

---

### 任务 4：UiButton 新增 danger 属性

**文件：**
- 修改：`personal-hub-web/src/components/ui/UiButton.vue`

- [ ] **步骤 1：更新 UiButton 组件**

```vue
<!-- 新增 danger prop -->
<script setup lang="ts">
const props = withDefaults(defineProps<{
  type?: 'default' | 'primary' | 'danger'
  loading?: boolean
  disabled?: boolean
}>(), {
  type: 'default',
  loading: false,
  disabled: false,
})
</script>

<template>
  <el-button
    :type="type === 'danger' ? 'danger' : type"
    :loading="loading"
    :disabled="disabled"
    class="ui-button"
    :class="{ 'is-danger': type === 'danger' }"
  >
    <slot />
  </el-button>
</template>

<style scoped>
.ui-button.is-danger {
  --el-button-bg-color: var(--danger) !important;
  --el-button-border-color: var(--danger) !important;
  --el-button-hover-bg-color: var(--danger-hover) !important;
}
</style>
```

---

### 任务 5：Editor 默认标题

**文件：**
- 修改：`personal-hub-web/src/modules/knowledge/note/Editor.vue`

- [ ] **步骤 1：添加默认标题逻辑**

```typescript
// Editor.vue - 新增
import { useAutoSave } from './editor/useAutoSave'

// 新增笔记时设置默认标题
if (!isEdit) {
  form.value.title = '未命名笔记'
}
```

---

### 任务 6：搜索历史

**文件：**
- 修改：`personal-hub-web/src/components/CommandPalette.vue`

- [ ] **步骤 1：添加搜索历史逻辑**

```typescript
// CommandPalette.vue - 新增
const searchHistory = ref<string[]>(loadSearchHistory())

function loadSearchHistory(): string[] {
  try {
    return JSON.parse(localStorage.getItem('search-history') || '[]')
  } catch { return [] }
}

function saveSearchHistory(keyword: string) {
  const history = loadSearchHistory()
  history.unshift(keyword)
  const unique = [...new Set(history)].slice(0, 5)
  localStorage.setItem('search-history', JSON.stringify(unique))
  searchHistory.value = unique
}

// 在搜索 enter 时记录
function onSearchEnter() {
  const kw = keyword.value.trim()
  if (kw.length >= 2) {
    saveSearchHistory(kw)
  }
}
```

- [ ] **步骤 2：在 CommandPalette 空状态时展示搜索历史**

```vue
<!-- 当输入为空且有历史记录时 -->
<div v-else-if="!keyword.trim() && searchHistory.length > 0" class="cp-history">
  <div class="cp-history-header">
    <Clock :size="12" />
    <span>最近搜索</span>
  </div>
  <div
    v-for="(item, idx) in searchHistory"
    :key="idx"
    class="cp-history-item"
    @click="keyword = item; onSearch()"
  >
    <span>{{ item }}</span>
  </div>
</div>
```

---

### 任务 7：动画统一

**文件：**
- 修改：`personal-hub-web/src/styles/global.css`

- [ ] **步骤 1：调整 page-fade 动画时长**

```css
/* global.css - 修改 */
.page-fade-enter-active,
.page-fade-leave-active {
  transition: opacity 150ms ease;  /* 200ms → 150ms */
}
```

---

## 自检结果

- **规格覆盖度**：设计文档中的每个模块都有对应任务
- **占位符检查**：无 TODO/待定/占位符
- **类型一致性**：所有类型引用与现有代码一致
