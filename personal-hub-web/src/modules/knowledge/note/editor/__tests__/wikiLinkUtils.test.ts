import { describe, expect, it } from 'vitest'
import {
  contentHasWikiLinkTo,
  extractOpenWikiQuery,
  parseWikiLinks,
  transformWikiLinks,
} from '../wikiLinkUtils'

describe('wikiLinkUtils', () => {
  it('parses title and alias', () => {
    const links = parseWikiLinks('see [[线性代数]] and [[考试计划|计划]]')
    expect(links).toHaveLength(2)
    expect(links[0]).toMatchObject({ title: '线性代数' })
    expect(links[1]).toMatchObject({ title: '考试计划', alias: '计划' })
  })

  it('contentHasWikiLinkTo matches exact title with alias', () => {
    expect(contentHasWikiLinkTo('x [[A]] y', 'A')).toBe(true)
    expect(contentHasWikiLinkTo('x [[A|别名]] y', 'A')).toBe(true)
    expect(contentHasWikiLinkTo('x [[AB]] y', 'A')).toBe(false)
  })

  it('transformWikiLinks resolves and marks missing', () => {
    const md = transformWikiLinks('go [[已有]] and [[没有]]', (t) => (t === '已有' ? 42 : null))
    expect(md).toContain('[已有](/notes/42/preview)')
    expect(md).toContain('[没有](#wiki-missing:')
  })

  it('extractOpenWikiQuery detects unclosed [[', () => {
    expect(extractOpenWikiQuery('hello [[线')).toBe('线')
    expect(extractOpenWikiQuery('hello [[线]] more')).toBe(null)
    expect(extractOpenWikiQuery('no wiki')).toBe(null)
  })
})
