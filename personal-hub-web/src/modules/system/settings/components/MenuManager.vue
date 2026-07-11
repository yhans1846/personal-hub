<script setup lang="ts">
import { ref, onMounted, nextTick, onUnmounted } from 'vue'
import { useLayoutStore } from '@/store/layoutStore'
import { ElMessage } from 'element-plus'
import { GripVertical, Eye, EyeOff } from 'lucide-vue-next'
import Sortable from 'sortablejs'
import type { MenuItem } from '@/types/layout'

const layoutStore = useLayoutStore()
const listRef = ref<HTMLElement | null>(null)
let sortable: Sortable | null = null

onMounted(async () => {
  await nextTick()
  if (listRef.value) {
    sortable = Sortable.create(listRef.value, {
      animation: 200,
      handle: '.drag-handle',
      ghostClass: 'menu-item--ghost',
      onEnd: (evt) => {
        const items = layoutStore.menuItems as any[]
        const item = items.splice(evt.oldIndex!, 1)[0]
        items.splice(evt.newIndex!, 0, item)
        items.forEach((it: any, idx: number) => { it.order = idx + 1 })
        persist()
      }
    })
  }
})

onUnmounted(() => { sortable?.destroy() })

function toggleVisibility(item: MenuItem) {
  if (item.fixed) return
  item.visible = !item.visible
  persist()
}

async function persist() {
  await layoutStore.saveMenuConfig([...layoutStore.menuItems])
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
    <div ref="listRef" class="menu-list">
      <div
        v-for="item in layoutStore.menuItems"
        :key="item.code"
        class="menu-item"
        :class="{ 'menu-item--hidden': !item.visible, 'menu-item--fixed': item.fixed }"
      >
        <span class="drag-handle"><GripVertical :size="16" /></span>
        <span class="menu-item-title">{{ item.title }}</span>
        <el-tag v-if="item.section === 'manage'" size="small" type="info">管理</el-tag>
        <el-tag v-else-if="item.section === 'stats'" size="small" type="info">统计</el-tag>
        <button
          class="vis-toggle"
          :class="{ 'is-hidden': !item.visible }"
          :disabled="item.fixed"
          :title="item.fixed ? '首页不可隐藏' : item.visible ? '点击隐藏' : '点击显示'"
          @click="toggleVisibility(item)"
        >
          <Eye v-if="item.visible" :size="16" />
          <EyeOff v-else :size="16" />
        </button>
      </div>
    </div>
  </div>
</template>

<style scoped>
.manager-toolbar { display: flex; justify-content: space-between; align-items: center; margin-bottom: var(--sp-3); }
.manager-hint { font-size: var(--text-xs); color: var(--text-tertiary); }
.menu-list { display: flex; flex-direction: column; gap: var(--sp-1); }
.menu-item {
  display: flex; align-items: center; gap: var(--sp-3);
  padding: var(--sp-2) var(--sp-3);
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
</style>
