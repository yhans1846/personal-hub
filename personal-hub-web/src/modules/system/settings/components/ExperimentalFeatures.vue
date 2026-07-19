<script setup lang="ts">
import { ElMessage } from 'element-plus'
import { RotateCcw, FlaskConical, AlertTriangle } from 'lucide-vue-next'
import { useFeatureFlagStore, type FlagMeta } from '@/store/featureFlagStore'
import { storeToRefs } from 'pinia'
import type { FeatureFlags } from '@/types/layout'

const store = useFeatureFlagStore()
const { flags } = storeToRefs(store)
const FLAG_META = store.FLAG_META

function handleToggle(meta: FlagMeta) {
  if (!meta.available) {
    ElMessage.info('该功能即将推出')
    return
  }
  const key = meta.key as keyof FeatureFlags
  store.toggle(key)
  ElMessage.success(flags.value[key] ? `已开启：${meta.label}` : `已关闭：${meta.label}`)
}

function handleReset() {
  store.resetAll()
  ElMessage.success('已恢复实验功能默认开关')
}
</script>

<template>
  <div class="experimental-features">
    <div class="beta-notice">
      <AlertTriangle :size="16" />
      <span>实验性功能可能不稳定，请谨慎启用。</span>
    </div>

    <section class="setting-section">
      <div class="section-title-row">
        <h3 class="section-title">功能开关</h3>
        <button class="reset-link" @click="handleReset">
          <RotateCcw :size="12" />
          关闭全部
        </button>
      </div>
      <div class="flag-list">
        <div
          v-for="meta in FLAG_META"
          :key="meta.key"
          class="flag-item"
          :class="{ 'flag-item--disabled': !meta.available }"
        >
          <div class="flag-info">
            <FlaskConical :size="16" class="flag-icon" />
            <div class="flag-text">
              <span class="flag-label">{{ meta.label }}</span>
              <span class="flag-desc">{{ meta.description }}</span>
            </div>
          </div>
          <button
            :class="['toggle-btn', { active: flags[meta.key], disabled: !meta.available }]"
            :disabled="!meta.available"
            @click="handleToggle(meta)"
          >
            {{ !meta.available ? '即将推出' : (flags[meta.key] ? '已开启' : '已关闭') }}
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

  </div>
</template>

<style scoped>
.beta-notice {
  display: flex; align-items: center; gap: var(--sp-2);
  padding: var(--sp-3) var(--sp-3);
  background: color-mix(in srgb, var(--warning) 10%, transparent);
  border: 1px solid color-mix(in srgb, var(--warning) 30%, transparent);
  border-radius: var(--radius-md);
  font-size: var(--text-xs);
  color: var(--warning);
  margin-bottom: var(--sp-5);
}

.setting-section { margin-bottom: var(--sp-5); }
.setting-section:last-child { margin-bottom: 0; }

.section-title-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: var(--sp-3);
}

.section-title {
  margin: 0 0 var(--sp-3);
  font-size: var(--text-sm);
  font-weight: 600;
  color: var(--text-secondary);
}
.section-title-row .section-title { margin-bottom: 0; }

.reset-link {
  display: inline-flex;
  align-items: center;
  gap: var(--sp-1);
  font-size: var(--text-xs);
  color: var(--text-tertiary);
  background: none;
  border: none;
  cursor: pointer;
  padding: 2px 6px;
  border-radius: var(--radius-sm);
  transition: all var(--transition);
}
.reset-link:hover {
  color: var(--accent);
  background: var(--accent-light);
}

.flag-list { display: flex; flex-direction: column; gap: var(--sp-1); }
.flag-item {
  display: flex; align-items: center; justify-content: space-between;
  padding: var(--sp-2) 0;
  border-radius: var(--radius-md);
  transition: background var(--transition-duration) ease;
}
.flag-item:hover { background: var(--bg-hover); }
.flag-item--disabled { opacity: 0.72; }
.flag-info { display: flex; align-items: center; gap: var(--sp-3); flex: 1; }
.flag-icon { flex-shrink: 0; color: var(--text-tertiary); }
.flag-text { display: flex; flex-direction: column; gap: 1px; }
.flag-label { font-size: var(--text-sm); font-weight: 500; color: var(--text-primary); }
.flag-desc { font-size: var(--text-xs); color: var(--text-tertiary); }

.toggle-btn {
  padding: 5px var(--sp-3);
  font-size: var(--text-xs);
  border: 1px solid var(--border-color);
  background: var(--bg-card);
  border-radius: var(--radius-sm);
  color: var(--text-tertiary);
  cursor: pointer;
  transition: all var(--transition);
  white-space: nowrap;
  flex-shrink: 0;
}
.toggle-btn:hover:not(:disabled) { border-color: var(--accent); color: var(--accent); }
.toggle-btn.active {
  border-color: var(--accent);
  color: var(--accent);
  background: color-mix(in srgb, var(--accent) 8%, transparent);
}
.toggle-btn:disabled,
.toggle-btn.disabled {
  cursor: not-allowed;
  opacity: 0.85;
}

.feedback-text { font-size: var(--text-sm); color: var(--text-tertiary); margin: 0 0 var(--sp-2); }
.feedback-link {
  font-size: var(--text-sm); color: var(--accent); text-decoration: none;
  font-weight: 500;
}
.feedback-link:hover { text-decoration: underline; }

</style>
