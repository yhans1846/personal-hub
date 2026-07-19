<script setup lang="ts">
import { RotateCcw } from 'lucide-vue-next'
import { useReadingConfigStore, type PreviewTheme } from '@/store/readingConfigStore'
import { storeToRefs } from 'pinia'

const store = useReadingConfigStore()
const { config } = storeToRefs(store)

// ─── 常量 ───
const FONT_SIZES = [14, 16, 18, 20, 22] as const
const WIDTHS = [
  { label: '窄', value: 900 },
  { label: '标准', value: 1100 },
  { label: '宽', value: 1280 },
] as const
const LINE_HEIGHTS = [1.4, 1.6, 1.8, 2.0] as const
const THEMES: { value: PreviewTheme; icon: string; label: string; desc: string }[] = [
  { value: 'follow', icon: '💻', label: '跟随系统', desc: '继承系统主题' },
  { value: 'light', icon: '☀️', label: '浅色', desc: '白底深色文字' },
  { value: 'dark', icon: '🌙', label: '深色', desc: '深色护眼' },
  { value: 'sepia', icon: '🌿', label: '护眼', desc: '豆沙绿护眼' },
]
const IMAGE_WIDTHS = [60, 70, 80, 90, 100] as const

// ─── 字号 ───
const fontSizeIdx = () => (FONT_SIZES as readonly number[]).indexOf(config.value.fontSize)
function changeFontSize(delta: number) {
  const idx = fontSizeIdx()
  const next = FONT_SIZES[idx + delta]
  if (next !== undefined) store.updateConfig({ fontSize: next })
}

// ─── 恢复默认 ───
async function handleReset() {
  await store.resetConfig()
}
</script>

<template>
  <div class="reading-experience">
    <!-- 阅读基础 -->
    <section class="setting-section">
      <div class="section-title-row">
        <h3 class="section-title">阅读基础</h3>
        <button class="reset-link" @click="handleReset">
          <RotateCcw :size="12" />
          恢复默认
        </button>
      </div>

      <div class="setting-row">
        <label class="setting-label">字号</label>
        <div class="font-size-control">
          <button
            class="size-btn"
            :disabled="fontSizeIdx() <= 0"
            @click="changeFontSize(-1)"
          >A−</button>
          <span class="size-value">{{ config.fontSize }}</span>
          <button
            class="size-btn"
            :disabled="fontSizeIdx() >= FONT_SIZES.length - 1"
            @click="changeFontSize(+1)"
          >A+</button>
        </div>
      </div>

      <div class="setting-row">
        <label class="setting-label">宽度</label>
        <div class="width-options">
          <button
            v-for="w in WIDTHS"
            :key="w.value"
            :class="['width-btn', { active: config.readingWidth === w.value }]"
            @click="store.updateConfig({ readingWidth: w.value })"
          >{{ w.label }}</button>
        </div>
      </div>

      <div class="setting-row">
        <label class="setting-label">行高</label>
        <div class="lh-control">
          <input
            type="range"
            :min="LINE_HEIGHTS[0]"
            :max="LINE_HEIGHTS[LINE_HEIGHTS.length - 1]"
            step="0.2"
            :value="config.lineHeight"
            class="lh-slider"
            @input="store.updateConfig({ lineHeight: Number(($event.target as HTMLInputElement).value) })"
          />
          <span class="lh-value">{{ config.lineHeight.toFixed(1) }}</span>
        </div>
      </div>
    </section>

    <!-- 预览主题 -->
    <section class="setting-section">
      <h3 class="section-title">预览主题</h3>
      <div class="theme-grid">
        <button
          v-for="t in THEMES"
          :key="t.value"
          :class="['theme-card', { active: config.theme === t.value }]"
          @click="store.updateConfig({ theme: t.value })"
        >
          <span class="theme-icon">{{ t.icon }}</span>
          <div class="theme-info">
            <span class="theme-name">{{ t.label }}</span>
            <span class="theme-desc">{{ t.desc }}</span>
          </div>
        </button>
      </div>
    </section>

    <!-- 图片显示 -->
    <section class="setting-section">
      <h3 class="section-title">图片显示</h3>
      <div class="image-width-options">
        <button
          v-for="pct in IMAGE_WIDTHS"
          :key="pct"
          :class="['pct-btn', { active: config.imageMaxWidth === pct }]"
          @click="store.updateConfig({ imageMaxWidth: pct })"
        >{{ pct }}%</button>
      </div>
    </section>

    <!-- 段距 -->
    <section class="setting-section">
      <h3 class="section-title">段落间距</h3>
      <div class="inline-options">
        <button
          v-for="gap in [1.0, 1.2, 1.5]"
          :key="gap"
          :class="['inline-btn', { active: config.paragraphGap === gap }]"
          @click="store.updateConfig({ paragraphGap: gap })"
        >{{ gap }}em</button>
      </div>
    </section>

    <!-- 代码字体 -->
    <section class="setting-section">
      <h3 class="section-title">代码字体</h3>
      <div class="inline-options">
        <button
          :class="['inline-btn', { active: config.codeFontFamily === 'system' }]"
          @click="store.updateConfig({ codeFontFamily: 'system' })"
        >系统字体</button>
        <button
          :class="['inline-btn', { active: config.codeFontFamily === 'monospace' }]"
          @click="store.updateConfig({ codeFontFamily: 'monospace' })"
        >等宽字体</button>
      </div>
    </section>

    <!-- 代码字号 -->
    <section class="setting-section">
      <h3 class="section-title">代码字号</h3>
      <div class="inline-options">
        <button
          :class="['inline-btn', { active: config.codeFontSize === 'same' }]"
          @click="store.updateConfig({ codeFontSize: 'same' })"
        >同正文</button>
        <button
          :class="['inline-btn', { active: config.codeFontSize === '13px' }]"
          @click="store.updateConfig({ codeFontSize: '13px' })"
        >13px</button>
        <button
          :class="['inline-btn', { active: config.codeFontSize === '14px' }]"
          @click="store.updateConfig({ codeFontSize: '14px' })"
        >14px</button>
      </div>
    </section>
  </div>
</template>

<style scoped>
/* ─── 分区（节奏见 settings-layout.css，此处仅补组件私有） ─── */
.setting-section {
  margin-bottom: var(--sp-5);
}
.setting-section:last-child {
  margin-bottom: 0;
}

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
.section-title-row .section-title {
  margin-bottom: 0;
}

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

.setting-row {
  display: flex;
  align-items: center;
  gap: var(--sp-4);
  margin-bottom: var(--sp-3);
}
.setting-row:last-child {
  margin-bottom: 0;
}

.setting-label {
  font-size: var(--text-sm);
  color: var(--text-tertiary);
  width: var(--sp-10);
  flex-shrink: 0;
}

/* ─── 字号 ─── */
.font-size-control {
  display: flex;
  align-items: center;
  gap: var(--sp-2);
}

.size-btn {
  width: var(--sp-8);
  height: var(--sp-6);
  border: 1px solid var(--border-color);
  background: var(--bg-card);
  border-radius: var(--radius-sm);
  font-size: var(--text-sm);
  color: var(--text-secondary);
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all var(--transition);
}

.size-btn:hover:not(:disabled) {
  border-color: var(--accent);
  color: var(--accent);
  background: var(--accent-light);
}

.size-btn:disabled {
  opacity: 0.3;
  cursor: not-allowed;
}

.size-value {
  min-width: var(--sp-6);
  text-align: center;
  font-size: var(--text-base);
  font-weight: 600;
  color: var(--text-primary);
}

/* ─── 宽度 ─── */
.width-options {
  display: flex;
  gap: var(--sp-1);
}

.width-btn {
  padding: 6px var(--sp-3);
  font-size: var(--text-sm);
  border: 1px solid var(--border-color);
  background: var(--bg-card);
  border-radius: var(--radius-sm);
  color: var(--text-secondary);
  cursor: pointer;
  transition: all var(--transition);
}

.width-btn:hover {
  border-color: var(--accent);
  color: var(--accent);
}

.width-btn.active {
  border-color: var(--accent);
  color: var(--accent);
  background: color-mix(in srgb, var(--accent) 8%, transparent);
}

/* ─── 行高滑块 ─── */
.lh-control {
  flex: 1;
  display: flex;
  align-items: center;
  gap: var(--sp-3);
}

.lh-slider {
  -webkit-appearance: none;
  appearance: none;
  flex: 1;
  max-width: 200px;
  height: var(--sp-1);
  border-radius: var(--radius-sm);
  background: var(--border-color);
  outline: none;
  cursor: pointer;
}

.lh-slider::-webkit-slider-thumb {
  -webkit-appearance: none;
  width: var(--sp-3);
  height: var(--sp-3);
  border-radius: 50%;
  background: var(--accent);
  border: 2px solid var(--bg-body);
  box-shadow: 0 0 0 1px var(--accent);
  cursor: pointer;
  transition: transform var(--transition-duration) ease;
}

.lh-slider::-webkit-slider-thumb:hover {
  transform: scale(1.15);
}

.lh-slider::-moz-range-thumb {
  width: var(--sp-3);
  height: var(--sp-3);
  border-radius: 50%;
  background: var(--accent);
  border: 2px solid var(--bg-body);
  box-shadow: 0 0 0 1px var(--accent);
  cursor: pointer;
}

.lh-value {
  min-width: var(--sp-6);
  font-size: var(--text-sm);
  font-weight: 500;
  color: var(--text-primary);
}

/* ─── 主题卡片 ─── */
.theme-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: var(--sp-2);
}

.theme-card {
  display: flex;
  align-items: center;
  gap: var(--sp-3);
  padding: var(--sp-3);
  border: 1px solid var(--border-color);
  background: var(--bg-card);
  border-radius: var(--radius-md);
  cursor: pointer;
  text-align: left;
  transition: all var(--transition);
}

.theme-card:hover {
  border-color: var(--accent-border);
  background: var(--bg-hover);
}

.theme-card.active {
  border-color: var(--accent);
  background: color-mix(in srgb, var(--accent) 8%, transparent);
  box-shadow: 0 0 0 1px var(--accent);
}

.theme-icon {
  font-size: var(--text-xl);
  flex-shrink: 0;
  width: var(--sp-8);
  text-align: center;
}

.theme-info {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.theme-name {
  font-size: var(--text-sm);
  font-weight: 500;
  color: var(--text-primary);
}

.theme-desc {
  font-size: var(--text-xs);
  color: var(--text-tertiary);
}

/* ─── 图片比例 ─── */
.image-width-options {
  display: flex;
  gap: var(--sp-1);
}

.pct-btn {
  flex: 1;
  padding: var(--sp-2) var(--sp-1);
  font-size: var(--text-sm);
  font-weight: 500;
  border: 1px solid var(--border-color);
  background: var(--bg-card);
  border-radius: var(--radius-sm);
  color: var(--text-secondary);
  cursor: pointer;
  transition: all var(--transition);
}

.pct-btn:hover {
  border-color: var(--accent);
  color: var(--accent);
}

.pct-btn.active {
  border-color: var(--accent);
  color: var(--accent);
  background: color-mix(in srgb, var(--accent) 8%, transparent);
}

/* ─── 内联选项组 ─── */
.inline-options {
  display: flex;
  gap: var(--sp-1);
}

.inline-btn {
  padding: 6px var(--sp-3);
  font-size: var(--text-sm);
  border: 1px solid var(--border-color);
  background: var(--bg-card);
  border-radius: var(--radius-sm);
  color: var(--text-secondary);
  cursor: pointer;
  transition: all var(--transition);
}

.inline-btn:hover {
  border-color: var(--accent);
  color: var(--accent);
}

.inline-btn.active {
  border-color: var(--accent);
  color: var(--accent);
  background: color-mix(in srgb, var(--accent) 8%, transparent);
}

</style>
