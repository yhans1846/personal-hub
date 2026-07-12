<script setup lang="ts">
import { ref, watch, computed } from 'vue'
import { createStudyPlan, updateStudyPlan, getStudyPlanById } from '@/api/studyplanApi'
import { ElMessage } from 'element-plus'
import { Calendar } from 'lucide-vue-next'
import { UiDialog, UiInput, UiButton } from '@/components/ui'

const props = withDefaults(defineProps<{
  modelValue: boolean
  entityId?: number
}>(), { entityId: undefined })

const emit = defineEmits<{
  'update:modelValue': [value: boolean]
  'saved': []
}>()

const form = ref({
  name: '', goal: '', progress: 0,
  startDate: null as string | null, endDate: null as string | null, status: 0
})
const saving = ref(false)

const statusOptions = [
  { value: 0, label: '未开始', emoji: '📋' },
  { value: 1, label: '进行中', emoji: '📖' },
  { value: 2, label: '已完成', emoji: '✅' },
  { value: 3, label: '已放弃', emoji: '⏹️' }
]

const dialogTitle = computed(() => props.entityId ? '编辑计划' : '新建计划')
const dialogSubtitle = computed(() => props.entityId ? '' : '设定学习目标')

const progressColor = computed(() => {
  if (form.value.progress >= 100) return 'var(--success)'
  if (form.value.progress >= 50) return 'var(--accent)'
  return 'var(--warning)'
})

const currentStatus = computed(() => statusOptions.find(s => s.value === form.value.status))

watch(() => props.modelValue, async (val) => {
  if (!val) return
  if (props.entityId) {
    const r = (await getStudyPlanById(props.entityId)).data.data
    form.value = { name: r.name, goal: r.goal || '', progress: r.progress, startDate: r.startDate, endDate: r.endDate, status: r.status }
  } else {
    form.value = { name: '', goal: '', progress: 0, startDate: null, endDate: null, status: 0 }
  }
})

async function handleSave() {
  if (!form.value.name.trim()) { ElMessage.warning('请输入计划名称'); return }
  saving.value = true
  try {
    if (props.entityId) {
      await updateStudyPlan(props.entityId, form.value)
      ElMessage.success('已更新')
    } else {
      await createStudyPlan(form.value)
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
    :title="dialogTitle"
    :subtitle="dialogSubtitle"
    @update:model-value="emit('update:modelValue', $event)"
  >
    <!-- 计划名称 -->
    <UiInput
      v-model="form.name"
      placeholder="计划名称"
      class="title-input"
      maxlength="200"
    />

    <div class="section-divider" />

    <!-- 状态卡片 -->
    <div class="section-label">状态</div>
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

    <!-- 进度滑块 -->
    <div class="section-label">进度</div>
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

    <!-- 时间范围 -->
    <div class="section-label">时间范围</div>
    <div class="date-range-row">
      <button
        type="button"
        class="date-chip"
        :class="{ 'has-date': !!form.startDate }"
        @click="$refs.startInput?.showPicker ? $refs.startInput.showPicker() : $refs.startInput?.click()"
      >
        <Calendar :size="14" />
        <span>{{ form.startDate || '开始日期' }}</span>
        <input ref="startInput" type="date" class="date-input-abs" :value="form.startDate || ''" @change="form.startDate = ($event.target as HTMLInputElement).value || null" />
      </button>
      <span class="date-range-arrow">→</span>
      <button
        type="button"
        class="date-chip"
        :class="{ 'has-date': !!form.endDate }"
        @click="$refs.endInput?.showPicker ? $refs.endInput.showPicker() : $refs.endInput?.click()"
      >
        <Calendar :size="14" />
        <span>{{ form.endDate || '结束日期' }}</span>
        <input ref="endInput" type="date" class="date-input-abs" :value="form.endDate || ''" @change="form.endDate = ($event.target as HTMLInputElement).value || null" />
      </button>
    </div>

    <div class="section-divider" />

    <!-- 学习目标 -->
    <div class="content-section">
      <textarea
        v-model="form.goal"
        class="content-editor"
        placeholder="设定学习目标、计划安排或学习心得…"
      />
    </div>

    <template #footer>
      <el-button text @click="emit('update:modelValue', false)">取消</el-button>
      <UiButton type="primary" :loading="saving" @click="handleSave">保存</UiButton>
    </template>
  </UiDialog>
</template>

<style scoped>
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

.section-divider {
  height: 1px;
  background: var(--border-light);
  margin: var(--sp-4) 0;
}

.section-label {
  font-size: var(--text-xs);
  color: var(--text-tertiary);
  font-weight: 500;
  margin-bottom: var(--sp-3);
  letter-spacing: 0.3px;
  text-transform: uppercase;
}

/* ---- 状态卡片 ---- */
.status-row {
  display: flex;
  gap: var(--sp-2);
  margin-bottom: var(--sp-5);
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

.status-emoji { font-size: 14px; line-height: 1; }
.status-label {
  font-size: var(--text-sm);
  font-weight: 500;
  color: var(--text-secondary);
}
.status-card.active .status-label {
  color: var(--accent);
}

/* ---- 进度滑块 ---- */
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
.progress-slider::-webkit-slider-runnable-track {
  height: 6px;
  border-radius: 3px;
  background: linear-gradient(to right,
    var(--progress-color, var(--accent)) 0%,
    var(--progress-color, var(--accent)) var(--progress, 0%),
    var(--bg-hover) var(--progress, 0%)
  );
}

.progress-value {
  font-size: var(--text-sm);
  font-weight: 600;
  min-width: 40px;
  text-align: right;
}

/* ---- 日期范围 ---- */
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
