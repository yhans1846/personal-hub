import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { getLayoutAll, saveLayout, resetLayout } from '@/api/layoutApi'
import { useStorageSync } from '@/composables/useStorageSync'
import { DEFAULT_MENU_ITEMS, DEFAULT_DASHBOARD_ITEMS, DEFAULT_STATS_CARDS, ensureDashboardCards, REMOVED_DASHBOARD_CODES } from '@/modules/dashboard/defaultLayouts'
import type { MenuItem, CardItem, LayoutItem, AppearanceConfig, ExtendedAppearanceConfig } from '@/types/layout'

const STORAGE_KEY_MENU = 'layout-menu'
const STORAGE_KEY_DASHBOARD = 'layout-dashboard-v5'
const STORAGE_KEY_STATS = 'layout-stats'

function loadFromStorage<T>(key: string, defaults: T[]): T[] {
  try {
    const stored = localStorage.getItem(key)
    if (stored) return JSON.parse(stored)
  } catch { /* ignore */ }
  return defaults.map(item => ({ ...item }))
}

function saveToStorage(key: string, data: any[]) {
  localStorage.setItem(key, JSON.stringify(data))
}

function toLayoutItem(item: MenuItem | CardItem): LayoutItem {
  return { code: item.code, visible: item.visible, order: item.order }
}

/**
 * 布局配置 Store
 */
export const useLayoutStore = defineStore('layout', () => {
  // --- State ---
  const menuItems = ref<MenuItem[]>(migrateMenuItems(loadFromStorage<MenuItem>(STORAGE_KEY_MENU, DEFAULT_MENU_ITEMS)))
  const dashboardCards = ref<CardItem[]>(
    ensureDashboardCards(loadFromStorage<CardItem>(STORAGE_KEY_DASHBOARD, DEFAULT_DASHBOARD_ITEMS)),
  )
  // 补齐新增卡片后写回本地，避免刷新丢失
  saveToStorage(STORAGE_KEY_DASHBOARD, dashboardCards.value)
  const statsCards = ref<CardItem[]>(loadFromStorage<CardItem>(STORAGE_KEY_STATS, DEFAULT_STATS_CARDS))
  const loaded = ref(false)

  // --- Getters ---
  /** 按 section 分组的可见菜单，已排序 */
  const visibleMenuSections = computed(() => {
    const sections: { title: string; key: string; items: MenuItem[] }[] = [
      { title: '工作区', key: 'workspace', items: [] },
      { title: '知识', key: 'knowledge', items: [] },
      { title: '资源', key: 'resource', items: [] },
      { title: '管理', key: 'manage', items: [] },
      { title: '统计', key: 'stats', items: [] },
    ]
    const sorted = [...menuItems.value]
      .filter(item => item.visible)
      .sort((a, b) => a.order - b.order)

    for (const item of sorted) {
      const section = sections.find(s => s.key === item.section)
      if (section) section.items.push(item)
    }
    return sections.filter(s => s.items.length > 0)
  })

  /** 可见且已排序的 dashboard 卡片 */
  const visibleDashboardCards = computed(() => {
    return [...dashboardCards.value]
      .filter(card => card.visible)
      .sort((a, b) => a.order - b.order)
  })

  /** 可见且已排序的统计卡片 */
  const visibleStatsCards = computed(() => {
    return [...statsCards.value]
      .filter(card => card.visible)
      .sort((a, b) => a.order - b.order)
  })

  // --- Actions ---
  /** 从后端加载配置（合并到本地，保留展示字段） */
  async function fetchLayout() {
    try {
      const res = await getLayoutAll()
      const layouts = res.data.data
      for (const layout of layouts) {
        if (layout.layoutType === 'menu' && layout.layoutJson) {
          const parsed = JSON.parse(layout.layoutJson)
          if (parsed.items) {
            mergeMenuItems(parsed.items)
            menuItems.value = migrateMenuItems(menuItems.value)
            saveToStorage(STORAGE_KEY_MENU, menuItems.value)
          }
        }
        if (layout.layoutType === 'dashboard' && layout.layoutJson) {
          const parsed = JSON.parse(layout.layoutJson)
          if (parsed.items) {
            mergeDashboardCards(parsed.items)
            saveToStorage(STORAGE_KEY_DASHBOARD, dashboardCards.value)
          }
        }
        if (layout.layoutType === 'stats' && layout.layoutJson) {
          const parsed = JSON.parse(layout.layoutJson)
          if (parsed.items) {
            mergeStatsCards(parsed.items)
            saveToStorage(STORAGE_KEY_STATS, statsCards.value)
          }
        }
      }
    } catch {
      console.warn('获取布局配置失败，使用本地缓存')
    }
    await fetchAppearanceFromBackend()
    loaded.value = true
  }

  /** 保存菜单配置 */
  async function saveMenuConfig(items: MenuItem[]) {
    menuItems.value = items
    saveToStorage(STORAGE_KEY_MENU, items)
    await saveLayout({
      layoutType: 'menu',
      layoutJson: JSON.stringify({ items: items.map(toLayoutItem) })
    })
  }

  /** 保存 Dashboard 卡片配置 */
  async function saveDashboardConfig(items: CardItem[]) {
    dashboardCards.value = items
    saveToStorage(STORAGE_KEY_DASHBOARD, items)
    await saveLayout({
      layoutType: 'dashboard',
      layoutJson: JSON.stringify({ items: items.map(toLayoutItem) })
    })
  }

  /** 恢复默认菜单 */
  async function resetMenuConfig() {
    menuItems.value = DEFAULT_MENU_ITEMS.map(item => ({ ...item }))
    saveToStorage(STORAGE_KEY_MENU, menuItems.value)
    await resetLayout('menu')
  }

  /** 恢复默认 Dashboard */
  async function resetDashboardConfig() {
    dashboardCards.value = DEFAULT_DASHBOARD_ITEMS.map(item => ({ ...item }))
    saveToStorage(STORAGE_KEY_DASHBOARD, dashboardCards.value)
    await resetLayout('dashboard')
  }

  /** 保存统计卡片配置 */
  async function saveStatsConfig(items: CardItem[]) {
    statsCards.value = items
    saveToStorage(STORAGE_KEY_STATS, items)
    await saveLayout({
      layoutType: 'stats',
      layoutJson: JSON.stringify({ items: items.map(toLayoutItem) })
    })
  }

  /** 恢复默认统计卡片 */
  async function resetStatsConfig() {
    statsCards.value = DEFAULT_STATS_CARDS.map(item => ({ ...item }))
    saveToStorage(STORAGE_KEY_STATS, statsCards.value)
    await resetLayout('stats')
  }

  /** 恢复所有默认配置 */
  async function resetAll() {
    await resetMenuConfig()
    await resetDashboardConfig()
    await resetStatsConfig()
  }

  // ---- 外观配置（通过 useStorageSync 管理 localStorage + 后端同步） ----

  const STORAGE_KEY_APPEARANCE = 'appearance-config'

  const DEFAULT_APPEARANCE: ExtendedAppearanceConfig = {
    theme: 'light',
    accent: 'blue',
    borderRadius: 'lg',
    animationSpeed: 'normal',
    density: 'standard',
    contentWidth: 80,
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

  const appearanceSync = useStorageSync<ExtendedAppearanceConfig>({
    storageKey: STORAGE_KEY_APPEARANCE,
    defaults: DEFAULT_APPEARANCE,
    fetchApi: async () => {
      const res = await getLayoutAll()
      const appLayout = (res.data.data as any[]).find((l: any) => l.layoutType === 'appearance')
      if (!appLayout?.layoutJson) return null
      const parsed = JSON.parse(appLayout.layoutJson)
      return { ...parsed, contentWidth: normalizeContentWidth(parsed.contentWidth) }
    },
    saveApi: async (data) => {
      await saveLayout({
        layoutType: 'appearance',
        layoutJson: JSON.stringify(data),
      })
    },
    silent: true,  // 原 fetchAppearanceFromBackend 为静默 catch
    debugLabel: 'Appearance',
  })

  const appearanceConfig = appearanceSync.data
  // 本地旧配置迁移
  appearanceConfig.contentWidth = normalizeContentWidth(appearanceConfig.contentWidth)

  /** 将外观配置落到 document CSS 变量 / data-*（主题、圆角、动画、密度、内容宽） */
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

    // 圆角：选项值即基准（与设置页 4/8/12/16px 文案一致）
    const radiusBase: Record<string, number> = { sm: 4, md: 8, lg: 12, xl: 16 }
    const r = radiusBase[ext.borderRadius] ?? 12
    root.style.setProperty('--radius-sm', `${Math.max(2, Math.round(r * 0.5))}px`)
    root.style.setProperty('--radius-md', `${r}px`)
    root.style.setProperty('--radius-lg', `${r}px`)
    root.style.setProperty('--radius-xl', `${Math.round(r * 1.25)}px`)

    // 动画：全局 --transition 被大量组件引用
    const animMs: Record<string, string> = { off: '0ms', slow: '350ms', normal: '200ms', fast: '100ms' }
    const dur = animMs[ext.animationSpeed] ?? '200ms'
    root.style.setProperty('--transition-duration', dur)
    root.style.setProperty('--transition', `${dur} ease`)

    // 密度：缩放间距 token（main-content padding 等依赖 --sp-*）
    const densityScale: Record<string, number> = { comfortable: 1.25, standard: 1, compact: 0.75 }
    const s = densityScale[ext.density] ?? 1
    const spBase: Record<string, number> = {
      '1': 4, '2': 8, '3': 12, '4': 16, '5': 20, '6': 24, '8': 32, '10': 40, '12': 48,
    }
    for (const [k, v] of Object.entries(spBase)) {
      root.style.setProperty(`--sp-${k}`, `${Math.round(v * s)}px`)
    }
    root.style.setProperty('--sp-density', String(s))

    // 内容区宽度：相对主内容区百分比
    root.style.setProperty('--content-max-width', `${ext.contentWidth}%`)
  }

  // 本地缓存就绪后立刻应用（避免刷新后设置丢失）
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

  // --- Helpers ---
  /** 迁移旧菜单：检测旧 section 分配 → 静默重置默认；移除已废弃项 */
  function migrateMenuItems(items: MenuItem[]): MenuItem[] {
    // 1) 检测旧 section 缓存（与默认分配不匹配即判定为旧数据）
    const defaultSectionMap = new Map(DEFAULT_MENU_ITEMS.map(m => [m.code, m.section]))
    const needsSectionReset = items.some(item => {
      const expected = defaultSectionMap.get(item.code)
      return expected && item.section !== expected
    })
    if (needsSectionReset) {
      const defaults = DEFAULT_MENU_ITEMS.map(item => ({ ...item }))
      saveToStorage(STORAGE_KEY_MENU, defaults)
      return defaults
    }
    // 2) 移除已废弃的 code，补充默认中缺失的项
    const validCodes = new Set(DEFAULT_MENU_ITEMS.map(m => m.code))
    const migrated = items.filter(item => validCodes.has(item.code))
    if (migrated.length !== items.length) {
      const existingCodes = new Set(migrated.map(m => m.code))
      for (const def of DEFAULT_MENU_ITEMS) {
        if (!existingCodes.has(def.code)) {
          migrated.push({ ...def })
        }
      }
      saveToStorage(STORAGE_KEY_MENU, migrated)
    }
    return migrated
  }

  function mergeMenuItems(items: LayoutItem[]) {
    for (const incoming of items) {
      const existing = menuItems.value.find(m => m.code === incoming.code)
      if (existing) {
        existing.visible = incoming.visible
        existing.order = incoming.order
      }
    }
  }

  function mergeDashboardCards(items: LayoutItem[]) {
    const kept = items.filter(i => !REMOVED_DASHBOARD_CODES.has(i.code))
    const backendCodes = new Set(kept.map(i => i.code))
    const incomplete = DEFAULT_DASHBOARD_ITEMS.some(d => !backendCodes.has(d.code))

    if (incomplete) {
      // 后端仍是旧卡片集：只同步 visible，order 用 Bento 默认
      const visibleMap = new Map(kept.map(i => [i.code, i.visible]))
      dashboardCards.value = DEFAULT_DASHBOARD_ITEMS.map(def => ({
        ...def,
        visible: visibleMap.has(def.code) ? visibleMap.get(def.code)! : def.visible,
        order: def.order,
      }))
    } else {
      for (const incoming of kept) {
        const existing = dashboardCards.value.find(c => c.code === incoming.code)
        if (existing) {
          existing.visible = incoming.visible
          existing.order = incoming.order
        }
      }
      dashboardCards.value = ensureDashboardCards(dashboardCards.value)
    }
    saveToStorage(STORAGE_KEY_DASHBOARD, dashboardCards.value)
  }

  function mergeStatsCards(items: LayoutItem[]) {
    for (const incoming of items) {
      const existing = statsCards.value.find(c => c.code === incoming.code)
      if (existing) {
        existing.visible = incoming.visible
        existing.order = incoming.order
      }
    }
  }

  return {
    menuItems, dashboardCards, statsCards, loaded,
    visibleMenuSections, visibleDashboardCards, visibleStatsCards,
    fetchLayout, saveMenuConfig, saveDashboardConfig, saveStatsConfig,
    resetMenuConfig, resetDashboardConfig, resetStatsConfig, resetAll,
    appearanceConfig, DEFAULT_APPEARANCE,
    saveAppearanceConfig, resetAppearanceConfig,
  }
})
