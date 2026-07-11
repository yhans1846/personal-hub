/**
 * 估算 Markdown 文本阅读时间
 * @param content 原始 Markdown 内容
 * @returns 可读的时长字符串
 */
export function estimateReadingTime(content?: string): string {
  if (!content) return '< 1 分钟'

  const text = content.replace(/[#*`~>\[\]()!|-]/g, '').replace(/\s+/g, '')
  const charCount = text.length
  if (charCount === 0) return '< 1 分钟'

  const minutes = Math.ceil(charCount / 350)
  if (minutes < 1) return '< 1 分钟'
  return `预计阅读 ${minutes} 分钟`
}

/**
 * 格式化相对时间
 */
export function formatRelativeTime(dateStr?: string): string {
  if (!dateStr) return ''
  const date = new Date(dateStr)
  const now = new Date()
  const diff = now.getTime() - date.getTime()

  if (diff < 60000) return '刚刚'
  if (diff < 3600000) return `${Math.floor(diff / 60000)} 分钟前`
  if (diff < 86400000) return `${Math.floor(diff / 3600000)} 小时前`
  if (diff < 604800000) return `${Math.floor(diff / 86400000)} 天前`
  return date.toLocaleDateString('zh-CN', { month: 'short', day: 'numeric' })
}

/**
 * 检查是否在最近编辑窗口内（默认 24 小时）
 */
export function isRecentlyEdited(dateStr?: string, windowMs = 86400000): boolean {
  if (!dateStr) return false
  return Date.now() - new Date(dateStr).getTime() < windowMs
}
