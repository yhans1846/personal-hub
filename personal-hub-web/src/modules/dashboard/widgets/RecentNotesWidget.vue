<script setup lang="ts">
import { useRouter } from 'vue-router'
import { FileText } from 'lucide-vue-next'
import type { NoteVO } from '@/types/note'
import { formatRelative } from '../format'
import DashCard from './DashCard.vue'
import './dash-list.css'

defineProps<{
  notes: NoteVO[]
}>()

const router = useRouter()
</script>

<template>
  <DashCard title="最近编辑" :icon="FileText" icon-class="text-accent" more-to="/notes" more-label="">
    <div v-if="notes.length === 0" class="dash-empty">暂无笔记</div>
    <div v-else class="dash-list">
      <div
        v-for="note in notes.slice(0, 5)"
        :key="note.id"
        class="dash-list-item"
        @click="router.push(`/notes/${note.id}/edit`)"
      >
        <FileText :size="14" class="item-icon" />
        <div class="dash-list-main">
          <span class="dash-list-title">{{ note.title || '无标题' }}</span>
          <span v-if="note.excerpt" class="dash-list-sub">{{ note.excerpt }}</span>
        </div>
        <span class="dash-list-meta">{{ formatRelative(note.updatedAt) }}</span>
      </div>
    </div>
  </DashCard>
</template>

<style scoped>
.item-icon { color: var(--accent); flex-shrink: 0; }
</style>
