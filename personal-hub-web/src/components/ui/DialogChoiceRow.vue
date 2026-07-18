<script setup lang="ts">
export interface DialogChoiceOption {
  value: string | number
  label: string
  emoji?: string
  color?: string
}

defineProps<{
  modelValue: string | number
  options: DialogChoiceOption[]
}>()

defineEmits<{
  'update:modelValue': [value: string | number]
}>()
</script>

<template>
  <div class="dialog-choice-row">
    <button
      v-for="opt in options"
      :key="String(opt.value)"
      type="button"
      class="dialog-choice"
      :class="{ active: modelValue === opt.value }"
      :style="{
        '--card-color': opt.color || 'var(--accent)',
        '--card-bg': modelValue === opt.value
          ? `color-mix(in srgb, ${opt.color || 'var(--accent)'} 12%, transparent)`
          : 'transparent',
      }"
      @click="$emit('update:modelValue', opt.value)"
    >
      <span v-if="opt.emoji" class="dialog-choice__emoji">{{ opt.emoji }}</span>
      <span class="dialog-choice__label">{{ opt.label }}</span>
    </button>
  </div>
</template>

<style scoped>
.dialog-choice-row {
  display: flex;
  flex-wrap: wrap;
  gap: var(--sp-2);
}

.dialog-choice {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: var(--sp-2) var(--sp-3);
  border-radius: var(--radius-md);
  border: 2px solid transparent;
  background: var(--bg-hover);
  cursor: pointer;
  transition: all var(--transition);
  flex: 1 1 auto;
  justify-content: center;
  min-width: 0;
}

.dialog-choice:hover {
  border-color: var(--border-color, var(--border));
  background: var(--bg-card);
}

.dialog-choice.active {
  border-color: var(--card-color, var(--accent));
  background: var(--card-bg, var(--accent-light));
}

.dialog-choice__emoji {
  font-size: 14px;
  line-height: 1;
}

.dialog-choice__label {
  font-size: var(--text-sm);
  font-weight: 500;
  color: var(--text-secondary);
}

.dialog-choice.active .dialog-choice__label {
  color: var(--card-color, var(--accent));
}
</style>
