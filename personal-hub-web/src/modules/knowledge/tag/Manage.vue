<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { getTags, createTag, updateTag, deleteTag } from '@/modules/knowledge/api'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Tags, Pencil, Trash2, Search, ArrowUpDown } from 'lucide-vue-next'
import type { TagVO } from '@/types/tag'
import { handleApiError } from '@/utils/apiResult'
import { PageHeader, EmptyState, ListToolbar } from '@/components'
import { UiDialog, UiInput, DialogPropCard, DialogPropGrid, DialogFooterActions } from '@/components/ui'
import TagStatsCards from './TagStatsCards.vue'
import { formatDate } from '@/utils/formatTime'

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
  } catch (e: unknown) {
    handleApiError(e, '操作失败')
  }
}

async function handleDelete(id: number) {
  await ElMessageBox.confirm('确定删除该标签？删除后所有关联将被清除。', '提示', { type: 'warning' })
  try {
    await deleteTag(id)
    ElMessage.success('已删除')
    fetchList()
  } catch (e: unknown) {
    handleApiError(e, '删除失败')
  }
}

function onColorInput(e: Event) {
  form.value.color = (e.target as HTMLInputElement).value
}

function toggleSortDir() {
  sortDir.value = sortDir.value === 'asc' ? 'desc' : 'asc'
}
</script>

<template>
  <div class="tag-manage">
    <!-- ========== Header ========== -->
    <PageHeader title="标签管理" />

    <!-- ========== 统计卡片 ========== -->
    <TagStatsCards :total="stats.total" :total-usage="stats.totalUsage" :recent-added="stats.recentAdded" />

    <!-- ========== 工具栏 ========== -->
    <ListToolbar
      :search="searchQuery"
      search-placeholder="搜索标签..."
      search-width="260px"
      create-label="新建标签"
      @update:search="searchQuery = $event"
      @create="openCreate"
    >
      <template #filters>
        <el-select v-model="colorFilter" placeholder="全部颜色" clearable style="width: 120px">
          <el-option v-for="c in uniqueColors" :key="c" :value="c" :label="colorNames[c] || c">
            <span class="color-option">● {{ colorNames[c] || c }}</span>
          </el-option>
        </el-select>
        <el-select v-model="sortBy" style="width: 120px">
          <el-option value="usageCount" label="使用次数" />
          <el-option value="name" label="名称" />
          <el-option value="createdAt" label="创建时间" />
        </el-select>
        <button class="sort-dir-btn" @click="toggleSortDir" :title="sortDir === 'asc' ? '正序' : '倒序'">
          <ArrowUpDown :size="15" class="sort-icon" :class="{ reversed: sortDir === 'desc' }" />
        </button>
      </template>
    </ListToolbar>

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
    <UiDialog v-model="dialogVisible" size="sm" :title="isEdit ? '编辑标签' : '新建标签'">
      <DialogPropGrid :cols="1">
        <DialogPropCard label="名称">
          <UiInput v-model="form.name" placeholder="标签名称" maxlength="50" show-word-limit />
        </DialogPropCard>

        <DialogPropCard label="颜色">
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
        </DialogPropCard>
      </DialogPropGrid>

      <template #footer>
        <DialogFooterActions @cancel="dialogVisible = false" @confirm="handleSave" />
      </template>
    </UiDialog>
  </div>
</template>

<style scoped>
.tag-manage {
  max-width: 1100px;
}

.color-option { font-size: var(--text-sm); }

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
  transition: all var(--transition);
  cursor: default;
  overflow: hidden;
  content-visibility: auto;
  contain-intrinsic-size: auto 72px;
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
  border-radius: var(--radius-md);
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
.color-picker { display: flex; gap: 8px; flex-wrap: wrap; }
.color-btn {
  width: 28px; height: 28px; border-radius: 50%; border: 2px solid transparent;
  cursor: pointer; transition: all var(--transition);
}
.color-btn:hover { transform: scale(1.15); }
.color-btn.active { border-color: var(--text-primary); transform: scale(1.15); }
.color-btn--custom {
  position: relative; width: auto; padding: 0 10px; border-radius: var(--radius-sm);
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
