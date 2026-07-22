<script setup lang="ts">
import { ref, computed, onMounted, nextTick, onUnmounted } from 'vue'
import { useLayoutStore } from '@/store/layoutStore'
import { ElMessage } from 'element-plus'
import { GripVertical } from 'lucide-vue-next'
import Sortable from 'sortablejs'
import type { MenuItem } from '@/types/layout'
import UiTooltip from '@/components/UiTooltip.vue'

const layoutStore = useLayoutStore()

const SECTION_LABELS: Record<string, string> = {
  workspace: '工作区',
  knowledge: '知识',
  resource: '资源',
  manage: '管理',
  stats: '统计',
}
const SECTION_ORDER = ['workspace', 'knowledge', 'resource', 'manage', 'stats']

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

const groupEls = ref<Record<string, HTMLElement | null>>({})
let sortableInstances: Sortable[] = []

function rebuildOrder() {
  const ordered: MenuItem[] = []
  for (const group of menuGroups.value) {
    const el = groupEls.value[group.key]
    if (!el) continue
    el.querySelectorAll('.menu-chip').forEach(itemEl => {
      const code = (itemEl as HTMLElement).dataset.code
      const found = layoutStore.menuItems.find(m => m.code === code)
      if (found) ordered.push(found)
    })
  }
  for (const item of layoutStore.menuItems) {
    if (!ordered.find(m => m.code === item.code)) ordered.push(item)
  }
  ordered.forEach((it, idx) => { it.order = idx + 1 })
  persist(ordered)
}

function destroySortables() {
  sortableInstances.forEach(s => s.destroy())
  sortableInstances = []
}

async function initSortables() {
  destroySortables()
  await nextTick()
  for (const group of menuGroups.value) {
    const el = groupEls.value[group.key]
    if (!el) continue
    sortableInstances.push(Sortable.create(el, {
      animation: 180,
      handle: '.chip-handle',
      ghostClass: 'menu-chip--ghost',
      onEnd: rebuildOrder,
    }))
  }
}

onMounted(() => { initSortables() })
onUnmounted(() => { destroySortables() })

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
  await initSortables()
}
</script>

<template>
  <div class="menu-manager">
    <div class="manager-toolbar">
      <span class="manager-hint">点击标签显隐 · 拖把手排序</span>
      <el-button size="small" text @click="handleReset">恢复默认</el-button>
    </div>

    <div class="menu-matrix">
      <div v-for="group in menuGroups" :key="group.key" class="matrix-row">
        <div class="matrix-label">{{ group.title }}</div>
        <div
          :ref="(el: any) => groupEls[group.key] = el as HTMLElement"
          class="matrix-chips"
        >
          <UiTooltip
            v-for="item in group.items"
            :key="item.code"
            :content="item.fixed ? fixedHint(item) : (item.visible ? '点击隐藏' : '点击显示')"
          >
            <button
              type="button"
              class="menu-chip"
              :class="{
                'menu-chip--hidden': !item.visible,
                'menu-chip--fixed': item.fixed,
              }"
              :data-code="item.code"
              @click="toggleVisibility(item)"
            >
              <UiTooltip content="拖拽排序">
                <span class="chip-handle" @click.stop>
                  <GripVertical :size="12" />
                </span>
              </UiTooltip>
              <span class="chip-title">{{ item.title }}</span>
            </button>
          </UiTooltip>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.manager-toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: var(--sp-3);
}
.manager-hint {
  font-size: var(--text-xs);
  color: var(--text-tertiary);
}

.menu-matrix {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.matrix-row {
  display: grid;
  grid-template-columns: 56px 1fr;
  gap: 10px 12px;
  align-items: start;
}

.matrix-label {
  padding-top: 6px;
  font-size: 12px;
  font-weight: 600;
  color: var(--text-tertiary);
  letter-spacing: 0.02em;
  user-select: none;
}

.matrix-chips {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
  min-height: 28px;
}

.menu-chip {
  display: inline-flex;
  align-items: center;
  gap: 2px;
  height: 28px;
  padding: 0 10px 0 4px;
  border: 1px solid var(--border-color);
  border-radius: var(--radius-md);
  background: var(--bg-card);
  color: var(--text-primary);
  font-size: 12px;
  cursor: pointer;
  transition: all var(--transition);
}
.menu-chip:hover {
  border-color: var(--accent-border, var(--accent));
  background: var(--bg-hover);
}
.menu-chip--fixed {
  border-color: color-mix(in srgb, var(--accent) 35%, transparent);
  background: color-mix(in srgb, var(--accent) 8%, transparent);
  color: var(--accent);
  font-weight: 600;
  cursor: default;
}
.menu-chip--hidden {
  opacity: 0.45;
  color: var(--text-tertiary);
  background: var(--bg-hover);
}
.menu-chip--hidden .chip-title {
  text-decoration: line-through;
}
.menu-chip--ghost {
  opacity: 0.35;
  background: var(--accent-light);
}

.chip-handle {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 18px;
  height: 18px;
  color: var(--text-tertiary);
  cursor: grab;
  border-radius: var(--radius-sm);
  flex-shrink: 0;
}
.chip-handle:hover { color: var(--text-secondary); background: var(--bg-hover); }
.chip-handle:active { cursor: grabbing; }

.chip-title {
  line-height: 1;
  white-space: nowrap;
}

@media (max-width: 480px) {
  .matrix-row {
    grid-template-columns: 1fr;
    gap: 4px;
  }
  .matrix-label { padding-top: 0; }
}
</style>
