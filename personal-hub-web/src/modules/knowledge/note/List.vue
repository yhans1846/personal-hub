<script setup lang="ts">
import { computed, ref, watch } from 'vue'
import { getNoteList, deleteNote, archiveNote, toggleFavorite, exportNotesBatch } from '@/modules/knowledge/api'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, FileText, Star, Trash2, Clock, Eye, Upload, LayoutList, LayoutGrid, Download, CheckSquare, FolderTree, PanelLeft } from 'lucide-vue-next'
import { EmptyState, PageHeader, ListToolbar, ListPagination } from '@/components'
import type { NoteVO, NoteQuery, NoteFolderSelection, NoteFolderTreeVO } from '@/types/note'
import { estimateReadingTime, formatRelativeTime, isRecentlyEdited } from '@/utils/readingTime'
import { formatRelativeUpdated } from '@/utils/formatTime'
import ImportMarkdownDialog from './ImportMarkdownDialog.vue'
import NoteCardContextMenu, { type CardMenuEntry } from './NoteCardContextMenu.vue'
import NoteWorkspaceOverlay from './NoteWorkspaceOverlay.vue'
import KnowledgeSpaceNav from './KnowledgeSpaceNav.vue'
import NoteHome from './NoteHome.vue'
import { useDeepLinkDialog } from '@/composables/useDeepLinkDialog'
import { useMainContentFill } from '@/composables/useMainContentFill'
import { useFillPageSize } from '@/composables/useFillPageSize'
import { useProductViewMode } from '@/composables/useProductViewMode'
import { usePaginatedList } from '@/composables/usePaginatedList'
import { handleApiError, unwrapPage } from '@/utils/apiResult'
import { triggerBlobDownload } from '@/utils/file'
import { noteFolderDraggingKind } from './folderDragState'
import UiTooltip from '@/components/UiTooltip.vue'

const MAX_EXPORT = 50

type Workspace =
  | null
  | { mode: 'create'; folderId: number | null }
  | { mode: 'edit'; id: number }

const showImport = ref(false)
const selectMode = ref(false)
const selectedIds = ref<number[]>([])
const exporting = ref(false)
const cardMenuRef = ref<InstanceType<typeof NoteCardContextMenu> | null>(null)
const activeNote = ref<NoteVO | null>(null)
const workspace = ref<Workspace>(null)
const workspaceSessionKey = ref(0)
const folderSelection = ref<NoteFolderSelection>('home')
const folderDrawerOpen = ref(false)
const FOLDER_PANE_KEY = 'note-folder-pane-collapsed'
const folderPaneCollapsed = ref(localStorage.getItem(FOLDER_PANE_KEY) === '1')
const folderTreeData = ref<NoteFolderTreeVO | null>(null)
const isHome = computed(() => folderSelection.value === 'home')
const { viewMode, setViewMode } = useProductViewMode('note-view', 'card')
const selectedCount = computed(() => selectedIds.value.length)

function setFolderPaneCollapsed(v: boolean) {
  folderPaneCollapsed.value = v
  localStorage.setItem(FOLDER_PANE_KEY, v ? '1' : '0')
}

function toggleFolderPane() {
  // 移动端：抽屉；桌面：收起/展开窄条
  if (window.matchMedia('(max-width: 768px)').matches) {
    folderDrawerOpen.value = !folderDrawerOpen.value
    return
  }
  setFolderPaneCollapsed(!folderPaneCollapsed.value)
}

const createFolderId = computed((): number | null => {
  const sel = folderSelection.value
  if (typeof sel === 'number' && Number.isFinite(sel)) return sel
  return null
})

function folderQueryParam(sel: NoteFolderSelection): string | undefined {
  if (sel === 'home' || sel === 'all') return undefined
  if (sel === 'none') return 'none'
  return String(sel)
}

const { list, total, loading, query, fetchList, onSearch, onPageChange } = usePaginatedList<NoteVO, NoteQuery & { page: number; size: number }>({
  initialQuery: { page: 1, size: 10, keyword: '' },
  fetchPage: (q) => unwrapPage(getNoteList(q)),
  errorMessage: '加载笔记失败',
})

watch(folderSelection, (sel) => {
  folderDrawerOpen.value = false
  if (sel === 'home') return
  query.value.folderId = folderQueryParam(sel)
  query.value.page = 1
  fetchList()
})

useMainContentFill()

const { pageSize } = useFillPageSize((size) => {
  query.value.size = size
  if (isHome.value) return
  query.value.page = 1
  fetchList()
})

const folderTreeRef = ref<{ reload: () => Promise<void> } | null>(null)

function onFolderTreeLoaded(data: NoteFolderTreeVO) {
  folderTreeData.value = data
}

function onFolderChanged() {
  folderTreeRef.value?.reload()
  if (!isHome.value) fetchList()
}

function onImportDone() {
  showImport.value = false
  folderTreeRef.value?.reload()
  if (!isHome.value) fetchList()
}

const cardMenuEntries: CardMenuEntry[] = [
  { type: 'item', id: 'edit', label: '编辑' },
  { type: 'item', id: 'preview', label: '新标签页预览' },
  { type: 'separator' },
  { type: 'item', id: 'favorite', label: '收藏 / 取消收藏' },
  { type: 'item', id: 'export', label: '导出 Markdown' },
  { type: 'separator' },
  { type: 'item', id: 'archive', label: '归档' },
  { type: 'item', id: 'delete', label: '移入回收站', danger: true },
]

function openImport() {
  showImport.value = true
}
function openCreate() {
  workspaceSessionKey.value += 1
  workspace.value = { mode: 'create', folderId: createFolderId.value }
}
function openEdit(id: number) {
  workspaceSessionKey.value += 1
  workspace.value = { mode: 'edit', id }
}

function closeWorkspace() {
  workspace.value = null
  folderTreeRef.value?.reload()
  if (!isHome.value) fetchList()
}
function onWorkspaceNoteId(id: number) {
  if (workspace.value?.mode === 'create') {
    workspace.value = { mode: 'edit', id }
  }
}

useDeepLinkDialog({ openCreate, openEdit })

function goCreate() { openCreate() }
function goEdit(id: number) { openEdit(id) }
function goPreview(id: number) { window.open(`/notes/${id}/preview`, '_blank') }

const workspaceCreateFolderId = computed(() =>
  workspace.value?.mode === 'create' ? workspace.value.folderId : null,
)

function onNoteDragStart(e: DragEvent, noteId: number) {
  noteFolderDraggingKind.value = 'note'
  const payload = JSON.stringify({ type: 'note', id: noteId })
  e.dataTransfer?.setData('application/x-ph-note', payload)
  e.dataTransfer?.setData('application/x-ph-folder-drag', payload)
  e.dataTransfer?.setData('text/plain', payload)
  e.dataTransfer!.effectAllowed = 'move'
}

function onNoteDragEnd() {
  noteFolderDraggingKind.value = null
}

function toggleSelectMode() {
  selectMode.value = !selectMode.value
  if (!selectMode.value) selectedIds.value = []
}

function isSelected(id: number) {
  return selectedIds.value.includes(id)
}

function toggleSelect(id: number) {
  if (isSelected(id)) {
    selectedIds.value = selectedIds.value.filter((x) => x !== id)
  } else {
    if (selectedIds.value.length >= MAX_EXPORT) {
      ElMessage.warning(`单次最多导出 ${MAX_EXPORT} 篇`)
      return
    }
    selectedIds.value = [...selectedIds.value, id]
  }
}

function onRowClick(note: NoteVO) {
  if (selectMode.value) {
    toggleSelect(note.id)
    return
  }
  goEdit(note.id)
}

async function handleBatchExport() {
  if (exporting.value) return
  if (selectedIds.value.length === 0) {
    ElMessage.warning('请先勾选要导出的笔记')
    return
  }
  if (selectedIds.value.length > MAX_EXPORT) {
    ElMessage.warning(`单次最多导出 ${MAX_EXPORT} 篇`)
    return
  }
  exporting.value = true
  try {
    const res = await exportNotesBatch(selectedIds.value)
    const stamp = new Date()
    const pad = (n: number) => String(n).padStart(2, '0')
    const name = `notes-export-${stamp.getFullYear()}${pad(stamp.getMonth() + 1)}${pad(stamp.getDate())}-${pad(stamp.getHours())}${pad(stamp.getMinutes())}.zip`
    triggerBlobDownload(new Blob([res.data], { type: 'application/zip' }), name)
    ElMessage.success(`已导出 ${selectedIds.value.length} 篇笔记`)
  } catch (e) {
    handleApiError(e, '导出失败')
  } finally {
    exporting.value = false
  }
}

function notePreviewText(note: NoteVO): string {
  return note.excerpt || note.content || ''
}

function noteCardPreview(note: NoteVO): string {
  const text = note.excerpt
    || note.content?.replace(/#{1,6}\s/g, '').replace(/[*`~>]/g, '').slice(0, 120)
    || ''
  return text || '暂无内容'
}

function visibleTags(tags: NoteVO['tags']) {
  const shown = tags.slice(0, 2)
  const more = tags.length > 2 ? tags.length - 2 : 0
  return { shown, more }
}

async function handleDelete(id: number) {
  await ElMessageBox.confirm('确定将此笔记移入回收站？', '提示', { type: 'warning' })
  try {
    await deleteNote(id)
    ElMessage.success('已移入回收站')
    folderTreeRef.value?.reload()
    if (!isHome.value) fetchList()
  } catch (e) {
    handleApiError(e, '移入回收站失败')
  }
}

async function handleArchive(id: number) {
  await ElMessageBox.confirm('确定归档此笔记？归档后可在回收站恢复。', '归档', { type: 'info' })
  try {
    await archiveNote(id)
    ElMessage.success('已归档')
    folderTreeRef.value?.reload()
    if (!isHome.value) fetchList()
  } catch (e) {
    handleApiError(e, '归档失败')
  }
}

async function handleToggleFavorite(note: NoteVO) {
  await toggleFavorite(note.id)
  note.isFavorite = note.isFavorite ? 0 : 1
}

function onCardContextMenu(e: MouseEvent, note: NoteVO) {
  activeNote.value = note
  cardMenuRef.value?.openAt(e)
}

function handleExport(note: NoteVO) {
  const title = note.title || '无标题笔记'
  const content = `# ${title}\n\n${note.content || ''}`
  const blob = new Blob([content], { type: 'text/markdown;charset=utf-8' })
  const url = URL.createObjectURL(blob)
  const a = document.createElement('a')
  a.href = url
  a.download = `${title}.md`
  a.click()
  URL.revokeObjectURL(url)
}

async function onCardMenuAction(actionId: string) {
  const note = activeNote.value
  if (!note) return
  switch (actionId) {
    case 'edit':
      goEdit(note.id)
      break
    case 'preview':
      goPreview(note.id)
      break
    case 'favorite':
      await handleToggleFavorite(note)
      break
    case 'export':
      handleExport(note)
      break
    case 'archive':
      await handleArchive(note.id)
      break
    case 'delete':
      await handleDelete(note.id)
      break
  }
}
</script>

<template>
  <div class="plan-page">
    <div class="plan-top">
      <PageHeader title="笔记" />

      <ListToolbar
        v-if="!isHome"
        :search="query.keyword ?? ''"
        search-placeholder="搜索笔记标题或摘要..."
        search-width="240px"
        create-label="新建笔记"
        @update:search="query.keyword = $event"
        @search="onSearch"
        @create="goCreate"
      >
        <template #filters>
          <UiTooltip :content="folderPaneCollapsed ? '展开知识空间' : '知识空间'" placement="bottom">
            <button
              type="button"
              class="toolbar-btn folder-toggle-btn"
              :class="{ active: !folderPaneCollapsed }"
              @click="toggleFolderPane"
            >
              <FolderTree :size="14" /> 知识空间
            </button>
          </UiTooltip>
          <div class="view-toggle">
            <UiTooltip content="列表">
              <button type="button" class="view-btn" :class="{ active: viewMode === 'table' }" @click="setViewMode('table')">
                <LayoutList :size="15" />
              </button>
            </UiTooltip>
            <UiTooltip content="卡片">
              <button type="button" class="view-btn" :class="{ active: viewMode === 'card' }" @click="setViewMode('card')">
                <LayoutGrid :size="15" />
              </button>
            </UiTooltip>
          </div>
        </template>
        <template #actions>
          <button
            type="button"
            class="toolbar-btn"
            :class="{ active: selectMode }"
            @click="toggleSelectMode"
          >
            <CheckSquare :size="14" /> {{ selectMode ? '取消多选' : '多选' }}
          </button>
          <button
            v-if="selectMode"
            type="button"
            class="toolbar-btn"
            :disabled="selectedCount === 0 || exporting"
            @click="handleBatchExport"
          >
            <Download :size="14" /> 导出 ZIP{{ selectedCount ? ` (${selectedCount})` : '' }}
          </button>
          <button type="button" class="toolbar-btn" @click="openImport">
            <Upload :size="14" /> 导入
          </button>
        </template>
      </ListToolbar>

      <div v-else class="note-home-toolbar">
        <UiTooltip :content="folderPaneCollapsed ? '展开知识空间' : '知识空间'" placement="bottom">
          <button
            type="button"
            class="toolbar-btn folder-toggle-btn"
            :class="{ active: !folderPaneCollapsed }"
            @click="toggleFolderPane"
          >
            <FolderTree :size="14" /> 知识空间
          </button>
        </UiTooltip>
        <button type="button" class="toolbar-btn toolbar-btn--primary" @click="goCreate">
          <Plus :size="14" /> 新建笔记
        </button>
      </div>
    </div>

    <div class="plan-middle note-middle">
      <div
        class="folder-drawer-mask"
        :class="{ open: folderDrawerOpen }"
        @click="folderDrawerOpen = false"
      />
      <div
        class="folder-pane"
        :class="{ open: folderDrawerOpen, collapsed: folderPaneCollapsed }"
      >
        <KnowledgeSpaceNav
          v-show="!folderPaneCollapsed"
          ref="folderTreeRef"
          v-model="folderSelection"
          @changed="onFolderChanged"
          @loaded="onFolderTreeLoaded"
          @open-note="openEdit"
          @collapse="setFolderPaneCollapsed(true)"
        />
        <div v-if="folderPaneCollapsed" class="folder-pane-rail">
          <UiTooltip content="展开知识空间" placement="right">
            <button type="button" class="folder-pane-expand" @click="setFolderPaneCollapsed(false)">
              <PanelLeft :size="16" />
            </button>
          </UiTooltip>
        </div>
      </div>

      <div class="note-list-pane">
      <NoteHome
        v-if="isHome"
        :tree="folderTreeData"
        @open-note="openEdit"
        @select-folder="folderSelection = $event"
        @changed="folderTreeRef?.reload()"
      />

      <template v-else>
      <div v-if="loading && viewMode === 'card'" class="card-grid-skeleton">
        <div v-for="i in pageSize" :key="i" class="skeleton-note-card" />
      </div>

      <div v-else-if="loading" class="table-skeleton">
        <div v-for="i in pageSize" :key="i" class="skeleton-row" />
      </div>

      <EmptyState v-else-if="list.length === 0" :icon="FileText" illustration="note" text="还没有笔记，开始写第一篇吧" action-label="新建笔记" :action-icon="Plus" @action="goCreate" />

      <div v-else-if="viewMode === 'table'" class="product-table">
        <div class="pt-head">
          <div v-if="selectMode" class="col-check" />
          <div class="col-title">标题</div>
          <div class="col-cat">分类</div>
          <div class="col-tags">标签</div>
          <div class="col-updated">更新</div>
          <div class="col-actions" />
        </div>
        <div class="pt-body" :style="{ gridTemplateRows: `repeat(${pageSize}, minmax(0, 1fr))` }">
          <div
            v-for="note in list"
            :key="note.id"
            class="pt-row"
            :class="{ 'pt-row--selected': selectMode && isSelected(note.id) }"
            draggable="true"
            @dragstart="onNoteDragStart($event, note.id)"
            @dragend="onNoteDragEnd"
            @click="onRowClick(note)"
          >
            <div v-if="selectMode" class="col-check" @click.stop>
              <input type="checkbox" :checked="isSelected(note.id)" @change="toggleSelect(note.id)" />
            </div>
            <div class="col-title">
              <div class="name-title">{{ note.title || '无标题笔记' }}</div>
            </div>
            <div class="col-cat">
              <template v-if="note.categories?.length">
                <span v-for="c in note.categories" :key="c.id" class="soft-tag">{{ c.name }}</span>
              </template>
              <span v-else class="muted">—</span>
            </div>
            <div class="col-tags">
              <template v-if="note.tags?.length">
                <span
                  v-for="t in visibleTags(note.tags).shown"
                  :key="t.id"
                  class="soft-tag soft-tag--tag"
                  :style="t.color ? { background: t.color + '20', color: t.color } : undefined"
                >{{ t.name }}</span>
                <span v-if="visibleTags(note.tags).more" class="tag-more">+{{ visibleTags(note.tags).more }}</span>
              </template>
              <span v-else class="muted">—</span>
            </div>
            <div class="col-updated cell-date">{{ formatRelativeUpdated(note.updatedAt) }}</div>
            <div class="col-actions" @click.stop>
              <UiTooltip content="预览">
                <button type="button" class="icon-action" @click="goPreview(note.id)">
                  <Eye :size="15" />
                </button>
              </UiTooltip>
              <UiTooltip content="移入回收站">
                <button type="button" class="icon-action icon-action--danger" @click="handleDelete(note.id)">
                  <Trash2 :size="15" />
                </button>
              </UiTooltip>
            </div>
          </div>
          <div
            v-for="n in Math.max(0, pageSize - list.length)"
            :key="'pad-' + n"
            class="pt-row pt-row--pad"
            aria-hidden="true"
          />
        </div>
      </div>

      <div v-else class="note-grid">
        <div
          v-for="note in list"
          :key="note.id"
          class="note-card"
          :class="{ 'note-card--selected': selectMode && isSelected(note.id) }"
          draggable="true"
          @dragstart="onNoteDragStart($event, note.id)"
          @dragend="onNoteDragEnd"
          @click="onRowClick(note)"
          @contextmenu="onCardContextMenu($event, note)"
        >
          <div v-if="selectMode" class="note-card-check" @click.stop>
            <input type="checkbox" :checked="isSelected(note.id)" @change="toggleSelect(note.id)" />
          </div>
          <div class="note-card-header">
            <div class="note-card-title-row">
              <FileText :size="14" class="note-type-icon" />
              <span class="note-card-title">{{ note.title }}</span>
            </div>
            <button class="fav-btn" :class="{ favored: note.isFavorite === 1 }" @click.stop="handleToggleFavorite(note)">
              <Star :size="14" :fill="note.isFavorite === 1 ? 'currentColor' : 'none'" />
            </button>
          </div>
          <div class="note-card-meta">
            <span v-for="cat in note.categories" :key="cat.id" class="meta-tag">{{ cat.name }}</span>
            <span class="reading-time"><Clock :size="11" /> {{ estimateReadingTime(notePreviewText(note)) }}</span>
          </div>
          <div class="note-card-body">
            <p class="note-card-preview">{{ noteCardPreview(note) }}</p>
          </div>
          <div class="note-card-footer">
            <div class="note-card-tags">
              <span v-for="tag in note.tags.slice(0, 3)" :key="tag.id" class="meta-tag meta-tag--tag" :style="tag.color ? { background: tag.color + '20', color: tag.color } : {}">{{ tag.name }}</span>
              <span v-if="note.tags.length > 3" class="meta-tag meta-tag--more">+{{ note.tags.length - 3 }}</span>
            </div>
            <div class="note-card-footer-right">
              <UiTooltip v-if="isRecentlyEdited(note.updatedAt)" content="最近编辑">
                <span class="edited-dot" />
              </UiTooltip>
              <span class="edited-time">{{ formatRelativeTime(note.updatedAt) }}</span>
              <UiTooltip content="预览">
                <button class="icon-btn" @click.stop="goPreview(note.id)">
                  <Eye :size="14" />
                </button>
              </UiTooltip>
              <button class="delete-btn" @click.stop="handleDelete(note.id)">
                <Trash2 :size="14" />
              </button>
            </div>
          </div>
        </div>
        <div
          v-for="n in Math.max(0, pageSize - list.length)"
          :key="'pad-' + n"
          class="note-card note-card--pad"
          aria-hidden="true"
        />
      </div>
      </template>
      </div>
    </div>

    <div class="plan-foot">
      <ListPagination v-if="!isHome && total > 0" :total="total" :page="query.page ?? 1" :size="pageSize" @update:page="onPageChange" />
    </div>

    <NoteCardContextMenu
      ref="cardMenuRef"
      :entries="cardMenuEntries"
      @action="onCardMenuAction"
    />
  </div>

  <el-dialog
    v-model="showImport"
    width="800px"
    :close-on-click-modal="false"
    class="note-import-dialog"
  >
    <ImportMarkdownDialog :folder-id="createFolderId" @done="onImportDone" />
  </el-dialog>

  <NoteWorkspaceOverlay
    v-if="workspace"
    :key="workspaceSessionKey"
    :note-id="workspace.mode === 'edit' ? workspace.id : undefined"
    :folder-id="workspaceCreateFolderId"
    @close="closeWorkspace"
    @note-id="onWorkspaceNoteId"
  />
</template>

<style scoped>
.plan-page {
  width: 100%;
  height: 100%;
  min-height: 0;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}
.plan-top { flex-shrink: 0; }
.plan-middle {
  flex: 1;
  min-height: 0;
  overflow: hidden;
  display: flex;
  flex-direction: column;
}
.note-middle {
  flex-direction: row;
  position: relative;
}
.note-home-toolbar {
  display: flex;
  align-items: center;
  justify-content: flex-end;
  gap: 8px;
  padding: 0 0 8px;
}
.note-home-toolbar .toolbar-btn--primary {
  background: var(--accent);
  color: #fff;
  border-color: transparent;
}
.note-home-toolbar .toolbar-btn--primary:hover {
  filter: brightness(1.05);
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
.note-list-pane {
  flex: 1;
  min-width: 0;
  min-height: 0;
  overflow: hidden;
  display: flex;
  flex-direction: column;
}
.folder-drawer-mask {
  display: none;
}
.folder-toggle-btn {
  display: inline-flex;
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
.plan-foot {
  flex-shrink: 0;
  padding-top: 8px;
}

.note-grid {
  flex: 1;
  min-height: 0;
  display: grid;
  grid-template-columns: repeat(5, minmax(0, 1fr));
  grid-auto-rows: 1fr;
  gap: var(--sp-4);
}
@media (max-width: 1100px) {
  .note-grid { grid-template-columns: repeat(4, minmax(0, 1fr)); }
}
@media (max-width: 720px) {
  .note-grid { grid-template-columns: repeat(2, minmax(0, 1fr)); }
}
.card-grid-skeleton {
  flex: 1;
  display: grid;
  grid-template-columns: repeat(5, minmax(0, 1fr));
  grid-auto-rows: 1fr;
  gap: var(--sp-4);
}
.skeleton-note-card { border-radius: var(--radius-lg); background: var(--bg-hover); animation: pulse 1.5s ease-in-out infinite; }

.table-skeleton {
  flex: 1;
  min-height: 0;
  display: flex;
  flex-direction: column;
  gap: 8px;
}
.table-skeleton .skeleton-row {
  flex: 1;
  border-radius: var(--radius-md);
  background: var(--bg-hover);
  animation: pulse 1.5s ease-in-out infinite;
}

.note-card {
  position: relative;
  background: var(--bg-card); border: 1px solid var(--border-color); border-radius: var(--radius-lg);
  padding: var(--sp-4) var(--sp-5); cursor: pointer; height: 100%; min-height: 0;
  transition: all var(--transition); display: flex; flex-direction: column;
}
.note-card--pad {
  visibility: hidden;
  pointer-events: none;
  border: none;
  background: transparent;
}
.note-card:hover { box-shadow: var(--shadow-md); border-color: var(--accent-border); }
.note-card-header { display: flex; justify-content: space-between; align-items: flex-start; margin-bottom: var(--sp-2); }
.note-card-title-row { display: flex; align-items: center; gap: var(--sp-2); min-width: 0; }
.note-type-icon { color: var(--accent); opacity: 0.6; flex-shrink: 0; margin-top: 2px; }
.note-card-title {
  font-size: var(--text-base); font-weight: 600; line-height: var(--leading-tight);
  display: -webkit-box; -webkit-line-clamp: 2; -webkit-box-orient: vertical; overflow: hidden;
}
.fav-btn { background: none; border: none; cursor: pointer; color: var(--text-tertiary); padding: 4px; border-radius: var(--radius-sm); flex-shrink: 0; transition: all var(--transition); }
.fav-btn:hover { color: var(--warning); background: var(--warning-light); transform: scale(1.15); }
.fav-btn:active { transform: scale(0.85); }
.fav-btn.favored { color: var(--warning); animation: fav-pop 350ms ease; }
@keyframes fav-pop {
  0% { transform: scale(1); }
  40% { transform: scale(1.3); }
  100% { transform: scale(1); }
}
.note-card-meta { display: flex; gap: var(--sp-2); margin-bottom: var(--sp-3); flex-wrap: wrap; align-items: center; }
.reading-time { display: inline-flex; align-items: center; gap: 3px; font-size: 11px; color: var(--text-tertiary); margin-left: auto; }
.meta-tag { display: inline-block; padding: 0 6px; height: 20px; line-height: 20px; border-radius: var(--radius-sm); font-size: 11px; background: var(--accent-light); color: var(--accent); }
.meta-tag--tag { background: var(--bg-hover); color: var(--text-tertiary); }
.meta-tag--more { background: transparent; color: var(--text-tertiary); }
.note-card-body { flex: 1; margin-bottom: var(--sp-3); min-height: 0; overflow: hidden; }
.note-card-preview { font-size: var(--text-sm); color: var(--text-secondary); line-height: var(--leading-relaxed); display: -webkit-box; -webkit-line-clamp: 3; -webkit-box-orient: vertical; overflow: hidden; }
.note-card-footer { display: flex; justify-content: space-between; align-items: center; padding-top: var(--sp-2); border-top: 1px solid var(--border-light); }
.note-card-tags { display: flex; gap: 4px; flex-wrap: wrap; }
.note-card-footer-right { display: flex; align-items: center; gap: var(--sp-2); flex-shrink: 0; }
.edited-dot { width: 6px; height: 6px; border-radius: 50%; background: var(--success); flex-shrink: 0; }
.edited-time { font-size: 11px; color: var(--text-tertiary); white-space: nowrap; }
.delete-btn { background: none; border: none; color: var(--text-tertiary); cursor: pointer; padding: 4px; border-radius: var(--radius-sm); transition: all var(--transition); }
.delete-btn:hover { color: var(--danger); background: var(--danger-light); }
.icon-btn { background: none; border: none; color: var(--text-tertiary); cursor: pointer; padding: 4px; border-radius: var(--radius-sm); transition: all var(--transition); display: inline-flex; align-items: center; }
.icon-btn:hover { color: var(--accent); background: color-mix(in srgb, var(--accent) 10%, transparent); }
.note-card--selected {
  border-color: var(--accent);
  background: color-mix(in srgb, var(--accent) 6%, var(--bg-card));
}
.note-card-check {
  position: absolute;
  top: 10px;
  left: 10px;
  z-index: 1;
}
.col-check {
  display: flex;
  align-items: center;
  justify-content: center;
}
.pt-row--selected {
  background: color-mix(in srgb, var(--accent) 6%, transparent);
}

/* 本页列宽（共享壳见 styles/product-list.css） */
.pt-head, .pt-row {
  grid-template-columns:
    minmax(160px, 2fr)
    minmax(100px, 1fr)
    minmax(120px, 1.2fr)
    120px
    72px;
}
.plan-page:has(.col-check) .pt-head,
.plan-page:has(.col-check) .pt-row {
  grid-template-columns:
    36px
    minmax(160px, 2fr)
    minmax(100px, 1fr)
    minmax(120px, 1.2fr)
    120px
    72px;
}
.soft-tag {
  display: inline-flex;
  align-items: center;
  padding: 1px 7px;
  border-radius: var(--radius-sm);
  font-size: 12px;
  background: var(--accent-light);
  color: var(--accent);
  margin-right: 4px;
}
.soft-tag--tag {
  background: var(--bg-hover);
  color: var(--text-tertiary);
}
.tag-more {
  font-size: 12px;
  color: var(--text-tertiary);
}
.col-actions {
  gap: 2px;
}
.icon-action {
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
  transition: background var(--transition-duration) ease, color var(--transition-duration) ease;
}
.icon-action:hover {
  background: var(--bg-hover);
  color: var(--text-primary);
}
.icon-action--danger:hover {
  color: var(--danger);
  background: var(--danger-light);
}
</style>

<style>
/* el-dialog 挂到 body，需非 scoped */
.note-import-dialog.el-dialog {
  max-width: calc(100vw - 32px);
}
</style>
