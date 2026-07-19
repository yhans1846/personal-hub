<script setup lang="ts">
import { ref, onMounted, nextTick, onUnmounted } from 'vue'
import { useLayoutStore } from '@/store/layoutStore'
import { ElMessage } from 'element-plus'
import { GripVertical } from 'lucide-vue-next'
import Sortable from 'sortablejs'
import type { CardItem } from '@/types/layout'

const layoutStore = useLayoutStore()
const listRef = ref<HTMLElement | null>(null)
let sortable: Sortable | null = null

onMounted(async () => {
  await nextTick()
  if (listRef.value) {
    sortable = Sortable.create(listRef.value, {
      animation: 180,
      handle: '.chip-handle',
      ghostClass: 'card-chip--ghost',
      onEnd: (evt) => {
        const items = layoutStore.dashboardCards as CardItem[]
        const from = evt.oldIndex ?? 0
        const to = evt.newIndex ?? 0
        const [item] = items.splice(from, 1)
        if (!item) return
        items.splice(to, 0, item)
        items.forEach((it, idx) => { it.order = idx + 1 })
        persist()
      },
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
  <div class="card-manager">
    <div class="manager-toolbar">
      <span class="manager-hint">点击标签显隐 · 拖把手排序</span>
      <el-button size="small" text @click="handleReset">恢复默认</el-button>
    </div>
    <div ref="listRef" class="chip-row">
      <button
        v-for="card in layoutStore.dashboardCards"
        :key="card.code"
        type="button"
        class="card-chip"
        :class="{ 'card-chip--hidden': !card.visible }"
        :title="card.visible ? '点击隐藏' : '点击显示'"
        @click="toggleVisibility(card)"
      >
        <span class="chip-handle" title="拖拽排序" @click.stop>
          <GripVertical :size="12" />
        </span>
        <span class="chip-title">{{ card.title }}</span>
      </button>
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
.manager-hint { font-size: var(--text-xs); color: var(--text-tertiary); }
.chip-row {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
  min-height: 28px;
}
.card-chip {
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
.card-chip:hover {
  border-color: var(--accent-border, var(--accent));
  background: var(--bg-hover);
}
.card-chip--hidden {
  opacity: 0.45;
  color: var(--text-tertiary);
  background: var(--bg-hover);
}
.card-chip--hidden .chip-title { text-decoration: line-through; }
.card-chip--ghost { opacity: 0.35; background: var(--accent-light); }
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
.chip-title { line-height: 1; white-space: nowrap; }
</style>
