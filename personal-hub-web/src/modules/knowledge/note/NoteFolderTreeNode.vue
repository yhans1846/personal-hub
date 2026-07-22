<script setup lang="ts">
import { computed } from 'vue'
import { ChevronDown, ChevronRight, Folder, MoreHorizontal, Pencil, Trash2 } from 'lucide-vue-next'
import type { NoteFolderSelection, NoteFolderVO } from '@/types/note'
import type { FolderDropHint, FolderDropPlace } from './folderDragState'
import NoteFolderTreeNode from './NoteFolderTreeNode.vue'

const props = defineProps<{
  node: NoteFolderVO
  depth: number
  selected: NoteFolderSelection
  expanded: Set<number>
  dropHint: FolderDropHint | null
  menuOpenId: number | null
}>()

const emit = defineEmits<{
  select: [value: NoteFolderSelection]
  toggle: [id: number]
  'drag-start': [e: DragEvent, id: number]
  'zone-over': [e: DragEvent, id: number, place: FolderDropPlace]
  'zone-drop': [e: DragEvent, id: number, place: FolderDropPlace]
  menu: [id: number | null]
  rename: [node: NoteFolderVO]
  delete: [node: NoteFolderVO]
}>()

const hasChildren = computed(() => (props.node.children?.length ?? 0) > 0)
const open = computed(() => props.expanded.has(props.node.id))
const active = computed(() => props.selected === props.node.id)
const menuOpen = computed(() => props.menuOpenId === props.node.id)

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
</script>

<template>
  <div class="folder-node">
    <div
      class="folder-row folder-row--node folder-row--drop"
      :class="{
        active,
        'drop-inside': place === 'inside',
        'drop-before': place === 'before',
        'drop-after': place === 'after',
      }"
      :style="{ paddingLeft: `${10 + depth * 14}px` }"
      draggable="true"
      @click="emit('select', node.id)"
      @dragstart="emit('drag-start', $event, node.id)"
      @dragover="onRowOver"
      @drop="onRowDrop"
    >
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
        <button
          type="button"
          class="folder-icon-btn"
          title="更多"
          @click="emit('menu', menuOpen ? null : node.id)"
        >
          <MoreHorizontal :size="14" />
        </button>
        <div v-if="menuOpen" class="folder-menu">
          <button type="button" class="folder-menu-item" @click="emit('rename', node)">
            <Pencil :size="13" /> 重命名
          </button>
          <button type="button" class="folder-menu-item folder-menu-item--danger" @click="emit('delete', node)">
            <Trash2 :size="13" /> 删除
          </button>
        </div>
      </div>
    </div>

    <template v-if="hasChildren && open">
      <NoteFolderTreeNode
        v-for="child in node.children"
        :key="child.id"
        :node="child"
        :depth="depth + 1"
        :selected="selected"
        :expanded="expanded"
        :drop-hint="dropHint"
        :menu-open-id="menuOpenId"
        @select="emit('select', $event)"
        @toggle="emit('toggle', $event)"
        @drag-start="(e, id) => emit('drag-start', e, id)"
        @zone-over="(e, id, p) => emit('zone-over', e, id, p)"
        @zone-drop="(e, id, p) => emit('zone-drop', e, id, p)"
        @menu="emit('menu', $event)"
        @rename="emit('rename', $event)"
        @delete="emit('delete', $event)"
      />
    </template>
  </div>
</template>
