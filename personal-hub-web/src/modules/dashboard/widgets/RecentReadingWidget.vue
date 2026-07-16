<script setup lang="ts">
import { useRouter } from 'vue-router'
import { BookMarked } from 'lucide-vue-next'
import type { ReadingVO } from '@/types/reading'
import { buildEditPath } from '@/utils/deepLink'
import DashCard from './DashCard.vue'
import './dash-list.css'

defineProps<{
  readings: ReadingVO[]
}>()

const router = useRouter()
</script>

<template>
  <DashCard title="最近阅读" :icon="BookMarked" icon-class="text-accent" more-to="/readings">
    <div v-if="readings.length === 0" class="dash-empty">还没有阅读记录</div>
    <div v-else class="dash-list">
      <div
        v-for="r in readings.slice(0, 5)"
        :key="r.id"
        class="dash-list-item"
        @click="router.push(buildEditPath('/readings', r.id))"
      >
        <BookMarked :size="14" class="item-icon" />
        <div class="dash-list-main">
          <span class="dash-list-title">{{ r.bookTitle }}</span>
          <div class="dash-progress">
            <div class="dash-progress-bar" :style="{ width: `${r.progress || 0}%` }" />
          </div>
          <span class="dash-list-sub">
            <template v-if="r.author">{{ r.author }} · </template>{{ r.progress || 0 }}%
          </span>
        </div>
      </div>
    </div>
  </DashCard>
</template>

<style scoped>
.item-icon { color: var(--accent); flex-shrink: 0; }
</style>
