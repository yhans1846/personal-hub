<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { getTags, createTag, updateTag, deleteTag } from '@/api/tagApi'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Tags, Plus, Pencil, Trash2, Search, ArrowUpDown } from 'lucide-vue-next'
import type { TagVO } from '@/types/tag'
import PageHeader from '@/components/PageHeader.vue'
import EmptyState from '@/components/EmptyState.vue'
import { UiDialog, UiInput } from '@/components/ui'

// ============ 状态 ============
const list = ref<TagVO[]>([])
const dialogVisible = ref(false)
const isEdit = ref(false)
const form = ref({ id: 0, name: '', color: '#409eff' })
const searchQuery = ref('')
const colorFilter = ref<string>('')
const sortBy = ref<'name' | 'createdAt' | 'usageCount'>('usageCount')
const sortDir = ref<'asc' | 'desc'>('desc')

const COLORS = [
  '#409eff', '#67c23a', '#e6a23c', '#f56c6c',
  '#909399', '#9b59b6', '#2ecc71', '#3498db',
  '#e74c3c', '#f39c12', '#1abc9c', '#34495e',
]

// ============ 计算属性 ============
const stats = computed(() => {
  const total = list.value.length
  const totalUsage = list.value.reduce((acc, t) => acc + (t.usageCount ?? 0), 0)
  const sevenDaysAgo = new Date(Date.now() - 7 * 24 * 60 * 60 * 1000).getTime()
  const recentAdded = list.value.filter(t => {
    if (!t.createdAt) return false
    const tTime = new Date(t.createdAt).getTime()
    return tTime >= sevenDaysAgo
  }).length
  return { total, totalUsage, recentAdded }
})

const hotTags = computed(() => {
  return [...list.value]
    .sort((a, b) => (b.usageCount ?? 0) - (a.usageCount ?? 0))
    .slice(0, 5)
    .filter(t => (t.usageCount ?? 0) > 0)
})

const uniqueColors = computed(() => {
  const colors = new Set(list.value.map(t => t.color || '#409eff'))
  return Array.from(colors).sort()
})

// 颜色名称映射
const colorNames: Record<string, string> = {
  '#409eff': '蓝色', '#67c23a': '绿色', '#e6a23c': '橙色', '#f56c6c': '红色',
  '#909399': '灰色', '#9b59b6': '紫色', '#2ecc71': '翠绿', '#3498db': '天蓝',
  '#e74c3c': '深红', '#f39c12': '金黄', '#1abc9c': '青色', '#34495e': '深灰',
}

const filteredList = computed(() => {
  let result = [...list.value]

  // 搜索
  if (searchQuery.value.trim()) {
    const q = searchQuery.value.trim().toLowerCase()
    result = result.filter(t => t.name.toLowerCase().includes(q))
  }

  // 颜色筛选
  if (colorFilter.value) {
    result = result.filter(t => (t.color || '#409eff') === colorFilter.value)
  }

  // 排序
  result.sort((a, b) => {
    let cmp = 0
    if (sortBy.value === 'name') cmp = a.name.localeCompare(b.name)
    else if (sortBy.value === 'createdAt') cmp = (a.createdAt || '').localeCompare(b.createdAt || '')
    else if (sortBy.value === 'usageCount') cmp = (a.usageCount ?? 0) - (b.usageCount ?? 0)
    return sortDir.value === 'asc' ? cmp : -cmp
  })

  return result
})

// ============ 生命周期 ============
onMounted(() => fetchList())

// ============ 方法 ============
async function fetchList() {
  const res = await getTags()
  list.value = res.data.data || []
}

function openCreate() {
  isEdit.value = false
  form.value = { id: 0, name: '', color: '#409eff' }
  dialogVisible.value = true
}

function openEdit(row: TagVO) {
  isEdit.value = true
  form.value = { id: row.id, name: row.name, color: row.color || '#409eff' }
  dialogVisible.value = true
}

async function handleSave() {
  if (!form.value.name.trim()) { ElMessage.warning('请输入名称'); return }
  try {
    if (isEdit.value) {
      await updateTag(form.value.id, { name: form.value.name, color: form.value.color })
      ElMessage.success('已更新')
    } else {
      await createTag({ name: form.value.name, color: form.value.color })
      ElMessage.success('已创建')
    }
    dialogVisible.value = false
    fetchList()
  } catch (e: any) {
    ElMessage.error(e.response?.data?.message || '操作失败')
  }
}

async function handleDelete(id: number) {
  await ElMessageBox.confirm('确定删除该标签？删除后所有关联将被清除。', '提示', { type: 'warning' })
  try {
    await deleteTag(id)
    ElMessage.success('已删除')
    fetchList()
  } catch (e: any) {
    ElMessage.error(e.response?.data?.message || '删除失败')
  }
}

function onColorInput(e: Event) {
  form.value.color = (e.target as HTMLInputElement).value
}

function toggleSortDir() {
  sortDir.value = sortDir.value === 'asc' ? 'desc' : 'asc'
}

function formatDate(dateStr?: string) {
  if (!dateStr) return ''
  return dateStr.slice(0, 10)
}
</script>

<template>
  <div class="tag-manage">
    <!-- ========== Header ========== -->
    <PageHeader title="标签管理" subtitle="管理所有模块的标签，支持颜色标记和使用统计">
      <span class="header-count">{{ stats.total }} 个标签 · 引用 {{ stats.totalUsage }} 次 · 本周新增 {{ stats.recentAdded }}</span>
    </PageHeader>

    <!-- ========== 统计卡片 ========== -->
    <div class="stats-row">
      <div class="stat-card stat-card--total">
        <span class="stat-value">{{ stats.total }}</span>
        <span class="stat-label">标签总数</span>
      </div>
      <div class="stat-card stat-card--usage">
        <span class="stat-value">{{ stats.totalUsage }}</span>
        <span class="stat-label">引用总次数</span>
      </div>
      <div class="stat-card stat-card--recent">
        <span class="stat-value">+{{ stats.recentAdded }}</span>
        <span class="stat-label">本周新增</span>
      </div>
    </div>

    <!-- ========== 热门标签 ========== -->
    <div v-if="hotTags.length > 0" class="hot-tags-bar">
      <span class="hot-tags-label">🔥 热门</span>
      <div class="hot-tags-list">
        <span
          v-for="tag in hotTags"
          :key="tag.id"
          class="hot-tag-item"
        >
          <span class="hot-tag-dot" :style="{ background: tag.color || '#409eff' }" />
          {{ tag.name }}
          <span class="hot-tag-count">{{ tag.usageCount }}</span>
        </span>
      </div>
    </div>

    <!-- ========== 工具栏 ========== -->
    <div class="toolbar">
      <div class="toolbar-left">
        <div class="search-box">
          <Search :size="16" class="search-icon" />
          <input
            v-model="searchQuery"
            type="text"
            class="search-input"
            placeholder="搜索标签..."
          />
          <button v-if="searchQuery" class="search-clear" @click="searchQuery = ''">✕</button>
        </div>

        <select v-model="colorFilter" class="filter-select">
          <option value="">全部颜色</option>
          <option v-for="c in uniqueColors" :key="c" :value="c">
            ● {{ colorNames[c] || c }}
          </option>
        </select>

        <div class="sort-group">
          <select v-model="sortBy" class="sort-select">
            <option value="usageCount">使用次数</option>
            <option value="name">名称</option>
            <option value="createdAt">创建时间</option>
          </select>
          <button class="sort-dir-btn" @click="toggleSortDir" :title="sortDir === 'asc' ? '正序' : '倒序'">
            <ArrowUpDown :size="15" class="sort-icon" :class="{ reversed: sortDir === 'desc' }" />
          </button>
        </div>
      </div>
      <el-button type="primary" size="default" @click="openCreate">
        <Plus :size="16" /> 新建标签
      </el-button>
    </div>

    <!-- ========== 内容区 ========== -->
    <EmptyState
      v-if="list.length === 0"
      :icon="Tags"
      illustration="default"
      text="暂无标签"
      action-label="新建标签"
      @action="openCreate"
    />

    <EmptyState
      v-else-if="filteredList.length === 0 && (searchQuery || colorFilter)"
      :icon="Search"
      text="没有匹配的标签"
    />

    <div v-else class="tag-grid">
      <div v-for="item in filteredList" :key="item.id" class="tag-card">
        <!-- 颜色标记 -->
        <div class="tag-color-strip" :style="{ background: item.color || '#409eff' }" />

        <div class="tag-card-body">
          <!-- 第一行：名称 + 使用次数 -->
          <div class="tag-card-top">
            <span class="tag-card-name">{{ item.name }}</span>
            <span class="tag-card-badge" :class="{ 'badge-zero': !item.usageCount }">
              {{ item.usageCount ?? 0 }}
              <span class="badge-unit">篇</span>
            </span>
          </div>
          <!-- 第二行：创建时间 -->
          <span class="tag-card-date" v-if="item.createdAt">
            {{ formatDate(item.createdAt) }}
          </span>
        </div>

        <!-- 操作 -->
        <div class="tag-card-actions">
          <button class="icon-btn" title="编辑" @click="openEdit(item)"><Pencil :size="14" /></button>
          <button class="icon-btn icon-btn--danger" title="删除" @click="handleDelete(item.id)"><Trash2 :size="14" /></button>
        </div>
      </div>
    </div>

    <!-- ========== 编辑弹窗 ========== -->
    <UiDialog v-model="dialogVisible" :title="isEdit ? '编辑标签' : '新建标签'">
      <UiInput v-model="form.name" placeholder="标签名称" maxlength="50" show-word-limit class="tag-dialog-title" />

      <div class="tag-dialog-section-label">颜色</div>
      <div class="color-picker">
        <button
          v-for="c in COLORS" :key="c"
          type="button"
          class="color-btn"
          :class="{ active: form.color === c }"
          :style="{ background: c }"
          @click="form.color = c"
        />
        <label
          class="color-btn color-btn--custom"
          :style="{ background: form.color }"
          title="自定义颜色"
        >
          自定义
          <input
            type="color"
            class="color-input-abs"
            :value="form.color"
            @input="onColorInput"
          />
        </label>
      </div>

      <template #footer>
        <el-button text @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSave">保存</el-button>
      </template>
    </UiDialog>
  </div>
</template>

<style scoped>
.tag-manage {
  max-width: 1100px;
}

/* ---- Header ---- */
.header-count {
  font-size: var(--text-sm);
  color: var(--text-tertiary);
  margin-top: 2px;
}

/* ---- 统计卡片 ---- */
.stats-row {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: var(--sp-3);
  margin-bottom: var(--sp-4);
}

.stat-card {
  display: flex;
  flex-direction: column;
  gap: 4px;
  padding: var(--sp-4);
  border-radius: var(--radius-lg);
  border: 1px solid var(--border-color);
  background: var(--bg-card);
  transition: all var(--transition);
}
.stat-card:hover { box-shadow: var(--shadow-sm); }
.stat-value {
  font-size: 24px;
  font-weight: 700;
  line-height: 1.2;
}
.stat-label {
  font-size: var(--text-xs);
  color: var(--text-tertiary);
  font-weight: 500;
}
.stat-card--total .stat-value { color: var(--accent); }
.stat-card--usage .stat-value { color: var(--success); }
.stat-card--recent .stat-value { color: var(--warning); }

/* ---- 热门标签条 ---- */
.hot-tags-bar {
  display: flex;
  align-items: center;
  gap: var(--sp-3);
  margin-bottom: var(--sp-4);
  padding: var(--sp-3) var(--sp-4);
  background: var(--bg-card);
  border: 1px solid var(--border-color);
  border-radius: var(--radius-lg);
  flex-wrap: wrap;
}
.hot-tags-label {
  font-size: var(--text-sm);
  font-weight: 600;
  color: var(--warning);
  white-space: nowrap;
}
.hot-tags-list {
  display: flex;
  gap: var(--sp-2);
  flex-wrap: wrap;
}
.hot-tag-item {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  padding: 3px 10px;
  border-radius: 100px;
  font-size: var(--text-xs);
  font-weight: 500;
  color: var(--text-secondary);
  background: var(--bg-hover);
  border: 1px solid var(--border-light);
}
.hot-tag-dot {
  width: 7px;
  height: 7px;
  border-radius: 50%;
  flex-shrink: 0;
}
.hot-tag-count {
  font-size: 10px;
  font-weight: 600;
  color: var(--text-tertiary);
  background: var(--bg-card);
  padding: 0 5px;
  border-radius: 4px;
  margin-left: 2px;
}

/* ---- 工具栏 ---- */
.toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: var(--sp-3);
  margin-bottom: var(--sp-5);
  flex-wrap: wrap;
}

.toolbar-left {
  display: flex;
  align-items: center;
  gap: var(--sp-3);
  flex: 1;
  flex-wrap: wrap;
}

.search-box {
  position: relative;
  display: flex;
  align-items: center;
  flex: 1;
  min-width: 160px;
  max-width: 260px;
}
.search-icon {
  position: absolute;
  left: 10px;
  color: var(--text-tertiary);
  pointer-events: none;
}
.search-input {
  width: 100%;
  padding: 8px 32px 8px 34px;
  border: 1px solid var(--border-color);
  border-radius: var(--radius-md);
  background: var(--bg-card);
  font-size: var(--text-sm);
  color: var(--text-primary);
  outline: none;
  transition: border-color var(--transition);
}
.search-input:focus { border-color: var(--accent); }
.search-input::placeholder { color: var(--text-placeholder); }
.search-clear {
  position: absolute;
  right: 6px;
  background: none;
  border: none;
  cursor: pointer;
  color: var(--text-tertiary);
  font-size: 13px;
  padding: 2px 6px;
  border-radius: 4px;
}
.search-clear:hover { color: var(--text-primary); background: var(--bg-hover); }

.filter-select,
.sort-select {
  padding: 7px 10px;
  border: 1px solid var(--border-color);
  border-radius: var(--radius-md);
  background: var(--bg-card);
  font-size: var(--text-sm);
  color: var(--text-secondary);
  outline: none;
  cursor: pointer;
  min-width: 90px;
}
.filter-select:focus,
.sort-select:focus { border-color: var(--accent); }

.sort-group {
  display: flex;
  align-items: center;
  gap: 4px;
}
.sort-dir-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 32px;
  height: 32px;
  border: 1px solid var(--border-color);
  border-radius: var(--radius-md);
  background: var(--bg-card);
  cursor: pointer;
  color: var(--text-tertiary);
  transition: all var(--transition);
}
.sort-dir-btn:hover { border-color: var(--accent); color: var(--accent); }
.sort-icon { transition: transform var(--transition); }
.sort-icon.reversed { transform: rotate(180deg); }

/* ---- Grid 卡片布局 ---- */
.tag-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(240px, 1fr));
  gap: var(--sp-3);
}

.tag-card {
  display: flex;
  align-items: center;
  gap: var(--sp-3);
  padding: 0;
  background: var(--bg-card);
  border: 1px solid var(--border-color);
  border-radius: var(--radius-lg);
  transition: all 0.2s ease;
  cursor: default;
  overflow: hidden;
}
.tag-card:hover {
  box-shadow: var(--shadow-md);
  border-color: var(--accent-border);
  transform: translateY(-2px);
}

.tag-color-strip {
  width: 5px;
  align-self: stretch;
  flex-shrink: 0;
}

.tag-card-body {
  flex: 1;
  min-width: 0;
  padding: var(--sp-3) 0;
  display: flex;
  flex-direction: column;
  gap: 3px;
}

.tag-card-top {
  display: flex;
  align-items: center;
  gap: var(--sp-2);
}

.tag-card-name {
  font-size: var(--text-sm);
  font-weight: 600;
  color: var(--text-primary);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.tag-card-badge {
  display: inline-flex;
  align-items: center;
  gap: 1px;
  font-size: var(--text-xs);
  font-weight: 600;
  color: var(--accent);
  background: var(--accent-light);
  padding: 1px 9px 1px 7px;
  border-radius: 100px;
  white-space: nowrap;
  flex-shrink: 0;
}
.tag-card-badge.badge-zero {
  color: var(--text-tertiary);
  background: var(--bg-hover);
}
.badge-unit {
  font-weight: 400;
  font-size: 10px;
  opacity: 0.7;
}

.tag-card-date {
  font-size: var(--text-xs);
  color: var(--text-tertiary);
}

.tag-card-actions {
  display: flex;
  gap: 1px;
  padding-right: var(--sp-2);
  opacity: 0;
  transition: opacity var(--transition);
  flex-shrink: 0;
}
.tag-card:hover .tag-card-actions { opacity: 1; }

.icon-btn {
  background: none;
  border: none;
  cursor: pointer;
  padding: 6px;
  border-radius: var(--radius-sm);
  color: var(--text-tertiary);
  transition: all var(--transition);
  display: flex;
  align-items: center;
}
.icon-btn:hover { color: var(--accent); background: var(--accent-light); }
.icon-btn--danger:hover { color: var(--danger); background: var(--danger-light); }

/* ---- Dialog ---- */
.tag-dialog-title {
  margin-bottom: var(--sp-5);
}
.tag-dialog-title :deep(input) {
  font-size: var(--text-lg) !important;
  font-weight: 600;
  border: none !important;
  padding-left: 0 !important;
  background: transparent !important;
}
.tag-dialog-title :deep(input)::placeholder {
  color: var(--text-placeholder);
  font-weight: 400;
}

.tag-dialog-section-label {
  font-size: var(--text-xs);
  color: var(--text-tertiary);
  font-weight: 500;
  margin-bottom: var(--sp-3);
  letter-spacing: 0.3px;
  text-transform: uppercase;
}

.color-picker { display: flex; gap: 8px; flex-wrap: wrap; }
.color-btn {
  width: 28px; height: 28px; border-radius: 50%; border: 2px solid transparent;
  cursor: pointer; transition: all var(--transition);
}
.color-btn:hover { transform: scale(1.15); }
.color-btn.active { border-color: var(--text-primary); transform: scale(1.15); }
.color-btn--custom {
  position: relative; width: auto; padding: 0 10px; border-radius: 6px;
  font-size: 11px; color: #fff; font-weight: 500; line-height: 28px;
  border: 1px solid rgba(255,255,255,.25); letter-spacing: .5px;
  cursor: pointer; transition: all var(--transition);
}
.color-btn--custom:hover { transform: none; filter: brightness(1.15); }
.color-btn--custom.active { border-color: var(--text-primary); }
.color-input-abs {
  position: absolute; inset: 0; opacity: 0; cursor: pointer;
  width: 100%; height: 100%; border: none;
}
</style>
