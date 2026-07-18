<script setup lang="ts">
import { computed, useSlots } from 'vue'

defineOptions({ inheritAttrs: false })

const props = withDefaults(defineProps<{
  modelValue: boolean
  title?: string
  subtitle?: string
  /** sm=520 · md=720 · lg=860 */
  size?: 'sm' | 'md' | 'lg'
  width?: string | number
  destroyOnClose?: boolean
}>(), {
  size: 'md',
  destroyOnClose: true,
})

defineEmits<{ 'update:modelValue': [boolean] }>()

const slots = useSlots()
const hasFooter = computed(() => Boolean(slots.footer))

const SIZE_MAP = { sm: '520px', md: '720px', lg: '860px' } as const

const resolvedWidth = computed(() => {
  if (props.width != null && props.width !== '') return props.width
  return SIZE_MAP[props.size]
})
</script>

<template>
  <el-dialog
    :model-value="modelValue"
    :title="title"
    :width="resolvedWidth"
    :destroy-on-close="destroyOnClose"
    align-center
    class="ui-dialog"
    :class="{ 'ui-dialog--no-footer': !hasFooter }"
    v-bind="$attrs"
    @update:model-value="$emit('update:modelValue', $event)"
  >
    <p v-if="subtitle" class="ui-dialog__subtitle">{{ subtitle }}</p>
    <div class="ui-dialog__body">
      <slot />
    </div>
    <template v-if="hasFooter" #footer>
      <slot name="footer" />
    </template>
  </el-dialog>
</template>

<style scoped>
.ui-dialog__subtitle {
  margin: 0 0 var(--sp-4);
  font-size: var(--text-sm);
  color: var(--text-secondary);
  line-height: 1.5;
}

.ui-dialog__body {
  max-height: min(70vh, 640px);
  overflow-y: auto;
  scrollbar-width: none;
  -ms-overflow-style: none;
}

.ui-dialog__body::-webkit-scrollbar {
  display: none;
  width: 0;
  height: 0;
}
</style>

<style>
.ui-dialog.el-dialog {
  border-radius: var(--radius-lg);
  overflow: hidden;
  box-shadow: var(--shadow-lg);
}

.ui-dialog .el-dialog__header {
  padding: var(--sp-5) var(--sp-6) var(--sp-3);
  margin-right: 0;
  border-bottom: 1px solid var(--border);
}

.ui-dialog .el-dialog__title {
  font-size: var(--text-lg);
  font-weight: 600;
  color: var(--text);
  letter-spacing: -0.01em;
}

.ui-dialog .el-dialog__headerbtn {
  top: var(--sp-5);
  right: var(--sp-5);
  width: 28px;
  height: 28px;
}

.ui-dialog .el-dialog__body {
  padding: var(--sp-5) var(--sp-6);
}

.ui-dialog .el-dialog__footer {
  padding: var(--sp-3) var(--sp-6) var(--sp-5);
  border-top: 1px solid var(--border);
}

.ui-dialog--no-footer .el-dialog__body {
  padding-bottom: var(--sp-6);
}
</style>
