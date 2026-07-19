/** 主题初始化与布局壳层挂在 window 上的钩子 */
interface Window {
  /** 用户已手动选过主题时，禁止跟随系统 prefers-color-scheme */
  __themeUserOverride?: boolean
}
