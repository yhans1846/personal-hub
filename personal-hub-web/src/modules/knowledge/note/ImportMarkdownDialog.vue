<script setup lang="ts">
import { computed, ref, onMounted } from 'vue'
import { importMarkdownFile, importMarkdownContent } from '@/modules/knowledge/api'
import { getCategories } from '@/modules/knowledge/api'
import { getTags } from '@/modules/knowledge/api'
import { ElMessage } from 'element-plus'
import { Upload, FileText, CheckCircle, XCircle, AlertTriangle, X } from 'lucide-vue-next'
import type { ImportReport, ImportResourceResult } from '@/types/note'
import type { CategoryVO } from '@/types/category'
import type { TagVO } from '@/types/tag'
import { handleApiError } from '@/utils/apiResult'
import UiTooltip from '@/components/UiTooltip.vue'

const emit = defineEmits<{ done: [] }>()

const props = defineProps<{
  /** 当前选中文件夹；空/未分类/全部/首页 → 未分类 */
  folderId?: number | null
}>()

// ─── Tab ───
const activeTab = ref<'file' | 'content'>('file')

// ─── 分类 & 标签 ───
const categories = ref<CategoryVO[]>([])
const tags = ref<TagVO[]>([])
const selectedCategoryIds = ref<number[]>([])
const selectedTagIds = ref<number[]>([])
onMounted(async () => {
  try {
    const [catRes, tagRes] = await Promise.all([getCategories('note'), getTags()])
    categories.value = catRes.data.data
    tags.value = tagRes.data.data
  } catch { /* ignore */ }
})

// ─── 文件导入（可多选，每个 .md 一篇笔记）───
const files = ref<File[]>([])
const fileInput = ref<HTMLInputElement | null>(null)
const fileTitle = ref('')
const baseDir = ref('')
const isMulti = computed(() => files.value.length > 1)

function onFileChange(e: Event) {
  const input = e.target as HTMLInputElement
  const picked = Array.from(input.files ?? []).filter((f) =>
    f.name.toLowerCase().endsWith('.md'),
  )
  if (input.files && input.files.length > 0 && picked.length === 0) {
    ElMessage.warning('请选择 .md 文件')
    input.value = ''
    return
  }
  files.value = picked
  if (picked.length === 1) {
    const name = picked[0].name
    fileTitle.value = name.toLowerCase().endsWith('.md') ? name.slice(0, -3) : name
  } else {
    fileTitle.value = ''
  }
}

function removeFile(index: number) {
  files.value = files.value.filter((_, i) => i !== index)
  if (files.value.length === 1) {
    const name = files.value[0].name
    fileTitle.value = name.toLowerCase().endsWith('.md') ? name.slice(0, -3) : name
  } else if (files.value.length === 0) {
    fileTitle.value = ''
    if (fileInput.value) fileInput.value.value = ''
  }
}

// ─── 内容粘贴 ───
const pastedContent = ref('')
const contentTitle = ref('')

// ─── 导入状态 ───
const importing = ref(false)
const importProgress = ref('')
const report = ref<ImportReport | null>(null)
const noteResults = ref<{ name: string; ok: boolean; message: string }[]>([])
const anyNoteImported = ref(false)

function emptyReport(): ImportReport {
  return {
    total: 0,
    success: 0,
    skipped: 0,
    failed: 0,
    resources: [],
  }
}

function mergeReports(target: ImportReport, part: ImportReport, noteLabel?: string) {
  target.total += part.total
  target.success += part.success
  target.skipped += part.skipped
  target.failed += part.failed
  target.resources.push(
    ...(part.resources ?? []).map((r) => ({
      ...r,
      noteLabel: noteLabel || r.noteLabel,
    })),
  )
  if (part.warning) {
    const tagged = noteLabel ? `「${noteLabel}」${part.warning}` : part.warning
    target.warning = target.warning ? `${target.warning}\n${tagged}` : tagged
  }
  if (part.noteId != null && target.noteId == null) {
    target.noteId = part.noteId
  }
}

function noteLabelFromFile(file: File, overrideTitle?: string) {
  if (overrideTitle?.trim()) return overrideTitle.trim()
  return file.name.replace(/\.md$/i, '')
}

async function handleImport() {
  importing.value = true
  report.value = null
  noteResults.value = []
  importProgress.value = ''
  try {
    if (activeTab.value === 'file') {
      if (files.value.length === 0) {
        ElMessage.warning('请选择 Markdown 文件')
        return
      }

      const merged = emptyReport()
      const results: { name: string; ok: boolean; message: string }[] = []
      let okCount = 0
      let failCount = 0
      const total = files.value.length

      for (let i = 0; i < total; i++) {
        const file = files.value[i]
        importProgress.value = `正在导入 ${i + 1}/${total}：${file.name}`
        const title =
          total === 1 && fileTitle.value.trim()
            ? fileTitle.value.trim()
            : undefined
        const noteLabel = noteLabelFromFile(file, title)
        try {
          const res = await importMarkdownFile(
            file,
            title,
            selectedCategoryIds.value,
            selectedTagIds.value,
            baseDir.value || undefined,
            props.folderId ?? null,
          )
          const part = res.data.data
          mergeReports(merged, part, noteLabel)
          if (part.noteId != null) {
            okCount++
            anyNoteImported.value = true
            results.push({ name: file.name, ok: true, message: '导入成功' })
          } else {
            failCount++
            results.push({ name: file.name, ok: false, message: '未返回笔记 ID' })
          }
        } catch (e: unknown) {
          failCount++
          const msg = e instanceof Error ? e.message : '导入失败'
          results.push({ name: file.name, ok: false, message: msg })
        }
      }

      noteResults.value = results
      report.value = merged

      if (failCount === 0) {
        ElMessage.success(
          total === 1
            ? `导入成功（${merged.total} 个资源）`
            : `已导入 ${okCount} 篇笔记（资源 ${merged.success}/${merged.total}）`,
        )
      } else if (okCount === 0) {
        ElMessage.error(total === 1 ? '导入失败' : `${failCount} 篇全部失败`)
      } else {
        ElMessage.info(`完成：成功 ${okCount} 篇，失败 ${failCount} 篇`)
      }
    } else {
      if (!pastedContent.value.trim()) {
        ElMessage.warning('请粘贴 Markdown 内容')
        return
      }
      const res = await importMarkdownContent({
        content: pastedContent.value,
        title: contentTitle.value || undefined,
        categoryIds: selectedCategoryIds.value,
        tagIds: selectedTagIds.value,
        folderId: props.folderId ?? null,
      })
      report.value = res.data.data
      if (report.value.noteId != null) {
        anyNoteImported.value = true
        const label = contentTitle.value.trim() || '粘贴内容'
        noteResults.value = [{ name: label, ok: true, message: '导入成功' }]
        report.value = {
          ...report.value,
          resources: (report.value.resources ?? []).map((r) => ({
            ...r,
            noteLabel: label,
          })),
        }
      }

      if (report.value.failed === 0 && report.value.skipped === 0) {
        ElMessage.success(`导入成功（${report.value.total} 个资源）`)
      } else {
        ElMessage.info(
          `导入完成：${report.value.success} 成功，${report.value.failed} 失败，${report.value.skipped} 跳过`,
        )
      }
    }
  } catch (e: unknown) {
    handleApiError(e, '导入失败')
  } finally {
    importing.value = false
    importProgress.value = ''
  }
}

function continueImport() {
  report.value = null
  noteResults.value = []
}

function noteTip(r: { name: string; ok: boolean; message: string }) {
  return `${r.name} — ${r.message}`
}

function noteLine(r: { name: string; ok: boolean; message: string }) {
  return r.ok ? r.name : `${r.name} — ${r.message}`
}

function resourceTip(r: ImportResourceResult) {
  const parts: string[] = []
  if (r.noteLabel) parts.push(`笔记「${r.noteLabel}」`)
  parts.push(r.originalRef)
  if (r.resolvedPath) parts.push(`→ ${r.resolvedPath}`)
  if (r.message) parts.push(r.message)
  return parts.join(' — ')
}

/** 列表单行展示（完整信息在悬停 tip） */
function resourceLine(r: ImportResourceResult) {
  const note = r.noteLabel ? `「${r.noteLabel}」` : ''
  if (r.success && r.resolvedPath) {
    return [note, `${r.originalRef} → ${r.resolvedPath}`].filter(Boolean).join(' ')
  }
  if (r.success) {
    return [note, r.originalRef].filter(Boolean).join(' ')
  }
  const short =
    r.message.includes('相对路径') ? '相对路径无法定位（需填写资源目录）'
      : r.message
  return [note, `${r.originalRef} — ${short}`].filter(Boolean).join(' ')
}

function handleClose() {
  if (anyNoteImported.value || report.value?.noteId) {
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
        <div
          class="result-icon"
          :class="{
            success: noteResults.every((r) => r.ok) && report.failed === 0,
            warning: noteResults.some((r) => !r.ok) || report.failed > 0,
          }"
        >
          <CheckCircle v-if="noteResults.every((r) => r.ok) && report.failed === 0" :size="24" />
          <AlertTriangle v-else :size="24" />
        </div>
        <div class="result-text">
          <p class="result-title">导入完成</p>
          <p class="result-detail">
            <template v-if="noteResults.length > 1">
              笔记 {{ noteResults.filter((r) => r.ok).length }}/{{ noteResults.length }} ·
            </template>
            资源 {{ report.total }} · {{ report.success }} 成功 · {{ report.failed }} 失败 · {{ report.skipped }} 跳过
          </p>
        </div>
      </div>

      <!-- 各笔记结果 -->
      <div v-if="noteResults.length > 1" class="note-result-list">
        <UiTooltip
          v-for="(r, i) in noteResults"
          :key="i"
          :content="noteTip(r)"
          placement="top-start"
        >
          <div class="result-row" :class="{ failed: !r.ok }">
            <span class="item-icon">
              <CheckCircle v-if="r.ok" :size="12" />
              <XCircle v-else :size="12" />
            </span>
            <span class="item-line">{{ noteLine(r) }}</span>
          </div>
        </UiTooltip>
      </div>

      <!-- 相对路径警告 -->
      <div v-if="report.warning" class="result-warning">
        <AlertTriangle :size="14" />
        {{ report.warning }}
      </div>

      <!-- 资源列表 -->
      <div v-if="report.resources.length" class="result-list">
        <UiTooltip
          v-for="(r, i) in report.resources"
          :key="i"
          :content="resourceTip(r)"
          placement="top-start"
        >
          <div
            class="result-row"
            :class="{ failed: !r.success && !r.skipped, skipped: r.skipped }"
          >
            <span class="item-icon">
              <CheckCircle v-if="r.success" :size="12" />
              <XCircle v-else-if="!r.skipped" :size="12" />
              <AlertTriangle v-else :size="12" />
            </span>
            <span class="item-line">{{ resourceLine(r) }}</span>
          </div>
        </UiTooltip>
      </div>

      <div class="result-actions">
        <button class="btn btn-secondary" @click="continueImport">继续导入</button>
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
          <label class="form-label">
            选择 Markdown 文件
            <span class="label-hint">可多选，每个文件一篇笔记</span>
          </label>
          <div class="file-upload-area" @click="fileInput?.click()">
            <Upload :size="24" class="upload-icon" />
            <p v-if="files.length === 0" class="upload-text">点击选择 .md 文件（可多选）</p>
            <p v-else class="upload-text file-selected">已选 {{ files.length }} 个文件</p>
          </div>
          <input
            ref="fileInput"
            type="file"
            accept=".md"
            multiple
            style="display: none"
            @change="onFileChange"
          />
          <ul v-if="files.length > 0" class="file-list">
            <li v-for="(f, idx) in files" :key="`${f.name}-${idx}`" class="file-list-item">
              <FileText :size="14" class="file-list-icon" />
              <span class="file-list-name" :title="f.name">{{ f.name }}</span>
              <button
                type="button"
                class="file-list-remove"
                :disabled="importing"
                aria-label="移除"
                @click.stop="removeFile(idx)"
              >
                <X :size="14" />
              </button>
            </li>
          </ul>
        </div>

        <div v-if="!isMulti" class="form-group">
          <label class="form-label">笔记标题（可选）</label>
          <input v-model="fileTitle" class="form-input" placeholder="默认为文件名" />
        </div>
        <p v-else class="multi-hint">多文件导入时，每篇笔记标题使用对应文件名（去掉 .md）。</p>

        <div class="form-group">
          <label class="form-label">
            Markdown 文件所在目录（可选）
            <span class="label-hint">用于解析相对路径图片；多文件共用</span>
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

      <p v-if="importProgress" class="import-progress">{{ importProgress }}</p>

      <!-- 提交 -->
      <div class="form-actions">
        <button
          class="btn btn-primary"
          :disabled="importing || (activeTab === 'file' && files.length === 0)"
          @click="handleImport"
        >
          <template v-if="importing">导入中...</template>
          <template v-else-if="activeTab === 'file' && files.length > 1">
            导入 {{ files.length }} 篇
          </template>
          <template v-else>开始导入</template>
        </button>
      </div>
    </div>
  </div>
</template>

<style scoped>
.import-dialog {
  padding: 0;
}
.import-result {
  min-height: 0;
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
.file-list {
  list-style: none;
  margin: 0;
  padding: 0;
  max-height: 160px;
  overflow-y: auto;
  display: flex;
  flex-direction: column;
  gap: 4px;
}
.file-list-item {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 6px 8px;
  border-radius: var(--radius-sm);
  background: var(--bg-hover);
  font-size: var(--text-sm);
}
.file-list-icon {
  flex-shrink: 0;
  color: var(--text-tertiary);
}
.file-list-name {
  flex: 1;
  min-width: 0;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  color: var(--text-primary);
}
.file-list-remove {
  flex-shrink: 0;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 24px;
  height: 24px;
  border: none;
  border-radius: var(--radius-sm);
  background: transparent;
  color: var(--text-tertiary);
  cursor: pointer;
}
.file-list-remove:hover:not(:disabled) {
  color: var(--danger);
  background: var(--bg-card);
}
.file-list-remove:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}
.multi-hint {
  margin: 0;
  font-size: 12px;
  color: var(--text-tertiary);
}
.import-progress {
  margin: 0;
  font-size: var(--text-sm);
  color: var(--text-secondary);
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
  white-space: pre-line;
}
.note-result-list {
  max-height: 240px;
  overflow-x: hidden;
  overflow-y: auto;
  display: flex;
  flex-direction: column;
  gap: 6px;
  margin-bottom: 12px;
}
.result-list {
  max-height: 280px;
  overflow-x: hidden;
  overflow-y: auto;
  display: flex;
  flex-direction: column;
  gap: 6px;
  margin-bottom: 16px;
}
/* el-tooltip 触发器：占满宽度，且禁止在 flex 列表里被压扁 */
.note-result-list :deep(.el-tooltip__trigger),
.result-list :deep(.el-tooltip__trigger),
.note-result-list :deep(.el-only-child__content),
.result-list :deep(.el-only-child__content) {
  display: block !important;
  width: 100%;
  max-width: 100%;
  min-width: 0;
  flex-shrink: 0;
  height: auto !important;
}
.result-row {
  display: flex;
  flex-wrap: nowrap;
  align-items: center;
  gap: 8px;
  flex-shrink: 0;
  width: 100%;
  max-width: 100%;
  box-sizing: border-box;
  min-height: 36px;
  height: auto;
  padding: 8px 12px;
  border-radius: var(--radius-sm);
  font-size: 13px;
  line-height: 1.5;
  color: var(--text-secondary);
  background: var(--bg-hover);
  overflow: hidden;
  cursor: default;
}
.result-row.failed {
  color: var(--danger);
  background: var(--danger-light, #fef0f0);
}
.result-row.skipped {
  color: var(--warning, #e6a23c);
}
.item-icon {
  flex: 0 0 auto;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 16px;
  height: 16px;
  line-height: 1;
}
.item-icon :deep(svg) {
  display: block;
  width: 14px;
  height: 14px;
  flex-shrink: 0;
}
.item-line {
  flex: 1 1 auto;
  min-width: 0;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  font-family: var(--font-mono);
  font-size: 13px;
  line-height: 1.5;
  color: var(--text-primary);
}
.result-row.failed .item-line,
.result-row.skipped .item-line {
  color: inherit;
}
.result-actions {
  display: flex;
  justify-content: flex-end;
  gap: 8px;
}
</style>
