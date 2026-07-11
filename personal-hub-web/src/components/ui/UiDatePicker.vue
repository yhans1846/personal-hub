<script setup lang="ts">
defineOptions({ inheritAttrs: false })

const props = defineProps<{
  modelValue?: any
}>()

const emit = defineEmits<{
  'update:modelValue': [value: any]
}>()
</script>

<template>
  <el-date-picker
    :model-value="props.modelValue"
    @update:model-value="emit('update:modelValue', $event)"
    v-bind="$attrs"
  />
</template>
