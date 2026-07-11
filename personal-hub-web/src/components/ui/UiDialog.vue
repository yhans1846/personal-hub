<script setup lang="ts">
defineOptions({ inheritAttrs: false })

const props = withDefaults(defineProps<{
  modelValue: boolean
  title: string
  width?: string | number
}>(), { width: 520 })

const emit = defineEmits<{
  'update:modelValue': [value: boolean]
}>()
</script>

<template>
  <el-dialog
    :model-value="props.modelValue"
    :width="props.width"
    :title="props.title"
    align-center
    @update:model-value="emit('update:modelValue', $event)"
    v-bind="$attrs"
  >
    <template v-for="(_, slot) in $slots" #[slot]="scope">
      <slot :name="slot" v-bind="scope" />
    </template>
  </el-dialog>
</template>
