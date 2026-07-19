<script setup lang="ts">
import { useThemeStore } from '@/store/themeStore'
import { ElMessage } from 'element-plus'
import { storeToRefs } from 'pinia'
import { Sun, Moon, Leaf, RotateCcw, Maximize2, Minimize2, Wind, Minus, Plus } from 'lucide-vue-next'
import type { ExtendedAppearanceConfig } from '@/types/layout'

const themeStore = useThemeStore()
const { appearanceConfig } = storeToRefs(themeStore)

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
] as const

const ANIMATION_OPTIONS = [
  { value: 'off', label: '关闭' },
  { value: 'slow', label: '慢' },
  { value: 'normal', label: '标准' },
  { value: 'fast', label: '快' },
] as const

const DENSITY_OPTIONS = [
  { value: 'comfortable', label: '舒适', icon: Maximize2 },
  { value: 'standard', label: '标准', icon: Minimize2 },
  { value: 'compact', label: '紧凑', icon: Wind },
] as const

function setTheme(theme: ExtendedAppearanceConfig['theme']) {
  themeStore.saveAppearanceConfig({ ...appearanceConfig.value, theme })
}

function setAccent(key: string) {
  themeStore.saveAppearanceConfig({
    ...appearanceConfig.value,
    accent: key as ExtendedAppearanceConfig['accent'],
  })
}

function setAppearance<K extends keyof ExtendedAppearanceConfig>(
  key: K,
  value: ExtendedAppearanceConfig[K],
) {
  themeStore.saveAppearanceConfig({ ...appearanceConfig.value, [key]: value })
}

/** 拖动时即时预览宽度，松手再持久化 */
function clampWidth(v: number) {
  return Math.min(100, Math.max(50, Math.round(v)))
}

function previewContentWidth(v: number) {
  const n = clampWidth(v)
  appearanceConfig.value.contentWidth = n
  document.documentElement.style.setProperty('--content-max-width', `${n}%`)
  document.documentElement.setAttribute('data-content-width', String(n))
  return n
}

function onContentWidthInput(e: Event) {
  previewContentWidth(Number((e.target as HTMLInputElement).value))
}

function onContentWidthCommit(e: Event) {
  setAppearance('contentWidth', clampWidth(Number((e.target as HTMLInputElement).value)))
}

function nudgeContentWidth(delta: number) {
  setAppearance('contentWidth', clampWidth(appearanceConfig.value.contentWidth + delta))
}

async function handleReset() {
  await themeStore.resetAppearanceConfig()
  ElMessage.success('外观已恢复默认')
}
</script>

<template>
  <div class="appearance-settings">
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
        <button
          :class="['theme-card', { active: appearanceConfig.theme === 'sepia' }]"
          @click="setTheme('sepia')"
        >
          <Leaf :size="20" class="theme-card-icon" />
          <div class="theme-card-info">
            <span class="theme-card-name">护眼</span>
            <span class="theme-card-desc">豆沙绿，无纯白</span>
          </div>
        </button>
      </div>
    </section>

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

    <section class="setting-section">
      <h3 class="section-title">界面圆角</h3>
      <div class="inline-options">
        <button
          v-for="ro in RADIUS_OPTIONS"
          :key="ro.value"
          :class="['inline-btn', { active: appearanceConfig.borderRadius === ro.value }]"
          @click="setAppearance('borderRadius', ro.value)"
        >{{ ro.label }}</button>
      </div>
    </section>

    <section class="setting-section">
      <h3 class="section-title">动画速度</h3>
      <div class="inline-options">
        <button
          v-for="ao in ANIMATION_OPTIONS"
          :key="ao.value"
          :class="['inline-btn', { active: appearanceConfig.animationSpeed === ao.value }]"
          @click="setAppearance('animationSpeed', ao.value)"
        >{{ ao.label }}</button>
      </div>
    </section>

    <section class="setting-section">
      <h3 class="section-title">界面密度</h3>
      <div class="density-options">
        <button
          v-for="d in DENSITY_OPTIONS"
          :key="d.value"
          :class="['density-card', { active: appearanceConfig.density === d.value }]"
          @click="setAppearance('density', d.value)"
        >
          <component :is="d.icon" :size="18" />
          <span>{{ d.label }}</span>
        </button>
      </div>
    </section>

    <section class="setting-section">
      <h3 class="section-title">内容区宽度</h3>
      <div class="width-control">
        <button
          type="button"
          class="width-nudge"
          title="减小"
          :disabled="appearanceConfig.contentWidth <= 50"
          @click="nudgeContentWidth(-1)"
        >
          <Minus :size="14" />
        </button>
        <input
          type="range"
          class="width-slider"
          min="50"
          max="100"
          step="1"
          :value="appearanceConfig.contentWidth"
          @input="onContentWidthInput"
          @change="onContentWidthCommit"
        />
        <button
          type="button"
          class="width-nudge"
          title="增大"
          :disabled="appearanceConfig.contentWidth >= 100"
          @click="nudgeContentWidth(1)"
        >
          <Plus :size="14" />
        </button>
        <span class="width-value">{{ appearanceConfig.contentWidth }}%</span>
      </div>
    </section>
  </div>
</template>

<style scoped>
.setting-section { margin-bottom: 20px; }
.setting-section:last-child { margin-bottom: 0; }

.section-title-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 10px;
}

.section-title {
  margin: 0 0 10px;
  font-size: 13px;
  font-weight: 600;
  color: var(--text-secondary);
}
.section-title-row .section-title { margin-bottom: 0; }

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
  border-radius: var(--radius-sm);
  transition: all var(--transition);
}
.reset-link:hover {
  color: var(--accent);
  background: var(--accent-light);
}

.theme-cards {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 8px;
}
@media (max-width: 720px) {
  .theme-cards { grid-template-columns: 1fr; }
}
.theme-card {
  display: flex; align-items: center; gap: 10px;
  padding: 14px 16px;
  border: 1px solid var(--border-color);
  background: var(--bg-card);
  border-radius: var(--radius-md);
  cursor: pointer;
  text-align: left;
  transition: all var(--transition);
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

.accent-grid { display: flex; gap: 8px; flex-wrap: wrap; }
.accent-btn {
  display: flex; align-items: center; gap: 8px;
  padding: 8px 14px;
  border: 1px solid var(--border-color);
  background: var(--bg-card);
  border-radius: var(--radius-md);
  cursor: pointer;
  transition: all var(--transition);
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

.inline-options { display: flex; gap: 4px; flex-wrap: wrap; }
.inline-btn {
  display: inline-flex; align-items: center; gap: 6px;
  padding: 6px 14px; font-size: 13px;
  border: 1px solid var(--border-color); background: var(--bg-card);
  border-radius: var(--radius-sm); color: var(--text-secondary);
  cursor: pointer; transition: all var(--transition);
}
.inline-btn:hover { border-color: var(--accent); color: var(--accent); }
.inline-btn.active {
  border-color: var(--accent); color: var(--accent);
  background: color-mix(in srgb, var(--accent) 8%, transparent);
}
.btn-hint { font-size: 11px; color: var(--text-tertiary); }
.inline-btn.active .btn-hint { color: var(--accent); opacity: 0.75; }

.width-control {
  display: flex;
  align-items: center;
  gap: 10px;
}
.width-nudge {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 28px;
  height: 28px;
  flex-shrink: 0;
  border: 1px solid var(--border-color);
  border-radius: var(--radius-sm);
  background: var(--bg-card);
  color: var(--text-secondary);
  cursor: pointer;
  transition: all var(--transition);
}
.width-nudge:hover:not(:disabled) {
  border-color: var(--accent);
  color: var(--accent);
  background: color-mix(in srgb, var(--accent) 8%, transparent);
}
.width-nudge:disabled {
  opacity: 0.4;
  cursor: not-allowed;
}
.width-value {
  min-width: 40px;
  font-size: 13px;
  font-weight: 500;
  color: var(--accent);
  text-align: right;
}
.width-slider {
  -webkit-appearance: none;
  appearance: none;
  flex: 1;
  height: 4px;
  border-radius: var(--radius-sm);
  background: var(--border-color);
  outline: none;
  cursor: pointer;
}
.width-slider::-webkit-slider-thumb {
  -webkit-appearance: none;
  appearance: none;
  width: 16px;
  height: 16px;
  border-radius: 50%;
  background: var(--accent);
  border: 2px solid var(--bg-card);
  box-shadow: 0 0 0 1px var(--accent);
  cursor: pointer;
}
.width-slider::-moz-range-thumb {
  width: 16px;
  height: 16px;
  border-radius: 50%;
  background: var(--accent);
  border: 2px solid var(--bg-card);
  box-shadow: 0 0 0 1px var(--accent);
  cursor: pointer;
}

.density-options { display: flex; gap: 8px; }
.density-card {
  flex: 1; display: flex; flex-direction: column; align-items: center; gap: 6px;
  padding: 12px; font-size: 12px;
  border: 1px solid var(--border-color); background: var(--bg-card);
  border-radius: var(--radius-md); color: var(--text-secondary);
  cursor: pointer; transition: all var(--transition);
}
.density-card:hover { border-color: var(--accent); color: var(--accent); }
.density-card.active {
  border-color: var(--accent); color: var(--accent);
  background: color-mix(in srgb, var(--accent) 8%, transparent);
}
</style>
