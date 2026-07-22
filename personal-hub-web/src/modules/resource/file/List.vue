<script setup lang="ts">
import { ref, onMounted, onUnmounted, watch } from 'vue'
import { getFileList, uploadFile, deleteFile, clearAllFiles } from '@/modules/resource/api'
import { getCategories } from '@/modules/knowledge/api'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Upload, FileIcon, ImageIcon, FileText, Archive, Download, Trash2, Eye, FolderOpen } from 'lucide-vue-next'
import type { FileVO, FileQuery } from '@/types/file'
import type { CategoryVO } from '@/types/category'
import { PageHeader, EmptyState, ListToolbar, ListPagination } from '@/components'
import FilePreviewDialog from '@/components/FilePreviewDialog.vue'
import { useMainContentFill } from '@/composables/useMainContentFill'
import { usePaginatedList, type PageQuery } from '@/composables/usePaginatedList'
import { downloadFileBlob, getFilePreviewUrl, revokePreviewUrl, getFilePreviewKind } from '@/utils/file'
import { handleApiError, unwrapPage, unwrapResult } from '@/utils/apiResult'
import UiTooltip from '@/components/UiTooltip.vue'

/** 一屏 5×4 铺满 */
const PAGE_SIZE = 20

type FileListQuery = FileQuery & PageQuery
const uploadLoading = ref(false)
const categories = ref<CategoryVO[]>([])
const showUpload = ref(false)
const uploadCategoryId = ref<number | null>(null)
const previewOpen = ref(false)
const previewFile = ref<FileVO | null>(null)
const thumbMap = ref<Record<number, string>>({})
const typeOptions = [
  { value: 'image', label: '图片' },
  { value: 'pdf', label: 'PDF' },
  { value: 'doc', label: '文档' },
  { value: 'archive', label: '压缩包' },
]

const { list, total, loading, query, fetchList, onSearch, onPageChange } = usePaginatedList<FileVO, FileListQuery>({
  initialQuery: { page: 1, size: PAGE_SIZE, keyword: '' },
  fetchPage: async (q) => {
    const params: FileQuery = {
      page: q.page,
      size: q.size,
      keyword: q.keyword || undefined,
      type: q.type || undefined,
      categoryId: q.categoryId || undefined,
    }
    return unwrapPage(getFileList(params))
  },
  onFetched: (records) => loadThumbs(records),
  errorMessage: '加载文件失败',
})

useMainContentFill()

onMounted(() => {
  fetchList()
  fetchCategories()
})

onUnmounted(() => {
  Object.values(thumbMap.value).forEach(revokePreviewUrl)
})

async function loadThumbs(files: FileVO[]) {
  Object.values(thumbMap.value).forEach(revokePreviewUrl)
  thumbMap.value = {}
  const images = files.filter(f => getFilePreviewKind(f.type) === 'image')
  await Promise.all(images.map(async (f) => {
    try {
      const url = await getFilePreviewUrl(f.id)
      thumbMap.value = { ...thumbMap.value, [f.id]: url }
    } catch { /* ignore thumb errors */ }
  }))
}

async function fetchCategories() {
  try {
    categories.value = await unwrapResult(getCategories('file'))
  } catch { /* ignore */ }
}

function openUploadPicker(e: MouseEvent) {
  const zone = (e.currentTarget as HTMLElement | null)?.closest('.upload-zone')
  zone?.querySelector<HTMLInputElement>('input[type=file]')?.click()
}

async function handleUpload(files: FileList | null) {
  if (!files || files.length === 0) return
  uploadLoading.value = true
  try {
    for (let i = 0; i < files.length; i++) {
      await uploadFile(files[i], uploadCategoryId.value)
    }
    ElMessage.success(`上传成功 ${files.length} 个文件`)
    showUpload.value = false
    fetchList()
  } catch (e) {
    handleApiError(e, '上传失败')
  } finally {
    uploadLoading.value = false
  }
}

async function handleDownload(file: FileVO, e?: Event) {
  e?.stopPropagation()
  try {
    await downloadFileBlob(file.id, file.name)
  } catch (err) {
    handleApiError(err, '下载失败')
  }
}

async function handleDelete(id: number, e?: Event) {
  e?.stopPropagation()
  await ElMessageBox.confirm('确定删除该文件？', '提示', { type: 'warning' })
  try {
    await deleteFile(id)
    ElMessage.success('已删除')
    fetchList()
  } catch (err) {
    handleApiError(err, '删除失败')
  }
}

async function handleClearAll() {
  if (total.value === 0 && !query.value.keyword && !query.value.type && !query.value.categoryId) {
    ElMessage.info('暂无文件')
    return
  }
  try {
    await ElMessageBox.confirm(
      '将永久删除全部文件（含磁盘文件），且不可恢复。确定清空？',
      '清空文件',
      { type: 'error', confirmButtonText: '确定清空', cancelButtonText: '取消' },
    )
    const deleted = (await unwrapResult(clearAllFiles())).deleted
    ElMessage.success(deleted > 0 ? `已清空 ${deleted} 个文件` : '暂无文件')
    query.value.page = 1
    fetchList()
  } catch (e) {
    if (e === 'cancel' || (e && typeof e === 'object' && 'action' in e)) return
    handleApiError(e, '清空失败')
  }
}

function openPreview(file: FileVO) {
  previewFile.value = file
  previewOpen.value = true
}

function getFileIcon(icon: string) {
  switch (icon) {
    case 'image': return ImageIcon
    case 'pdf': return FileText
    case 'archive': return Archive
    default: return FileIcon
  }
}

watch(previewOpen, (open) => {
  if (!open) previewFile.value = null
})
</script>

<template>
  <div class="plan-page">
    <div class="plan-top">
      <PageHeader title="文件" :subtitle="`共 ${total} 个文件`" />

      <ListToolbar
        :search="query.keyword ?? ''"
        search-placeholder="搜索文件..."
        search-width="200px"
        create-label="上传文件"
        @update:search="query.keyword = $event"
        @search="onSearch"
        @create="showUpload = true"
      >
        <template #filters>
          <el-select v-model="query.type" placeholder="类型" style="width:110px" clearable @change="onSearch">
            <el-option v-for="t in typeOptions" :key="t.value" :value="t.value" :label="t.label" />
          </el-select>
          <el-select v-model="query.categoryId" placeholder="分类" style="width:140px" clearable @change="onSearch">
            <el-option v-for="c in categories" :key="c.id" :label="c.name" :value="c.id" />
          </el-select>
        </template>
        <template #actions>
          <button
            type="button"
            class="toolbar-btn toolbar-btn--danger"
            :disabled="total === 0 && !query.keyword && !query.type && !query.categoryId"
            @click="handleClearAll"
          >
            <Trash2 :size="14" /> 清空
          </button>
        </template>
      </ListToolbar>

      <div
        v-if="showUpload"
        class="upload-zone"
        @drop.prevent="handleUpload(($event as DragEvent).dataTransfer?.files ?? null)"
        @dragover.prevent
      >
        <input type="file" multiple hidden ref="fileInputRef" @change="handleUpload(($event.target as HTMLInputElement).files)" />
        <Upload :size="28" />
        <p>
          拖拽文件到此处或
          <el-button text type="primary" :loading="uploadLoading" @click="openUploadPicker">
            点击选择
          </el-button>
        </p>
        <div class="upload-category" @click.stop>
          <span class="upload-category-label">上传到分类</span>
          <el-select v-model="uploadCategoryId" placeholder="不选分类" clearable style="width:160px">
            <el-option v-for="c in categories" :key="c.id" :label="c.name" :value="c.id" />
          </el-select>
        </div>
        <p class="upload-hint">支持 PDF、Office、MD、图片、ZIP 等，单文件最大 50MB</p>
        <el-button size="small" @click="showUpload = false">取消</el-button>
      </div>
    </div>

    <div class="plan-middle">
      <div v-if="loading" class="file-grid">
        <div v-for="i in PAGE_SIZE" :key="i" class="skeleton-card" />
      </div>

      <EmptyState
        v-else-if="list.length === 0"
        :icon="FileIcon"
        illustration="file"
        text="还没有文件，上传你的第一个文件吧"
        :action-icon="Upload"
        action-label="上传文件"
        @action="showUpload = true"
      />

      <div v-else class="file-grid">
        <div
          v-for="file in list"
          :key="file.id"
          class="file-card"
          @click="openPreview(file)"
        >
          <div class="card-preview">
            <img
              v-if="thumbMap[file.id]"
              :src="thumbMap[file.id]"
              class="card-preview-img"
              alt=""
              loading="lazy"
              decoding="async"
            />
            <component
              v-else
              :is="getFileIcon(file.typeIcon)"
              :size="28"
              class="card-preview-icon"
            />
          </div>
          <UiTooltip :content="file.name">
            <div class="card-title">{{ file.name }}</div>
          </UiTooltip>
          <div class="card-footer">
            <span class="card-category">
              <template v-if="file.categoryName">
                <FolderOpen :size="11" /> {{ file.categoryName }}
              </template>
              <template v-else>{{ file.typeLabel }} · {{ file.sizeFormatted }}</template>
            </span>
            <div class="card-actions" @click.stop>
              <UiTooltip content="预览">
                <button type="button" class="icon-btn" @click.stop="openPreview(file)">
                  <Eye :size="13" />
                </button>
              </UiTooltip>
              <UiTooltip content="下载">
                <button type="button" class="icon-btn" @click.stop="handleDownload(file, $event)">
                  <Download :size="13" />
                </button>
              </UiTooltip>
              <UiTooltip content="删除">
                <button type="button" class="icon-btn icon-btn--danger" @click.stop="handleDelete(file.id, $event)">
                  <Trash2 :size="13" />
                </button>
              </UiTooltip>
            </div>
          </div>
        </div>
        <div
          v-for="n in Math.max(0, PAGE_SIZE - list.length)"
          :key="'pad-' + n"
          class="file-card file-card--pad"
          aria-hidden="true"
        />
      </div>
    </div>

    <div class="plan-foot">
      <ListPagination v-if="total > 0" :total="total" :page="query.page ?? 1" :size="PAGE_SIZE" @update:page="onPageChange" />
    </div>

    <FilePreviewDialog v-model="previewOpen" :file="previewFile" />
  </div>
</template>

<style scoped>
.plan-page {
  width: 100%;
  height: 100%;
  min-height: 0;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}
.plan-top { flex-shrink: 0; }
.plan-middle {
  flex: 1;
  min-height: 0;
  overflow: hidden;
  display: flex;
  flex-direction: column;
}
.plan-foot { flex-shrink: 0; padding-top: 8px; }

.upload-zone {
  border: 2px dashed var(--border-color);
  border-radius: var(--radius-lg);
  padding: var(--sp-4);
  text-align: center;
  margin-top: var(--sp-3);
  margin-bottom: var(--sp-2);
  background: var(--bg-card);
  color: var(--text-secondary);
  transition: all var(--transition);
}
.upload-zone:hover { border-color: var(--accent); background: var(--accent-light); }
.upload-hint { font-size: var(--text-xs); color: var(--text-tertiary); margin-top: var(--sp-1); }
.upload-category {
  display: inline-flex;
  align-items: center;
  gap: var(--sp-2);
  margin: var(--sp-2) 0;
}
.upload-category-label {
  font-size: var(--text-xs);
  color: var(--text-secondary);
}

/* 一屏铺满：5×4（PAGE_SIZE=20），卡片随格子拉伸 */
.file-grid {
  flex: 1;
  min-height: 0;
  display: grid;
  grid-template-columns: repeat(5, minmax(0, 1fr));
  grid-auto-rows: 1fr;
  gap: 8px;
}
@media (max-width: 1100px) {
  .file-grid { grid-template-columns: repeat(4, minmax(0, 1fr)); }
}
@media (max-width: 720px) {
  .file-grid { grid-template-columns: repeat(2, minmax(0, 1fr)); }
}

.skeleton-card {
  min-height: 0;
  border-radius: var(--radius-md);
  background: var(--bg-hover);
  animation: pulse 1.5s ease-in-out infinite;
}

.file-card {
  box-sizing: border-box;
  min-width: 0;
  min-height: 0;
  height: 100%;
  display: flex;
  flex-direction: column;
  gap: 6px;
  padding: 8px;
  background: var(--bg-card);
  border: 1px solid var(--border-color);
  border-radius: var(--radius-md);
  cursor: pointer;
  transition: border-color var(--transition), box-shadow var(--transition);
  overflow: hidden;
}
.file-card--pad {
  visibility: hidden;
  pointer-events: none;
  background: transparent;
  border-color: transparent;
  cursor: default;
}
.file-card:hover:not(.file-card--pad) {
  box-shadow: var(--shadow-sm);
  border-color: var(--accent-border);
}

.card-preview {
  flex: 1;
  min-height: 0;
  width: 100%;
  border-radius: var(--radius-sm);
  overflow: hidden;
  background: color-mix(in srgb, var(--accent) 8%, var(--bg-hover));
  color: var(--accent);
  display: flex;
  align-items: center;
  justify-content: center;
}
.card-preview-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  display: block;
}
.card-preview-icon {
  flex-shrink: 0;
}

.card-title {
  flex-shrink: 0;
  font-size: var(--text-sm);
  font-weight: 500;
  line-height: 1.3;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  padding: 0 2px;
  color: var(--text-primary);
}
.card-footer {
  flex-shrink: 0;
  display: flex;
  align-items: center;
  gap: var(--sp-2);
  font-size: var(--text-xs);
  color: var(--text-tertiary);
  padding: 0 2px;
  min-height: 22px;
}
.card-category {
  display: flex;
  align-items: center;
  gap: 2px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  flex: 1;
  min-width: 0;
}
.card-actions {
  display: flex;
  gap: 2px;
  flex-shrink: 0;
  opacity: 0;
  transition: opacity var(--transition);
}
.file-card:hover:not(.file-card--pad) .card-actions { opacity: 1; }
.icon-btn {
  background: none;
  border: none;
  cursor: pointer;
  padding: 4px;
  border-radius: var(--radius-sm);
  color: var(--text-tertiary);
  transition: all var(--transition);
  display: flex;
}
.icon-btn:hover { color: var(--accent); background: var(--accent-light); }
.icon-btn--danger:hover { color: var(--danger); background: var(--danger-light); }
</style>
