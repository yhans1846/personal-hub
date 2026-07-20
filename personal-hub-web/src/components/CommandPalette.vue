<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted, watch, type Component } from 'vue'
import { useRouter } from 'vue-router'
import { globalSearch } from '@/modules/dashboard/api'
import {
  Search, LayoutDashboard, FileText, CheckSquare, PenLine, BookOpen, Bookmark,
  Target, BookMarked, FolderOpen, BarChart3, Clock, Plus,
} from 'lucide-vue-next'
import type { SearchGroup, SearchItem } from '@/modules/dashboard/api'
import { buildCreatePath } from '@/utils/deepLink'

const router = useRouter()
const visible = ref(false)
const keyword = ref('')
const activeIndex = ref(0)
const loading = ref(false)
const apiGroups = ref<SearchGroup[]>([])
const showApiResults = ref(false)

// 搜索历史
const searchHistory = ref<string[]>(loadSearchHistory())
function loadSearchHistory(): string[] {
  try { return JSON.parse(localStorage.getItem('search-history') || '[]') }
  catch { return [] }
}
function saveSearchHistory(kw: string) {
  const history = loadSearchHistory()
  history.unshift(kw)
  const unique = [...new Set(history)].slice(0, 5)
  localStorage.setItem('search-history', JSON.stringify(unique))
  searchHistory.value = unique
}

// 本地页面路由
const pageRoutes = [
  { path: '/dashboard', label: '首页', icon: LayoutDashboard },
  { path: '/notes', label: '笔记', icon: FileText },
  { path: '/todos', label: '待办任务', icon: CheckSquare },
  { path: '/diaries', label: '日记', icon: PenLine },
  { path: '/bookmarks', label: '收藏夹', icon: Bookmark },
  { path: '/study-records', label: '学习记录', icon: BookOpen },
  { path: '/study-plans', label: '学习计划', icon: Target },
  { path: '/readings', label: '阅读记录', icon: BookMarked },
  { path: '/files', label: '文件', icon: FolderOpen },
  { path: '/stats', label: '数据统计', icon: BarChart3 },
]

/** 快捷新建（对齐 Dashboard 快捷操作） */
const createActions: { label: string; path: string; icon: Component; aliases: string[] }[] = [
  { label: '新建笔记', path: '/notes/new', icon: FileText, aliases: ['笔记', 'note', '写笔记'] },
  { label: '写日记', path: buildCreatePath('/diaries'), icon: PenLine, aliases: ['日记', 'diary'] },
  { label: '添加书籍', path: buildCreatePath('/readings'), icon: BookMarked, aliases: ['阅读', '书籍', 'reading'] },
  { label: '开始学习', path: buildCreatePath('/study-records'), icon: BookOpen, aliases: ['学习', '学习记录', 'study'] },
  { label: '新建任务', path: buildCreatePath('/todos'), icon: CheckSquare, aliases: ['待办', '任务', 'todo'] },
  { label: '新建计划', path: buildCreatePath('/study-plans'), icon: Target, aliases: ['计划', '学习计划', 'plan'] },
]

const filteredPages = computed(() => {
  const kw = keyword.value.toLowerCase().trim()
  if (!kw) return pageRoutes
  return pageRoutes.filter(p =>
    p.label.toLowerCase().includes(kw) || p.path.includes(kw)
  )
})

const filteredCreates = computed(() => {
  const kw = keyword.value.toLowerCase().trim()
  if (!kw) return createActions
  return createActions.filter((a) => {
    if (a.label.toLowerCase().includes(kw)) return true
    if (kw === '新建' || kw === 'create' || kw === 'new') return true
    return a.aliases.some((x) => x.toLowerCase().includes(kw) || kw.includes(x.toLowerCase()))
  })
})

const allResults = computed(() => {
  const items: { type: string; label: string; path?: string; item?: SearchItem; icon?: Component }[] = []
  const kw = keyword.value.trim()
  if (kw) {
    items.push({
      type: 'search',
      label: `在全站搜索「${kw}」`,
      path: `/search?q=${encodeURIComponent(kw)}`,
      icon: Search,
    })
  }
  for (const a of filteredCreates.value) {
    items.push({ type: 'create', label: a.label, path: a.path, icon: a.icon })
  }
  for (const p of filteredPages.value) {
    items.push({ type: 'page', label: p.label, path: p.path, icon: p.icon })
  }
  if (showApiResults.value) {
    for (const g of apiGroups.value) {
      for (const item of g.items.slice(0, 3)) {
        items.push({ type: g.type, label: item.title, path: item.url, item })
      }
    }
  }
  return items
})
function submitGlobalSearch(kw?: string) {
  const q = (kw ?? keyword.value).trim()
  if (!q) return
  saveSearchHistory(q)
  router.push({ path: '/search', query: { q } })
  visible.value = false
}

let debounceTimer: ReturnType<typeof setTimeout> | null = null

watch(keyword, () => {
  activeIndex.value = 0
  showApiResults.value = false
  apiGroups.value = []
  if (debounceTimer) clearTimeout(debounceTimer)

  const kw = keyword.value.trim()
  if (kw.length >= 2) {
    debounceTimer = setTimeout(async () => {
      loading.value = true
      try {
        const res = await globalSearch(kw)
        apiGroups.value = res.data.data.groups || []
        showApiResults.value = true
      } catch {
        apiGroups.value = []
      } finally {
        loading.value = false
      }
    }, 300)
  }
})

function onKeydown(e: KeyboardEvent) {
  if ((e.ctrlKey || e.metaKey) && e.key === 'k') {
    e.preventDefault()
    visible.value = !visible.value
    if (visible.value) {
      keyword.value = ''
      activeIndex.value = 0
      apiGroups.value = []
      showApiResults.value = false
      setTimeout(() => document.getElementById('cp-input')?.focus(), 50)
    }
  }

  if (!visible.value) return

  if (e.key === 'Escape') {
    visible.value = false
    return
  }

  if (e.key === 'ArrowDown') {
    e.preventDefault()
    activeIndex.value = Math.min(activeIndex.value + 1, allResults.value.length - 1)
  }
  if (e.key === 'ArrowUp') {
    e.preventDefault()
    activeIndex.value = Math.max(activeIndex.value - 1, 0)
  }
  if (e.key === 'Enter') {
    e.preventDefault()
    const kw = keyword.value.trim()
    const hit = allResults.value[activeIndex.value]
    if (hit?.path) {
      if (kw) saveSearchHistory(kw)
      router.push(hit.path)
      visible.value = false
    } else if (kw) {
      submitGlobalSearch(kw)
    }
  }
}

onMounted(() => window.addEventListener('keydown', onKeydown))
onUnmounted(() => window.removeEventListener('keydown', onKeydown))

function selectItem(item: { path?: string }) {
  const kw = keyword.value.trim()
  if (kw) saveSearchHistory(kw)
  if (item.path) router.push(item.path)
  visible.value = false
}

function goToSearchPage(kw: string) {
  keyword.value = kw
  submitGlobalSearch(kw)
}

function onOverlayClick() {
  visible.value = false
}

/** 外部调用的打开方法 */
function open() {
  visible.value = true
  keyword.value = ''
  activeIndex.value = 0
  apiGroups.value = []
  showApiResults.value = false
  setTimeout(() => document.getElementById('cp-input')?.focus(), 50)
}

defineExpose({ open })
</script>

<template>
  <Teleport to="body">
    <div v-if="visible" class="cp-overlay" @click.self="onOverlayClick">
      <div class="cp-panel">
        <div class="cp-search">
          <Search :size="16" class="cp-search-icon" />
          <input
            id="cp-input"
            v-model="keyword"
            class="cp-input"
            placeholder="搜索或新建：笔记、日记、任务..."
            spellcheck="false"
          />
          <kbd class="cp-kbd">ESC</kbd>
        </div>

        <div v-if="allResults.length > 0" class="cp-results">
          <div
            v-for="(item, idx) in allResults"
            :key="idx"
            class="cp-item"
            :class="{ active: activeIndex === idx }"
            @click="selectItem(item)"
            @mouseenter="activeIndex = idx"
          >
            <component :is="item.icon" v-if="item.icon" :size="14" class="cp-item-icon" />
            <span class="cp-item-label">{{ item.label }}</span>
            <span v-if="item.type === 'search'" class="cp-item-badge">搜索</span>
            <span v-else-if="item.type === 'create'" class="cp-item-badge cp-item-badge--create">
              <Plus :size="10" /> 新建
            </span>
            <span v-else-if="item.type === 'page'" class="cp-item-badge">页面</span>
            <span v-else class="cp-item-badge cp-item-badge--api">{{ item.type }}</span>
          </div>
          <div v-if="loading" class="cp-loading">搜索中...</div>
        </div>

        <div v-if="!keyword.trim() && searchHistory.length > 0" class="cp-history">
          <div class="cp-history-header">
            <Clock :size="12" />
            <span>最近搜索</span>
          </div>
          <div
            v-for="(item, idx) in searchHistory"
            :key="'h-' + idx"
            class="cp-history-item"
            @click="goToSearchPage(item)"
          >
            <Search :size="12" class="cp-history-search-icon" />
            <span>{{ item }}</span>
          </div>
        </div>

        <div v-else-if="keyword.trim().length > 0 && !loading && allResults.length === 0" class="cp-empty">
          <button type="button" class="cp-search-fallback" @click="submitGlobalSearch()">
            在全站搜索「{{ keyword.trim() }}」
          </button>
        </div>
      </div>
    </div>
  </Teleport>
</template>

<style scoped>
.cp-overlay {
  position: fixed; inset: 0; z-index: 9999;
  background: rgba(0,0,0,0.4);
  display: flex; justify-content: center; padding-top: 15vh;
}
.cp-panel {
  width: 560px; max-height: 480px;
  background: var(--bg-card);
  border-radius: var(--radius-xl);
  box-shadow: var(--shadow-xl);
  overflow: hidden;
  display: flex; flex-direction: column;
}
.cp-search {
  display: flex; align-items: center; gap: var(--sp-3);
  padding: var(--sp-3) var(--sp-4);
  border-bottom: 1px solid var(--border-color);
}
.cp-search-icon { color: var(--text-tertiary); flex-shrink: 0; }
.cp-input {
  flex: 1; border: none; outline: none;
  font-size: var(--text-base);
  background: transparent;
  color: var(--text-primary);
  font-family: var(--font-sans);
}
.cp-input::placeholder { color: var(--text-placeholder); }
.cp-kbd {
  font-size: 11px; padding: 2px 6px;
  border-radius: var(--radius-sm);
  background: var(--bg-hover); color: var(--text-tertiary);
  border: 1px solid var(--border-color);
  font-family: var(--font-mono);
}
.cp-results {
  flex: 1; overflow-y: auto;
  padding: var(--sp-2);
}
.cp-item {
  display: flex; align-items: center; gap: var(--sp-3);
  padding: var(--sp-2) var(--sp-3);
  border-radius: var(--radius-sm);
  cursor: pointer; transition: background var(--transition);
}
.cp-item.active, .cp-item:hover { background: var(--bg-hover); }
.cp-item-icon { color: var(--text-secondary); flex-shrink: 0; }
.cp-item-label { flex: 1; font-size: var(--text-sm); color: var(--text-primary); }
.cp-item-badge {
  font-size: 11px; padding: 0 6px; border-radius: var(--radius-sm);
  background: var(--accent-light); color: var(--accent);
}
.cp-item-badge--api { background: var(--bg-hover); color: var(--text-tertiary); }
.cp-item-badge--create {
  display: inline-flex;
  align-items: center;
  gap: 2px;
  background: var(--accent-light);
  color: var(--accent);
}
.cp-loading {
  text-align: center; padding: var(--sp-4);
  font-size: var(--text-sm); color: var(--text-tertiary);
}
.cp-empty {
  text-align: center; padding: var(--sp-6);
  font-size: var(--text-sm); color: var(--text-tertiary);
}
.cp-search-fallback {
  background: none; border: none; cursor: pointer;
  font-size: var(--text-sm); color: var(--accent);
  padding: var(--sp-2) var(--sp-3); border-radius: var(--radius-sm);
  transition: background var(--transition);
}
.cp-search-fallback:hover { background: var(--bg-hover); }
.cp-hint {
  text-align: center; padding: var(--sp-6);
  font-size: var(--text-sm); color: var(--text-tertiary);
}
.cp-hint-sub { margin-top: var(--sp-2); }

/* 搜索历史 */
.cp-history { padding: var(--sp-2); }
.cp-history-header {
  display: flex; align-items: center; gap: var(--sp-2);
  padding: var(--sp-1) var(--sp-3) var(--sp-2);
  font-size: 11px; color: var(--text-tertiary);
}
.cp-history-item {
  display: flex; align-items: center; gap: var(--sp-3);
  padding: var(--sp-2) var(--sp-3);
  border-radius: var(--radius-sm);
  cursor: pointer; transition: background var(--transition);
  font-size: var(--text-sm); color: var(--text-secondary);
}
.cp-history-item:hover { background: var(--bg-hover); color: var(--text-primary); }
.cp-history-search-icon { color: var(--text-tertiary); flex-shrink: 0; }
.cp-hint-sub kbd {
  font-size: 11px; padding: 1px 5px;
  border-radius: var(--radius-sm);
  background: var(--bg-hover); border: 1px solid var(--border-color);
  font-family: var(--font-mono);
}
</style>
