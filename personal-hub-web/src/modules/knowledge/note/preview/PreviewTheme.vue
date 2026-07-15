<script setup lang="ts">
import type { PreviewTheme } from '@/store/readingConfigStore'

const props = defineProps<{
  theme: PreviewTheme
}>()

const emit = defineEmits<{
  (e: 'update:theme', value: PreviewTheme): void
}>()

const THEME_OPTIONS: { value: PreviewTheme; label: string; icon: string; desc: string }[] = [
  { value: 'follow', label: '跟随系统', icon: '💻', desc: '继承系统主题' },
  { value: 'light', label: '浅色', icon: '☀️', desc: '白底深色文字' },
  { value: 'dark', label: '深色', icon: '🌙', desc: '深色护眼' },
  { value: 'sepia', label: '护眼', icon: '🌿', desc: '豆沙绿护眼' },
]
</script>

<template>
  <div class="theme-picker">
    <button
      v-for="opt in THEME_OPTIONS"
      :key="opt.value"
      :class="['theme-option', { active: theme === opt.value }]"
      @click="emit('update:theme', opt.value)"
    >
      <span class="theme-icon">{{ opt.icon }}</span>
      <div class="theme-info">
        <span class="theme-name">{{ opt.label }}</span>
        <span class="theme-desc">{{ opt.desc }}</span>
      </div>
      <span v-if="theme === opt.value" class="theme-check">✓</span>
    </button>
  </div>
</template>

<style scoped>
.theme-picker {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.theme-option {
  display: flex;
  align-items: center;
  gap: 10px;
  width: 100%;
  padding: 8px 10px;
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
  font-size: 18px;
  flex-shrink: 0;
  width: 28px;
  text-align: center;
}

.theme-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 1px;
}

.theme-name {
  font-size: 13px;
  font-weight: 500;
  color: var(--text-primary);
}

.theme-desc {
  font-size: 11px;
  color: var(--text-tertiary);
}

.theme-check {
  font-size: 14px;
  color: var(--accent);
  font-weight: 600;
}
</style>
