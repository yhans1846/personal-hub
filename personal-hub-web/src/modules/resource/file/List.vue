<script setup lang="ts">
import { ref, onMounted, onUnmounted, watch } from 'vue'
import { useRouter } from 'vue-router'
import { getFileList, uploadFile, deleteFile, updateFileCategory } from '@/modules/resource/api'
import { getCategories } from '@/modules/knowledge/api'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Upload, FileIcon, ImageIcon, FileText, Archive, Download, Trash2, Eye } from 'lucide-vue-next'
import type { FileVO, FileQuery } from '@/types/file'
import type { CategoryVO } from '@/types/category'
import { PageHeader, EmptyState, ListToolbar, ListPagination } from '@/components'
import FilePreviewDialog from '@/components/FilePreviewDialog.vue'
import { useMainContentFill } from '@/composables/useMainContentFill'
import { useFillPageSize } from '@/composables/useFillPageSize'
import { usePaginatedList, type PageQuery } from '@/composables/usePaginatedList'
import { downloadFileBlob, getFilePreviewUrl, revokePreviewUrl, getFilePreviewKind } from '@/utils/file'
import { handleApiError, unwrapPage, unwrapResult } from '@/utils/apiResult'

const router = useRouter()
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
  initialQuery: { page: 1, size: 10, keyword: '' },
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
const { pageSize } = useFillPageSize((size) => {
  query.value.size = size
  query.value.page = 1
  fetchList()
})

onMounted(() => {
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

async function handleCategoryChange(file: FileVO, categoryId: number | null, e?: Event) {
  e?.stopPropagation()
  try {
    const updated = await unwrapResult(updateFileCategory(file.id, categoryId))
    const idx = list.value.findIndex(f => f.id === file.id)
    if (idx >= 0) list.value[idx] = { ...list.value[idx], ...updated }
    ElMessage.success(categoryId ? '已更新分类' : '已清除分类')
  } catch (err) {
    handleApiError(err, '更新分类失败')
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

function goCategories() { router.push('/categories') }

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
          <el-button @click="goCategories">分类管理</el-button>
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
          <el-button text type="primary" :loading="uploadLoading" @click="($event) => { const el = ($event.currentTarget as HTMLElement).closest('.upload-zone')?.querySelector<HTMLInputElement>('input[type=file]'); el?.click() }">
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
        <div v-for="i in pageSize" :key="i" class="skeleton-file-card" />
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
          role="button"
          tabindex="0"
          @click="openPreview(file)"
          @keydown.enter="openPreview(file)"
        >
          <div class="file-card-icon" :class="{ 'file-card-icon--thumb': !!thumbMap[file.id] }">
            <img v-if="thumbMap[file.id]" :src="thumbMap[file.id]" class="file-thumb" alt="" />
            <component v-else :is="getFileIcon(file.typeIcon)" :size="24" />
          </div>
          <div class="file-card-info">
            <span class="file-card-name" :title="file.name">{{ file.name }}</span>
            <div class="file-card-meta">
              <span class="meta-type">{{ file.typeLabel }}</span>
              <span class="meta-size">{{ file.sizeFormatted }}</span>
            </div>
            <div class="file-card-cat" @click.stop>
              <el-select
                :model-value="file.categoryId"
                placeholder="未分类"
                clearable
                size="small"
                style="width:120px"
                @change="(v: number | null) => handleCategoryChange(file, v ?? null)"
              >
                <el-option v-for="c in categories" :key="c.id" :label="c.name" :value="c.id" />
              </el-select>
            </div>
          </div>
          <div class="file-card-actions">
            <button class="icon-btn" title="预览" @click="openPreview(file)">
              <Eye :size="14" />
            </button>
            <button class="icon-btn" title="下载" @click="handleDownload(file, $event)">
              <Download :size="14" />
            </button>
            <button class="icon-btn icon-btn--danger" title="删除" @click="handleDelete(file.id, $event)">
              <Trash2 :size="14" />
            </button>
          </div>
        </div>
        <div
          v-for="n in Math.max(0, pageSize - list.length)"
          :key="'pad-' + n"
          class="file-card file-card--pad"
          aria-hidden="true"
        />
      </div>
    </div>

    <div class="plan-foot">
      <ListPagination v-if="total > 0" :total="total" :page="query.page ?? 1" :size="pageSize" @update:page="onPageChange" />
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

.file-grid {
  flex: 1;
  min-height: 0;
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  grid-auto-rows: 1fr;
  gap: 6px;
  align-content: stretch;
}
@media (max-width: 720px) {
  .file-grid { grid-template-columns: 1fr; }
}

.skeleton-file-card {
  min-height: 0;
  border-radius: var(--radius-md);
  background: var(--bg-hover);
  animation: pulse 1.5s ease-in-out infinite;
}

.file-card {
  display: flex;
  align-items: center;
  gap: var(--sp-3);
  padding: var(--sp-3);
  background: var(--bg-card);
  border: 1px solid var(--border-color);
  border-radius: var(--radius-md);
  transition: all var(--transition);
  min-height: 0;
  min-width: 0;
  overflow: hidden;
  cursor: pointer;
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
.file-card-icon {
  width: 56px;
  height: 56px;
  border-radius: var(--radius-sm);
  background: var(--accent-light);
  color: var(--accent);
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  overflow: hidden;
}
.file-card-icon--thumb {
  background: var(--bg-hover);
  padding: 0;
}
.file-thumb {
  width: 100%;
  height: 100%;
  object-fit: cover;
}
.file-card-info { flex: 1; min-width: 0; }
.file-card-name {
  font-size: var(--text-sm);
  font-weight: 500;
  display: block;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}
.file-card-meta {
  display: flex;
  gap: var(--sp-2);
  margin-top: 2px;
  font-size: var(--text-xs);
  color: var(--text-tertiary);
}
.file-card-cat {
  margin-top: 4px;
}

.file-card-actions {
  display: flex;
  gap: 2px;
  flex-shrink: 0;
  opacity: 0;
  transition: opacity var(--transition);
}
.file-card:hover .file-card-actions { opacity: 1; }

.icon-btn {
  background: none;
  border: none;
  cursor: pointer;
  padding: 6px;
  border-radius: var(--radius-sm);
  color: var(--text-tertiary);
  transition: all var(--transition);
  display: flex;
  align-items: center;
}
.icon-btn:hover { color: var(--accent); background: var(--accent-light); }
.icon-btn--danger:hover { color: var(--danger); background: var(--danger-light); }
</style>
