<script setup lang="ts">
defineOptions({ inheritAttrs: false })

withDefaults(defineProps<{
  type?: 'primary' | 'default' | 'danger'
  loading?: boolean
}>(), { type: 'default' })
</script>

<template>
  <el-button
    :type="type"
    :loading="loading"
    v-bind="$attrs"
  >
    <slot />
  </el-button>
</template>
