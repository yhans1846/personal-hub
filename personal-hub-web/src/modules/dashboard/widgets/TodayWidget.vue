<script setup lang="ts">
import { CheckCircle, Target } from 'lucide-vue-next'
import type { TodoVO } from '@/types/todo'
import type { StudyPlanVO } from '@/types/studyplan'
import { buildEditPath } from '@/utils/deepLink'
import { useRouter } from 'vue-router'
import DashCard from './DashCard.vue'
import './dash-list.css'

defineProps<{
  todayTodos: TodoVO[]
  todayPlans: StudyPlanVO[]
}>()

const emit = defineEmits<{
  toggleDone: [id: number]
}>()

const router = useRouter()
</script>

<template>
  <DashCard title="今日任务" :icon="CheckCircle" icon-class="text-accent" more-to="/todos">
    <div v-if="todayTodos.length === 0 && todayPlans.length === 0" class="dash-empty">
      暂无今日计划，去添加一个吧
    </div>
    <div v-else class="dash-list">
      <div
        v-for="todo in todayTodos"
        :key="'t' + todo.id"
        class="dash-list-item"
        @click="emit('toggleDone', todo.id)"
      >
        <div class="dash-todo-check" :class="{ checked: todo.isDone === 1 }">
          <CheckCircle v-if="todo.isDone === 1" :size="12" stroke="#fff" />
        </div>
        <div class="dash-list-main">
          <span class="dash-list-title">{{ todo.title }}</span>
        </div>
        <el-tag v-if="todo.priority === 1" type="danger" size="small">高</el-tag>
      </div>
      <div
        v-for="plan in todayPlans.slice(0, 4)"
        :key="plan.id"
        class="dash-list-item"
        @click="router.push(buildEditPath('/study-plans', plan.id))"
      >
        <Target :size="16" class="plan-icon" />
        <div class="dash-list-main">
          <span class="dash-list-title">{{ plan.name }}</span>
          <div class="dash-progress">
            <div class="dash-progress-bar" :style="{ width: `${plan.progress || 0}%` }" />
          </div>
        </div>
        <span class="dash-list-meta">{{ plan.progress }}%</span>
      </div>
    </div>
  </DashCard>
</template>

<style scoped>
.plan-icon { color: var(--info); flex-shrink: 0; }
</style>
