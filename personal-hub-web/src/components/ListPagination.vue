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
    v-if="total > 0"
    :total="total"
    :page-size="size"
    :current-page="page"
    layout="total, prev, pager, next"
    class="list-pagination"
    @current-change="emit('update:page', $event)"
  />
</template>

<style scoped>
.list-pagination {
  margin-top: var(--sp-6);
  justify-content: flex-end;
}
</style>
