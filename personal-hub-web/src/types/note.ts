/** 笔记 */
export interface NoteVO {
  id: number
  title: string
  content: string
  /** 列表摘要（无全文时用于卡片预览） */
  excerpt?: string
  /** 所属文件夹，null/undefined = 未分类 */
  folderId?: number | null
  isFavorite: number
  isDeleted: number
  createdAt: string
  updatedAt: string
  deletedAt?: string
  deleteReason?: string
  categories: CategoryItem[]
  tags: TagItem[]
}

export interface CategoryItem {
  id: number
  name: string
}

export interface TagItem {
  id: number
  name: string
  color?: string
}

/** 创建/编辑笔记 */
export interface NoteCreateDTO {
  title: string
  content: string
  categoryIds: number[]
  tagIds: number[]
  /** 新建时可指定文件夹；null = 未分类 */
  folderId?: number | null
}

/** 导入报告 */
export interface ImportReport {
  total: number
  success: number
  skipped: number
  failed: number
  resources: ImportResourceResult[]
  noteId?: number
  rewrittenContent?: string
  warning?: string
}

/** 单条资源导入结果 */
export interface ImportResourceResult {
  originalRef: string
  resolvedPath?: string
  success: boolean
  skipped: boolean
  message: string
  /** 前端多文件聚合时标记所属笔记 */
  noteLabel?: string
}

/** 笔记查询参数 */
export interface NoteQuery {
  page?: number
  size?: number
  keyword?: string
  categoryId?: number
  tagId?: number
  isFavorite?: boolean
  isDeleted?: boolean
  /**
   * 文件夹筛选：省略/`all` = 全部；`none` = 未分类；数字 = 该夹直属笔记
   */
  folderId?: string | number
}

/** 文件夹树中的笔记摘要 */
export interface NoteFolderNoteItem {
  id: number
  title: string
  folderId?: number | null
  updatedAt: string
}

/** 笔记文件夹树节点 */
export interface NoteFolderVO {
  id: number
  name: string
  parentId: number | null
  sortOrder: number
  /** 直属笔记数（不含子夹） */
  noteCount?: number
  notes?: NoteFolderNoteItem[]
  children?: NoteFolderVO[]
}

/** 文件夹树接口（含全部/未分类计数） */
export interface NoteFolderTreeVO {
  folders: NoteFolderVO[]
  totalCount: number
  uncategorizedCount: number
  uncategorizedNotes?: NoteFolderNoteItem[]
}

/** 列表树选中：首页 / 全部 / 未分类 / 用户文件夹 id */
export type NoteFolderSelection = 'home' | 'all' | 'none' | number
