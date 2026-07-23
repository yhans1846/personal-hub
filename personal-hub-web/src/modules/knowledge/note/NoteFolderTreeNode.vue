<script setup lang="ts">
import { computed } from 'vue'
import { ChevronDown, ChevronRight, ChevronsDownUp, ChevronsUpDown, FileText, Folder, FolderPlus, MoreHorizontal, Pencil, Trash2 } from 'lucide-vue-next'
import type { NoteFolderSelection, NoteFolderVO } from '@/types/note'
import type { FolderDropHint, FolderDropPlace } from './folderDragState'
import NoteFolderTreeNode from './NoteFolderTreeNode.vue'
import UiTooltip from '@/components/UiTooltip.vue'

const props = defineProps<{
  node: NoteFolderVO
  depth: number
  selected: NoteFolderSelection
  expanded: Set<number>
  dropHint: FolderDropHint | null
  menuOpenId: number | null
  activeNoteId?: number | null
  readonly?: boolean
}>()

const emit = defineEmits<{
  select: [value: NoteFolderSelection]
  toggle: [id: number]
  'toggle-subtree': [node: NoteFolderVO]
  'drag-start': [e: DragEvent, id: number]
  'zone-over': [e: DragEvent, id: number, place: FolderDropPlace]
  'zone-drop': [e: DragEvent, id: number, place: FolderDropPlace]
  menu: [id: number | null]
  'create-child': [node: NoteFolderVO]
  rename: [node: NoteFolderVO]
  delete: [node: NoteFolderVO]
  'open-note': [id: number]
}>()

const hasSubfolders = computed(() => (props.node.children?.length ?? 0) > 0)
const hasNotes = computed(() => (props.node.notes?.length ?? 0) > 0)
const hasChildren = computed(() => hasSubfolders.value || hasNotes.value)
const open = computed(() => props.expanded.has(props.node.id))
const active = computed(() => props.selected === props.node.id)
const menuOpen = computed(() => props.menuOpenId === props.node.id)

/** 本夹及子孙中可展开 id（有子夹或笔记） */
function collectSubtreeIds(n: NoteFolderVO): number[] {
  const ids: number[] = []
  const walk = (list: NoteFolderVO[]) => {
    for (const item of list) {
      const kids = (item.children?.length ?? 0) > 0 || (item.notes?.length ?? 0) > 0
      if (kids) ids.push(item.id)
      if (item.children?.length) walk(item.children)
    }
  }
  walk([n])
  return ids
}

const subtreeIds = computed(() => collectSubtreeIds(props.node))
const canToggleSubtree = computed(() => subtreeIds.value.length > 0)
const subtreeFullyExpanded = computed(() =>
  canToggleSubtree.value && subtreeIds.value.every((id) => props.expanded.has(id)),
)

const place = computed((): FolderDropPlace | null => {
  const h = props.dropHint
  if (h?.kind === 'folder' && h.id === props.node.id) return h.place
  return null
})

function onEdgeOver(e: DragEvent, p: FolderDropPlace) {
  e.preventDefault()
  e.stopPropagation()
  emit('zone-over', e, props.node.id, p)
}

function onEdgeDrop(e: DragEvent, p: FolderDropPlace) {
  e.preventDefault()
  e.stopPropagation()
  emit('zone-drop', e, props.node.id, p)
}

function onRowOver(e: DragEvent) {
  e.preventDefault()
  e.stopPropagation()
  emit('zone-over', e, props.node.id, 'inside')
}

function onRowDrop(e: DragEvent) {
  e.preventDefault()
  e.stopPropagation()
  emit('zone-drop', e, props.node.id, 'inside')
}

function noteTitle(title?: string) {
  return title?.trim() || '无标题笔记'
}
</script>

<template>
  <div class="folder-node">
    <div
      class="folder-row folder-row--node"
      :class="{
        active,
        'folder-row--drop': !readonly,
        'drop-inside': !readonly && place === 'inside',
        'drop-before': !readonly && place === 'before',
        'drop-after': !readonly && place === 'after',
      }"
      :style="{ paddingLeft: `${10 + depth * 14}px` }"
      :draggable="!readonly"
      @click="emit('select', node.id)"
      @dragstart="!readonly && emit('drag-start', $event, node.id)"
      @dragover="!readonly && onRowOver($event)"
      @drop="!readonly && onRowDrop($event)"
    >
      <template v-if="!readonly">
        <!-- 上/下缘：平级插入；中间行：成为子文件夹 -->
        <div
          class="folder-drop-edge folder-drop-edge--before"
          @dragover="onEdgeOver($event, 'before')"
          @drop="onEdgeDrop($event, 'before')"
        />
        <div
          class="folder-drop-edge folder-drop-edge--after"
          @dragover="onEdgeOver($event, 'after')"
          @drop="onEdgeDrop($event, 'after')"
        />
      </template>

      <button
        type="button"
        class="folder-expand"
        @click.stop="hasChildren && emit('toggle', node.id)"
      >
        <ChevronDown v-if="hasChildren && open" :size="14" />
        <ChevronRight v-else-if="hasChildren" :size="14" />
        <span v-else class="folder-expand-spacer" />
      </button>
      <Folder :size="14" class="folder-row-icon" />
      <span class="folder-row-label">{{ node.name }}</span>
      <span class="folder-count">{{ node.noteCount ?? 0 }}</span>
      <div class="folder-row-actions" @click.stop>
        <UiTooltip
          v-if="canToggleSubtree"
          :content="subtreeFullyExpanded ? '全部收起' : '全部展开'"
          placement="bottom"
          :show-after="300"
        >
          <button
            type="button"
            class="folder-icon-btn"
            @click="emit('toggle-subtree', node)"
          >
            <ChevronsDownUp v-if="subtreeFullyExpanded" :size="14" />
            <ChevronsUpDown v-else :size="14" />
          </button>
        </UiTooltip>
        <template v-if="!readonly">
          <UiTooltip content="更多" placement="bottom" :show-after="400">
            <button
              type="button"
              class="folder-icon-btn"
              @click="emit('menu', menuOpen ? null : node.id)"
            >
              <MoreHorizontal :size="14" />
            </button>
          </UiTooltip>
          <div v-if="menuOpen" class="folder-menu">
            <button type="button" class="folder-menu-item" @click="emit('create-child', node)">
              <FolderPlus :size="13" /> 新建文件夹
            </button>
            <button type="button" class="folder-menu-item" @click="emit('rename', node)">
              <Pencil :size="13" /> 重命名
            </button>
            <button type="button" class="folder-menu-item folder-menu-item--danger" @click="emit('delete', node)">
              <Trash2 :size="13" /> 删除
            </button>
          </div>
        </template>
      </div>
    </div>

    <template v-if="hasChildren && open">
      <button
        v-for="note in node.notes ?? []"
        :key="'n-' + note.id"
        type="button"
        class="folder-row folder-row--note"
        :class="{ active: activeNoteId === note.id }"
        :style="{ paddingLeft: `${24 + depth * 14}px` }"
        @click="emit('open-note', note.id)"
      >
        <span class="folder-expand-spacer" />
        <FileText :size="14" class="folder-row-icon" />
        <span class="folder-row-label">{{ noteTitle(note.title) }}</span>
      </button>
      <NoteFolderTreeNode
        v-for="child in node.children"
        :key="child.id"
        :node="child"
        :depth="depth + 1"
        :selected="selected"
        :expanded="expanded"
        :drop-hint="dropHint"
        :menu-open-id="menuOpenId"
        :active-note-id="activeNoteId ?? null"
        :readonly="readonly"
        @select="emit('select', $event)"
        @toggle="emit('toggle', $event)"
        @toggle-subtree="emit('toggle-subtree', $event)"
        @drag-start="(e, id) => emit('drag-start', e, id)"
        @zone-over="(e, id, p) => emit('zone-over', e, id, p)"
        @zone-drop="(e, id, p) => emit('zone-drop', e, id, p)"
        @menu="emit('menu', $event)"
        @create-child="emit('create-child', $event)"
        @rename="emit('rename', $event)"
        @delete="emit('delete', $event)"
        @open-note="emit('open-note', $event)"
      />
    </template>
  </div>
</template>
