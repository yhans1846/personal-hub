import { createRouter, createWebHistory } from 'vue-router'

const router = createRouter({
  history: createWebHistory(),
  routes: [
    {
      path: '/login',
      name: 'Login',
      meta: { title: '登录' },
      component: () => import('@/modules/system/login/Login.vue')
    },
    {
      path: '/notes/:id/preview',
      name: 'NotePreview',
      meta: { title: '预览' },
      component: () => import('@/modules/knowledge/note/Preview.vue'),
    },
    {
      path: '/',
      component: () => import('@/components/AppLayout.vue'),
      redirect: '/dashboard',
      children: [
        { path: 'dashboard', name: 'Dashboard', meta: { title: '工作台' }, component: () => import('@/modules/dashboard/Dashboard.vue') },
        { path: 'notes', name: 'NoteList', meta: { title: '笔记列表' }, component: () => import('@/modules/knowledge/note/List.vue') },
        { path: 'notes/new', name: 'NoteCreate', meta: { title: '新建笔记' }, component: () => import('@/modules/knowledge/note/Editor.vue') },
        { path: 'notes/:id/edit', name: 'NoteEdit', meta: { title: '编辑笔记' }, component: () => import('@/modules/knowledge/note/Editor.vue') },
        { path: 'categories', name: 'CategoryManage', meta: { title: '分类管理' }, component: () => import('@/modules/knowledge/category/CategoryManage.vue') },
        { path: 'tags', name: 'TagManage', meta: { title: '标签管理' }, component: () => import('@/modules/knowledge/tag/Manage.vue') },
        { path: 'stats', name: 'Stats', meta: { title: '统计' }, component: () => import('@/modules/stats/StatsView.vue') },
        { path: 'search', name: 'Search', meta: { title: '搜索' }, component: () => import('@/modules/search/SearchView.vue') },
        { path: 'notes/recycle', name: 'RecycleBin', meta: { title: '回收站' }, component: () => import('@/modules/knowledge/note/RecycleBin.vue') },
        { path: 'todos', name: 'TodoList', meta: { title: '待办' }, component: () => import('@/modules/planning/todo/List.vue') },
        { path: 'todos/:id/edit', redirect: (to) => ({ path: '/todos', query: { edit: String(to.params.id) } }) },
        { path: 'diaries', name: 'DiaryList', meta: { title: '日记' }, component: () => import('@/modules/knowledge/diary/List.vue') },
        { path: 'diaries/new', redirect: { path: '/diaries', query: { create: '1' } } },
        { path: 'bookmarks', name: 'BookmarkList', meta: { title: '收藏' }, component: () => import('@/modules/resource/bookmark/List.vue') },
        { path: 'study-plans', name: 'StudyPlanList', meta: { title: '学习计划' }, component: () => import('@/modules/planning/studyplan/List.vue') },
        { path: 'study-plans/:id/edit', redirect: (to) => ({ path: '/study-plans', query: { edit: String(to.params.id) } }) },
        { path: 'readings', name: 'ReadingList', meta: { title: '阅读记录' }, component: () => import('@/modules/knowledge/reading/List.vue') },
        { path: 'readings/:id/edit', redirect: (to) => ({ path: '/readings', query: { edit: String(to.params.id) } }) },
        { path: 'study-records', name: 'StudyRecords', meta: { title: '学习记录' }, component: () => import('@/modules/knowledge/study/List.vue') },
        { path: 'study-records/new', redirect: { path: '/study-records', query: { create: '1' } } },
        { path: 'study-records/:id/edit', redirect: (to) => ({ path: '/study-records', query: { edit: String(to.params.id) } }) },
        { path: 'files', name: 'FileList', meta: { title: '文件管理' }, component: () => import('@/modules/resource/file/List.vue') },
        { path: 'settings', name: 'Settings', meta: { title: '系统设置' }, component: () => import('@/modules/system/settings/SettingsView.vue') }
      ]
    }
  ]
})

/** 路由守卫：未登录跳转登录页 + 动态标题 */
router.beforeEach((to, _from, next) => {
  // 设置页面标题
  const pageTitle = (to.meta?.title as string) || ''
  document.title = pageTitle ? `${pageTitle} | Personal Hub` : 'Personal Hub'

  const token = localStorage.getItem('token')
  if (to.path !== '/login' && !token) {
    next('/login')
  } else if (to.path === '/login' && token) {
    next('/')
  } else {
    next()
  }
})

export default router
