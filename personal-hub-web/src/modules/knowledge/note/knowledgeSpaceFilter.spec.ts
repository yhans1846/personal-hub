import { describe, expect, it } from 'vitest'
import {
  filterFolderTree,
  filterNoteSummaries,
  filterUncategorizedNotes,
  matchQuery,
} from './knowledgeSpaceFilter'
import type { NoteFolderNoteItem, NoteFolderVO } from '@/types/note'

function note(id: number, title: string): NoteFolderNoteItem {
  return { id, title, updatedAt: '2026-07-23' }
}

function folder(
  id: number,
  name: string,
  partial: Partial<NoteFolderVO> = {},
): NoteFolderVO {
  return {
    id,
    name,
    parentId: null,
    sortOrder: 0,
    ...partial,
  }
}

describe('matchQuery', () => {
  it('matches case-insensitively and ignores empty query', () => {
    expect(matchQuery('Hello World', 'hello')).toBe(true)
    expect(matchQuery('Hello World', '  ')).toBe(true)
    expect(matchQuery(null, 'x')).toBe(false)
  })
})

describe('filterNoteSummaries', () => {
  it('returns all when query is blank', () => {
    const list = [{ id: 1, title: 'Alpha' }, { id: 2, title: 'Beta' }]
    expect(filterNoteSummaries(list, '  ')).toEqual(list)
  })

  it('filters by title substring', () => {
    const list = [{ id: 1, title: 'Alpha' }, { id: 2, title: 'Beta' }]
    expect(filterNoteSummaries(list, 'alp').map((n) => n.id)).toEqual([1])
  })
})

describe('filterFolderTree', () => {
  const tree: NoteFolderVO[] = [
    folder(1, 'Work', {
      notes: [note(10, 'Meeting notes'), note(11, 'Budget')],
      children: [
        folder(2, 'Archive', {
          notes: [note(20, 'Old draft')],
        }),
      ],
    }),
    folder(3, 'Personal', {
      notes: [note(30, 'Journal')],
    }),
  ]

  it('keeps folder and all notes when folder name matches', () => {
    const result = filterFolderTree(tree, 'work')
    expect(result).toHaveLength(1)
    expect(result[0].name).toBe('Work')
    expect(result[0].notes?.map((n) => n.id)).toEqual([10, 11])
  })

  it('keeps ancestor when only descendant note matches', () => {
    const result = filterFolderTree(tree, 'draft')
    expect(result).toHaveLength(1)
    expect(result[0].name).toBe('Work')
    expect(result[0].children).toHaveLength(1)
    expect(result[0].children![0].notes?.map((n) => n.id)).toEqual([20])
  })
})

describe('filterUncategorizedNotes', () => {
  it('delegates to filterNoteSummaries', () => {
    const notes = [note(1, 'Todo'), note(2, 'Done')]
    expect(filterUncategorizedNotes(notes, 'todo').map((n) => n.id)).toEqual([1])
  })
})
