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

/** 外观配置 */
export interface AppearanceConfig {
  theme: 'light' | 'dark' | 'sepia'
  accent: 'blue' | 'purple' | 'pink' | 'red' | 'orange' | 'green' | 'teal' | 'cyan' | 'indigo'
}

/** 外观配置（含 Phase 2 扩展） */
export interface ExtendedAppearanceConfig extends AppearanceConfig {
  borderRadius: 'sm' | 'md' | 'lg' | 'xl'
  animationSpeed: 'off' | 'slow' | 'normal' | 'fast'
  density: 'comfortable' | 'standard' | 'compact'
  /** 主内容区最大宽度占比 50–100，默认 80 */
  contentWidth: number
}

/** 通知配置 */
export interface NotificationConfig {
  desktopEnabled: boolean
  enabledTypes: string[]
  soundEnabled: boolean
  soundName: string
  doNotDisturb: boolean
  dndStart: string
  dndEnd: string
}

/** 实验功能配置 */
export interface FeatureFlags {
  mermaid: boolean
  katex: boolean
  aiAssistant: boolean
  backlink: boolean
}
