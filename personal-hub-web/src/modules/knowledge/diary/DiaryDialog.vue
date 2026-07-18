<script setup lang="ts">
import { ref, watch, computed, toRef, onUnmounted, nextTick } from 'vue'
import {
  createDiary, updateDiary, getDiaryById,
  uploadDiaryImage, deleteDiaryImage,
} from '@/modules/knowledge/api'
import { getDiaryImagePreviewUrl, revokePreviewUrl } from '@/utils/file'
import { ElMessage } from 'element-plus'
import { ImagePlus, X, MapPin, LocateFixed, Loader2 } from 'lucide-vue-next'
import Sortable from 'sortablejs'
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
import ImageLightbox from '@/components/ImageLightbox.vue'
import { useEntityDialog, type EntityDialogEmit } from '@/composables/useEntityDialog'

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

interface ImageItem {
  name: string
  url: string
}

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
const saving = ref(false)
const locating = ref(false)
const uploading = ref(false)
const previewImages = ref<ImageItem[]>([])
const isDragging = ref(false)
const lightboxOpen = ref(false)
const lightboxIndex = ref(0)
const fileInputRef = ref<HTMLInputElement | null>(null)
const imageGridRef = ref<HTMLElement | null>(null)
const workingId = ref<number | undefined>()
let imageSortable: Sortable | null = null
let suppressLightboxClick = false

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

const canUploadImages = computed(() => !!workingId.value)
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
  previewImages.value.forEach(img => revokePreviewUrl(img.url))
  previewImages.value = []
  for (const name of form.value.imageFiles) {
    const url = await getDiaryImagePreviewUrl(id, name)
    previewImages.value.push({ name, url })
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
  previewImages.value.forEach(img => revokePreviewUrl(img.url))
  previewImages.value = []
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

async function handleSave() {
  saving.value = true
  try {
    const payload = { ...form.value }
    if (workingId.value) {
      await updateDiary(workingId.value, payload)
      ElMessage.success('已更新')
      onSaved()
    } else {
      const res = await createDiary(payload)
      const id = res.data.data.id
      workingId.value = id
      emit('created', id)
      ElMessage.success('已创建，可以添加配图')
    }
  } finally { saving.value = false }
}

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

async function handleImageUpload(files: FileList | File[]) {
  if (!workingId.value) {
    ElMessage.warning('请先保存日记，再添加配图')
    return
  }
  const diaryId = workingId.value
  const valid: File[] = []
  for (const f of files) {
    if (f.type.startsWith('image/')) valid.push(f)
    else ElMessage.warning(`"${f.name}" 不是图片，已跳过`)
  }
  if (!valid.length) return

  uploading.value = true
  try {
    for (const file of valid) {
      const res = await uploadDiaryImage(diaryId, file)
      const name = res.data.data.name
      form.value.imageFiles.push(name)
      const url = await getDiaryImagePreviewUrl(diaryId, name)
      previewImages.value.push({ name, url })
    }
  } finally { uploading.value = false }
}

function onFileChange(e: Event) {
  const files = (e.target as HTMLInputElement).files
  if (files?.length) {
    handleImageUpload(files)
    ;(e.target as HTMLInputElement).value = ''
  }
}

function openLightbox(idx: number) {
  if (suppressLightboxClick) return
  lightboxIndex.value = idx
  lightboxOpen.value = true
}

function destroyImageSortable() {
  imageSortable?.destroy()
  imageSortable = null
}

async function initImageSortable() {
  await nextTick()
  destroyImageSortable()
  if (!imageGridRef.value || previewImages.value.length < 2 || !workingId.value) return
  const diaryId = workingId.value
  imageSortable = Sortable.create(imageGridRef.value, {
    animation: 200,
    easing: 'cubic-bezier(0.25, 0.46, 0.45, 0.94)',
    ghostClass: 'image-sortable-ghost',
    chosenClass: 'image-sortable-chosen',
    dragClass: 'image-sortable-drag',
    filter: '.image-remove-btn',
    preventOnFilter: true,
    onStart() {
      suppressLightboxClick = true
    },
    async onEnd(evt) {
      const { oldIndex, newIndex } = evt
      window.setTimeout(() => { suppressLightboxClick = false }, 0)
      if (oldIndex == null || newIndex == null || oldIndex === newIndex) return
      const list = [...previewImages.value]
      const [item] = list.splice(oldIndex, 1)
      list.splice(newIndex, 0, item)
      previewImages.value = list
      form.value.imageFiles = list.map(i => i.name)
      try {
        await updateDiary(diaryId, { ...form.value, imageFiles: form.value.imageFiles })
      } catch {
        ElMessage.warning('顺序保存失败，请再试一次')
      }
    },
  })
}

watch(() => previewImages.value.map(i => i.name).join(','), () => {
  initImageSortable()
})

async function handleRemoveImage(img: ImageItem) {
  if (!workingId.value) return
  try {
    await deleteDiaryImage(workingId.value, img.name)
  } catch {
    // ignore
  }
  revokePreviewUrl(img.url)
  form.value.imageFiles = form.value.imageFiles.filter(n => n !== img.name)
  previewImages.value = previewImages.value.filter(p => p.name !== img.name)
}

function onDragOver(e: DragEvent) {
  e.preventDefault()
  isDragging.value = true
}

function onDragLeave() {
  isDragging.value = false
}

function onDrop(e: DragEvent) {
  e.preventDefault()
  isDragging.value = false
  const files = e.dataTransfer?.files
  if (files?.length) handleImageUpload(files)
}

onUnmounted(() => {
  destroyImageSortable()
  previewImages.value.forEach(img => revokePreviewUrl(img.url))
})
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
            <button
              v-for="w in weatherOptions"
              :key="w.label"
              type="button"
              class="weather-btn"
              :class="{ active: form.weather === w.emoji }"
              :title="w.label"
              @click="form.weather = form.weather === w.emoji ? '' : w.emoji"
            >
              {{ w.emoji }}
            </button>
          </div>
          <div class="location-block">
            <div class="location-input-wrapper">
              <MapPin :size="14" class="location-icon" />
              <input
                v-model="form.location"
                class="location-input"
                placeholder="添加地点（可选）"
                maxlength="200"
              />
            </div>
            <button
              type="button"
              class="locate-btn"
              :disabled="locating"
              :title="hasCoords ? '重新定位' : '使用当前位置'"
              @click="locateHere"
            >
              <Loader2 v-if="locating" :size="14" class="spin" />
              <LocateFixed v-else :size="14" />
              <span>{{ locating ? '定位中' : '定位' }}</span>
            </button>
          </div>
          <div v-if="hasCoords" class="coords-row">
            <span class="coords-pill">已定位 · {{ coordsLabel }}</span>
            <button type="button" class="coords-clear" @click="clearCoords">清除坐标</button>
          </div>
        </div>
      </DialogPropCard>

      <DialogPropCard label="配图">
        <p v-if="!canUploadImages" class="image-need-save">请先保存日记，再添加配图</p>
        <div v-if="previewImages.length" ref="imageGridRef" class="image-grid">
          <div
            v-for="(img, idx) in previewImages"
            :key="img.name"
            class="image-item"
            :title="previewImages.length > 1 ? '拖拽调整顺序' : undefined"
          >
            <img
              :src="img.url"
              class="preview-img"
              alt=""
              draggable="false"
              @click="openLightbox(idx)"
            />
            <button
              type="button"
              class="image-remove-btn"
              :disabled="uploading"
              @click.stop="handleRemoveImage(img)"
            >
              <X :size="16" />
            </button>
          </div>
        </div>
        <p v-if="previewImages.length > 1" class="image-sort-hint">拖拽缩略图可调整顺序，首张将作为封面</p>

        <ImageLightbox
          v-model="lightboxOpen"
          v-model:index="lightboxIndex"
          :urls="previewImages.map(i => i.url)"
        />

        <div
          class="dropzone"
          :class="{ 'dropzone--dragging': isDragging, 'dropzone--disabled': !canUploadImages }"
          @dragover="canUploadImages && onDragOver($event)"
          @dragleave="onDragLeave"
          @drop="canUploadImages && onDrop($event)"
          @click="canUploadImages && fileInputRef?.click()"
        >
          <ImagePlus :size="28" class="dropzone-icon" />
          <span class="dropzone-text">{{ canUploadImages ? '拖拽图片到这里，或点击上传' : '保存后可上传配图' }}</span>
          <span class="dropzone-hint">支持 JPG、PNG、WEBP，可多选</span>
          <input
            ref="fileInputRef"
            type="file"
            accept="image/*"
            multiple
            class="dropzone-input"
            :disabled="!canUploadImages"
            @change="onFileChange"
          />
        </div>
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

.weather-group {
  display: flex;
  gap: 4px;
}

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
.location-input::placeholder {
  color: var(--text-placeholder);
}
.location-input:focus {
  color: var(--text-primary);
}

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
.locate-btn:disabled {
  opacity: 0.65;
  cursor: wait;
}

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
.coords-clear:hover {
  color: var(--danger);
}

.spin {
  animation: locate-spin 0.8s linear infinite;
}
@keyframes locate-spin {
  to { transform: rotate(360deg); }
}

.image-grid {
  display: flex;
  flex-wrap: wrap;
  gap: var(--sp-3);
  margin-bottom: var(--sp-2);
}

.image-item {
  position: relative;
  flex: 0 0 auto;
  cursor: grab;
  touch-action: none;
}
.image-item:active {
  cursor: grabbing;
}

.image-sort-hint {
  margin: 0 0 var(--sp-3);
  font-size: var(--text-xs);
  color: var(--text-tertiary);
}

.image-need-save {
  margin: 0 0 var(--sp-3);
  font-size: var(--text-xs);
  color: var(--warning);
}

.image-sortable-ghost {
  opacity: 0.35;
}
.image-sortable-chosen .preview-img {
  box-shadow: 0 0 0 2px var(--accent);
}
.image-sortable-drag {
  opacity: 1;
  cursor: grabbing;
}

.preview-img {
  width: 96px;
  height: 96px;
  border-radius: var(--radius-md);
  object-fit: cover;
  border: 1px solid var(--border-color);
  cursor: zoom-in;
  display: block;
}

.image-remove-btn {
  position: absolute;
  top: 4px;
  right: 4px;
  width: 24px;
  height: 24px;
  border-radius: 50%;
  background: rgba(0, 0, 0, 0.55);
  color: #fff;
  border: none;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: background var(--transition);
}
.image-remove-btn:hover {
  background: rgba(0, 0, 0, 0.75);
}

.dropzone {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 6px;
  padding: var(--sp-6);
  border: 2px dashed var(--border-color);
  border-radius: var(--radius-md);
  cursor: pointer;
  transition: all var(--transition);
}
.dropzone:hover,
.dropzone--dragging {
  border-color: var(--accent);
  background: var(--accent-light);
}
.dropzone--disabled {
  opacity: 0.55;
  cursor: not-allowed;
}
.dropzone--disabled:hover {
  border-color: var(--border-color);
  background: transparent;
}

.dropzone-icon {
  color: var(--text-tertiary);
}
.dropzone:hover .dropzone-icon,
.dropzone--dragging .dropzone-icon {
  color: var(--accent);
}

.dropzone-text {
  font-size: var(--text-sm);
  color: var(--text-secondary);
}
.dropzone-hint {
  font-size: var(--text-xs);
  color: var(--text-tertiary);
}
.dropzone-input {
  display: none;
}
</style>
