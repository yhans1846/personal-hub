<script setup lang="ts">
import { Calendar } from 'lucide-vue-next'

withDefaults(defineProps<{
  modelValue: string | null
  placeholder?: string
  clearable?: boolean
}>(), {
  placeholder: '选择日期',
  clearable: true,
})

defineEmits<{
  'update:modelValue': [value: string | null]
}>()
</script>

<template>
  <el-date-picker
    class="dialog-date-chip"
    :class="{ 'dialog-date-chip--filled': !!modelValue }"
    :model-value="modelValue"
    type="date"
    value-format="YYYY-MM-DD"
    :placeholder="placeholder"
    :clearable="clearable"
    :editable="false"
    popper-class="ph-date-popper"
    @update:model-value="$emit('update:modelValue', ($event as string) || null)"
  >
    <template #prefix>
      <Calendar :size="14" class="dialog-date-chip__icon" />
    </template>
  </el-date-picker>
</template>

<style scoped>
.dialog-date-chip {
  --el-date-editor-width: auto;
  width: auto !important;
}

.dialog-date-chip :deep(.el-input__wrapper) {
  border-radius: var(--radius-sm) !important;
  padding: 2px 12px 2px 10px !important;
  box-shadow: 0 0 0 1px var(--border-color, var(--border)) inset !important;
  background: var(--bg-card) !important;
  transition: all var(--transition) !important;
  min-height: 32px;
}

.dialog-date-chip :deep(.el-input__wrapper:hover),
.dialog-date-chip :deep(.el-input__wrapper.is-focus) {
  box-shadow: 0 0 0 1px var(--accent-border, var(--accent)) inset !important;
}

.dialog-date-chip--filled :deep(.el-input__wrapper) {
  background: var(--accent-light) !important;
  box-shadow: 0 0 0 1px var(--accent-border, var(--accent)) inset !important;
}

.dialog-date-chip :deep(.el-input__inner) {
  font-size: var(--text-sm) !important;
  color: var(--text-tertiary) !important;
  height: 28px !important;
  line-height: 28px !important;
}

.dialog-date-chip--filled :deep(.el-input__inner) {
  color: var(--text-primary) !important;
  font-weight: 500;
}

.dialog-date-chip :deep(.el-input__prefix) {
  color: var(--text-tertiary);
}

.dialog-date-chip--filled :deep(.el-input__prefix) {
  color: var(--accent);
}

.dialog-date-chip__icon {
  display: block;
}

.dialog-date-chip :deep(.el-input__suffix .el-icon) {
  color: var(--text-tertiary);
}
</style>
