/** 全站 UI 字体预置（阅读/Markdown 不跟此联动） */

export type UiFontKey = 'source-sans' | 'source-serif' | 'lxgw-wenkai' | 'inter' | 'system'

export interface UiFontOption {
  key: UiFontKey
  label: string
  /** CSS font-family 栈 */
  stack: string
  /** Google Fonts / Bunny 样式表；null = 不加载 Web 字体 */
  stylesheetHref: string | null
}

export const UI_FONT_OPTIONS: UiFontOption[] = [
  {
    key: 'source-sans',
    label: '思源黑体',
    stack: `"Noto Sans SC", "Source Han Sans SC", -apple-system, BlinkMacSystemFont, sans-serif`,
    stylesheetHref:
      'https://fonts.bunny.net/css?family=noto-sans-sc:400,500,700&display=swap',
  },
  {
    key: 'source-serif',
    label: '思源宋体',
    stack: `"Noto Serif SC", "Source Han Serif SC", Georgia, "Times New Roman", serif`,
    stylesheetHref:
      'https://fonts.bunny.net/css?family=noto-serif-sc:400,500,700&display=swap',
  },
  {
    key: 'lxgw-wenkai',
    label: '霞鹜文楷',
    stack: `"LXGW WenKai", "LXGW WenKai Screen", "PingFang SC", "Microsoft YaHei", serif`,
    stylesheetHref:
      'https://cdn.jsdelivr.net/npm/@fontsource/lxgw-wenkai@5.2.5/chinese-simplified-400.css',
  },
  {
    key: 'inter',
    label: 'Inter',
    stack: `Inter, -apple-system, BlinkMacSystemFont, "Segoe UI", "PingFang SC", "Microsoft YaHei", sans-serif`,
    stylesheetHref: 'https://fonts.bunny.net/css?family=inter:400,500,600,700&display=swap',
  },
  {
    key: 'system',
    label: '系统默认',
    stack: `-apple-system, BlinkMacSystemFont, "Segoe UI", "Noto Sans SC", "PingFang SC", "Microsoft YaHei", system-ui, sans-serif`,
    stylesheetHref: null,
  },
]

export const DEFAULT_UI_FONT: UiFontKey = 'source-sans'

const loadedHrefs = new Set<string>()

/** 测试用：清空已注入 stylesheet 缓存 */
export function resetUiFontStylesheetCache() {
  loadedHrefs.clear()
}

export function normalizeUiFont(raw: unknown): UiFontKey {
  if (typeof raw === 'string' && UI_FONT_OPTIONS.some((o) => o.key === raw)) {
    return raw as UiFontKey
  }
  return DEFAULT_UI_FONT
}

export function getUiFontOption(key: UiFontKey): UiFontOption {
  return UI_FONT_OPTIONS.find((o) => o.key === key) ?? UI_FONT_OPTIONS[0]!
}

/** 按需注入 stylesheet（已加载则跳过） */
export function ensureUiFontStylesheet(href: string | null) {
  if (!href || loadedHrefs.has(href) || typeof document === 'undefined') return
  const id = `ph-ui-font-${btoa(href).replace(/[^a-zA-Z0-9]/g, '').slice(0, 24)}`
  if (document.getElementById(id)) {
    loadedHrefs.add(href)
    return
  }
  const link = document.createElement('link')
  link.id = id
  link.rel = 'stylesheet'
  link.href = href
  document.head.appendChild(link)
  loadedHrefs.add(href)
}

/** 应用到 document：CSS 变量 + data-ui-font */
export function applyUiFontToDOM(raw: unknown) {
  const key = normalizeUiFont(raw)
  const opt = getUiFontOption(key)
  ensureUiFontStylesheet(opt.stylesheetHref)
  const root = document.documentElement
  root.style.setProperty('--font-sans', opt.stack)
  root.setAttribute('data-ui-font', key)
}
