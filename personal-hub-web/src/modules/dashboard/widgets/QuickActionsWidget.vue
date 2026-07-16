<script setup lang="ts">
import { FileText, CheckCircle, BookOpen, PenLine } from 'lucide-vue-next'
import DashCard from './DashCard.vue'

const emit = defineEmits<{
  create: [type: string]
}>()
</script>

<template>
  <DashCard title="快捷操作" :icon="PenLine">
    <div class="quick-grid">
      <button class="quick-btn" @click="emit('create', 'note')">
        <FileText :size="18" />
        <span>新建笔记</span>
      </button>
      <button class="quick-btn" @click="emit('create', 'todo')">
        <CheckCircle :size="18" />
        <span>新建任务</span>
      </button>
      <button class="quick-btn" @click="emit('create', 'study')">
        <BookOpen :size="18" />
        <span>开始学习</span>
      </button>
      <button class="quick-btn" @click="emit('create', 'diary')">
        <PenLine :size="18" />
        <span>写日记</span>
      </button>
    </div>
  </DashCard>
</template>

<style scoped>
.quick-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: var(--sp-2);
  padding: var(--sp-3);
}
.quick-btn {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: var(--sp-2);
  padding: var(--sp-4) var(--sp-3);
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
</style>
