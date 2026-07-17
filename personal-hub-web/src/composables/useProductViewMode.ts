import { ref } from 'vue'

export type ProductViewMode = 'table' | 'card'

function readStoredMode(storageKey: string, defaultMode: ProductViewMode): ProductViewMode {
  const stored = localStorage.getItem(storageKey)
  if (stored === 'table' || stored === 'card') return stored
  return defaultMode
}

/**
 * Product 列表 Table/Card 视图切换，持久化到 localStorage。
 */
export function useProductViewMode(storageKey: string, defaultMode: ProductViewMode = 'table') {
  const viewMode = ref<ProductViewMode>(readStoredMode(storageKey, defaultMode))

  function setViewMode(mode: ProductViewMode) {
    viewMode.value = mode
    localStorage.setItem(storageKey, mode)
  }

  return { viewMode, setViewMode }
}
