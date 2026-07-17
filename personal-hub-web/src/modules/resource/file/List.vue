<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { getFileList, uploadFile, deleteFile, getFileDownloadUrl } from '@/modules/resource/api'
import { getCategories } from '@/api/categoryApi'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Upload, FileIcon, ImageIcon, FileText, Archive, Download, Trash2 } from 'lucide-vue-next'
import type { FileVO, FileQuery } from '@/types/file'
import type { CategoryVO } from '@/types/category'
import { PageHeader, EmptyState, ListToolbar, ListPagination } from '@/components'
import { useMainContentFill } from '@/composables/useMainContentFill'
import { useFillPageSize } from '@/composables/useFillPageSize'

const router = useRouter()
const list = ref<FileVO[]>([])
const total = ref(0)
const loading = ref(false)
const uploadLoading = ref(false)
const query = ref<FileQuery>({ page: 1, size: 10, keyword: '' })
const categories = ref<CategoryVO[]>([])
const showUpload = ref(false)
const typeOptions = [
  { value: '', label: '全部' },
  { value: 'image', label: '图片' },
  { value: 'pdf', label: 'PDF' },
  { value: 'doc', label: '文档' },
  { value: 'archive', label: '压缩包' },
]

useMainContentFill()
const { pageSize } = useFillPageSize((size) => {
  query.value.size = size
  query.value.page = 1
  fetchList()
})

onMounted(() => {
  fetchCategories()
})

async function fetchList() {
  loading.value = true
  try {
    const res = await getFileList(query.value)
    list.value = res.data.data.records
    total.value = res.data.data.total
  } finally {
    loading.value = false
  }
}

async function fetchCategories() {
  try {
    categories.value = (await getCategories('file')).data.data
  } catch { /* ignore */ }
}

function onSearch() { query.value.page = 1; fetchList() }
function onPageChange(page: number) { query.value.page = page; fetchList() }

async function handleUpload(files: FileList | null) {
  if (!files || files.length === 0) return
  uploadLoading.value = true
  try {
    for (let i = 0; i < files.length; i++) {
      await uploadFile(files[i])
    }
    ElMessage.success(`上传成功 ${files.length} 个文件`)
    showUpload.value = false
    fetchList()
  } catch (e: any) {
    ElMessage.error(e?.response?.data?.message || '上传失败')
  } finally {
    uploadLoading.value = false
  }
}

function handleDownload(id: number) {
  window.open(getFileDownloadUrl(id), '_blank')
}

async function handleDelete(id: number) {
  await ElMessageBox.confirm('确定删除该文件？', '提示', { type: 'warning' })
  await deleteFile(id)
  ElMessage.success('已删除')
  fetchList()
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
          <el-select v-model="query.categoryId" placeholder="分类" style="width:130px" clearable @change="onSearch">
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
        <div v-for="file in list" :key="file.id" class="file-card">
          <div class="file-card-icon">
            <component :is="getFileIcon(file.typeIcon)" :size="24" />
          </div>
          <div class="file-card-info">
            <span class="file-card-name" :title="file.name">{{ file.name }}</span>
            <div class="file-card-meta">
              <span class="meta-type">{{ file.typeLabel }}</span>
              <span class="meta-size">{{ file.sizeFormatted }}</span>
              <span v-if="file.categoryName" class="meta-cat">{{ file.categoryName }}</span>
            </div>
          </div>
          <div class="file-card-actions">
            <button class="icon-btn" title="下载" @click="handleDownload(file.id)">
              <Download :size="14" />
            </button>
            <button class="icon-btn icon-btn--danger" title="删除" @click="handleDelete(file.id)">
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
}
.file-card--pad {
  visibility: hidden;
  pointer-events: none;
  background: transparent;
  border-color: transparent;
}
.file-card:hover:not(.file-card--pad) {
  box-shadow: var(--shadow-sm);
  border-color: var(--accent-border);
}
.file-card-icon {
  width: 36px;
  height: 36px;
  border-radius: var(--radius-sm);
  background: var(--accent-light);
  color: var(--accent);
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
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
.meta-cat {
  padding: 0 4px;
  border-radius: 3px;
  background: var(--bg-hover);
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
