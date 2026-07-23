<script setup lang="ts">
import { ref } from 'vue'
import { PanelLeftClose, PanelLeft, PanelRightClose, PanelRight } from 'lucide-vue-next'
import UiTooltip from '@/components/UiTooltip.vue'

export interface TocItem {
  text: string
  level: number
  id: string
}

const props = withDefaults(defineProps<{
  items: TocItem[]
  activeId: string
  /** 预览页右侧；编辑器内嵌默认左侧 */
  side?: 'left' | 'right'
}>(), {
  side: 'left',
})

const emit = defineEmits<{
  (e: 'scroll-to', id: string): void
  (e: 'resize', width: number): void
}>()

const COLLAPSED_WIDTH = 34
const EXPANDED_WIDTH = 220
const TOC_MIN = 160
const TOC_MAX = 400

const isExpanded = ref(true)
const currentWidth = ref(EXPANDED_WIDTH)
const isResizing = ref(false)

function toggle() {
  isExpanded.value = !isExpanded.value
}

function startResize(e: MouseEvent) {
  isResizing.value = true
  document.body.style.cursor = 'col-resize'
  document.body.style.userSelect = 'none'
  const startX = e.clientX
  const startW = currentWidth.value
  const right = props.side === 'right'

  const onMove = (ev: MouseEvent) => {
    const delta = right ? startX - ev.clientX : ev.clientX - startX
    currentWidth.value = Math.min(TOC_MAX, Math.max(TOC_MIN, startW + delta))
    emit('resize', currentWidth.value)
  }
  const onUp = () => {
    isResizing.value = false
    document.body.style.cursor = ''
    document.body.style.userSelect = ''
    document.removeEventListener('mousemove', onMove)
    document.removeEventListener('mouseup', onUp)
  }
  document.addEventListener('mousemove', onMove)
  document.addEventListener('mouseup', onUp)
}

function scrollTo(id: string) {
  emit('scroll-to', id)
}
</script>

<template>
  <div
    class="toc-wrapper"
    :class="{ collapsed: !isExpanded, 'toc-wrapper--right': side === 'right' }"
    :style="{ width: isExpanded ? currentWidth + 'px' : COLLAPSED_WIDTH + 'px' }"
  >
    <template v-if="isExpanded">
      <aside v-if="items.length > 0" class="preview-toc">
        <div class="toc-header">
          <span class="toc-title">目录</span>
          <UiTooltip content="收起目录" placement="bottom">
            <button class="toc-collapse-btn" @click="toggle">
              <PanelRightClose v-if="side === 'right'" :size="14" />
              <PanelLeftClose v-else :size="14" />
            </button>
          </UiTooltip>
        </div>
        <nav class="toc-list">
          <button
            v-for="item in items"
            :key="item.id"
            :class="['toc-item', `toc-level-${item.level}`, { active: activeId === item.id }]"
            @click="scrollTo(item.id)"
          >
            <span class="toc-text">{{ item.text }}</span>
          </button>
        </nav>
      </aside>
      <aside v-else class="preview-toc preview-toc--empty">
        <div class="toc-header">
          <span class="toc-title">目录</span>
          <UiTooltip content="收起目录" placement="bottom">
            <button class="toc-collapse-btn" @click="toggle">
              <PanelRightClose v-if="side === 'right'" :size="14" />
              <PanelLeftClose v-else :size="14" />
            </button>
          </UiTooltip>
        </div>
        <p class="toc-empty">暂无目录</p>
      </aside>

      <!-- 拖拽条 -->
      <div
        class="toc-resize-handle"
        :class="{ active: isResizing }"
        @mousedown.prevent="startResize"
      />
    </template>

    <!-- 折叠后的展开按钮 -->
    <div v-else class="toc-collapsed-bar">
      <UiTooltip content="展开目录" :placement="side === 'right' ? 'left' : 'right'">
        <button class="toc-expand-btn" @click="toggle">
          <PanelRight v-if="side === 'right'" :size="14" />
          <PanelLeft v-else :size="14" />
        </button>
      </UiTooltip>
    </div>
  </div>
</template>

<style scoped>
.toc-wrapper {
  position: relative;
  display: flex;
  flex-shrink: 0;
  overflow: hidden;
  transition: width var(--transition-duration) ease;
  border-right: 1px solid var(--preview-border, var(--border-color));
  background: var(--preview-bg, transparent);
  z-index: 10;
}

.toc-wrapper--right {
  border-right: none;
  border-left: 1px solid var(--preview-border, var(--border-color));
}

.toc-wrapper.collapsed {
  border-right: none;
}

.toc-wrapper--right.collapsed {
  border-left: none;
}

/* 折叠后的展开条 */
.toc-collapsed-bar {
  position: absolute;
  top: 0;
  left: 0;
  bottom: 0;
  width: 32px;
  display: flex;
  flex-direction: column;
  align-items: center;
  padding-top: 12px;
}

.toc-wrapper--right .toc-collapsed-bar {
  left: auto;
  right: 0;
}

.toc-expand-btn {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 28px;
  height: 28px;
  border: none;
  background: none;
  border-radius: var(--radius-sm);
  color: var(--preview-text, var(--text-tertiary));
  opacity: 0.5;
  cursor: pointer;
  transition: color var(--transition-duration) ease, background var(--transition-duration) ease, opacity var(--transition-duration) ease;
}

.toc-expand-btn:hover {
  color: var(--preview-heading, var(--text-primary));
  opacity: 1;
  background: color-mix(in srgb, var(--preview-text, var(--text-primary)) 6%, transparent);
}

/* 目录头部（位于顶栏下方右栏，勿做成第二顶栏高度） */
.toc-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 12px 10px 8px;
}

.toc-title {
  font-size: 12px;
  font-weight: 600;
  color: var(--preview-text, var(--text-tertiary));
  opacity: 0.55;
  text-transform: uppercase;
  letter-spacing: 0.05em;
}

.toc-collapse-btn {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 24px;
  height: 24px;
  border: none;
  background: none;
  border-radius: var(--radius-sm);
  color: var(--preview-text, var(--text-tertiary));
  opacity: 0.5;
  cursor: pointer;
  transition: color var(--transition-duration) ease, background var(--transition-duration) ease, opacity var(--transition-duration) ease;
}

.toc-collapse-btn:hover {
  color: var(--preview-heading, var(--text-primary));
  opacity: 1;
  background: color-mix(in srgb, var(--preview-text, var(--text-primary)) 6%, transparent);
}

/* 展开后的目录 */
.preview-toc {
  flex: 1;
  overflow-y: auto;
  padding: 0 var(--sp-3) var(--sp-5);
  scrollbar-width: none;
}

.preview-toc::-webkit-scrollbar {
  display: none;
}

.toc-wrapper:hover .preview-toc {
  scrollbar-width: thin;
}

.toc-wrapper:hover .preview-toc::-webkit-scrollbar {
  display: block;
  width: 4px;
}

.toc-wrapper:hover .preview-toc::-webkit-scrollbar-thumb {
  background: var(--border-color);
  border-radius: var(--radius-sm);
}

.preview-toc--empty {
  display: flex;
  flex-direction: column;
}

.toc-empty {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 12px;
  color: var(--text-tertiary);
}

.toc-list {
  display: flex;
  flex-direction: column;
  gap: 1px;
}

.toc-item {
  display: flex;
  align-items: center;
  padding: 4px 10px;
  font-size: 13px;
  line-height: 1.5;
  color: var(--preview-text, var(--text-tertiary));
  opacity: 0.72;
  border: none;
  background: none;
  border-radius: var(--radius-sm);
  cursor: pointer;
  text-align: left;
  width: 100%;
  transition: color var(--transition-duration) ease, background var(--transition-duration) ease, opacity var(--transition-duration) ease;
}

.toc-item:hover {
  color: var(--preview-heading, var(--text-secondary));
  opacity: 1;
  background: color-mix(in srgb, var(--preview-text, var(--text-primary)) 6%, transparent);
}

.toc-item.active {
  color: var(--accent);
  opacity: 1;
  background: color-mix(in srgb, var(--accent) 8%, transparent);
  font-weight: 500;
}

.toc-text {
  flex: 1;
  min-width: 0;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

/* 层级缩进 */
.toc-level-1 { padding-left: 4px; }
.toc-level-2 { padding-left: 18px; }
.toc-level-3 { padding-left: 32px; }
.toc-level-4 { padding-left: 46px; }
.toc-level-5 { padding-left: 60px; }
.toc-level-6 { padding-left: 74px; }

.toc-level-1 .toc-text {
  font-weight: 500;
  color: var(--preview-heading, var(--text-secondary));
  opacity: 1;
}

/* 拖拽条 */
.toc-resize-handle {
  position: absolute;
  right: 0;
  top: 0;
  bottom: 0;
  width: 4px;
  cursor: col-resize;
  flex-shrink: 0;
  background: transparent;
  transition: background var(--transition-duration);
  z-index: 2;
}

.toc-wrapper--right .toc-resize-handle {
  right: auto;
  left: 0;
}

.toc-resize-handle:hover,
.toc-resize-handle.active {
  background: var(--accent);
}
</style>
