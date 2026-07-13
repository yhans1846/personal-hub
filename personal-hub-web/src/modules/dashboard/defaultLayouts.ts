import type { MenuItem, CardItem } from '@/types/layout'

/** 默认菜单配置 - 保留 5 分组结构 */
export const DEFAULT_MENU_ITEMS: MenuItem[] = [
  // ===== 工作区 =====
  { code: 'dashboard', title: '首页', section: 'workspace', route: '/dashboard', order: 1, visible: true, fixed: true },
  // ===== 知识 =====
  { code: 'notes', title: '笔记', section: 'knowledge', route: '/notes', order: 2, visible: true },
  { code: 'diaries', title: '日记', section: 'knowledge', route: '/diaries', order: 3, visible: true },
  { code: 'readings', title: '阅读记录', section: 'knowledge', route: '/readings', order: 4, visible: true },
  { code: 'study-records', title: '学习记录', section: 'knowledge', route: '/study-records', order: 5, visible: true },
  // ===== 工作台 =====
  { code: 'todos', title: '待办任务', section: 'knowledge', route: '/todos', order: 6, visible: true },
  { code: 'study-plans', title: '学习计划', section: 'knowledge', route: '/study-plans', order: 7, visible: true },
  // ===== 资源 =====
  { code: 'bookmarks', title: '收藏夹', section: 'resource', route: '/bookmarks', order: 8, visible: true },
  { code: 'files', title: '文件管理', section: 'resource', route: '/files', order: 9, visible: true },
  // ===== 管理 =====
  { code: 'tags', title: '标签管理', section: 'manage', route: '/tags', order: 10, visible: true },
  { code: 'categories', title: '分类管理', section: 'manage', route: '/categories', order: 11, visible: true },
  { code: 'settings', title: '系统设置', section: 'manage', route: '/settings', order: 12, visible: true, fixed: true },
  { code: 'recycle', title: '回收站', section: 'manage', route: '/notes/recycle', order: 13, visible: true },
  // ===== 统计 =====
  { code: 'stats', title: '数据统计', section: 'stats', route: '/stats', order: 14, visible: true },
]

/** 默认 Dashboard 卡片配置 — 两栏布局 */
export const DEFAULT_DASHBOARD_ITEMS: CardItem[] = [
  { code: 'today_plan', title: '今日任务', order: 1, visible: true },
  { code: 'recent_notes', title: '最近编辑', order: 2, visible: true },
  { code: 'recent_studies', title: '学习记录', order: 3, visible: true },
  { code: 'recent_reading', title: '最近阅读', order: 4, visible: true },
  { code: 'pending_todos', title: '待办任务', order: 5, visible: false },
]

/** 统计页面卡片配置 */
export const DEFAULT_STATS_CARDS: CardItem[] = [
  { code: 'kpi', title: 'KPI 统计卡', order: 1, visible: true },
  { code: 'study-trend', title: '学习趋势', order: 2, visible: true },
  { code: 'note-trend', title: '笔记趋势', order: 3, visible: true },
  { code: 'todo-donut', title: 'Todo 完成率', order: 4, visible: true },
  { code: 'cat-tag', title: '分类/标签排行', order: 5, visible: true },
  { code: 'timeline', title: '最近活动', order: 6, visible: true },
  { code: 'insight', title: '学习洞察', order: 7, visible: true },
]
