<script setup lang="ts">
import { ref, onMounted, reactive } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Trash2, Download, Upload, RotateCcw, FileArchive, Loader2 } from 'lucide-vue-next'
import { exportData, backupNow, getBackupList, importBackup } from '@/modules/system/api'

// ===== 缓存 =====
const cacheSize = ref('计算中...')
const PROTECTED_KEYS = ['token', 'theme-preference', 'accent-preference', 'appearance-config', 'notification-config', 'feature-flags']

function estimateCacheSize(): string {
  let totalBytes = 0
  for (let i = 0; i < localStorage.length; i++) {
    const key = localStorage.key(i)
    if (key && !PROTECTED_KEYS.includes(key)) {
      totalBytes += (localStorage.getItem(key) || '').length * 2
    }
  }
  if (totalBytes === 0) return '约 0 KB'
  if (totalBytes < 1024) return `约 ${totalBytes} B`
  if (totalBytes < 1024 * 1024) return `约 ${(totalBytes / 1024).toFixed(1)} KB`
  return `约 ${(totalBytes / 1024 / 1024).toFixed(1)} MB`
}

async function handleClearCache() {
  try {
    await ElMessageBox.confirm('清空缓存后，部分页面首次加载可能略慢，数据不会丢失。', '清空缓存', {
      confirmButtonText: '确定', cancelButtonText: '取消', type: 'info',
    })
    const sizeBefore = estimateCacheSize()
    const keysToRemove: string[] = []
    for (let i = 0; i < localStorage.length; i++) {
      const key = localStorage.key(i)
      if (key && !PROTECTED_KEYS.includes(key)) keysToRemove.push(key)
    }
    keysToRemove.forEach(key => localStorage.removeItem(key))
    cacheSize.value = estimateCacheSize()
    ElMessage.success(`已清空缓存（${sizeBefore}）`)
  } catch { /* cancelled */ }
}

// ===== 导出 =====
const MODULE_OPTIONS = [
  { value: 'notes', label: '笔记' },
  { value: 'diaries', label: '日记' },
  { value: 'todos', label: '待办' },
  { value: 'bookmarks', label: '收藏夹' },
  { value: 'study', label: '学习记录' },
  { value: 'reading', label: '阅读记录' },
  { value: 'tags', label: '标签' },
  { value: 'categories', label: '分类' },
  { value: 'settings', label: '系统配置' },
]

const selectedModules = ref<string[]>(['notes', 'diaries', 'todos'])
const exportFormat = ref<'markdown' | 'json'>('markdown')
const exporting = ref(false)

function toggleModule(value: string) {
  const idx = selectedModules.value.indexOf(value)
  if (idx >= 0) selectedModules.value.splice(idx, 1)
  else selectedModules.value.push(value)
}

async function handleExport() {
  if (!selectedModules.value.length) {
    ElMessage.warning('请选择至少一个模块')
    return
  }
  exporting.value = true
  try {
    const res = await exportData({ modules: selectedModules.value, format: exportFormat.value })
    const url = res.data.data?.downloadUrl
    if (url) {
      window.open(url, '_blank')
      ElMessage.success('数据导出成功')
    }
  } catch {
    ElMessage.error('导出失败，请稍后重试')
  } finally {
    exporting.value = false
  }
}

// ===== 备份 =====
const backingUp = ref(false)
const backupList = ref<any[]>([])
const loadingBackups = ref(false)
const importFile = ref<File | null>(null)

async function handleBackupNow() {
  backingUp.value = true
  try {
    const res = await backupNow()
    const url = res.data.data?.downloadUrl
    ElMessage.success('备份完成')
    if (url) window.open(url, '_blank')
    await loadBackups()
  } catch {
    ElMessage.error('备份失败')
  } finally {
    backingUp.value = false
  }
}

async function loadBackups() {
  loadingBackups.value = true
  try {
    const res = await getBackupList()
    backupList.value = res.data.data || []
  } catch {
    // 后端接口可能尚未就绪
    backupList.value = []
  } finally {
    loadingBackups.value = false
  }
}

function handleFileChange(e: Event) {
  const target = e.target as HTMLInputElement
  if (target.files?.length) importFile.value = target.files[0]
}

async function handleImport() {
  if (!importFile.value) {
    ElMessage.warning('请先选择备份文件')
    return
  }
  try {
    await ElMessageBox.confirm(
      '导入备份将覆盖当前数据，此操作不可撤销。确定继续吗？',
      '导入备份',
      { confirmButtonText: '确定导入', cancelButtonText: '取消', type: 'warning' },
    )
    await importBackup(importFile.value)
    ElMessage.success('数据恢复成功')
    importFile.value = null
  } catch {
    if (importFile.value) ElMessage.error('导入失败')
  }
}

onMounted(() => {
  cacheSize.value = estimateCacheSize()
  loadBackups()
})
</script>

<template>
  <div class="data-management">
    <!-- 缓存管理 -->
    <section class="setting-section">
      <h3 class="section-title">缓存管理</h3>
      <div class="cache-info">
        <p class="cache-detail">
          当前缓存数据：<strong>{{ cacheSize }}</strong>
        </p>
        <p class="cache-hint">清空后系统会自动重建，登录状态和账号偏好不会丢失。</p>
      </div>
      <button class="action-btn" @click="handleClearCache">
        <Trash2 :size="14" />
        清空缓存
      </button>
    </section>

    <!-- 数据导出 -->
    <section class="setting-section">
      <h3 class="section-title">数据导出</h3>
      <div class="export-modules">
        <span class="field-label">选择导出内容</span>
        <div class="chip-group">
          <button
            v-for="mo in MODULE_OPTIONS"
            :key="mo.value"
            :class="['chip', { active: selectedModules.includes(mo.value) }]"
            @click="toggleModule(mo.value)"
          >
            {{ mo.label }}
          </button>
        </div>
      </div>
      <div class="export-format">
        <span class="field-label">导出格式</span>
        <div class="inline-options">
          <button
            :class="['inline-btn', { active: exportFormat === 'markdown' }]"
            @click="exportFormat = 'markdown'"
          >Markdown (ZIP)</button>
          <button
            :class="['inline-btn', { active: exportFormat === 'json' }]"
            @click="exportFormat = 'json'"
          >JSON</button>
        </div>
      </div>
      <button class="action-btn primary" :disabled="exporting" @click="handleExport">
        <Loader2 v-if="exporting" :size="14" class="spin" />
        <Download v-else :size="14" />
        {{ exporting ? '导出中...' : '导出数据' }}
      </button>
    </section>

    <!-- 数据备份 -->
    <section class="setting-section">
      <h3 class="section-title">数据备份</h3>
      <div class="backup-actions">
        <div class="action-row">
          <button class="action-btn" :disabled="backingUp" @click="handleBackupNow">
            <Loader2 v-if="backingUp" :size="14" class="spin" />
            <FileArchive v-else :size="14" />
            {{ backingUp ? '备份中...' : '立即备份' }}
          </button>
        </div>
        <div class="backup-divider">数据恢复</div>
        <div class="action-row">
          <div class="file-select">
            <label class="file-label">
              <Upload :size="14" />
              {{ importFile ? importFile.name : '选择备份文件' }}
              <input type="file" accept=".json,.gz,.zip" class="file-input" @change="handleFileChange" />
            </label>
          </div>
          <button class="action-btn danger" :disabled="!importFile" @click="handleImport">
            导入恢复
          </button>
        </div>
        <p class="import-warning">警告：恢复操作将覆盖当前数据，不可撤销。</p>
      </div>
    </section>
  </div>
</template>

<style scoped>
.data-management { }

.setting-section { margin-bottom: 20px; }

.section-title {
  margin: 0 0 10px;
  font-size: 13px;
  font-weight: 600;
  color: var(--text-secondary);
}

.cache-info { margin-bottom: 12px; }
.cache-detail { margin: 0 0 4px; font-size: var(--text-sm); color: var(--text-primary); }
.cache-hint { margin: 0; font-size: var(--text-xs); color: var(--text-tertiary); line-height: 1.5; }

/* ─── 按钮 ─── */
.action-btn {
  display: inline-flex; align-items: center; gap: 6px;
  padding: 8px 16px; font-size: 13px;
  border: 1px solid var(--border-color); background: var(--bg-card);
  border-radius: 6px; cursor: pointer;
  color: var(--text-secondary); transition: all 150ms ease;
  margin-right: 8px; margin-bottom: 8px;
}
.action-btn:hover:not(:disabled) { border-color: var(--accent); color: var(--accent); }
.action-btn:disabled { opacity: 0.4; cursor: not-allowed; }
.action-btn.primary { color: var(--accent); border-color: var(--accent); }
.action-btn.danger { color: var(--danger); border-color: var(--danger); }
.action-btn.danger:hover:not(:disabled) { background: var(--danger-light); }

/* ─── Chip 组 ─── */
.chip-group { display: flex; gap: 6px; flex-wrap: wrap; margin-bottom: 12px; }
.chip {
  padding: 5px 12px; font-size: 12px;
  border: 1px solid var(--border-color); background: var(--bg-card);
  border-radius: 20px; color: var(--text-secondary);
  cursor: pointer; transition: all 150ms ease;
}
.chip:hover { border-color: var(--accent); color: var(--accent); }
.chip.active { border-color: var(--accent); color: var(--accent); background: color-mix(in srgb, var(--accent) 10%, transparent); }

.field-label { display: block; font-size: 12px; color: var(--text-tertiary); margin-bottom: 6px; }

.export-format { margin-bottom: 12px; }

.inline-options { display: flex; gap: 4px; }
.inline-btn {
  padding: 6px 14px; font-size: 13px;
  border: 1px solid var(--border-color); background: var(--bg-card);
  border-radius: 6px; color: var(--text-secondary);
  cursor: pointer; transition: all 150ms ease;
}
.inline-btn:hover { border-color: var(--accent); color: var(--accent); }
.inline-btn.active { border-color: var(--accent); color: var(--accent); background: color-mix(in srgb, var(--accent) 8%, transparent); }

/* ─── 备份 ─── */
.backup-actions { margin-top: 4px; }
.action-row { display: flex; align-items: center; gap: 8px; margin-bottom: 12px; }
.backup-divider {
  font-size: 11px; font-weight: 600; color: var(--text-tertiary);
  text-transform: uppercase; letter-spacing: 0.5px;
  margin: 16px 0 12px; padding-top: 12px;
  border-top: 1px solid var(--border-light);
}
.file-select { flex: 1; }
.file-label {
  display: flex; align-items: center; gap: 6px;
  padding: 8px 14px; font-size: 13px;
  border: 1px dashed var(--border-color); background: var(--bg-hover);
  border-radius: 6px; color: var(--text-tertiary);
  cursor: pointer; transition: all 150ms ease;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.file-label:hover { border-color: var(--accent); color: var(--accent); }
.file-input { display: none; }
.import-warning { font-size: 11px; color: var(--danger); margin: 4px 0 0; }

/* ─── 动画 ─── */
@keyframes spin { from { transform: rotate(0deg); } to { transform: rotate(360deg); } }
.spin { animation: spin 1s linear infinite; }
</style>
