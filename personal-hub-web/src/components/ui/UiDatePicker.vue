<script setup lang="ts">
defineOptions({ inheritAttrs: false })

const props = defineProps<{
  // The modelValue type varies by date picker mode:
  // date -> Date, datetime -> Date, daterange -> [Date, Date], etc.
  // We use a looser type to accommodate all modes, but avoid 'any'
  modelValue?: string | number | Date | [Date, Date] | [string, string]
}>()

const emit = defineEmits<{
  'update:modelValue': [value: string | number | Date | [Date, Date] | [string, string]]
}>()
</script>

<template>
  <el-date-picker
    :model-value="props.modelValue"
    @update:model-value="emit('update:modelValue', $event)"
    v-bind="$attrs"
  />
</template>
