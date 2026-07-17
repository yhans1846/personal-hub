import { ref, onMounted, onUnmounted } from 'vue'
import { resolveFillPageSize } from './resolveFillPageSize'

/**
 * 按 `.main-content` 可视高度得到 10/8/6；resize 防抖 150ms。
 * size 变化时调用 onSizeChange（调用方应重置 page 并重新请求）。
 */
export function useFillPageSize(onSizeChange?: (size: number) => void) {
  const pageSize = ref(10)
  let timer: ReturnType<typeof setTimeout> | null = null

  function measure() {
    const el = document.querySelector('.main-content') as HTMLElement | null
    const h = el?.clientHeight ?? window.innerHeight
    const next = resolveFillPageSize(h)
    if (next !== pageSize.value) {
      pageSize.value = next
      onSizeChange?.(next)
    }
  }

  function onResize() {
    if (timer) clearTimeout(timer)
    timer = setTimeout(measure, 150)
  }

  onMounted(() => {
    measure()
    window.addEventListener('resize', onResize)
  })
  onUnmounted(() => {
    window.removeEventListener('resize', onResize)
    if (timer) clearTimeout(timer)
  })

  return { pageSize }
}
