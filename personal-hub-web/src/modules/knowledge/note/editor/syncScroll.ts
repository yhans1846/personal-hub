/** 按滚动比例将 source 同步到 target（分屏编辑/预览联动） */
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

/**
 * 双向联动两个滚动容器；用锁避免回声。
 * @returns 解除绑定
 */
export function bindSyncScroll(a: HTMLElement, b: HTMLElement): () => void {
  let locked = false

  const onA = () => {
    if (locked) return
    locked = true
    syncScrollByRatio(a, b)
    requestAnimationFrame(() => { locked = false })
  }
  const onB = () => {
    if (locked) return
    locked = true
    syncScrollByRatio(b, a)
    requestAnimationFrame(() => { locked = false })
  }

  a.addEventListener('scroll', onA, { passive: true })
  b.addEventListener('scroll', onB, { passive: true })

  return () => {
    a.removeEventListener('scroll', onA)
    b.removeEventListener('scroll', onB)
  }
}
