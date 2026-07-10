<script setup lang="ts">
import { useAuthStore } from '@/store/authStore'
import { useRouter } from 'vue-router'
import { Layers, LayoutDashboard, FileText, BookOpen, CheckSquare, PenLine, Bookmark, Target, BookMarked, FolderOpen, Grid3X3, Tags, Trash2, Search } from 'lucide-vue-next'

const authStore = useAuthStore()
const router = useRouter()
</script>

<template>
  <div class="app-shell">
    <!-- 顶部栏 -->
    <header class="topbar">
      <div class="topbar-left">
        <router-link to="/" class="topbar-brand">
          <Layers :size="22" class="brand-icon" />
          <span class="brand-text">Personal Hub</span>
        </router-link>
      </div>
      <div class="topbar-center">
        <div class="search-trigger" @click="router.push('/search')">
          <Search :size="14" />
          <span class="search-hint">搜索...</span>
        </div>
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

          <div class="nav-divider" />

          <router-link to="/notes/categories" class="nav-item nav-item--sub" :class="{ active: $route.path === '/notes/categories' }">
            <Grid3X3 :size="14" />
            <span>分类管理</span>
          </router-link>

          <router-link to="/notes/tags" class="nav-item nav-item--sub" :class="{ active: $route.path === '/notes/tags' }">
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
.brand-icon { color: var(--accent); }

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
  overflow-y: auto; flex-shrink: 0; padding: var(--sp-3) 0;
}
.sidebar-nav { display: flex; flex-direction: column; gap: 1px; }
.nav-item {
  display: flex; align-items: center; gap: var(--sp-3);
  padding: 8px var(--sp-4); margin: 0 var(--sp-2);
  border-radius: var(--radius-sm);
  font-size: var(--text-sm); color: var(--text-secondary); text-decoration: none;
  transition: all var(--transition);
}
.nav-item:hover { background: var(--bg-hover); color: var(--text-primary); }
.nav-item.active { background: var(--accent-light); color: var(--accent); font-weight: 500; }
.nav-item--sub { padding-left: calc(var(--sp-4) + 30px); font-size: var(--text-xs); }
.nav-item--sub :deep(svg) { width: 14px; height: 14px; opacity: 0.6; }
.nav-divider { height: 1px; background: var(--border-color); margin: var(--sp-2) var(--sp-4); }

/* ============ 主内容 ============ */
.main-content { flex: 1; overflow-y: auto; padding: var(--sp-8); }
.content-container { max-width: var(--content-max-width); margin: 0 auto; width: 100%; }
</style>
