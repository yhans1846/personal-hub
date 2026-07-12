<script setup lang="ts">
import { ref, watch, computed } from 'vue'
import { createStudyRecord, updateStudyRecord, getStudyRecordById } from '@/api/studyApi'
import { ElMessage } from 'element-plus'
import { Clock, Calendar } from 'lucide-vue-next'
import { UiDialog, UiButton } from '@/components/ui'

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

watch(() => props.modelValue, async (val) => {
  if (!val) return
  if (props.entityId) {
    const r = (await getStudyRecordById(props.entityId)).data.data
    form.value = { subject: r.subject, date: r.date, duration: r.duration, content: r.content || '', reflection: r.reflection || '' }
  } else {
    form.value = {
      subject: '', date: new Date().toISOString().slice(0, 10),
      duration: 60, content: '', reflection: ''
    }
  }
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
    <!-- 学习主题 -->
    <input
      v-model="form.subject"
      class="study-title-input"
      placeholder="学习主题"
    />

    <div class="section-divider" />

    <!-- 日期 + 时长 — 并排 -->
    <div class="meta-duo">
      <div class="meta-group">
        <span class="meta-label">日期</span>
        <button type="button" class="meta-chip" @click="$refs.dateInput?.showPicker ? $refs.dateInput.showPicker() : $refs.dateInput?.click()">
          <Calendar :size="14" />
          <span>{{ form.date || '选择日期' }}</span>
          <input ref="dateInput" type="date" class="date-input-abs" :value="form.date" @change="form.date = ($event.target as HTMLInputElement).value" />
        </button>
      </div>
      <div class="meta-group">
        <span class="meta-label">时长</span>
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
      </div>
    </div>

    <div class="section-divider" />

    <!-- 学习内容 -->
    <div class="section-label">学习内容</div>
    <textarea
      v-model="form.content"
      class="content-editor"
      placeholder="记录今天学了什么…"
    />

    <div class="section-spacer" />

    <!-- 心得 -->
    <div class="section-label">心得</div>
    <textarea
      v-model="form.reflection"
      class="content-editor"
      placeholder="有什么收获或思考？"
    />

    <template #footer>
      <el-button text @click="emit('update:modelValue', false)">取消</el-button>
      <UiButton type="primary" :loading="saving" @click="handleSave">保存</UiButton>
    </template>
  </UiDialog>
</template>

<style scoped>
.study-title-input {
  width: 100%;
  font-size: var(--text-lg);
  font-weight: 600;
  border: none;
  outline: none;
  background: transparent;
  color: var(--text-primary);
  padding: 0;
  margin-bottom: var(--sp-2);
}
.study-title-input::placeholder {
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

.section-spacer {
  height: var(--sp-5);
}

/* ---- 元数据双栏 ---- */
.meta-duo {
  display: flex;
  gap: var(--sp-6);
  margin-bottom: var(--sp-2);
}

.meta-group {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.meta-label {
  font-size: var(--text-xs);
  color: var(--text-tertiary);
  font-weight: 500;
}

.meta-chip {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: var(--sp-2) var(--sp-4);
  border-radius: var(--radius-full);
  border: 1px solid var(--border-color);
  background: var(--bg-card);
  font-size: var(--text-sm);
  color: var(--text-primary);
  cursor: pointer;
  transition: all var(--transition);
  position: relative;
}
.meta-chip:hover {
  border-color: var(--accent-border);
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

.duration-group {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: var(--sp-2) var(--sp-4);
  border-radius: var(--radius-full);
  border: 1px solid var(--border-color);
  background: var(--bg-card);
}

.meta-icon {
  flex-shrink: 0;
  color: var(--text-tertiary);
}

.duration-input {
  width: 56px;
  border: none;
  outline: none;
  font-size: var(--text-sm);
  color: var(--text-primary);
  background: transparent;
  text-align: center;
}
.duration-input::placeholder { color: var(--text-placeholder); }

.duration-unit {
  font-size: var(--text-xs);
  color: var(--text-tertiary);
}

/* ---- 编辑器 ---- */
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
