<script setup lang="ts">
defineOptions({ inheritAttrs: false })

const props = defineProps<{
  modelValue?: string | number | boolean | object
  options?: { value: string | number; label: string }[]
}>()

const emit = defineEmits<{
  'update:modelValue': [value: string | number | boolean | object]
}>()
</script>

<template>
  <el-select
    :model-value="props.modelValue"
    @update:model-value="emit('update:modelValue', $event)"
    v-bind="$attrs"
  >
    <el-option
      v-for="opt in options"
      :key="opt.value"
      :value="opt.value"
      :label="opt.label"
    />
    <template v-for="(_, slot) in $slots" #[slot]="scope">
      <slot :name="slot" v-bind="scope" />
    </template>
  </el-select>
</template>
