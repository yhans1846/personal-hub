<script setup lang="ts">
import type { Component } from 'vue'

defineProps<{
  icon: Component
  text: string
  actionLabel?: string
  actionIcon?: Component
}>()

const emit = defineEmits<{
  action: []
}>()
</script>

<template>
  <div class="empty-state">
    <div class="empty-state__icon">
      <component :is="icon" :size="48" />
    </div>
    <p class="empty-state__text">{{ text }}</p>
    <el-button v-if="actionLabel" type="primary" @click="emit('action')">
      <component :is="actionIcon" v-if="actionIcon" :size="14" style="margin-right: 4px" />
      {{ actionLabel }}
    </el-button>
  </div>
</template>
