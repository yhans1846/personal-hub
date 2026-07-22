<script setup lang="ts">
import { ref, watch, computed, onUnmounted, nextTick } from 'vue'
import { uploadDiaryImage, deleteDiaryImage, updateDiary } from '@/modules/knowledge/api'
import { getDiaryImagePreviewUrl, revokePreviewUrl } from '@/utils/file'
import { ElMessage } from 'element-plus'
import { ImagePlus, X } from 'lucide-vue-next'
import Sortable from 'sortablejs'
import ImageLightbox from '@/components/ImageLightbox.vue'
import UiTooltip from '@/components/UiTooltip.vue'

const props = defineProps<{
  diaryId?: number
}>()

const imageFiles = defineModel<string[]>('imageFiles', { required: true })

interface ImageItem {
  name: string
  url: string
}

const uploading = ref(false)
const isDragging = ref(false)
const lightboxOpen = ref(false)
const lightboxIndex = ref(0)
const fileInputRef = ref<HTMLInputElement | null>(null)
const imageGridRef = ref<HTMLElement | null>(null)
const previewImages = ref<ImageItem[]>([])
let imageSortable: Sortable | null = null
let suppressLightboxClick = false

const canUpload = computed(() => !!props.diaryId)

watch(
  () => props.diaryId,
  async (id) => {
    previewImages.value.forEach(img => revokePreviewUrl(img.url))
    previewImages.value = []
    if (!id || !imageFiles.value.length) return
    for (const name of imageFiles.value) {
      const url = await getDiaryImagePreviewUrl(id, name)
      previewImages.value.push({ name, url })
    }
  },
  { immediate: true },
)

async function handleImageUpload(files: FileList | File[]) {
  if (!props.diaryId) {
    ElMessage.warning('请先保存日记，再添加配图')
    return
  }
  const diaryId = props.diaryId
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
      imageFiles.value = [...imageFiles.value, name]
      const url = await getDiaryImagePreviewUrl(diaryId, name)
      previewImages.value.push({ name, url })
    }
  } finally {
    uploading.value = false
  }
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
  if (!imageGridRef.value || previewImages.value.length < 2 || !props.diaryId) return
  const diaryId = props.diaryId
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
      if (!item) return
      list.splice(newIndex, 0, item)
      previewImages.value = list
      imageFiles.value = list.map(i => i.name)
      try {
        await updateDiary(diaryId, { imageFiles: imageFiles.value })
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
  if (!props.diaryId) return
  try {
    await deleteDiaryImage(props.diaryId, img.name)
  } catch {
    /* ignore */
  }
  revokePreviewUrl(img.url)
  imageFiles.value = imageFiles.value.filter(n => n !== img.name)
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
  <div class="diary-image-panel">
    <p v-if="!canUpload" class="image-need-save">请先保存日记，再添加配图</p>
    <div v-if="previewImages.length" ref="imageGridRef" class="image-grid">
      <UiTooltip
        v-for="(img, idx) in previewImages"
        :key="img.name"
        content="拖拽调整顺序"
        :disabled="previewImages.length <= 1"
      >
        <div class="image-item">
          <img
            :src="img.url"
            class="preview-img"
            alt=""
            draggable="false"
            @click="openLightbox(idx)"
          >
          <button
            type="button"
            class="image-remove-btn"
            :disabled="uploading"
            @click.stop="handleRemoveImage(img)"
          >
            <X :size="16" />
          </button>
        </div>
      </UiTooltip>
    </div>
    <p v-if="previewImages.length > 1" class="image-sort-hint">拖拽缩略图可调整顺序，首张将作为封面</p>

    <ImageLightbox
      v-model="lightboxOpen"
      v-model:index="lightboxIndex"
      :urls="previewImages.map(i => i.url)"
    />

    <div
      class="dropzone"
      :class="{ 'dropzone--dragging': isDragging, 'dropzone--disabled': !canUpload }"
      @dragover="canUpload && onDragOver($event)"
      @dragleave="onDragLeave"
      @drop="canUpload && onDrop($event)"
      @click="canUpload && fileInputRef?.click()"
    >
      <ImagePlus :size="28" class="dropzone-icon" />
      <span class="dropzone-text">{{ canUpload ? '拖拽图片到这里，或点击上传' : '保存后可上传配图' }}</span>
      <span class="dropzone-hint">支持 JPG、PNG、WEBP，可多选</span>
      <input
        ref="fileInputRef"
        type="file"
        accept="image/*"
        multiple
        class="dropzone-input"
        :disabled="!canUpload"
        @change="onFileChange"
      >
    </div>
  </div>
</template>

<style scoped>
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
.image-item:active { cursor: grabbing; }
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
.image-sortable-ghost { opacity: 0.35; }
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
.image-remove-btn:hover { background: rgba(0, 0, 0, 0.75); }
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
.dropzone-icon { color: var(--text-tertiary); }
.dropzone:hover .dropzone-icon,
.dropzone--dragging .dropzone-icon { color: var(--accent); }
.dropzone-text {
  font-size: var(--text-sm);
  color: var(--text-secondary);
}
.dropzone-hint {
  font-size: var(--text-xs);
  color: var(--text-tertiary);
}
.dropzone-input { display: none; }
</style>
