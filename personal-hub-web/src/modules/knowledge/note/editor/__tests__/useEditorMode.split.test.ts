import { describe, it, expect } from 'vitest'
import { nextPreviewToggle, initialEditorMode } from '../useEditorMode'

describe('editor mode helpers', () => {
  it('toggle preview from split → preview', () => {
    expect(nextPreviewToggle('split')).toBe('preview')
  })
  it('toggle preview from preview → split', () => {
    expect(nextPreviewToggle('preview')).toBe('split')
  })
  it('toggle from edit → preview', () => {
    expect(nextPreviewToggle('edit')).toBe('preview')
  })
  it('mobile initial edit', () => {
    expect(initialEditorMode(375)).toBe('edit')
  })
  it('desktop initial split', () => {
    expect(initialEditorMode(1280)).toBe('split')
  })
})
