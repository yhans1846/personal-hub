<script setup lang="ts">
import { useAuthStore } from '@/store/authStore'
import { useRouter } from 'vue-router'
import { LayoutDashboard, FileText, BookOpen, CheckSquare, PenLine, Bookmark, Target, BookMarked, FolderOpen, Grid3X3, Tags, Trash2, Search, BarChart3, Plus, Github, Sun, Moon, Bell, Palette } from 'lucide-vue-next'
import { ref, onMounted } from 'vue'

const authStore = useAuthStore()
const router = useRouter()

// 主题切换
const isDark = ref(document.documentElement.getAttribute('data-theme') === 'dark')
function toggleTheme() {
  isDark.value = !isDark.value
  const theme = isDark.value ? 'dark' : 'light'
  document.documentElement.setAttribute('data-theme', theme)
  localStorage.setItem('theme-preference', theme)
  ;(window as any).__themeUserOverride = true
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
}

onMounted(() => {
  const saved = localStorage.getItem('accent-preference')
  if (saved) setAccent(saved)
})

// 快捷创建
function handleQuickCreate(cmd: string) {
  const routes: Record<string, string> = {
    note: '/notes/new',
    todo: '/todos/new',
    diary: '/diaries/new',
    bookmark: '/bookmarks/new',
    'study-record': '/study-records/new'
  }
  router.push(routes[cmd] || '/notes/new')
}
</script>

<template>
  <div class="app-shell">
    <!-- 顶部栏 -->
    <header class="topbar">
      <div class="topbar-left">
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
        <div class="search-trigger" @click="router.push('/search')">
          <Search :size="14" />
          <span class="search-hint">搜索...</span>
        </div>
      </div>

      <div class="topbar-actions">
        <!-- 快捷创建 -->
        <el-dropdown trigger="click" @command="handleQuickCreate">
          <el-button type="primary" size="small" class="quick-create-btn">
            <Plus :size="16" />
            <span>新建</span>
          </el-button>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item command="note">📝 笔记</el-dropdown-item>
              <el-dropdown-item command="todo">✅ 待办</el-dropdown-item>
              <el-dropdown-item command="diary">📓 日记</el-dropdown-item>
              <el-dropdown-item command="bookmark">🔖 收藏</el-dropdown-item>
              <el-dropdown-item command="study-record">📚 学习记录</el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>

        <!-- GitHub -->
        <a href="https://github.com" target="_blank" class="topbar-icon-btn" title="GitHub">
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
        <button class="topbar-icon-btn" title="通知">
          <Bell :size="18" />
        </button>
      </div>

      <div class="topbar-right">
        <span class="user-name">{{ authStore.user?.nickname || authStore.user?.username }}</span>
        <el-dropdown trigger="click" @command="(cmd: string) => cmd === 'logout' && authStore.logout()">
          <div class="avatar-circle">
            {{ (authStore.user?.nickname || authStore.user?.username || '?')[0] }}
          </div>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item command="profile">个人信息</el-dropdown-item>
              <el-dropdown-item command="logout">退出登录</el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
      </div>
    </header>

    <div class="app-body">
      <!-- 侧边栏 -->
      <aside class="sidebar">
        <nav class="sidebar-nav">
          <div class="sidebar-section">
            <div class="section-header">工作区</div>
            <router-link to="/dashboard" class="nav-item" :class="{ active: $route.path === '/dashboard' }">
              <LayoutDashboard :size="18" />
              <span>首页</span>
            </router-link>

            <router-link to="/notes" class="nav-item" :class="{ active: $route.path.startsWith('/notes') && !$route.path.includes('/categories') && !$route.path.includes('/tags') && !$route.path.includes('/recycle') }">
              <FileText :size="18" />
              <span>笔记</span>
            </router-link>

            <router-link to="/study-records" class="nav-item" :class="{ active: $route.path.startsWith('/study-records') }">
              <BookOpen :size="18" />
              <span>学习记录</span>
            </router-link>

            <router-link to="/todos" class="nav-item" :class="{ active: $route.path.startsWith('/todos') }">
              <CheckSquare :size="18" />
              <span>待办任务</span>
            </router-link>

            <router-link to="/diaries" class="nav-item" :class="{ active: $route.path.startsWith('/diaries') }">
              <PenLine :size="18" />
              <span>日记</span>
            </router-link>

            <router-link to="/bookmarks" class="nav-item" :class="{ active: $route.path.startsWith('/bookmarks') && $route.path !== '/bookmarks/categories' }">
              <Bookmark :size="18" />
              <span>收藏夹</span>
            </router-link>

            <router-link to="/study-plans" class="nav-item" :class="{ active: $route.path.startsWith('/study-plans') }">
              <Target :size="18" />
              <span>学习计划</span>
            </router-link>

            <router-link to="/readings" class="nav-item" :class="{ active: $route.path.startsWith('/readings') }">
              <BookMarked :size="18" />
              <span>阅读记录</span>
            </router-link>

            <router-link to="/files" class="nav-item" :class="{ active: $route.path.startsWith('/files') && $route.path !== '/files/categories' }">
              <FolderOpen :size="18" />
              <span>文件</span>
            </router-link>
          </div>

          <div class="sidebar-section">
            <div class="section-header">管理</div>
            <router-link to="/notes/categories" class="nav-item nav-item--sub" :class="{ active: $route.path === '/notes/categories' }">
              <Grid3X3 :size="14" />
              <span>分类管理</span>
            </router-link>

            <router-link to="/tags" class="nav-item nav-item--sub" :class="{ active: $route.path === '/tags' }">
              <Tags :size="14" />
              <span>标签管理</span>
            </router-link>

            <router-link to="/files/categories" class="nav-item nav-item--sub" :class="{ active: $route.path === '/files/categories' }">
              <Grid3X3 :size="14" />
              <span>文件分类</span>
            </router-link>

            <router-link to="/bookmarks/categories" class="nav-item nav-item--sub" :class="{ active: $route.path === '/bookmarks/categories' }">
              <Grid3X3 :size="14" />
              <span>收藏夹分类</span>
            </router-link>

            <router-link to="/notes/recycle" class="nav-item nav-item--sub" :class="{ active: $route.path === '/notes/recycle' }">
              <Trash2 :size="14" />
              <span>回收站</span>
            </router-link>
          </div>

          <div class="sidebar-section">
            <div class="section-header">统计</div>
            <router-link to="/stats" class="nav-item nav-item--sub" :class="{ active: $route.path === '/stats' }">
              <BarChart3 :size="14" />
              <span>数据统计</span>
            </router-link>
          </div>
        </nav>
      </aside>

      <!-- 主内容 -->
      <main class="main-content">
        <div class="content-container">
          <router-view />
        </div>
      </main>
    </div>
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
  border-bottom: 1px solid var(--border-color);
  background: var(--bg-card);
  flex-shrink: 0;
  z-index: 100;
}
.topbar-left { display: flex; align-items: center; }
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

.topbar-right { display: flex; align-items: center; gap: var(--sp-3); }
.user-name { font-size: var(--text-sm); color: var(--text-secondary); }
.avatar-circle {
  width: 30px; height: 30px; border-radius: 50%;
  background: var(--accent-light); color: var(--accent);
  display: flex; align-items: center; justify-content: center;
  font-size: var(--text-sm); font-weight: 600; cursor: pointer; transition: background var(--transition);
}
.avatar-circle:hover { background: var(--accent-border); }

/* ============ 主体区域 ============ */
.app-body { display: flex; flex: 1; overflow: hidden; }

/* ============ 侧边栏 ============ */
.sidebar {
  width: var(--sidebar-width); background: var(--bg-sidebar);
  border-right: 1px solid var(--border-color);
  overflow-y: auto; flex-shrink: 0; padding: var(--sp-4) 0;
}
.sidebar-nav { display: flex; flex-direction: column; }

/* 侧边栏分组 */
.sidebar-section { margin-bottom: var(--sp-2); }
.section-header {
  padding: var(--sp-3) var(--sp-4) var(--sp-1) var(--sp-4);
  font-size: 11px; font-weight: 600;
  color: var(--text-tertiary);
  text-transform: uppercase; letter-spacing: 0.5px;
  user-select: none;
}

/* 导航项 */
.nav-item {
  display: flex; align-items: center; gap: var(--sp-3);
  height: 40px; padding: 0 var(--sp-4); margin: 0 var(--sp-2);
  border-radius: var(--radius-md);
  font-size: 14px; font-weight: 500; color: var(--text-secondary); text-decoration: none;
  transition: all var(--transition);
}
.nav-item:hover { background: var(--bg-hover); color: var(--text-primary); }
.nav-item.active {
  background: var(--accent); color: #fff; font-weight: 500;
}
.nav-item.active :deep(svg) { color: #fff; }
.nav-item--sub { font-size: 13px; color: var(--text-tertiary); font-weight: 400; padding-left: calc(var(--sp-4) + 24px); }
.nav-item--sub :deep(svg) { width: 14px; height: 14px; opacity: 0.6; }

/* ============ 主内容 ============ */
.main-content { flex: 1; overflow-y: auto; padding: var(--sp-8); }
.content-container { max-width: var(--content-max-width); margin: 0 auto; width: 100%; }
</style>
