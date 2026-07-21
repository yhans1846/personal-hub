import { describe, it, expect, vi } from 'vitest'
import { resolveEditorClose } from '../requestEditorClose'

describe('resolveEditorClose', () => {
  it('not dirty → allow immediately', async () => {
    const forceSave = vi.fn()
    const r = await resolveEditorClose({
      dirty: false,
      confirm: vi.fn(),
      forceSave,
    })
    expect(r).toBe('proceed')
    expect(forceSave).not.toHaveBeenCalled()
  })

  it('dirty + confirm save → forceSave then proceed', async () => {
    const forceSave = vi.fn().mockResolvedValue(undefined)
    const confirm = vi.fn().mockResolvedValue('save')
    const r = await resolveEditorClose({ dirty: true, confirm, forceSave })
    expect(forceSave).toHaveBeenCalled()
    expect(r).toBe('proceed')
  })

  it('dirty + discard → proceed without save', async () => {
    const forceSave = vi.fn()
    const confirm = vi.fn().mockResolvedValue('discard')
    const r = await resolveEditorClose({ dirty: true, confirm, forceSave })
    expect(forceSave).not.toHaveBeenCalled()
    expect(r).toBe('proceed')
  })

  it('dirty + cancel → abort', async () => {
    const forceSave = vi.fn()
    const confirm = vi.fn().mockResolvedValue('cancel')
    const r = await resolveEditorClose({ dirty: true, confirm, forceSave })
    expect(r).toBe('abort')
  })
})
