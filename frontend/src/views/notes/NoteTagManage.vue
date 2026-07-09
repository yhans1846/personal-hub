<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { getTags, createTag, updateTag, deleteTag } from '@/api/noteApi'
import { ElMessage, ElMessageBox } from 'element-plus'

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
    <div class="toolbar">
      <h3>笔记标签管理</h3>
      <el-button type="primary" @click="openCreate">新建标签</el-button>
    </div>
    <el-table :data="list" stripe>
      <el-table-column prop="name" label="名称" />
      <el-table-column label="操作" width="160">
        <template #default="{ row }">
          <el-button size="small" text @click="openEdit(row)">编辑</el-button>
          <el-button size="small" text type="danger" @click="handleDelete(row.id)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑标签' : '新建标签'" width="400px">
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
.toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}
.toolbar h3 { margin: 0; }
</style>
