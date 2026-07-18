import request from '@/api/request'
import type { Result, PageResult } from '@/types/common'
import type { NoteVO, NoteCreateDTO, NoteQuery, ImportReport } from '@/types/note'
import type { DiaryVO, DiaryCreateDTO, DiaryQuery } from '@/types/diary'
import type { StudyRecordVO, StudyRecordCreateDTO, StudyRecordQuery } from '@/types/study'
import type { ReadingVO, ReadingCreateDTO, ReadingQuery } from '@/types/reading'
import type { TagVO, TagCreateDTO, TagUpdateDTO } from '@/types/tag'
import type { CategoryVO, CategoryCreateDTO, CategoryUpdateDTO } from '@/types/category'

// ====== 笔记 ======
export function getNoteList(params: NoteQuery) {
  return request.get<Result<PageResult<NoteVO>>>('/notes', { params })
}
export function getNoteById(id: number) {
  return request.get<Result<NoteVO>>(`/notes/${id}`)
}
export function createNote(data: NoteCreateDTO) {
  return request.post<Result<NoteVO>>('/notes', data)
}
export function updateNote(id: number, data: NoteCreateDTO) {
  return request.put<Result<NoteVO>>(`/notes/${id}`, data)
}
export function deleteNote(id: number) {
  return request.delete<Result<void>>(`/notes/${id}`)
}
export function toggleFavorite(id: number) {
  return request.put<Result<void>>(`/notes/${id}/favorite`)
}
export function getRecentNotes(page = 1, size = 10) {
  return request.get<Result<PageResult<NoteVO>>>('/notes/recent', { params: { page, size } })
}
export function restoreNote(id: number) {
  return request.patch<Result<void>>(`/notes/${id}/restore`)
}
export function permanentDeleteNote(id: number) {
  return request.delete<Result<void>>(`/notes/${id}/permanent`)
}
export function getRecycleList(params: NoteQuery) {
  return request.get<Result<PageResult<NoteVO>>>('/notes/recycle', { params })
}
export function getNotePreview(id: number) {
  return request.get<Result<NoteVO>>(`/notes/${id}/preview`)
}

export function exportNote(id: number) {
  return request.get(`/notes/${id}/export`, { responseType: 'blob' })
}

/** 导入 Markdown 文件 */
export function importMarkdownFile(
  file: File,
  title?: string,
  categoryIds?: number[],
  tagIds?: number[],
  baseDir?: string,
) {
  const form = new FormData()
  form.append('file', file)
  if (title) form.append('title', title)
  if (categoryIds?.length) categoryIds.forEach(id => form.append('categoryIds', String(id)))
  if (tagIds?.length) tagIds.forEach(id => form.append('tagIds', String(id)))
  if (baseDir) form.append('baseDir', baseDir)
  return request.post<Result<ImportReport>>('/notes/import', form)
}

/** 粘贴 Markdown 内容导入 */
export function importMarkdownContent(data: {
  content: string
  title?: string
  categoryIds?: number[]
  tagIds?: number[]
}) {
  return request.post<Result<ImportReport>>('/notes/import-content', data)
}

export function uploadNoteImage(noteId: number, file: File) {
  const form = new FormData()
  form.append('file', file)
  return request.post<Result<Record<string, string>>>(`/notes/${noteId}/images`, form)
}

// ====== 日记 ======
export function getDiaryList(params: DiaryQuery) {
  return request.get<Result<PageResult<DiaryVO>>>('/diaries', { params })
}
export function getDiaryByMonth(month: string) {
  return request.get<Result<DiaryVO[]>>('/diaries/month', { params: { month } })
}
export function getDiaryById(id: number) {
  return request.get<Result<DiaryVO>>(`/diaries/${id}`)
}
export function createDiary(data: DiaryCreateDTO) {
  return request.post<Result<DiaryVO>>('/diaries', data)
}
export function updateDiary(id: number, data: DiaryCreateDTO) {
  return request.put<Result<DiaryVO>>(`/diaries/${id}`, data)
}
export function deleteDiary(id: number) {
  return request.delete<Result<void>>(`/diaries/${id}`)
}
export function uploadDiaryImage(diaryId: number, file: File) {
  const form = new FormData()
  form.append('file', file)
  return request.post<Result<{ name: string }>>(`/diaries/${diaryId}/images`, form)
}
export function deleteDiaryImage(diaryId: number, filename: string) {
  return request.delete<Result<void>>(`/diaries/${diaryId}/images/${encodeURIComponent(filename)}`)
}

// ====== 学习记录 ======
export function getStudyRecordList(params: StudyRecordQuery) {
  return request.get<Result<PageResult<StudyRecordVO>>>('/study-records', { params })
}
export function getStudyRecordById(id: number) {
  return request.get<Result<StudyRecordVO>>(`/study-records/${id}`)
}
export function createStudyRecord(data: StudyRecordCreateDTO) {
  return request.post<Result<StudyRecordVO>>('/study-records', data)
}
export function updateStudyRecord(id: number, data: StudyRecordCreateDTO) {
  return request.put<Result<StudyRecordVO>>(`/study-records/${id}`, data)
}
export function deleteStudyRecord(id: number) {
  return request.delete<Result<void>>(`/study-records/${id}`)
}
export interface StudyStats {
  todayDuration: number
  weekDuration: number
  streak: number
}
export function getStudyStats() {
  return request.get<Result<StudyStats>>('/study-records/stats')
}

// ====== 阅读记录 ======
export function getReadingList(params: ReadingQuery) {
  return request.get<Result<PageResult<ReadingVO>>>('/readings', { params })
}
export function getReadingById(id: number) {
  return request.get<Result<ReadingVO>>(`/readings/${id}`)
}
export function createReading(data: ReadingCreateDTO) {
  return request.post<Result<ReadingVO>>('/readings', data)
}
export function updateReading(id: number, data: ReadingCreateDTO) {
  return request.put<Result<ReadingVO>>(`/readings/${id}`, data)
}
export function deleteReading(id: number) {
  return request.delete<Result<void>>(`/readings/${id}`)
}
/** 导出阅读记录 XLSX；scope=filtered 带筛选，scope=all 全部；直接 blob 下载，不落盘 */
export function exportReadings(
  scope: 'filtered' | 'all',
  params?: Omit<ReadingQuery, 'page' | 'size'>,
) {
  return request.get('/readings/export', {
    params: { scope, ...params },
    responseType: 'blob',
  })
}

// ====== 标签 ======
export function getTags() {
  return request.get<Result<TagVO[]>>('/tags')
}
export function createTag(data: TagCreateDTO) {
  return request.post<Result<TagVO>>('/tags', data)
}
export function updateTag(id: number, data: TagUpdateDTO) {
  return request.put<Result<void>>(`/tags/${id}`, data)
}
export function deleteTag(id: number) {
  return request.delete<Result<void>>(`/tags/${id}`)
}

// ====== 分类 ======
export function getCategories(type: string) {
  return request.get<Result<CategoryVO[]>>('/categories', { params: { type } })
}
export function createCategory(data: CategoryCreateDTO) {
  return request.post<Result<CategoryVO>>('/categories', data)
}
export function updateCategory(id: number, data: CategoryUpdateDTO) {
  return request.put<Result<CategoryVO>>(`/categories/${id}`, data)
}
export function deleteCategory(id: number) {
  return request.delete<Result<void>>(`/categories/${id}`)
}
export function batchUpdateCategorySort(list: { id: number; sortOrder: number }[]) {
  return request.put<Result<void>>('/categories/sort', list)
}
