<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { createTodo, updateTodo, getTodoById } from '@/api/todoApi'
import { ElMessage } from 'element-plus'

const route = useRoute()
const router = useRouter()
const isEdit = !!route.params.id

const form = ref({
  title: '',
  content: '',
  priority: 2,
  dueDate: null as string | null
})
const saving = ref(false)

onMounted(async () => {
  if (isEdit) {
    const res = await getTodoById(Number(route.params.id))
    const r = res.data.data
    form.value.title = r.title
    form.value.content = r.content || ''
    form.value.priority = r.priority
    form.value.dueDate = r.dueDate
  }
})

async function handleSave() {
  if (!form.value.title.trim()) { ElMessage.warning('请输入任务标题'); return }
  saving.value = true
  try {
    if (isEdit) {
      await updateTodo(Number(route.params.id), form.value)
      ElMessage.success('已更新')
    } else {
      await createTodo(form.value)
      ElMessage.success('已创建')
    }
    router.push('/todos')
  } finally {
    saving.value = false
  }
}

const priorityOptions = [
  { value: 1, label: '高', type: 'danger' as const },
  { value: 2, label: '中', type: 'warning' as const },
  { value: 3, label: '低', type: 'info' as const }
]
</script>

<template>
  <div class="form-page">
    <h3>{{ isEdit ? '编辑任务' : '新建任务' }}</h3>
    <el-form label-width="100px" style="max-width:600px">
      <el-form-item label="任务标题" required>
        <el-input v-model="form.title" placeholder="例如：完成接口文档编写" maxlength="200" show-word-limit />
      </el-form-item>
      <el-form-item label="优先级">
        <el-radio-group v-model="form.priority">
          <el-radio v-for="item in priorityOptions" :key="item.value" :value="item.value">
            <el-tag :type="item.type" size="small">{{ item.label }}</el-tag>
          </el-radio>
        </el-radio-group>
      </el-form-item>
      <el-form-item label="截止日期">
        <el-date-picker v-model="form.dueDate" type="date" value-format="YYYY-MM-DD" placeholder="选择截止日期" style="width:100%" clearable />
      </el-form-item>
      <el-form-item label="任务内容">
        <el-input v-model="form.content" type="textarea" :rows="4" placeholder="补充任务详情（可选）" />
      </el-form-item>
      <el-form-item>
        <el-button @click="router.push('/todos')">取消</el-button>
        <el-button type="primary" :loading="saving" @click="handleSave">保存</el-button>
      </el-form-item>
    </el-form>
  </div>
</template>

<style scoped>
.form-page { max-width: 640px; }
.form-page h3 { margin-bottom: 20px; }
</style>
