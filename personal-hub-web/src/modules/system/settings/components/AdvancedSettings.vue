<script setup lang="ts">
import UiCard from '@/components/ui/UiCard.vue'
import NotificationSettings from './NotificationSettings.vue'
import DataManagement from './DataManagement.vue'
import ExperimentalFeatures from './ExperimentalFeatures.vue'
</script>

<template>
  <div class="advanced-settings">
    <UiCard class="settings-card">
      <h3 class="card-title">缓存与数据</h3>
      <DataManagement />
    </UiCard>
    <UiCard class="settings-card">
      <h3 class="card-title">通知偏好</h3>
      <NotificationSettings />
    </UiCard>
    <UiCard class="settings-card">
      <h3 class="card-title">实验功能</h3>
      <ExperimentalFeatures />
    </UiCard>
  </div>
</template>

<style scoped>
.advanced-settings {
  width: 100%;
}
.settings-card {
  margin-bottom: var(--sp-3);
}
.card-title {
  margin: 0 0 var(--sp-2);
  font-size: 15px;
  font-weight: 700;
  color: var(--text-primary);
}
</style>
