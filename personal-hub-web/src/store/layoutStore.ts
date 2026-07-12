import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { getLayoutAll, saveLayout, resetLayout } from '@/api/layoutApi'
import { DEFAULT_MENU_ITEMS, DEFAULT_DASHBOARD_ITEMS } from '@/modules/dashboard/defaultLayouts'
import type { MenuItem, CardItem, LayoutItem, AppearanceConfig } from '@/types/layout'

const STORAGE_KEY_MENU = 'layout-menu'
const STORAGE_KEY_DASHBOARD = 'layout-dashboard'

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
  const dashboardCards = ref<CardItem[]>(loadFromStorage<CardItem>(STORAGE_KEY_DASHBOARD, DEFAULT_DASHBOARD_ITEMS))
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

  /** 恢复所有默认配置 */
  async function resetAll() {
    await resetMenuConfig()
    await resetDashboardConfig()
  }

  // ---- 外观配置 ----
  const STORAGE_KEY_APPEARANCE = 'appearance-config'

  const DEFAULT_APPEARANCE: AppearanceConfig = {
    theme: 'light',
    accent: 'blue',
  }

  const appearanceConfig = ref<AppearanceConfig>(loadAppearance())

  function loadAppearance(): AppearanceConfig {
    try {
      const stored = localStorage.getItem(STORAGE_KEY_APPEARANCE)
      if (stored) return { ...DEFAULT_APPEARANCE, ...JSON.parse(stored) }
    } catch { /* ignore */ }
    return { ...DEFAULT_APPEARANCE }
  }

  function saveAppearanceLocally(config: AppearanceConfig) {
    localStorage.setItem(STORAGE_KEY_APPEARANCE, JSON.stringify(config))
  }

  async function fetchAppearanceFromBackend() {
    try {
      const res = await getLayoutAll()
      const appLayout = (res.data.data as any[]).find((l: any) => l.layoutType === 'appearance')
      if (appLayout?.layoutJson) {
        const parsed = JSON.parse(appLayout.layoutJson)
        Object.assign(appearanceConfig.value, { ...DEFAULT_APPEARANCE, ...parsed })
        saveAppearanceLocally(appearanceConfig.value)
        applyAppearanceToDOM(appearanceConfig.value)
      }
    } catch { /* ignore */ }
  }

  function applyAppearanceToDOM(config: AppearanceConfig) {
    document.documentElement.setAttribute('data-theme', config.theme)
    document.documentElement.setAttribute('data-accent', config.accent)
    localStorage.setItem('theme-preference', config.theme)
    localStorage.setItem('accent-preference', config.accent)
  }

  async function saveAppearanceConfig(config: AppearanceConfig) {
    appearanceConfig.value = config
    saveAppearanceLocally(config)
    applyAppearanceToDOM(config)
    await saveLayout({
      layoutType: 'appearance',
      layoutJson: JSON.stringify({ theme: config.theme, accent: config.accent }),
    })
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
    for (const incoming of items) {
      const existing = dashboardCards.value.find(c => c.code === incoming.code)
      if (existing) {
        existing.visible = incoming.visible
        existing.order = incoming.order
      }
    }
  }

  return {
    menuItems, dashboardCards, loaded,
    visibleMenuSections, visibleDashboardCards,
    fetchLayout, saveMenuConfig, saveDashboardConfig,
    resetMenuConfig, resetDashboardConfig, resetAll,
    appearanceConfig, DEFAULT_APPEARANCE,
    saveAppearanceConfig, resetAppearanceConfig,
  }
})
