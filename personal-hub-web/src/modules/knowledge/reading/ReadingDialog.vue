<script setup lang="ts">
import { ref, watch, computed, toRef } from 'vue'
import { createReading, updateReading, getReadingById } from '@/modules/knowledge/api'
import { ElMessage } from 'element-plus'
import { Star } from 'lucide-vue-next'
import {
  UiDialog,
  UiInput,
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

const form = ref({
  bookTitle: '', author: '', coverUrl: '', totalChapters: 0, currentChapter: 0,
  progress: 0, rating: undefined as number | undefined, totalDuration: undefined as number | undefined,
  status: 0, notes: '', startDate: null as string | null, endDate: null as string | null
})
const saving = ref(false)

const statusOptions = [
  { value: 0, label: '未读', emoji: '📋' },
  { value: 1, label: '在读', emoji: '📖' },
  { value: 2, label: '读完', emoji: '✅' }
]

const dialogTitle = computed(() => props.entityId ? '编辑阅读记录' : '添加书籍')
const dialogSubtitle = computed(() => props.entityId ? '' : '记录一本值得回顾的书')

const progressColor = computed(() => {
  if (form.value.progress >= 100) return 'var(--success)'
  if (form.value.progress >= 50) return 'var(--accent)'
  return 'var(--warning)'
})

async function loadEntity(id: number) {
  const r = (await getReadingById(id)).data.data
  form.value = {
    bookTitle: r.bookTitle, author: r.author || '', coverUrl: r.coverUrl || '',
    totalChapters: r.totalChapters, currentChapter: r.currentChapter, progress: r.progress,
    rating: r.rating, totalDuration: r.totalDuration, status: r.status,
    notes: r.notes || '', startDate: r.startDate, endDate: r.endDate
  }
}

watch(() => props.modelValue, (val) => {
  if (!val || props.entityId) return
  form.value = {
    bookTitle: '', author: '', coverUrl: '', totalChapters: 0, currentChapter: 0,
    progress: 0, rating: undefined, totalDuration: undefined, status: 0,
    notes: '', startDate: null, endDate: null
  }
})

const { onSaved } = useEntityDialog({
  modelValue: toRef(props, 'modelValue'),
  entityId: toRef(props, 'entityId'),
  emit: (event, value) => emit(event as any, value),
  loadEntity,
})

async function handleSave() {
  if (!form.value.bookTitle.trim()) { ElMessage.warning('请输入书名'); return }
  saving.value = true
  try {
    if (props.entityId) {
      await updateReading(props.entityId, form.value)
      ElMessage.success('已更新')
    } else {
      await createReading(form.value)
      ElMessage.success('已创建')
    }
    onSaved()
  } finally { saving.value = false }
}

function onProgressChange(e: Event) {
  const val = Number((e.target as HTMLInputElement).value)
  form.value.progress = val
  if (val >= 100) form.value.status = 2
  else if (val > 0 && form.value.status === 0) form.value.status = 1
}

function toggleRating(r: number) {
  form.value.rating = form.value.rating === r ? undefined : r
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
      v-model="form.bookTitle"
      placeholder="书名"
      maxlength="255"
    />

    <DialogPropGrid>
      <DialogPropCard label="作者">
        <UiInput v-model="form.author" placeholder="（可选）" maxlength="200" class="field-input" />
      </DialogPropCard>
      <DialogPropCard label="封面">
        <UiInput v-model="form.coverUrl" placeholder="图片 URL（可选）" class="field-input" />
      </DialogPropCard>
    </DialogPropGrid>

    <DialogPropGrid>
      <DialogPropCard label="阅读状态">
        <DialogChoiceRow v-model="form.status" :options="statusOptions" />
      </DialogPropCard>
      <DialogPropCard label="阅读进度">
        <div class="progress-row">
          <input
            type="range"
            :min="0"
            :max="100"
            :value="form.progress"
            class="progress-slider"
            :style="{ '--progress-color': progressColor }"
            @input="onProgressChange"
          />
          <span class="progress-value" :style="{ color: progressColor }">{{ form.progress }}%</span>
        </div>
        <div class="chapter-row">
          <div class="chapter-field">
            <span class="inline-label">总章节</span>
            <input
              type="number"
              :min="0"
              :value="form.totalChapters"
              class="number-input"
              @input="form.totalChapters = Number(($event.target as HTMLInputElement).value)"
            />
          </div>
          <div class="chapter-divider">/</div>
          <div class="chapter-field">
            <span class="inline-label">当前</span>
            <input
              type="number"
              :min="0"
              :max="form.totalChapters || 9999"
              :value="form.currentChapter"
              class="number-input"
              @input="form.currentChapter = Number(($event.target as HTMLInputElement).value)"
            />
          </div>
        </div>
      </DialogPropCard>
    </DialogPropGrid>

    <DialogPropGrid>
      <DialogPropCard label="评分 · 时长">
        <div class="rating-duration-row">
          <div class="rating-group">
            <button
              v-for="i in 5"
              :key="i"
              type="button"
              class="star-btn"
              :class="{ active: i <= (form.rating || 0) }"
              @click="toggleRating(i)"
            >
              <Star
                :size="20"
                :fill="i <= (form.rating || 0) ? 'var(--warning)' : 'none'"
                :color="i <= (form.rating || 0) ? 'var(--warning)' : 'var(--text-tertiary)'"
              />
            </button>
            <span v-if="!form.rating" class="rating-hint">点击评分</span>
          </div>
          <div class="duration-field">
            <span class="inline-label">时长（分钟）</span>
            <input
              type="number"
              :min="0"
              :step="10"
              :value="form.totalDuration"
              class="number-input"
              placeholder="0"
              @input="form.totalDuration = Number(($event.target as HTMLInputElement).value) || undefined"
            />
          </div>
        </div>
      </DialogPropCard>
      <DialogPropCard label="阅读时间">
        <div class="date-range-row">
          <DialogDateChip v-model="form.startDate" placeholder="开始日期" />
          <span class="date-range-arrow">→</span>
          <DialogDateChip v-model="form.endDate" placeholder="读完日期" />
        </div>
      </DialogPropCard>
    </DialogPropGrid>

    <DialogEditor
      v-model="form.notes"
      size="md"
      placeholder="记录读后感、摘抄、精彩段落…"
    />

    <template #footer>
      <DialogFooterActions :saving="saving" @cancel="emit('update:modelValue', false)" @confirm="handleSave" />
    </template>
  </UiDialog>
</template>

<style scoped>
.field-input :deep(input) {
  border: none !important;
  padding-left: 0 !important;
  background: transparent !important;
  font-size: var(--text-sm) !important;
}
.field-input :deep(input)::placeholder {
  color: var(--text-placeholder);
}

.inline-label {
  font-size: var(--text-xs);
  color: var(--text-tertiary);
  font-weight: 500;
}

.progress-row {
  display: flex;
  align-items: center;
  gap: var(--sp-3);
  margin-bottom: var(--sp-3);
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
  width: 18px;
  height: 18px;
  border-radius: 50%;
  background: var(--progress-color, var(--accent));
  border: 2px solid var(--bg-card);
  box-shadow: var(--shadow-sm);
  cursor: pointer;
  transition: transform var(--transition);
}
.progress-slider::-webkit-slider-thumb:hover {
  transform: scale(1.2);
}
.progress-value {
  font-size: var(--text-sm);
  font-weight: 600;
  min-width: 40px;
  text-align: right;
}

.chapter-row {
  display: flex;
  align-items: center;
  gap: var(--sp-2);
}

.chapter-field {
  display: flex;
  align-items: center;
  gap: var(--sp-2);
}

.chapter-divider {
  color: var(--text-tertiary);
  font-size: var(--text-lg);
  font-weight: 300;
}

.number-input {
  width: 72px;
  border: 1px solid var(--border-color);
  border-radius: var(--radius-sm);
  padding: 4px 8px;
  font-size: var(--text-sm);
  color: var(--text-primary);
  background: var(--bg-card);
  outline: none;
  text-align: center;
  transition: border-color var(--transition);
}
.number-input:focus {
  border-color: var(--accent);
}
.number-input::placeholder { color: var(--text-placeholder); }

.rating-duration-row {
  display: flex;
  align-items: center;
  gap: var(--sp-4);
  flex-wrap: wrap;
}

.rating-group {
  display: flex;
  align-items: center;
  gap: 2px;
}

.star-btn {
  background: none;
  border: none;
  cursor: pointer;
  padding: 2px;
  transition: transform var(--transition);
  display: flex;
}
.star-btn:hover { transform: scale(1.2); }
.star-btn.active { transform: scale(1.05); }

.rating-hint {
  font-size: var(--text-xs);
  color: var(--text-tertiary);
  margin-left: var(--sp-2);
}

.duration-field {
  display: flex;
  align-items: center;
  gap: var(--sp-2);
}

.date-range-row {
  display: flex;
  align-items: center;
  gap: var(--sp-2);
  flex-wrap: wrap;
}

.date-range-arrow {
  color: var(--text-tertiary);
  font-size: var(--text-sm);
}
</style>
