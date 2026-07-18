<script setup lang="ts">
import { ref, watch, computed, toRef } from 'vue'
import { createStudyPlan, updateStudyPlan, getStudyPlanById } from '@/modules/planning/api'
import { getTags } from '@/modules/knowledge/api'
import { ElMessage } from 'element-plus'
import { Calendar, Link } from 'lucide-vue-next'
import { UiDialog, UiInput, DialogSection, DialogDivider, DialogFooterActions } from '@/components/ui'
import { useEntityDialog } from '@/composables/useEntityDialog'
import type { TagVO } from '@/types/tag'

const props = withDefaults(defineProps<{
  modelValue: boolean
  entityId?: number
}>(), { entityId: undefined })

const emit = defineEmits<{
  'update:modelValue': [value: boolean]
  'saved': []
}>()

const emptyForm = () => ({
  name: '',
  source: '',
  author: '',
  url: '',
  remark: '',
  progress: 0,
  startDate: null as string | null,
  endDate: null as string | null,
  status: 0,
  tagIds: [] as number[],
})

const form = ref(emptyForm())
const tags = ref<TagVO[]>([])
const saving = ref(false)

const statusOptions = [
  { value: 0, label: '未开始', emoji: '⚪' },
  { value: 1, label: '学习中', emoji: '🟢' },
  { value: 2, label: '已完成', emoji: '✅' },
  { value: 3, label: '已暂停', emoji: '🟡' },
]

const dialogTitle = computed(() => props.entityId ? '编辑计划' : '新建计划')
const dialogSubtitle = computed(() => props.entityId ? '' : '记录一门课程或学习资源')

const progressColor = computed(() => {
  if (form.value.progress >= 100) return 'var(--success)'
  if (form.value.progress >= 50) return 'var(--accent)'
  return 'var(--warning)'
})

async function loadEntity(id: number) {
  const r = (await getStudyPlanById(id)).data.data
  form.value = {
    name: r.name,
    source: r.source || '',
    author: r.author || '',
    url: r.url || '',
    remark: r.remark || '',
    progress: r.progress ?? 0,
    startDate: r.startDate,
    endDate: r.endDate,
    status: r.status,
    tagIds: (r.tags || []).map((t: TagVO) => t.id),
  }
}

watch(() => props.modelValue, async (val) => {
  if (!val) return
  try {
    const tagRes = await getTags()
    tags.value = tagRes.data.data
  } catch { /* ignore */ }
  if (!props.entityId) {
    form.value = emptyForm()
  }
})

const { onSaved } = useEntityDialog({
  modelValue: toRef(props, 'modelValue'),
  entityId: toRef(props, 'entityId'),
  emit: (event, value) => emit(event as any, value),
  loadEntity,
})

async function handleSave() {
  if (!form.value.name.trim()) {
    ElMessage.warning('请输入计划名称')
    return
  }
  let url = form.value.url.trim()
  if (url && !/^https?:\/\//i.test(url)) {
    url = 'https://' + url
  }
  saving.value = true
  try {
    const payload = {
      name: form.value.name.trim(),
      source: form.value.source.trim() || null,
      author: form.value.author.trim() || null,
      url: url || null,
      remark: form.value.remark.trim() || null,
      progress: form.value.progress,
      startDate: form.value.startDate,
      endDate: form.value.endDate,
      status: form.value.status,
      tagIds: form.value.tagIds,
    }
    if (props.entityId) {
      await updateStudyPlan(props.entityId, payload)
      ElMessage.success('已更新')
    } else {
      await createStudyPlan(payload)
      ElMessage.success('已创建')
    }
    onSaved()
  } finally {
    saving.value = false
  }
}
</script>

<template>
  <UiDialog
    :model-value="modelValue"
    :title="dialogTitle"
    :subtitle="dialogSubtitle"
    @update:model-value="emit('update:modelValue', $event)"
  >
    <UiInput
      v-model="form.name"
      placeholder="计划名称"
      class="title-input"
      maxlength="200"
    />

    <div class="meta-grid">
      <UiInput v-model="form.source" placeholder="来源（如 B站）" maxlength="100" />
      <UiInput v-model="form.author" placeholder="作者" maxlength="100" />
    </div>

    <div class="url-row">
      <Link :size="14" class="url-icon" />
      <input
        v-model="form.url"
        class="url-input"
        placeholder="https:// 资源地址"
        maxlength="500"
      />
    </div>

    <DialogDivider />

    <DialogSection label="分类标签">
      <div class="chip-row">
        <button
          v-for="t in tags"
          :key="t.id"
          type="button"
          class="chip tag-chip"
          :class="{ active: form.tagIds.includes(t.id) }"
          :style="form.tagIds.includes(t.id) ? { '--chip-color': t.color } : {}"
          @click="form.tagIds = form.tagIds.includes(t.id) ? form.tagIds.filter(id => id !== t.id) : [...form.tagIds, t.id]"
        >
          <span class="tag-dot" :style="{ background: t.color }" />
          {{ t.name }}
        </button>
        <span v-if="tags.length === 0" class="chip-empty">暂无标签，可先在标签管理中创建</span>
      </div>
    </DialogSection>

    <DialogSection label="状态">
      <div class="status-row">
        <button
          v-for="s in statusOptions"
          :key="s.value"
          type="button"
          class="status-card"
          :class="{ active: form.status === s.value }"
          @click="form.status = s.value"
        >
          <span class="status-emoji">{{ s.emoji }}</span>
          <span class="status-label">{{ s.label }}</span>
        </button>
      </div>
    </DialogSection>

    <DialogSection label="进度">
      <div class="progress-row">
        <input
          type="range"
          :min="0"
          :max="100"
          :value="form.progress"
          class="progress-slider"
          :style="{ '--progress-color': progressColor }"
          @input="form.progress = Number(($event.target as HTMLInputElement).value)"
        />
        <span class="progress-value" :style="{ color: progressColor }">{{ form.progress }}%</span>
      </div>
    </DialogSection>

    <DialogSection label="时间范围">
      <div class="date-range-row">
        <button
          type="button"
          class="date-chip"
          :class="{ 'has-date': !!form.startDate }"
          @click="($refs.startInput as HTMLInputElement)?.showPicker?.() ?? ($refs.startInput as HTMLInputElement)?.click()"
        >
          <Calendar :size="14" />
          <span>{{ form.startDate || '开始日期' }}</span>
          <input
            ref="startInput"
            type="date"
            class="date-input-abs"
            :value="form.startDate || ''"
            @change="form.startDate = ($event.target as HTMLInputElement).value || null"
          />
        </button>
        <span class="date-range-arrow">→</span>
        <button
          type="button"
          class="date-chip"
          :class="{ 'has-date': !!form.endDate }"
          @click="($refs.endInput as HTMLInputElement)?.showPicker?.() ?? ($refs.endInput as HTMLInputElement)?.click()"
        >
          <Calendar :size="14" />
          <span>{{ form.endDate || '结束日期' }}</span>
          <input
            ref="endInput"
            type="date"
            class="date-input-abs"
            :value="form.endDate || ''"
            @change="form.endDate = ($event.target as HTMLInputElement).value || null"
          />
        </button>
      </div>
    </DialogSection>

    <DialogDivider />

    <div class="content-section">
      <textarea
        v-model="form.remark"
        class="content-editor"
        placeholder="备注…"
      />
    </div>

    <template #footer>
      <DialogFooterActions :saving="saving" @cancel="emit('update:modelValue', false)" @confirm="handleSave" />
    </template>
  </UiDialog>
</template>

<style scoped>
.title-input {
  margin-bottom: var(--sp-3);
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

.meta-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: var(--sp-3);
  margin-bottom: var(--sp-3);
}

.url-row {
  display: flex;
  align-items: center;
  gap: 6px;
  margin-bottom: var(--sp-3);
}
.url-icon {
  flex-shrink: 0;
  color: var(--text-tertiary);
}
.url-input {
  flex: 1;
  border: none;
  background: transparent;
  font-size: var(--text-sm);
  color: var(--text-secondary);
  outline: none;
  padding: 4px 0;
}
.url-input::placeholder {
  color: var(--text-placeholder);
}
.url-input:focus {
  color: var(--text-primary);
}

.chip-row {
  display: flex;
  flex-wrap: wrap;
  gap: var(--sp-2);
}
.chip {
  display: inline-flex;
  align-items: center;
  gap: 5px;
  padding: 5px 12px;
  border-radius: var(--radius-full);
  border: 1px solid var(--border-color);
  background: var(--bg-card);
  font-size: var(--text-xs);
  color: var(--text-secondary);
  cursor: pointer;
  transition: all var(--transition);
  white-space: nowrap;
}
.chip:hover {
  border-color: var(--accent-border);
  color: var(--text-primary);
}
.chip.active {
  border-color: var(--chip-color, var(--accent));
  color: var(--chip-color, var(--accent));
  background: var(--accent-light);
}
.tag-chip.active {
  background: color-mix(in srgb, var(--chip-color, var(--accent)) 12%, transparent);
}
.tag-dot {
  width: 7px;
  height: 7px;
  border-radius: 50%;
  display: inline-block;
  flex-shrink: 0;
}
.chip-empty {
  font-size: var(--text-xs);
  color: var(--text-placeholder);
}

.status-row {
  display: flex;
  flex-wrap: wrap;
  gap: var(--sp-2);
}
.status-card {
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
.status-card:hover {
  border-color: var(--border-color);
  background: var(--bg-card);
}
.status-card.active {
  border-color: var(--accent);
  background: var(--accent-light);
}
.status-emoji {
  font-size: 14px;
  line-height: 1;
}
.status-label {
  font-size: var(--text-sm);
  font-weight: 500;
  color: var(--text-secondary);
}
.status-card.active .status-label {
  color: var(--accent);
}

.progress-row {
  display: flex;
  align-items: center;
  gap: var(--sp-3);
  margin-bottom: var(--sp-5);
}
.progress-slider {
  flex: 1;
  -webkit-appearance: none;
  appearance: none;
  height: 6px;
  border-radius: 3px;
  background: var(--bg-hover);
  outline: none;
  cursor: pointer;
}
.progress-slider::-webkit-slider-thumb {
  -webkit-appearance: none;
  width: 16px;
  height: 16px;
  border-radius: 50%;
  background: var(--progress-color, var(--accent));
  border: 2px solid var(--bg-card);
  box-shadow: var(--shadow-sm);
  cursor: pointer;
}
.progress-value {
  font-size: var(--text-sm);
  font-weight: 600;
  min-width: 40px;
  text-align: right;
}

.date-range-row {
  display: flex;
  align-items: center;
  gap: var(--sp-2);
  margin-bottom: var(--sp-5);
}
.date-chip {
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
.date-chip:hover {
  border-color: var(--accent-border);
  color: var(--text-primary);
}
.date-chip.has-date {
  color: var(--text-primary);
  border-color: var(--accent-border);
  background: var(--accent-light);
}
.date-range-arrow {
  color: var(--text-tertiary);
  font-size: var(--text-sm);
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

.content-section {
  margin-bottom: var(--sp-2);
}
.content-editor {
  width: 100%;
  min-height: 160px;
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
