export interface ReadingVO {
  id: number
  bookTitle: string
  author: string
  coverUrl: string
  totalChapters: number
  currentChapter: number
  progress: number
  rating?: number
  totalDuration?: number
  status: number
  statusLabel: string
  notes: string
  startDate: string | null
  endDate: string | null
  createdAt: string
  updatedAt: string
}

export interface ReadingCreateDTO {
  bookTitle: string
  author?: string
  coverUrl?: string
  totalChapters?: number
  currentChapter?: number
  progress?: number
  rating?: number
  totalDuration?: number
  status?: number
  notes?: string
  startDate?: string | null
  endDate?: string | null
}

export interface ReadingQuery {
  page?: number
  size?: number
  keyword?: string
  status?: number | ''
  sortBy?: string
  sortDir?: 'asc' | 'desc'
}
