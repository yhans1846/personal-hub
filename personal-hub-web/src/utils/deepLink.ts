/** 列表页 Dialog/Drawer 深链：?edit=id / ?create=1 */

export function buildEditPath(listPath: string, id: number | string): string {
  return `${listPath}?edit=${id}`
}

export function buildCreatePath(listPath: string): string {
  return `${listPath}?create=1`
}

export function parseDeepLinkQuery(query: Record<string, unknown>): {
  editId: number | undefined
  create: boolean
} {
  const rawEdit = query.edit
  const editStr = Array.isArray(rawEdit) ? rawEdit[0] : rawEdit
  const editId =
    editStr != null && editStr !== '' && Number.isFinite(Number(editStr)) && Number(editStr) > 0
      ? Number(editStr)
      : undefined

  const rawCreate = query.create
  const createStr = Array.isArray(rawCreate) ? String(rawCreate[0]) : String(rawCreate ?? '')
  const create = createStr === '1' || createStr.toLowerCase() === 'true'

  return { editId, create }
}

const NOTIFICATION_PATHS: Record<string, string> = {
  todo: '/todos',
  study_plan: '/study-plans',
}

export function getNotificationRelatedPath(
  relatedType: string | null | undefined,
  relatedId: number | null | undefined,
): string {
  if (!relatedType || relatedId == null || relatedId <= 0) return ''
  const base = NOTIFICATION_PATHS[relatedType]
  if (!base) return ''
  return buildEditPath(base, relatedId)
}
