<script setup lang="ts">
import UiCard from '@/components/ui/UiCard.vue'
import NotificationSettings from './NotificationSettings.vue'
import ExperimentalFeatures from './ExperimentalFeatures.vue'
</script>

<template>
  <!-- 多根节点，由父级 .settings-stack 统一卡片间距 -->
  <UiCard class="settings-card">
    <h3 class="card-title">通知偏好</h3>
    <NotificationSettings />
  </UiCard>
  <UiCard class="settings-card">
    <h3 class="card-title">实验功能</h3>
    <ExperimentalFeatures />
  </UiCard>
</template>
