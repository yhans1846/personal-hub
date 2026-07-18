<script setup lang="ts">
import { ref, watch, computed, toRef } from 'vue'
import { createStudyPlan, updateStudyPlan, getStudyPlanById } from '@/modules/planning/api'
import { getTags } from '@/modules/knowledge/api'
import { ElMessage } from 'element-plus'
import { Link } from 'lucide-vue-next'
import {
  UiDialog,
  UiInput,
  DialogTitleField,
  DialogPropGrid,
  DialogPropCard,
  DialogChoiceRow,
  DialogChipRow,
  DialogDateChip,
  DialogEditor,
  DialogFooterActions,
} from '@/components/ui'
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

const tagOptions = computed(() => tags.value.map(t => ({ id: t.id, name: t.name, color: t.color })))

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
    <DialogTitleField
      v-model="form.name"
      placeholder="计划名称"
      maxlength="200"
    />

    <DialogPropGrid>
      <DialogPropCard label="来源">
        <UiInput v-model="form.source" placeholder="如 B站" maxlength="100" class="field-input" />
      </DialogPropCard>
      <DialogPropCard label="作者">
        <UiInput v-model="form.author" placeholder="（可选）" maxlength="100" class="field-input" />
      </DialogPropCard>
    </DialogPropGrid>

    <div class="url-row">
      <Link :size="14" class="url-icon" />
      <input
        v-model="form.url"
        class="url-input"
        placeholder="https:// 资源地址"
        maxlength="500"
      />
    </div>

    <DialogPropGrid>
      <DialogPropCard label="分类标签">
        <DialogChipRow
          v-model="form.tagIds"
          :options="tagOptions"
          multiple
          empty-text="暂无标签，可先在标签管理中创建"
        />
      </DialogPropCard>
      <DialogPropCard label="状态">
        <DialogChoiceRow v-model="form.status" :options="statusOptions" />
      </DialogPropCard>
    </DialogPropGrid>

    <DialogPropGrid>
      <DialogPropCard label="进度">
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
      </DialogPropCard>
      <DialogPropCard label="时间范围">
        <div class="date-range-row">
          <DialogDateChip v-model="form.startDate" placeholder="开始日期" />
          <span class="date-range-arrow">→</span>
          <DialogDateChip v-model="form.endDate" placeholder="结束日期" />
        </div>
      </DialogPropCard>
    </DialogPropGrid>

    <DialogEditor
      v-model="form.remark"
      size="md"
      placeholder="备注…"
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

.url-row {
  display: flex;
  align-items: center;
  gap: 6px;
  margin: calc(var(--sp-2) * -1) 0 var(--sp-4);
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

.progress-row {
  display: flex;
  align-items: center;
  gap: var(--sp-3);
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
  flex-wrap: wrap;
}
.date-range-arrow {
  color: var(--text-tertiary);
  font-size: var(--text-sm);
}
</style>
