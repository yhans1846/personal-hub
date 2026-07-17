<script setup lang="ts">
import { ref } from 'vue'
import UiCard from '@/components/ui/UiCard.vue'
import NotificationSettings from './NotificationSettings.vue'
import DataManagement from './DataManagement.vue'
import ExperimentalFeatures from './ExperimentalFeatures.vue'

const activePanels = ref(['notification'])
</script>

<template>
  <el-collapse v-model="activePanels" class="advanced-settings">
    <el-collapse-item title="通知偏好" name="notification">
      <UiCard class="settings-card">
        <NotificationSettings />
      </UiCard>
    </el-collapse-item>
    <el-collapse-item title="缓存与数据" name="data">
      <UiCard class="settings-card">
        <DataManagement />
      </UiCard>
    </el-collapse-item>
    <el-collapse-item title="实验功能" name="experimental">
      <UiCard class="settings-card">
        <ExperimentalFeatures />
      </UiCard>
    </el-collapse-item>
  </el-collapse>
</template>

<style scoped>
.advanced-settings {
  width: 100%;
  border: none;
}
.advanced-settings :deep(.el-collapse-item__header) {
  font-size: 15px;
  font-weight: 700;
  color: var(--text-primary);
  background: transparent;
  border-bottom: 1px solid var(--border-color);
  height: auto;
  line-height: 1.4;
  padding: var(--sp-3) 0;
}
.advanced-settings :deep(.el-collapse-item__wrap) {
  border-bottom: none;
}
.advanced-settings :deep(.el-collapse-item__content) {
  padding: var(--sp-3) 0 var(--sp-2);
}
.settings-card {
  margin-bottom: 0;
}
</style>
