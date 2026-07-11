<script setup lang="ts">
defineProps<{
  total: number
  page: number
  size: number
}>()

const emit = defineEmits<{
  'update:page': [value: number]
}>()
</script>

<template>
  <el-pagination
    v-if="total > size"
    :total="total"
    :page-size="size"
    :current-page="page"
    layout="total, prev, pager, next"
    style="margin-top: var(--sp-6); justify-content: flex-end"
    @current-change="emit('update:page', $event)"
  />
</template>
