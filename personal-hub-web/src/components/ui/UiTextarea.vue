<script setup lang="ts">
defineOptions({ inheritAttrs: false })

const props = defineProps<{
  modelValue?: string
}>()

const emit = defineEmits<{
  'update:modelValue': [value: string]
}>()
</script>

<template>
  <el-input
    :model-value="props.modelValue"
    type="textarea"
    :autosize="{ minRows: 4, maxRows: 12 }"
    @update:model-value="emit('update:modelValue', $event)"
    v-bind="$attrs"
  />
</template>
