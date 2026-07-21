import { headingIdFromText } from './parseToc'

export const SCROLL_SYNC_LOCK_MS = 220
export const SCROLL_SYNC_TOP_OFFSET = 32

export type ScrollSyncSource = 'editor' | 'preview'

/** 观墨同款：短时锁定驱动源，避免双向回声 */
export class ScrollSyncSession {
  source: ScrollSyncSource | null = null
  private timer: ReturnType<typeof setTimeout> | null = null

  lock(source: ScrollSyncSource, ms = SCROLL_SYNC_LOCK_MS) {
    this.source = source
    if (this.timer != null) clearTimeout(this.timer)
    this.timer = setTimeout(() => {
      this.source = null
      this.timer = null
    }, ms)
  }

  dispose() {
    if (this.timer != null) clearTimeout(this.timer)
    this.timer = null
    this.source = null
  }
}

function headingNodes(root: HTMLElement): HTMLElement[] {
  return Array.from(root.querySelectorAll<HTMLElement>('h1, h2, h3, h4, h5, h6'))
}

function ensureHeadingId(el: HTMLElement): string {
  if (el.id) return el.id
  const clone = el.cloneNode(true) as HTMLElement
  clone.querySelectorAll('.heading-anchor').forEach((a) => a.remove())
  const id = headingIdFromText(clone.textContent ?? '')
  if (id) el.id = id
  return id
}

/** 视口顶部附近当前标题 id（对齐观墨 getEditorTopLine / getPreviewLineAtTop） */
export function getHeadingIdAtScrollTop(
  container: HTMLElement,
  offsetPx = SCROLL_SYNC_TOP_OFFSET,
): string | null {
  const probeY = container.getBoundingClientRect().top + offsetPx
  let active: HTMLElement | null = null
  for (const h of headingNodes(container)) {
    const id = ensureHeadingId(h)
    if (!id) continue
    if (h.getBoundingClientRect().top <= probeY + 4) active = h
    else break
  }
  return active ? ensureHeadingId(active) : null
}

function cssEscapeId(id: string): string {
  if (typeof CSS !== 'undefined' && typeof CSS.escape === 'function') return CSS.escape(id)
  return id.replace(/([ !"#$%&'()*+,./:;<=>?@[\\\]^`{|}~])/g, '\\$1')
}

export function scrollContainerToHeading(
  container: HTMLElement,
  headingId: string,
  offsetPx = SCROLL_SYNC_TOP_OFFSET,
): boolean {
  const el =
    container.querySelector<HTMLElement>(`#${cssEscapeId(headingId)}`)
    ?? headingNodes(container).find((h) => ensureHeadingId(h) === headingId)
    ?? null
  if (!el) return false
  const containerRect = container.getBoundingClientRect()
  const elRect = el.getBoundingClientRect()
  const nextTop = container.scrollTop + (elRect.top - containerRect.top) - offsetPx
  container.scrollTo({ top: Math.max(0, nextTop) })
  return true
}

/**
 * 分屏标题锚点联动（参考 Guanmo-open EditorArea：锚点映射 + ScrollSyncSession）。
 * 比例滚动在两端内容高度差大时会「对不齐」，标题锚点更稳。
 */
export function bindHeadingSyncScroll(
  editor: HTMLElement,
  preview: HTMLElement,
): () => void {
  const session = new ScrollSyncSession()
  let editorFrame: number | null = null
  let previewFrame: number | null = null

  const onEditorScroll = () => {
    if (session.source === 'preview') return
    if (editorFrame != null) return
    editorFrame = requestAnimationFrame(() => {
      editorFrame = null
      const id = getHeadingIdAtScrollTop(editor)
      if (!id) return
      session.lock('editor')
      scrollContainerToHeading(preview, id)
    })
  }

  const onPreviewScroll = () => {
    if (session.source === 'editor') return
    if (previewFrame != null) return
    previewFrame = requestAnimationFrame(() => {
      previewFrame = null
      const id = getHeadingIdAtScrollTop(preview)
      if (!id) return
      session.lock('preview')
      scrollContainerToHeading(editor, id)
    })
  }

  editor.addEventListener('scroll', onEditorScroll, { passive: true })
  preview.addEventListener('scroll', onPreviewScroll, { passive: true })

  return () => {
    editor.removeEventListener('scroll', onEditorScroll)
    preview.removeEventListener('scroll', onPreviewScroll)
    if (editorFrame != null) cancelAnimationFrame(editorFrame)
    if (previewFrame != null) cancelAnimationFrame(previewFrame)
    session.dispose()
  }
}

/** @deprecated 保留比例算法供单测；分屏请用 bindHeadingSyncScroll */
export function syncScrollByRatio(source: HTMLElement, target: HTMLElement): void {
  const maxSource = source.scrollHeight - source.clientHeight
  const maxTarget = target.scrollHeight - target.clientHeight
  if (maxSource <= 0 || maxTarget <= 0) {
    target.scrollTop = 0
    return
  }
  target.scrollTop = (source.scrollTop / maxSource) * maxTarget
}

export function bindSyncScroll(a: HTMLElement, b: HTMLElement): () => void {
  return bindHeadingSyncScroll(a, b)
}
