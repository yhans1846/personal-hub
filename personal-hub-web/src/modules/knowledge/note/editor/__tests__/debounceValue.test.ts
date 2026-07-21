import { describe, it, expect, vi, beforeEach, afterEach } from 'vitest'
import { ref, nextTick } from 'vue'
import { useDebouncedRef } from '../debounceValue'

describe('useDebouncedRef', () => {
  beforeEach(() => { vi.useFakeTimers() })
  afterEach(() => { vi.useRealTimers() })

  it('updates after delay', async () => {
    const source = ref('a')
    const debounced = useDebouncedRef(source, 150)
    expect(debounced.value).toBe('a')
    source.value = 'b'
    expect(debounced.value).toBe('a')
    vi.advanceTimersByTime(150)
    await nextTick()
    expect(debounced.value).toBe('b')
  })
})
