<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { globalSearch } from '@/api/dashboardApi'
import { ElMessage } from 'element-plus'
import { Search, FileText, PenLine, CheckSquare, BookOpen, Bookmark, BookMarked, FolderOpen, Target } from 'lucide-vue-next'
import type { SearchGroup, SearchItem } from '@/api/dashboardApi'

const router = useRouter()
const route = useRoute()

const keyword = ref('')
const loading = ref(false)
const searched = ref(false)
const groups = ref<SearchGroup[]>([])
const total = ref(0)

const iconMap: Record<string, any> = {
  FileText, PenLine, CheckSquare, BookOpen,
  Bookmark, BookMarked, FolderOpen, Target
}

onMounted(() => {
  const q = route.query.q as string
  if (q) {
    keyword.value = q
    doSearch()
  }
})

async function doSearch() {
  const kw = keyword.value.trim()
  if (!kw) { ElMessage.warning('请输入搜索关键词'); return }

  loading.value = true
  searched.value = true
  try {
    const res = await globalSearch(kw)
    groups.value = res.data.data.groups
    total.value = res.data.data.total
  } catch {
    groups.value = []
    total.value = 0
  } finally {
    loading.value = false
  }
}

function goTo(item: SearchItem) {
  if (item.url) router.push(item.url)
}

function highlight(text: string, kw: string): string {
  if (!text || !kw) return text || ''
  const escaped = kw.replace(/[.*+?^${}()|[\]\\]/g, '\\$&')
  return text.replace(new RegExp(`(${escaped})`, 'gi'), '<mark class="hl">$1</mark>')
}
</script>

<template>
  <div class="search-page">
    <!-- 搜索框 -->
    <div class="search-bar">
      <el-input
        v-model="keyword"
        placeholder="搜索笔记、日记、待办、收藏..."
        size="large"
        clearable
        @keyup.enter="doSearch"
      >
        <template #prefix>
          <Search :size="16" style="color: var(--text-tertiary)" />
        </template>
        <template #append>
          <el-button @click="doSearch" :loading="loading">搜索</el-button>
        </template>
      </el-input>
    </div>

    <!-- 未搜索提示 -->
    <div v-if="!searched" class="empty-state" style="margin-top: 80px;">
      <div class="empty-state__icon"><Search :size="48" /></div>
      <div class="empty-state__text">输入关键词搜索所有模块内容</div>
    </div>

    <!-- 搜索中 -->
    <div v-if="loading" class="loading-skeleton" style="margin-top: var(--sp-6);">
      <div v-for="i in 3" :key="i" class="skeleton-card" />
    </div>

    <!-- 搜索结果 -->
    <template v-if="!loading && searched">
      <!-- 无结果 -->
      <div v-if="total === 0" class="empty-state" style="margin-top: 60px;">
        <div class="empty-state__icon"><Search :size="48" /></div>
        <div class="empty-state__text">没有找到 "{{ keyword }}" 相关的结果</div>
      </div>

      <!-- 结果列表 -->
      <div v-else class="search-results">
        <div class="result-summary">共找到 {{ total }} 条结果</div>

        <div v-for="group in groups" :key="group.type" class="result-group">
          <div class="group-header">
            <component :is="iconMap[group.icon] || FileText" :size="16" />
            <span>{{ group.label }}</span>
            <span class="group-count">{{ group.count }}</span>
          </div>
          <div class="group-items">
            <div
              v-for="item in group.items" :key="`${group.type}-${item.id}`"
              class="result-item"
              @click="goTo(item)"
            >
              <div class="item-title" v-html="highlight(item.title, keyword)" />
              <div v-if="item.snippet" class="item-snippet" v-html="highlight(item.snippet, keyword)" />
              <div class="item-meta">
                <span v-if="item.date">{{ item.date }}</span>
              </div>
            </div>
          </div>
        </div>
      </div>
    </template>
  </div>
</template>

<style scoped>
.search-page { max-width: 720px; margin: 0 auto; }
.search-bar { margin-bottom: var(--sp-6); }

.loading-skeleton { display: flex; flex-direction: column; gap: var(--sp-4); }
.skeleton-card { height: 80px; border-radius: var(--radius-md); background: var(--bg-hover); animation: pulse 1.5s ease-in-out infinite; }

.result-summary { font-size: var(--text-sm); color: var(--text-tertiary); margin-bottom: var(--sp-4); }

.result-group { margin-bottom: var(--sp-5); }
.group-header {
  display: flex; align-items: center; gap: var(--sp-2);
  font-size: var(--text-sm); font-weight: 600; margin-bottom: var(--sp-2); color: var(--text-secondary);
}
.group-count {
  font-size: var(--text-xs); color: var(--text-tertiary); font-weight: 400;
  background: var(--bg-hover); padding: 0 6px; border-radius: 10px; line-height: 18px;
}

.group-items { display: flex; flex-direction: column; gap: 2px; }
.result-item {
  padding: var(--sp-3) var(--sp-4); border-radius: var(--radius-sm);
  cursor: pointer; transition: background var(--transition);
  border: 1px solid transparent;
}
.result-item:hover { background: var(--bg-hover); border-color: var(--border-light); }

.item-title { font-size: var(--text-sm); font-weight: 500; margin-bottom: 2px; color: var(--text-primary); }
.item-snippet {
  font-size: var(--text-xs); color: var(--text-secondary);
  line-height: var(--leading-normal); overflow: hidden;
  display: -webkit-box; -webkit-line-clamp: 2; -webkit-box-orient: vertical;
}
.item-meta { font-size: var(--text-xs); color: var(--text-tertiary); margin-top: 4px; }

:deep(.hl) { background: #fef3c7; color: #d97706; padding: 0 2px; border-radius: 2px; }
</style>
