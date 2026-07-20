<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Trash2, Upload, FileArchive, Loader2, Download, RotateCcw } from 'lucide-vue-next'
import {
  backupNow,
  importBackup,
  getBackupList,
  getBackupSettings,
  updateBackupSettings,
  downloadBackup,
  restoreBackup,
  deleteBackup,
  type BackupFrequency,
  type UserBackupItem,
} from '@/modules/system/api'
import { triggerBlobDownload } from '@/utils/file'
import { handleApiError, unwrapResult } from '@/utils/apiResult'
import { formatUpdated } from '@/utils/formatTime'
import UiCard from '@/components/ui/UiCard.vue'

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
const frequency = ref<BackupFrequency>('daily')
const savingFreq = ref(false)
const history = ref<UserBackupItem[]>([])
const historyLoading = ref(false)

function formatSize(bytes: number): string {
  if (!bytes || bytes <= 0) return '—'
  if (bytes < 1024) return `${bytes} B`
  if (bytes < 1024 * 1024) return `${(bytes / 1024).toFixed(1)} KB`
  return `${(bytes / 1024 / 1024).toFixed(1)} MB`
}

function triggerLabel(t: string): string {
  return t === 'AUTO' ? '自动' : '手动'
}

function statusLabel(s: string): string {
  return s === 'OK' ? '成功' : '失败'
}

async function loadHistory() {
  historyLoading.value = true
  try {
    history.value = await unwrapResult(getBackupList())
  } catch (e) {
    history.value = []
    handleApiError(e, '加载备份历史失败')
  } finally {
    historyLoading.value = false
  }
}

async function loadSettings() {
  try {
    const s = await unwrapResult(getBackupSettings())
    frequency.value = s.frequency || 'daily'
  } catch {
    frequency.value = 'daily'
  }
}

async function onFrequencyChange(val: BackupFrequency) {
  savingFreq.value = true
  try {
    await unwrapResult(updateBackupSettings({ frequency: val }))
    ElMessage.success('已更新自动备份频率')
  } catch (e) {
    handleApiError(e, '保存失败')
    await loadSettings()
  } finally {
    savingFreq.value = false
  }
}

async function handleBackupNow() {
  backingUp.value = true
  try {
    const res = await backupNow()
    const blob = new Blob([res.data], { type: 'application/zip' })
    const stamp = new Date().toISOString().slice(0, 19).replace(/[:T]/g, (c) => (c === 'T' ? '-' : ''))
    triggerBlobDownload(blob, `personal-hub-backup-${stamp}.zip`)
    ElMessage.success('备份完成（已保存到服务器并下载）')
    await loadHistory()
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

async function handleDownload(item: UserBackupItem) {
  try {
    const res = await downloadBackup(item.id)
    const blob = new Blob([res.data], { type: 'application/zip' })
    const stamp = (item.createdAt || '').replace(/[:\sT]/g, '-').slice(0, 16)
    triggerBlobDownload(blob, `personal-hub-backup-${stamp || item.id}.zip`)
  } catch (e) {
    handleApiError(e, '下载失败')
  }
}

async function handleRestore(item: UserBackupItem) {
  try {
    await ElMessageBox.confirm(
      '将用该历史备份全量覆盖当前数据；恢复前会先自动保存一份当前快照。确定继续？',
      '从历史恢复',
      { confirmButtonText: '确定恢复', cancelButtonText: '取消', type: 'warning' },
    )
    await unwrapResult(restoreBackup(item.id))
    ElMessage.success('已从历史备份恢复，请刷新页面')
    await loadHistory()
  } catch (e) {
    if (e === 'cancel' || (e && typeof e === 'object' && 'action' in e)) return
    handleApiError(e, '恢复失败')
  }
}

async function handleDelete(item: UserBackupItem) {
  try {
    await ElMessageBox.confirm('确定删除该服务器备份？删除后不可恢复。', '删除备份', {
      confirmButtonText: '删除', cancelButtonText: '取消', type: 'error',
    })
    await unwrapResult(deleteBackup(item.id))
    ElMessage.success('已删除')
    await loadHistory()
  } catch (e) {
    if (e === 'cancel' || (e && typeof e === 'object' && 'action' in e)) return
    handleApiError(e, '删除失败')
  }
}

onMounted(() => {
  cacheSize.value = estimateCacheSize()
  loadSettings()
  loadHistory()
})
</script>

<template>
  <!-- 多根节点，对齐高级 Tab：card-title + 卡片内 section-title -->
  <UiCard class="settings-card">
    <h3 class="card-title">缓存管理</h3>
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
  </UiCard>

  <UiCard class="settings-card">
    <h3 class="card-title">数据备份</h3>
    <p class="cache-hint" style="margin-bottom: var(--sp-3)">
      导出业务数据、用户资料、头像与相关文件为 ZIP；不含密码、用户名、通知与审计日志。立即备份会写入服务器历史并下载到本地。
    </p>

    <div class="freq-row">
      <span class="freq-label">自动备份</span>
      <el-select
        v-model="frequency"
        size="small"
        style="width: 140px"
        :disabled="savingFreq"
        @change="onFrequencyChange"
      >
        <el-option label="关闭" value="off" />
        <el-option label="每天" value="daily" />
        <el-option label="每周" value="weekly" />
      </el-select>
      <span class="cache-hint">服务器保留最近 7 份成功备份（凌晨 2:00）</span>
    </div>

    <div class="action-row">
      <button class="action-btn" :disabled="backingUp" @click="handleBackupNow">
        <Loader2 v-if="backingUp" :size="14" class="spin" />
        <FileArchive v-else :size="14" />
        {{ backingUp ? '备份中...' : '立即备份' }}
      </button>
    </div>

    <div class="setting-section">
      <h3 class="section-title">服务器备份历史</h3>
      <div v-if="historyLoading" class="cache-hint">加载中…</div>
      <p v-else-if="history.length === 0" class="cache-hint">暂无服务器备份</p>
      <div v-else class="history-list">
        <div v-for="item in history" :key="item.id" class="history-row">
          <div class="history-main">
            <span class="history-time">{{ formatUpdated(item.createdAt, '-') }}</span>
            <span class="history-meta">{{ triggerLabel(item.triggerType) }} · {{ formatSize(item.fileSize) }}</span>
            <span class="history-status" :class="{ fail: item.status !== 'OK' }">{{ statusLabel(item.status) }}</span>
            <span v-if="item.errorMessage" class="history-err" :title="item.errorMessage">{{ item.errorMessage }}</span>
          </div>
          <div class="history-actions">
            <button
              type="button"
              class="icon-action"
              title="下载"
              :disabled="item.status !== 'OK'"
              @click="handleDownload(item)"
            >
              <Download :size="14" />
            </button>
            <button
              type="button"
              class="icon-action"
              title="恢复"
              :disabled="item.status !== 'OK'"
              @click="handleRestore(item)"
            >
              <RotateCcw :size="14" />
            </button>
            <button type="button" class="icon-action danger" title="删除" @click="handleDelete(item)">
              <Trash2 :size="14" />
            </button>
          </div>
        </div>
      </div>
    </div>

    <div class="setting-section">
      <h3 class="section-title">数据恢复</h3>
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
    </div>
  </UiCard>
</template>

<style scoped>
.cache-info { margin-bottom: var(--sp-3); }
.cache-detail { margin: 0 0 var(--sp-1); font-size: var(--text-sm); color: var(--text-primary); }
.cache-hint { margin: 0; font-size: var(--text-xs); color: var(--text-tertiary); line-height: 1.5; }

.freq-row {
  display: flex;
  align-items: center;
  gap: var(--sp-3);
  flex-wrap: wrap;
  margin-bottom: var(--sp-3);
}
.freq-label {
  font-size: var(--text-sm);
  color: var(--text-secondary);
}

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

.history-list {
  display: flex;
  flex-direction: column;
  gap: var(--sp-2);
  margin-bottom: var(--sp-2);
}
.history-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: var(--sp-3);
  padding: var(--sp-2) var(--sp-3);
  border: 1px solid var(--border-color);
  border-radius: var(--radius-sm);
  background: var(--bg-card);
}
.history-main {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: var(--sp-2);
  min-width: 0;
  font-size: var(--text-sm);
}
.history-time { color: var(--text-primary); font-weight: 500; }
.history-meta { color: var(--text-tertiary); font-size: var(--text-xs); }
.history-status { font-size: var(--text-xs); color: var(--success); }
.history-status.fail { color: var(--danger); }
.history-err {
  font-size: var(--text-xs);
  color: var(--danger);
  max-width: 180px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.history-actions { display: flex; gap: 2px; flex-shrink: 0; }
.icon-action {
  display: inline-flex; align-items: center; justify-content: center;
  width: 28px; height: 28px; border: none; border-radius: var(--radius-sm);
  background: transparent; color: var(--text-tertiary); cursor: pointer;
  transition: background var(--transition), color var(--transition);
}
.icon-action:hover:not(:disabled) { background: var(--bg-hover); color: var(--text-primary); }
.icon-action:disabled { opacity: 0.35; cursor: not-allowed; }
.icon-action.danger:hover:not(:disabled) { color: var(--danger); background: var(--danger-light); }

@keyframes spin { from { transform: rotate(0deg); } to { transform: rotate(360deg); } }
.spin { animation: spin 1s linear infinite; }
</style>
