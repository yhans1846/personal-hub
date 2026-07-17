<script setup lang="ts">
import type { Component } from 'vue'
import { FileText, PenLine, BookMarked, BookOpen, CheckSquare, Target } from 'lucide-vue-next'
import DashCard from './DashCard.vue'

const emit = defineEmits<{
  create: [type: string]
}>()

/** 与侧栏「知识」分组一致：笔记 → 日记 → 阅读 → 学习记录 → 待办 → 学习计划 */
const actions: { type: string; label: string; icon: Component }[] = [
  { type: 'note', label: '新建笔记', icon: FileText },
  { type: 'diary', label: '写日记', icon: PenLine },
  { type: 'reading', label: '添加书籍', icon: BookMarked },
  { type: 'study', label: '开始学习', icon: BookOpen },
  { type: 'todo', label: '新建任务', icon: CheckSquare },
  { type: 'plan', label: '新建计划', icon: Target },
]
</script>

<template>
  <DashCard title="快捷操作" :icon="PenLine">
    <div class="quick-grid">
      <button
        v-for="a in actions"
        :key="a.type"
        class="quick-btn"
        type="button"
        @click="emit('create', a.type)"
      >
        <component :is="a.icon" :size="18" />
        <span>{{ a.label }}</span>
      </button>
    </div>
  </DashCard>
</template>

<style scoped>
.quick-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: var(--sp-2);
  padding: var(--sp-3);
}
.quick-btn {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: var(--sp-2);
  padding: var(--sp-3) var(--sp-2);
  border-radius: var(--radius-md);
  border: 1px solid var(--border-color);
  background: var(--bg-card);
  color: var(--text-secondary);
  cursor: pointer;
  font-size: var(--text-sm);
  font-family: var(--font-sans);
  transition: all var(--transition);
  min-height: 72px;
}
.quick-btn:hover {
  border-color: var(--accent);
  color: var(--accent);
  background: var(--accent-light);
}

@media (max-width: 720px) {
  .quick-grid {
    grid-template-columns: repeat(2, 1fr);
  }
}
</style>
