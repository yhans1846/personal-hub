<script setup lang="ts">
defineOptions({ inheritAttrs: false })

const props = defineProps<{
  modelValue?: string | number
}>()

const emit = defineEmits<{
  'update:modelValue': [value: string | number]
}>()
</script>

<template>
  <el-input
    :model-value="props.modelValue"
    @update:model-value="emit('update:modelValue', $event)"
    v-bind="$attrs"
  />
</template>
