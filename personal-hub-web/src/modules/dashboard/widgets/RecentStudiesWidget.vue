<script setup lang="ts">
import { useRouter } from 'vue-router'
import { BookOpen } from 'lucide-vue-next'
import type { StudyRecordVO } from '@/types/study'
import { buildEditPath } from '@/utils/deepLink'
import { formatDuration, formatRelative } from '../format'
import DashCard from './DashCard.vue'
import './dash-list.css'

defineProps<{
  studies: StudyRecordVO[]
}>()

const router = useRouter()
</script>

<template>
  <DashCard title="最近学习" :icon="BookOpen" icon-class="text-info" more-to="/study-records" more-label="">
    <div v-if="studies.length === 0" class="dash-empty">暂无学习记录</div>
    <div v-else class="dash-list">
      <div
        v-for="s in studies.slice(0, 5)"
        :key="s.id"
        class="dash-list-item"
        @click="router.push(buildEditPath('/study-records', s.id))"
      >
        <BookOpen :size="14" class="item-icon" />
        <div class="dash-list-main">
          <span class="dash-list-title">{{ s.subject }}</span>
          <span class="dash-list-sub">{{ formatDuration(s.duration) }} · {{ s.date || formatRelative(s.updatedAt) }}</span>
        </div>
      </div>
    </div>
  </DashCard>
</template>

<style scoped>
.item-icon { color: var(--info); flex-shrink: 0; }
</style>
