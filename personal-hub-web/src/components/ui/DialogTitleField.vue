<script setup lang="ts">
import UiInput from './UiInput.vue'

defineOptions({ inheritAttrs: false })

defineProps<{
  modelValue?: string
}>()

defineEmits<{
  'update:modelValue': [value: string]
}>()
</script>

<template>
  <UiInput
    class="dialog-title-field"
    :model-value="modelValue ?? ''"
    v-bind="$attrs"
    @update:model-value="$emit('update:modelValue', String($event ?? ''))"
  />
</template>

<style scoped>
.dialog-title-field {
  margin-bottom: var(--sp-4);
}
.dialog-title-field :deep(input) {
  font-size: var(--text-lg) !important;
  font-weight: 600;
  border: none !important;
  padding-left: 0 !important;
  background: transparent !important;
  box-shadow: none !important;
}
.dialog-title-field :deep(input)::placeholder {
  color: var(--text-placeholder);
  font-weight: 400;
}
.dialog-title-field :deep(.el-input__wrapper) {
  box-shadow: none !important;
  background: transparent !important;
  padding-left: 0 !important;
}
</style>
