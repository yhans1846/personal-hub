import { describe, it, expect, vi, beforeEach, afterEach } from 'vitest'
import {
  syncScrollByRatio,
  ScrollSyncSession,
  getHeadingIdAtScrollTop,
  scrollContainerToHeading,
} from '../syncScroll'

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
})

describe('ScrollSyncSession', () => {
  beforeEach(() => { vi.useFakeTimers() })
  afterEach(() => { vi.useRealTimers() })

  it('locks source then clears', () => {
    const s = new ScrollSyncSession()
    s.lock('editor', 220)
    expect(s.source).toBe('editor')
    vi.advanceTimersByTime(220)
    expect(s.source).toBeNull()
  })
})

describe('heading sync helpers', () => {
  it('getHeadingIdAtScrollTop picks last heading above probe', () => {
    const h1 = { id: 'A', getBoundingClientRect: () => ({ top: 10 }) }
    const h2 = { id: 'B', getBoundingClientRect: () => ({ top: 50 }) }
    const h3 = { id: 'C', getBoundingClientRect: () => ({ top: 200 }) }
    const container = {
      getBoundingClientRect: () => ({ top: 0 }),
      querySelectorAll: () => [h1, h2, h3],
    } as unknown as HTMLElement
    // probe at 0+32=32 → only A (top 10) is above
    expect(getHeadingIdAtScrollTop(container, 32)).toBe('A')
  })

  it('scrollContainerToHeading adjusts scrollTop', () => {
    const target = { id: 'Hello-World' }
    const container = {
      scrollTop: 0,
      getBoundingClientRect: () => ({ top: 100 }),
      querySelector: (sel: string) => (sel.includes('Hello-World') ? target : null),
      querySelectorAll: () => [],
      scrollTo: vi.fn(({ top }: { top: number }) => { (container as { scrollTop: number }).scrollTop = top }),
    } as unknown as HTMLElement
    Object.assign(target, {
      getBoundingClientRect: () => ({ top: 300 }),
    })
    expect(scrollContainerToHeading(container, 'Hello-World', 32)).toBe(true)
    // 0 + (300-100) - 32 = 168
    expect(container.scrollTop).toBe(168)
  })
})
