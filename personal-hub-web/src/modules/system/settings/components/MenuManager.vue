<script setup lang="ts">
import { ref, computed, onMounted, nextTick, onUnmounted } from 'vue'
import { useLayoutStore } from '@/store/layoutStore'
import { ElMessage } from 'element-plus'
import { GripVertical, Eye, EyeOff, ChevronDown } from 'lucide-vue-next'
import Sortable from 'sortablejs'
import type { MenuItem } from '@/types/layout'

const layoutStore = useLayoutStore()

const SECTION_LABELS: Record<string, string> = {
  workspace: '工作区',
  knowledge: '知识',
  resource: '资源',
  manage: '管理',
  stats: '统计',
}
const SECTION_ORDER = ['workspace', 'knowledge', 'resource', 'manage', 'stats']

// 按 section 分组
const menuGroups = computed(() => {
  const grouped = new Map<string, MenuItem[]>()
  for (const item of layoutStore.menuItems) {
    if (!grouped.has(item.section)) grouped.set(item.section, [])
    grouped.get(item.section)!.push(item)
  }
  return SECTION_ORDER
    .filter(key => grouped.has(key))
    .map(key => ({ key, title: SECTION_LABELS[key], items: grouped.get(key)! }))
})

// 分组折叠
const MGR_COLLAPSE_KEY = 'menu-mgr-collapsed'
const collapsedGroups = ref<Set<string>>(new Set(loadCollapsed()))
function loadCollapsed(): string[] {
  try { return JSON.parse(localStorage.getItem(MGR_COLLAPSE_KEY) || '[]') } catch { return [] }
}
function saveCollapsed() {
  localStorage.setItem(MGR_COLLAPSE_KEY, JSON.stringify([...collapsedGroups.value]))
}
function toggleGroup(key: string) {
  if (collapsedGroups.value.has(key)) collapsedGroups.value.delete(key)
  else collapsedGroups.value.add(key)
  saveCollapsed()
}

// Sortable：每组独立实例
const groupEls = ref<Record<string, HTMLElement | null>>({})
let sortableInstances: Sortable[] = []

/** 拖拽结束后按 DOM 顺序重建 sort order */
function rebuildOrder() {
  const ordered: MenuItem[] = []
  for (const group of menuGroups.value) {
    const el = groupEls.value[group.key]
    if (!el) continue
    const itemEls = el.querySelectorAll('.menu-item')
    itemEls.forEach(itemEl => {
      const code = (itemEl as HTMLElement).dataset.code
      const found = layoutStore.menuItems.find(m => m.code === code)
      if (found) ordered.push(found)
    })
  }
  // 补充游离项（防御）
  for (const item of layoutStore.menuItems) {
    if (!ordered.find(m => m.code === item.code)) ordered.push(item)
  }
  ordered.forEach((it, idx) => { it.order = idx + 1 })
  persist(ordered)
}

onMounted(async () => {
  await nextTick()
  for (const group of menuGroups.value) {
    const el = groupEls.value[group.key]
    if (!el) continue
    const sort = Sortable.create(el, {
      animation: 200,
      handle: '.drag-handle',
      ghostClass: 'menu-item--ghost',
      onEnd: rebuildOrder,
    })
    sortableInstances.push(sort)
  }
})

onUnmounted(() => {
  sortableInstances.forEach(s => s.destroy())
  sortableInstances = []
})

function toggleVisibility(item: MenuItem) {
  if (item.fixed) return
  item.visible = !item.visible
  persist([...layoutStore.menuItems])
}

function fixedHint(item: MenuItem): string {
  if (item.code === 'dashboard') return '首页不可隐藏'
  if (item.code === 'settings') return '系统设置不可隐藏'
  return ''
}

async function persist(items: MenuItem[]) {
  await layoutStore.saveMenuConfig(items)
}

async function handleReset() {
  await layoutStore.resetMenuConfig()
  ElMessage.success('菜单已恢复默认')
}
</script>

<template>
  <div class="menu-manager">
    <div class="manager-toolbar">
      <span class="manager-hint">拖拽排序，点击切换显示/隐藏</span>
      <el-button size="small" text @click="handleReset">恢复默认</el-button>
    </div>
    <div class="menu-groups">
      <div v-for="group in menuGroups" :key="group.key" class="menu-group">
        <div
          class="group-header"
          :class="{ collapsed: collapsedGroups.has(group.key) }"
          @click="toggleGroup(group.key)"
        >
          <ChevronDown :size="14" class="group-chevron" />
          <span>{{ group.title }}</span>
        </div>
        <Transition name="collapse">
          <div
            v-if="!collapsedGroups.has(group.key)"
            :ref="(el: any) => groupEls[group.key] = el as HTMLElement"
            class="group-items"
          >
            <div
              v-for="item in group.items"
              :key="item.code"
              :data-code="item.code"
              class="menu-item"
              :class="{ 'menu-item--hidden': !item.visible, 'menu-item--fixed': item.fixed }"
            >
              <span class="drag-handle"><GripVertical :size="16" /></span>
              <span class="menu-item-title">{{ item.title }}</span>
              <button
                class="vis-toggle"
                :class="{ 'is-hidden': !item.visible }"
                :disabled="item.fixed"
                :title="item.fixed ? fixedHint(item) : item.visible ? '点击隐藏' : '点击显示'"
                @click="toggleVisibility(item)"
              >
                <Eye v-if="item.visible" :size="16" />
                <EyeOff v-else :size="16" />
              </button>
            </div>
          </div>
        </Transition>
      </div>
    </div>
  </div>
</template>

<style scoped>
.manager-toolbar { display: flex; justify-content: space-between; align-items: center; margin-bottom: var(--sp-4); }
.manager-hint { font-size: var(--text-xs); color: var(--text-tertiary); }

.menu-groups { display: flex; flex-direction: column; gap: 32px; }

/* 分组标题 — Section 规范 */
.group-header {
  display: flex; align-items: center; gap: 6px;
  font-size: 13px; font-weight: 600;
  color: var(--text-secondary);
  padding: 0 0 8px 0;
  cursor: pointer; user-select: none;
  border-bottom: 1px solid var(--border-light);
  transition: color 150ms ease;
}
.group-header:hover { color: var(--text-primary); }
.group-chevron {
  flex-shrink: 0; transition: transform 200ms ease; opacity: 0.45;
}
.group-header:hover .group-chevron { opacity: 0.8; }
.group-header.collapsed .group-chevron { transform: rotate(-90deg); }

/* 分组内列表 */
.group-items { display: flex; flex-direction: column; gap: var(--sp-1); padding-top: 8px; }

.menu-item {
  display: flex; align-items: center; gap: var(--sp-3);
  padding: var(--sp-2) var(--sp-3) var(--sp-2) var(--sp-6);
  border-radius: var(--radius-sm);
  transition: background var(--transition);
  cursor: default;
}
.menu-item:hover { background: var(--bg-hover); }
.menu-item--hidden { opacity: 0.5; }
.menu-item--fixed { border-left: 3px solid var(--accent); }
.menu-item-title { flex: 1; font-size: var(--text-sm); }
.drag-handle { cursor: grab; color: var(--text-tertiary); display: flex; }
.drag-handle:active { cursor: grabbing; }
.vis-toggle {
  width: 28px; height: 28px; display: flex; align-items: center; justify-content: center;
  border: none; background: transparent; color: var(--text-secondary);
  cursor: pointer; border-radius: var(--radius-sm);
  transition: all var(--transition);
}
.vis-toggle:hover { background: var(--bg-hover); }
.vis-toggle.is-hidden { color: var(--text-placeholder); }
.vis-toggle:disabled { opacity: 0.4; cursor: not-allowed; }
.menu-item--ghost { opacity: 0.3; background: var(--accent-light); }

/* 折叠动画 */
.collapse-enter-active,
.collapse-leave-active {
  transition: max-height 200ms ease, opacity 200ms ease;
  overflow: hidden;
}
.collapse-enter-from,
.collapse-leave-to {
  max-height: 0;
  opacity: 0;
}
.collapse-enter-to,
.collapse-leave-from {
  max-height: 400px;
  opacity: 1;
}
</style>
