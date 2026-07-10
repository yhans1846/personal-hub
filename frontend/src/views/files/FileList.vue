<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { getFileList, uploadFile, deleteFile, getFileDownloadUrl, getFileCategories } from '@/api/fileApi'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Upload, FileIcon, ImageIcon, FileText, Archive, Download, Trash2, Plus } from 'lucide-vue-next'
import type { FileVO, FileQuery, FileCategory } from '@/types/file'

const router = useRouter()
const list = ref<FileVO[]>([])
const total = ref(0)
const loading = ref(false)
const uploadLoading = ref(false)
const query = ref<FileQuery>({ page: 1, size: 20, keyword: '' })
const categories = ref<FileCategory[]>([])
const showUpload = ref(false)

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
    <div class="page-header">
      <h2>文件</h2>
      <p>共 {{ total }} 个文件</p>
    </div>

    <div class="toolbar">
      <div class="toolbar-left">
        <el-input v-model="query.keyword" placeholder="搜索文件名..." style="width:200px" clearable @clear="onSearch" @keyup.enter="onSearch">
          <template #prefix><Search :size="14" style="color: var(--text-tertiary)" /></template>
        </el-input>
        <el-select v-model="query.type" placeholder="文件类型" style="width:110px" clearable @change="onSearch">
          <el-option label="全部" value="" />
          <el-option label="图片" value="image" />
          <el-option label="PDF" value="pdf" />
          <el-option label="文档" value="doc" />
          <el-option label="压缩包" value="archive" />
        </el-select>
        <el-select v-model="query.categoryId" placeholder="分类" style="width:130px" clearable @change="onSearch">
          <el-option v-for="c in categories" :key="c.id" :label="c.name" :value="c.id" />
        </el-select>
      </div>
      <el-button type="primary" @click="showUpload = true">
        <Upload :size="14" /> 上传文件
      </el-button>
    </div>

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

    <div v-else-if="list.length === 0" class="empty-state" style="padding: var(--sp-12) var(--sp-6);">
      <div class="empty-state__icon"><FileIcon :size="48" /></div>
      <div class="empty-state__text">还没有文件，上传你的第一个文件吧</div>
      <el-button type="primary" @click="showUpload = true">
        <Upload :size="14" /> 上传文件
      </el-button>
    </div>

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

    <el-pagination
      v-if="total > query.size"
      v-model:current-page="query.page"
      :total="total" :page-size="query.size"
      layout="total, prev, pager, next"
      style="margin-top: var(--sp-6); justify-content: flex-end"
      @current-change="onPageChange"
    />
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
