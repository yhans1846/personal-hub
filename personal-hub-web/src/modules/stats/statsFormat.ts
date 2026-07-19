import { ArrowUp, ArrowDown, Minus } from 'lucide-vue-next'
import type { ActivityItem } from '@/modules/dashboard/api'

export function formatReadingHours(hours: number): string {
  if (hours < 1) return `${Math.round(hours * 60)} 分钟`
  return `${hours.toFixed(1)} 小时`
}

export function changeIcon(change: number | null | undefined) {
  if (change == null || change === 0) return Minus
  return change > 0 ? ArrowUp : ArrowDown
}

export function changeColor(change: number | null | undefined): string {
  if (change == null || change === 0) return 'var(--text-tertiary)'
  return change > 0 ? 'var(--success)' : 'var(--danger)'
}

export function changeText(change: number | null | undefined, unit = '%'): string {
  if (change == null || change === 0) return '持平'
  const prefix = change > 0 ? '+' : ''
  return `较上周 ${prefix}${change}${unit}`
}

export function moduleLabel(m: string): string {
  const map: Record<string, string> = {
    NOTE: '笔记', TODO: '待办', FILE: '文件',
    STUDY: '学习', READING: '阅读', BOOKMARK: '收藏',
    DIARY: '日记', STUDY_PLAN: '学习计划', AUTH: '认证',
  }
  return map[m] || m
}

export function actionLabel(a: string): string {
  const map: Record<string, string> = {
    CREATE: '新增', UPDATE: '更新', DELETE: '删除',
    RESTORE: '恢复', LOGIN: '登录',
  }
  return map[a] || a
}

export function activityDateLabel(item: ActivityItem): string {
  if (!item.createdAt) return ''
  const d = new Date(item.createdAt)
  const now = new Date()
  const today = new Date(now.getFullYear(), now.getMonth(), now.getDate())
  const target = new Date(d.getFullYear(), d.getMonth(), d.getDate())
  const diffDays = Math.floor((today.getTime() - target.getTime()) / 86400000)

  if (diffDays === 0) return '今天'
  if (diffDays === 1) return '昨天'
  if (diffDays < 7) return `${diffDays} 天前`
  return `${d.getMonth() + 1}月${d.getDate()}日`
}

export function groupActivities(items: ActivityItem[] | undefined) {
  if (!items?.length) return [] as { label: string; items: ActivityItem[] }[]
  const groups: { label: string; items: ActivityItem[] }[] = []
  let currentLabel = ''
  let currentGroup: ActivityItem[] = []

  for (const item of items) {
    const label = activityDateLabel(item)
    if (label !== currentLabel) {
      if (currentGroup.length > 0) {
        groups.push({ label: currentLabel, items: currentGroup })
      }
      currentLabel = label
      currentGroup = [item]
    } else {
      currentGroup.push(item)
    }
  }
  if (currentGroup.length > 0) {
    groups.push({ label: currentLabel, items: currentGroup })
  }
  return groups
}
