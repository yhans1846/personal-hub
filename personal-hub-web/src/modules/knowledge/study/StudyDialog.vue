<script setup lang="ts">
import { ref, watch, computed, toRef } from 'vue'
import { createStudyRecord, updateStudyRecord, getStudyRecordById } from '@/modules/knowledge/api'
import { ElMessage } from 'element-plus'
import { Clock } from 'lucide-vue-next'
import {
  UiDialog,
  DialogTitleField,
  DialogPropGrid,
  DialogPropCard,
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

const form = ref({ subject: '', date: '', duration: 60, content: '', reflection: '' })
const saving = ref(false)

const dialogTitle = computed(() => props.entityId ? '编辑学习记录' : '新建学习记录')
const dialogSubtitle = computed(() => props.entityId ? '' : '记录今天的学习')

const dateModel = computed({
  get: () => form.value.date || null,
  set: (v: string | null) => { form.value.date = v || '' },
})

async function loadEntity(id: number) {
  const r = (await getStudyRecordById(id)).data.data
  form.value = { subject: r.subject, date: r.date, duration: r.duration, content: r.content || '', reflection: r.reflection || '' }
}

watch(() => props.modelValue, (val) => {
  if (!val || props.entityId) return
  form.value = {
    subject: '', date: new Date().toISOString().slice(0, 10),
    duration: 60, content: '', reflection: '',
  }
})

const { onSaved } = useEntityDialog({
  modelValue: toRef(props, 'modelValue'),
  entityId: toRef(props, 'entityId'),
  emit: (event, value) => emit(event as any, value),
  loadEntity,
})

async function handleSave() {
  if (!form.value.subject.trim()) { ElMessage.warning('请输入学习主题'); return }
  if (!form.value.duration || form.value.duration < 1) { ElMessage.warning('时长至少1分钟'); return }
  saving.value = true
  try {
    if (props.entityId) {
      await updateStudyRecord(props.entityId, form.value)
      ElMessage.success('已更新')
    } else {
      await createStudyRecord(form.value)
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
    <DialogTitleField v-model="form.subject" placeholder="学习主题" />

    <DialogPropGrid>
      <DialogPropCard label="日期">
        <DialogDateChip v-model="dateModel" placeholder="选择日期" :clearable="false" />
      </DialogPropCard>
      <DialogPropCard label="时长">
        <div class="duration-group">
          <Clock :size="14" class="meta-icon" />
          <input
            type="number"
            :min="1"
            :max="1440"
            :value="form.duration"
            class="duration-input"
            @input="form.duration = Math.max(1, Number(($event.target as HTMLInputElement).value || 1))"
          />
          <span class="duration-unit">分钟</span>
        </div>
      </DialogPropCard>
    </DialogPropGrid>

    <DialogPropCard label="学习内容" class="stack-card">
      <DialogEditor v-model="form.content" size="sm" placeholder="记录今天学了什么…" />
    </DialogPropCard>

    <DialogPropCard label="心得" class="stack-card">
      <DialogEditor v-model="form.reflection" size="sm" placeholder="有什么收获或思考？" />
    </DialogPropCard>

    <template #footer>
      <DialogFooterActions :saving="saving" @cancel="emit('update:modelValue', false)" @confirm="handleSave" />
    </template>
  </UiDialog>
</template>

<style scoped>
.duration-group {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: var(--sp-2) var(--sp-3);
  border-radius: var(--radius-sm);
  border: 1px solid var(--border-color, var(--border));
  background: var(--bg-hover);
  width: fit-content;
}
.meta-icon { flex-shrink: 0; color: var(--text-tertiary); }
.duration-input {
  width: 56px;
  border: none;
  outline: none;
  font-size: var(--text-sm);
  color: var(--text-primary);
  background: transparent;
  text-align: center;
}
.duration-unit { font-size: var(--text-xs); color: var(--text-tertiary); }
.stack-card { margin-bottom: var(--sp-3); }
.stack-card :deep(.dialog-editor) { margin-bottom: 0; min-height: 120px; }
</style>
