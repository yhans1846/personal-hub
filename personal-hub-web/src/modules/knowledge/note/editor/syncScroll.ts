/**
 * 分屏滚动同步。
 *
 * 深度结论（对照 Guanmo-open + 本项目 Vditor IR）：
 * - 观墨用 CodeMirror 行号 + data-md-line 锚点；我们 IR 没有稳定行锚点。
 * - IR 标题 DOM 含 `.vditor-ir__marker`，纯「标题 id 映射」常匹配失败 → 看起来像「完全没联动」。
 * - 可靠做法：左右用**同一层 pane 滚动**（内容撑开），再做**比例同步 + 短时锁定**（观墨的 ScrollSyncSession）。
 */

export const SCROLL_SYNC_LOCK_MS = 220

export type ScrollSyncSource = 'editor' | 'preview'

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

export function syncScrollByRatio(source: HTMLElement, target: HTMLElement): void {
  const maxSource = source.scrollHeight - source.clientHeight
  const maxTarget = target.scrollHeight - target.clientHeight
  if (maxSource <= 0) {
    target.scrollTop = 0
    return
  }
  if (maxTarget <= 0) {
    target.scrollTop = 0
    return
  }
  target.scrollTop = (source.scrollTop / maxSource) * maxTarget
}

function cssEscapeId(id: string): string {
  if (typeof CSS !== 'undefined' && typeof CSS.escape === 'function') return CSS.escape(id)
  return id.replace(/([ !"#$%&'()*+,./:;<=>?@[\\\]^`{|}~])/g, '\\$1')
}

/** 在滚动容器内滚到标题（用于大纲 → 预览/编辑） */
export function scrollContainerToHeading(
  container: HTMLElement,
  headingId: string,
  offsetPx = 24,
  behavior: ScrollBehavior = 'auto',
): boolean {
  const el = container.querySelector<HTMLElement>(`#${cssEscapeId(headingId)}`)
  if (!el) return false
  const containerRect = container.getBoundingClientRect()
  const elRect = el.getBoundingClientRect()
  const nextTop = container.scrollTop + (elRect.top - containerRect.top) - offsetPx
  container.scrollTo({ top: Math.max(0, nextTop), behavior })
  return true
}

/**
 * 双向比例联动 + 观墨式短时锁定。
 * a/b 必须是真正 overflow 滚动的节点（本项目为 `.pane-editor` / `.pane-preview`）。
 */
export function bindSyncScroll(a: HTMLElement, b: HTMLElement): () => void {
  const session = new ScrollSyncSession()
  let aFrame: number | null = null
  let bFrame: number | null = null

  const onA = () => {
    if (session.source === 'preview') return
    if (aFrame != null) return
    aFrame = requestAnimationFrame(() => {
      aFrame = null
      session.lock('editor')
      syncScrollByRatio(a, b)
    })
  }

  const onB = () => {
    if (session.source === 'editor') return
    if (bFrame != null) return
    bFrame = requestAnimationFrame(() => {
      bFrame = null
      session.lock('preview')
      syncScrollByRatio(b, a)
    })
  }

  a.addEventListener('scroll', onA, { passive: true })
  b.addEventListener('scroll', onB, { passive: true })

  return () => {
    a.removeEventListener('scroll', onA)
    b.removeEventListener('scroll', onB)
    if (aFrame != null) cancelAnimationFrame(aFrame)
    if (bFrame != null) cancelAnimationFrame(bFrame)
    session.dispose()
  }
}
