<script setup lang="ts">
import type { PreviewSettings } from './usePreviewSettings'
import type { PreviewTheme } from './usePreviewTheme'

const props = defineProps<{
  settings: PreviewSettings
  theme: PreviewTheme
}>()

const emit = defineEmits<{
  (e: 'update:settings', value: PreviewSettings): void
  (e: 'update:theme', value: PreviewTheme): void
}>()

const FONT_SIZES = [14, 16, 18, 20, 22] as const
const WIDTHS = [
  { label: '窄', value: 900 },
  { label: '标准', value: 1100 },
  { label: '宽', value: 1280 },
] as const
const LINE_HEIGHTS = [1.4, 1.6, 1.8, 2.0] as const

const THEMES: { value: PreviewTheme; icon: string; label: string }[] = [
  { value: 'light', icon: '☀️', label: '浅色' },
  { value: 'dark', icon: '🌙', label: '深色' },
  { value: 'sepia', icon: '🌿', label: '护眼' },
]

function changeFontSize(delta: number) {
  const idx = FONT_SIZES.indexOf(props.settings.fontSize as unknown as number)
  if (idx === -1) return
  const next = FONT_SIZES[idx + delta]
  if (next) emit('update:settings', { ...props.settings, fontSize: next })
}

function setWidth(value: number) {
  emit('update:settings', { ...props.settings, readingWidth: value })
}

function setLineHeight(value: number) {
  emit('update:settings', { ...props.settings, lineHeight: value })
}

function lhPercent(lh: number): number {
  const min = LINE_HEIGHTS[0]
  const max = LINE_HEIGHTS[LINE_HEIGHTS.length - 1]
  return ((lh - min) / (max - min)) * 100
}
</script>

<template>
  <div class="reading-settings">
    <h3 class="settings-title">阅读设置</h3>

    <!-- 字号 -->
    <div class="setting-row">
      <label class="setting-label">字号</label>
      <div class="font-size-control">
        <button
          class="size-btn"
          :disabled="FONT_SIZES.indexOf(settings.fontSize as unknown as number) <= 0"
          @click="changeFontSize(-1)"
        >A−</button>
        <span class="size-value">{{ settings.fontSize }}</span>
        <button
          class="size-btn"
          :disabled="FONT_SIZES.indexOf(settings.fontSize as unknown as number) >= FONT_SIZES.length - 1"
          @click="changeFontSize(+1)"
        >A+</button>
      </div>
    </div>

    <!-- 宽度 -->
    <div class="setting-row">
      <label class="setting-label">宽度</label>
      <div class="width-options">
        <button
          v-for="w in WIDTHS"
          :key="w.value"
          :class="['width-btn', { active: settings.readingWidth === w.value }]"
          @click="setWidth(w.value)"
        >{{ w.label }}</button>
      </div>
    </div>

    <!-- 行高 -->
    <div class="setting-row">
      <label class="setting-label">行高</label>
      <div class="lh-control">
        <input
          type="range"
          :min="LINE_HEIGHTS[0]"
          :max="LINE_HEIGHTS[LINE_HEIGHTS.length - 1]"
          step="0.2"
          :value="settings.lineHeight"
          class="lh-slider"
          @input="setLineHeight(Number(($event.target as HTMLInputElement).value))"
        />
        <span class="lh-value">{{ settings.lineHeight.toFixed(1) }}</span>
      </div>
    </div>

    <div class="settings-divider" />

    <!-- 主题 -->
    <div class="theme-list">
      <button
        v-for="t in THEMES"
        :key="t.value"
        :class="['theme-option', { active: theme === t.value }]"
        @click="emit('update:theme', t.value)"
      >
        <span class="theme-icon">{{ t.icon }}</span>
        <span class="theme-label">{{ t.label }}</span>
        <span v-if="theme === t.value" class="theme-check">✓</span>
      </button>
    </div>
  </div>
</template>

<style scoped>
.reading-settings {
  display: flex;
  flex-direction: column;
  gap: 14px;
  padding: 4px 0;
}

.settings-title {
  margin: 0;
  font-size: 13px;
  font-weight: 600;
  color: var(--text-primary);
}

.setting-row {
  display: flex;
  align-items: center;
  gap: 12px;
}

.setting-label {
  font-size: 12px;
  color: var(--text-tertiary);
  width: 36px;
  flex-shrink: 0;
}

/* 字号 */
.font-size-control {
  display: flex;
  align-items: center;
  gap: 6px;
}

.size-btn {
  width: 32px;
  height: 28px;
  border: 1px solid var(--border-color);
  background: transparent;
  border-radius: 6px;
  font-size: 13px;
  color: var(--text-secondary);
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 150ms ease;
}

.size-btn:hover:not(:disabled) {
  border-color: var(--text-tertiary);
  color: var(--text-primary);
}

.size-btn:disabled {
  opacity: 0.3;
  cursor: not-allowed;
}

.size-value {
  min-width: 24px;
  text-align: center;
  font-size: 14px;
  font-weight: 500;
  color: var(--text-primary);
}

/* 宽度 */
.width-options {
  display: flex;
  gap: 3px;
}

.width-btn {
  padding: 4px 10px;
  font-size: 12px;
  border: 1px solid var(--border-color);
  background: transparent;
  border-radius: 6px;
  color: var(--text-secondary);
  cursor: pointer;
  transition: all 150ms ease;
}

.width-btn:hover {
  border-color: var(--text-tertiary);
  color: var(--text-primary);
}

.width-btn.active {
  border-color: var(--accent);
  color: var(--accent);
  background: color-mix(in srgb, var(--accent) 8%, transparent);
}

/* 行高滑块 */
.lh-control {
  flex: 1;
  display: flex;
  align-items: center;
  gap: 8px;
}

.lh-slider {
  -webkit-appearance: none;
  appearance: none;
  flex: 1;
  height: 4px;
  border-radius: 2px;
  background: var(--border-color);
  outline: none;
  cursor: pointer;
}

.lh-slider::-webkit-slider-thumb {
  -webkit-appearance: none;
  width: 14px;
  height: 14px;
  border-radius: 50%;
  background: var(--accent);
  border: 2px solid var(--bg-body);
  box-shadow: 0 0 0 1px var(--accent);
  cursor: pointer;
  transition: transform 100ms ease;
}

.lh-slider::-webkit-slider-thumb:hover {
  transform: scale(1.15);
}

.lh-slider::-moz-range-thumb {
  width: 14px;
  height: 14px;
  border-radius: 50%;
  background: var(--accent);
  border: 2px solid var(--bg-body);
  box-shadow: 0 0 0 1px var(--accent);
  cursor: pointer;
}

.lh-value {
  min-width: 28px;
  font-size: 12px;
  font-weight: 500;
  color: var(--text-primary);
  text-align: right;
}

/* 分割线 */
.settings-divider {
  height: 1px;
  background: var(--border-color);
  margin: 2px 0;
}

/* 主题 */
.theme-list {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.theme-option {
  display: flex;
  align-items: center;
  gap: 8px;
  width: 100%;
  padding: 6px 8px;
  border: none;
  background: none;
  border-radius: 6px;
  cursor: pointer;
  text-align: left;
  transition: background 150ms ease;
}

.theme-option:hover {
  background: var(--bg-hover);
}

.theme-option.active {
  background: color-mix(in srgb, var(--accent) 8%, transparent);
}

.theme-icon {
  font-size: 16px;
  width: 24px;
  text-align: center;
  flex-shrink: 0;
}

.theme-label {
  flex: 1;
  font-size: 13px;
  color: var(--text-primary);
}

.theme-check {
  font-size: 13px;
  color: var(--accent);
  font-weight: 600;
}
</style>
