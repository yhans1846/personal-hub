<script setup lang="ts">
import { ref, watch, computed, toRef } from 'vue'
import { createTodo, updateTodo, getTodoById } from '@/modules/planning/api'
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
import { useEntityDialog, useEntityFormSave } from '@/composables/useEntityDialog'
import { unwrapResult } from '@/utils/apiResult'

const props = withDefaults(defineProps<{
  modelValue: boolean
  entityId?: number
}>(), { entityId: undefined })

const emit = defineEmits<{
  'update:modelValue': [value: boolean]
  'saved': []
}>()

const form = ref({ title: '', content: '', priority: 2, dueDate: null as string | null })
const entityIdRef = toRef(props, 'entityId')

const priorityOptions = [
  { value: 1, label: '高', color: 'var(--danger)', emoji: '🔴' },
  { value: 2, label: '中', color: 'var(--warning)', emoji: '🟡' },
  { value: 3, label: '低', color: 'var(--text-tertiary)', emoji: '⚪' },
]

const dialogTitle = computed(() => props.entityId ? '编辑任务' : '新建任务')
const dialogSubtitle = computed(() => props.entityId ? '' : '今天准备完成什么？')

async function loadEntity(id: number) {
  const r = await unwrapResult(getTodoById(id))
  form.value = { title: r.title, content: r.content || '', priority: r.priority, dueDate: r.dueDate }
}

watch(() => props.modelValue, (val) => {
  if (!val || props.entityId) return
  form.value = { title: '', content: '', priority: 2, dueDate: null }
})

const { onSaved } = useEntityDialog({
  modelValue: toRef(props, 'modelValue'),
  entityId: entityIdRef,
  emit,
  loadEntity,
})

const { saving, handleSave } = useEntityFormSave({
  entityId: entityIdRef,
  validate: () => form.value.title.trim() ? null : '请输入任务标题',
  create: () => createTodo(form.value).then(() => undefined),
  update: (id) => updateTodo(id, form.value).then(() => undefined),
  onSaved,
})
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
