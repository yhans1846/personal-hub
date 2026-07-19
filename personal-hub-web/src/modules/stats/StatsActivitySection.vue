<script setup lang="ts">
import { computed } from 'vue'
import type { StatsVO } from '@/modules/dashboard/api'
import { Clock, Lightbulb } from 'lucide-vue-next'
import { actionLabel, groupActivities, moduleLabel } from './statsFormat'

const props = defineProps<{
  stats: StatsVO
  hasCard: (code: string) => boolean
}>()

const groupedActivities = computed(() => groupActivities(props.stats.recentActivity))
const singleColumn = computed(
  () => !props.hasCard('timeline') !== !props.hasCard('insight'),
)
</script>

<template>
  <div class="bottom-grid" :class="{ 'bottom-grid--single': singleColumn }">
    <div v-show="hasCard('timeline')" class="bottom-card">
      <div class="chart-header">
        <Clock :size="16" stroke="var(--text-secondary)" />
        <span>最近活动</span>
      </div>
      <div v-if="groupedActivities.length > 0" class="timeline">
        <template v-for="group in groupedActivities" :key="group.label">
          <div class="timeline-date">{{ group.label }}</div>
          <div class="timeline-items">
            <div v-for="item in group.items" :key="item.id" class="timeline-item">
              <div class="tl-dot" :class="`tl-dot--${item.action.toLowerCase()}`" />
              <div class="tl-body">
                <span class="tl-module">{{ moduleLabel(item.module) }}</span>
                <span class="tl-page-action">{{ actionLabel(item.action) }}</span>
                <span class="tl-content">{{ item.content }}</span>
              </div>
              <span class="tl-time">{{ item.timeLabel }}</span>
            </div>
          </div>
        </template>
      </div>
      <div v-else class="empty-box">暂无活动记录</div>
    </div>

    <div v-show="hasCard('insight')" class="bottom-card">
      <div class="chart-header">
        <Lightbulb :size="16" stroke="var(--warning)" />
        <span>学习洞察</span>
      </div>
      <div v-if="stats.insights.length > 0" class="insights">
        <div
          v-for="(insight, i) in stats.insights"
          :key="i"
          class="insight-item"
          :style="{ '--delay': `${i * 0.1}s` }"
        >
          <span class="insight-icon">{{ insight.icon }}</span>
          <div class="insight-body">
            <span class="insight-title">{{ insight.title }}</span>
            <span class="insight-desc">{{ insight.description }}</span>
          </div>
        </div>
      </div>
      <div v-else class="empty-box">数据不足，继续使用后将生成洞察</div>
    </div>
  </div>
</template>

<style scoped>
.bottom-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: var(--sp-4);
  align-items: stretch;
}
.bottom-grid--single { grid-template-columns: 1fr; }
.bottom-grid--single .bottom-card { max-width: 600px; }
.bottom-card {
  background: var(--bg-card);
  border: 1px solid var(--border-color);
  border-radius: var(--radius-xl);
  padding: var(--sp-4) var(--sp-5);
  display: flex;
  flex-direction: column;
  height: 450px;
}
.chart-header {
  display: flex;
  align-items: center;
  gap: var(--sp-2);
  font-size: var(--text-sm);
  font-weight: 600;
  color: var(--text-primary);
  margin-bottom: var(--sp-2);
  flex-shrink: 0;
}
.bottom-card > .timeline,
.bottom-card > .insights {
  flex: 1;
  overflow-y: auto;
  min-height: 0;
  scrollbar-width: none;
}
.bottom-card > .timeline::-webkit-scrollbar,
.bottom-card > .insights::-webkit-scrollbar { display: none; }
.timeline-date {
  font-size: var(--text-xs);
  font-weight: 600;
  color: var(--text-secondary);
  padding: var(--sp-2) 0 var(--sp-1);
  margin-top: var(--sp-1);
  text-transform: uppercase;
  letter-spacing: 0.3px;
}
.timeline-date:first-of-type { margin-top: 0; }
.timeline-items { padding-left: var(--sp-1); }
.timeline-item {
  display: flex;
  align-items: flex-start;
  gap: var(--sp-3);
  padding: var(--sp-2) 0;
  position: relative;
}
.timeline-item::before {
  content: '';
  position: absolute;
  left: 5px;
  top: 24px;
  bottom: -4px;
  width: 1px;
  background: var(--border-color);
}
.timeline-item:last-child::before { display: none; }
.tl-dot {
  width: 10px;
  height: 10px;
  border-radius: 50%;
  background: var(--border-color);
  flex-shrink: 0;
  margin-top: 4px;
  position: relative;
  z-index: 1;
}
.tl-dot--create { background: var(--success); }
.tl-dot--update { background: var(--accent); }
.tl-dot--delete { background: var(--danger); }
.tl-dot--restore { background: var(--warning); }
.tl-body {
  flex: 1;
  min-width: 0;
  font-size: var(--text-sm);
  color: var(--text-primary);
  line-height: 1.4;
}
.tl-module { font-weight: 500; margin-right: var(--sp-1); }
.tl-page-action { color: var(--text-secondary); margin-right: var(--sp-1); }
.tl-content { color: var(--text-tertiary); word-break: break-all; }
.tl-time {
  font-size: var(--text-xs);
  color: var(--text-tertiary);
  flex-shrink: 0;
  margin-top: 2px;
}
.insights {
  display: flex;
  flex-direction: column;
  gap: var(--sp-2);
  padding-top: var(--sp-1);
}
.insight-item {
  display: flex;
  align-items: flex-start;
  gap: var(--sp-3);
  padding: var(--sp-2) var(--sp-3);
  border-radius: var(--radius-md);
  background: var(--bg-hover);
  animation: slideUp 0.3s ease var(--delay, 0s) both;
}
.insight-icon { font-size: 18px; flex-shrink: 0; margin-top: 1px; }
.insight-body { display: flex; flex-direction: column; gap: 1px; }
.insight-title {
  font-size: var(--text-sm);
  font-weight: 600;
  color: var(--text-primary);
}
.insight-desc {
  font-size: var(--text-xs);
  color: var(--text-tertiary);
}
.empty-box {
  padding: var(--sp-8) 0;
  text-align: center;
  font-size: var(--text-sm);
  color: var(--text-tertiary);
}
@keyframes slideUp {
  from { opacity: 0; transform: translateY(8px); }
  to { opacity: 1; transform: translateY(0); }
}
@media (max-width: 1024px) {
  .bottom-grid { grid-template-columns: 1fr; }
}
</style>
