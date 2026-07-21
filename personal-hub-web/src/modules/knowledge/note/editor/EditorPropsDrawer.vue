<script setup lang="ts">
import type { CategoryVO } from '@/types/category'
import type { TagVO } from '@/types/tag'

defineProps<{
  visible: boolean
  categoryIds: number[]
  tagIds: number[]
  categories: CategoryVO[]
  tags: TagVO[]
  createdAtText?: string
  readingTimeText?: string
}>()

const emit = defineEmits<{
  'update:visible': [boolean]
  'update:categoryIds': [number[]]
  'update:tagIds': [number[]]
}>()
</script>

<template>
  <el-drawer
    :model-value="visible"
    title="笔记属性"
    direction="rtl"
    size="320px"
    @update:model-value="emit('update:visible', $event)"
  >
    <div class="props-field">
      <label>分类</label>
      <el-select
        :model-value="categoryIds"
        multiple
        placeholder="选择分类"
        style="width: 100%"
        @update:model-value="emit('update:categoryIds', $event)"
      >
        <el-option v-for="c in categories" :key="c.id" :label="c.name" :value="c.id" />
      </el-select>
    </div>
    <div class="props-field">
      <label>标签</label>
      <el-select
        :model-value="tagIds"
        multiple
        placeholder="选择标签"
        style="width: 100%"
        @update:model-value="emit('update:tagIds', $event)"
      >
        <el-option v-for="t in tags" :key="t.id" :label="t.name" :value="t.id" />
      </el-select>
    </div>
    <p v-if="createdAtText" class="props-static">创建 {{ createdAtText }}</p>
    <p v-if="readingTimeText" class="props-static">阅读 {{ readingTimeText }}</p>
  </el-drawer>
</template>

<style scoped>
.props-field { margin-bottom: 16px; }
.props-field label {
  display: block;
  margin-bottom: 8px;
  font-size: var(--text-sm);
  color: var(--text-secondary);
}
.props-static {
  font-size: var(--text-sm);
  color: var(--text-tertiary);
  margin: 8px 0 0;
}
</style>
