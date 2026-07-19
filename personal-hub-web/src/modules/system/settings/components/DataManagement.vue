<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Trash2, Upload, FileArchive, Loader2 } from 'lucide-vue-next'
import { backupNow, importBackup } from '@/modules/system/api'
import { triggerBlobDownload } from '@/utils/file'
import { handleApiError } from '@/utils/apiResult'

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

// ===== 备份 =====
const backingUp = ref(false)
const importing = ref(false)
const importFile = ref<File | null>(null)

async function handleBackupNow() {
  backingUp.value = true
  try {
    const res = await backupNow()
    const blob = new Blob([res.data], { type: 'application/zip' })
    const stamp = new Date().toISOString().slice(0, 19).replace(/[:T]/g, (c) => (c === 'T' ? '-' : ''))
    triggerBlobDownload(blob, `personal-hub-backup-${stamp}.zip`)
    ElMessage.success('备份完成')
  } catch (e) {
    handleApiError(e, '备份失败')
  } finally {
    backingUp.value = false
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
    importing.value = true
    await importBackup(importFile.value)
    ElMessage.success('数据恢复成功，请刷新页面')
    importFile.value = null
  } catch (e) {
    if (importing.value) handleApiError(e, '导入失败')
  } finally {
    importing.value = false
  }
}

onMounted(() => {
  cacheSize.value = estimateCacheSize()
})
</script>

<template>
  <div class="data-management">
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

    <section class="setting-section">
      <h3 class="section-title">数据备份</h3>
      <p class="cache-hint" style="margin-bottom: var(--sp-3)">
        导出业务数据与相关文件为 ZIP；不含密码、通知与审计日志。
      </p>
      <div class="action-row">
        <button class="action-btn" :disabled="backingUp" @click="handleBackupNow">
          <Loader2 v-if="backingUp" :size="14" class="spin" />
          <FileArchive v-else :size="14" />
          {{ backingUp ? '备份中...' : '立即备份' }}
        </button>
      </div>
      <h3 class="backup-divider">数据恢复</h3>
      <div class="action-row">
        <div class="file-select">
          <label class="file-label">
            <Upload :size="14" />
            {{ importFile ? importFile.name : '选择备份文件（.zip）' }}
            <input type="file" accept=".zip,application/zip" class="file-input" @change="handleFileChange" />
          </label>
        </div>
        <button class="action-btn danger" :disabled="!importFile || importing" @click="handleImport">
          <Loader2 v-if="importing" :size="14" class="spin" />
          {{ importing ? '导入中...' : '导入恢复' }}
        </button>
      </div>
      <p class="import-warning">警告：恢复操作将覆盖当前数据，不可撤销。</p>
    </section>
  </div>
</template>

<style scoped>
.setting-section { margin-bottom: var(--sp-5); }
.setting-section:last-child { margin-bottom: 0; }

.section-title {
  margin: 0 0 var(--sp-3);
  font-size: var(--text-sm);
  font-weight: 600;
  color: var(--text-secondary);
}

.cache-info { margin-bottom: var(--sp-3); }
.cache-detail { margin: 0 0 var(--sp-1); font-size: var(--text-sm); color: var(--text-primary); }
.cache-hint { margin: 0; font-size: var(--text-xs); color: var(--text-tertiary); line-height: 1.5; }

.action-btn {
  display: inline-flex; align-items: center; gap: 6px;
  padding: var(--sp-2) var(--sp-4); font-size: var(--text-sm);
  border: 1px solid var(--border-color); background: var(--bg-card);
  border-radius: var(--radius-sm); cursor: pointer;
  color: var(--text-secondary); transition: all var(--transition);
}
.action-btn:hover:not(:disabled) { border-color: var(--accent); color: var(--accent); }
.action-btn:disabled { opacity: 0.4; cursor: not-allowed; }
.action-btn.danger { color: var(--danger); border-color: var(--danger); }
.action-btn.danger:hover:not(:disabled) { background: var(--danger-light); }

.action-row {
  display: flex;
  align-items: center;
  gap: var(--sp-2);
  margin-bottom: var(--sp-3);
}
.action-row:last-child { margin-bottom: 0; }
.backup-divider {
  margin: var(--sp-5) 0 var(--sp-3);
  font-size: var(--text-sm);
  font-weight: 600;
  color: var(--text-secondary);
}
.file-select { flex: 1; min-width: 0; }
.file-label {
  display: flex; align-items: center; gap: 6px;
  padding: var(--sp-2) var(--sp-3); font-size: var(--text-sm);
  border: 1px dashed var(--border-color); background: var(--bg-hover);
  border-radius: var(--radius-sm); color: var(--text-tertiary);
  cursor: pointer; transition: all var(--transition);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.file-label:hover { border-color: var(--accent); color: var(--accent); }
.file-input { display: none; }
.import-warning { font-size: var(--text-xs); color: var(--danger); margin: 0; }

@keyframes spin { from { transform: rotate(0deg); } to { transform: rotate(360deg); } }
.spin { animation: spin 1s linear infinite; }
</style>
