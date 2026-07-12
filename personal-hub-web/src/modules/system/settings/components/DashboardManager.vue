<script setup lang="ts">
import { ref, onMounted, nextTick, onUnmounted } from 'vue'
import { useLayoutStore } from '@/store/layoutStore'
import { ElMessage } from 'element-plus'
import { GripVertical, Eye, EyeOff } from 'lucide-vue-next'
import Sortable from 'sortablejs'
import type { CardItem } from '@/types/layout'

const layoutStore = useLayoutStore()
const listRef = ref<HTMLElement | null>(null)
let sortable: Sortable | null = null

onMounted(async () => {
  await nextTick()
  if (listRef.value) {
    sortable = Sortable.create(listRef.value, {
      animation: 200,
      handle: '.drag-handle',
      ghostClass: 'card-item--ghost',
      onEnd: (evt) => {
        const items = layoutStore.dashboardCards as any[]
        const item = items.splice(evt.oldIndex!, 1)[0]
        items.splice(evt.newIndex!, 0, item)
        items.forEach((it: any, idx: number) => { it.order = idx + 1 })
        persist()
      }
    })
  }
})

onUnmounted(() => { sortable?.destroy() })

function toggleVisibility(item: CardItem) {
  item.visible = !item.visible
  persist()
}

async function persist() {
  await layoutStore.saveDashboardConfig([...layoutStore.dashboardCards])
}

async function handleReset() {
  await layoutStore.resetDashboardConfig()
  ElMessage.success('Dashboard 布局已恢复默认')
}
</script>

<template>
  <div class="dashboard-manager">
    <div class="manager-toolbar">
      <span class="manager-hint">拖拽排序，点击切换显示/隐藏</span>
      <el-button size="small" text @click="handleReset">恢复默认</el-button>
    </div>
    <div ref="listRef" class="card-list">
      <div
        v-for="card in layoutStore.dashboardCards"
        :key="card.code"
        class="card-item"
        :class="{ 'card-item--hidden': !card.visible }"
      >
        <span class="drag-handle"><GripVertical :size="16" /></span>
        <span class="card-item-title">{{ card.title }}</span>
        <button
          class="vis-toggle"
          :class="{ 'is-hidden': !card.visible }"
          :title="card.visible ? '点击隐藏' : '点击显示'"
          @click="toggleVisibility(card)"
        >
          <Eye v-if="card.visible" :size="16" />
          <EyeOff v-else :size="16" />
        </button>
      </div>
    </div>
  </div>
</template>

<style scoped>
.manager-toolbar { display: flex; justify-content: space-between; align-items: center; margin-bottom: var(--sp-4); }
.manager-hint { font-size: var(--text-xs); color: var(--text-tertiary); }
.card-list { display: flex; flex-direction: column; gap: var(--sp-1); }
.card-item {
  display: flex; align-items: center; gap: var(--sp-3);
  padding: 6px var(--sp-3);
  border-radius: var(--radius-sm);
  transition: background var(--transition);
  cursor: default;
}
.card-item:hover { background: var(--bg-hover); }
.card-item--hidden { opacity: 0.5; }
.card-item-title { flex: 1; font-size: var(--text-sm); }
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
.card-item--ghost { opacity: 0.3; background: var(--accent-light); }
</style>
