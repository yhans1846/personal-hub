import type { NotificationVO } from '@/types/notification'

/** 与后端 NotificationType 对齐 */
export const NOTIFICATION_TYPE_VALUES = [
  'TODO_OVERDUE',
  'PLAN_DEADLINE',
  'PLAN_COMPLETED',
] as const

export type NotificationTypeValue = (typeof NOTIFICATION_TYPE_VALUES)[number]

export const NOTIFICATION_TYPE_OPTIONS: { value: NotificationTypeValue; label: string }[] = [
  { value: 'TODO_OVERDUE', label: '待办超期' },
  { value: 'PLAN_DEADLINE', label: '计划即将截止' },
  { value: 'PLAN_COMPLETED', label: '计划已完成' },
]

const LEGACY_TYPE_MAP: Record<string, NotificationTypeValue[]> = {
  todo_due: ['TODO_OVERDUE'],
  study_progress: ['PLAN_DEADLINE', 'PLAN_COMPLETED'],
  note_reminder: [],
  system: [],
}

/** 将旧设置页类型迁移为后端枚举；无效时回退默认全开 */
export function normalizeEnabledTypes(types: string[] | undefined | null): string[] {
  if (!types?.length) return [...NOTIFICATION_TYPE_VALUES]

  const known = new Set<string>(NOTIFICATION_TYPE_VALUES)
  const hasLegacy = types.some((t) => t in LEGACY_TYPE_MAP)
  const hasKnown = types.some((t) => known.has(t))

  if (hasLegacy) {
    const next = new Set<string>()
    for (const t of types) {
      if (known.has(t)) next.add(t)
      else if (t in LEGACY_TYPE_MAP) {
        for (const mapped of LEGACY_TYPE_MAP[t]) next.add(mapped)
      }
    }
    return next.size > 0 ? [...next] : [...NOTIFICATION_TYPE_VALUES]
  }

  if (hasKnown) return types.filter((t) => known.has(t))
  return [...NOTIFICATION_TYPE_VALUES]
}

export function filterNotificationsByTypes(
  list: NotificationVO[],
  enabledTypes: string[],
): NotificationVO[] {
  if (!enabledTypes.length) return []
  const set = new Set(enabledTypes)
  return list.filter((n) => set.has(n.type))
}

export function countUnreadByTypes(
  list: NotificationVO[],
  enabledTypes: string[],
): number {
  return filterNotificationsByTypes(list, enabledTypes).filter((n) => !n.isRead).length
}
