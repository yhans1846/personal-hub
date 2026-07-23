import { defineStore } from 'pinia'
import { getLayoutAll, saveLayout } from '@/api/layoutApi'
import { useStorageSync } from '@/composables/useStorageSync'
import type { AppearanceConfig, ExtendedAppearanceConfig, LayoutResponse } from '@/types/layout'
import { applyUiFontToDOM, DEFAULT_UI_FONT, normalizeUiFont } from '@/utils/uiFonts'

const STORAGE_KEY_APPEARANCE = 'appearance-config'

const DEFAULT_APPEARANCE: ExtendedAppearanceConfig = {
  theme: 'light',
  accent: 'blue',
  borderRadius: 'lg',
  animationSpeed: 'normal',
  density: 'standard',
  contentWidth: 80,
  uiFont: DEFAULT_UI_FONT,
}

/** 兼容旧枚举 narrow/standard/wide/full → 百分比 */
function normalizeContentWidth(raw: unknown): number {
  if (typeof raw === 'number' && Number.isFinite(raw)) {
    return Math.min(100, Math.max(50, Math.round(raw)))
  }
  const legacy: Record<string, number> = {
    narrow: 60, standard: 75, wide: 90, full: 100,
  }
  if (typeof raw === 'string' && legacy[raw] != null) return legacy[raw]
  return 80
}

/** 将外观配置落到 document CSS 变量 / data-* */
function applyAppearanceToDOM(config: AppearanceConfig | ExtendedAppearanceConfig) {
  const ext = { ...DEFAULT_APPEARANCE, ...config } as ExtendedAppearanceConfig
  ext.contentWidth = normalizeContentWidth(ext.contentWidth)
  const root = document.documentElement

  root.setAttribute('data-theme', ext.theme)
  root.setAttribute('data-accent', ext.accent)
  root.setAttribute('data-density', ext.density)
  root.setAttribute('data-radius', ext.borderRadius)
  root.setAttribute('data-anim', ext.animationSpeed)
  root.setAttribute('data-content-width', String(ext.contentWidth))
  localStorage.setItem('theme-preference', ext.theme)
  localStorage.setItem('accent-preference', ext.accent)

  const radiusBase: Record<string, number> = { sm: 4, md: 8, lg: 12, xl: 16 }
  const r = radiusBase[ext.borderRadius] ?? 12
  root.style.setProperty('--radius-sm', `${Math.max(2, Math.round(r * 0.5))}px`)
  root.style.setProperty('--radius-md', `${r}px`)
  root.style.setProperty('--radius-lg', `${r}px`)
  root.style.setProperty('--radius-xl', `${Math.round(r * 1.25)}px`)
  root.style.setProperty('--radius-full', '9999px')

  const animMs: Record<string, string> = { off: '0ms', slow: '350ms', normal: '200ms', fast: '100ms' }
  const dur = animMs[ext.animationSpeed] ?? '200ms'
  root.style.setProperty('--transition-duration', dur)
  root.style.setProperty('--transition', `${dur} ease`)

  const densityScale: Record<string, number> = { comfortable: 1.25, standard: 1, compact: 0.75 }
  const s = densityScale[ext.density] ?? 1
  const spBase: Record<string, number> = {
    '1': 4, '2': 8, '3': 12, '4': 16, '5': 20, '6': 24, '8': 32, '10': 40, '12': 48,
  }
  for (const [k, v] of Object.entries(spBase)) {
    root.style.setProperty(`--sp-${k}`, `${Math.round(v * s)}px`)
  }
  root.style.setProperty('--sp-density', String(s))
  root.style.setProperty('--content-max-width', `${ext.contentWidth}%`)

  ext.uiFont = normalizeUiFont(ext.uiFont)
  applyUiFontToDOM(ext.uiFont)

  requestAnimationFrame(() => {
    const accent = getComputedStyle(root).getPropertyValue('--accent').trim()
    if (accent) {
      root.style.setProperty('--el-color-primary', accent)
    }
  })
}

/**
 * 主题 / 外观配置（与 layout 菜单、工作台卡片分离）
 */
export const useThemeStore = defineStore('theme', () => {
  const appearanceSync = useStorageSync<ExtendedAppearanceConfig>({
    storageKey: STORAGE_KEY_APPEARANCE,
    defaults: DEFAULT_APPEARANCE,
    fetchApi: async () => {
      const res = await getLayoutAll()
      const layouts = (res.data.data ?? []) as LayoutResponse[]
      const appLayout = layouts.find((l) => l.layoutType === 'appearance')
      if (!appLayout?.layoutJson) return null
      const parsed = JSON.parse(appLayout.layoutJson) as Partial<ExtendedAppearanceConfig>
      return {
        ...parsed,
        contentWidth: normalizeContentWidth(parsed.contentWidth),
        uiFont: normalizeUiFont(parsed.uiFont),
      }
    },
    saveApi: async (data) => {
      await saveLayout({
        layoutType: 'appearance',
        layoutJson: JSON.stringify(data),
      })
    },
    silent: true,
    debugLabel: 'Appearance',
  })

  const appearanceConfig = appearanceSync.data
  appearanceConfig.contentWidth = normalizeContentWidth(appearanceConfig.contentWidth)
  appearanceConfig.uiFont = normalizeUiFont(appearanceConfig.uiFont)
  applyAppearanceToDOM(appearanceConfig)

  async function fetchAppearanceFromBackend() {
    await appearanceSync.fetchFromBackend()
    applyAppearanceToDOM(appearanceConfig)
  }

  async function saveAppearanceConfig(config: ExtendedAppearanceConfig) {
    Object.assign(appearanceConfig, { ...DEFAULT_APPEARANCE, ...config })
    appearanceSync.saveToLocal(appearanceConfig)
    applyAppearanceToDOM(appearanceConfig)
    await appearanceSync.saveToBackend(appearanceConfig)
  }

  async function resetAppearanceConfig() {
    await saveAppearanceConfig({ ...DEFAULT_APPEARANCE })
  }

  return {
    appearanceConfig,
    DEFAULT_APPEARANCE,
    fetchAppearanceFromBackend,
    saveAppearanceConfig,
    resetAppearanceConfig,
  }
})
