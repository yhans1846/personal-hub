<script setup lang="ts">
const props = withDefaults(defineProps<{
  disabled?: boolean
  danger?: boolean
}>(), {
  disabled: false,
  danger: false,
})

const emit = defineEmits<{ click: [] }>()

function onClick() {
  if (props.disabled) return
  emit('click')
}
</script>

<template>
  <button
    type="button"
    class="ph-context-menu-item"
    :class="{ disabled, danger }"
    :disabled="disabled"
    @click="onClick"
  >
    <slot />
  </button>
</template>

<style scoped>
.ph-context-menu-item {
  display: flex;
  align-items: center;
  width: 100%;
  gap: 8px;
  padding: 6px 10px;
  border: none;
  border-radius: var(--radius-sm);
  background: transparent;
  color: var(--text-primary);
  font-size: var(--text-sm);
  text-align: left;
  cursor: pointer;
  transition: background var(--transition);
}
.ph-context-menu-item:hover:not(.disabled) {
  background: var(--bg-hover);
}
.ph-context-menu-item.disabled {
  opacity: 0.45;
  cursor: not-allowed;
}
.ph-context-menu-item.danger {
  color: var(--danger);
}
</style>
