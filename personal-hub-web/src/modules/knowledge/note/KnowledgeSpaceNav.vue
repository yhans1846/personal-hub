<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { FileText, Home, PanelLeftClose, Plus, Search, Star } from 'lucide-vue-next'
import { getNoteList, getRecentNotes } from '@/modules/knowledge/api'
import type { NoteFolderSelection, NoteFolderTreeVO, NoteVO } from '@/types/note'
import { handleApiError, unwrapPage } from '@/utils/apiResult'
import UiTooltip from '@/components/UiTooltip.vue'
import NoteFolderTree from './NoteFolderTree.vue'
import { filterNoteSummaries } from './knowledgeSpaceFilter'

const props = defineProps<{
  modelValue: NoteFolderSelection
  activeNoteId?: number | null
  readonly?: boolean
}>()

const emit = defineEmits<{
  'update:modelValue': [value: NoteFolderSelection]
  changed: []
  loaded: [data: NoteFolderTreeVO]
  'open-note': [id: number]
  collapse: []
}>()

const query = ref('')
const recent = ref<NoteVO[]>([])
const favorites = ref<NoteVO[]>([])

const filteredRecent = computed(() => filterNoteSummaries(recent.value, query.value))
const filteredFavorites = computed(() => filterNoteSummaries(favorites.value, query.value))
const isHomeActive = computed(() => props.modelValue === 'home')

async function loadSideLists() {
  try {
    const [r, f] = await Promise.all([
      unwrapPage(getRecentNotes(1, 5)),
      unwrapPage(getNoteList({ page: 1, size: 10, isFavorite: true })),
    ])
    recent.value = r.records ?? []
    favorites.value = f.records ?? []
  } catch (e) {
    handleApiError(e, '加载最近/收藏失败')
  }
}

function selectHome() {
  emit('update:modelValue', 'home')
}

onMounted(() => {
  void loadSideLists()
})
</script>

<template>
  <aside class="ks-nav" aria-label="知识空间">
    <div class="ks-head">
      <span class="ks-title">知识空间</span>
      <UiTooltip content="收起侧栏" placement="bottom">
        <button
          type="button"
          class="ks-icon-btn"
          aria-label="收起侧栏"
          @click="emit('collapse')"
        >
          <PanelLeftClose :size="14" />
        </button>
      </UiTooltip>
    </div>

    <div class="ks-search">
      <Search :size="14" class="ks-search-icon" />
      <input
        v-model="query"
        type="search"
        class="ks-search-input"
        placeholder="搜索笔记与文件夹…"
        aria-label="搜索笔记与文件夹"
      >
    </div>

    <div class="ks-body">
      <button
        type="button"
        class="ks-row"
        :class="{ active: isHomeActive }"
        @click="selectHome"
      >
        <Home :size="14" class="ks-row-icon" />
        <span class="ks-row-label">首页</span>
      </button>

      <div class="ks-section">
        <div class="ks-section-label">最近</div>
        <button
          v-for="note in filteredRecent"
          :key="'r-' + note.id"
          type="button"
          class="ks-row"
          :class="{ active: activeNoteId === note.id }"
          @click="emit('open-note', note.id)"
        >
          <FileText :size="14" class="ks-row-icon" />
          <span class="ks-row-label">{{ note.title || '无标题笔记' }}</span>
        </button>
      </div>

      <div v-if="filteredFavorites.length > 0" class="ks-section">
        <div class="ks-section-label">收藏</div>
        <button
          v-for="note in filteredFavorites"
          :key="'f-' + note.id"
          type="button"
          class="ks-row"
          :class="{ active: activeNoteId === note.id }"
          @click="emit('open-note', note.id)"
        >
          <Star :size="14" class="ks-row-icon" />
          <span class="ks-row-label">{{ note.title || '无标题笔记' }}</span>
        </button>
      </div>

      <div class="ks-section ks-section--space">
        <div class="ks-section-head">
          <div class="ks-section-label ks-section-label--inline">我的空间</div>
          <UiTooltip v-if="!readonly" content="新建文件夹" placement="bottom">
            <button
              type="button"
              class="ks-icon-btn"
              disabled
              aria-label="新建文件夹"
            >
              <Plus :size="14" />
            </button>
          </UiTooltip>
        </div>

        <!-- 完整树占位；任务 3 改为 embedded / hide-chrome -->
        <div class="ks-tree">
          <NoteFolderTree
            :model-value="modelValue"
            :active-note-id="activeNoteId ?? null"
            :readonly="readonly"
            @update:model-value="emit('update:modelValue', $event)"
            @changed="emit('changed')"
            @loaded="emit('loaded', $event)"
            @open-note="emit('open-note', $event)"
            @collapse="emit('collapse')"
          />
        </div>
      </div>
    </div>
  </aside>
</template>

<style scoped>
.ks-nav {
  display: flex;
  flex-direction: column;
  height: 100%;
  min-height: 0;
  width: 240px;
  flex-shrink: 0;
  border-right: 1px solid var(--border-color);
  background: color-mix(in srgb, var(--bg-card) 70%, var(--bg-body));
}

.ks-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 10px 12px 8px;
  flex-shrink: 0;
}

.ks-title {
  font-size: var(--text-sm);
  font-weight: 600;
  color: var(--text-secondary);
}

.ks-icon-btn {
  width: 24px;
  height: 24px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  border: none;
  border-radius: 6px;
  background: transparent;
  color: var(--text-tertiary);
  cursor: pointer;
  transition: color var(--transition-duration) ease, background var(--transition-duration) ease;
}

.ks-icon-btn:hover:not(:disabled) {
  background: var(--bg-hover);
  color: var(--text-primary);
}

.ks-icon-btn:disabled {
  opacity: 0.4;
  cursor: not-allowed;
}

@media (max-width: 768px) {
  .ks-head .ks-icon-btn {
    display: none;
  }
}

.ks-search {
  position: relative;
  flex-shrink: 0;
  margin: 0 10px 8px;
}

.ks-search-icon {
  position: absolute;
  left: 10px;
  top: 50%;
  transform: translateY(-50%);
  color: var(--text-tertiary);
  pointer-events: none;
}

.ks-search-input {
  width: 100%;
  box-sizing: border-box;
  height: 32px;
  padding: 0 10px 0 30px;
  border: 1px solid var(--border-color);
  border-radius: 6px;
  background: color-mix(in srgb, var(--bg-body) 80%, transparent);
  color: var(--text-primary);
  font-size: 13px;
  outline: none;
}

.ks-search-input::placeholder {
  color: var(--text-tertiary);
}

.ks-search-input:focus {
  border-color: color-mix(in srgb, var(--accent) 45%, var(--border-color));
}

.ks-body {
  flex: 1;
  min-height: 0;
  overflow: auto;
  padding: 0 6px 12px;
}

.ks-section {
  margin-top: 6px;
}

.ks-section--space {
  display: flex;
  flex-direction: column;
  min-height: 0;
}

.ks-section-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin: 10px 8px 4px;
}

.ks-section-label {
  margin: 10px 8px 4px;
  font-size: 11px;
  font-weight: 600;
  color: var(--text-tertiary);
  letter-spacing: 0.02em;
}

.ks-section-label--inline {
  margin: 0;
}

.ks-row {
  display: flex;
  align-items: center;
  gap: 6px;
  width: 100%;
  min-height: 32px;
  border: none;
  background: transparent;
  color: var(--text-primary);
  text-align: left;
  padding: 6px 10px;
  border-radius: 6px;
  cursor: pointer;
  font-size: var(--text-sm);
  box-sizing: border-box;
}

.ks-row:hover {
  background: var(--bg-hover);
}

.ks-row.active {
  background: color-mix(in srgb, var(--accent) 12%, transparent);
  color: var(--accent);
}

.ks-row-icon {
  flex-shrink: 0;
  opacity: 0.75;
}

.ks-row-label {
  flex: 1;
  min-width: 0;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.ks-tree {
  flex: 1;
  min-height: 0;
}

/* 内嵌完整树时去掉外层边框/固定宽，避免双层壳；「文件夹」头留到任务 3 再藏 */
.ks-tree :deep(.folder-tree) {
  width: 100%;
  border-right: none;
  background: transparent;
  height: auto;
}
</style>
