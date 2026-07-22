<script setup lang="ts">
import { ExternalLink } from 'lucide-vue-next'
import type { BookmarkVO } from '@/types/bookmark'
import DashCard from './DashCard.vue'
import UiTooltip from '@/components/UiTooltip.vue'
import './dash-list.css'

defineProps<{
  links: BookmarkVO[]
}>()

/** 默认图标：取名称首字（中文一字 / 英文大写） */
function nameIcon(title: string) {
  const t = (title || '').trim()
  if (!t) return '?'
  const ch = Array.from(t)[0]
  return /[a-z]/i.test(ch) ? ch.toUpperCase() : ch
}
</script>

<template>
  <DashCard title="外部快捷" :icon="ExternalLink" icon-class="text-accent" more-to="/bookmarks">
    <div v-if="links.length === 0" class="dash-empty">
      去收藏夹勾选「展示到首页」
      <router-link to="/bookmarks" class="empty-link">去配置 →</router-link>
    </div>
    <div v-else class="icon-grid">
      <UiTooltip v-for="item in links" :key="item.id" :content="item.title">
        <a
          class="icon-btn"
          :href="item.url"
          target="_blank"
          rel="noopener noreferrer"
        >
          <span class="name-icon">{{ nameIcon(item.title) }}</span>
          <span class="icon-label">{{ item.title }}</span>
        </a>
      </UiTooltip>
    </div>
  </DashCard>
</template>

<style scoped>
.empty-link {
  display: inline-block;
  margin-left: var(--sp-1);
  color: var(--accent);
  text-decoration: none;
}
.empty-link:hover { text-decoration: underline; }

.icon-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: var(--sp-2);
  padding: var(--sp-3);
}
.icon-btn {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: var(--sp-2);
  padding: var(--sp-3) var(--sp-1);
  border-radius: var(--radius-md);
  border: 1px solid transparent;
  text-decoration: none;
  color: var(--text-secondary);
  transition: all var(--transition);
  min-height: 72px;
  min-width: 0;
}
.icon-btn:hover {
  border-color: var(--accent);
  color: var(--accent);
  background: var(--accent-light);
}
.name-icon {
  width: 36px;
  height: 36px;
  border-radius: var(--radius-md);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: var(--text-base);
  font-weight: 600;
  color: var(--accent);
  background: var(--accent-light);
  flex-shrink: 0;
}
.icon-btn:hover .name-icon {
  background: var(--bg-card);
}
.icon-label {
  font-size: var(--text-xs);
  text-align: center;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  width: 100%;
  max-width: 100%;
}
@media (max-width: 640px) {
  .icon-grid { grid-template-columns: repeat(3, 1fr); }
}
</style>
