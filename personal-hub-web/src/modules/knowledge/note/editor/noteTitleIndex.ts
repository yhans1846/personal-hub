import { getNoteList } from '@/modules/knowledge/api'

const BACKEND_MAX_SIZE = 100

let cache: Map<string, number> | null = null
let cacheAt = 0
const TTL_MS = 60_000

/** 标题 → 笔记 id（同名保留列表中先出现的，通常为较新） */
export async function getNoteTitleIdMap(force = false): Promise<Map<string, number>> {
  if (!force && cache && Date.now() - cacheAt < TTL_MS) {
    return cache
  }
  const map = new Map<string, number>()
  let page = 1
  let total: number | null = null
  try {
    while (total === null || (page - 1) * BACKEND_MAX_SIZE < total) {
      const res = await getNoteList({ page, size: BACKEND_MAX_SIZE })
      const pageRes = res.data?.data
      if (!pageRes) break
      const records = pageRes.records ?? []
      for (const n of records) {
        const title = (n.title || '').trim()
        if (title && !map.has(title)) {
          map.set(title, n.id)
        }
      }
      if (total === null) {
        total = pageRes.total ?? 0
      }
      page++
    }
  } catch {
    // 失败时返回已有结果，不抛异常
  }
  cache = map
  cacheAt = Date.now()
  return map
}

export function invalidateNoteTitleIndex() {
  cache = null
  cacheAt = 0
}
