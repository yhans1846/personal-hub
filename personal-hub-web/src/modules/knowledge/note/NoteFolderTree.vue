<script setup lang="ts">
import { computed, onMounted, ref, watch } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Folder, FolderOpen, FolderPlus } from 'lucide-vue-next'
import {
  createNoteFolder,
  deleteNoteFolder,
  getNoteFolderTree,
  moveNoteFolder,
  renameNoteFolder,
  updateNoteFolder,
} from '@/modules/knowledge/api'
import type { NoteFolderSelection, NoteFolderVO } from '@/types/note'
import { handleApiError, unwrapResult } from '@/utils/apiResult'
import NoteFolderTreeNode from './NoteFolderTreeNode.vue'
import {
  noteFolderDraggingKind,
  type FolderDropHint,
  type FolderDropPlace,
} from './folderDragState'

const EXPAND_KEY = 'note-folder-expanded'

const props = defineProps<{
  modelValue: NoteFolderSelection
}>()

const emit = defineEmits<{
  'update:modelValue': [value: NoteFolderSelection]
  changed: []
}>()

const tree = ref<NoteFolderVO[]>([])
const totalCount = ref(0)
const uncategorizedCount = ref(0)
const loading = ref(false)
const expanded = ref<Set<number>>(loadExpanded())
const dropHint = ref<FolderDropHint | null>(null)
const menuOpenId = ref<number | null>(null)

const selected = computed({
  get: () => props.modelValue,
  set: (v: NoteFolderSelection) => emit('update:modelValue', v),
})

function loadExpanded(): Set<number> {
  try {
    const raw = localStorage.getItem(EXPAND_KEY)
    if (!raw) return new Set()
    const arr = JSON.parse(raw) as number[]
    return new Set(Array.isArray(arr) ? arr : [])
  } catch {
    return new Set()
  }
}

function persistExpanded() {
  localStorage.setItem(EXPAND_KEY, JSON.stringify([...expanded.value]))
}

function toggleExpand(id: number) {
  const next = new Set(expanded.value)
  if (next.has(id)) next.delete(id)
  else next.add(id)
  expanded.value = next
  persistExpanded()
}

function select(sel: NoteFolderSelection) {
  selected.value = sel
  menuOpenId.value = null
}

async function loadTree() {
  loading.value = true
  try {
    const data = (await unwrapResult(getNoteFolderTree())) ?? {
      folders: [],
      totalCount: 0,
      uncategorizedCount: 0,
    }
    tree.value = data.folders ?? []
    totalCount.value = data.totalCount ?? 0
    uncategorizedCount.value = data.uncategorizedCount ?? 0
  } catch (e) {
    handleApiError(e, '加载文件夹失败')
  } finally {
    loading.value = false
  }
}

function nextSortUnder(parentId: number | null): number {
  const siblings = siblingsOf(parentId)
  if (!siblings.length) return 0
  return Math.max(...siblings.map((s) => s.sortOrder ?? 0)) + 1
}

function siblingsOf(parentId: number | null): NoteFolderVO[] {
  if (parentId == null) return tree.value
  return findNode(tree.value, parentId)?.children ?? []
}

function findNode(nodes: NoteFolderVO[], id: number): NoteFolderVO | null {
  for (const n of nodes) {
    if (n.id === id) return n
    const child = findNode(n.children ?? [], id)
    if (child) return child
  }
  return null
}

function isDescendant(ancestorId: number, maybeChildId: number): boolean {
  const root = findNode(tree.value, ancestorId)
  if (!root) return false
  const walk = (nodes: NoteFolderVO[]): boolean => {
    for (const n of nodes) {
      if (n.id === maybeChildId) return true
      if (walk(n.children ?? [])) return true
    }
    return false
  }
  return walk(root.children ?? [])
}

function createParentId(): number | null {
  const sel = selected.value
  if (typeof sel === 'number' && Number.isFinite(sel)) return sel
  return null
}

async function onCreate() {
  try {
    const { value } = await ElMessageBox.prompt('文件夹名称', '新建文件夹', {
      confirmButtonText: '创建',
      cancelButtonText: '取消',
      inputPattern: /\S+/,
      inputErrorMessage: '名称不能为空',
    })
    const name = String(value).trim()
    const parentId = createParentId()
    const created = await unwrapResult(createNoteFolder({ name, parentId }))
    if (parentId != null) {
      const next = new Set(expanded.value)
      next.add(parentId)
      expanded.value = next
      persistExpanded()
    }
    await loadTree()
    if (created?.id != null) select(created.id)
    emit('changed')
    ElMessage.success('已创建文件夹')
  } catch (e) {
    if (e === 'cancel' || e === 'close') return
    handleApiError(e, '创建文件夹失败')
  }
}

async function onRename(node: NoteFolderVO) {
  menuOpenId.value = null
  try {
    const { value } = await ElMessageBox.prompt('文件夹名称', '重命名', {
      confirmButtonText: '保存',
      cancelButtonText: '取消',
      inputValue: node.name,
      inputPattern: /\S+/,
      inputErrorMessage: '名称不能为空',
    })
    await renameNoteFolder(node.id, String(value).trim())
    await loadTree()
    emit('changed')
    ElMessage.success('已重命名')
  } catch (e) {
    if (e === 'cancel' || e === 'close') return
    handleApiError(e, '重命名失败')
  }
}

async function onDelete(node: NoteFolderVO) {
  menuOpenId.value = null
  try {
    await ElMessageBox.confirm(
      '文件夹将删除，其中笔记会移到未分类，笔记本身不会删除。确定继续？',
      '删除文件夹',
      { type: 'warning', confirmButtonText: '删除', cancelButtonText: '取消' },
    )
    await deleteNoteFolder(node.id)
    if (selected.value === node.id) select('all')
    await loadTree()
    emit('changed')
    ElMessage.success('已删除文件夹')
  } catch (e) {
    if (e === 'cancel' || e === 'close') return
    handleApiError(e, '删除文件夹失败')
  }
}

type DragPayload =
  | { type: 'note'; id: number }
  | { type: 'folder'; id: number }

function parseDrag(e: DragEvent): DragPayload | null {
  const raw = e.dataTransfer?.getData('application/x-ph-note')
    || e.dataTransfer?.getData('application/x-ph-folder')
    || e.dataTransfer?.getData('application/x-ph-folder-drag')
    || e.dataTransfer?.getData('text/plain')
  if (!raw) return null
  try {
    const data = JSON.parse(raw) as DragPayload
    if (data?.type === 'note' && typeof data.id === 'number') return data
    if (data?.type === 'folder' && typeof data.id === 'number') return data
  } catch {
    /* ignore */
  }
  return null
}

function onFolderDragStart(e: DragEvent, id: number) {
  noteFolderDraggingKind.value = 'folder'
  const payload = JSON.stringify({ type: 'folder', id })
  e.dataTransfer?.setData('application/x-ph-folder', payload)
  e.dataTransfer?.setData('application/x-ph-folder-drag', payload)
  e.dataTransfer?.setData('text/plain', payload)
  e.dataTransfer!.effectAllowed = 'move'
}

function isNoteDragging(e: DragEvent): boolean {
  if (noteFolderDraggingKind.value === 'note') return true
  if (noteFolderDraggingKind.value === 'folder') return false
  const types = Array.from(e.dataTransfer?.types ?? [])
  return types.some((t) => t === 'application/x-ph-note' || t.endsWith('ph-note'))
}

/** 进入行范围即锁定目标；禁止冒泡到外层「根级」区 */
function onZoneDragOver(
  e: DragEvent,
  target: 'none' | 'root' | number,
  place: FolderDropPlace = 'inside',
) {
  e.preventDefault()
  e.stopPropagation()
  if (e.dataTransfer) e.dataTransfer.dropEffect = 'move'

  if (target === 'none') {
    dropHint.value = { kind: 'none' }
    return
  }
  if (target === 'root') {
    dropHint.value = { kind: 'root' }
    return
  }

  // 笔记：整行放入该夹；文件夹：显式 place（缘=平级，行中=子级）
  if (isNoteDragging(e)) {
    dropHint.value = { kind: 'folder', id: target, place: 'inside' }
    return
  }
  dropHint.value = { kind: 'folder', id: target, place }
}

function onTreeBodyDragLeave(e: DragEvent) {
  const current = e.currentTarget as HTMLElement
  const related = e.relatedTarget as Node | null
  if (related && current.contains(related)) return
  dropHint.value = null
}

function clearDragState() {
  dropHint.value = null
  noteFolderDraggingKind.value = null
}

async function reindexSiblings(parentId: number | null, orderedIds: number[]) {
  for (let i = 0; i < orderedIds.length; i++) {
    const id = orderedIds[i]!
    const node = findNode(tree.value, id)
    if (node && (node.parentId ?? null) === parentId && node.sortOrder === i) {
      continue
    }
    await moveNoteFolder(id, { parentId, sortOrder: i })
    if (node) {
      node.parentId = parentId
      node.sortOrder = i
    }
  }
}

async function moveFolderToPlace(dragId: number, targetId: number, place: FolderDropPlace) {
  if (place === 'inside') {
    if (dragId === targetId) return
    if (isDescendant(dragId, targetId)) {
      ElMessage.warning('不能将文件夹移动到其子文件夹下')
      return
    }
    await moveNoteFolder(dragId, {
      parentId: targetId,
      sortOrder: nextSortUnder(targetId),
    })
    const next = new Set(expanded.value)
    next.add(targetId)
    expanded.value = next
    persistExpanded()
    return
  }

  const target = findNode(tree.value, targetId)
  if (!target) return
  const parentId = target.parentId ?? null

  if (parentId != null && (parentId === dragId || isDescendant(dragId, parentId))) {
    ElMessage.warning('不能将文件夹移动到其子文件夹下')
    return
  }

  const ordered = siblingsOf(parentId).map((s) => s.id).filter((id) => id !== dragId)
  const tIdx = ordered.indexOf(targetId)
  if (tIdx < 0) return
  ordered.splice(place === 'before' ? tIdx : tIdx + 1, 0, dragId)
  await reindexSiblings(parentId, ordered)
}

async function handleDrop(
  e: DragEvent,
  target: 'none' | 'root' | number,
  placeArg: FolderDropPlace = 'inside',
) {
  e.preventDefault()
  e.stopPropagation()
  const hint = dropHint.value
  dropHint.value = null
  noteFolderDraggingKind.value = null
  const payload = parseDrag(e)
  if (!payload) return

  try {
    if (payload.type === 'note') {
      const assignTo = target === 'none' || target === 'root' ? null : target
      await updateNoteFolder(payload.id, assignTo)
      ElMessage.success(assignTo == null ? '已移到未分类' : '已移到文件夹')
      await loadTree()
      emit('changed')
      return
    }

    if (payload.type === 'folder') {
      if (target === 'none') {
        ElMessage.warning('文件夹不能移入「未分类」')
        return
      }
      if (target === 'root') {
        const ordered = tree.value.map((s) => s.id).filter((id) => id !== payload.id)
        ordered.push(payload.id)
        await reindexSiblings(null, ordered)
      } else {
        const place: FolderDropPlace =
          hint?.kind === 'folder' && hint.id === target ? hint.place : placeArg
        await moveFolderToPlace(payload.id, target, place)
      }
      await loadTree()
      emit('changed')
      ElMessage.success('已移动文件夹')
    }
  } catch (err) {
    handleApiError(err, '移动失败')
    await loadTree()
  }
}

onMounted(loadTree)

watch(
  () => props.modelValue,
  () => { menuOpenId.value = null },
)

defineExpose({ reload: loadTree })
</script>

<template>
  <aside class="folder-tree" aria-label="笔记文件夹">
    <div class="folder-tree-head">
      <span class="folder-tree-title">文件夹</span>
      <button type="button" class="folder-icon-btn" title="新建文件夹" @click="onCreate">
        <FolderPlus :size="15" />
      </button>
    </div>

    <div
      class="folder-tree-body"
      @dragleave="onTreeBodyDragLeave"
      @dragend="clearDragState"
    >
      <button
        type="button"
        class="folder-row"
        :class="{ active: selected === 'all' }"
        @click="select('all')"
      >
        <FolderOpen :size="14" class="folder-row-icon" />
        <span class="folder-row-label">全部</span>
        <span class="folder-count">{{ totalCount }}</span>
      </button>

      <div
        role="button"
        tabindex="0"
        class="folder-row folder-row--drop"
        :class="{ active: selected === 'none', 'drop-inside': dropHint?.kind === 'none' }"
        @click="select('none')"
        @keydown.enter="select('none')"
        @dragover="onZoneDragOver($event, 'none')"
        @drop="handleDrop($event, 'none')"
      >
        <Folder :size="14" class="folder-row-icon" />
        <span class="folder-row-label">未分类</span>
        <span class="folder-count">{{ uncategorizedCount }}</span>
      </div>

      <p v-if="!loading && tree.length === 0" class="folder-empty">暂无文件夹，点击右上角新建</p>

      <div v-else class="folder-section-label">我的文件夹</div>

      <div class="folder-list">
        <NoteFolderTreeNode
          v-for="node in tree"
          :key="node.id"
          :node="node"
          :depth="0"
          :selected="selected"
          :expanded="expanded"
          :drop-hint="dropHint"
          :menu-open-id="menuOpenId"
          @select="select"
          @toggle="toggleExpand"
          @drag-start="onFolderDragStart"
          @zone-over="onZoneDragOver"
          @zone-drop="handleDrop"
          @menu="menuOpenId = $event"
          @rename="onRename"
          @delete="onDelete"
        />
      </div>

      <div
        class="folder-root-drop"
        :class="{ 'drop-over': dropHint?.kind === 'root' }"
        @dragover="onZoneDragOver($event, 'root')"
        @drop="handleDrop($event, 'root')"
      >
        拖到此处放到根级末尾
      </div>
    </div>
  </aside>
</template>

<style scoped>
.folder-tree {
  display: flex;
  flex-direction: column;
  height: 100%;
  min-height: 0;
  width: 240px;
  flex-shrink: 0;
  border-right: 1px solid var(--border-color);
  background: color-mix(in srgb, var(--bg-card) 70%, var(--bg-body));
}
.folder-tree-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 10px 12px 8px;
  flex-shrink: 0;
}
.folder-tree-title {
  font-size: var(--text-sm);
  font-weight: 600;
  color: var(--text-secondary);
}
.folder-tree-body {
  flex: 1;
  min-height: 0;
  overflow: auto;
  padding: 0 6px 12px;
}
.folder-empty {
  margin: 8px 8px 0;
  font-size: 12px;
  color: var(--text-tertiary);
}
.folder-section-label {
  margin: 10px 8px 4px;
  font-size: 11px;
  font-weight: 600;
  color: var(--text-tertiary);
  letter-spacing: 0.02em;
}
.folder-list {
  display: flex;
  flex-direction: column;
  gap: 2px;
  margin-top: 4px;
}
.folder-root-drop {
  margin-top: 8px;
  min-height: 36px;
  padding: 8px;
  border-radius: var(--radius-sm);
  border: 1px dashed var(--border-color);
  font-size: 12px;
  color: var(--text-tertiary);
  display: flex;
  align-items: center;
  justify-content: center;
  text-align: center;
}
.folder-root-drop.drop-over {
  border-color: var(--accent);
  color: var(--accent);
  background: color-mix(in srgb, var(--accent) 8%, transparent);
}

.folder-row {
  display: flex;
  align-items: center;
  gap: 6px;
  width: 100%;
  min-height: 36px;
  border: none;
  background: transparent;
  color: var(--text-primary);
  text-align: left;
  padding: 8px 10px;
  border-radius: var(--radius-sm);
  cursor: pointer;
  font-size: var(--text-sm);
  box-sizing: border-box;
}
.folder-row--drop > .folder-row-icon,
.folder-row--drop > .folder-row-label,
.folder-row--drop > .folder-count {
  pointer-events: none;
}
.folder-row:hover {
  background: var(--bg-hover);
}
.folder-row.active {
  background: color-mix(in srgb, var(--accent) 12%, transparent);
  color: var(--accent);
}
.folder-row.drop-inside {
  outline: 2px solid var(--accent);
  background: color-mix(in srgb, var(--accent) 14%, transparent);
}
.folder-row-icon {
  flex-shrink: 0;
  opacity: 0.75;
}
.folder-row-label {
  flex: 1;
  min-width: 0;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.folder-count {
  flex-shrink: 0;
  min-width: 1.25em;
  padding: 0 4px;
  font-size: 11px;
  font-variant-numeric: tabular-nums;
  color: var(--text-tertiary);
  text-align: right;
}
.folder-icon-btn {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 26px;
  height: 26px;
  border: none;
  border-radius: var(--radius-sm);
  background: transparent;
  color: var(--text-tertiary);
  cursor: pointer;
}
.folder-icon-btn:hover {
  background: var(--bg-hover);
  color: var(--text-primary);
}

:deep(.folder-row) {
  display: flex;
  align-items: center;
  gap: 6px;
  width: 100%;
  min-height: 36px;
  border: none;
  background: transparent;
  color: var(--text-primary);
  text-align: left;
  padding: 8px 10px;
  border-radius: var(--radius-sm);
  cursor: pointer;
  font-size: var(--text-sm);
  position: relative;
  box-sizing: border-box;
}
:deep(.folder-row--drop > .folder-row-icon),
:deep(.folder-row--drop > .folder-row-label),
:deep(.folder-row--drop > .folder-count) {
  pointer-events: none;
}
:deep(.folder-row:hover) {
  background: var(--bg-hover);
}
:deep(.folder-row.active) {
  background: color-mix(in srgb, var(--accent) 12%, transparent);
  color: var(--accent);
}
:deep(.folder-row.drop-inside) {
  outline: 2px solid var(--accent);
  background: color-mix(in srgb, var(--accent) 14%, transparent);
}
:deep(.folder-drop-edge) {
  position: absolute;
  left: 0;
  right: 0;
  height: 10px;
  z-index: 3;
}
:deep(.folder-drop-edge--before) {
  top: 0;
}
:deep(.folder-drop-edge--after) {
  bottom: 0;
}
:deep(.folder-row.drop-before)::before,
:deep(.folder-row.drop-after)::after {
  content: '';
  position: absolute;
  left: 8px;
  right: 8px;
  height: 2px;
  border-radius: 1px;
  background: var(--accent);
  pointer-events: none;
  z-index: 4;
}
:deep(.folder-row.drop-before)::before {
  top: 0;
}
:deep(.folder-row.drop-after)::after {
  bottom: 0;
}
:deep(.folder-row-icon) {
  flex-shrink: 0;
  opacity: 0.75;
}
:deep(.folder-row-label) {
  flex: 1;
  min-width: 0;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
:deep(.folder-count) {
  flex-shrink: 0;
  min-width: 1.25em;
  padding: 0 4px;
  font-size: 11px;
  font-variant-numeric: tabular-nums;
  color: var(--text-tertiary);
  text-align: right;
}
:deep(.folder-expand) {
  width: 18px;
  height: 18px;
  padding: 0;
  border: none;
  background: transparent;
  color: var(--text-tertiary);
  display: inline-flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  cursor: pointer;
}
:deep(.folder-expand-spacer) {
  display: inline-block;
  width: 14px;
}
:deep(.folder-row-actions) {
  position: relative;
  flex-shrink: 0;
  opacity: 0;
  pointer-events: auto;
}
:deep(.folder-row--node:hover .folder-row-actions),
:deep(.folder-row--node.active .folder-row-actions) {
  opacity: 1;
}
:deep(.folder-icon-btn) {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 26px;
  height: 26px;
  border: none;
  border-radius: var(--radius-sm);
  background: transparent;
  color: var(--text-tertiary);
  cursor: pointer;
}
:deep(.folder-icon-btn:hover) {
  background: var(--bg-hover);
  color: var(--text-primary);
}
:deep(.folder-menu) {
  position: absolute;
  right: 0;
  top: 100%;
  z-index: 5;
  min-width: 110px;
  padding: 4px;
  border-radius: var(--radius-md);
  border: 1px solid var(--border-color);
  background: var(--bg-card);
  box-shadow: var(--shadow-md);
}
:deep(.folder-menu-item) {
  display: flex;
  align-items: center;
  gap: 6px;
  width: 100%;
  border: none;
  background: transparent;
  padding: 6px 8px;
  border-radius: var(--radius-sm);
  font-size: 12px;
  color: var(--text-primary);
  cursor: pointer;
}
:deep(.folder-menu-item:hover) {
  background: var(--bg-hover);
}
:deep(.folder-menu-item--danger) {
  color: var(--danger);
}
</style>
