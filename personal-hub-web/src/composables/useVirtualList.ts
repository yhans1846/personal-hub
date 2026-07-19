import { computed, ref, toValue, type MaybeRefOrGetter } from 'vue'

export type VirtualListOptions<T> = {
  /** 完整数据源 */
  items: MaybeRefOrGetter<T[]>
  /** 固定行高（px） */
  itemHeight: number
  /** 上下额外渲染行数，默认 4 */
  overscan?: number
}

export type VirtualListRow<T> = {
  item: T
  index: number
  offset: number
}

/**
 * 固定行高的窗口化列表（无第三方依赖）。
 * 适用于通知下拉等单列、近似等高行的长列表。
 */
export function useVirtualList<T>(options: VirtualListOptions<T>) {
  const scrollTop = ref(0)
  const viewportHeight = ref(0)
  const overscan = options.overscan ?? 4
  const itemHeight = options.itemHeight

  const items = computed(() => toValue(options.items))
  const totalHeight = computed(() => items.value.length * itemHeight)

  const range = computed(() => {
    const len = items.value.length
    if (len === 0) return { start: 0, end: 0 }
    const vh = viewportHeight.value || itemHeight * 8
    const start = Math.max(0, Math.floor(scrollTop.value / itemHeight) - overscan)
    const visible = Math.ceil(vh / itemHeight) + overscan * 2
    const end = Math.min(len, start + visible)
    return { start, end }
  })

  const visibleItems = computed<VirtualListRow<T>[]>(() => {
    const { start, end } = range.value
    const slice = items.value.slice(start, end)
    return slice.map((item, i) => ({
      item,
      index: start + i,
      offset: (start + i) * itemHeight,
    }))
  })

  function onScroll(e: Event) {
    const el = e.currentTarget as HTMLElement
    scrollTop.value = el.scrollTop
    viewportHeight.value = el.clientHeight
  }

  function setViewportHeight(h: number) {
    viewportHeight.value = h
  }

  return {
    itemHeight,
    totalHeight,
    visibleItems,
    range,
    onScroll,
    setViewportHeight,
  }
}
