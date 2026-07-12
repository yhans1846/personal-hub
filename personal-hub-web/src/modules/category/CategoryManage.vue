<script setup lang="ts">
import { ref, onMounted, computed, watch } from 'vue'
import { getCategories, createCategory, updateCategory, deleteCategory } from '@/api/categoryApi'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Pencil, Trash2, FolderOpen, Folder, Bookmark } from 'lucide-vue-next'
import PageHeader from '@/components/PageHeader.vue'
import EmptyState from '@/components/EmptyState.vue'
import { UiDialog, UiInput, UiButton } from '@/components/ui'
import type { CategoryVO } from '@/types/category'

const activeType = ref<'note' | 'bookmark' | 'file'>('note')

const tabs = [
  { type: 'note' as const, label: '笔记分类', icon: FolderOpen },
  { type: 'bookmark' as const, label: '收藏夹分类', icon: Bookmark },
  { type: 'file' as const, label: '文件分类', icon: Folder }
]

const list = ref<CategoryVO[]>([])
const loading = ref(false)
const dialogVisible = ref(false)
const isEdit = ref(false)
const form = ref({ id: 0, name: '' })

watch(() => activeType.value, () => fetchList())
onMounted(() => fetchList())

async function fetchList() {
  loading.value = true
  try {
    const res = await getCategories(activeType.value)
    list.value = res.data.data || []
  } finally { loading.value = false }
}

function openCreate() {
  isEdit.value = false; form.value = { id: 0, name: '' }; dialogVisible.value = true
}

function openEdit(item: CategoryVO) {
  isEdit.value = true; form.value = { id: item.id, name: item.name }; dialogVisible.value = true
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
    dialogVisible.value = false; fetchList()
  } catch (e: any) {
    ElMessage.error(e.response?.data?.message || '操作失败')
  }
}

async function handleDelete(id: number) {
  await ElMessageBox.confirm('确定删除该分类？', '提示', { type: 'warning' })
  try {
    await deleteCategory(id)
    ElMessage.success('已删除'); fetchList()
  } catch (e: any) {
    ElMessage.error(e.response?.data?.message || '删除失败')
  }
}
</script>

<template>
  <div>
    <PageHeader title="分类管理" subtitle="管理所有模块的分类" />

    <!-- Tab 切换 -->
    <div class="category-tabs">
      <button
        v-for="tab in tabs"
        :key="tab.type"
        class="tab-btn"
        :class="{ active: activeType === tab.type }"
        @click="activeType = tab.type"
      >
        <component :is="tab.icon" :size="14" />
        {{ tab.label }}
      </button>
    </div>

    <div class="toolbar">
      <span class="text-secondary">{{ list.length }} 个分类</span>
      <el-button type="primary" size="small" @click="openCreate">
        <Plus :size="14" /> 新建分类
      </el-button>
    </div>

    <div v-if="loading" class="loading-skeleton">
      <div v-for="i in 3" :key="i" class="skeleton-row" />
    </div>

    <EmptyState v-else-if="list.length === 0" :icon="tabs.find(t => t.type === activeType)?.icon || Folder" illustration="default" text="暂无分类，创建一个吧" action-label="新建分类" @action="openCreate" />

    <div v-else class="manage-list">
      <div v-for="item in list" :key="item.id" class="manage-item">
        <div class="manage-item-left">
          <component :is="tabs.find(t => t.type === activeType)?.icon || Folder" :size="16" class="text-tertiary" />
          <span class="manage-item-name">{{ item.name }}</span>
          <span class="manage-item-meta" v-if="item.count">({{ item.count }})</span>
          <span class="manage-item-meta">排序 {{ item.sortOrder }}</span>
        </div>
        <div class="manage-item-actions">
          <button class="icon-btn" @click="openEdit(item)"><Pencil :size="14" /></button>
          <button class="icon-btn icon-btn--danger" @click="handleDelete(item.id)"><Trash2 :size="14" /></button>
        </div>
      </div>
    </div>

    <UiDialog v-model="dialogVisible" :title="isEdit ? '编辑分类' : `新建${tabs.find(t => t.type === activeType)?.label || '分类'}`">
      <UiInput v-model="form.name" placeholder="分类名称" maxlength="50" show-word-limit class="category-dialog-input" @keyup.enter="handleSave" />
      <template #footer>
        <el-button text @click="dialogVisible = false">取消</el-button>
        <UiButton type="primary" @click="handleSave">保存</UiButton>
      </template>
    </UiDialog>
  </div>
</template>

<style scoped>
.loading-skeleton { display: flex; flex-direction: column; gap: var(--sp-2); }
.skeleton-row { height: 52px; border-radius: var(--radius-md); background: var(--bg-hover); animation: pulse 1.5s ease-in-out infinite; }

.category-tabs { display: flex; gap: var(--sp-2); margin-bottom: var(--sp-5); }
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

.tab-btn {
  display: flex; align-items: center; gap: var(--sp-1);
  padding: 8px 16px; border-radius: var(--radius-md);
  border: 1px solid var(--border-color); background: var(--bg-card);
  font-size: var(--text-sm); color: var(--text-secondary); cursor: pointer;
  transition: all var(--transition);
}
.tab-btn:hover { border-color: var(--accent-border); color: var(--text-primary); }
.tab-btn.active { border-color: var(--accent); color: var(--accent); background: var(--accent-light); }

.manage-list { display: flex; flex-direction: column; gap: var(--sp-2); max-width: 480px; }
.manage-item {
  display: flex; align-items: center; justify-content: space-between;
  padding: var(--sp-3) var(--sp-4); background: var(--bg-card);
  border: 1px solid var(--border-color); border-radius: var(--radius-md);
  transition: all var(--transition);
}
.manage-item:hover { border-color: var(--accent-border); }
.manage-item-left { display: flex; align-items: center; gap: var(--sp-2); }
.manage-item-name { font-size: var(--text-sm); font-weight: 500; }
.manage-item-meta { font-size: var(--text-xs); color: var(--text-tertiary); }
.manage-item-actions { display: flex; gap: var(--sp-1); opacity: 0; transition: opacity var(--transition); }
.manage-item:hover .manage-item-actions { opacity: 1; }

.icon-btn { background: none; border: none; cursor: pointer; padding: 6px; border-radius: var(--radius-sm); color: var(--text-tertiary); transition: all var(--transition); display: flex; align-items: center; }
.icon-btn:hover { color: var(--accent); background: var(--accent-light); }
.icon-btn--danger:hover { color: var(--danger); background: var(--danger-light); }

.toolbar { display: flex; justify-content: space-between; align-items: center; margin-bottom: var(--sp-5); }
</style>
