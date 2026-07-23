import { describe, expect, it, vi, beforeEach } from 'vitest'
import { nextTick } from 'vue'
import { ElMessage } from 'element-plus'
import { usePaginatedList } from '../usePaginatedList'

describe('usePaginatedList', () => {
  beforeEach(() => {
    vi.restoreAllMocks()
  })

  it('loads page into list/total', async () => {
    const fetchPage = vi.fn().mockResolvedValue({
      records: [{ id: 1 }],
      total: 3,
    })
    const { list, total, loading, query, fetchList } = usePaginatedList({
      initialQuery: { page: 1, size: 10, keyword: '' },
      fetchPage,
    })

    const p = fetchList()
    expect(loading.value).toBe(true)
    await p
    expect(loading.value).toBe(false)
    expect(list.value).toEqual([{ id: 1 }])
    expect(total.value).toBe(3)
    expect(fetchPage).toHaveBeenCalledWith(query.value)
  })

  it('onSearch resets page and refetches', async () => {
    const fetchPage = vi.fn().mockResolvedValue({ records: [], total: 0 })
    const { query, onSearch } = usePaginatedList({
      initialQuery: { page: 3, size: 10 },
      fetchPage,
    })
    await onSearch()
    expect(query.value.page).toBe(1)
    expect(fetchPage).toHaveBeenCalled()
  })

  it('shows error and clears list on failure', async () => {
    const errorSpy = vi.spyOn(ElMessage, 'error').mockImplementation(() => undefined as never)
    const fetchPage = vi.fn().mockRejectedValue(new Error('加载失败了'))
    const { list, total, fetchList } = usePaginatedList({
      initialQuery: { page: 1, size: 10 },
      fetchPage,
      errorMessage: '列表加载失败',
    })
    list.value = [{ id: 9 } as never]
    total.value = 9
    await fetchList()
    await nextTick()
    expect(list.value).toEqual([])
    expect(total.value).toBe(0)
    expect(errorSpy).toHaveBeenCalledWith('加载失败了')
  })

  it('does not keep loading true while onFetched side-effects run', async () => {
    let resolveFetched!: () => void
    const fetchedDone = new Promise<void>((r) => {
      resolveFetched = r
    })
    const fetchPage = vi.fn().mockResolvedValue({
      records: [{ id: 1 }],
      total: 1,
    })
    const onFetched = vi.fn().mockImplementation(() => fetchedDone)
    const { list, loading, fetchList } = usePaginatedList({
      initialQuery: { page: 1, size: 10 },
      fetchPage,
      onFetched,
    })

    await fetchList()
    expect(list.value).toEqual([{ id: 1 }])
    expect(loading.value).toBe(false)
    expect(onFetched).toHaveBeenCalled()
    // onFetched 仍可挂起；列表已可用
    resolveFetched()
    await fetchedDone
  })
})
