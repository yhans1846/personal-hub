<script setup lang="ts">
import { useLayoutStore } from '@/store/layoutStore'
import { ElMessage } from 'element-plus'
import { storeToRefs } from 'pinia'
import { Sun, Moon, RotateCcw } from 'lucide-vue-next'

const layoutStore = useLayoutStore()
const { appearanceConfig } = storeToRefs(layoutStore)

const ACCENT_OPTIONS = [
  { key: 'blue', label: '蓝色', color: '#4F7BFF' },
  { key: 'purple', label: '紫色', color: '#8B5CF6' },
  { key: 'green', label: '绿色', color: '#10B981' },
  { key: 'orange', label: '橙色', color: '#F97316' },
  { key: 'cyan', label: '青色', color: '#06B6D4' },
]

function setTheme(theme: 'light' | 'dark') {
  layoutStore.saveAppearanceConfig({ ...appearanceConfig.value, theme })
}

function setAccent(key: string) {
  layoutStore.saveAppearanceConfig({ ...appearanceConfig.value, accent: key as any })
}

async function handleReset() {
  await layoutStore.resetAppearanceConfig()
  ElMessage.success('外观已恢复默认')
}
</script>

<template>
  <div class="appearance-settings">
    <!-- 主题 -->
    <section class="setting-section">
      <h3 class="section-title">主题</h3>
      <div class="theme-cards">
        <button
          :class="['theme-card', { active: appearanceConfig.theme === 'light' }]"
          @click="setTheme('light')"
        >
          <Sun :size="20" class="theme-card-icon" />
          <div class="theme-card-info">
            <span class="theme-card-name">浅色</span>
            <span class="theme-card-desc">白底深色文字</span>
          </div>
        </button>
        <button
          :class="['theme-card', { active: appearanceConfig.theme === 'dark' }]"
          @click="setTheme('dark')"
        >
          <Moon :size="20" class="theme-card-icon" />
          <div class="theme-card-info">
            <span class="theme-card-name">深色</span>
            <span class="theme-card-desc">深底浅色文字</span>
          </div>
        </button>
      </div>
    </section>

    <!-- 强调色 -->
    <section class="setting-section">
      <h3 class="section-title">强调色</h3>
      <div class="accent-grid">
        <button
          v-for="opt in ACCENT_OPTIONS"
          :key="opt.key"
          :class="['accent-btn', { active: appearanceConfig.accent === opt.key }]"
          :style="{ '--accent-color': opt.color }"
          @click="setAccent(opt.key)"
        >
          <span class="accent-dot" :style="{ background: opt.color }" />
          <span class="accent-label">{{ opt.label }}</span>
        </button>
      </div>
    </section>

    <!-- 恢复默认 -->
    <div class="reset-area">
      <button class="reset-btn" @click="handleReset">
        <RotateCcw :size="14" />
        恢复默认
      </button>
    </div>
  </div>
</template>

<style scoped>
.appearance-settings { max-width: 520px; }

.setting-section { margin-bottom: 28px; }

.section-title {
  margin: 0 0 14px;
  font-size: 13px;
  font-weight: 600;
  color: var(--text-secondary);
}

/* 主题卡片 */
.theme-cards { display: grid; grid-template-columns: 1fr 1fr; gap: 8px; }
.theme-card {
  display: flex; align-items: center; gap: 10px;
  padding: 14px 16px;
  border: 1px solid var(--border-color);
  background: var(--bg-card);
  border-radius: 8px;
  cursor: pointer;
  text-align: left;
  transition: all 150ms ease;
}
.theme-card:hover { border-color: var(--accent-border); background: var(--bg-hover); }
.theme-card.active {
  border-color: var(--accent);
  background: color-mix(in srgb, var(--accent) 8%, transparent);
  box-shadow: 0 0 0 1px var(--accent);
}
.theme-card-icon { flex-shrink: 0; color: var(--text-secondary); }
.theme-card.active .theme-card-icon { color: var(--accent); }
.theme-card-info { display: flex; flex-direction: column; gap: 2px; }
.theme-card-name { font-size: 13px; font-weight: 500; color: var(--text-primary); }
.theme-card-desc { font-size: 11px; color: var(--text-tertiary); }

/* 强调色 */
.accent-grid { display: flex; gap: 8px; flex-wrap: wrap; }
.accent-btn {
  display: flex; align-items: center; gap: 8px;
  padding: 8px 14px;
  border: 1px solid var(--border-color);
  background: var(--bg-card);
  border-radius: 8px;
  cursor: pointer;
  transition: all 150ms ease;
}
.accent-btn:hover { border-color: var(--accent-border); background: var(--bg-hover); }
.accent-btn.active {
  border-color: var(--accent-color);
  background: color-mix(in srgb, var(--accent-color) 10%, transparent);
}
.accent-dot {
  width: 14px; height: 14px; border-radius: 50%; flex-shrink: 0;
}
.accent-label { font-size: 13px; color: var(--text-primary); }
.accent-btn.active .accent-label { color: var(--accent-color); font-weight: 500; }

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
