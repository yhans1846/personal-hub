<script setup lang="ts">
import type { SaveStatus } from './useAutoSave'
import type { EditorMode } from './useEditorMode'
import {
  ArrowLeft,
  X,
  Star,
  MoreHorizontal,
  Download,
  Trash2,
  Eye,
  Edit3,
  Columns2,
  Maximize2,
  Minimize2,
  SlidersHorizontal,
} from 'lucide-vue-next'
import UiTooltip from '@/components/UiTooltip.vue'

withDefaults(
  defineProps<{
    title: string
    saveStatus: SaveStatus
    isFavorite: boolean
    mode: EditorMode
    isFullscreen?: boolean
    /** back=返回列表；close=关闭 overlay */
    closeMode?: 'back' | 'close'
  }>(),
  { closeMode: 'back' },
)

const emit = defineEmits<{
  back: []
  toggleFavorite: []
  toggleFullscreen: []
  exportNote: []
  remove: []
  'update:title': [value: string]
  'update:mode': [mode: EditorMode]
  openProps: []
}>()
</script>

<template>
  <header
    class="editor-header"
    :class="{ 'is-fullscreen': isFullscreen }"
  >
    <div class="header-left">
      <UiTooltip :content="closeMode === 'close' ? '关闭' : '返回'" placement="bottom">
        <button
          class="header-btn"
          @click="emit('back')"
        >
          <X v-if="closeMode === 'close'" :size="18" />
          <ArrowLeft v-else :size="18" />
          <span>{{ closeMode === 'close' ? '关闭' : '返回' }}</span>
        </button>
      </UiTooltip>
    </div>

    <div class="header-center">
      <input
        class="header-title"
        :value="title"
        placeholder="请输入标题..."
        @input="emit('update:title', ($event.target as HTMLInputElement).value)"
      />
    </div>

    <div class="header-right">
      <span v-if="saveStatus === 'saving'" class="save-status saving">
        <span class="status-dot" /> 保存中...
      </span>
      <span v-else-if="saveStatus === 'success'" class="save-status success">
        <span class="status-dot" /> 已保存
      </span>
      <span v-else-if="saveStatus === 'error'" class="save-status error">
        <span class="status-dot" /> 保存失败
      </span>
      <span v-else-if="saveStatus === 'dirty'" class="save-status dirty">
        <span class="status-dot" /> 未保存
      </span>

      <UiTooltip content="笔记属性" placement="bottom">
        <button
          class="header-btn icon-only"
          @click="emit('openProps')"
        >
          <SlidersHorizontal :size="16" />
        </button>
      </UiTooltip>

      <div class="mode-group">
        <UiTooltip content="仅编辑" placement="bottom">
          <button
            class="header-btn icon-only"
            :class="{ active: mode === 'edit' }"
            @click="emit('update:mode', 'edit')"
          >
            <Edit3 :size="16" />
          </button>
        </UiTooltip>
        <UiTooltip content="分屏" placement="bottom">
          <button
            class="header-btn icon-only"
            :class="{ active: mode === 'split' }"
            @click="emit('update:mode', 'split')"
          >
            <Columns2 :size="16" />
          </button>
        </UiTooltip>
        <UiTooltip content="仅预览" placement="bottom">
          <button
            class="header-btn icon-only"
            :class="{ active: mode === 'preview' }"
            @click="emit('update:mode', 'preview')"
          >
            <Eye :size="16" />
          </button>
        </UiTooltip>
      </div>

      <UiTooltip :content="isFavorite ? '取消收藏' : '收藏'" placement="bottom">
        <button
          class="header-btn icon-only"
          @click="emit('toggleFavorite')"
        >
          <Star
            :size="16"
            :fill="isFavorite ? 'var(--warning)' : 'none'"
            :stroke="isFavorite ? 'var(--warning)' : 'var(--text-tertiary)'"
          />
        </button>
      </UiTooltip>

      <UiTooltip :content="isFullscreen ? '退出全屏 (Esc)' : '全屏 (Ctrl+Shift+F)'" placement="bottom">
        <button
          class="header-btn icon-only"
          @click="emit('toggleFullscreen')"
        >
          <Maximize2 v-if="!isFullscreen" :size="16" />
          <Minimize2 v-else :size="16" />
        </button>
      </UiTooltip>

      <el-dropdown trigger="click" placement="bottom-end">
        <button class="header-btn icon-only" @click.prevent>
          <MoreHorizontal :size="16" />
        </button>
        <template #dropdown>
          <el-dropdown-menu>
            <el-dropdown-item @click="emit('exportNote')">
              <Download :size="14" /> 导出 Markdown
            </el-dropdown-item>
            <el-dropdown-item divided @click="emit('remove')" style="color: var(--danger)">
              <Trash2 :size="14" /> 删除笔记
            </el-dropdown-item>
          </el-dropdown-menu>
        </template>
      </el-dropdown>
    </div>
  </header>
</template>

<style scoped>
.editor-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  height: 56px;
  padding: 0 24px;
  border-bottom: 1px solid var(--border-color);
  flex-shrink: 0;
  background: var(--bg-card);
  z-index: 10;
  transition: background var(--transition-duration), border-color var(--transition-duration);
}
.editor-header.is-fullscreen {
  background: var(--bg-body);
}
.header-left,
.header-right {
  display: flex;
  align-items: center;
  gap: 6px;
}
.header-center {
  flex: 1;
  display: flex;
  align-items: center;
  padding: 0 16px;
  min-width: 0;
}
.header-title {
  flex: 1;
  width: 100%;
  border: none;
  outline: none;
  background: transparent;
  font-size: var(--text-lg);
  font-weight: 600;
  color: var(--text-primary);
  font-family: var(--font-sans);
  padding: 6px 8px;
  border-radius: var(--radius-sm);
  transition: background var(--transition);
  min-width: 0;
}
.header-title::placeholder {
  color: var(--text-placeholder);
  font-weight: 400;
}
.header-title:focus {
  background: var(--bg-hover);
}
.header-btn {
  display: flex;
  align-items: center;
  gap: 4px;
  background: none;
  border: none;
  color: var(--text-secondary);
  font-size: var(--text-sm);
  cursor: pointer;
  padding: 6px 10px;
  border-radius: var(--radius-sm);
  transition: all var(--transition);
  white-space: nowrap;
}
.header-btn:hover {
  background: var(--bg-hover);
  color: var(--text-primary);
}
.header-btn.icon-only {
  padding: 6px;
}
.header-btn.active {
  color: var(--accent);
  background: var(--bg-hover);
}
.mode-group {
  display: flex;
  align-items: center;
  gap: 2px;
  padding: 2px;
  border-radius: var(--radius-sm);
}
.save-status {
  font-size: var(--text-xs);
  display: flex;
  align-items: center;
  gap: 4px;
  padding: 4px 8px;
  border-radius: var(--radius-sm);
  white-space: nowrap;
}
.status-dot {
  width: 6px;
  height: 6px;
  border-radius: 50%;
  display: inline-block;
}
.save-status.saving { color: var(--accent); }
.save-status.saving .status-dot { background: var(--accent); animation: pulse 1s infinite; }
.save-status.success { color: var(--success); }
.save-status.success .status-dot { background: var(--success); }
.save-status.error { color: var(--danger); background: var(--danger-light); }
.save-status.error .status-dot { background: var(--danger); }
.save-status.dirty { color: var(--warning); }
.save-status.dirty .status-dot { background: var(--warning); }
@keyframes pulse {
  0%, 100% { opacity: 1; }
  50% { opacity: 0.4; }
}
@media (max-width: 768px) {
  .editor-header {
    padding: 0 12px;
    height: 48px;
  }
  .header-btn span {
    display: none;
  }
  .header-center {
    padding: 0 8px;
  }
}
</style>
