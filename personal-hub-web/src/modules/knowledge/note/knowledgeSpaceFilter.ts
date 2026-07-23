import type { NoteFolderNoteItem, NoteFolderVO } from '@/types/note'

export type NoteSummary = { id: number; title?: string | null }

export function matchQuery(title: string | null | undefined, q: string): boolean {
  const needle = q.trim().toLowerCase()
  if (!needle) return true
  return (title ?? '').toLowerCase().includes(needle)
}

export function filterNoteSummaries<T extends NoteSummary>(list: T[], q: string): T[] {
  if (!q.trim()) return list
  return list.filter((n) => matchQuery(n.title, q))
}

/** 递归过滤夹：保留名称匹配或子孙/笔记匹配的节点 */
export function filterFolderTree(nodes: NoteFolderVO[], q: string): NoteFolderVO[] {
  if (!q.trim()) return nodes
  const out: NoteFolderVO[] = []
  for (const n of nodes) {
    const children = filterFolderTree(n.children ?? [], q)
    const notes = (n.notes ?? []).filter((note) => matchQuery(note.title, q))
    const selfMatch = matchQuery(n.name, q)
    if (selfMatch || children.length || notes.length) {
      out.push({ ...n, children, notes: selfMatch ? n.notes : notes })
    }
  }
  return out
}

export function filterUncategorizedNotes(notes: NoteFolderNoteItem[], q: string): NoteFolderNoteItem[] {
  return filterNoteSummaries(notes, q)
}
