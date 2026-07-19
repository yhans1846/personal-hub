<script setup lang="ts">
import { ref } from 'vue'

withDefaults(defineProps<{
  maxCols?: number
  maxRows?: number
}>(), {
  maxCols: 8,
  maxRows: 6,
})

const emit = defineEmits<{ select: [rows: number, cols: number] }>()

const hoverRows = ref(0)
const hoverCols = ref(0)

function onCellEnter(row: number, col: number) {
  hoverRows.value = row
  hoverCols.value = col
}

function onCellClick(row: number, col: number) {
  emit('select', row, col)
}

function onLeave() {
  hoverRows.value = 0
  hoverCols.value = 0
}
</script>

<template>
  <div class="table-grid-picker" @mouseleave="onLeave">
    <div class="grid-label">
      {{ hoverRows && hoverCols ? `${hoverCols} × ${hoverRows}` : '选择表格大小' }}
    </div>
    <div class="grid">
      <div
        v-for="row in maxRows"
        :key="row"
        class="grid-row"
      >
        <button
          v-for="col in maxCols"
          :key="col"
          type="button"
          class="grid-cell"
          :class="{ active: row <= hoverRows && col <= hoverCols }"
          @mouseenter="onCellEnter(row, col)"
          @click="onCellClick(row, col)"
        />
      </div>
    </div>
  </div>
</template>

<style scoped>
.table-grid-picker {
  padding: 8px 10px;
}
.grid-label {
  font-size: 11px;
  color: var(--text-tertiary);
  margin-bottom: 6px;
  min-height: 16px;
}
.grid {
  display: flex;
  flex-direction: column;
  gap: 2px;
}
.grid-row {
  display: flex;
  gap: 2px;
}
.grid-cell {
  width: 16px;
  height: 16px;
  padding: 0;
  border: 1px solid var(--border-color);
  border-radius: var(--radius-sm);
  background: var(--bg-body);
  cursor: pointer;
  transition: background var(--transition-duration), border-color var(--transition-duration);
}
.grid-cell.active {
  background: color-mix(in srgb, var(--accent) 35%, transparent);
  border-color: var(--accent);
}
.grid-cell:hover {
  border-color: var(--accent);
}
</style>
