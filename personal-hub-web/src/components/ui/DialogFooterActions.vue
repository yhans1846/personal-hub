<script setup lang="ts">
import UiButton from './UiButton.vue'

withDefaults(defineProps<{
  saving?: boolean
  confirmText?: string
  cancelText?: string
}>(), {
  confirmText: '保存',
  cancelText: '取消',
})

defineEmits<{
  cancel: []
  confirm: []
}>()
</script>

<template>
  <div class="dialog-footer-actions">
    <UiButton text @click="$emit('cancel')">{{ cancelText }}</UiButton>
    <UiButton type="primary" :loading="saving" @click="$emit('confirm')">
      {{ confirmText }}
    </UiButton>
  </div>
</template>

<style scoped>
.dialog-footer-actions {
  display: flex;
  justify-content: flex-end;
  align-items: center;
  gap: var(--sp-2);
}
</style>
