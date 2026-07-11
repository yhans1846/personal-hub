<script setup lang="ts">
import { ref, watch } from 'vue'
import { createTodo, updateTodo, getTodoById } from '@/api/todoApi'
import { ElMessage } from 'element-plus'
import { UiDialog, UiInput, UiTextarea, UiDatePicker, UiButton } from '@/components/ui'

const props = withDefaults(defineProps<{
  modelValue: boolean
  entityId?: number
}>(), { entityId: undefined })

const emit = defineEmits<{
  'update:modelValue': [value: boolean]
  'saved': []
}>()

const form = ref({ title: '', content: '', priority: 2, dueDate: null as string | null })
const saving = ref(false)

const priorityOptions = [
  { value: 1, label: '高', type: 'danger' as const },
  { value: 2, label: '中', type: 'warning' as const },
  { value: 3, label: '低', type: 'info' as const }
]

watch(() => props.modelValue, async (val) => {
  if (!val) return
  if (props.entityId) {
    const res = await getTodoById(props.entityId)
    const r = res.data.data
    form.value = { title: r.title, content: r.content || '', priority: r.priority, dueDate: r.dueDate }
  } else {
    form.value = { title: '', content: '', priority: 2, dueDate: null }
  }
})

async function handleSave() {
  if (!form.value.title.trim()) { ElMessage.warning('请输入任务标题'); return }
  saving.value = true
  try {
    if (props.entityId) {
      await updateTodo(props.entityId, form.value)
      ElMessage.success('已更新')
    } else {
      await createTodo(form.value)
      ElMessage.success('已创建')
    }
    emit('update:modelValue', false)
    emit('saved')
  } finally { saving.value = false }
}
</script>

<template>
  <UiDialog
    :model-value="modelValue"
    :title="entityId ? '编辑任务' : '新建任务'"
    @update:model-value="emit('update:modelValue', $event)"
  >
    <el-form label-position="top">
      <el-form-item label="任务标题" required>
        <UiInput v-model="form.title" placeholder="例如：完成接口文档编写" maxlength="200" show-word-limit />
      </el-form-item>
      <el-form-item label="优先级">
        <el-radio-group v-model="form.priority">
          <el-radio v-for="item in priorityOptions" :key="item.value" :value="item.value">
            <el-tag :type="item.type" size="small">{{ item.label }}</el-tag>
          </el-radio>
        </el-radio-group>
      </el-form-item>
      <el-form-item label="截止日期">
        <UiDatePicker v-model="form.dueDate" type="date" value-format="YYYY-MM-DD" placeholder="选择截止日期" style="width:100%" clearable />
      </el-form-item>
      <el-form-item label="任务内容">
        <UiTextarea v-model="form.content" placeholder="补充任务详情（可选）" />
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="emit('update:modelValue', false)">取消</el-button>
      <UiButton type="primary" :loading="saving" @click="handleSave">保存</UiButton>
    </template>
  </UiDialog>
</template>
