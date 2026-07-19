/**
 * 估算 Markdown 文本阅读时间
 * @param content 原始 Markdown 内容
 * @returns 可读的时长字符串
 */
export function estimateReadingTime(content?: string): string {
  if (!content) return '< 1 分钟'

  const text = content.replace(/[#*`~>[\]()!|-]/g, '').replace(/\s+/g, '')
  const charCount = text.length
  if (charCount === 0) return '< 1 分钟'

  const minutes = Math.ceil(charCount / 350)
  if (minutes < 1) return '< 1 分钟'
  return `预计阅读 ${minutes} 分钟`
}

export { formatRelativeTime } from './formatTime'

/**
 * 检查是否在最近编辑窗口内（默认 24 小时）
 */
export function isRecentlyEdited(dateStr?: string, windowMs = 86400000): boolean {
  if (!dateStr) return false
  return Date.now() - new Date(dateStr).getTime() < windowMs
}
