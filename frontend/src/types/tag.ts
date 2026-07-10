/** 统一标签 */
export interface TagVO {
  id: number
  name: string
  color: string
  usageCount: number
  createdAt: string
}

/** 创建标签 */
export interface TagCreateDTO {
  name: string
  color?: string
}

/** 更新标签 */
export interface TagUpdateDTO {
  name: string
  color?: string
}
