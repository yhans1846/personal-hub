<script setup lang="ts">
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { getNoteList, deleteNote, toggleFavorite } from '@/modules/knowledge/api'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, FileText, Star, Trash2, Clock, Eye, Upload } from 'lucide-vue-next'
import { EmptyState, PageHeader, ListToolbar, ListPagination } from '@/components'
import type { NoteVO, NoteQuery } from '@/types/note'
import { estimateReadingTime, formatRelativeTime, isRecentlyEdited } from '@/utils/readingTime'
import ImportMarkdownDialog from './ImportMarkdownDialog.vue'
import NoteCardContextMenu, { type CardMenuEntry } from './NoteCardContextMenu.vue'
import { useMainContentFill } from '@/composables/useMainContentFill'
import { useFillPageSize } from '@/composables/useFillPageSize'

const router = useRouter()
const list = ref<NoteVO[]>([])
const total = ref(0)
const loading = ref(false)
const query = ref<NoteQuery>({ page: 1, size: 10, keyword: '' })
const showImport = ref(false)
const cardMenuRef = ref<InstanceType<typeof NoteCardContextMenu> | null>(null)
const activeNote = ref<NoteVO | null>(null)

useMainContentFill()

const { pageSize } = useFillPageSize((size) => {
  query.value.size = size
  query.value.page = 1
  fetchList()
})

const cardMenuEntries: CardMenuEntry[] = [
  { type: 'item', id: 'edit', label: '编辑' },
  { type: 'item', id: 'preview', label: '新标签页预览' },
  { type: 'separator' },
  { type: 'item', id: 'favorite', label: '收藏 / 取消收藏' },
  { type: 'item', id: 'export', label: '导出 Markdown' },
  { type: 'separator' },
  { type: 'item', id: 'delete', label: '移入回收站', danger: true },
]

function openImport() {
  showImport.value = true
}

async function fetchList() {
  loading.value = true
  try {
    const res = await getNoteList(query.value)
    list.value = res.data.data.records
    total.value = res.data.data.total
  } finally {
    loading.value = false
  }
}

function onSearch() { query.value.page = 1; fetchList() }
function onPageChange(page: number) { query.value.page = page; fetchList() }
function goCreate() { router.push('/notes/new') }
function goEdit(id: number) { router.push(`/notes/${id}/edit`) }
function goPreview(id: number) { window.open(`/notes/${id}/preview`, '_blank') }

function notePreviewText(note: NoteVO): string {
  return note.excerpt || note.content || ''
}

function noteCardPreview(note: NoteVO): string {
  const text = note.excerpt
    || note.content?.replace(/#{1,6}\s/g, '').replace(/[*`~>]/g, '').slice(0, 120)
    || ''
  return text || '暂无内容'
}

async function handleDelete(id: number) {
  await ElMessageBox.confirm('确定将此笔记移入回收站？', '提示', { type: 'warning' })
  await deleteNote(id)
  ElMessage.success('已移入回收站')
  fetchList()
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
    case 'delete':
      await handleDelete(note.id)
      break
  }
}
</script>

<template>
  <div class="plan-page">
    <div class="plan-top">
      <PageHeader title="笔记" subtitle="记录想法与知识" />

      <ListToolbar :search="query.keyword ?? ''" search-placeholder="搜索笔记标题..." search-width="240px" create-label="新建笔记" @update:search="query.keyword = $event" @search="onSearch" @create="goCreate">
        <template #actions>
          <button class="toolbar-import-btn" @click="openImport">
            <Upload :size="14" /> 导入
          </button>
        </template>
      </ListToolbar>
    </div>

    <div class="plan-middle">
      <div v-if="loading" class="card-grid-skeleton">
        <div v-for="i in pageSize" :key="i" class="skeleton-note-card" />
      </div>

      <EmptyState v-else-if="list.length === 0" :icon="FileText" illustration="note" text="还没有笔记，开始写第一篇吧" action-label="新建笔记" :action-icon="Plus" @action="goCreate" />

      <div v-else class="note-grid">
        <div
          v-for="note in list"
          :key="note.id"
          class="note-card"
          @click="goEdit(note.id)"
          @contextmenu="onCardContextMenu($event, note)"
        >
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
              <span v-if="isRecentlyEdited(note.updatedAt)" class="edited-dot" title="最近编辑" />
              <span class="edited-time">{{ formatRelativeTime(note.updatedAt) }}</span>
              <button class="icon-btn" title="预览" @click.stop="goPreview(note.id)">
                <Eye :size="14" />
              </button>
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
    </div>

    <div class="plan-foot">
      <ListPagination v-if="total > 0" :total="total" :page="query.page ?? 1" :size="pageSize" @update:page="onPageChange" />
    </div>

    <NoteCardContextMenu
      ref="cardMenuRef"
      :entries="cardMenuEntries"
      @action="onCardMenuAction"
    />
  </div>

  <el-dialog
    v-model="showImport"
    width="520px"
    :close-on-click-modal="false"
  >
    <ImportMarkdownDialog @done="showImport = false; fetchList()" />
  </el-dialog>
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

.note-card {
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
.fav-btn { background: none; border: none; cursor: pointer; color: var(--text-tertiary); padding: 4px; border-radius: 4px; flex-shrink: 0; transition: all 250ms cubic-bezier(0.4, 0, 0.2, 1); }
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
.meta-tag { display: inline-block; padding: 0 6px; height: 20px; line-height: 20px; border-radius: 4px; font-size: 11px; background: var(--accent-light); color: var(--accent); }
.meta-tag--tag { background: var(--bg-hover); color: var(--text-tertiary); }
.meta-tag--more { background: transparent; color: var(--text-tertiary); }
.note-card-body { flex: 1; margin-bottom: var(--sp-3); min-height: 0; overflow: hidden; }
.note-card-preview { font-size: var(--text-sm); color: var(--text-secondary); line-height: var(--leading-relaxed); display: -webkit-box; -webkit-line-clamp: 3; -webkit-box-orient: vertical; overflow: hidden; }
.note-card-footer { display: flex; justify-content: space-between; align-items: center; padding-top: var(--sp-2); border-top: 1px solid var(--border-light); }
.note-card-tags { display: flex; gap: 4px; flex-wrap: wrap; }
.note-card-footer-right { display: flex; align-items: center; gap: var(--sp-2); flex-shrink: 0; }
.edited-dot { width: 6px; height: 6px; border-radius: 50%; background: var(--success); flex-shrink: 0; }
.edited-time { font-size: 11px; color: var(--text-tertiary); white-space: nowrap; }
.delete-btn { background: none; border: none; color: var(--text-tertiary); cursor: pointer; padding: 4px; border-radius: 4px; transition: all var(--transition); }
.delete-btn:hover { color: var(--danger); background: var(--danger-light); }
.icon-btn { background: none; border: none; color: var(--text-tertiary); cursor: pointer; padding: 4px; border-radius: 4px; transition: all var(--transition); display: inline-flex; align-items: center; }
.icon-btn:hover { color: var(--accent); background: color-mix(in srgb, var(--accent) 10%, transparent); }
.toolbar-import-btn {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  height: 32px;
  padding: 0 12px;
  border: 1px solid var(--border-color);
  border-radius: var(--radius-sm);
  background: var(--bg-card);
  color: var(--text-secondary);
  font-size: var(--text-sm);
  cursor: pointer;
  transition: all var(--transition);
  white-space: nowrap;
}
.toolbar-import-btn:hover {
  border-color: var(--accent);
  color: var(--accent);
  background: var(--accent-light);
}
</style>
