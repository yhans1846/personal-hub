import { createRouter, createWebHistory } from 'vue-router'

const router = createRouter({
  history: createWebHistory(),
  routes: [
    {
      path: '/login',
      name: 'Login',
      component: () => import('@/views/Login.vue')
    },
    {
      path: '/',
      component: () => import('@/components/AppLayout.vue'),
      redirect: '/dashboard',
      children: [
        { path: 'dashboard', name: 'Dashboard', component: () => import('@/views/Dashboard.vue') },
        { path: 'notes', name: 'NoteList', component: () => import('@/views/notes/NoteList.vue') },
        { path: 'notes/new', name: 'NoteCreate', component: () => import('@/views/notes/NoteEditor.vue') },
        { path: 'notes/:id/edit', name: 'NoteEdit', component: () => import('@/views/notes/NoteEditor.vue') },
        { path: 'notes/categories', name: 'NoteCategories', component: () => import('@/views/notes/NoteCategoryManage.vue') },
        { path: 'notes/tags', name: 'NoteTags', component: () => import('@/views/notes/NoteTagManage.vue') },
        { path: 'notes/recycle', name: 'RecycleBin', component: () => import('@/views/notes/RecycleBin.vue') },
        { path: 'study-records', name: 'StudyRecords', component: () => import('@/views/study/StudyRecordList.vue') },
        { path: 'study-records/new', name: 'StudyRecordCreate', component: () => import('@/views/study/StudyRecordForm.vue') },
        { path: 'study-records/:id/edit', name: 'StudyRecordEdit', component: () => import('@/views/study/StudyRecordForm.vue') }
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
