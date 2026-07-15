<script setup lang="ts">
import { ref, onMounted, watch } from 'vue'
import { getRecycleList, restoreNote, permanentDeleteNote } from '@/modules/knowledge/api'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Trash2, Eye, RotateCcw, AlertTriangle } from 'lucide-vue-next'
import type { NoteVO } from '@/types/note'
import PageHeader from '@/components/PageHeader.vue'
import EmptyState from '@/components/EmptyState.vue'
import ListToolbar from '@/components/ListToolbar.vue'
import ListPagination from '@/components/ListPagination.vue'
import NoteCardContextMenu, { type CardMenuEntry } from './NoteCardContextMenu.vue'

const loading = ref(false)
const total = ref(0)
const list = ref<any[]>([])
const page = ref(1)
const size = 20
const keyword = ref('')
const cardMenuRef = ref<InstanceType<typeof NoteCardContextMenu> | null>(null)
const activeNote = ref<NoteVO | null>(null)

const cardMenuEntries: CardMenuEntry[] = [
  { type: 'item', id: 'preview', label: '预览' },
  { type: 'item', id: 'restore', label: '恢复' },
  { type: 'separator' },
  { type: 'item', id: 'delete', label: '永久删除', danger: true },
]

onMounted(() => fetchList())

watch(page, () => fetchList())

function formatTime(dateStr?: string): string {
  if (!dateStr) return '-'
  return dateStr.slice(0, 16).replace('T', ' ')
}

async function fetchList() {
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
</script>

<template>
  <div>
    <PageHeader title="回收站" subtitle="已删除的笔记可以在此恢复或永久清除" />

    <ListToolbar
      v-model:search="keyword"
      search-placeholder="搜索已删除的笔记..."
      @search="onSearch"
    />

    <div v-if="loading" class="loading-state">加载中...</div>

    <EmptyState
      v-else-if="list.length === 0"
      :icon="Trash2"
      illustration="default"
      :text="keyword ? '未找到匹配的笔记' : '回收站为空'"
    />

    <div v-else class="recycle-list">
      <div
        v-for="note in list"
        :key="note.id"
        class="recycle-card"
        @contextmenu="onCardContextMenu($event, note)"
      >
        <div class="recycle-card__main">
          <div class="recycle-card__title">{{ note.title }}</div>
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
            <span class="meta-item" v-if="note.deleteReason">
              <span class="meta-label">原因</span>
              <span class="delete-reason">{{ note.deleteReason === 'USER_DELETE' ? '用户删除' : note.deleteReason }}</span>
            </span>
          </div>
          <div class="recycle-card__times">
            <span class="time-item">创建 {{ formatTime(note.createdAt) }}</span>
            <span class="time-divider">|</span>
            <span class="time-item">更新 {{ formatTime(note.updatedAt) }}</span>
            <span class="time-divider">|</span>
            <span class="time-item time-item--danger">删除 {{ formatTime(note.deletedAt) }}</span>
          </div>
        </div>
        <div class="recycle-card__actions">
          <el-tooltip content="查看">
            <el-button size="small" text @click="handlePreview(note.id)">
              <Eye :size="15" />
            </el-button>
          </el-tooltip>
          <el-tooltip content="恢复">
            <el-button size="small" text @click="handleRestore(note.id)">
              <RotateCcw :size="15" />
            </el-button>
          </el-tooltip>
          <el-tooltip content="永久删除">
            <el-button size="small" text type="danger" @click="handlePermanentDelete(note.id)">
              <AlertTriangle :size="15" />
            </el-button>
          </el-tooltip>
        </div>
      </div>
    </div>

    <ListPagination
      :total="total"
      :page="page"
      :size="size"
      @update:page="page = $event"
    />

    <NoteCardContextMenu
      ref="cardMenuRef"
      :entries="cardMenuEntries"
      @action="onCardMenuAction"
    />
  </div>
</template>

<style scoped>
.loading-state {
  padding: 48px 0;
  text-align: center;
  color: var(--text-tertiary);
}

.recycle-list {
  display: flex;
  flex-direction: column;
  gap: var(--sp-2);
  margin-top: var(--sp-4);
}

.recycle-card {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  padding: var(--sp-4) var(--sp-5);
  background: var(--bg-card);
  border: 1px solid var(--border-color);
  border-radius: var(--radius-md);
  transition: box-shadow var(--transition);
}

.recycle-card:hover {
  box-shadow: var(--shadow-sm);
}

.recycle-card__main {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
  gap: var(--sp-2);
}

.recycle-card__title {
  font-size: var(--text-sm);
  font-weight: 600;
  color: var(--text-primary);
  line-height: 1.5;
}

.recycle-card__meta {
  display: flex;
  flex-wrap: wrap;
  gap: var(--sp-3);
  font-size: var(--text-xs);
}

.meta-item {
  display: inline-flex;
  align-items: center;
  gap: 4px;
}

.meta-label {
  color: var(--text-tertiary);
}

.meta-tags {
  display: inline-flex;
  gap: 4px;
  flex-wrap: wrap;
}

.tag-chip {
  display: inline-block;
  padding: 0 6px;
  line-height: 1.6;
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
  line-height: 1.6;
  font-size: var(--text-xs);
  background: color-mix(in srgb, var(--text-tertiary) 10%, transparent);
  color: var(--text-tertiary);
  border-radius: var(--radius-sm);
}

.recycle-card__times {
  display: flex;
  align-items: center;
  gap: var(--sp-2);
  font-size: var(--text-xs);
  color: var(--text-tertiary);
}

.time-item--danger {
  color: var(--danger);
  font-weight: 500;
}

.time-divider {
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
</style>
