<script setup lang="ts">
import type { Component } from 'vue'
import { computed } from 'vue'

const props = defineProps<{
  icon: Component
  text: string
  actionLabel?: string
  actionIcon?: Component
  illustration?: string
}>()

const emit = defineEmits<{
  action: []
}>()

const illustrations: Record<string, string> = {
  note: `<svg width="120" height="120" viewBox="0 0 120 120" fill="none">
    <rect x="30" y="18" width="56" height="72" rx="6" stroke="var(--text-tertiary)" stroke-width="1.8" fill="var(--bg-card)" stroke-linejoin="round" opacity="0.6"/>
    <path d="M86 32H32" stroke="var(--text-tertiary)" stroke-width="1.8" stroke-linecap="round" opacity="0.35"/>
    <path d="M86 44H48" stroke="var(--text-tertiary)" stroke-width="1.8" stroke-linecap="round" opacity="0.35"/>
    <path d="M86 56H52" stroke="var(--text-tertiary)" stroke-width="1.8" stroke-linecap="round" opacity="0.35"/>
    <path d="M86 68H56" stroke="var(--text-tertiary)" stroke-width="1.8" stroke-linecap="round" opacity="0.35"/>
    <path d="M72 18L80 10L90 18L82 26L72 18Z" fill="var(--accent)" opacity="0.25" stroke="var(--accent)" stroke-width="1.2" stroke-linejoin="round"/>
    <path d="M80 10L82 8" stroke="var(--accent)" stroke-width="1.8" stroke-linecap="round"/>
  </svg>`,

  todo: `<svg width="120" height="120" viewBox="0 0 120 120" fill="none">
    <circle cx="52" cy="40" r="22" stroke="var(--text-tertiary)" stroke-width="1.8" opacity="0.5"/>
    <path d="M42 40L49 47L62 33" stroke="var(--success)" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round" opacity="0.7"/>
    <path d="M40 72L80 72" stroke="var(--text-tertiary)" stroke-width="1.8" stroke-linecap="round" opacity="0.3"/>
    <path d="M40 84L70 84" stroke="var(--text-tertiary)" stroke-width="1.8" stroke-linecap="round" opacity="0.3"/>
    <path d="M40 96L76 96" stroke="var(--text-tertiary)" stroke-width="1.8" stroke-linecap="round" opacity="0.3"/>
    <path d="M88 38L96 30C98 28 100 30 98 32L90 40L86 42L88 38Z" fill="var(--warning)" opacity="0.3" stroke="var(--warning)" stroke-width="1.2" stroke-linejoin="round"/>
    <path d="M96 30L98 28" stroke="var(--warning)" stroke-width="1.8" stroke-linecap="round" opacity="0.6"/>
  </svg>`,

  diary: `<svg width="120" height="120" viewBox="0 0 120 120" fill="none">
    <rect x="30" y="14" width="52" height="74" rx="6" stroke="var(--text-tertiary)" stroke-width="1.8" fill="var(--bg-card)" stroke-linejoin="round" opacity="0.55"/>
    <path d="M38 14V88" stroke="var(--text-tertiary)" stroke-width="1.8" opacity="0.4"/>
    <rect x="50" y="30" width="18" height="18" rx="4" stroke="var(--text-tertiary)" stroke-width="1.5" opacity="0.3" fill="var(--bg-hover)"/>
    <circle cx="53" cy="36" r="2" fill="var(--accent)" opacity="0.5"/>
    <circle cx="59" cy="36" r="2" fill="var(--accent)" opacity="0.5"/>
    <circle cx="65" cy="36" r="2" fill="var(--accent)" opacity="0.5"/>
    <path d="M50 42C50 42 53 46 59 46C65 46 68 42 68 42" stroke="var(--accent)" stroke-width="1.2" stroke-linecap="round" opacity="0.5"/>
    <path d="M50 54H68" stroke="var(--text-tertiary)" stroke-width="1.5" stroke-linecap="round" opacity="0.3"/>
    <path d="M50 62H64" stroke="var(--text-tertiary)" stroke-width="1.5" stroke-linecap="round" opacity="0.3"/>
    <path d="M50 70H60" stroke="var(--text-tertiary)" stroke-width="1.5" stroke-linecap="round" opacity="0.3"/>
    <rect x="82" y="22" width="18" height="50" rx="4" fill="var(--accent)" opacity="0.15" stroke="var(--accent)" stroke-width="1.2" stroke-linejoin="round"/>
    <path d="M91 22V72" stroke="var(--accent)" stroke-width="1.5" opacity="0.4"/>
  </svg>`,

  bookmark: `<svg width="120" height="120" viewBox="0 0 120 120" fill="none">
    <path d="M60 16L70 38L94 40L76 56L80 80L60 68L40 80L44 56L26 40L50 38L60 16Z" fill="var(--warning)" opacity="0.15" stroke="var(--warning)" stroke-width="1.8" stroke-linejoin="round" opacity="0.5"/>
    <path d="M60 24L67 40L84 42L71 54L74 70L60 61L46 70L49 54L36 42L53 40L60 24Z" fill="var(--warning)" opacity="0.25" stroke="var(--warning)" stroke-width="1.2" stroke-linejoin="round"/>
  </svg>`,

  study: `<svg width="120" height="120" viewBox="0 0 120 120" fill="none">
    <circle cx="60" cy="56" r="24" stroke="var(--text-tertiary)" stroke-width="1.8" opacity="0.45"/>
    <circle cx="60" cy="56" r="3" fill="var(--info)" opacity="0.5"/>
    <path d="M60 38V56L72 62" stroke="var(--info)" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" opacity="0.7"/>
    <path d="M28 90C28 90 36 78 60 78C84 78 92 90 92 90" stroke="var(--text-tertiary)" stroke-width="1.8" stroke-linecap="round" opacity="0.3"/>
    <rect x="48" y="20" width="24" height="6" rx="3" fill="var(--info)" opacity="0.2"/>
  </svg>`,

  reading: `<svg width="120" height="120" viewBox="0 0 120 120" fill="none">
    <rect x="26" y="36" width="38" height="52" rx="4" fill="var(--bg-card)" stroke="var(--text-tertiary)" stroke-width="1.8" opacity="0.5" stroke-linejoin="round"/>
    <rect x="32" y="42" width="26" height="6" rx="2" fill="var(--text-tertiary)" opacity="0.15"/>
    <rect x="32" y="54" width="20" height="4" rx="2" fill="var(--text-tertiary)" opacity="0.15"/>
    <rect x="32" y="64" width="24" height="4" rx="2" fill="var(--text-tertiary)" opacity="0.15"/>
    <path d="M52 30V76L44 68L36 76V30" stroke="var(--accent)" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round" opacity="0.5"/>
    <rect x="58" y="42" width="34" height="46" rx="4" fill="var(--bg-card)" stroke="var(--text-tertiary)" stroke-width="1.8" opacity="0.4" stroke-linejoin="round"/>
    <rect x="64" y="48" width="22" height="5" rx="2" fill="var(--text-tertiary)" opacity="0.12"/>
    <rect x="64" y="58" width="16" height="3" rx="2" fill="var(--text-tertiary)" opacity="0.12"/>
  </svg>`,

  file: `<svg width="120" height="120" viewBox="0 0 120 120" fill="none">
    <path d="M50 18H70L90 38V98C90 100 88 102 86 102H34C32 102 30 100 30 98V22C30 20 32 18 34 18H50Z" fill="var(--bg-card)" stroke="var(--text-tertiary)" stroke-width="1.8" stroke-linejoin="round" opacity="0.55"/>
    <path d="M50 18V30C50 34 52 38 56 38H90" fill="var(--bg-hover)" stroke="var(--text-tertiary)" stroke-width="1.5" stroke-linejoin="round" opacity="0.35"/>
    <rect x="42" y="54" width="30" height="3" rx="1.5" fill="var(--text-tertiary)" opacity="0.2"/>
    <rect x="42" y="64" width="36" height="3" rx="1.5" fill="var(--text-tertiary)" opacity="0.2"/>
    <rect x="42" y="74" width="24" height="3" rx="1.5" fill="var(--text-tertiary)" opacity="0.2"/>
    <rect x="70" y="78" width="14" height="14" rx="4" fill="var(--accent)" opacity="0.15" stroke="var(--accent)" stroke-width="1.2"/>
    <path d="M77 82V88M74 85H80" stroke="var(--accent)" stroke-width="1.5" stroke-linecap="round" opacity="0.6"/>
  </svg>`,

  plan: `<svg width="120" height="120" viewBox="0 0 120 120" fill="none">
    <circle cx="60" cy="56" r="30" stroke="var(--text-tertiary)" stroke-width="1.8" opacity="0.35"/>
    <circle cx="60" cy="56" r="22" stroke="var(--text-tertiary)" stroke-width="1.5" opacity="0.25"/>
    <circle cx="60" cy="56" r="14" stroke="var(--accent)" stroke-width="1.8" opacity="0.5"/>
    <circle cx="60" cy="56" r="5" fill="var(--accent)" opacity="0.4"/>
    <path d="M60 14V26" stroke="var(--text-tertiary)" stroke-width="1.5" stroke-linecap="round" opacity="0.3"/>
    <path d="M60 86V98" stroke="var(--text-tertiary)" stroke-width="1.5" stroke-linecap="round" opacity="0.3"/>
    <path d="M22 56H34" stroke="var(--text-tertiary)" stroke-width="1.5" stroke-linecap="round" opacity="0.3"/>
    <path d="M86 56H98" stroke="var(--text-tertiary)" stroke-width="1.5" stroke-linecap="round" opacity="0.3"/>
    <path d="M34 34L42 42" stroke="var(--text-tertiary)" stroke-width="1.5" stroke-linecap="round" opacity="0.25"/>
    <path d="M78 78L86 86" stroke="var(--text-tertiary)" stroke-width="1.5" stroke-linecap="round" opacity="0.25"/>
  </svg>`,

  default: `<svg width="120" height="120" viewBox="0 0 120 120" fill="none">
    <circle cx="60" cy="50" r="26" stroke="var(--text-tertiary)" stroke-width="1.8" opacity="0.3"/>
    <circle cx="52" cy="44" r="3" fill="var(--text-tertiary)" opacity="0.2"/>
    <circle cx="68" cy="44" r="3" fill="var(--text-tertiary)" opacity="0.2"/>
    <path d="M50 56C50 56 54 62 60 62C66 62 70 56 70 56" stroke="var(--text-tertiary)" stroke-width="1.8" stroke-linecap="round" opacity="0.3"/>
    <path d="M30 88C30 88 38 76 60 76C82 76 90 88 90 88" stroke="var(--text-tertiary)" stroke-width="1.5" stroke-linecap="round" opacity="0.25"/>
    <circle cx="60" cy="96" r="14" stroke="var(--accent)" stroke-width="1.5" opacity="0.2"/>
    <path d="M56 96L59 99L64 93" stroke="var(--accent)" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round" opacity="0.4"/>
  </svg>`
}

const currentIllustration = computed(() => {
  return props.illustration && illustrations[props.illustration]
    ? illustrations[props.illustration]
    : null
})
</script>

<template>
  <div class="empty-state">
    <div v-if="currentIllustration" class="empty-state__illustration" v-html="currentIllustration" />
    <div v-else class="empty-state__icon">
      <component :is="icon" :size="48" />
    </div>
    <p class="empty-state__text">{{ text }}</p>
    <el-button v-if="actionLabel" type="primary" @click="emit('action')">
      <component :is="actionIcon" v-if="actionIcon" :size="14" style="margin-right: 4px" />
      {{ actionLabel }}
    </el-button>
  </div>
</template>

<style scoped>
.empty-state__illustration {
  margin-bottom: var(--sp-4);
  display: flex;
  justify-content: center;
  opacity: 0.85;
}
</style>
