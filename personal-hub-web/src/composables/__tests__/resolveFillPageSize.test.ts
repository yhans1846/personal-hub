import { describe, it, expect } from 'vitest'
import { resolveFillPageSize } from '../resolveFillPageSize'

describe('resolveFillPageSize', () => {
  it('≥640 → 10', () => {
    expect(resolveFillPageSize(640)).toBe(10)
    expect(resolveFillPageSize(900)).toBe(10)
  })
  it('520–639 → 8', () => {
    expect(resolveFillPageSize(520)).toBe(8)
    expect(resolveFillPageSize(639)).toBe(8)
  })
  it('<520 → 6', () => {
    expect(resolveFillPageSize(519)).toBe(6)
    expect(resolveFillPageSize(0)).toBe(6)
  })
})
