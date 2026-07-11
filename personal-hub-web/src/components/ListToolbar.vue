<script setup lang="ts">
import { Plus } from 'lucide-vue-next'

defineProps<{
  search: string
  searchPlaceholder?: string
  searchWidth?: string
  createLabel?: string
  createIcon?: boolean
}>()

const emit = defineEmits<{
  'update:search': [value: string]
  search: []
  create: []
}>()
</script>

<template>
  <div class="toolbar">
    <div class="toolbar-left">
      <el-input
        :model-value="search"
        :placeholder="searchPlaceholder || '搜索...'"
        :style="{ width: searchWidth || '200px' }"
        clearable
        @update:model-value="emit('update:search', $event)"
        @clear="emit('search')"
        @keyup.enter="emit('search')"
      >
        <template #prefix>
          <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="var(--text-tertiary)" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><circle cx="11" cy="11" r="8"/><path d="m21 21-4.3-4.3"/></svg>
        </template>
      </el-input>
      <slot name="filters" />
    </div>
    <div class="toolbar-right" v-if="$slots.actions">
      <slot name="actions" />
    </div>
    <el-button v-if="createLabel" type="primary" @click="emit('create')">
      <span><Plus :size="14" v-if="createIcon !== false" /> {{ createLabel }}</span>
    </el-button>
  </div>
</template>
