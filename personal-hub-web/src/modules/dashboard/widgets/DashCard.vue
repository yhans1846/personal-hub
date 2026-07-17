<script setup lang="ts">
import type { Component } from 'vue'
import { ArrowRight } from 'lucide-vue-next'

defineProps<{
  title: string
  icon?: Component
  iconClass?: string
  moreTo?: string
  moreLabel?: string
}>()
</script>

<template>
  <div class="dash-card">
    <div class="dash-card-header">
      <div class="dash-card-header-left">
        <component :is="icon" v-if="icon" :size="16" :class="['dash-card-icon', iconClass]" />
        <h3>{{ title }}</h3>
      </div>
      <router-link v-if="moreTo" :to="moreTo" class="dash-card-more">
        <template v-if="moreLabel !== ''">{{ moreLabel ?? '查看全部' }} </template>
        <ArrowRight :size="12" />
      </router-link>
      <slot name="header-extra" />
    </div>
    <div class="dash-card-body">
      <slot />
    </div>
  </div>
</template>

<style scoped>
.dash-card {
  background: var(--bg-card);
  border: 1px solid var(--border-color);
  border-radius: var(--radius-lg);
  overflow: hidden;
  height: 100%;
  display: flex;
  flex-direction: column;
  min-height: 0;
}
.dash-card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: var(--sp-3) var(--sp-4);
  border-bottom: 1px solid var(--border-light);
  flex-shrink: 0;
}
.dash-card-header-left {
  display: flex;
  align-items: center;
  gap: var(--sp-2);
}
.dash-card-header h3 {
  font-size: var(--text-sm);
  font-weight: 600;
  margin: 0;
}
.dash-card-icon { flex-shrink: 0; }
.dash-card-icon.text-accent { color: var(--accent); }
.dash-card-icon.text-info { color: var(--info); }
.dash-card-icon.text-warning { color: var(--warning); }
.dash-card-more {
  display: flex;
  align-items: center;
  gap: 2px;
  font-size: var(--text-xs);
  color: var(--text-tertiary);
  text-decoration: none;
}
.dash-card-more:hover { color: var(--accent); }
.dash-card-body {
  flex: 1;
  min-height: 0;
  overflow-y: auto;
  display: flex;
  flex-direction: column;
}
</style>
