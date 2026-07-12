<script setup lang="ts">
import { ElMessage } from 'element-plus'
import { RotateCcw, FlaskConical, AlertTriangle } from 'lucide-vue-next'
import { useFeatureFlagStore } from '@/store/featureFlagStore'
import { storeToRefs } from 'pinia'

const store = useFeatureFlagStore()
const { flags, FLAG_META } = storeToRefs(store)

function handleToggle(key: any) {
  store.toggle(key)
  const meta = FLAG_META.value.find(m => m.key === key)
  ElMessage.success(flags[key] ? `已开启：${meta?.label}` : `已关闭：${meta?.label}`)
}

function handleReset() {
  store.resetAll()
  ElMessage.success('已关闭所有实验功能')
}
</script>

<template>
  <div class="experimental-features">
    <div class="beta-notice">
      <AlertTriangle :size="16" />
      <span>实验性功能可能不稳定，请谨慎启用。</span>
    </div>

    <section class="setting-section">
      <h3 class="section-title">功能开关</h3>
      <div class="flag-list">
        <div
          v-for="meta in FLAG_META"
          :key="meta.key"
          class="flag-item"
        >
          <div class="flag-info">
            <FlaskConical :size="16" class="flag-icon" />
            <div class="flag-text">
              <span class="flag-label">{{ meta.label }}</span>
              <span class="flag-desc">{{ meta.description }}</span>
            </div>
          </div>
          <button
            :class="['toggle-btn', { active: flags[meta.key] }]"
            @click="handleToggle(meta.key)"
          >
            {{ flags[meta.key] ? '已开启' : '已关闭' }}
          </button>
        </div>
      </div>
    </section>

    <!-- 反馈 -->
    <section class="setting-section">
      <h3 class="section-title">反馈</h3>
      <p class="feedback-text">
        遇到问题或有建议？请在 GitHub 提交 Issue。
      </p>
      <a
        href="https://github.com/yhans1846/personal-hub/issues"
        target="_blank"
        class="feedback-link"
      >前往 GitHub →</a>
    </section>

    <!-- 恢复默认 -->
    <div class="reset-area">
      <button class="reset-btn" @click="handleReset">
        <RotateCcw :size="14" />
        关闭全部
      </button>
    </div>
  </div>
</template>

<style scoped>
.experimental-features { }

.beta-notice {
  display: flex; align-items: center; gap: 8px;
  padding: 10px 14px;
  background: color-mix(in srgb, var(--warning) 10%, transparent);
  border: 1px solid color-mix(in srgb, var(--warning) 30%, transparent);
  border-radius: 8px;
  font-size: 12px;
  color: var(--warning);
  margin-bottom: 24px;
}

.setting-section { margin-bottom: 28px; }

.section-title {
  margin: 0 0 14px;
  font-size: 13px;
  font-weight: 600;
  color: var(--text-secondary);
}

/* 功能开关列表 */
.flag-list { display: flex; flex-direction: column; gap: 2px; }
.flag-item {
  display: flex; align-items: center; justify-content: space-between;
  padding: 10px 12px;
  border-radius: 8px;
  transition: background 150ms ease;
}
.flag-item:hover { background: var(--bg-hover); }
.flag-info { display: flex; align-items: center; gap: 10px; flex: 1; }
.flag-icon { flex-shrink: 0; color: var(--text-tertiary); }
.flag-text { display: flex; flex-direction: column; gap: 1px; }
.flag-label { font-size: 13px; font-weight: 500; color: var(--text-primary); }
.flag-desc { font-size: 11px; color: var(--text-tertiary); }

/* 开关按钮 */
.toggle-btn {
  padding: 5px 12px;
  font-size: 12px;
  border: 1px solid var(--border-color);
  background: var(--bg-card);
  border-radius: 6px;
  color: var(--text-tertiary);
  cursor: pointer;
  transition: all 150ms ease;
  white-space: nowrap;
  flex-shrink: 0;
}
.toggle-btn:hover { border-color: var(--accent); color: var(--accent); }
.toggle-btn.active {
  border-color: var(--accent);
  color: var(--accent);
  background: color-mix(in srgb, var(--accent) 8%, transparent);
}

/* 反馈 */
.feedback-text { font-size: 13px; color: var(--text-tertiary); margin: 0 0 8px; }
.feedback-link {
  font-size: 13px; color: var(--accent); text-decoration: none;
  font-weight: 500;
}
.feedback-link:hover { text-decoration: underline; }

/* 恢复默认 */
.reset-area { padding-top: 12px; border-top: 1px solid var(--border-light); }
.reset-btn {
  display: inline-flex; align-items: center; gap: 6px;
  padding: 8px 16px; font-size: 13px; color: var(--text-tertiary);
  border: 1px solid var(--border-color); background: var(--bg-card);
  border-radius: 6px; cursor: pointer; transition: all 150ms ease;
}
.reset-btn:hover { color: var(--danger); border-color: var(--danger); background: var(--danger-light); }
</style>
