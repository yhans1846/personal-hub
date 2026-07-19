<script setup lang="ts">
import { ref, watch, computed, nextTick, onUnmounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Download, FileWarning } from 'lucide-vue-next'
import Vditor from 'vditor'
import 'vditor/dist/index.css'
import {
  UiDialog,
  UiButton,
} from '@/components/ui'
import ImageLightbox from '@/components/ImageLightbox.vue'
import {
  getFilePreviewUrl,
  getFilePreviewText,
  getFilePreviewKind,
  revokePreviewUrl,
  downloadFileBlob,
  type FilePreviewKind,
} from '@/utils/file'
import type { FileVO } from '@/types/file'

const props = defineProps<{
  modelValue: boolean
  file: FileVO | null
}>()

const emit = defineEmits<{
  'update:modelValue': [boolean]
}>()

const loading = ref(false)
const kind = ref<FilePreviewKind>('unsupported')
const blobUrl = ref('')
const textContent = ref('')
const mdRef = ref<HTMLDivElement | null>(null)
const lightboxOpen = ref(false)
const downloading = ref(false)

const title = computed(() => props.file?.name || '文件预览')

function clearPreview() {
  if (blobUrl.value) {
    revokePreviewUrl(blobUrl.value)
    blobUrl.value = ''
  }
  textContent.value = ''
  if (mdRef.value) mdRef.value.innerHTML = ''
}

async function loadPreview() {
  clearPreview()
  if (!props.file) return
  kind.value = getFilePreviewKind(props.file.type)
  if (kind.value === 'unsupported') return

  loading.value = true
  try {
    if (kind.value === 'image' || kind.value === 'pdf') {
      blobUrl.value = await getFilePreviewUrl(props.file.id)
    } else if (kind.value === 'text' || kind.value === 'markdown') {
      textContent.value = await getFilePreviewText(props.file.id)
      if (kind.value === 'markdown') {
        await nextTick()
        if (mdRef.value) {
          const theme = document.documentElement.getAttribute('data-theme') === 'dark' ? 'dark' : 'light'
          await Vditor.preview(mdRef.value, textContent.value, {
            mode: 'light',
            theme: { current: theme },
            hljs: { style: theme === 'dark' ? 'native' : 'github' },
          })
        }
      }
    }
  } catch {
    ElMessage.error('预览加载失败')
    kind.value = 'unsupported'
  } finally {
    loading.value = false
  }
}

watch(
  () => [props.modelValue, props.file?.id] as const,
  ([open]) => {
    if (open && props.file) loadPreview()
    else clearPreview()
  },
)

onUnmounted(() => clearPreview())

function close() {
  emit('update:modelValue', false)
}

async function handleDownload() {
  if (!props.file) return
  downloading.value = true
  try {
    await downloadFileBlob(props.file.id, props.file.name)
  } catch {
    ElMessage.error('下载失败')
  } finally {
    downloading.value = false
  }
}
</script>

<template>
  <UiDialog
    :model-value="modelValue"
    :title="title"
    size="lg"
    @update:model-value="emit('update:modelValue', $event)"
  >
    <div v-loading="loading" class="preview-body">
      <!-- 图片 -->
      <div v-if="kind === 'image' && blobUrl" class="preview-image-wrap">
        <img
          :src="blobUrl"
          class="preview-image"
          alt=""
          @click="lightboxOpen = true"
        />
        <p class="preview-hint">点击图片可放大</p>
        <ImageLightbox v-model="lightboxOpen" :urls="[blobUrl]" />
      </div>

      <!-- PDF -->
      <iframe
        v-else-if="kind === 'pdf' && blobUrl"
        :src="blobUrl"
        class="preview-pdf"
        title="PDF 预览"
      />

      <!-- Markdown -->
      <div
        v-else-if="kind === 'markdown'"
        ref="mdRef"
        class="preview-md markdown-prose"
      />

      <!-- 纯文本 -->
      <pre v-else-if="kind === 'text'" class="preview-text">{{ textContent }}</pre>

      <!-- 不支持 -->
      <div v-else-if="!loading" class="preview-unsupported">
        <FileWarning :size="36" />
        <p>该类型暂不支持在线预览</p>
        <p class="preview-hint">可下载后用本地应用打开（如 Office、压缩包）</p>
        <UiButton type="primary" :loading="downloading" @click="handleDownload">
          <Download :size="14" />
          下载文件
        </UiButton>
      </div>
    </div>

    <template #footer>
      <div class="preview-footer">
        <UiButton text :loading="downloading" @click="handleDownload">
          <Download :size="14" style="margin-right:4px;vertical-align:-2px" />
          下载
        </UiButton>
        <UiButton type="primary" @click="close">关闭</UiButton>
      </div>
    </template>
  </UiDialog>
</template>

<style scoped>
.preview-body {
  min-height: 280px;
}

.preview-image-wrap {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: var(--sp-2);
}

.preview-image {
  max-width: 100%;
  max-height: min(72vh, 680px);
  object-fit: contain;
  border-radius: var(--radius-md);
  cursor: zoom-in;
  background: var(--bg-hover);
}

.preview-pdf {
  width: 100%;
  height: min(72vh, 680px);
  border: 1px solid var(--border-color);
  border-radius: var(--radius-md);
  background: var(--bg-hover);
}

.preview-md {
  max-height: min(60vh, 560px);
  overflow: auto;
  padding: var(--sp-2);
}

.preview-text {
  margin: 0;
  max-height: min(60vh, 560px);
  overflow: auto;
  padding: var(--sp-4);
  border-radius: var(--radius-md);
  background: var(--bg-hover);
  font-family: var(--font-mono);
  font-size: var(--text-sm);
  line-height: 1.6;
  white-space: pre-wrap;
  word-break: break-word;
  color: var(--text-primary);
}

.preview-unsupported {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: var(--sp-3);
  padding: var(--sp-8) var(--sp-4);
  color: var(--text-secondary);
  text-align: center;
}

.preview-hint {
  margin: 0;
  font-size: var(--text-xs);
  color: var(--text-tertiary);
}

.preview-footer {
  display: flex;
  align-items: center;
  justify-content: flex-end;
  width: 100%;
  gap: var(--sp-2);
}
</style>
