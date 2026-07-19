import { describe, expect, it } from 'vitest'
import { ref } from 'vue'
import { useVirtualList } from '../useVirtualList'

describe('useVirtualList', () => {
  it('slices visible window by scrollTop', () => {
    const items = ref(Array.from({ length: 100 }, (_, i) => ({ id: i })))
    const { visibleItems, setViewportHeight, onScroll, totalHeight } = useVirtualList({
      items,
      itemHeight: 40,
      overscan: 2,
    })

    setViewportHeight(200)
    expect(totalHeight.value).toBe(4000)

    // 模拟滚到第 10 行附近
    onScroll({
      currentTarget: { scrollTop: 400, clientHeight: 200 },
    } as unknown as Event)

    const idxs = visibleItems.value.map((r) => r.index)
    expect(idxs[0]).toBeLessThanOrEqual(10)
    expect(idxs[idxs.length - 1]).toBeGreaterThanOrEqual(14)
    expect(visibleItems.value.length).toBeLessThan(items.value.length)
  })

  it('returns empty when no items', () => {
    const { visibleItems, totalHeight } = useVirtualList({
      items: [],
      itemHeight: 40,
    })
    expect(totalHeight.value).toBe(0)
    expect(visibleItems.value).toEqual([])
  })
})
