/**
 * 布局配置类型定义
 */

/** 布局配置项（后端存储格式） */
export interface LayoutItem {
  code: string
  visible: boolean
  order: number
}

/** 菜单项（含展示信息） */
export interface MenuItem extends LayoutItem {
  title: string
  route?: string
  section: 'workspace' | 'knowledge' | 'planning' | 'resource' | 'manage' | 'stats'
  /** 首页不可隐藏 */
  fixed?: boolean
}

/** Dashboard 卡片项 */
export interface CardItem extends LayoutItem {
  title: string
}

/** API 响应格式 */
export interface LayoutResponse {
  layoutType: string
  layoutJson: string
}

/** 保存布局请求 */
export interface LayoutSaveRequest {
  layoutType: string
  layoutJson: string
}
