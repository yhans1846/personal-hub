<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { getFileCategories, createFileCategory, updateFileCategory, deleteFileCategory } from '@/api/fileApi'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Folder, FolderPlus, Pencil, Trash2 } from 'lucide-vue-next'

const list = ref<any[]>([])
const dialogVisible = ref(false)
const isEdit = ref(false)
const form = ref({ id: 0, name: '', sortOrder: 0 })

onMounted(() => fetchList())

async function fetchList() {
  const res = await getFileCategories()
  list.value = res.data.data
}

function openCreate() {
  isEdit.value = false
  form.value = { id: 0, name: '', sortOrder: 0 }
  dialogVisible.value = true
}

function openEdit(row: any) {
  isEdit.value = true
  form.value = { id: row.id, name: row.name, sortOrder: row.sortOrder }
  dialogVisible.value = true
}

async function handleSave() {
  if (!form.value.name.trim()) { ElMessage.warning('请输入名称'); return }
  if (isEdit.value) {
    await updateFileCategory(form.value.id, { name: form.value.name, sortOrder: form.value.sortOrder })
    ElMessage.success('已更新')
  } else {
    await createFileCategory({ name: form.value.name, sortOrder: form.value.sortOrder })
    ElMessage.success('已创建')
  }
  dialogVisible.value = false
  fetchList()
}

async function handleDelete(id: number) {
  await ElMessageBox.confirm('确定删除该分类？', '提示', { type: 'warning' })
  await deleteFileCategory(id)
  ElMessage.success('已删除')
  fetchList()
}
</script>

<template>
  <div>
    <div class="page-header">
      <h2>文件分类</h2>
      <p>管理文件分类</p>
    </div>

    <div class="toolbar">
      <span class="text-secondary">{{ list.length }} 个分类</span>
      <el-button type="primary" size="small" @click="openCreate">
        <FolderPlus :size="14" /> 新建分类
      </el-button>
    </div>

    <div v-if="list.length === 0" class="empty-state">
      <div class="empty-state__icon"><Folder :size="48" /></div>
      <div class="empty-state__text">暂无分类</div>
    </div>

    <div v-else class="manage-list">
      <div v-for="item in list" :key="item.id" class="manage-item">
        <div class="manage-item-left">
          <Folder :size="16" class="text-tertiary" />
          <span class="manage-item-name">{{ item.name }}</span>
          <span class="manage-item-meta">排序 {{ item.sortOrder }}</span>
        </div>
        <div class="manage-item-actions">
          <button class="icon-btn" @click="openEdit(item)"><Pencil :size="14" /></button>
          <button class="icon-btn icon-btn--danger" @click="handleDelete(item.id)"><Trash2 :size="14" /></button>
        </div>
      </div>
    </div>

    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑分类' : '新建分类'" width="400px" top="30vh">
      <el-form>
        <el-form-item label="名称">
          <el-input v-model="form.name" />
        </el-form-item>
        <el-form-item label="排序">
          <el-input-number v-model="form.sortOrder" :min="0" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSave">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped>
.manage-list { display: flex; flex-direction: column; gap: var(--sp-2); }
.manage-item {
  display: flex; justify-content: space-between; align-items: center;
  padding: var(--sp-3) var(--sp-5); background: var(--bg-card);
  border: 1px solid var(--border-color); border-radius: var(--radius-md);
  transition: box-shadow var(--transition);
}
.manage-item:hover { box-shadow: var(--shadow-sm); }
.manage-item-left { display: flex; align-items: center; gap: var(--sp-3); }
.manage-item-name { font-size: var(--text-sm); font-weight: 500; }
.manage-item-meta { font-size: var(--text-xs); color: var(--text-tertiary); }
.manage-item-actions { display: flex; gap: var(--sp-1); }
.icon-btn {
  background: none; border: none; cursor: pointer;
  padding: 6px; border-radius: var(--radius-sm);
  color: var(--text-tertiary); transition: all var(--transition);
  display: flex; align-items: center;
}
.icon-btn:hover { color: var(--accent); background: var(--accent-light); }
.icon-btn--danger:hover { color: var(--danger); background: var(--danger-light); }
</style>
