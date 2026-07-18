<script setup lang="ts">
import { computed } from 'vue'

export interface DialogChipOption {
  id: number
  name: string
  color?: string
}

const props = withDefaults(defineProps<{
  modelValue: number | number[] | null
  options: DialogChipOption[]
  multiple?: boolean
  emptyText?: string
}>(), {
  multiple: false,
  emptyText: '暂无选项',
})

const emit = defineEmits<{
  'update:modelValue': [value: number | number[] | null]
}>()

const selectedIds = computed(() => {
  if (props.multiple) {
    return Array.isArray(props.modelValue) ? props.modelValue : []
  }
  return props.modelValue == null ? [] : [props.modelValue as number]
})

function isActive(id: number) {
  return selectedIds.value.includes(id)
}

function toggle(id: number) {
  if (props.multiple) {
    const cur = Array.isArray(props.modelValue) ? [...props.modelValue] : []
    const next = cur.includes(id) ? cur.filter(x => x !== id) : [...cur, id]
    emit('update:modelValue', next)
    return
  }
  emit('update:modelValue', props.modelValue === id ? null : id)
}
</script>

<template>
  <div class="dialog-chip-row">
    <button
      v-for="opt in options"
      :key="opt.id"
      type="button"
      class="dialog-chip"
      :class="{ active: isActive(opt.id) }"
      :style="isActive(opt.id) && opt.color ? { '--chip-color': opt.color } : undefined"
      @click="toggle(opt.id)"
    >
      <span
        v-if="opt.color"
        class="dialog-chip__dot"
        :style="{ background: opt.color }"
      />
      <slot name="prefix" :option="opt" />
      {{ opt.name }}
    </button>
    <span v-if="options.length === 0" class="dialog-chip-empty">{{ emptyText }}</span>
  </div>
</template>

<style scoped>
.dialog-chip-row {
  display: flex;
  flex-wrap: wrap;
  gap: var(--sp-2);
}

.dialog-chip {
  display: inline-flex;
  align-items: center;
  gap: 5px;
  padding: 5px 12px;
  border-radius: var(--radius-sm);
  border: 1px solid var(--border-color, var(--border));
  background: var(--bg-card);
  font-size: var(--text-xs);
  color: var(--text-secondary);
  cursor: pointer;
  transition: all var(--transition);
  white-space: nowrap;
}

.dialog-chip:hover {
  border-color: var(--accent-border, var(--accent));
  color: var(--text-primary);
}

.dialog-chip.active {
  border-color: var(--chip-color, var(--accent));
  color: var(--chip-color, var(--accent));
  background: color-mix(in srgb, var(--chip-color, var(--accent)) 12%, transparent);
}

.dialog-chip__dot {
  width: 7px;
  height: 7px;
  border-radius: 50%;
  flex-shrink: 0;
}

.dialog-chip-empty {
  font-size: var(--text-xs);
  color: var(--text-placeholder);
}
</style>
