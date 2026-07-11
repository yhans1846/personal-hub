import { createRouter, createWebHistory } from 'vue-router'

const router = createRouter({
  history: createWebHistory(),
  routes: [
    {
      path: '/login',
      name: 'Login',
      component: () => import('@/modules/system/login/Login.vue')
    },
    {
      path: '/',
      component: () => import('@/components/AppLayout.vue'),
      redirect: '/dashboard',
      children: [
        { path: 'dashboard', name: 'Dashboard', component: () => import('@/modules/dashboard/Dashboard.vue') },
        { path: 'notes', name: 'NoteList', component: () => import('@/modules/knowledge/note/List.vue') },
        { path: 'notes/new', name: 'NoteCreate', component: () => import('@/modules/knowledge/note/Editor.vue') },
        { path: 'notes/:id/edit', name: 'NoteEdit', component: () => import('@/modules/knowledge/note/Editor.vue') },
        { path: 'notes/categories', name: 'NoteCategories', component: () => import('@/modules/knowledge/note/CategoryManage.vue') },
        { path: 'notes/tags', name: 'NoteTags', component: () => import('@/modules/knowledge/tag/Manage.vue') },
        { path: 'tags', name: 'TagManage', component: () => import('@/modules/knowledge/tag/Manage.vue') },
        { path: 'stats', name: 'Stats', component: () => import('@/modules/stats/StatsView.vue') },
        { path: 'search', name: 'Search', component: () => import('@/modules/search/SearchView.vue') },
        { path: 'notes/recycle', name: 'RecycleBin', component: () => import('@/modules/knowledge/note/RecycleBin.vue') },
        { path: 'study-records', name: 'StudyRecords', component: () => import('@/modules/knowledge/study/List.vue') },
        { path: 'study-records/new', name: 'StudyRecordCreate', component: () => import('@/modules/knowledge/study/Form.vue') },
        { path: 'study-records/:id/edit', name: 'StudyRecordEdit', component: () => import('@/modules/knowledge/study/Form.vue') },
        { path: 'todos', name: 'TodoList', component: () => import('@/modules/planning/todo/List.vue') },
        { path: 'todos/new', name: 'TodoCreate', component: () => import('@/modules/planning/todo/Form.vue') },
        { path: 'todos/:id/edit', name: 'TodoEdit', component: () => import('@/modules/planning/todo/Form.vue') },
        { path: 'diaries', name: 'DiaryList', component: () => import('@/modules/knowledge/diary/List.vue') },
        { path: 'diaries/new', name: 'DiaryCreate', component: () => import('@/modules/knowledge/diary/Form.vue') },
        { path: 'diaries/:id/edit', name: 'DiaryEdit', component: () => import('@/modules/knowledge/diary/Form.vue') },
        { path: 'bookmarks', name: 'BookmarkList', component: () => import('@/modules/resource/bookmark/List.vue') },
        { path: 'bookmarks/new', name: 'BookmarkCreate', component: () => import('@/modules/resource/bookmark/Form.vue') },
        { path: 'bookmarks/:id/edit', name: 'BookmarkEdit', component: () => import('@/modules/resource/bookmark/Form.vue') },
        { path: 'bookmarks/categories', name: 'BookmarkCategories', component: () => import('@/modules/resource/bookmark/CategoryManage.vue') },
        { path: 'study-plans', name: 'StudyPlanList', component: () => import('@/modules/planning/studyplan/List.vue') },
        { path: 'study-plans/new', name: 'StudyPlanCreate', component: () => import('@/modules/planning/studyplan/Form.vue') },
        { path: 'study-plans/:id/edit', name: 'StudyPlanEdit', component: () => import('@/modules/planning/studyplan/Form.vue') },
        { path: 'readings', name: 'ReadingList', component: () => import('@/modules/knowledge/reading/List.vue') },
        { path: 'readings/new', name: 'ReadingCreate', component: () => import('@/modules/knowledge/reading/Form.vue') },
        { path: 'readings/:id/edit', name: 'ReadingEdit', component: () => import('@/modules/knowledge/reading/Form.vue') },
        { path: 'files', name: 'FileList', component: () => import('@/modules/resource/file/List.vue') },
        { path: 'files/categories', name: 'FileCategories', component: () => import('@/modules/resource/file/CategoryManage.vue') }
      ]
    }
  ]
})

/** 路由守卫：未登录跳转登录页 */
router.beforeEach((to, _from, next) => {
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
