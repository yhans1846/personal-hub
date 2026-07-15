import type { TocItem } from '../preview/PreviewToc.vue'

/** 从 Markdown 正文解析标题目录 */
export function parseTocFromMarkdown(content: string): TocItem[] {
  const items: TocItem[] = []
  for (const line of content.split('\n')) {
    const match = line.match(/^(#{1,6})\s+(.+)$/)
    if (!match) continue
    const level = match[1].length
    const text = match[2].trim()
    items.push({ text, level, id: text.replace(/\s+/g, '-') })
  }
  return items
}
