<script setup lang="ts">
import { ref } from 'vue'
import { getRecycleList, restoreNote, permanentDeleteNote } from '@/modules/knowledge/api'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Trash2, Eye, RotateCcw, AlertTriangle } from 'lucide-vue-next'
import type { NoteVO } from '@/types/note'
import PageHeader from '@/components/PageHeader.vue'
import EmptyState from '@/components/EmptyState.vue'
import ListToolbar from '@/components/ListToolbar.vue'
import ListPagination from '@/components/ListPagination.vue'
import NoteCardContextMenu, { type CardMenuEntry } from './NoteCardContextMenu.vue'
import { useMainContentFill } from '@/composables/useMainContentFill'
import { useFillPageSize } from '@/composables/useFillPageSize'
import { formatUpdated } from '@/utils/formatTime'

const loading = ref(false)
const total = ref(0)
const list = ref<NoteVO[]>([])
const keyword = ref('')
const page = ref(1)
const cardMenuRef = ref<InstanceType<typeof NoteCardContextMenu> | null>(null)
const activeNote = ref<NoteVO | null>(null)

useMainContentFill()
const { pageSize } = useFillPageSize((size) => {
  page.value = 1
  fetchList(size)
})

const cardMenuEntries: CardMenuEntry[] = [
  { type: 'item', id: 'preview', label: '预览' },
  { type: 'item', id: 'restore', label: '恢复' },
  { type: 'separator' },
  { type: 'item', id: 'delete', label: '永久删除', danger: true },
]

async function fetchList(size = pageSize.value) {
  loading.value = true
  try {
    const res = await getRecycleList({ page: page.value, size, keyword: keyword.value || undefined })
    list.value = res.data.data.records
    total.value = res.data.data.total
  } catch {
    list.value = []
    total.value = 0
  } finally {
    loading.value = false
  }
}

function onSearch() {
  page.value = 1
  fetchList()
}

function onPageChange(p: number) {
  page.value = p
  fetchList()
}

async function handleRestore(id: number) {
  try {
    await restoreNote(id)
    ElMessage.success('已恢复')
    fetchList()
  } catch {
    // 业务异常已由全局拦截器处理
  }
}

async function handlePermanentDelete(id: number) {
  try {
    await ElMessageBox.confirm('永久删除后笔记内容和关联资源将被彻底清除，不可恢复。确定？', '确认删除', {
      type: 'error',
      confirmButtonText: '确定删除',
      cancelButtonText: '取消'
    })
    await permanentDeleteNote(id)
    ElMessage.success('已永久删除')
    fetchList()
  } catch {
    // 取消弹窗或异常都不处理
  }
}

function handlePreview(id: number) {
  window.open(`/notes/${id}/preview`, '_blank')
}

function onCardContextMenu(e: MouseEvent, note: NoteVO) {
  activeNote.value = note
  cardMenuRef.value?.openAt(e)
}

async function onCardMenuAction(actionId: string) {
  const note = activeNote.value
  if (!note) return
  switch (actionId) {
    case 'preview':
      handlePreview(note.id)
      break
    case 'restore':
      await handleRestore(note.id)
      break
    case 'delete':
      await handlePermanentDelete(note.id)
      break
  }
}

/** 回收站内再标「已删」冗余，展示时去掉前缀 */
function displayTitle(title?: string | null): string {
  if (!title) return '未命名笔记'
  return title.replace(/^\[已删\]\s*/, '')
}

function deleteReasonLabel(reason?: string | null): string {
  if (reason === 'USER_DELETE') return '用户删除'
  if (reason === 'AUTO_ARCHIVE') return '归档'
  return reason || ''
}
</script>

<template>
  <div class="plan-page">
    <div class="plan-top">
      <PageHeader title="回收站" />

      <ListToolbar
        v-model:search="keyword"
        search-placeholder="搜索已删除的笔记..."
        @search="onSearch"
      />
    </div>

    <div class="plan-middle">
      <div v-if="loading" class="list-skeleton" :style="{ gridTemplateRows: `repeat(${pageSize}, minmax(4.75rem, 1fr))` }">
        <div v-for="i in pageSize" :key="i" class="skeleton-row" />
      </div>

      <EmptyState
        v-else-if="list.length === 0"
        :icon="Trash2"
        illustration="default"
        :text="keyword ? '未找到匹配的笔记' : '回收站为空'"
      />

      <div v-else class="recycle-list" :style="{ gridTemplateRows: `repeat(${pageSize}, minmax(4.75rem, 1fr))` }">
        <div
          v-for="note in list"
          :key="note.id"
          class="recycle-card"
          @contextmenu="onCardContextMenu($event, note)"
        >
          <div class="recycle-card__main">
            <div class="recycle-card__title">{{ displayTitle(note.title) }}</div>
            <div class="recycle-card__meta">
              <span v-if="note.categories?.length" class="meta-item">
                <span class="meta-label">分类</span>
                <span class="meta-tags">
                  <span v-for="c in note.categories" :key="c.id" class="tag-chip">{{ c.name }}</span>
                </span>
              </span>
              <span v-if="note.tags?.length" class="meta-item">
                <span class="meta-label">标签</span>
                <span class="meta-tags">
                  <span v-for="t in note.tags" :key="t.id" class="tag-chip" :style="t.color ? { '--tag-color': t.color } : undefined">{{ t.name }}</span>
                </span>
              </span>
              <span v-if="note.deleteReason" class="meta-item">
                <span class="meta-label">原因</span>
                <span class="delete-reason">{{ deleteReasonLabel(note.deleteReason) }}</span>
              </span>
            </div>
            <div class="recycle-card__times">
              <span class="time-item">创建 {{ formatUpdated(note.createdAt, '-') }}</span>
              <span class="time-divider" aria-hidden="true">·</span>
              <span class="time-item">更新 {{ formatUpdated(note.updatedAt, '-') }}</span>
              <span class="time-divider" aria-hidden="true">·</span>
              <span class="time-item time-item--danger">删除 {{ formatUpdated(note.deletedAt, '-') }}</span>
            </div>
          </div>
          <div class="recycle-card__actions">
            <el-tooltip content="预览">
              <button type="button" class="icon-action" @click="handlePreview(note.id)">
                <Eye :size="15" />
              </button>
            </el-tooltip>
            <el-tooltip content="恢复">
              <button type="button" class="icon-action" @click="handleRestore(note.id)">
                <RotateCcw :size="15" />
              </button>
            </el-tooltip>
            <el-tooltip content="永久删除">
              <button type="button" class="icon-action icon-action--danger" @click="handlePermanentDelete(note.id)">
                <AlertTriangle :size="15" />
              </button>
            </el-tooltip>
          </div>
        </div>
        <div
          v-for="n in Math.max(0, pageSize - list.length)"
          :key="'pad-' + n"
          class="recycle-card recycle-card--pad"
          aria-hidden="true"
        />
      </div>
    </div>

    <div class="plan-foot">
      <ListPagination
        v-if="total > 0"
        :total="total"
        :page="page"
        :size="pageSize"
        @update:page="onPageChange"
      />
    </div>

    <NoteCardContextMenu
      ref="cardMenuRef"
      :entries="cardMenuEntries"
      @action="onCardMenuAction"
    />
  </div>
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

.list-skeleton {
  flex: 1;
  min-height: 0;
  display: grid;
  gap: var(--sp-2);
}
.skeleton-row {
  border-radius: var(--radius-md);
  background: var(--bg-hover);
  animation: pulse 1.5s ease-in-out infinite;
}

.recycle-list {
  flex: 1;
  min-height: 0;
  display: grid;
  gap: var(--sp-2);
}

.recycle-card {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: var(--sp-4);
  padding: var(--sp-3) var(--sp-4);
  background: var(--bg-card);
  border: 1px solid var(--border-color);
  border-radius: var(--radius-md);
  transition: box-shadow var(--transition);
  min-height: 0;
  overflow: hidden;
}

.recycle-card--pad {
  visibility: hidden;
  pointer-events: none;
  border: none;
  background: transparent;
}

.recycle-card:hover {
  box-shadow: var(--shadow-sm);
}

.recycle-card__main {
  flex: 1 1 auto;
  min-width: 0;
  display: flex;
  flex-direction: column;
  justify-content: center;
  gap: 4px;
}

.recycle-card__title {
  flex-shrink: 0;
  font-size: var(--text-sm);
  font-weight: 600;
  color: var(--text-primary);
  line-height: 1.35;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.recycle-card__meta {
  flex-shrink: 0;
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: var(--sp-2) var(--sp-3);
  font-size: var(--text-xs);
  line-height: 1.4;
}

.meta-item {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  min-width: 0;
}

.meta-label {
  flex-shrink: 0;
  color: var(--text-tertiary);
}

.meta-tags {
  display: inline-flex;
  gap: 4px;
  flex-wrap: wrap;
  min-width: 0;
}

.tag-chip {
  display: inline-block;
  padding: 0 6px;
  line-height: 1.5;
  font-size: var(--text-xs);
  background: color-mix(in srgb, var(--accent-color, var(--accent)) 10%, transparent);
  color: var(--accent-color, var(--accent));
  border-radius: var(--radius-sm);
}

.tag-chip[style] {
  background: color-mix(in srgb, var(--tag-color) 15%, transparent);
  color: var(--tag-color);
}

.delete-reason {
  display: inline-block;
  padding: 0 6px;
  line-height: 1.5;
  font-size: var(--text-xs);
  background: color-mix(in srgb, var(--text-tertiary) 10%, transparent);
  color: var(--text-tertiary);
  border-radius: var(--radius-sm);
}

.recycle-card__times {
  flex-shrink: 0;
  display: flex;
  align-items: center;
  gap: var(--sp-2);
  min-width: 0;
  max-width: 100%;
  font-size: var(--text-xs);
  line-height: 1.4;
  color: var(--text-tertiary);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.time-item {
  flex-shrink: 0;
  white-space: nowrap;
}

.time-item--danger {
  color: var(--danger);
  font-weight: 500;
}

.time-divider {
  flex-shrink: 0;
  color: var(--border-color);
}

.recycle-card__actions {
  display: flex;
  align-items: center;
  gap: var(--sp-1);
  flex-shrink: 0;
  margin-left: var(--sp-4);
  opacity: 0;
  transition: opacity var(--transition);
}

.recycle-card:hover .recycle-card__actions {
  opacity: 1;
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
