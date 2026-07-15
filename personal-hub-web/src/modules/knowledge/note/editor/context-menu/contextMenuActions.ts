import type Vditor from 'vditor'

export type HeadingLevel = 1 | 2 | 3 | 4 | 5 | 6

/** 生成 GFM 表格 Markdown */
export function buildTableMarkdown(rows: number, cols: number): string {
  const header = '| ' + Array(cols).fill(' ').join(' | ') + ' |'
  const sep = '| ' + Array(cols).fill('---').join(' | ') + ' |'
  const body = Array.from({ length: Math.max(0, rows - 1) }, () =>
    '| ' + Array(cols).fill(' ').join(' | ') + ' |',
  )
  return ['', header, sep, ...body, ''].join('\n')
}

/** 扫描文档中已有脚注编号，返回下一个可用编号 */
export function nextFootnoteIndex(markdown: string): number {
  const matches = markdown.matchAll(/\[\^(\d+)\]/g)
  let max = 0
  for (const m of matches) {
    const n = Number(m[1])
    if (!Number.isNaN(n)) max = Math.max(max, n)
  }
  return max + 1
}

/** 扫描文档中已有链接引用 ID */
export function nextReferenceId(markdown: string): string {
  const matches = markdown.matchAll(/^\[([^\]]+)\]:\s/mg)
  let max = 0
  for (const m of matches) {
    const id = m[1]
    const num = Number(id.replace(/^ref/i, ''))
    if (!Number.isNaN(num)) max = Math.max(max, num)
  }
  return `ref${max + 1}`
}

export function headingPrefix(level: HeadingLevel): string {
  return '#'.repeat(level) + ' '
}

export function wrapHeading(level: HeadingLevel, text: string): string {
  const lines = text.split('\n')
  return lines.map((line) => `${headingPrefix(level)}${line}`).join('\n')
}

export function stripHeading(text: string): string {
  return text
    .split('\n')
    .map((line) => line.replace(/^#{1,6}\s+/, ''))
    .join('\n')
}

function focusVditor(vditor: Vditor) {
  vditor.focus()
}

export function insertAtCursor(vditor: Vditor, md: string) {
  vditor.insertMD(md)
  focusVditor(vditor)
}

export function wrapSelection(vditor: Vditor, before: string, after: string) {
  const text = vditor.getSelection()
  if (text) {
    vditor.updateValue(`${before}${text}${after}`)
  } else {
    vditor.insertMD(`${before}${after}`)
  }
  focusVditor(vditor)
}

export function replaceSelection(vditor: Vditor, insert: string) {
  const text = vditor.getSelection()
  if (text) {
    vditor.updateValue(insert.replace('$selection', text))
  } else {
    vditor.insertMD(insert)
  }
  focusVditor(vditor)
}

export function insertHeading(vditor: Vditor, level: HeadingLevel) {
  insertAtCursor(vditor, `\n${headingPrefix(level)}标题\n`)
}

export function convertSelectionToHeading(vditor: Vditor, level: HeadingLevel) {
  const text = vditor.getSelection()
  if (!text) {
    insertHeading(vditor, level)
    return
  }
  vditor.updateValue(wrapHeading(level, text))
  focusVditor(vditor)
}

export function convertSelectionToParagraph(vditor: Vditor) {
  const text = vditor.getSelection()
  if (!text) return
  vditor.updateValue(stripHeading(text))
  focusVditor(vditor)
}

export function insertQuote(vditor: Vditor) {
  insertAtCursor(vditor, '\n> 引用\n')
}

export function wrapQuote(vditor: Vditor) {
  const text = vditor.getSelection()
  if (text) {
    vditor.updateValue(text.split('\n').map((l) => `> ${l}`).join('\n'))
  } else {
    insertQuote(vditor)
  }
  focusVditor(vditor)
}

export function insertList(vditor: Vditor, type: 'unordered' | 'ordered' | 'task') {
  const templates = {
    unordered: '\n- 列表项\n',
    ordered: '\n1. 列表项\n',
    task: '\n- [ ] 任务\n',
  }
  insertAtCursor(vditor, templates[type])
}

export function insertLink(vditor: Vditor) {
  insertAtCursor(vditor, '[链接文字](https://example.com)')
}

export function wrapLink(vditor: Vditor) {
  wrapSelection(vditor, '[', '](https://example.com)')
}

export function insertReferenceLink(vditor: Vditor) {
  const id = nextReferenceId(vditor.getValue())
  insertAtCursor(vditor, `\n[链接文字][${id}]\n\n[${id}]: https://example.com\n`)
}

export function insertFootnote(vditor: Vditor) {
  const n = nextFootnoteIndex(vditor.getValue())
  insertAtCursor(vditor, `\n[^${n}]\n\n[^${n}]: 脚注内容\n`)
}

export function wrapFootnote(vditor: Vditor) {
  const n = nextFootnoteIndex(vditor.getValue())
  wrapSelection(vditor, '', `[^${n}]`)
  const md = vditor.getValue()
  if (!md.includes(`[^${n}]:`)) {
    vditor.insertMD(`\n\n[^${n}]: 脚注内容\n`)
  }
  focusVditor(vditor)
}

export function insertHorizontalRule(vditor: Vditor) {
  insertAtCursor(vditor, '\n---\n')
}

export function insertTable(vditor: Vditor, rows: number, cols: number) {
  insertAtCursor(vditor, buildTableMarkdown(rows, cols))
}

export function insertCodeBlock(vditor: Vditor, lang = '') {
  insertAtCursor(vditor, `\n\`\`\`${lang}\n\n\`\`\`\n`)
}

export function wrapCodeBlock(vditor: Vditor) {
  wrapSelection(vditor, '\n```\n', '\n```\n')
}

export function insertMermaid(vditor: Vditor) {
  insertAtCursor(vditor, '\n```mermaid\ngraph TD\n  A --> B\n```\n')
}

export function insertMathBlock(vditor: Vditor) {
  insertAtCursor(vditor, '\n$$\nE = mc^2\n$$\n')
}

export function insertInlineMath(vditor: Vditor) {
  wrapSelection(vditor, '$', '$')
}

export function insertImageMarkdown(vditor: Vditor, url: string, alt = 'image') {
  insertAtCursor(vditor, `![${alt}](${url})`)
}

export function formatBold(vditor: Vditor) { wrapSelection(vditor, '**', '**') }
export function formatItalic(vditor: Vditor) { wrapSelection(vditor, '*', '*') }
export function formatStrike(vditor: Vditor) { wrapSelection(vditor, '~~', '~~') }
export function formatInlineCode(vditor: Vditor) { wrapSelection(vditor, '`', '`') }
export function formatSuperscript(vditor: Vditor) { wrapSelection(vditor, '^', '^') }
export function formatSubscript(vditor: Vditor) { wrapSelection(vditor, '~', '~') }

export async function pasteFromClipboard(vditor: Vditor) {
  try {
    const text = await navigator.clipboard.readText()
    vditor.insertValue(text)
    focusVditor(vditor)
  } catch {
    // clipboard denied
  }
}

export function copySelection() {
  const sel = window.getSelection()?.toString()
  if (sel) navigator.clipboard.writeText(sel)
}

