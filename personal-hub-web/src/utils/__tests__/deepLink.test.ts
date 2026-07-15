import { describe, expect, it } from 'vitest'
import {
  buildCreatePath,
  buildEditPath,
  getNotificationRelatedPath,
  parseDeepLinkQuery,
} from '../deepLink'

describe('deepLink', () => {
  it('buildEditPath appends edit query', () => {
    expect(buildEditPath('/todos', 10)).toBe('/todos?edit=10')
    expect(buildEditPath('/study-plans', 4)).toBe('/study-plans?edit=4')
  })

  it('buildCreatePath appends create query', () => {
    expect(buildCreatePath('/study-records')).toBe('/study-records?create=1')
    expect(buildCreatePath('/diaries')).toBe('/diaries?create=1')
  })

  it('parseDeepLinkQuery reads edit id and create flag', () => {
    expect(parseDeepLinkQuery({ edit: '15' })).toEqual({ editId: 15, create: false })
    expect(parseDeepLinkQuery({ create: '1' })).toEqual({ editId: undefined, create: true })
    expect(parseDeepLinkQuery({ create: 'true' })).toEqual({ editId: undefined, create: true })
    expect(parseDeepLinkQuery({})).toEqual({ editId: undefined, create: false })
    expect(parseDeepLinkQuery({ edit: 'abc' })).toEqual({ editId: undefined, create: false })
  })

  it('getNotificationRelatedPath maps known types to list deep links', () => {
    expect(getNotificationRelatedPath('todo', 10)).toBe('/todos?edit=10')
    expect(getNotificationRelatedPath('study_plan', 2)).toBe('/study-plans?edit=2')
    expect(getNotificationRelatedPath('unknown', 1)).toBe('')
    expect(getNotificationRelatedPath('todo', null)).toBe('')
  })
})
