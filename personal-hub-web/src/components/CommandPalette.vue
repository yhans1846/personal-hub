<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted, watch } from 'vue'
import { useRouter } from 'vue-router'
import { globalSearch } from '@/api/dashboardApi'
import { Search, LayoutDashboard, FileText, CheckSquare, PenLine, BookOpen, Bookmark, Target, BookMarked, FolderOpen, BarChart3 } from 'lucide-vue-next'
import type { SearchGroup } from '@/api/dashboardApi'

const router = useRouter()
const visible = ref(false)
const keyword = ref('')
const activeIndex = ref(0)
const loading = ref(false)
const apiGroups = ref<SearchGroup[]>([])
const showApiResults = ref(false)

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

const filteredPages = computed(() => {
  const kw = keyword.value.toLowerCase().trim()
  if (!kw) return pageRoutes
  return pageRoutes.filter(p =>
    p.label.toLowerCase().includes(kw) || p.path.includes(kw)
  )
})

const allResults = computed(() => {
  const items: { type: string; label: string; path?: string; item?: any; icon?: any }[] = []
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
    const hit = allResults.value[activeIndex.value]
    if (hit) {
      if (hit.path) router.push(hit.path)
      visible.value = false
    }
  }
}

onMounted(() => window.addEventListener('keydown', onKeydown))
onUnmounted(() => window.removeEventListener('keydown', onKeydown))

function selectItem(item: { path?: string }) {
  if (item.path) router.push(item.path)
  visible.value = false
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
            placeholder="搜索页面、笔记、任务..."
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
            <span v-if="item.type === 'page'" class="cp-item-badge">页面</span>
            <span v-else class="cp-item-badge cp-item-badge--api">{{ item.type }}</span>
          </div>
          <div v-if="loading" class="cp-loading">搜索中...</div>
        </div>

        <div v-else-if="keyword.trim().length > 0 && !loading" class="cp-empty">
          没有匹配结果
        </div>

        <div v-else class="cp-hint">
          <p>输入关键词搜索页面和内容</p>
          <p class="cp-hint-sub"><kbd>↑</kbd> <kbd>↓</kbd> 导航 <kbd>⏎</kbd> 打开 <kbd>ESC</kbd> 关闭</p>
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
  width: 560px; max-height: 400px;
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
  border-radius: 4px;
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
  font-size: 11px; padding: 0 6px; border-radius: 4px;
  background: var(--accent-light); color: var(--accent);
}
.cp-item-badge--api { background: var(--bg-hover); color: var(--text-tertiary); }
.cp-loading {
  text-align: center; padding: var(--sp-4);
  font-size: var(--text-sm); color: var(--text-tertiary);
}
.cp-empty {
  text-align: center; padding: var(--sp-6);
  font-size: var(--text-sm); color: var(--text-tertiary);
}
.cp-hint {
  text-align: center; padding: var(--sp-6);
  font-size: var(--text-sm); color: var(--text-tertiary);
}
.cp-hint-sub { margin-top: var(--sp-2); }
.cp-hint-sub kbd {
  font-size: 11px; padding: 1px 5px;
  border-radius: 3px;
  background: var(--bg-hover); border: 1px solid var(--border-color);
  font-family: var(--font-mono);
}
</style>
