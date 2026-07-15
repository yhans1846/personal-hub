import { describe, expect, it } from 'vitest'
import {
  buildTableMarkdown,
  nextFootnoteIndex,
  nextReferenceId,
  wrapHeading,
} from '../context-menu/contextMenuActions'

describe('contextMenuActions', () => {
  it('buildTableMarkdown generates GFM table', () => {
    const md = buildTableMarkdown(2, 3)
    expect(md).toContain('| --- | --- | --- |')
    expect(md.split('\n').filter((l) => l.startsWith('|')).length).toBe(3)
  })

  it('nextFootnoteIndex increments from existing footnotes', () => {
    expect(nextFootnoteIndex('hello[^1] world[^3]')).toBe(4)
    expect(nextFootnoteIndex('no footnotes')).toBe(1)
  })

  it('nextReferenceId increments from reference definitions', () => {
    expect(nextReferenceId('[ref1]: https://a.com\n[ref2]: https://b.com')).toBe('ref3')
  })

  it('wrapHeading applies heading prefix per line', () => {
    expect(wrapHeading(2, 'line1\nline2')).toBe('## line1\n## line2')
  })
})
