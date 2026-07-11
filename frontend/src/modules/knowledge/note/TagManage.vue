<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { getTags, createTag, updateTag, deleteTag } from '@/api/tagApi'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Tags, Plus, Pencil, Trash2 } from 'lucide-vue-next'

const list = ref<any[]>([])
const dialogVisible = ref(false)
const isEdit = ref(false)
const form = ref({ id: 0, name: '' })

onMounted(() => fetchList())

async function fetchList() {
  const res = await getTags()
  list.value = res.data.data
}

function openCreate() {
  isEdit.value = false
  form.value = { id: 0, name: '' }
  dialogVisible.value = true
}

function openEdit(row: any) {
  isEdit.value = true
  form.value = { id: row.id, name: row.name }
  dialogVisible.value = true
}

async function handleSave() {
  if (!form.value.name.trim()) { ElMessage.warning('请输入名称'); return }
  if (isEdit.value) {
    await updateTag(form.value.id, { name: form.value.name })
    ElMessage.success('已更新')
  } else {
    await createTag({ name: form.value.name })
    ElMessage.success('已创建')
  }
  dialogVisible.value = false
  fetchList()
}

async function handleDelete(id: number) {
  await ElMessageBox.confirm('确定删除该标签？', '提示', { type: 'warning' })
  await deleteTag(id)
  ElMessage.success('已删除')
  fetchList()
}
</script>

<template>
  <div>
    <div class="page-header">
      <h2>标签管理</h2>
      <p>管理笔记标签</p>
    </div>

    <div class="toolbar">
      <span class="text-secondary">{{ list.length }} 个标签</span>
      <el-button type="primary" size="small" @click="openCreate">
        <Plus :size="14" /> 新建标签
      </el-button>
    </div>

    <div v-if="list.length === 0" class="empty-state">
      <div class="empty-state__icon"><Tags :size="48" /></div>
      <div class="empty-state__text">暂无标签</div>
    </div>

    <div v-else class="tag-cloud">
      <div v-for="item in list" :key="item.id" class="tag-chip">
        <span class="tag-chip-name">{{ item.name }}</span>
        <div class="tag-chip-actions">
          <button class="chip-btn" @click="openEdit(item)"><Pencil :size="12" /></button>
          <button class="chip-btn chip-btn--danger" @click="handleDelete(item.id)"><Trash2 :size="12" /></button>
        </div>
      </div>
    </div>

    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑标签' : '新建标签'" width="400px" top="30vh">
      <el-form>
        <el-form-item label="名称">
          <el-input v-model="form.name" />
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
.tag-cloud { display: flex; flex-wrap: wrap; gap: var(--sp-3); }
.tag-chip {
  display: flex; align-items: center; gap: var(--sp-2);
  padding: 6px 12px 6px 14px; background: var(--bg-card);
  border: 1px solid var(--border-color); border-radius: var(--radius-full);
  font-size: var(--text-sm); transition: all var(--transition);
}
.tag-chip:hover { box-shadow: var(--shadow-sm); border-color: var(--accent-border); }
.tag-chip-name { font-weight: 500; }
.tag-chip-actions { display: flex; gap: 2px; }
.chip-btn {
  background: none; border: none; cursor: pointer;
  padding: 2px; border-radius: 4px; color: var(--text-tertiary);
  display: flex; align-items: center; transition: all var(--transition);
}
.chip-btn:hover { color: var(--accent); background: var(--accent-light); }
.chip-btn--danger:hover { color: var(--danger); background: var(--danger-light); }
</style>
