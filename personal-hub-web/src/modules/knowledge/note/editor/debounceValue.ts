import { ref, watch, type Ref } from 'vue'

export function useDebouncedRef<T>(source: Ref<T>, delayMs: number): Ref<T> {
  const out = ref(source.value) as Ref<T>
  let timer: ReturnType<typeof setTimeout> | undefined
  watch(source, (v) => {
    if (timer) clearTimeout(timer)
    timer = setTimeout(() => { out.value = v }, delayMs)
  }, { flush: 'sync' })
  return out
}
