<script setup lang="ts">
withDefaults(defineProps<{
  modelValue?: string
  placeholder?: string
  size?: 'sm' | 'md' | 'lg'
}>(), {
  size: 'md',
  placeholder: '',
})

defineEmits<{
  'update:modelValue': [value: string]
}>()
</script>

<template>
  <textarea
    class="dialog-editor"
    :class="`dialog-editor--${size}`"
    :value="modelValue"
    :placeholder="placeholder"
    @input="$emit('update:modelValue', ($event.target as HTMLTextAreaElement).value)"
  />
</template>

<style scoped>
.dialog-editor {
  display: block;
  width: 100%;
  border: none;
  outline: none;
  resize: vertical;
  font-family: var(--font-sans);
  font-size: var(--text-base);
  line-height: var(--leading-relaxed, 1.65);
  color: var(--text-primary);
  background: transparent;
  padding: 0;
  margin-bottom: var(--sp-2);
}

.dialog-editor::placeholder {
  color: var(--text-placeholder);
}

.dialog-editor--sm { min-height: 140px; }
.dialog-editor--md { min-height: 200px; }
.dialog-editor--lg { min-height: 280px; }
</style>
