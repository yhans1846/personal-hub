/** Wiki 双向链接解析与预处理（规格 2026-07-21-wiki-backlink） */

export type WikiLinkMatch = {
  raw: string
  title: string
  alias?: string
  index: number
}

/** 匹配 [[title]] 或 [[title|alias]]（非贪婪，单行内） */
export const WIKI_LINK_RE = /\[\[([^\]|\n]+?)(?:\|([^\]\n]+))?\]\]/g

export function parseWikiLinks(markdown: string): WikiLinkMatch[] {
  const out: WikiLinkMatch[] = []
  const re = new RegExp(WIKI_LINK_RE.source, 'g')
  let m: RegExpExecArray | null
  while ((m = re.exec(markdown)) !== null) {
    out.push({
      raw: m[0],
      title: m[1].trim(),
      alias: m[2]?.trim() || undefined,
      index: m.index,
    })
  }
  return out
}

/** 正文是否包含指向给定标题的 wiki 链（精确标题，可带别名） */
export function contentHasWikiLinkTo(markdown: string, noteTitle: string): boolean {
  const target = noteTitle.trim()
  if (!target) return false
  return parseWikiLinks(markdown).some((l) => l.title === target)
}

/**
 * 将 wiki 链转为 Markdown 链接。
 * 命中 → /notes/{id}/preview；未命中 → #wiki-missing:编码标题
 */
export function transformWikiLinks(
  markdown: string,
  resolveId: (title: string) => number | null | undefined,
): string {
  return markdown.replace(WIKI_LINK_RE, (_raw, titlePart: string, aliasPart?: string) => {
    const title = String(titlePart).trim()
    const label = (aliasPart != null ? String(aliasPart).trim() : title) || title
    const id = resolveId(title)
    if (id != null && id > 0) {
      return `[${escapeMdLabel(label)}](/notes/${id}/preview)`
    }
    return `[${escapeMdLabel(label)}](#wiki-missing:${encodeURIComponent(title)})`
  })
}

function escapeMdLabel(label: string): string {
  return label.replace(/[[\]]/g, '')
}

/** 从未闭合的 [[ 片段提取查询词（光标前文本） */
export function extractOpenWikiQuery(textBeforeCursor: string): string | null {
  const idx = textBeforeCursor.lastIndexOf('[[')
  if (idx < 0) return null
  const after = textBeforeCursor.slice(idx + 2)
  if (after.includes(']]') || after.includes('\n')) return null
  return after
}
