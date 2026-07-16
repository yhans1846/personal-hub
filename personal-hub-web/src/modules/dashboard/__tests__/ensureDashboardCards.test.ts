import { describe, it, expect } from 'vitest'
import {
  DEFAULT_DASHBOARD_ITEMS,
  REMOVED_DASHBOARD_CODES,
  ensureDashboardCards,
  DASHBOARD_CARD_SPAN,
} from '../defaultLayouts'

describe('ensureDashboardCards', () => {
  it('剔除 resource_snapshot / pending_todos 并补齐 external_links', () => {
    const result = ensureDashboardCards([
      { code: 'today_plan', title: '今日任务', order: 1, visible: true },
      { code: 'resource_snapshot', title: '资源盘点', order: 3, visible: true },
      { code: 'pending_todos', title: '待办任务', order: 9, visible: true },
    ])
    expect(result.some(c => c.code === 'resource_snapshot')).toBe(false)
    expect(result.some(c => c.code === 'pending_todos')).toBe(false)
    expect(result.some(c => c.code === 'external_links')).toBe(true)
    expect(REMOVED_DASHBOARD_CODES.has('resource_snapshot')).toBe(true)
  })

  it('六宫格默认均为 span 4 且无资源盘点', () => {
    expect(DEFAULT_DASHBOARD_ITEMS).toHaveLength(6)
    expect(DEFAULT_DASHBOARD_ITEMS.some(c => c.code === 'resource_snapshot')).toBe(false)
    for (const item of DEFAULT_DASHBOARD_ITEMS) {
      expect(DASHBOARD_CARD_SPAN[item.code]).toBe(4)
      expect(item.visible).toBe(true)
    }
    expect(DEFAULT_DASHBOARD_ITEMS.find(c => c.code === 'external_links')?.visible).toBe(true)
  })
})
