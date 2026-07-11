<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { getBookmarkCategories, createBookmarkCategory, updateBookmarkCategory, deleteBookmarkCategory } from '@/api/bookmarkApi'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Pencil, Trash2, FolderOpen } from 'lucide-vue-next'
import type { BookmarkCategoryVO } from '@/types/bookmark'
import PageHeader from '@/components/PageHeader.vue'
import EmptyState from '@/components/EmptyState.vue'

const list = ref<BookmarkCategoryVO[]>([])
const loading = ref(false)
const showForm = ref(false)
const editing = ref<BookmarkCategoryVO | null>(null)
const formName = ref('')

onMounted(() => fetchList())

async function fetchList() {
  loading.value = true
  try {
    const res = await getBookmarkCategories()
    list.value = res.data.data
  } finally { loading.value = false }
}

function openCreate() {
  editing.value = null
  formName.value = ''
  showForm.value = true
}

function openEdit(item: BookmarkCategoryVO) {
  editing.value = item
  formName.value = item.name
  showForm.value = true
}

function closeForm() { showForm.value = false }

async function handleSave() {
  if (!formName.value.trim()) { ElMessage.warning('请输入分类名称'); return }
  try {
    if (editing.value) {
      await updateBookmarkCategory(editing.value.id, { name: formName.value.trim(), sortOrder: editing.value.sortOrder })
      ElMessage.success('已更新')
    } else {
      await createBookmarkCategory({ name: formName.value.trim() })
      ElMessage.success('已创建')
    }
    closeForm()
    fetchList()
  } catch { /* ignore */ }
}

async function handleDelete(item: BookmarkCategoryVO) {
  await ElMessageBox.confirm(`确定删除分类「${item.name}」？`, '提示', { type: 'warning' })
  await deleteBookmarkCategory(item.id)
  ElMessage.success('已删除')
  fetchList()
}
</script>

<template>
  <div>
    <PageHeader title="收藏夹分类" :subtitle="`共 ${list.length} 个分类`" />

    <div v-if="loading" class="loading-skeleton">
      <div v-for="i in 3" :key="i" class="skeleton-row" />
    </div>

    <EmptyState v-else-if="list.length === 0" :icon="FolderOpen" illustration="default" text="还没有分类，创建一个吧" action-label="新建分类" @action="openCreate" />

    <div v-else class="category-list">
      <div v-for="item in list" :key="item.id" class="category-item">
        <div class="category-info">
          <FolderOpen :size="16" class="category-icon" />
          <span class="category-name">{{ item.name }}</span>
        </div>
        <div class="category-actions">
          <button class="icon-btn" @click="openEdit(item)"><Pencil :size="14" /></button>
          <button class="icon-btn icon-btn--danger" @click="handleDelete(item)"><Trash2 :size="14" /></button>
        </div>
      </div>
    </div>

    <!-- 新建/编辑对话框 -->
    <el-dialog
      :model-value="showForm"
      :title="editing ? '编辑分类' : '新建分类'"
      width="400px"
      @close="closeForm"
    >
      <el-form label-position="top">
        <el-form-item label="分类名称">
          <el-input v-model="formName" placeholder="例如：开发工具" maxlength="50" @keyup.enter="handleSave" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="closeForm">取消</el-button>
        <el-button type="primary" @click="handleSave">保存</el-button>
      </template>
    </el-dialog>

    <el-button class="fab" type="primary" circle @click="openCreate">
      <Plus :size="16" />
    </el-button>
  </div>
</template>

<style scoped>
.loading-skeleton { display: flex; flex-direction: column; gap: var(--sp-2); }
.skeleton-row { height: 52px; border-radius: var(--radius-md); background: var(--bg-hover); animation: pulse 1.5s ease-in-out infinite; }

.category-list { display: flex; flex-direction: column; gap: var(--sp-2); max-width: 480px; }
.category-item {
  display: flex; align-items: center; justify-content: space-between;
  padding: var(--sp-3) var(--sp-4); background: var(--bg-card);
  border: 1px solid var(--border-color); border-radius: var(--radius-md);
  transition: all var(--transition);
}
.category-item:hover { border-color: var(--accent-border); }

.category-info { display: flex; align-items: center; gap: var(--sp-2); }
.category-icon { color: var(--accent); }
.category-name { font-size: var(--text-sm); font-weight: 500; }

.category-actions { display: flex; gap: var(--sp-1); opacity: 0; transition: opacity var(--transition); }
.category-item:hover .category-actions { opacity: 1; }

.icon-btn { background: none; border: none; cursor: pointer; padding: 6px; border-radius: var(--radius-sm); color: var(--text-tertiary); transition: all var(--transition); display: flex; align-items: center; }
.icon-btn:hover { color: var(--accent); background: var(--accent-light); }
.icon-btn--danger:hover { color: var(--danger); background: var(--danger-light); }

.fab { position: fixed; bottom: var(--sp-8); right: var(--sp-8); z-index: 10; }
</style>
