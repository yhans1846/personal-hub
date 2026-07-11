<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { getFileList, uploadFile, deleteFile, getFileDownloadUrl, getFileCategories } from '@/api/fileApi'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Upload, FileIcon, ImageIcon, FileText, Archive, Download, Trash2, Plus } from 'lucide-vue-next'
import type { FileVO, FileQuery, FileCategory } from '@/types/file'
import { PageHeader, EmptyState, ListToolbar, ListPagination } from '@/components'

const router = useRouter()
const list = ref<FileVO[]>([])
const total = ref(0)
const loading = ref(false)
const uploadLoading = ref(false)
const query = ref<FileQuery>({ page: 1, size: 20, keyword: '' })
const categories = ref<FileCategory[]>([])
const showUpload = ref(false)
const typeOptions = [
  { value: '', label: '全部' },
  { value: 'image', label: '图片' },
  { value: 'pdf', label: 'PDF' },
  { value: 'doc', label: '文档' },
  { value: 'archive', label: '压缩包' },
]

onMounted(() => {
  fetchList()
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
    const res = await getFileCategories()
    categories.value = res.data.data
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
</script>

<template>
  <div>
    <PageHeader title="文件" :subtitle="`共 ${total} 个文件`" />

    <ListToolbar :search="query.keyword" search-placeholder="搜索文件..." search-width="200px" @update:search="query.keyword = $event" @search="onSearch">
      <template #filters>
        <el-select v-model="query.type" placeholder="类型" style="width:110px" clearable @change="onSearch">
          <el-option v-for="t in typeOptions" :key="t.value" :value="t.value" :label="t.label" />
        </el-select>
        <el-select v-model="query.categoryId" placeholder="分类" style="width:130px" clearable @change="onSearch">
          <el-option v-for="c in categories" :key="c.id" :label="c.name" :value="c.id" />
        </el-select>
      </template>
    </ListToolbar>

    <!-- 上传区域 -->
    <div v-if="showUpload" class="upload-zone" @drop.prevent="handleUpload(($event as any).dataTransfer.files)" @dragover.prevent>
      <input type="file" multiple hidden ref="fileInputRef" @change="handleUpload(($event.target as HTMLInputElement).files)" />
      <Upload :size="32" />
      <p>拖拽文件到此处或 <el-button text type="primary" @click="($event) => { const el = document.querySelector<HTMLInputElement>('.upload-zone input[type=file]'); el?.click() }">点击选择</el-button></p>
      <p class="upload-hint">支持 PDF、Word、Excel、PPT、MD、图片、ZIP 等格式，单文件最大 50MB</p>
      <el-button size="small" @click="showUpload = false">取消</el-button>
    </div>

    <div v-if="loading" class="loading-skeleton">
      <div v-for="i in 8" :key="i" class="skeleton-file-card" />
    </div>

    <EmptyState v-else-if="list.length === 0" :icon="FileIcon" illustration="file" text="还没有文件，上传你的第一个文件吧" :action-icon="Upload" action-label="上传文件" @action="showUpload = true" />

    <div v-else class="file-grid">
      <div v-for="file in list" :key="file.id" class="file-card">
        <div class="file-card-icon">
          <component :is="getFileIcon(file.typeIcon)" :size="28" />
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
          <button class="icon-btn" @click="handleDownload(file.id)" title="下载">
            <Download :size="14" />
          </button>
          <button class="icon-btn icon-btn--danger" @click="handleDelete(file.id)" title="删除">
            <Trash2 :size="14" />
          </button>
        </div>
      </div>
    </div>

    <ListPagination v-if="total > query.size" :total="total" :page="query.page" :size="query.size" @update:page="onPageChange" />
  </div>
</template>

<style scoped>
.loading-skeleton { display: grid; grid-template-columns: repeat(auto-fill, minmax(240px, 1fr)); gap: var(--sp-3); }
.skeleton-file-card { height: 100px; border-radius: var(--radius-lg); background: var(--bg-hover); animation: pulse 1.5s ease-in-out infinite; }

/* 上传区域 */
.upload-zone {
  border: 2px dashed var(--border-color);
  border-radius: var(--radius-lg);
  padding: var(--sp-8);
  text-align: center;
  margin-bottom: var(--sp-5);
  background: var(--bg-card);
  color: var(--text-secondary);
  transition: all var(--transition);
  cursor: pointer;
}
.upload-zone:hover { border-color: var(--accent); background: var(--accent-light); }
.upload-hint { font-size: var(--text-xs); color: var(--text-tertiary); margin-top: var(--sp-2); }

/* 文件卡片网格 */
.file-grid { display: grid; grid-template-columns: repeat(auto-fill, minmax(280px, 1fr)); gap: var(--sp-3); }

.file-card {
  display: flex; align-items: center; gap: var(--sp-3);
  padding: var(--sp-3) var(--sp-4);
  background: var(--bg-card);
  border: 1px solid var(--border-color);
  border-radius: var(--radius-md);
  transition: all var(--transition);
}
.file-card:hover { box-shadow: var(--shadow-sm); border-color: var(--accent-border); }
.file-card-icon {
  width: 40px; height: 40px;
  border-radius: var(--radius-sm);
  background: var(--accent-light);
  color: var(--accent);
  display: flex; align-items: center; justify-content: center;
  flex-shrink: 0;
}
.file-card-info { flex: 1; min-width: 0; }
.file-card-name {
  font-size: var(--text-sm); font-weight: 500;
  display: block; white-space: nowrap; overflow: hidden; text-overflow: ellipsis;
}
.file-card-meta { display: flex; gap: var(--sp-2); margin-top: 2px; font-size: var(--text-xs); color: var(--text-tertiary); }
.meta-type, .meta-size, .meta-cat { display: inline-block; }
.meta-cat { padding: 0 4px; border-radius: 3px; background: var(--bg-hover); }

.file-card-actions {
  display: flex; gap: 2px; flex-shrink: 0;
  opacity: 0; transition: opacity var(--transition);
}
.file-card:hover .file-card-actions { opacity: 1; }

.icon-btn {
  background: none; border: none; cursor: pointer;
  padding: 6px; border-radius: var(--radius-sm);
  color: var(--text-tertiary); transition: all var(--transition);
  display: flex; align-items: center;
}
.icon-btn:hover { color: var(--accent); background: var(--accent-light); }
.icon-btn--danger:hover { color: var(--danger); background: var(--danger-light); }
</style>
