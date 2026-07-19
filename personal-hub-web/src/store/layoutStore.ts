import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { getLayoutAll, saveLayout, resetLayout } from '@/api/layoutApi'
import { DEFAULT_MENU_ITEMS, DEFAULT_DASHBOARD_ITEMS, DEFAULT_STATS_CARDS, ensureDashboardCards, REMOVED_DASHBOARD_CODES } from '@/modules/dashboard/defaultLayouts'
import type { MenuItem, CardItem, LayoutItem, LayoutResponse } from '@/types/layout'
import { useThemeStore } from '@/store/themeStore'

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

function saveToStorage(key: string, data: unknown[]) {
  localStorage.setItem(key, JSON.stringify(data))
}

function toLayoutItem(item: MenuItem | CardItem): LayoutItem {
  return { code: item.code, visible: item.visible, order: item.order }
}

/**
 * 布局配置 Store（菜单 / 工作台 / 统计卡片）
 * 外观主题见 useThemeStore
 */
export const useLayoutStore = defineStore('layout', () => {
  const menuItems = ref<MenuItem[]>(migrateMenuItems(loadFromStorage<MenuItem>(STORAGE_KEY_MENU, DEFAULT_MENU_ITEMS)))
  const dashboardCards = ref<CardItem[]>(
    ensureDashboardCards(loadFromStorage<CardItem>(STORAGE_KEY_DASHBOARD, DEFAULT_DASHBOARD_ITEMS)),
  )
  saveToStorage(STORAGE_KEY_DASHBOARD, dashboardCards.value)
  const statsCards = ref<CardItem[]>(loadFromStorage<CardItem>(STORAGE_KEY_STATS, DEFAULT_STATS_CARDS))
  const loaded = ref(false)

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

  const visibleDashboardCards = computed(() => {
    return [...dashboardCards.value]
      .filter(card => card.visible)
      .sort((a, b) => a.order - b.order)
  })

  const visibleStatsCards = computed(() => {
    return [...statsCards.value]
      .filter(card => card.visible)
      .sort((a, b) => a.order - b.order)
  })

  async function fetchLayout() {
    try {
      const res = await getLayoutAll()
      const layouts = (res.data.data ?? []) as LayoutResponse[]
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
    await useThemeStore().fetchAppearanceFromBackend()
    loaded.value = true
  }

  async function saveMenuConfig(items: MenuItem[]) {
    menuItems.value = items
    saveToStorage(STORAGE_KEY_MENU, items)
    await saveLayout({
      layoutType: 'menu',
      layoutJson: JSON.stringify({ items: items.map(toLayoutItem) })
    })
  }

  async function saveDashboardConfig(items: CardItem[]) {
    dashboardCards.value = items
    saveToStorage(STORAGE_KEY_DASHBOARD, items)
    await saveLayout({
      layoutType: 'dashboard',
      layoutJson: JSON.stringify({ items: items.map(toLayoutItem) })
    })
  }

  async function resetMenuConfig() {
    menuItems.value = DEFAULT_MENU_ITEMS.map(item => ({ ...item }))
    saveToStorage(STORAGE_KEY_MENU, menuItems.value)
    await resetLayout('menu')
  }

  async function resetDashboardConfig() {
    dashboardCards.value = DEFAULT_DASHBOARD_ITEMS.map(item => ({ ...item }))
    saveToStorage(STORAGE_KEY_DASHBOARD, dashboardCards.value)
    await resetLayout('dashboard')
  }

  async function saveStatsConfig(items: CardItem[]) {
    statsCards.value = items
    saveToStorage(STORAGE_KEY_STATS, items)
    await saveLayout({
      layoutType: 'stats',
      layoutJson: JSON.stringify({ items: items.map(toLayoutItem) })
    })
  }

  async function resetStatsConfig() {
    statsCards.value = DEFAULT_STATS_CARDS.map(item => ({ ...item }))
    saveToStorage(STORAGE_KEY_STATS, statsCards.value)
    await resetLayout('stats')
  }

  async function resetAll() {
    await resetMenuConfig()
    await resetDashboardConfig()
    await resetStatsConfig()
  }

  function migrateMenuItems(items: MenuItem[]): MenuItem[] {
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
  }
})
