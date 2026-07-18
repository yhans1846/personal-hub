<script setup lang="ts">
import { ref, watch, computed, toRef, onUnmounted } from 'vue'
import { createDiary, updateDiary, getDiaryById } from '@/modules/knowledge/api'
import { uploadFile, deleteFile } from '@/modules/resource/api'
import { getFilePreviewUrl, revokePreviewUrl } from '@/utils/file'
import { ElMessage } from 'element-plus'
import { ImagePlus, X, MapPin } from 'lucide-vue-next'
import { UiDialog, UiInput, DialogSection, DialogDivider, DialogFooterActions } from '@/components/ui'
import ImageLightbox from '@/components/ImageLightbox.vue'
import { useEntityDialog } from '@/composables/useEntityDialog'

const props = withDefaults(defineProps<{
  modelValue: boolean
  entityId?: number
  initialDate?: string
}>(), { entityId: undefined, initialDate: '' })

const emit = defineEmits<{
  'update:modelValue': [value: boolean]
  'saved': []
}>()

interface ImageItem {
  id: number
  url: string
}

const form = ref({
  date: '',
  title: '',
  content: '',
  mood: 3,
  weather: '',
  location: '',
  imageFileIds: [] as number[],
})
const saving = ref(false)
const uploading = ref(false)
const previewImages = ref<ImageItem[]>([])
const isDragging = ref(false)
const lightboxOpen = ref(false)
const lightboxIndex = ref(0)

const moodOptions = [
  { value: 1, emoji: '😊', label: '很好' },
  { value: 2, emoji: '😄', label: '不错' },
  { value: 3, emoji: '😐', label: '一般' },
  { value: 4, emoji: '😔', label: '难过' },
  { value: 5, emoji: '😭', label: '糟糕' },
]

const weatherOptions = [
  { emoji: '☀️', label: '晴' },
  { emoji: '🌤️', label: '多云' },
  { emoji: '☁️', label: '阴' },
  { emoji: '🌧️', label: '雨' },
  { emoji: '⛈️', label: '雷阵雨' },
  { emoji: '❄️', label: '雪' },
]

const dialogTitle = computed(() => props.entityId ? '编辑日记' : '今天')
const dialogSubtitle = computed(() => props.entityId ? '' : '记录今天发生的事情')

const selectedMood = computed(() => moodOptions.find(m => m.value === form.value.mood))

async function loadEntity(id: number) {
  const res = await getDiaryById(id)
  const r = res.data.data
  form.value = {
    date: r.date,
    title: r.title || '',
    content: r.content || '',
    mood: r.mood || 3,
    weather: r.weather || '',
    location: r.location || '',
    imageFileIds: r.imageFileIds || [],
  }
  // 清理旧 blob URL
  previewImages.value.forEach(img => revokePreviewUrl(img.url))
  // 加载已有图片预览
  previewImages.value = []
  for (const fileId of form.value.imageFileIds) {
    const url = await getFilePreviewUrl(fileId)
    previewImages.value.push({ id: fileId, url })
  }
}

watch(() => props.modelValue, (val) => {
  if (!val || props.entityId) return
  const now = new Date()
  const today = `${now.getFullYear()}-${String(now.getMonth() + 1).padStart(2, '0')}-${String(now.getDate()).padStart(2, '0')}`
  form.value = {
    date: props.initialDate || today,
    title: '', content: '', mood: 3, weather: '', location: '', imageFileIds: [],
  }
  // 清理旧 blob URL
  previewImages.value.forEach(img => revokePreviewUrl(img.url))
  previewImages.value = []
})

const { loading, close, onSaved } = useEntityDialog({
  modelValue: toRef(props, 'modelValue'),
  entityId: toRef(props, 'entityId'),
  emit: (event, value) => emit(event as any, value),
  loadEntity,
})

async function handleSave() {
  saving.value = true
  try {
    if (props.entityId) {
      await updateDiary(props.entityId, form.value)
      ElMessage.success('已更新')
    } else {
      await createDiary(form.value)
      ElMessage.success('已创建')
    }
    onSaved()
  } finally { saving.value = false }
}

// ---- Image Upload ----
async function handleImageUpload(files: FileList | File[]) {
  const valid: File[] = []
  for (const f of files) {
    if (f.type.startsWith('image/')) valid.push(f)
    else ElMessage.warning(`"${f.name}" 不是图片，已跳过`)
  }
  if (!valid.length) return

  uploading.value = true
  try {
    for (const file of valid) {
      const res = await uploadFile(file)
      const fileId = res.data.data.id
      form.value.imageFileIds.push(fileId)
      const url = await getFilePreviewUrl(fileId)
      previewImages.value.push({ id: fileId, url })
    }
  } finally { uploading.value = false }
}

function onFileChange(e: Event) {
  const files = (e.target as HTMLInputElement).files
  if (files?.length) {
    handleImageUpload(files)
    // 清空 input 以便重复选择同一个文件
    ;(e.target as HTMLInputElement).value = ''
  }
}

function openLightbox(idx: number) {
  lightboxIndex.value = idx
  lightboxOpen.value = true
}

/** 删除单张图片（调用后端删除 + 清理本地状态） */
async function handleRemoveImage(img: ImageItem) {
  try {
    await deleteFile(img.id)
  } catch {
    // 后端删除失败不阻断本地清理
  }
  revokePreviewUrl(img.url)
  form.value.imageFileIds = form.value.imageFileIds.filter(id => id !== img.id)
  previewImages.value = previewImages.value.filter(p => p.id !== img.id)
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

/** 组件卸载时清理所有 blob URL */
onUnmounted(() => {
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
    <!-- 日期 + 元数据行 -->
    <div class="entry-meta">
      <span class="entry-date">📅 {{ form.date }}</span>
      <span v-if="selectedMood" class="entry-mood-chip">
        {{ selectedMood.emoji }} {{ selectedMood.label }}
      </span>
      <span v-if="form.weather" class="entry-weather-chip">
        {{ form.weather }}
      </span>
    </div>

    <!-- 标题 -->
    <UiInput
      v-model="form.title"
      placeholder="今天发生了什么？（可选的标题）"
      class="title-input"
      maxlength="200"
    />

    <DialogDivider />

    <DialogSection label="此刻心情">
      <div class="mood-row">
        <button
          v-for="m in moodOptions"
          :key="m.value"
          type="button"
          class="mood-card"
          :class="{ active: form.mood === m.value }"
          @click="form.mood = m.value"
        >
          <span class="mood-emoji">{{ m.emoji }}</span>
          <span class="mood-label">{{ m.label }}</span>
        </button>
      </div>
    </DialogSection>

    <DialogSection label="天气 · 地点">
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
        <div class="location-input-wrapper">
          <MapPin :size="14" class="location-icon" />
          <input
            v-model="form.location"
            class="location-input"
            placeholder="添加地点"
            maxlength="200"
          />
        </div>
      </div>
    </DialogSection>

    <DialogSection label="配图">
      <div v-if="previewImages.length" class="image-grid">
        <div v-for="(img, idx) in previewImages" :key="img.id" class="image-item">
          <img
            :src="img.url"
            class="preview-img"
            alt=""
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

      <ImageLightbox
        v-model="lightboxOpen"
        v-model:index="lightboxIndex"
        :urls="previewImages.map(i => i.url)"
      />

      <div
        class="dropzone"
        :class="{ 'dropzone--dragging': isDragging }"
        @dragover="onDragOver"
        @dragleave="onDragLeave"
        @drop="onDrop"
        @click="$refs.fileInput?.click()"
      >
        <ImagePlus :size="28" class="dropzone-icon" />
        <span class="dropzone-text">拖拽图片到这里，或点击上传</span>
        <span class="dropzone-hint">支持 JPG、PNG、WEBP，可多选</span>
        <input
          ref="fileInput"
          type="file"
          accept="image/*"
          multiple
          class="dropzone-input"
          @change="onFileChange"
        />
      </div>
    </DialogSection>

    <DialogDivider />

    <div class="content-section">
      <textarea
        v-model="form.content"
        class="content-editor"
        placeholder="开始写吧… 支持 Markdown 格式"
      />
    </div>

    <template #footer>
      <DialogFooterActions :saving="saving" @cancel="emit('update:modelValue', false)" @confirm="handleSave" />
    </template>
  </UiDialog>
</template>

<style scoped>
/* ---- 日期元数据行 ---- */
.entry-meta {
  display: flex;
  align-items: center;
  gap: var(--sp-2);
  margin-bottom: var(--sp-5);
  flex-wrap: wrap;
}

.entry-date {
  font-size: var(--text-sm);
  color: var(--text-secondary);
  font-weight: 500;
}

.entry-mood-chip,
.entry-weather-chip {
  font-size: var(--text-xs);
  padding: 2px 10px;
  border-radius: var(--radius-full);
  background: var(--bg-hover);
  color: var(--text-secondary);
}

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

/* ---- 心情卡片 ---- */
.mood-row {
  display: flex;
  gap: var(--sp-2);
}

.mood-card {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 4px;
  padding: var(--sp-3) var(--sp-4);
  border-radius: var(--radius-md);
  border: 2px solid transparent;
  background: var(--bg-hover);
  cursor: pointer;
  transition: all var(--transition);
  min-width: 56px;
}
.mood-card:hover {
  border-color: var(--border-color);
  background: var(--bg-card);
}
.mood-card.active {
  border-color: var(--success);
  background: var(--success-light);
}

.mood-emoji {
  font-size: 24px;
  line-height: 1;
}
.mood-label {
  font-size: var(--text-xs);
  color: var(--text-tertiary);
  font-weight: 500;
}
.mood-card.active .mood-label {
  color: var(--success);
}

/* ---- 天气 + 地点 ---- */
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

.location-input-wrapper {
  display: flex;
  align-items: center;
  gap: 4px;
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
  min-width: 100px;
  width: auto;
}
.location-input::placeholder {
  color: var(--text-placeholder);
}
.location-input:focus {
  color: var(--text-primary);
}

/* ---- 图片网格 ---- */
.image-grid {
  display: flex;
  flex-wrap: wrap;
  gap: var(--sp-3);
  margin-bottom: var(--sp-4);
}

.image-item {
  position: relative;
  flex: 0 0 auto;
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

/* ---- DropZone ---- */
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
  margin-bottom: var(--sp-5);
}
.dropzone:hover,
.dropzone--dragging {
  border-color: var(--accent);
  background: var(--accent-light);
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

/* ---- 正文编辑器 ---- */
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
