<script setup lang="ts">
import type { Component } from 'vue'
import { useAuthStore } from '@/store/authStore'
import { useLayoutStore } from '@/store/layoutStore'
import { useThemeStore } from '@/store/themeStore'
import { useRoute } from 'vue-router'
import type { AppearanceConfig } from '@/types/layout'
import { LayoutDashboard, FileText, BookOpen, CheckSquare, PenLine, Bookmark, Target, BookMarked, FolderOpen, Grid3X3, Tags, Settings, Trash2, Search, BarChart3, Sun, Moon, Menu, X, ChevronDown, LogOut, PanelLeftClose, PanelLeftOpen } from 'lucide-vue-next'
import { ref, computed, onMounted, onUnmounted } from 'vue'
import CommandPalette from './CommandPalette.vue'
import NotificationBell from './NotificationBell.vue'
import ProfileDrawer from './ProfileDrawer.vue'
import UiTooltip from './UiTooltip.vue'
import type { MenuItem } from '@/types/layout'

const authStore = useAuthStore()
const layoutStore = useLayoutStore()
const themeStore = useThemeStore()
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
const profileDrawerOpen = ref(false)

function toggleSidebar() { sidebarOpen.value = !sidebarOpen.value }

// 命令面板引用
const commandPaletteRef = ref<InstanceType<typeof CommandPalette>>()
function openCommandPalette() { commandPaletteRef.value?.open() }

// 用户头像
const userName = computed(() => authStore.user?.nickname || authStore.user?.username || '')
const avatarInitial = computed(() => userName.value ? userName.value[0].toUpperCase() : '?')

// 当前日期
const DAY_NAMES = ['周日', '周一', '周二', '周三', '周四', '周五', '周六']
const todayStr = computed(() => {
  const d = new Date()
  return `${d.getMonth() + 1}月${d.getDate()}日 · ${DAY_NAMES[d.getDay()]}`
})

// 面包屑导航
const sectionMap: Record<string, string> = {
  notes: '知识', todos: '工作台', diaries: '知识',
  bookmarks: '资源', 'study-records': '知识', 'study-plans': '工作台',
  readings: '知识', files: '资源', categories: '管理',
  tags: '管理', stats: '统计', settings: '系统',
}
const breadcrumbItems = computed(() => {
  if (route.meta.hideBreadcrumb) return []
  if (route.path === '/dashboard') return [{ label: '首页' }]
  const items: { label: string }[] = []
  const seg = route.path.split('/')[1]
  if (sectionMap[seg]) items.push({ label: sectionMap[seg] })
  const title = route.meta?.title as string
  if (title) items.push({ label: title })
  return items.length ? items : [{ label: '首页' }]
})
const hideBreadcrumb = computed(() => route.meta.hideBreadcrumb === true || breadcrumbItems.value.length === 0)
function closeSidebar() { sidebarOpen.value = false }

// 桌面侧栏：展开 / 图标窄栏（移动端忽略）
const RAIL_KEY = 'sidebar-rail-collapsed'
const sidebarRailPref = ref(localStorage.getItem(RAIL_KEY) === '1')
const isDesktopNav = ref(typeof window !== 'undefined' ? window.matchMedia('(min-width: 769px)').matches : true)
let desktopNavMql: MediaQueryList | null = null
function onDesktopNavChange(e: MediaQueryListEvent) {
  isDesktopNav.value = e.matches
}
const sidebarRail = computed(() => sidebarRailPref.value && isDesktopNav.value)
function toggleSidebarRail() {
  sidebarRailPref.value = !sidebarRailPref.value
  localStorage.setItem(RAIL_KEY, sidebarRailPref.value ? '1' : '0')
}

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

function sectionItemsVisible(key: string) {
  return sidebarRail.value || !collapsedSections.value.has(key)
}

// 主题切换
const isDark = ref(document.documentElement.getAttribute('data-theme') === 'dark')
function toggleTheme() {
  isDark.value = !isDark.value
  const theme = isDark.value ? 'dark' : 'light'
  document.documentElement.setAttribute('data-theme', theme)
  localStorage.setItem('theme-preference', theme)
  window.__themeUserOverride = true
  const appConfig = themeStore.appearanceConfig
  if (appConfig) {
    themeStore.saveAppearanceConfig({
      ...appConfig,
      theme: theme as AppearanceConfig['theme'],
    })
  }
}

function setAccent(key: string) {
  document.documentElement.setAttribute('data-accent', key)
  localStorage.setItem('accent-preference', key)
  const appConfig = themeStore.appearanceConfig
  if (appConfig) {
    themeStore.saveAppearanceConfig({
      ...appConfig,
      accent: key as AppearanceConfig['accent'],
    })
  }
}

onMounted(() => {
  const saved = localStorage.getItem('accent-preference')
  if (saved) setAccent(saved)
  if (!layoutStore.loaded) layoutStore.fetchLayout()
  desktopNavMql = window.matchMedia('(min-width: 769px)')
  isDesktopNav.value = desktopNavMql.matches
  desktopNavMql.addEventListener('change', onDesktopNavChange)
})

onUnmounted(() => {
  desktopNavMql?.removeEventListener('change', onDesktopNavChange)
})
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
          <span class="brand-text">⚡ Personal Hub</span>
        </router-link>
      </div>
      <div class="topbar-center">
        <UiTooltip content="Ctrl+K 打开命令面板" placement="bottom" :show-after="400">
          <div class="search-trigger" @click="openCommandPalette">
            <Search :size="14" />
            <span class="search-hint">搜索所有内容...</span>
            <kbd class="search-kbd">Ctrl+K</kbd>
          </div>
        </UiTooltip>
      </div>

      <div class="topbar-actions">
        <!-- 当前日期 -->
        <span class="topbar-date">{{ todayStr }}</span>

        <!-- 主题切换 -->
        <UiTooltip :content="isDark ? '浅色模式' : '深色模式'" placement="bottom">
          <button class="topbar-icon-btn" @click="toggleTheme">
            <Sun v-if="isDark" :size="18" />
            <Moon v-else :size="18" />
          </button>
        </UiTooltip>

        <!-- 通知 -->
        <NotificationBell />

        <!-- 用户头像 -->
        <UiTooltip :content="userName || '个人资料'" placement="bottom">
          <button class="topbar-avatar-btn" @click="profileDrawerOpen = true">
            <img v-if="authStore.user?.avatar" :src="authStore.user.avatar" class="avatar-btn-img" alt="" />
            <span v-else class="avatar-btn-text">{{ avatarInitial }}</span>
          </button>
        </UiTooltip>
      </div>

    </header>

    <!-- 面包屑导航 -->
    <div v-if="!hideBreadcrumb" class="header-breadcrumb">
      <template v-for="(item, idx) in breadcrumbItems" :key="idx">
        <span v-if="idx > 0" class="breadcrumb-sep">/</span>
        <span class="breadcrumb-item">{{ item.label }}</span>
      </template>
    </div>

    <div class="app-body">
      <!-- 侧边栏遮罩（移动端） -->
      <div class="sidebar-overlay" :class="{ open: sidebarOpen }" @click="closeSidebar" />

      <!-- 侧边栏 -->
      <aside class="sidebar" :class="{ open: sidebarOpen, rail: sidebarRail }">
        <div class="sidebar-header">
          <span class="sidebar-title">导航</span>
          <button class="sidebar-close" @click="closeSidebar"><X :size="18" /></button>
        </div>
        <nav class="sidebar-nav">
          <div class="sidebar-section" v-for="section in layoutStore.visibleMenuSections" :key="section.key">
            <div
              v-if="!sidebarRail"
              class="section-header"
              :class="{ collapsed: collapsedSections.has(section.key) }"
              @click="toggleSection(section.key)"
            >
              <span class="section-title">{{ section.title }}</span>
              <ChevronDown :size="14" class="section-chevron" />
            </div>
            <Transition name="section-collapse">
              <div v-if="sectionItemsVisible(section.key)" class="section-items">
                <UiTooltip
                  v-for="item in section.items"
                  :key="item.code"
                  :content="item.title"
                  placement="right"
                  :disabled="!sidebarRail"
                  :show-after="200"
                >
                  <router-link
                    :to="item.route || '/'"
                    class="nav-item"
                    :class="{ active: isActiveRoute(item), 'nav-item--secondary': section.key !== 'workspace', 'nav-item--primary': section.key === 'workspace' }"
                  >
                    <component :is="iconMap[item.code] || LayoutDashboard" :size="18" />
                    <span class="nav-item-label">{{ item.title }}</span>
                  </router-link>
                </UiTooltip>
              </div>
            </Transition>
          </div>
        </nav>
        <div class="sidebar-footer">
          <UiTooltip
            :content="authStore.user?.nickname || authStore.user?.username || ''"
            placement="right"
            :disabled="!sidebarRail"
          >
            <div class="sidebar-user">
              <div class="sidebar-avatar">
                {{ (authStore.user?.nickname || authStore.user?.username || '?')[0] }}
              </div>
              <span class="sidebar-user-name">{{ authStore.user?.nickname || authStore.user?.username }}</span>
            </div>
          </UiTooltip>
          <div class="sidebar-footer-actions">
            <UiTooltip :content="sidebarRailPref ? '展开侧栏' : '收起侧栏'" :placement="sidebarRail ? 'right' : 'top'">
              <button
                type="button"
                class="sidebar-rail-toggle"
                @click="toggleSidebarRail"
              >
                <PanelLeftOpen v-if="sidebarRailPref" :size="16" />
                <PanelLeftClose v-else :size="16" />
              </button>
            </UiTooltip>
            <UiTooltip content="退出登录" :placement="sidebarRail ? 'right' : 'top'">
              <button class="sidebar-logout" @click="authStore.logout()">
                <LogOut :size="16" />
              </button>
            </UiTooltip>
          </div>
        </div>
      </aside>

      <!-- 主内容 -->
      <main class="main-content">
        <div class="content-container">
          <router-view />
        </div>
      </main>
    </div>
    <CommandPalette ref="commandPaletteRef" />

    <!-- 个人资料抽屉 -->
    <ProfileDrawer v-model:visible="profileDrawerOpen" />
  </div>
</template>

<style scoped>
/* ============ 布局容器 ============ */
.app-shell { display: flex; flex-direction: column; height: 100vh; overflow: hidden; }

/* ============ 顶部栏 ============ */
.topbar {
  height: 48px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 16px;
  border-bottom: 1px solid color-mix(in srgb, var(--border-color) 30%, transparent);
  background: var(--bg-card);
  flex-shrink: 0;
  z-index: 100;
}
.topbar-left { display: flex; align-items: center; gap: var(--sp-2); }
.hamburger {
  display: none; width: 28px; height: 28px;
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

.topbar-center { flex: 1; display: flex; justify-content: center; max-width: 480px; margin: 0 auto; }
.search-trigger {
  display: flex; align-items: center; gap: var(--sp-2);
  padding: 5px 12px; width: 100%;
  border-radius: var(--radius-lg);
  border: 1px solid var(--border-color);
  color: var(--text-tertiary); font-size: var(--text-sm);
  cursor: pointer; transition: all var(--transition);
  background: var(--bg-body);
}
.search-trigger:hover { border-color: var(--accent); color: var(--text-secondary); background: var(--bg-card); }
.search-hint { color: var(--text-placeholder); }
.search-kbd { margin-left: auto; font-size: 10px; padding: 1px 5px; border-radius: var(--radius-sm); background: var(--bg-hover); color: var(--text-tertiary); border: 1px solid var(--border-color); font-family: var(--font-mono); }

.topbar-actions { display: flex; align-items: center; gap: var(--sp-2); margin: 0 var(--sp-4); }
.topbar-date { font-size: 12px; color: var(--text-tertiary); white-space: nowrap; user-select: none; }
.topbar-icon-btn {
  width: 28px; height: 28px;
  display: flex; align-items: center; justify-content: center;
  border-radius: var(--radius-sm); border: none;
  background: transparent; color: var(--text-secondary);
  cursor: pointer; transition: all var(--transition);
}
.topbar-icon-btn:hover { background: var(--bg-hover); color: var(--text-primary); }
.quick-create-btn { gap: 4px; height: 28px; font-size: 13px; }

/* ─── 面包屑导航 ─── */
.header-breadcrumb {
  display: flex;
  align-items: center;
  height: 32px;
  padding: 0 24px;
  gap: 6px;
  background: var(--bg-card);
  border-bottom: 1px solid color-mix(in srgb, var(--border-color) 20%, transparent);
  flex-shrink: 0;
}
.breadcrumb-item {
  font-size: 12px;
  color: var(--text-tertiary);
  line-height: 1;
}
.breadcrumb-sep {
  font-size: 12px;
  color: var(--border-color);
  line-height: 1;
}
.breadcrumb-item:last-child {
  color: var(--text-secondary);
  font-weight: 500;
}

/* ─── 用户头像（右上角） ─── */
.topbar-avatar-btn {
  width: 28px; height: 28px; border-radius: 50%; overflow: hidden; cursor: pointer; flex-shrink: 0;
  background: var(--accent-light); border: none; padding: 0;
  display: flex; align-items: center; justify-content: center;
  transition: opacity var(--transition);
}
.topbar-avatar-btn:hover { opacity: 0.8; }
.avatar-btn-img { width: 100%; height: 100%; object-fit: cover; }
.avatar-btn-text { font-size: 12px; font-weight: 600; color: var(--accent); }

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
  transition: width var(--transition-duration) ease;
}
.sidebar.rail {
  width: var(--sidebar-rail-width, 56px);
}
.sidebar-header { display: none; }
.sidebar-nav {
  display: flex; flex-direction: column;
  flex: 1; overflow-y: auto; padding: var(--sp-4) 0;
}
.sidebar.rail .sidebar-nav {
  padding: var(--sp-3) 0;
  align-items: stretch;
}

.sidebar-section { margin-bottom: var(--sp-3); }
.sidebar-section:last-child { margin-bottom: 0; }
.sidebar-section + .sidebar-section {
  margin-top: var(--sp-2);
  padding-top: var(--sp-2);
  border-top: 1px solid color-mix(in srgb, var(--border-color) 35%, transparent);
}
.sidebar.rail .sidebar-section {
  margin-bottom: var(--sp-1);
}
.sidebar.rail .sidebar-section + .sidebar-section {
  margin-top: var(--sp-1);
  padding-top: var(--sp-1);
}
.section-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 8px;
  padding: 6px 18px 8px;
  user-select: none;
  cursor: pointer;
  transition: color var(--transition-duration) ease;
}
.section-title {
  font-size: 11px;
  font-weight: 600;
  line-height: 1;
  letter-spacing: 0.12em;
  color: var(--text-placeholder);
}
.section-header:hover .section-title { color: var(--text-tertiary); }
.section-chevron {
  flex-shrink: 0;
  color: var(--text-placeholder);
  opacity: 0.7;
  transition: transform var(--transition-duration) ease, opacity var(--transition-duration) ease;
}
.section-header:hover .section-chevron { opacity: 1; color: var(--text-tertiary); }
.section-header.collapsed .section-chevron { transform: rotate(-90deg); }
.section-items { overflow: hidden; }

/* 菜单项基础 */
.nav-item {
  display: flex; align-items: center; gap: var(--sp-3);
  position: relative;
  height: 38px; padding: 0 var(--sp-4); margin: 0 var(--sp-2) 1px;
  border-radius: var(--radius-md);
  font-size: 14px; font-weight: 500; color: var(--text-secondary); text-decoration: none;
  transition: background var(--transition-duration) ease, color var(--transition-duration) ease;
}
.sidebar.rail .section-items {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 2px;
  padding: 0 6px;
}
.sidebar.rail .section-items > *,
.sidebar.rail .section-items :deep(.el-tooltip__trigger),
.sidebar.rail .section-items :deep(.el-only-child__content) {
  display: flex !important;
  justify-content: center;
  align-items: center;
  width: 100% !important;
  max-width: 100%;
  box-sizing: border-box;
}
.sidebar.rail .nav-item {
  justify-content: center;
  align-items: center;
  gap: 0;
  width: 40px;
  height: 40px;
  margin: 0;
  padding: 0 !important;
  border-radius: var(--radius-md);
}
.sidebar.rail .section-items > * .nav-item {
  width: 40px;
  margin: 0;
}
.sidebar.rail .nav-item-label {
  display: none;
}
.nav-item:hover { background: var(--bg-hover); color: var(--text-primary); }
.nav-item:hover :deep(svg) { color: var(--text-primary); }

/* 首页 — 更突出 */
.nav-item--primary {
  font-weight: 600;
  color: var(--text-primary);
}

/* 二级菜单 — 可点击项，明显高于分组标签 */
.nav-item--secondary {
  font-weight: 500;
  color: var(--text-secondary);
}
.nav-item--secondary :deep(svg) {
  width: 16px; height: 16px; opacity: 0.72;
  color: var(--text-secondary);
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
  border-radius: 0 var(--radius-sm) var(--radius-sm) 0;
  opacity: 0.5;
}
.sidebar.rail .nav-item.active::before {
  left: 0;
  transform: translateY(-50%);
  height: 18px;
  border-radius: 0 var(--radius-sm) var(--radius-sm) 0;
}
.sidebar.rail .nav-item--secondary :deep(svg) {
  width: 18px;
  height: 18px;
}

/* 侧边栏底部用户信息 */
.sidebar-footer {
  display: flex; align-items: center; justify-content: space-between;
  gap: 4px;
  padding: var(--sp-3) var(--sp-4);
  border-top: 1px solid color-mix(in srgb, var(--border-color) 25%, transparent);
}
.sidebar.rail .sidebar-footer {
  flex-direction: column;
  align-items: center;
  padding: var(--sp-2) 6px;
  gap: 6px;
}
.sidebar-user {
  display: flex; align-items: center; gap: var(--sp-2);
  overflow: hidden;
  min-width: 0;
}
.sidebar.rail .sidebar-user,
.sidebar.rail .sidebar-footer > :deep(.el-tooltip__trigger),
.sidebar.rail .sidebar-footer > :deep(.el-only-child__content) {
  display: flex !important;
  justify-content: center;
  width: 100%;
}
.sidebar.rail .sidebar-user-name {
  display: none;
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
.sidebar-footer-actions {
  display: flex;
  align-items: center;
  gap: 2px;
  flex-shrink: 0;
}
.sidebar.rail .sidebar-footer-actions {
  flex-direction: column;
  align-items: center;
  width: 100%;
  gap: 4px;
}
.sidebar.rail .sidebar-footer-actions > *,
.sidebar.rail .sidebar-footer-actions :deep(.el-tooltip__trigger),
.sidebar.rail .sidebar-footer-actions :deep(.el-only-child__content) {
  display: flex !important;
  justify-content: center;
  width: 100%;
}
.sidebar-rail-toggle,
.sidebar-logout {
  width: 28px; height: 28px; flex-shrink: 0;
  display: flex; align-items: center; justify-content: center;
  background: none; border: none; color: var(--text-tertiary);
  cursor: pointer; border-radius: var(--radius-sm);
  transition: all var(--transition);
}
.sidebar-rail-toggle:hover,
.sidebar-logout:hover { background: var(--bg-hover); color: var(--text-primary); }
.sidebar-logout:hover { color: var(--danger); }
.sidebar.rail .sidebar-rail-toggle,
.sidebar.rail .sidebar-logout {
  width: 40px;
  height: 36px;
  margin: 0 auto;
}
.section-collapse-enter-active,
.section-collapse-leave-active {
  transition: max-height var(--transition-duration) ease, opacity var(--transition-duration) ease;
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
/* 学习计划等「一屏内」页面：主区不滚动，由页面内部消化高度 */
.main-content.main-content--fill {
  overflow: hidden;
  display: flex;
  flex-direction: column;
}
.main-content.main-content--fill .content-container {
  flex: 1;
  min-height: 0;
  display: flex;
  flex-direction: column;
  /* 与普通页一致：保留 max-width + 居中，仅放开高度链 */
}
.main-content.main-content--fill .content-container > * {
  flex: 1;
  min-height: 0;
  width: 100%;
}

/* 笔记编辑：取消 80% 内容限宽与主区 padding，写作区贴满主栏 */
.main-content.main-content--editor {
  overflow: hidden;
  display: flex;
  flex-direction: column;
  padding: 0;
}
.main-content.main-content--editor .content-container {
  max-width: none;
  flex: 1;
  min-height: 0;
  display: flex;
  flex-direction: column;
}
.main-content.main-content--editor .content-container > * {
  flex: 1;
  min-height: 0;
  width: 100%;
}

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
  .brand-text { font-size: 14px; }
  .main-content { padding: var(--sp-4); }
  .header-breadcrumb { padding: 0 var(--sp-3); height: 28px; }

  .sidebar-overlay { display: block; opacity: 0; visibility: hidden; transition: all var(--transition); }
  .sidebar-overlay.open { opacity: 1; visibility: visible; }

  .sidebar {
    position: fixed; top: 0; left: 0; bottom: 0; z-index: 95;
    transform: translateX(-100%); transition: transform var(--transition-duration) ease;
    padding-top: 0;
    width: var(--sidebar-width) !important;
  }
  .sidebar.rail {
    width: var(--sidebar-width) !important;
  }
  .sidebar.rail .nav-item {
    justify-content: flex-start;
    gap: var(--sp-3);
    width: auto;
    height: 38px;
    padding: 0 var(--sp-4) !important;
    margin: 0 var(--sp-2) 1px;
  }
  .sidebar.rail .nav-item-label,
  .sidebar.rail .sidebar-user-name {
    display: initial;
  }
  .sidebar.rail .sidebar-footer {
    flex-direction: row;
    padding: var(--sp-3) var(--sp-4);
  }
  .sidebar.rail .sidebar-footer-actions {
    flex-direction: row;
    width: auto;
  }
  .sidebar.rail .sidebar-rail-toggle {
    display: none;
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
