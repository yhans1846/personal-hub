import { describe, expect, it } from 'vitest'
import {
  countUnreadByTypes,
  filterNotificationsByTypes,
  normalizeEnabledTypes,
  NOTIFICATION_TYPE_VALUES,
} from '../notificationFilter'
import type { NotificationVO } from '@/types/notification'

function n(partial: Partial<NotificationVO> & Pick<NotificationVO, 'id' | 'type' | 'isRead'>): NotificationVO {
  return {
    title: 't',
    content: null,
    relatedId: null,
    relatedType: null,
    createdAt: '2026-01-01',
    ...partial,
  }
}

describe('normalizeEnabledTypes', () => {
  it('returns defaults for empty', () => {
    expect(normalizeEnabledTypes([])).toEqual([...NOTIFICATION_TYPE_VALUES])
    expect(normalizeEnabledTypes(null)).toEqual([...NOTIFICATION_TYPE_VALUES])
  })

  it('migrates legacy setting keys', () => {
    expect(normalizeEnabledTypes(['todo_due', 'study_progress'])).toEqual([
      'TODO_OVERDUE',
      'PLAN_DEADLINE',
      'PLAN_COMPLETED',
    ])
  })

  it('keeps known backend types', () => {
    expect(normalizeEnabledTypes(['TODO_OVERDUE'])).toEqual(['TODO_OVERDUE'])
  })
})

describe('filterNotificationsByTypes', () => {
  const list = [
    n({ id: 1, type: 'TODO_OVERDUE', isRead: false }),
    n({ id: 2, type: 'PLAN_DEADLINE', isRead: true }),
    n({ id: 3, type: 'PLAN_COMPLETED', isRead: false }),
  ]

  it('filters by enabled types', () => {
    expect(filterNotificationsByTypes(list, ['TODO_OVERDUE']).map((x) => x.id)).toEqual([1])
  })

  it('returns empty when no types enabled', () => {
    expect(filterNotificationsByTypes(list, [])).toEqual([])
  })

  it('counts unread within enabled types', () => {
    expect(countUnreadByTypes(list, ['TODO_OVERDUE', 'PLAN_COMPLETED'])).toBe(2)
    expect(countUnreadByTypes(list, ['PLAN_DEADLINE'])).toBe(0)
  })
})
