<script setup lang="ts">
import { ref, watch, computed, toRef } from 'vue'
import {
  createDiary, updateDiary, getDiaryById,
} from '@/modules/knowledge/api'
import { unwrapResult } from '@/utils/apiResult'
import { ElMessage } from 'element-plus'
import { MapPin, LocateFixed, Loader2 } from 'lucide-vue-next'
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
import { useEntityDialog, useEntityFormSave, type EntityDialogEmit } from '@/composables/useEntityDialog'
import DiaryImagePanel from './DiaryImagePanel.vue'
import UiTooltip from '@/components/UiTooltip.vue'

const props = withDefaults(defineProps<{
  modelValue: boolean
  entityId?: number
  initialDate?: string
}>(), { entityId: undefined, initialDate: '' })

const emit = defineEmits<{
  'update:modelValue': [value: boolean]
  'saved': []
  'created': [id: number]
}>()

const form = ref({
  date: '',
  title: '',
  content: '',
  mood: 3,
  weather: '',
  location: '',
  latitude: null as number | null,
  longitude: null as number | null,
  imageFiles: [] as string[],
})
const locating = ref(false)
const workingId = ref<number | undefined>()

const moodOptions = [
  { value: 1, label: '很好', emoji: '😊', color: 'var(--success)' },
  { value: 2, label: '不错', emoji: '😄', color: 'var(--success)' },
  { value: 3, label: '一般', emoji: '😐', color: 'var(--text-tertiary)' },
  { value: 4, label: '难过', emoji: '😔', color: 'var(--warning)' },
  { value: 5, label: '糟糕', emoji: '😭', color: 'var(--danger)' },
]

const weatherOptions = [
  { emoji: '☀️', label: '晴' },
  { emoji: '🌤️', label: '多云' },
  { emoji: '☁️', label: '阴' },
  { emoji: '🌧️', label: '雨' },
  { emoji: '⛈️', label: '雷阵雨' },
  { emoji: '❄️', label: '雪' },
]

const dialogTitle = computed(() => workingId.value ? '编辑日记' : '今天')
const dialogSubtitle = computed(() => workingId.value ? '' : '记录今天发生的事情')

const dateModel = computed({
  get: () => form.value.date || null,
  set: (v: string | null) => { form.value.date = v || '' },
})

async function loadEntity(id: number) {
  workingId.value = id
  const res = await getDiaryById(id)
  const r = res.data.data
  form.value = {
    date: r.date,
    title: r.title || '',
    content: r.content || '',
    mood: r.mood || 3,
    weather: r.weather || '',
    location: r.location || '',
    latitude: r.latitude ?? null,
    longitude: r.longitude ?? null,
    imageFiles: r.imageFiles || [],
  }
}

watch(() => props.modelValue, (val) => {
  if (!val) {
    workingId.value = undefined
    return
  }
  if (props.entityId) return
  workingId.value = undefined
  const now = new Date()
  const today = `${now.getFullYear()}-${String(now.getMonth() + 1).padStart(2, '0')}-${String(now.getDate()).padStart(2, '0')}`
  form.value = {
    date: props.initialDate || today,
    title: '', content: '', mood: 3, weather: '', location: '',
    latitude: null, longitude: null, imageFiles: [],
  }
})

watch(() => props.entityId, (id) => {
  if (id) workingId.value = id
})

const dialogEmit: EntityDialogEmit = ((event: 'update:modelValue' | 'saved', value?: boolean) => {
  if (event === 'update:modelValue') emit('update:modelValue', value as boolean)
  else emit('saved')
}) as EntityDialogEmit

const { onSaved } = useEntityDialog({
  modelValue: toRef(props, 'modelValue'),
  entityId: toRef(props, 'entityId'),
  emit: dialogEmit,
  loadEntity,
})

const { saving, handleSave } = useEntityFormSave({
  entityId: workingId,
  validate: () => null,
  create: async () => {
    const created = await unwrapResult(createDiary({ ...form.value }))
    workingId.value = created.id
    emit('created', created.id)
  },
  update: (id) => updateDiary(id, { ...form.value }).then(() => undefined),
  onSaved,
  createSuccessMessage: '已创建，可以添加配图',
  updateSuccessMessage: '已更新',
  errorMessage: '保存失败',
  invokeOnSavedAfterCreate: false,
})

const hasCoords = computed(() =>
  form.value.latitude != null && form.value.longitude != null
)

const coordsLabel = computed(() => {
  if (!hasCoords.value) return ''
  return `${form.value.latitude!.toFixed(5)}, ${form.value.longitude!.toFixed(5)}`
})

function clearCoords() {
  form.value.latitude = null
  form.value.longitude = null
}

function locateHere() {
  if (!navigator.geolocation) {
    ElMessage.warning('当前浏览器不支持定位')
    return
  }
  locating.value = true
  navigator.geolocation.getCurrentPosition(
    (pos) => {
      form.value.latitude = Number(pos.coords.latitude.toFixed(7))
      form.value.longitude = Number(pos.coords.longitude.toFixed(7))
      locating.value = false
      ElMessage.success('已获取当前位置')
    },
    (err) => {
      locating.value = false
      if (err.code === err.PERMISSION_DENIED) {
        ElMessage.warning('定位权限被拒绝，请在浏览器设置中允许')
      } else if (err.code === err.POSITION_UNAVAILABLE) {
        ElMessage.warning('暂时无法获取位置')
      } else if (err.code === err.TIMEOUT) {
        ElMessage.warning('定位超时，请重试')
      } else {
        ElMessage.warning('定位失败')
      }
    },
    { enableHighAccuracy: true, timeout: 12000, maximumAge: 60_000 },
  )
}
</script>

<template>
  <UiDialog
    :model-value="modelValue"
    :title="dialogTitle"
    :subtitle="dialogSubtitle"
    @update:model-value="emit('update:modelValue', $event)"
  >
    <DialogPropGrid>
      <DialogPropCard label="日期">
        <DialogDateChip v-model="dateModel" placeholder="选择日期" :clearable="false" />
      </DialogPropCard>
      <DialogPropCard label="此刻心情">
        <DialogChoiceRow v-model="form.mood" :options="moodOptions" />
      </DialogPropCard>
    </DialogPropGrid>

    <DialogTitleField
      v-model="form.title"
      placeholder="今天发生了什么？（可选的标题）"
      maxlength="200"
    />

    <DialogPropGrid :cols="1">
      <DialogPropCard label="天气 · 地点">
        <div class="meta-row">
          <div class="weather-group">
            <UiTooltip v-for="w in weatherOptions" :key="w.label" :content="w.label">
              <button
                type="button"
                class="weather-btn"
                :class="{ active: form.weather === w.emoji }"
                @click="form.weather = form.weather === w.emoji ? '' : w.emoji"
              >
                {{ w.emoji }}
              </button>
            </UiTooltip>
          </div>
          <div class="location-block">
            <div class="location-input-wrapper">
              <MapPin :size="14" class="location-icon" />
              <input
                v-model="form.location"
                class="location-input"
                placeholder="添加地点（可选）"
                maxlength="200"
              >
            </div>
            <UiTooltip :content="hasCoords ? '重新定位' : '使用当前位置'">
              <button
                type="button"
                class="locate-btn"
                :disabled="locating"
                @click="locateHere"
              >
                <Loader2 v-if="locating" :size="14" class="spin" />
                <LocateFixed v-else :size="14" />
                <span>{{ locating ? '定位中' : '定位' }}</span>
              </button>
            </UiTooltip>
          </div>
          <div v-if="hasCoords" class="coords-row">
            <span class="coords-pill">已定位 · {{ coordsLabel }}</span>
            <button type="button" class="coords-clear" @click="clearCoords">清除坐标</button>
          </div>
        </div>
      </DialogPropCard>

      <DialogPropCard label="配图">
        <DiaryImagePanel v-model:image-files="form.imageFiles" :diary-id="workingId" />
      </DialogPropCard>
    </DialogPropGrid>

    <DialogEditor
      v-model="form.content"
      size="lg"
      placeholder="开始写吧… 支持 Markdown 格式"
    />

    <template #footer>
      <DialogFooterActions :saving="saving" @cancel="emit('update:modelValue', false)" @confirm="handleSave" />
    </template>
  </UiDialog>
</template>

<style scoped>
.meta-row {
  display: flex;
  align-items: center;
  gap: var(--sp-4);
  flex-wrap: wrap;
}
.weather-group { display: flex; gap: 4px; }
.weather-btn {
  width: 36px;
  height: 36px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: var(--radius-sm);
  border: 1px solid transparent;
  background: var(--bg-hover);
  font-size: 18px;
  cursor: pointer;
  transition: all var(--transition);
  line-height: 1;
}
.weather-btn:hover {
  border-color: var(--border-color);
  background: var(--bg-card);
}
.weather-btn.active {
  border-color: var(--accent);
  background: var(--accent-light);
}
.location-block {
  display: flex;
  align-items: center;
  gap: var(--sp-2);
  flex: 1;
  min-width: 180px;
}
.location-input-wrapper {
  display: flex;
  align-items: center;
  gap: 4px;
  flex: 1;
  min-width: 0;
  padding: 4px 10px;
  border-radius: var(--radius-sm);
  border: 1px solid var(--border-color, var(--border));
  background: var(--bg-hover);
}
.location-icon {
  flex-shrink: 0;
  color: var(--text-tertiary);
}
.location-input {
  border: none;
  background: transparent;
  font-size: var(--text-sm);
  color: var(--text-secondary);
  outline: none;
  padding: 4px 0;
  min-width: 0;
  width: 100%;
}
.location-input::placeholder { color: var(--text-placeholder); }
.location-input:focus { color: var(--text-primary); }
.locate-btn {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  flex-shrink: 0;
  padding: 6px 12px;
  border-radius: var(--radius-sm);
  border: 1px solid var(--border-color, var(--border));
  background: var(--bg-card);
  font-size: var(--text-xs);
  color: var(--text-secondary);
  cursor: pointer;
  transition: all var(--transition);
}
.locate-btn:hover:not(:disabled) {
  border-color: var(--accent-border, var(--accent));
  color: var(--accent);
  background: var(--accent-light);
}
.locate-btn:disabled { opacity: 0.65; cursor: wait; }
.coords-row {
  display: flex;
  align-items: center;
  gap: var(--sp-2);
  width: 100%;
}
.coords-pill {
  font-size: var(--text-xs);
  color: var(--accent);
  background: var(--accent-light);
  padding: 2px 10px;
  border-radius: var(--radius-sm);
}
.coords-clear {
  border: none;
  background: transparent;
  font-size: var(--text-xs);
  color: var(--text-tertiary);
  cursor: pointer;
  padding: 0;
}
.coords-clear:hover { color: var(--danger); }
.spin { animation: locate-spin 0.8s linear infinite; }
@keyframes locate-spin {
  to { transform: rotate(360deg); }
}
</style>
