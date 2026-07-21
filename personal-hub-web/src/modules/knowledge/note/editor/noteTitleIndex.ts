import { getNoteList } from '@/modules/knowledge/api'

let cache: Map<string, number> | null = null
let cacheAt = 0
const TTL_MS = 60_000

/** 标题 → 笔记 id（同名保留列表中先出现的，通常为较新） */
export async function getNoteTitleIdMap(force = false): Promise<Map<string, number>> {
  if (!force && cache && Date.now() - cacheAt < TTL_MS) {
    return cache
  }
  const map = new Map<string, number>()
  const res = await getNoteList({ page: 1, size: 500 })
  const records = res.data?.data?.records ?? []
  for (const n of records) {
    const title = (n.title || '').trim()
    if (title && !map.has(title)) {
      map.set(title, n.id)
    }
  }
  cache = map
  cacheAt = Date.now()
  return map
}

export function invalidateNoteTitleIndex() {
  cache = null
  cacheAt = 0
}
