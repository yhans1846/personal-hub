import type { MenuItem, CardItem } from '@/types/layout'

/** 默认菜单配置（与改造前硬编码状态一致） */
export const DEFAULT_MENU_ITEMS: MenuItem[] = [
  // ===== 工作区 =====
  { code: 'dashboard', title: '首页', section: 'workspace', route: '/dashboard', order: 1, visible: true, fixed: true },
  { code: 'notes', title: '笔记', section: 'workspace', route: '/notes', order: 2, visible: true },
  { code: 'study-records', title: '学习记录', section: 'workspace', route: '/study-records', order: 3, visible: true },
  { code: 'todos', title: '待办任务', section: 'workspace', route: '/todos', order: 4, visible: true },
  { code: 'diaries', title: '日记', section: 'workspace', route: '/diaries', order: 5, visible: true },
  { code: 'bookmarks', title: '收藏夹', section: 'workspace', route: '/bookmarks', order: 6, visible: true },
  { code: 'study-plans', title: '学习计划', section: 'workspace', route: '/study-plans', order: 7, visible: true },
  { code: 'readings', title: '阅读记录', section: 'workspace', route: '/readings', order: 8, visible: true },
  { code: 'files', title: '文件', section: 'workspace', route: '/files', order: 9, visible: true },
  // ===== 管理 =====
  { code: 'categories', title: '分类管理', section: 'manage', route: '/notes/categories', order: 10, visible: true },
  { code: 'tags', title: '标签管理', section: 'manage', route: '/tags', order: 11, visible: true },
  { code: 'settings', title: '系统设置', section: 'manage', route: '/settings', order: 14, visible: true },
  { code: 'recycle', title: '回收站', section: 'manage', route: '/notes/recycle', order: 15, visible: true },
  // ===== 统计 =====
  { code: 'stats', title: '数据统计', section: 'stats', route: '/stats', order: 16, visible: true },
]

/** 默认 Dashboard 卡片配置 */
export const DEFAULT_DASHBOARD_ITEMS: CardItem[] = [
  { code: 'today_plan', title: '今日计划', order: 1, visible: true },
  { code: 'recent_reading', title: '最近阅读', order: 2, visible: true },
  { code: 'pending_todos', title: '待办任务', order: 3, visible: true },
  { code: 'recent_notes', title: '最近更新', order: 4, visible: true },
  { code: 'recent_bookmarks', title: '最近收藏', order: 5, visible: true },
  { code: 'recent_studies', title: '学习记录', order: 6, visible: true },
]
