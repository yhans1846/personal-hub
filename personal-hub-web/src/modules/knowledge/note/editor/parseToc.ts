import type { TocItem } from '../preview/PreviewToc.vue'

/** 与预览页目录锚点一致：标题文案空格 → `-` */
export function headingIdFromText(text: string): string {
  return text.trim().replace(/\s+/g, '-')
}

/** 从 Markdown 正文解析标题目录 */
export function parseTocFromMarkdown(content: string): TocItem[] {
  const items: TocItem[] = []
  for (const line of content.split('\n')) {
    const match = line.match(/^(#{1,6})\s+(.+)$/)
    if (!match) continue
    const level = match[1].length
    const text = match[2].trim()
    items.push({ text, level, id: headingIdFromText(text) })
  }
  return items
}

/**
 * 给预览 DOM 中的标题写入与 parseToc 一致的 id（对齐 Preview.vue setupHeadingAnchors）。
 * 忽略已有 `.heading-anchor` 文案，避免二次赋值污染 id。
 */
export function assignPreviewHeadingIds(root: HTMLElement): void {
  root.querySelectorAll('h1, h2, h3, h4, h5, h6').forEach((h) => {
    const clone = h.cloneNode(true) as HTMLElement
    clone.querySelectorAll('.heading-anchor').forEach((a) => a.remove())
    const id = headingIdFromText(clone.textContent ?? '')
    if (!id) return
    h.id = id
  })
}

/**
 * 给 Vditor IR 标题写入与 parseToc 一致的 id。
 * 必须去掉 `.vditor-ir__marker`（`#` 等）与预览节点，否则 id 与大纲对不上。
 */
export function assignEditorHeadingIds(root: HTMLElement): void {
  root.querySelectorAll('h1, h2, h3, h4, h5, h6').forEach((h) => {
    const clone = h.cloneNode(true) as HTMLElement
    clone
      .querySelectorAll('.vditor-ir__marker, .vditor-ir__preview, .heading-anchor')
      .forEach((n) => n.remove())
    const id = headingIdFromText(clone.textContent ?? '')
    if (!id) return
    h.id = id
  })
}
