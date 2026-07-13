<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { importMarkdownFile, importMarkdownContent } from '@/modules/knowledge/api'
import { getCategories } from '@/api/categoryApi'
import { getTags } from '@/modules/knowledge/api'
import { ElMessage } from 'element-plus'
import { Upload, FileText, Link, CheckCircle, XCircle, AlertTriangle } from 'lucide-vue-next'
import type { ImportReport } from '@/types/note'

const emit = defineEmits<{ done: [] }>()

// ─── Tab ───
const activeTab = ref<'file' | 'content'>('file')

// ─── 分类 & 标签 ───
const categories = ref<any[]>([])
const tags = ref<any[]>([])
const selectedCategoryIds = ref<number[]>([])
const selectedTagIds = ref<number[]>([])
onMounted(async () => {
  try {
    const [catRes, tagRes] = await Promise.all([getCategories('note'), getTags()])
    categories.value = catRes.data.data
    tags.value = tagRes.data.data
  } catch { /* ignore */ }
})

// ─── 文件导入 ───
const file = ref<File | null>(null)
const fileTitle = ref('')
const baseDir = ref('')

function onFileChange(e: Event) {
  const input = e.target as HTMLInputElement
  if (input.files?.length) {
    file.value = input.files[0]
    // 自动用文件名作为标题（不含扩展名）
    const name = input.files[0].name
    fileTitle.value = name.endsWith('.md') ? name.slice(0, -3) : name
  }
}

// ─── 内容粘贴 ───
const pastedContent = ref('')
const contentTitle = ref('')

// ─── 导入状态 ───
const importing = ref(false)
const report = ref<ImportReport | null>(null)

async function handleImport() {
  importing.value = true
  report.value = null
  try {
    let res
    if (activeTab.value === 'file') {
      if (!file.value) { ElMessage.warning('请选择 Markdown 文件'); return }
      res = await importMarkdownFile(
        file.value,
        fileTitle.value || undefined,
        selectedCategoryIds.value,
        selectedTagIds.value,
        baseDir.value || undefined,
      )
    } else {
      if (!pastedContent.value.trim()) { ElMessage.warning('请粘贴 Markdown 内容'); return }
      res = await importMarkdownContent({
        content: pastedContent.value,
        title: contentTitle.value || undefined,
        categoryIds: selectedCategoryIds.value,
        tagIds: selectedTagIds.value,
      })
    }
    report.value = res.data.data

    if (report.value.failed === 0 && report.value.skipped === 0) {
      ElMessage.success(`导入成功（${report.value.total} 个资源）`)
    } else {
      ElMessage.info(`导入完成：${report.value.success} 成功，${report.value.failed} 失败，${report.value.skipped} 跳过`)
    }
  } catch (e: any) {
    ElMessage.error(e?.response?.data?.message || '导入失败')
  } finally {
    importing.value = false
  }
}

function handleClose() {
  if (report.value?.noteId) {
    emit('done')
  }
}
</script>

<template>
  <div class="import-dialog">
    <!-- 标题 -->
    <div class="dialog-header">
      <h2 class="dialog-title">导入 Markdown</h2>
    </div>

    <!-- 导入结果 -->
    <div v-if="report" class="import-result">
      <div class="result-summary">
        <div class="result-icon" :class="{ success: report.failed === 0, warning: report.failed > 0 }">
          <CheckCircle v-if="report.failed === 0" :size="24" />
          <AlertTriangle v-else :size="24" />
        </div>
        <div class="result-text">
          <p class="result-title">导入完成</p>
          <p class="result-detail">
            {{ report.total }} 个资源 · {{ report.success }} 成功 · {{ report.failed }} 失败 · {{ report.skipped }} 跳过
          </p>
        </div>
      </div>

      <!-- 相对路径警告 -->
      <div v-if="report.warning" class="result-warning">
        <AlertTriangle :size="14" />
        {{ report.warning }}
      </div>

      <!-- 资源列表 -->
      <div v-if="report.resources.length" class="result-list">
        <div
          v-for="(r, i) in report.resources"
          :key="i"
          class="result-item"
          :class="{ failed: !r.success }"
        >
          <span class="item-icon">
            <CheckCircle v-if="r.success" :size="12" />
            <XCircle v-else :size="12" />
          </span>
          <span class="item-ref">{{ r.originalRef }}</span>
          <span v-if="r.resolvedPath" class="item-arrow">→</span>
          <span v-if="r.resolvedPath" class="item-path">{{ r.resolvedPath }}</span>
          <span v-if="!r.success && !r.skipped" class="item-error">{{ r.message }}</span>
          <span v-else-if="r.skipped" class="item-skipped">{{ r.message }}</span>
        </div>
      </div>

      <div class="result-actions">
        <button class="btn btn-secondary" @click="report = null">继续导入</button>
        <button class="btn btn-primary" @click="handleClose">完成</button>
      </div>
    </div>

    <!-- 导入表单 -->
    <div v-else class="import-form">
      <!-- 分类 & 标签 -->
      <div class="form-row">
        <div class="form-group half">
          <label class="form-label">分类</label>
          <el-select
            v-model="selectedCategoryIds"
            multiple
            placeholder="选择分类"
            style="width: 100%"
            size="small"
          >
            <el-option v-for="c in categories" :key="c.id" :label="c.name" :value="c.id" />
          </el-select>
        </div>
        <div class="form-group half">
          <label class="form-label">标签</label>
          <el-select
            v-model="selectedTagIds"
            multiple
            placeholder="选择标签"
            style="width: 100%"
            size="small"
          >
            <el-option v-for="t in tags" :key="t.id" :label="t.name" :value="t.id" />
          </el-select>
        </div>
      </div>

      <!-- Tab 切换 -->
      <div class="tab-bar">
        <button
          class="tab-btn"
          :class="{ active: activeTab === 'file' }"
          @click="activeTab = 'file'"
        >
          <Upload :size="14" /> 从文件导入
        </button>
        <button
          class="tab-btn"
          :class="{ active: activeTab === 'content' }"
          @click="activeTab = 'content'"
        >
          <FileText :size="14" /> 粘贴内容
        </button>
      </div>

      <!-- Tab: 文件导入 -->
      <div v-if="activeTab === 'file'" class="tab-content">
        <div class="form-group">
          <label class="form-label">选择 Markdown 文件</label>
          <div class="file-upload-area" @click="$refs.fileInput?.click()">
            <Upload :size="24" class="upload-icon" />
            <p v-if="!file" class="upload-text">点击选择 .md 文件</p>
            <p v-else class="upload-text file-selected">{{ file.name }}</p>
          </div>
          <input
            ref="fileInput"
            type="file"
            accept=".md"
            style="display: none"
            @change="onFileChange"
          />
        </div>

        <div class="form-group">
          <label class="form-label">笔记标题（可选）</label>
          <input v-model="fileTitle" class="form-input" placeholder="默认为文件名" />
        </div>

        <div class="form-group">
          <label class="form-label">
            Markdown 文件所在目录（可选）
            <span class="label-hint">用于解析相对路径图片</span>
          </label>
          <input
            v-model="baseDir"
            class="form-input"
            placeholder="例如：E:\WorkSpace\Python\learn\python_base_learn"
          />
        </div>
      </div>

      <!-- Tab: 粘贴内容 -->
      <div v-if="activeTab === 'content'" class="tab-content">
        <div class="form-group">
          <label class="form-label">Markdown 内容</label>
          <textarea
            v-model="pastedContent"
            class="form-textarea"
            placeholder="在此粘贴 Markdown 内容...

支持网络图片（http/https）和 Base64 图片自动下载。
相对路径图片无法解析，建议使用「从文件导入」。"
            rows="8"
          />
        </div>

        <div class="form-group">
          <label class="form-label">笔记标题（可选）</label>
          <input v-model="contentTitle" class="form-input" placeholder="从内容提取或手动输入" />
        </div>
      </div>

      <!-- 提交 -->
      <div class="form-actions">
        <button class="btn btn-primary" :disabled="importing" @click="handleImport">
          {{ importing ? '导入中...' : '开始导入' }}
        </button>
      </div>
    </div>
  </div>
</template>

<style scoped>
.import-dialog {
  padding: 0;
}

.dialog-header {
  margin-bottom: 20px;
}
.dialog-title {
  font-size: 18px;
  font-weight: 600;
  color: var(--text-primary);
  margin: 0;
}

/* ─── 表单 ─── */
.import-form {
  display: flex;
  flex-direction: column;
  gap: 16px;
}
.form-row {
  display: flex;
  gap: 12px;
}
.form-group {
  display: flex;
  flex-direction: column;
  gap: 6px;
}
.form-group.half {
  flex: 1;
}
.form-label {
  font-size: var(--text-sm);
  font-weight: 500;
  color: var(--text-secondary);
}
.label-hint {
  font-weight: 400;
  color: var(--text-tertiary);
  font-size: 12px;
}
.form-input {
  height: 36px;
  padding: 0 12px;
  border: 1px solid var(--border-color);
  border-radius: var(--radius-sm);
  background: var(--bg-card);
  color: var(--text-primary);
  font-size: var(--text-sm);
  outline: none;
  transition: border-color var(--transition);
}
.form-input:focus {
  border-color: var(--accent);
}
.form-textarea {
  padding: 12px;
  border: 1px solid var(--border-color);
  border-radius: var(--radius-sm);
  background: var(--bg-card);
  color: var(--text-primary);
  font-size: var(--text-sm);
  outline: none;
  resize: vertical;
  min-height: 160px;
  font-family: var(--font-mono);
  line-height: 1.6;
  transition: border-color var(--transition);
}
.form-textarea:focus {
  border-color: var(--accent);
}

/* ─── Tab ─── */
.tab-bar {
  display: flex;
  border: 1px solid var(--border-color);
  border-radius: var(--radius-sm);
  overflow: hidden;
}
.tab-btn {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
  height: 38px;
  border: none;
  background: var(--bg-card);
  color: var(--text-tertiary);
  font-size: var(--text-sm);
  cursor: pointer;
  transition: all var(--transition);
}
.tab-btn:hover {
  background: var(--bg-hover);
  color: var(--text-secondary);
}
.tab-btn.active {
  background: var(--accent);
  color: #fff;
}
.tab-content {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

/* ─── 文件上传 ─── */
.file-upload-area {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 8px;
  padding: 32px 16px;
  border: 2px dashed var(--border-color);
  border-radius: var(--radius-md);
  cursor: pointer;
  transition: all var(--transition);
}
.file-upload-area:hover {
  border-color: var(--accent);
  background: var(--accent-light);
}
.upload-icon {
  color: var(--text-tertiary);
}
.upload-text {
  color: var(--text-tertiary);
  font-size: var(--text-sm);
  margin: 0;
}
.upload-text.file-selected {
  color: var(--accent);
  font-weight: 500;
}

/* ─── 按钮 ─── */
.form-actions {
  display: flex;
  justify-content: flex-end;
  padding-top: 4px;
}
.btn {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  height: 36px;
  padding: 0 20px;
  border-radius: var(--radius-sm);
  font-size: var(--text-sm);
  font-weight: 500;
  cursor: pointer;
  transition: all var(--transition);
  border: none;
}
.btn-primary {
  background: var(--accent);
  color: #fff;
}
.btn-primary:hover {
  opacity: 0.9;
}
.btn-primary:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}
.btn-secondary {
  background: var(--bg-hover);
  color: var(--text-secondary);
}
.btn-secondary:hover {
  background: var(--border-color);
}

/* ─── 结果 ─── */
.result-summary {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 16px;
  background: var(--bg-hover);
  border-radius: var(--radius-md);
  margin-bottom: 12px;
}
.result-icon {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 40px;
  height: 40px;
  border-radius: 50%;
  flex-shrink: 0;
}
.result-icon.success {
  background: var(--success-light, #e8f5e9);
  color: var(--success);
}
.result-icon.warning {
  background: var(--warning-light, #fff3e0);
  color: var(--warning);
}
.result-text {
  flex: 1;
}
.result-title {
  font-size: var(--text-base);
  font-weight: 600;
  color: var(--text-primary);
  margin: 0 0 2px;
}
.result-detail {
  font-size: var(--text-sm);
  color: var(--text-secondary);
  margin: 0;
}
.result-warning {
  display: flex;
  align-items: flex-start;
  gap: 8px;
  padding: 10px 12px;
  background: var(--warning-light, #fff3e0);
  border-radius: var(--radius-sm);
  font-size: var(--text-sm);
  color: var(--warning);
  margin-bottom: 12px;
}
.result-list {
  max-height: 240px;
  overflow-y: auto;
  display: flex;
  flex-direction: column;
  gap: 4px;
  margin-bottom: 16px;
}
.result-item {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 6px 8px;
  border-radius: var(--radius-sm);
  font-size: 12px;
  color: var(--text-secondary);
  background: var(--bg-hover);
}
.result-item.failed {
  color: var(--danger);
}
.item-icon {
  flex-shrink: 0;
  display: flex;
}
.item-ref {
  font-family: var(--font-mono);
  max-width: 200px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.item-arrow {
  color: var(--text-tertiary);
  flex-shrink: 0;
}
.item-path {
  color: var(--accent);
  flex-shrink: 0;
}
.item-error {
  color: var(--danger);
  margin-left: auto;
  flex-shrink: 0;
}
.item-skipped {
  color: var(--text-tertiary);
  margin-left: auto;
  flex-shrink: 0;
}
.result-actions {
  display: flex;
  justify-content: flex-end;
  gap: 8px;
}
</style>
