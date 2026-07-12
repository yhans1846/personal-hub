<script setup lang="ts">
import type { Component } from 'vue'
import { useAuthStore } from '@/store/authStore'
import { useLayoutStore } from '@/store/layoutStore'
import { useRouter, useRoute } from 'vue-router'
import { LayoutDashboard, FileText, BookOpen, CheckSquare, PenLine, Bookmark, Target, BookMarked, FolderOpen, Grid3X3, Tags, Settings, Trash2, Search, BarChart3, Plus, Github, Sun, Moon, Palette, Menu, X, ChevronDown, Library, Archive, Cog, LogOut } from 'lucide-vue-next'
import { ref, onMounted } from 'vue'
import CommandPalette from './CommandPalette.vue'
import NotificationBell from './NotificationBell.vue'
import TodoDialog from '@/modules/planning/todo/TodoDialog.vue'
import DiaryDialog from '@/modules/knowledge/diary/DiaryDialog.vue'
import BookmarkDialog from '@/modules/resource/bookmark/BookmarkDialog.vue'
import StudyDialog from '@/modules/knowledge/study/StudyDialog.vue'
import type { MenuItem } from '@/types/layout'

const authStore = useAuthStore()
const layoutStore = useLayoutStore()
const router = useRouter()
const route = useRoute()

const iconMap: Record<string, Component> = {
  dashboard: LayoutDashboard,
  notes: FileText,
  'study-records': BookOpen,
  todos: CheckSquare,
  diaries: PenLine,
  bookmarks: Bookmark,
  'study-plans': Target,
  readings: BookMarked,
  files: FolderOpen,
  'note-categories': Grid3X3,
  tags: Tags,
  'file-categories': Grid3X3,
  'bookmark-categories': Grid3X3,
  recycle: Trash2,
  stats: BarChart3,
  settings: Settings,
}

const sectionIconMap: Record<string, Component> = {
  workspace: LayoutDashboard,
  knowledge: Library,
  resource: Archive,
  manage: Cog,
  stats: BarChart3,
}

function isActiveRoute(item: MenuItem): boolean {
  const path = route.path
  if (item.code === 'dashboard') return path === '/dashboard'
  if (item.code === 'notes') return path.startsWith('/notes') && !path.includes('/tags') && !path.includes('/recycle')
  if (item.code === 'bookmarks') return path.startsWith('/bookmarks')
  if (item.code === 'files') return path.startsWith('/files')
  if (item.route) return path.startsWith(item.route)
  return false
}

const sidebarOpen = ref(false)

function toggleSidebar() { sidebarOpen.value = !sidebarOpen.value }
function closeSidebar() { sidebarOpen.value = false }

// 菜单分组折叠
const COLLAPSE_KEY = 'sidebar-collapsed-sections'
const collapsedSections = ref<Set<string>>(new Set(loadCollapsed()))
function loadCollapsed(): string[] {
  try { return JSON.parse(localStorage.getItem(COLLAPSE_KEY) || '[]') } catch { return [] }
}
function saveCollapsed() {
  localStorage.setItem(COLLAPSE_KEY, JSON.stringify([...collapsedSections.value]))
}
function toggleSection(key: string) {
  if (collapsedSections.value.has(key)) collapsedSections.value.delete(key)
  else collapsedSections.value.add(key)
  saveCollapsed()
}

// 主题切换
const isDark = ref(document.documentElement.getAttribute('data-theme') === 'dark')
function toggleTheme() {
  isDark.value = !isDark.value
  const theme = isDark.value ? 'dark' : 'light'
  document.documentElement.setAttribute('data-theme', theme)
  localStorage.setItem('theme-preference', theme)
  ;(window as any).__themeUserOverride = true
  // 同步外观配置
  const appConfig = layoutStore.appearanceConfig
  if (appConfig) layoutStore.saveAppearanceConfig({ ...appConfig, theme: theme as any })
}

// 强调色
const accentColors = [
  { key: 'blue', color: '#4F7BFF' },
  { key: 'purple', color: '#8B5CF6' },
  { key: 'cyan', color: '#06B6D4' },
  { key: 'orange', color: '#F97316' },
  { key: 'green', color: '#10B981' },
]
const currentAccent = ref(document.documentElement.getAttribute('data-accent') || 'blue')
function setAccent(key: string) {
  currentAccent.value = key
  document.documentElement.setAttribute('data-accent', key)
  localStorage.setItem('accent-preference', key)
  // 同步外观配置
  const appConfig = layoutStore.appearanceConfig
  if (appConfig) layoutStore.saveAppearanceConfig({ ...appConfig, accent: key as any })
}

// 暴露全局钩子供 layoutStore 使用
;(window as any).__setTheme = (theme: string) => { isDark.value = theme === 'dark' }
;(window as any).__setAccent = (key: string) => { currentAccent.value = key }

onMounted(() => {
  const saved = localStorage.getItem('accent-preference')
  if (saved) setAccent(saved)
  if (!layoutStore.loaded) layoutStore.fetchLayout()
})

// 快捷创建 — 弹窗/跳转统一入口
const todoVisible = ref(false)
const diaryVisible = ref(false)
const bookmarkVisible = ref(false)
const studyVisible = ref(false)

function handleQuickCreate(cmd: string) {
  if (cmd === 'note') { router.push('/notes/new'); return }
  if (cmd === 'todo') { todoVisible.value = true; return }
  if (cmd === 'diary') { diaryVisible.value = true; return }
  if (cmd === 'bookmark') { bookmarkVisible.value = true; return }
  if (cmd === 'study-record') { studyVisible.value = true; return }
  router.push('/notes/new')
}
</script>

<template>
  <div class="app-shell">
    <!-- 顶部栏 -->
    <header class="topbar">
      <div class="topbar-left">
        <button class="hamburger" @click="toggleSidebar" aria-label="菜单">
          <Menu :size="20" />
        </button>
        <router-link to="/" class="topbar-brand">
          <svg class="brand-logo" width="22" height="22" viewBox="0 0 22 22" fill="none">
            <rect x="2" y="2" width="9" height="9" rx="2.5" fill="var(--accent)" opacity="0.55" />
            <rect x="7" y="7" width="9" height="9" rx="2.5" fill="var(--accent)" opacity="0.75" />
            <rect x="12" y="12" width="9" height="9" rx="2.5" fill="var(--accent)" />
          </svg>
          <span class="brand-text">Personal Hub</span>
        </router-link>
      </div>
      <div class="topbar-center">
        <div class="search-trigger" @click="router.push('/search')" title="Ctrl+K 打开命令面板">
          <Search :size="14" />
          <span class="search-hint">搜索...</span>
          <kbd class="search-kbd">Ctrl+K</kbd>
        </div>
      </div>

      <div class="topbar-actions">
        <!-- 快捷创建（暂隐藏） -->


        <!-- GitHub -->
        <a href="https://github.com/yhans1846/personal-hub" target="_blank" class="topbar-icon-btn" title="GitHub">
          <Github :size="18" />
        </a>

        <!-- 主题切换 -->
        <button class="topbar-icon-btn" :title="isDark ? '浅色模式' : '深色模式'" @click="toggleTheme">
          <Sun v-if="isDark" :size="18" />
          <Moon v-else :size="18" />
        </button>

        <!-- 强调色 -->
        <el-dropdown trigger="click" placement="bottom">
          <button class="topbar-icon-btn" title="主题色">
            <Palette :size="18" />
          </button>
          <template #dropdown>
            <div class="accent-picker">
              <span class="accent-picker__label">主题色</span>
              <div class="accent-picker__colors">
                <button
                  v-for="ac in accentColors" :key="ac.key"
                  class="accent-dot"
                  :class="{ active: currentAccent === ac.key }"
                  :style="{ background: ac.color }"
                  :title="ac.key"
                  @click="setAccent(ac.key)"
                />
              </div>
            </div>
          </template>
        </el-dropdown>

        <!-- 通知 -->
        <NotificationBell />
      </div>

    </header>

    <div class="app-body">
      <!-- 侧边栏遮罩（移动端） -->
      <div class="sidebar-overlay" :class="{ open: sidebarOpen }" @click="closeSidebar" />

      <!-- 侧边栏 -->
      <aside class="sidebar" :class="{ open: sidebarOpen }">
        <div class="sidebar-header">
          <span class="sidebar-title">导航</span>
          <button class="sidebar-close" @click="closeSidebar"><X :size="18" /></button>
        </div>
        <nav class="sidebar-nav">
          <div class="sidebar-section" v-for="section in layoutStore.visibleMenuSections" :key="section.key">
            <div class="section-header" :class="{ collapsed: collapsedSections.has(section.key) }" @click="toggleSection(section.key)">
              <ChevronDown :size="14" class="section-chevron" />
              <component :is="sectionIconMap[section.key]" :size="14" class="section-icon" />
              <span>{{ section.title }}</span>
            </div>
            <Transition name="section-collapse">
              <div v-if="!collapsedSections.has(section.key)" class="section-items">
                <router-link
                  v-for="item in section.items" :key="item.code"
                  :to="item.route || '/'"
                  class="nav-item"
                  :class="{ active: isActiveRoute(item), 'nav-item--secondary': section.key !== 'workspace', 'nav-item--primary': section.key === 'workspace' }"
                >
                  <component :is="iconMap[item.code] || LayoutDashboard" :size="18" />
                  <span>{{ item.title }}</span>
                </router-link>
              </div>
            </Transition>
          </div>
        </nav>
        <div class="sidebar-footer">
          <div class="sidebar-user">
            <div class="sidebar-avatar">
              {{ (authStore.user?.nickname || authStore.user?.username || '?')[0] }}
            </div>
            <span class="sidebar-user-name">{{ authStore.user?.nickname || authStore.user?.username }}</span>
          </div>
          <button class="sidebar-logout" title="退出登录" @click="authStore.logout()">
            <LogOut :size="16" />
          </button>
        </div>
      </aside>

      <!-- 主内容 -->
      <main class="main-content">
        <div class="content-container">
          <router-view />
        </div>
      </main>
    </div>
    <CommandPalette />

    <!-- 快捷创建弹窗 -->
    <TodoDialog v-model="todoVisible" @saved="todoVisible = false" />
    <DiaryDialog v-model="diaryVisible" @saved="diaryVisible = false" />
    <BookmarkDialog v-model="bookmarkVisible" @saved="bookmarkVisible = false" />
    <StudyDialog v-model="studyVisible" @saved="studyVisible = false" />
  </div>
</template>

<style scoped>
/* ============ 布局容器 ============ */
.app-shell { display: flex; flex-direction: column; height: 100vh; overflow: hidden; }

/* ============ 顶部栏 ============ */
.topbar {
  height: var(--topbar-height);
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 var(--sp-6);
  border-bottom: 1px solid color-mix(in srgb, var(--border-color) 30%, transparent);
  background: var(--bg-card);
  flex-shrink: 0;
  z-index: 100;
}
.topbar-left { display: flex; align-items: center; gap: var(--sp-2); }
.hamburger {
  display: none; width: 32px; height: 32px;
  align-items: center; justify-content: center;
  background: none; border: none; color: var(--text-secondary);
  cursor: pointer; border-radius: var(--radius-sm);
  transition: all var(--transition);
}
.hamburger:hover { background: var(--bg-hover); color: var(--text-primary); }
.topbar-brand {
  display: flex; align-items: center; gap: var(--sp-2);
  color: var(--text-primary); font-weight: 600; font-size: var(--text-base); text-decoration: none;
}
.brand-logo { flex-shrink: 0; }

.topbar-center { flex: 1; display: flex; justify-content: center; max-width: 400px; margin: 0 auto; }
.search-trigger {
  display: flex; align-items: center; gap: var(--sp-2);
  padding: 6px 14px; width: 100%;
  border-radius: var(--radius-sm);
  border: 1px solid var(--border-color);
  color: var(--text-tertiary); font-size: var(--text-sm);
  cursor: pointer; transition: all var(--transition);
  background: var(--bg-body);
}
.search-trigger:hover { border-color: var(--accent); color: var(--text-secondary); background: var(--bg-card); }
.search-hint { color: var(--text-placeholder); }
.search-kbd { margin-left: auto; font-size: 10px; padding: 1px 5px; border-radius: 4px; background: var(--bg-hover); color: var(--text-tertiary); border: 1px solid var(--border-color); font-family: var(--font-mono); }

.topbar-actions { display: flex; align-items: center; gap: var(--sp-2); margin: 0 var(--sp-4); }
.topbar-icon-btn {
  width: 32px; height: 32px;
  display: flex; align-items: center; justify-content: center;
  border-radius: var(--radius-sm); border: none;
  background: transparent; color: var(--text-secondary);
  cursor: pointer; transition: all var(--transition);
}
.topbar-icon-btn:hover { background: var(--bg-hover); color: var(--text-primary); }
.quick-create-btn { gap: 4px; height: 32px; }


/* ============ 主体区域 ============ */
.app-body { display: flex; flex: 1; overflow: hidden; }

/* ============ 侧边栏 ============ */
.sidebar-overlay {
  display: none; position: fixed; inset: 0; z-index: 90;
  background: rgba(0,0,0,0.4);
}
.sidebar {
  width: var(--sidebar-width); background: var(--bg-sidebar);
  border-right: 1px solid color-mix(in srgb, var(--border-color) 30%, transparent);
  flex-shrink: 0;
  display: flex; flex-direction: column;
}
.sidebar-header { display: none; }
.sidebar-nav {
  display: flex; flex-direction: column;
  flex: 1; overflow-y: auto; padding: var(--sp-4) 0;
}

.sidebar-section { margin-bottom: var(--sp-4); }
.sidebar-section:last-child { margin-bottom: 0; }
.section-header {
  display: flex; align-items: center; gap: 4px;
  padding: var(--sp-2) var(--sp-4) var(--sp-2) var(--sp-4);
  font-size: 11px; font-weight: 600;
  color: var(--text-tertiary);
  text-transform: uppercase; letter-spacing: 0.5px;
  user-select: none;
  cursor: pointer;
  position: relative;
  transition: color 150ms ease;
}
.section-header:hover { color: var(--text-primary); }
/* 短分割线 */
.section-header::after {
  content: '';
  position: absolute;
  left: var(--sp-4);
  bottom: 0;
  width: 28px;
  height: 1px;
  background: var(--border-color);
  opacity: 0.4;
}
.section-chevron {
  flex-shrink: 0;
  transition: transform 200ms ease;
  opacity: 0.45;
}
.section-icon {
  flex-shrink: 0;
  opacity: 0.7;
}
.section-header:hover .section-chevron { opacity: 0.8; }
.section-header:hover .section-icon { opacity: 0.9; }
.section-header.collapsed .section-chevron { transform: rotate(-90deg); }
.section-items { overflow: hidden; }

/* 菜单项基础 */
.nav-item {
  display: flex; align-items: center; gap: var(--sp-3);
  position: relative;
  height: 38px; padding: 0 var(--sp-4); margin: 0 var(--sp-2) 1px;
  border-radius: var(--radius-md);
  font-size: 14px; font-weight: 500; color: var(--text-secondary); text-decoration: none;
  transition: background 150ms ease, color 150ms ease;
}
.nav-item:hover { background: var(--bg-hover); color: var(--text-primary); }
.nav-item:hover :deep(svg) { color: var(--text-primary); }

/* 首页 — 更突出 */
.nav-item--primary {
  font-weight: 600;
}

/* 二级菜单 — 靠左对齐，仅字体/图标变轻 */
.nav-item--secondary {
  font-weight: 400;
  color: var(--text-tertiary);
}
.nav-item--secondary :deep(svg) {
  width: 16px; height: 16px; opacity: 0.55;
}

/* Active — 柔和 */
.nav-item.active {
  background: var(--accent-light);
  color: var(--accent);
  font-weight: 500;
}
.nav-item.active :deep(svg) { color: var(--accent); }
.nav-item.active::before {
  content: '';
  position: absolute;
  left: 0;
  top: 50%;
  transform: translateY(-50%);
  width: 3px;
  height: 18px;
  background: var(--accent);
  border-radius: 0 3px 3px 0;
  opacity: 0.5;
}

/* 侧边栏底部用户信息 */
.sidebar-footer {
  display: flex; align-items: center; justify-content: space-between;
  padding: var(--sp-3) var(--sp-4);
  border-top: 1px solid color-mix(in srgb, var(--border-color) 25%, transparent);
}
.sidebar-user {
  display: flex; align-items: center; gap: var(--sp-2);
  overflow: hidden;
}
.sidebar-avatar {
  width: 26px; height: 26px; border-radius: 50%; flex-shrink: 0;
  background: var(--accent-light); color: var(--accent);
  display: flex; align-items: center; justify-content: center;
  font-size: 11px; font-weight: 600;
}
.sidebar-user-name {
  font-size: var(--text-sm); color: var(--text-secondary);
  white-space: nowrap; overflow: hidden; text-overflow: ellipsis;
}
.sidebar-logout {
  width: 28px; height: 28px; flex-shrink: 0;
  display: flex; align-items: center; justify-content: center;
  background: none; border: none; color: var(--text-tertiary);
  cursor: pointer; border-radius: var(--radius-sm);
  transition: all 150ms ease;
}
.sidebar-logout:hover { background: var(--bg-hover); color: var(--danger); }
.section-collapse-enter-active,
.section-collapse-leave-active {
  transition: max-height 200ms ease, opacity 200ms ease;
  overflow: hidden;
}
.section-collapse-enter-from,
.section-collapse-leave-to {
  max-height: 0;
  opacity: 0;
}
.section-collapse-enter-to,
.section-collapse-leave-from {
  max-height: 600px;
  opacity: 1;
}

/* ============ 主内容 ============ */
.main-content { flex: 1; overflow-y: auto; padding: var(--sp-8); }
.content-container { max-width: var(--content-max-width); margin: 0 auto; width: 100%; }

/* ============ 响应式：平板 ============ */
@media (max-width: 1024px) {
  .quick-create-btn span { display: none; }
}

/* ============ 响应式：移动端 ============ */
@media (max-width: 768px) {
  .hamburger { display: flex; }
  .topbar { padding: 0 var(--sp-3); }
  .topbar-center { max-width: none; margin: 0 var(--sp-2); }
  .search-hint { display: none; }
  .topbar-actions { margin: 0 var(--sp-2); gap: var(--sp-1); }
  .quick-create-btn span { display: none; }
  .brand-text { display: none; }
  .main-content { padding: var(--sp-4); }

  .sidebar-overlay { display: block; opacity: 0; visibility: hidden; transition: all var(--transition); }
  .sidebar-overlay.open { opacity: 1; visibility: visible; }

  .sidebar {
    position: fixed; top: 0; left: 0; bottom: 0; z-index: 95;
    transform: translateX(-100%); transition: transform 250ms ease;
    padding-top: 0;
  }
  .sidebar.open { transform: translateX(0); }
  .sidebar-header {
    display: flex; align-items: center; justify-content: space-between;
    padding: var(--sp-3) var(--sp-4);
    border-bottom: 1px solid var(--border-color);
  }
  .sidebar-title { font-size: var(--text-sm); font-weight: 600; color: var(--text-primary); }
  .sidebar-close {
    background: none; border: none; color: var(--text-secondary);
    cursor: pointer; padding: 4px; border-radius: var(--radius-sm);
    transition: all var(--transition);
  }
  .sidebar-close:hover { background: var(--bg-hover); color: var(--text-primary); }
}
</style>
