export interface CategoryVO {
  id: number
  name: string
  type: 'note' | 'bookmark' | 'file'
  sortOrder: number
  count: number
  createdAt: string
}

export interface CategoryCreateDTO {
  name: string
  type: 'note' | 'bookmark' | 'file'
  sortOrder?: number
}

export interface CategoryUpdateDTO {
  name: string
  sortOrder?: number
}
