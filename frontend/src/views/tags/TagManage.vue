<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { getTags, createTag, updateTag, deleteTag } from '@/api/tagApi'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Tags, Plus, Pencil, Trash2 } from 'lucide-vue-next'
import type { TagVO } from '@/types/tag'

const list = ref<TagVO[]>([])
const dialogVisible = ref(false)
const isEdit = ref(false)
const form = ref({ id: 0, name: '', color: '#409eff' })

const COLORS = [
  '#409eff', '#67c23a', '#e6a23c', '#f56c6c',
  '#909399', '#9b59b6', '#2ecc71', '#3498db',
  '#e74c3c', '#f39c12', '#1abc9c', '#34495e',
]

onMounted(() => fetchList())

async function fetchList() {
  const res = await getTags()
  list.value = res.data.data
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
</script>

<template>
  <div>
    <div class="page-header">
      <h2>统一标签管理</h2>
      <p>管理所有模块的标签，支持颜色标记和使用统计</p>
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

    <div v-else class="tag-grid">
      <div v-for="item in list" :key="item.id" class="tag-card">
        <span class="tag-color-dot" :style="{ background: item.color || '#409eff' }" />
        <div class="tag-card-body">
          <span class="tag-card-name">{{ item.name }}</span>
          <span class="tag-card-count">{{ item.usageCount }} 次使用</span>
        </div>
        <div class="tag-card-actions">
          <button class="chip-btn" @click="openEdit(item)"><Pencil :size="12" /></button>
          <button class="chip-btn chip-btn--danger" @click="handleDelete(item.id)"><Trash2 :size="12" /></button>
        </div>
      </div>
    </div>

    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑标签' : '新建标签'" width="420px" top="30vh">
      <el-form>
        <el-form-item label="名称">
          <el-input v-model="form.name" placeholder="标签名称" maxlength="50" show-word-limit />
        </el-form-item>
        <el-form-item label="颜色">
          <div class="color-picker">
            <button
              v-for="c in COLORS" :key="c"
              class="color-btn"
              :class="{ active: form.color === c }"
              :style="{ background: c }"
              @click="form.color = c"
            />
          </div>
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
.tag-grid { display: flex; flex-wrap: wrap; gap: var(--sp-3); }

.tag-card {
  display: flex; align-items: center; gap: var(--sp-3);
  padding: 10px 14px; background: var(--bg-card);
  border: 1px solid var(--border-color); border-radius: var(--radius-md);
  min-width: 180px; transition: all var(--transition);
}
.tag-card:hover { box-shadow: var(--shadow-sm); border-color: var(--accent-border); }

.tag-color-dot { width: 12px; height: 12px; border-radius: 50%; flex-shrink: 0; }
.tag-card-body { flex: 1; display: flex; flex-direction: column; gap: 2px; }
.tag-card-name { font-size: var(--text-sm); font-weight: 500; }
.tag-card-count { font-size: var(--text-xs); color: var(--text-tertiary); }
.tag-card-actions { display: flex; gap: 2px; }

.chip-btn {
  background: none; border: none; cursor: pointer;
  padding: 4px; border-radius: 4px; color: var(--text-tertiary);
  display: flex; align-items: center; transition: all var(--transition);
}
.chip-btn:hover { color: var(--accent); background: var(--accent-light); }
.chip-btn--danger:hover { color: var(--danger); background: var(--danger-light); }

.color-picker { display: flex; gap: 8px; flex-wrap: wrap; }
.color-btn {
  width: 28px; height: 28px; border-radius: 50%; border: 2px solid transparent;
  cursor: pointer; transition: all var(--transition);
}
.color-btn:hover { transform: scale(1.15); }
.color-btn.active { border-color: var(--text-primary); transform: scale(1.15); }
</style>
