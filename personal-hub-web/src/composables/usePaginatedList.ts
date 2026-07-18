import { ref, type Ref } from 'vue'
import { handleApiError } from '@/utils/apiResult'

export type PageQuery = { page: number; size: number }

export type PaginatedPage<T> = {
  records: T[]
  total: number
}

/**
 * 列表页通用：loading + query + fetchList / onSearch / onPageChange
 */
export function usePaginatedList<T, Q extends PageQuery>(options: {
  initialQuery: Q
  fetchPage: (query: Q) => Promise<PaginatedPage<T>>
  /** 成功拉取后的钩子（如日记缩略图） */
  onFetched?: (records: T[]) => void | Promise<void>
  errorMessage?: string
}) {
  const list = ref<T[]>([]) as Ref<T[]>
  const total = ref(0)
  const loading = ref(false)
  const query = ref(options.initialQuery) as Ref<Q>

  async function fetchList() {
    loading.value = true
    try {
      const page = await options.fetchPage(query.value)
      list.value = page.records
      total.value = page.total
      await options.onFetched?.(page.records)
    } catch (e) {
      handleApiError(e, options.errorMessage ?? '加载失败')
      list.value = []
      total.value = 0
    } finally {
      loading.value = false
    }
  }

  function onSearch() {
    query.value.page = 1
    return fetchList()
  }

  function onPageChange(page: number) {
    query.value.page = page
    return fetchList()
  }

  return { list, total, loading, query, fetchList, onSearch, onPageChange }
}

/** Dialog 宿主：visible + editId + openCreate/openEdit */
export function useEntityDialogHost() {
  const dialogVisible = ref(false)
  const editId = ref<number | undefined>()

  function openCreate() {
    editId.value = undefined
    dialogVisible.value = true
  }

  function openEdit(id: number) {
    editId.value = id
    dialogVisible.value = true
  }

  return { dialogVisible, editId, openCreate, openEdit }
}
