/** 笔记新建/编辑路径（真实路由，可新开标签） */

export function noteCreatePath(folderId?: number | null): string {
  if (folderId != null && Number.isFinite(folderId) && folderId > 0) {
    return `/notes/new?folderId=${folderId}`
  }
  return '/notes/new'
}

export function noteEditPath(id: number): string {
  return `/notes/${id}/edit`
}

export function openNoteCreateInNewTab(folderId?: number | null) {
  window.open(noteCreatePath(folderId), '_blank', 'noopener,noreferrer')
}

export function openNoteEditInNewTab(id: number) {
  window.open(noteEditPath(id), '_blank', 'noopener,noreferrer')
}

/** 命令面板等：笔记新建/编辑走新标签，其余仍当前页跳转 */
export function isNoteEditorPath(path: string): boolean {
  if (path === '/notes/new' || path.startsWith('/notes/new?')) return true
  return /^\/notes\/\d+\/edit\/?$/.test(path.split('?')[0] ?? '')
}
