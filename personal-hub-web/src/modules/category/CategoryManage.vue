<script setup lang="ts">
import { ref, onMounted, onUnmounted, computed, watch, nextTick } from 'vue'
import { getCategories, createCategory, updateCategory, deleteCategory, batchUpdateCategorySort } from '@/api/categoryApi'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Pencil, Trash2, FolderOpen, Folder, Bookmark, GripVertical, Search, ArrowUpDown } from 'lucide-vue-next'
import Sortable from 'sortablejs'
import { PageHeader, EmptyState, ListToolbar } from '@/components'
import { UiDialog, UiInput, UiButton } from '@/components/ui'
import type { CategoryVO } from '@/types/category'
import CategoryStatsCards from './CategoryStatsCards.vue'

// ============ 状态 ============
const activeType = ref<'note' | 'bookmark' | 'file'>('note')
const list = ref<CategoryVO[]>([])
const loading = ref(false)
const searchQuery = ref('')
const sortBy = ref<'name' | 'createdAt' | 'count'>('createdAt')
const sortDir = ref<'asc' | 'desc'>('asc')
const dialogVisible = ref(false)
const isEdit = ref(false)
const form = ref({ id: 0, name: '' })
const gridRef = ref<HTMLElement | null>(null)
let sortableInstance: Sortable | null = null

const tabs = [
  { type: 'note' as const, label: '笔记', icon: FolderOpen },
  { type: 'bookmark' as const, label: '收藏', icon: Bookmark },
  { type: 'file' as const, label: '文件', icon: Folder }
]

// ============ 计算属性 ============
const stats = computed(() => {
  const total = list.value.length
  const inUse = list.value.filter(c => (c.count ?? 0) > 0).length
  const unused = total - inUse
  return { total, inUse, unused }
})

const filteredList = computed(() => {
  let result = [...list.value]

  // 搜索过滤
  if (searchQuery.value.trim()) {
    const q = searchQuery.value.trim().toLowerCase()
    result = result.filter(c => c.name.toLowerCase().includes(q))
  }

  // 排序
  result.sort((a, b) => {
    let cmp = 0
    if (sortBy.value === 'name') cmp = a.name.localeCompare(b.name)
    else if (sortBy.value === 'createdAt') cmp = (a.createdAt || '').localeCompare(b.createdAt || '')
    else if (sortBy.value === 'count') cmp = (a.count ?? 0) - (b.count ?? 0)
    return sortDir.value === 'asc' ? cmp : -cmp
  })

  return result
})

// ============ 生命周期 ============
watch(() => activeType.value, () => { fetchList(); destroySortable() })
onMounted(() => fetchList())
onUnmounted(() => destroySortable())

// ============ 方法 ============
async function fetchList() {
  loading.value = true
  try {
    const res = await getCategories(activeType.value)
    list.value = res.data.data || []
    await nextTick()
    initSortable()
  } finally { loading.value = false }
}

function initSortable() {
  destroySortable()
  if (!gridRef.value) return
  sortableInstance = new Sortable(gridRef.value, {
    handle: '.drag-handle',
    animation: 200,
    easing: 'cubic-bezier(0.25, 0.46, 0.45, 0.94)',
    ghostClass: 'sortable-ghost',
    dragClass: 'sortable-drag',
    onEnd: handleSortEnd
  })
}

function destroySortable() {
  if (sortableInstance) {
    sortableInstance.destroy()
    sortableInstance = null
  }
}

async function handleSortEnd() {
  if (!gridRef.value) return
  const items = [...gridRef.value.querySelectorAll('.category-card')] as HTMLElement[]
  const idMap = new Map(list.value.map(c => [c.id, c]))
  const sorted: { id: number; sortOrder: number }[] = []

  items.forEach((el, idx) => {
    const id = Number(el.dataset.id)
    const cat = idMap.get(id)
    if (cat) {
      cat.sortOrder = idx + 1
      sorted.push({ id, sortOrder: idx + 1 })
    }
  })

  // 重新排序 list
  list.value.sort((a, b) => a.sortOrder - b.sortOrder)

  try {
    await batchUpdateCategorySort(sorted)
  } catch {
    ElMessage.error('保存排序失败')
    fetchList()
  }
}

function openCreate() {
  isEdit.value = false
  form.value = { id: 0, name: '' }
  dialogVisible.value = true
}

function openEdit(item: CategoryVO) {
  isEdit.value = true
  form.value = { id: item.id, name: item.name }
  dialogVisible.value = true
}

async function handleSave() {
  if (!form.value.name.trim()) { ElMessage.warning('请输入名称'); return }
  try {
    if (isEdit.value) {
      await updateCategory(form.value.id, { name: form.value.name.trim() })
      ElMessage.success('已更新')
    } else {
      await createCategory({ name: form.value.name.trim(), type: activeType.value })
      ElMessage.success('已创建')
    }
    dialogVisible.value = false
    fetchList()
  } catch (e: any) {
    ElMessage.error(e.response?.data?.message || '操作失败')
  }
}

async function handleDelete(id: number) {
  await ElMessageBox.confirm('确定删除该分类？', '提示', { type: 'warning' })
  try {
    await deleteCategory(id)
    ElMessage.success('已删除')
    fetchList()
  } catch (e: any) {
    ElMessage.error(e.response?.data?.message || '删除失败')
  }
}

function toggleSortDir() {
  sortDir.value = sortDir.value === 'asc' ? 'desc' : 'asc'
}

function formatDate(dateStr?: string) {
  if (!dateStr) return ''
  return dateStr.slice(0, 10)
}

function getTypeIcon(type: string) {
  const tab = tabs.find(t => t.type === type)
  return tab?.icon || FolderOpen
}
</script>

<template>
  <div class="category-manage">
    <!-- ========== Header ========== -->
    <PageHeader title="分类管理" />

    <!-- ========== 统计卡片 ========== -->
    <CategoryStatsCards :total="stats.total" :in-use="stats.inUse" :unused="stats.unused" />

    <!-- ========== 模块切换 (Segment) ========== -->
    <div class="segment-group">
      <button
        v-for="tab in tabs"
        :key="tab.type"
        class="segment-btn"
        :class="{ active: activeType === tab.type }"
        @click="activeType = tab.type"
      >
        <component :is="tab.icon" :size="16" />
        {{ tab.label }}
        <span class="segment-count">{{ activeType === tab.type ? list.length : '—' }}</span>
      </button>
    </div>

    <!-- ========== 工具栏 ========== -->
    <ListToolbar
      :search="searchQuery"
      search-placeholder="搜索分类..."
      search-width="280px"
      create-label="新建分类"
      @update:search="searchQuery = $event"
      @create="openCreate"
    >
      <template #filters>
        <el-select v-model="sortBy" style="width: 120px">
          <el-option value="createdAt" label="创建时间" />
          <el-option value="name" label="名称" />
          <el-option value="count" label="使用次数" />
        </el-select>
        <button class="sort-dir-btn" @click="toggleSortDir" :title="sortDir === 'asc' ? '正序' : '倒序'">
          <ArrowUpDown :size="15" class="sort-icon" :class="{ reversed: sortDir === 'desc' }" />
        </button>
      </template>
    </ListToolbar>

    <!-- ========== 内容区 ========== -->
    <div v-if="loading" class="grid-skeleton">
      <div v-for="i in 4" :key="i" class="skeleton-card" />
    </div>

    <EmptyState
      v-else-if="list.length === 0"
      :icon="getTypeIcon(activeType)"
      illustration="default"
      text="暂无分类，创建一个吧"
      action-label="新建分类"
      @action="openCreate"
    />

    <EmptyState
      v-else-if="filteredList.length === 0 && searchQuery"
      :icon="Search"
      text="没有匹配的分类"
    />

    <div v-else ref="gridRef" class="category-grid">
      <div
        v-for="item in filteredList"
        :key="item.id"
        :data-id="item.id"
        class="category-card"
      >
        <!-- 拖拽手柄 -->
        <div class="drag-handle" title="拖拽排序">
          <GripVertical :size="16" />
        </div>

        <!-- 图标 -->
        <div class="card-icon" :class="`card-icon--${activeType}`">
          <component :is="getTypeIcon(activeType)" :size="22" />
        </div>

        <!-- 信息 -->
        <div class="card-body">
          <div class="card-name-row">
            <span class="card-name">{{ item.name }}</span>
            <span class="card-count" :class="{ 'count-zero': !item.count }">
              {{ item.count || 0 }} 篇
            </span>
          </div>
          <span class="card-date" v-if="item.createdAt">创建于 {{ formatDate(item.createdAt) }}</span>
        </div>

        <!-- 操作 -->
        <div class="card-actions">
          <button class="icon-btn" title="编辑" @click="openEdit(item)"><Pencil :size="15" /></button>
          <button class="icon-btn icon-btn--danger" title="删除" @click="handleDelete(item.id)"><Trash2 :size="15" /></button>
        </div>
      </div>
    </div>

    <!-- ========== 编辑弹窗 ========== -->
    <UiDialog v-model="dialogVisible" :title="isEdit ? '编辑分类' : `新建${tabs.find(t => t.type === activeType)?.label || ''}分类`">
      <UiInput v-model="form.name" placeholder="分类名称" maxlength="50" show-word-limit class="category-dialog-input" @keyup.enter="handleSave" />
      <template #footer>
        <el-button text @click="dialogVisible = false">取消</el-button>
        <UiButton type="primary" @click="handleSave">保存</UiButton>
      </template>
    </UiDialog>
  </div>
</template>

<style scoped>
.category-manage {
  max-width: 960px;
}

/* ---- Segment 切换 ---- */
.segment-group {
  display: flex;
  gap: 2px;
  margin-bottom: var(--sp-5);
  background: var(--bg-hover);
  border-radius: var(--radius-lg);
  padding: 3px;
  width: fit-content;
  border: 1px solid var(--border-color);
}

.segment-btn {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 7px 18px;
  border-radius: var(--radius-md);
  border: none;
  background: transparent;
  font-size: var(--text-sm);
  font-weight: 500;
  color: var(--text-secondary);
  cursor: pointer;
  transition: all var(--transition);
  white-space: nowrap;
}
.segment-btn:hover { color: var(--text-primary); }
.segment-btn.active {
  background: var(--bg-card);
  color: var(--accent);
  box-shadow: var(--shadow-sm);
}

.segment-count {
  font-size: var(--text-xs);
  opacity: 0.6;
  font-weight: 400;
  margin-left: 2px;
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
.category-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
  gap: var(--sp-3);
}

.category-card {
  display: flex;
  align-items: center;
  gap: var(--sp-3);
  padding: var(--sp-4);
  background: var(--bg-card);
  border: 1px solid var(--border-color);
  border-radius: var(--radius-lg);
  transition: all 0.2s ease;
  cursor: default;
  position: relative;
}
.category-card:hover {
  box-shadow: var(--shadow-md);
  border-color: var(--accent-border);
  transform: translateY(-2px);
}

.drag-handle {
  display: flex;
  align-items: center;
  cursor: grab;
  color: var(--text-tertiary);
  opacity: 0;
  transition: opacity var(--transition);
  padding: 2px;
  border-radius: 4px;
  flex-shrink: 0;
}
.category-card:hover .drag-handle { opacity: 0.5; }
.drag-handle:hover { opacity: 1 !important; color: var(--accent); background: var(--accent-light); }
.drag-handle:active { cursor: grabbing; }

.card-icon {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 40px;
  height: 40px;
  border-radius: var(--radius-md);
  flex-shrink: 0;
}
.card-icon--note { background: #e8f5e9; color: #43a047; }
.card-icon--bookmark { background: #fff3e0; color: #ef6c00; }
.card-icon--file { background: #e3f2fd; color: #1e88e5; }

.card-body {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.card-name-row {
  display: flex;
  align-items: center;
  gap: var(--sp-2);
}
.card-name {
  font-size: var(--text-sm);
  font-weight: 600;
  color: var(--text-primary);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}
.card-count {
  font-size: var(--text-xs);
  font-weight: 500;
  color: var(--accent);
  background: var(--accent-light);
  padding: 1px 8px;
  border-radius: 100px;
  white-space: nowrap;
  flex-shrink: 0;
}
.card-count.count-zero {
  color: var(--text-tertiary);
  background: var(--bg-hover);
}
.card-date {
  font-size: var(--text-xs);
  color: var(--text-tertiary);
}

.card-actions {
  display: flex;
  gap: 2px;
  opacity: 0;
  transition: opacity var(--transition);
  flex-shrink: 0;
}
.category-card:hover .card-actions { opacity: 1; }

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

/* ---- Sortable 样式 ---- */
.sortable-ghost {
  opacity: 0.3;
  border: 2px dashed var(--accent);
  background: var(--accent-light);
}
.sortable-drag {
  opacity: 0.85;
  box-shadow: var(--shadow-lg);
}

/* ---- Skeleton ---- */
.grid-skeleton {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
  gap: var(--sp-3);
}
.skeleton-card {
  height: 72px;
  border-radius: var(--radius-lg);
  background: var(--bg-hover);
  animation: pulse 1.5s ease-in-out infinite;
}

/* ---- Dialog ---- */
.category-dialog-input :deep(input) {
  font-size: var(--text-lg) !important;
  font-weight: 600;
  border: none !important;
  padding-left: 0 !important;
  background: transparent !important;
}
.category-dialog-input :deep(input)::placeholder {
  color: var(--text-placeholder);
  font-weight: 400;
}

/* ---- Animation ---- */
@keyframes pulse {
  0%, 100% { opacity: 1; }
  50% { opacity: 0.4; }
}
</style>
