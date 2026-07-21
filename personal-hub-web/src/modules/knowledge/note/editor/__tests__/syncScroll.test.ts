import { describe, it, expect, vi } from 'vitest'
import { syncScrollByRatio, bindSyncScroll } from '../syncScroll'

function fakeScrollEl(opts: {
  scrollHeight: number
  clientHeight: number
  scrollTop?: number
}): HTMLElement {
  const el = {
    scrollHeight: opts.scrollHeight,
    clientHeight: opts.clientHeight,
    scrollTop: opts.scrollTop ?? 0,
    addEventListener: vi.fn(),
    removeEventListener: vi.fn(),
  }
  return el as unknown as HTMLElement
}

describe('syncScrollByRatio', () => {
  it('maps mid scroll proportionally', () => {
    const source = fakeScrollEl({ scrollHeight: 1000, clientHeight: 200, scrollTop: 400 })
    const target = fakeScrollEl({ scrollHeight: 500, clientHeight: 100, scrollTop: 0 })
    // maxS=800, ratio=0.5; maxT=400 → 200
    syncScrollByRatio(source, target)
    expect(target.scrollTop).toBe(200)
  })

  it('resets target when source cannot scroll', () => {
    const source = fakeScrollEl({ scrollHeight: 100, clientHeight: 100, scrollTop: 0 })
    const target = fakeScrollEl({ scrollHeight: 500, clientHeight: 100, scrollTop: 50 })
    syncScrollByRatio(source, target)
    expect(target.scrollTop).toBe(0)
  })
})

describe('bindSyncScroll', () => {
  it('registers and cleans up listeners', () => {
    const a = fakeScrollEl({ scrollHeight: 200, clientHeight: 100 })
    const b = fakeScrollEl({ scrollHeight: 200, clientHeight: 100 })
    const unbind = bindSyncScroll(a, b)
    expect(a.addEventListener).toHaveBeenCalledWith('scroll', expect.any(Function), { passive: true })
    expect(b.addEventListener).toHaveBeenCalledWith('scroll', expect.any(Function), { passive: true })
    unbind()
    expect(a.removeEventListener).toHaveBeenCalled()
    expect(b.removeEventListener).toHaveBeenCalled()
  })
})
