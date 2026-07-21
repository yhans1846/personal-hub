import { describe, it, expect } from 'vitest'
import { headingIdFromText, parseTocFromMarkdown, assignPreviewHeadingIds } from '../parseToc'

describe('parseToc / heading anchors', () => {
  it('headingIdFromText replaces spaces', () => {
    expect(headingIdFromText('一、目标与原则')).toBe('一、目标与原则')
    expect(headingIdFromText('Hello World')).toBe('Hello-World')
  })

  it('parseTocFromMarkdown builds ids', () => {
    const items = parseTocFromMarkdown('# A\n\n## B C\n')
    expect(items).toEqual([
      { text: 'A', level: 1, id: 'A' },
      { text: 'B C', level: 2, id: 'B-C' },
    ])
  })

  it('assignPreviewHeadingIds writes matching ids', () => {
    const h2 = {
      textContent: 'Hello World',
      id: '',
      cloneNode: () => ({
        textContent: 'Hello World',
        querySelectorAll: () => [],
      }),
    }
    const h3 = {
      textContent: '一、目标',
      id: '',
      cloneNode: () => ({
        textContent: '一、目标',
        querySelectorAll: () => [],
      }),
    }
    const root = {
      querySelectorAll: (sel: string) => (sel.startsWith('h') ? [h2, h3] : []),
    }
    assignPreviewHeadingIds(root as unknown as HTMLElement)
    expect(h2.id).toBe('Hello-World')
    expect(h3.id).toBe('一、目标')
  })
})
