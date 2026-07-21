import { describe, it, expect, vi, beforeEach, afterEach } from 'vitest'
import { syncScrollByRatio, ScrollSyncSession, bindSyncScroll } from '../syncScroll'

function fakeScrollEl(opts: {
  scrollHeight: number
  clientHeight: number
  scrollTop?: number
}): HTMLElement {
  return {
    scrollHeight: opts.scrollHeight,
    clientHeight: opts.clientHeight,
    scrollTop: opts.scrollTop ?? 0,
    addEventListener: vi.fn(),
    removeEventListener: vi.fn(),
  } as unknown as HTMLElement
}

describe('syncScrollByRatio', () => {
  it('maps mid scroll proportionally', () => {
    const source = fakeScrollEl({ scrollHeight: 1000, clientHeight: 200, scrollTop: 400 })
    const target = fakeScrollEl({ scrollHeight: 500, clientHeight: 100, scrollTop: 0 })
    syncScrollByRatio(source, target)
    expect(target.scrollTop).toBe(200)
  })

  it('resets when source cannot scroll', () => {
    const source = fakeScrollEl({ scrollHeight: 100, clientHeight: 100, scrollTop: 0 })
    const target = fakeScrollEl({ scrollHeight: 500, clientHeight: 100, scrollTop: 50 })
    syncScrollByRatio(source, target)
    expect(target.scrollTop).toBe(0)
  })
})

describe('ScrollSyncSession', () => {
  beforeEach(() => { vi.useFakeTimers() })
  afterEach(() => { vi.useRealTimers() })

  it('locks then clears', () => {
    const s = new ScrollSyncSession()
    s.lock('editor', 220)
    expect(s.source).toBe('editor')
    vi.advanceTimersByTime(220)
    expect(s.source).toBeNull()
  })
})

describe('bindSyncScroll', () => {
  it('registers listeners', () => {
    const a = fakeScrollEl({ scrollHeight: 200, clientHeight: 100 })
    const b = fakeScrollEl({ scrollHeight: 200, clientHeight: 100 })
    const unbind = bindSyncScroll(a, b)
    expect(a.addEventListener).toHaveBeenCalledWith('scroll', expect.any(Function), { passive: true })
    unbind()
    expect(a.removeEventListener).toHaveBeenCalled()
  })
})
