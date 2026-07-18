<script setup lang="ts">
import { ref, watch, computed, toRef } from 'vue'
import { createTodo, updateTodo, getTodoById } from '@/modules/planning/api'
import { ElMessage } from 'element-plus'
import {
  UiDialog,
  DialogTitleField,
  DialogPropGrid,
  DialogPropCard,
  DialogChoiceRow,
  DialogDateChip,
  DialogEditor,
  DialogFooterActions,
} from '@/components/ui'
import { useEntityDialog } from '@/composables/useEntityDialog'

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
  { value: 1, label: '高', color: 'var(--danger)', emoji: '🔴' },
  { value: 2, label: '中', color: 'var(--warning)', emoji: '🟡' },
  { value: 3, label: '低', color: 'var(--text-tertiary)', emoji: '⚪' },
]

const dialogTitle = computed(() => props.entityId ? '编辑任务' : '新建任务')
const dialogSubtitle = computed(() => props.entityId ? '' : '今天准备完成什么？')

async function loadEntity(id: number) {
  const res = await getTodoById(id)
  const r = res.data.data
  form.value = { title: r.title, content: r.content || '', priority: r.priority, dueDate: r.dueDate }
}

watch(() => props.modelValue, (val) => {
  if (!val || props.entityId) return
  form.value = { title: '', content: '', priority: 2, dueDate: null }
})

const { onSaved } = useEntityDialog({
  modelValue: toRef(props, 'modelValue'),
  entityId: toRef(props, 'entityId'),
  emit: (event, value) => emit(event as any, value),
  loadEntity,
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
    onSaved()
  } finally { saving.value = false }
}
</script>

<template>
  <UiDialog
    :model-value="modelValue"
    :title="dialogTitle"
    :subtitle="dialogSubtitle"
    @update:model-value="emit('update:modelValue', $event)"
  >
    <DialogTitleField
      v-model="form.title"
      placeholder="做什么？"
      maxlength="200"
    />

    <DialogPropGrid>
      <DialogPropCard label="优先级">
        <DialogChoiceRow v-model="form.priority" :options="priorityOptions" />
      </DialogPropCard>
      <DialogPropCard label="截止日期">
        <DialogDateChip v-model="form.dueDate" placeholder="设置截止日期" />
      </DialogPropCard>
    </DialogPropGrid>

    <DialogEditor
      v-model="form.content"
      size="lg"
      placeholder="补充任务详情、备注或清单…"
    />

    <template #footer>
      <DialogFooterActions :saving="saving" @cancel="emit('update:modelValue', false)" @confirm="handleSave" />
    </template>
  </UiDialog>
</template>
