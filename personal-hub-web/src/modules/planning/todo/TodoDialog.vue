<script setup lang="ts">
import { ref, watch, computed, toRef } from 'vue'
import { createTodo, updateTodo, getTodoById } from '@/modules/planning/api'
import { ElMessage } from 'element-plus'
import { Calendar } from 'lucide-vue-next'
import { UiDialog, UiInput, DialogSection, DialogDivider, DialogFooterActions } from '@/components/ui'
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
  { value: 3, label: '低', color: 'var(--text-tertiary)', emoji: '⚪' }
]

const selectedPriority = computed(() => priorityOptions.find(p => p.value === form.value.priority))

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

const { loading, close, onSaved } = useEntityDialog({
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
    <!-- 标题 — 无 label，placeholder 即说明 -->
    <UiInput
      v-model="form.title"
      placeholder="做什么？"
      class="title-input"
      maxlength="200"
    />

    <DialogDivider />

    <DialogSection label="优先级">
      <div class="priority-row">
        <button
          v-for="p in priorityOptions"
          :key="p.value"
          type="button"
          class="priority-card"
          :class="{ active: form.priority === p.value }"
          :style="{
            '--card-color': p.color,
            '--card-bg': form.priority === p.value ? `${p.color}15` : 'transparent'
          }"
          @click="form.priority = p.value"
        >
          <span class="priority-emoji">{{ p.emoji }}</span>
          <span class="priority-label">{{ p.label }}</span>
        </button>
      </div>
    </DialogSection>

    <DialogSection label="截止日期">
      <div class="due-date-row">
        <button
          type="button"
          class="due-date-btn"
          :class="{ 'has-date': !!form.dueDate }"
          @click="$refs.dateInput?.showPicker ? $refs.dateInput.showPicker() : $refs.dateInput?.click()"
        >
          <Calendar :size="14" />
          <span>{{ form.dueDate || '设置截止日期' }}</span>
          <input
            ref="dateInput"
            type="date"
            class="date-input-abs"
            :value="form.dueDate || ''"
            @change="form.dueDate = ($event.target as HTMLInputElement).value || null"
          />
        </button>
        <button
          v-if="form.dueDate"
          type="button"
          class="due-date-clear"
          @click="form.dueDate = null"
        >
          ✕
        </button>
      </div>
    </DialogSection>

    <DialogDivider />

    <div class="content-section">
      <textarea
        v-model="form.content"
        class="content-editor"
        placeholder="补充任务详情、备注或清单…"
      />
    </div>

    <template #footer>
      <DialogFooterActions :saving="saving" @cancel="emit('update:modelValue', false)" @confirm="handleSave" />
    </template>
  </UiDialog>
</template>

<style scoped>
/* ---- 标题 ---- */
.title-input {
  margin-bottom: var(--sp-2);
}
.title-input :deep(input) {
  font-size: var(--text-lg) !important;
  font-weight: 600;
  border: none !important;
  padding-left: 0 !important;
  background: transparent !important;
}
.title-input :deep(input)::placeholder {
  color: var(--text-placeholder);
  font-weight: 400;
}

/* ---- 优先级 ---- */
.priority-row {
  display: flex;
  gap: var(--sp-2);
}

.priority-card {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: var(--sp-2) var(--sp-4);
  border-radius: var(--radius-md);
  border: 2px solid transparent;
  background: var(--bg-hover);
  cursor: pointer;
  transition: all var(--transition);
}
.priority-card:hover {
  border-color: var(--border-color);
  background: var(--bg-card);
}
.priority-card.active {
  border-color: var(--card-color, var(--accent));
  background: var(--card-bg, var(--accent-light));
}

.priority-emoji {
  font-size: 14px;
  line-height: 1;
}
.priority-label {
  font-size: var(--text-sm);
  font-weight: 500;
  color: var(--text-secondary);
}
.priority-card.active .priority-label {
  color: var(--card-color, var(--accent));
}

/* ---- 截止日期 ---- */
.due-date-row {
  display: flex;
  align-items: center;
  gap: var(--sp-2);
}

.due-date-btn {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: var(--sp-2) var(--sp-4);
  border-radius: var(--radius-full);
  border: 1px solid var(--border-color);
  background: var(--bg-card);
  font-size: var(--text-sm);
  color: var(--text-tertiary);
  cursor: pointer;
  transition: all var(--transition);
  position: relative;
}
.due-date-btn:hover {
  border-color: var(--accent-border);
  color: var(--text-primary);
}
.due-date-btn.has-date {
  color: var(--text-primary);
  border-color: var(--accent-border);
  background: var(--accent-light);
}

.date-input-abs {
  position: absolute;
  inset: 0;
  opacity: 0;
  cursor: pointer;
  width: 100%;
  height: 100%;
  border: none;
}

.due-date-clear {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 24px;
  height: 24px;
  border-radius: 50%;
  border: none;
  background: var(--bg-hover);
  color: var(--text-tertiary);
  cursor: pointer;
  font-size: 12px;
  transition: all var(--transition);
}
.due-date-clear:hover {
  background: var(--danger-light);
  color: var(--danger);
}

/* ---- 正文 ---- */
.content-section {
  margin-bottom: var(--sp-2);
}

.content-editor {
  width: 100%;
  min-height: 300px;
  border: none;
  outline: none;
  resize: vertical;
  font-family: var(--font-sans);
  font-size: var(--text-base);
  line-height: var(--leading-relaxed);
  color: var(--text-primary);
  background: transparent;
  padding: 0;
}
.content-editor::placeholder {
  color: var(--text-placeholder);
}
</style>
