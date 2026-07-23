<script setup lang="ts">
import { ref, watch } from 'vue'
import { PanelLeft } from 'lucide-vue-next'
import KnowledgeSpaceNav from './KnowledgeSpaceNav.vue'
import type { NoteFolderSelection, NoteFolderTreeVO } from '@/types/note'
import UiTooltip from '@/components/UiTooltip.vue'

const FOLDER_PANE_KEY = 'note-folder-pane-collapsed'

const props = defineProps<{
  modelValue: NoteFolderSelection
  activeNoteId?: number | null
  drawerOpen?: boolean
  /** 只读导航（预览页） */
  readonly?: boolean
}>()

const emit = defineEmits<{
  'update:modelValue': [value: NoteFolderSelection]
  'update:drawerOpen': [value: boolean]
  'open-note': [id: number]
  changed: []
  loaded: [data: NoteFolderTreeVO]
}>()

const collapsed = ref(localStorage.getItem(FOLDER_PANE_KEY) === '1')

function setCollapsed(v: boolean) {
  collapsed.value = v
  localStorage.setItem(FOLDER_PANE_KEY, v ? '1' : '0')
}

watch(
  () => props.drawerOpen,
  (open) => {
    if (open) setCollapsed(false)
  },
)

function closeDrawer() {
  emit('update:drawerOpen', false)
}
</script>

<template>
  <div class="folder-shell">
    <div
      class="folder-drawer-mask"
      :class="{ open: drawerOpen }"
      @click="closeDrawer"
    />
    <div
      class="folder-pane"
      :class="{ open: drawerOpen, collapsed }"
    >
      <KnowledgeSpaceNav
        v-show="!collapsed"
        :model-value="modelValue"
        :active-note-id="activeNoteId ?? null"
        :readonly="readonly"
        @update:model-value="emit('update:modelValue', $event)"
        @changed="emit('changed')"
        @loaded="emit('loaded', $event)"
        @open-note="emit('open-note', $event); closeDrawer()"
        @collapse="setCollapsed(true)"
      />
      <div v-if="collapsed" class="folder-pane-rail">
        <UiTooltip content="展开知识空间" placement="right">
          <button type="button" class="folder-pane-expand" @click="setCollapsed(false)">
            <PanelLeft :size="16" />
          </button>
        </UiTooltip>
      </div>
    </div>
  </div>
</template>

<style scoped>
.folder-shell {
  display: contents;
}
.folder-pane {
  flex-shrink: 0;
  height: 100%;
  min-height: 0;
  display: flex;
  flex-direction: column;
  transition: width var(--transition-duration) ease;
}
.folder-pane.collapsed {
  width: 36px;
}
.folder-pane-rail {
  width: 36px;
  height: 100%;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: flex-end;
  padding: 8px 0;
  border-right: 1px solid var(--border-color);
  background: color-mix(in srgb, var(--bg-card) 70%, var(--bg-body));
}
.folder-pane-expand {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 28px;
  height: 28px;
  border: none;
  border-radius: var(--radius-sm);
  background: transparent;
  color: var(--text-tertiary);
  cursor: pointer;
  transition: color var(--transition-duration) ease, background var(--transition-duration) ease;
}
.folder-pane-expand:hover {
  color: var(--text-primary);
  background: var(--bg-hover);
}
.folder-drawer-mask {
  display: none;
}
@media (max-width: 768px) {
  .folder-pane.collapsed {
    width: auto;
  }
  .folder-pane-rail {
    display: none;
  }
  .folder-pane {
    position: absolute;
    left: 0;
    top: 0;
    bottom: 0;
    z-index: 20;
    transform: translateX(-100%);
    transition: transform 0.2s ease;
    box-shadow: var(--shadow-md);
    background: var(--bg-body);
  }
  .folder-pane.open {
    transform: translateX(0);
  }
  .folder-drawer-mask {
    display: block;
    position: absolute;
    inset: 0;
    z-index: 15;
    background: rgba(0, 0, 0, 0.35);
    opacity: 0;
    pointer-events: none;
    transition: opacity 0.2s ease;
  }
  .folder-drawer-mask.open {
    opacity: 1;
    pointer-events: auto;
  }
}
</style>
