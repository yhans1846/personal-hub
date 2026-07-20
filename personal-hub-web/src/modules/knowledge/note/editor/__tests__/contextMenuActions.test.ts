import { describe, expect, it, vi } from 'vitest'
import type Vditor from 'vditor'
import {
  buildTableMarkdown,
  insertInlineMath,
  insertMathBlock,
  insertMermaid,
  nextFootnoteIndex,
  nextReferenceId,
  wrapHeading,
} from '../context-menu/contextMenuActions'

function mockVditor(selection = '') {
  return {
    insertMD: vi.fn(),
    updateValue: vi.fn(),
    getSelection: vi.fn(() => selection),
    focus: vi.fn(),
  } as unknown as Vditor & {
    insertMD: ReturnType<typeof vi.fn>
    updateValue: ReturnType<typeof vi.fn>
    getSelection: ReturnType<typeof vi.fn>
  }
}

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

  it('insertMermaid inserts mermaid fenced block', () => {
    const vditor = mockVditor()
    insertMermaid(vditor)
    expect(vditor.insertMD).toHaveBeenCalledWith(
      expect.stringContaining('```mermaid'),
    )
  })

  it('insertMathBlock inserts block math', () => {
    const vditor = mockVditor()
    insertMathBlock(vditor)
    expect(vditor.insertMD).toHaveBeenCalledWith(
      expect.stringContaining('$$\nE = mc^2\n$$'),
    )
  })

  it('insertInlineMath wraps selection with $', () => {
    const vditor = mockVditor('x^2')
    insertInlineMath(vditor)
    expect(vditor.updateValue).toHaveBeenCalledWith('$x^2$')
  })
})
