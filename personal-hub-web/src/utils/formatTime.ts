/**
 * 通用日期与时长格式化（列表 / Dashboard / 统计共用）
 */

/** 相对时间：刚刚 / N 分钟前 / … / 短日期 */
export function formatRelativeTime(dateStr?: string | null): string {
  if (!dateStr) return ''
  const date = new Date(dateStr.replace(' ', 'T'))
  const now = new Date()
  const diff = now.getTime() - date.getTime()
  if (Number.isNaN(diff)) return ''

  if (diff < 60000) return '刚刚'
  if (diff < 3600000) return `${Math.floor(diff / 60000)} 分钟前`
  if (diff < 86400000) return `${Math.floor(diff / 3600000)} 小时前`
  if (diff < 604800000) return `${Math.floor(diff / 86400000)} 天前`
  return date.toLocaleDateString('zh-CN', { month: 'short', day: 'numeric' })
}

/** 「更新于 …」相对时间文案 */
export function formatRelativeUpdated(dateStr?: string | null): string {
  if (!dateStr) return '更新于 —'
  const rel = formatRelativeTime(dateStr)
  if (!rel) return `更新于 ${formatDate(dateStr) || '—'}`
  if (rel === '刚刚' || rel.endsWith('前')) return `更新于 ${rel}`
  return `更新于 ${formatDate(dateStr) || rel}`
}

/** 分钟 → 「N 分钟」/「H 小时」/「H 小时 M 分钟」 */
export function formatDuration(minutes: number): string {
  if (minutes == null || Number.isNaN(minutes)) return '0 分钟'
  if (minutes < 60) return `${minutes} 分钟`
  const h = Math.floor(minutes / 60)
  const m = minutes % 60
  return m > 0 ? `${h} 小时 ${m} 分钟` : `${h} 小时`
}

/** YYYY-MM-DD（取日期部分）；空 → fallback */
export function formatDate(dateStr?: string | null, empty = ''): string {
  if (!dateStr) return empty
  return dateStr.slice(0, 10)
}

/** 更新时间展示：YYYY-MM-DD HH:mm */
export function formatUpdated(dateStr?: string | null, empty = '—'): string {
  if (!dateStr) return empty
  const s = dateStr.replace('T', ' ')
  return s.length >= 16 ? s.slice(0, 16) : s.slice(0, 10)
}

/** YYYY.MM.DD（卡片日期区间用） */
export function formatDateDot(dateStr?: string | null, empty = '—'): string {
  if (!dateStr) return empty
  return dateStr.slice(0, 10).replace(/-/g, '.')
}
