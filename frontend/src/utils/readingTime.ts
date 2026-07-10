/**
 * 估算 Markdown 文本阅读时间
 * @param content 原始 Markdown 内容
 * @returns 可读的时长字符串
 */
export function estimateReadingTime(content?: string): string {
  if (!content) return '< 1 分钟'

  // 去除 Markdown 标记和空白
  const text = content
    .replace(/[#*`~>\[\]()!|-]/g, '')
    .replace(/\s+/g, '')

  const charCount = text.length
  if (charCount === 0) return '< 1 分钟'

  // 中文阅读速度 ~400 字/分钟，英文 ~200 词/分钟
  // 混合内容用 350 字符/分钟作为估算
  const minutes = Math.ceil(charCount / 350)

  if (minutes < 1) return '< 1 分钟'
  return `预计阅读 ${minutes} 分钟`
}
