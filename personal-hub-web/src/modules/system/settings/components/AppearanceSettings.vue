<script setup lang="ts">
import { useLayoutStore } from '@/store/layoutStore'
import { ElMessage } from 'element-plus'
import { storeToRefs } from 'pinia'
import { Sun, Moon, RotateCcw, Maximize2, Minimize2, Wind } from 'lucide-vue-next'

const layoutStore = useLayoutStore()
const { appearanceConfig } = storeToRefs(layoutStore)

const ACCENT_OPTIONS = [
  { key: 'blue', label: '蓝色', color: '#4F7BFF' },
  { key: 'purple', label: '紫色', color: '#8B5CF6' },
  { key: 'pink', label: '粉色', color: '#EC4899' },
  { key: 'red', label: '红色', color: '#EF4444' },
  { key: 'orange', label: '橙色', color: '#F97316' },
  { key: 'green', label: '绿色', color: '#10B981' },
  { key: 'teal', label: '青色', color: '#14B8A6' },
  { key: 'cyan', label: '天蓝', color: '#06B6D4' },
  { key: 'indigo', label: '靛蓝', color: '#6366F1' },
]

const RADIUS_OPTIONS = [
  { value: 'sm', label: '4px' },
  { value: 'md', label: '8px' },
  { value: 'lg', label: '12px' },
  { value: 'xl', label: '16px' },
]

const ANIMATION_OPTIONS = [
  { value: 'off', label: '关闭' },
  { value: 'slow', label: '慢' },
  { value: 'normal', label: '标准' },
  { value: 'fast', label: '快' },
]

const DENSITY_OPTIONS = [
  { value: 'comfortable', label: '舒适', icon: Maximize2 },
  { value: 'standard', label: '标准', icon: Minimize2 },
  { value: 'compact', label: '紧凑', icon: Wind },
]

function setTheme(theme: 'light' | 'dark') {
  layoutStore.saveAppearanceConfig({ ...appearanceConfig.value, theme })
}

function setAccent(key: string) {
  layoutStore.saveAppearanceConfig({ ...appearanceConfig.value, accent: key as any })
}

function setAppearance(key: string, value: any) {
  const config = { ...appearanceConfig.value, [key]: value }
  layoutStore.saveAppearanceConfig(config)
  applyCSSVariables(config)
}

function applyCSSVariables(config: any) {
  // 圆角
  const radiusMap: Record<string, string> = { sm: '8px', md: '10px', lg: '12px', xl: '16px' }
  if (config.borderRadius) {
    document.documentElement.style.setProperty('--radius-sm', radiusMap[config.borderRadius] || '8px')
  }
  // 动画
  const animMap: Record<string, string> = { off: '0ms', slow: '350ms', normal: '200ms', fast: '100ms' }
  if (config.animationSpeed) {
    document.documentElement.style.setProperty('--transition-duration', animMap[config.animationSpeed] || '200ms')
  }
  // 密度
  const densityMap: Record<string, string> = { comfortable: '1.25', standard: '1', compact: '0.75' }
  if (config.density) {
    document.documentElement.style.setProperty('--sp-density', densityMap[config.density] || '1')
  }
}

async function handleReset() {
  await layoutStore.resetAppearanceConfig()
  applyCSSVariables({ borderRadius: 'lg', animationSpeed: 'normal', density: 'standard' })
  ElMessage.success('外观已恢复默认')
}
</script>

<template>
  <div class="appearance-settings">
    <!-- 主题 -->
    <section class="setting-section">
      <div class="section-title-row">
        <h3 class="section-title">主题</h3>
        <button class="reset-link" @click="handleReset">
          <RotateCcw :size="12" />
          恢复默认
        </button>
      </div>
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

    <!-- 界面圆角 -->
    <section class="setting-section">
      <h3 class="section-title">界面圆角</h3>
      <div class="inline-options">
        <button
          v-for="ro in RADIUS_OPTIONS"
          :key="ro.value"
          :class="['inline-btn', { active: (appearanceConfig as any).borderRadius === ro.value }]"
          @click="setAppearance('borderRadius', ro.value)"
        >{{ ro.label }}</button>
      </div>
    </section>

    <!-- 动画速度 -->
    <section class="setting-section">
      <h3 class="section-title">动画速度</h3>
      <div class="inline-options">
        <button
          v-for="ao in ANIMATION_OPTIONS"
          :key="ao.value"
          :class="['inline-btn', { active: (appearanceConfig as any).animationSpeed === ao.value }]"
          @click="setAppearance('animationSpeed', ao.value)"
        >{{ ao.label }}</button>
      </div>
    </section>

    <!-- 界面密度 -->
    <section class="setting-section">
      <h3 class="section-title">界面密度</h3>
      <div class="density-options">
        <button
          v-for="d in DENSITY_OPTIONS"
          :key="d.value"
          :class="['density-card', { active: (appearanceConfig as any).density === d.value }]"
          @click="setAppearance('density', d.value)"
        >
          <component :is="d.icon" :size="18" />
          <span>{{ d.label }}</span>
        </button>
      </div>
    </section>

  </div>
</template>

<style scoped>
.appearance-settings { }

.setting-section { margin-bottom: 20px; }

.section-title-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 10px;
}

.section-title {
  margin: 0;
  font-size: 13px;
  font-weight: 600;
  color: var(--text-secondary);
}

.reset-link {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  font-size: 11px;
  color: var(--text-tertiary);
  background: none;
  border: none;
  cursor: pointer;
  padding: 2px 6px;
  border-radius: 4px;
  transition: all 150ms ease;
}
.reset-link:hover {
  color: var(--accent);
  background: var(--accent-light);
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

/* 内联选项组 */
.inline-options { display: flex; gap: 4px; }
.inline-btn {
  padding: 6px 14px; font-size: 13px;
  border: 1px solid var(--border-color); background: var(--bg-card);
  border-radius: 6px; color: var(--text-secondary);
  cursor: pointer; transition: all 150ms ease;
}
.inline-btn:hover { border-color: var(--accent); color: var(--accent); }
.inline-btn.active { border-color: var(--accent); color: var(--accent); background: color-mix(in srgb, var(--accent) 8%, transparent); }

/* 密度 */
.density-options { display: flex; gap: 8px; }
.density-card {
  flex: 1; display: flex; flex-direction: column; align-items: center; gap: 6px;
  padding: 12px; font-size: 12px;
  border: 1px solid var(--border-color); background: var(--bg-card);
  border-radius: 8px; color: var(--text-secondary);
  cursor: pointer; transition: all 150ms ease;
}
.density-card:hover { border-color: var(--accent); color: var(--accent); }
.density-card.active {
  border-color: var(--accent); color: var(--accent);
  background: color-mix(in srgb, var(--accent) 8%, transparent);
}

</style>
