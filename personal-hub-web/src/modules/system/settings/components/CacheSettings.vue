<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Trash2 } from 'lucide-vue-next'

const cacheSize = ref('计算中...')
const PROTECTED_KEYS = ['token', 'theme-preference', 'accent-preference', 'appearance-config']

function estimateCacheSize(): string {
  let totalBytes = 0
  for (let i = 0; i < localStorage.length; i++) {
    const key = localStorage.key(i)
    if (key && !PROTECTED_KEYS.includes(key)) {
      totalBytes += (localStorage.getItem(key) || '').length * 2 // UTF-16
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
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'info',
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

onMounted(() => { cacheSize.value = estimateCacheSize() })
</script>

<template>
  <div class="cache-settings">
    <section class="setting-section">
      <h3 class="section-title">本地缓存</h3>
      <div class="cache-info">
        <p class="cache-detail">
          当前缓存数据：<strong>{{ cacheSize }}</strong>
        </p>
        <p class="cache-hint">
          缓存用于加速页面加载。清空后系统会自动重建，登录状态和账号偏好不会丢失。
        </p>
      </div>
      <button class="action-btn" @click="handleClearCache">
        <Trash2 :size="14" />
        清空缓存
      </button>
    </section>

    <section class="setting-section">
      <h3 class="section-title">数据备份与导出</h3>
      <div class="disabled-area">
        <button class="action-btn" disabled>导出数据</button>
        <button class="action-btn" disabled>导入数据</button>
        <p class="disabled-hint">数据备份与导入导出功能即将上线，敬请期待。</p>
      </div>
    </section>
  </div>
</template>

<style scoped>
.cache-settings { max-width: 598px; }
.setting-section { margin-bottom: 28px; }
.section-title {
  margin: 0 0 14px;
  font-size: 13px;
  font-weight: 600;
  color: var(--text-secondary);
}
.cache-info { margin-bottom: 16px; }
.cache-detail { margin: 0 0 6px; font-size: var(--text-sm); color: var(--text-primary); }
.cache-hint { margin: 0; font-size: var(--text-xs); color: var(--text-tertiary); line-height: 1.5; }

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

.disabled-hint {
  margin: 8px 0 0; font-size: var(--text-xs); color: var(--text-placeholder);
}
</style>
