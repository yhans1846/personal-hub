<script setup lang="ts">
import { ArrowLeft, Download, RotateCcw, Clock, BookOpen } from 'lucide-vue-next'
import UiTooltip from '@/components/UiTooltip.vue'

export interface DocMeta {
  updatedAt?: string
  readingTime?: string
}

withDefaults(defineProps<{
  title: string
  isTrash: boolean
  meta?: DocMeta
}>(), {
  meta: undefined,
})

const emit = defineEmits<{
  (e: 'back'): void
  (e: 'export'): void
  (e: 'restore'): void
}>()
</script>

<template>
  <header class="preview-header">
    <div class="header-left">
      <button class="header-back" @click="emit('back')">
        <ArrowLeft :size="15" />
        <span>返回</span>
      </button>

      <span v-if="isTrash" class="header-badge badge-warning">回收站</span>
      <span v-else class="header-badge badge-info">预览</span>
    </div>

    <div class="header-center">
      <h1 class="header-title">{{ title }}</h1>
      <div v-if="meta" class="header-meta">
        <span v-if="meta.updatedAt" class="meta-item">
          <Clock :size="11" />
          {{ meta.updatedAt }}
        </span>
        <span v-if="meta.readingTime" class="meta-item">
          <BookOpen :size="11" />
          {{ meta.readingTime }}
        </span>
      </div>
    </div>

    <div class="header-actions">
      <slot name="actions">
        <!-- 更多 -->
        <el-dropdown trigger="click" placement="bottom-end">
          <UiTooltip content="更多" placement="bottom">
            <button class="header-btn">
              <span style="letter-spacing: 2px">⋯</span>
            </button>
          </UiTooltip>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item @click="emit('export')">
                <Download :size="14" style="margin-right: 6px" />导出笔记
              </el-dropdown-item>
              <el-dropdown-item v-if="isTrash" @click="emit('restore')">
                <RotateCcw :size="14" style="margin-right: 6px" />恢复笔记
              </el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
      </slot>
    </div>
  </header>
</template>

<style scoped>
.preview-header {
  display: flex;
  align-items: center;
  height: 56px;
  padding: 0 var(--sp-5);
  border-bottom: 1px solid var(--preview-border, var(--border-color));
  flex-shrink: 0;
  gap: var(--sp-4);
  background: var(--preview-bg, var(--bg-body));
}

.header-left {
  display: flex;
  align-items: center;
  gap: var(--sp-3);
  flex-shrink: 0;
}

.header-back {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  padding: 4px 8px;
  font-size: var(--text-sm);
  color: var(--preview-text, var(--text-secondary));
  opacity: 0.72;
  border: none;
  background: none;
  border-radius: var(--radius-sm);
  cursor: pointer;
  transition: color var(--transition), background var(--transition), opacity var(--transition);
}

.header-back:hover {
  color: var(--preview-heading, var(--text-primary));
  opacity: 1;
  background: color-mix(in srgb, var(--preview-text, var(--text-primary)) 6%, transparent);
}

.header-badge {
  font-size: 11px;
  padding: 2px 8px;
  border-radius: var(--radius-md);
  font-weight: 500;
  line-height: 1.5;
}

.badge-info {
  background: color-mix(in srgb, var(--accent) 10%, transparent);
  color: var(--accent);
}

.badge-warning {
  background: color-mix(in srgb, var(--warning) 15%, transparent);
  color: var(--warning);
}

.header-center {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  min-width: 0;
  gap: 2px;
}

.header-title {
  font-size: var(--text-sm);
  font-weight: 500;
  color: var(--preview-heading, var(--text-primary));
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  max-width: 100%;
  margin: 0;
}

.header-meta {
  display: flex;
  align-items: center;
  gap: var(--sp-3);
}

.meta-item {
  display: inline-flex;
  align-items: center;
  gap: 3px;
  font-size: 11px;
  color: var(--preview-text, var(--text-tertiary));
  opacity: 0.55;
  white-space: nowrap;
}

.header-actions {
  display: flex;
  align-items: center;
  gap: 2px;
  flex-shrink: 0;
}

.header-btn {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 32px;
  height: 32px;
  border: none;
  background: none;
  border-radius: var(--radius-sm);
  color: var(--preview-text, var(--text-tertiary));
  opacity: 0.55;
  cursor: pointer;
  font-size: var(--text-sm);
  transition: color var(--transition), background var(--transition), opacity var(--transition);
}

.header-btn:hover {
  color: var(--preview-heading, var(--text-primary));
  opacity: 1;
  background: color-mix(in srgb, var(--preview-text, var(--text-primary)) 6%, transparent);
}

.header-btn.active {
  color: var(--accent);
  background: color-mix(in srgb, var(--accent) 10%, transparent);
}
</style>
