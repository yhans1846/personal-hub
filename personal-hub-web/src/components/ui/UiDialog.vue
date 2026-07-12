<script setup lang="ts">
import { computed } from 'vue'
import { X } from 'lucide-vue-next'

defineOptions({ inheritAttrs: false })

const props = withDefaults(defineProps<{
  modelValue: boolean
  title?: string
  subtitle?: string
  width?: string | number
  /** body 最大高度（默认 65vh） */
  bodyMaxHeight?: string
  /** 是否显示右上角关闭按钮，默认 true */
  showClose?: boolean
  /** 关闭前回调，返回 false 阻止关闭 */
  beforeClose?: () => boolean | Promise<boolean>
}>(), {
  width: 720,
  bodyMaxHeight: '65vh',
  showClose: true,
  title: '',
  subtitle: '',
  beforeClose: undefined
})

const emit = defineEmits<{
  'update:modelValue': [value: boolean]
}>()

const dialogStyle = computed(() => ({
  width: typeof props.width === 'number' ? `${props.width}px` : props.width
}))

async function handleClose() {
  if (props.beforeClose) {
    const ok = await props.beforeClose()
    if (!ok) return
  }
  emit('update:modelValue', false)
}
</script>

<template>
  <el-dialog
    :model-value="modelValue"
    :width="width"
    :before-close="handleClose"
    align-center
    :show-close="false"
    class="ui-dialog"
    @update:model-value="emit('update:modelValue', $event)"
    v-bind="$attrs"
  >
    <!-- ========== Header ========== -->
    <template #header="{ close, titleId, titleClass }">
      <div class="ui-dialog-header">
        <div class="ui-dialog-header-left">
          <h2 v-if="title" :id="titleId" :class="titleClass" class="ui-dialog-title">{{ title }}</h2>
          <p v-if="subtitle" class="ui-dialog-subtitle">{{ subtitle }}</p>
        </div>
        <div class="ui-dialog-header-right">
          <button
            v-if="showClose"
            class="ui-dialog-close"
            @click="close"
            :aria-label="'关闭'"
          >
            <X :size="20" />
          </button>
        </div>
      </div>
    </template>

    <!-- ========== Body (可滚动) ========== -->
    <div class="ui-dialog-body" :style="{ maxHeight: bodyMaxHeight }">
      <slot />
    </div>

    <!-- ========== Footer ========== -->
    <template #footer>
      <div class="ui-dialog-footer">
        <slot name="footer" />
      </div>
    </template>
  </el-dialog>
</template>

<style scoped>
/* ---- Dialog 容器（覆盖 el-dialog 默认） ---- */
:deep(.el-dialog) {
  border-radius: var(--radius-xl) !important;
  box-shadow: var(--shadow-xl) !important;
  padding: 0 !important;
  overflow: hidden;
}

/* ---- Header ---- */
:deep(.el-dialog__header) {
  padding: 0 !important;
  margin: 0 !important;
}

.ui-dialog-header {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  padding: var(--sp-6) var(--sp-8) var(--sp-3);
  border-bottom: 1px solid var(--border-light);
  position: sticky;
  top: 0;
  z-index: 10;
  background: var(--bg-card);
}

.ui-dialog-header-left {
  flex: 1;
  min-width: 0;
}

.ui-dialog-title {
  font-size: var(--text-xl);
  font-weight: 600;
  color: var(--text-primary);
  line-height: 1.3;
  margin: 0;
}

.ui-dialog-subtitle {
  font-size: var(--text-sm);
  color: var(--text-tertiary);
  margin-top: 2px;
  line-height: 1.4;
}

.ui-dialog-header-right {
  flex-shrink: 0;
  margin-left: var(--sp-4);
}

.ui-dialog-close {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 32px;
  height: 32px;
  border-radius: var(--radius-sm);
  border: none;
  background: transparent;
  color: var(--text-tertiary);
  cursor: pointer;
  transition: all var(--transition);
}
.ui-dialog-close:hover {
  background: var(--bg-hover);
  color: var(--text-primary);
}

/* ---- Body ---- */
:deep(.el-dialog__body) {
  padding: 0 !important;
}

.ui-dialog-body {
  padding: var(--sp-6) var(--sp-8);
  overflow-y: auto;
  overflow-x: hidden;
}

/* ---- Footer ---- */
:deep(.el-dialog__footer) {
  padding: 0 !important;
}

.ui-dialog-footer {
  display: flex;
  justify-content: flex-end;
  align-items: center;
  gap: var(--sp-3);
  padding: var(--sp-4) var(--sp-8) var(--sp-6);
  border-top: 1px solid var(--border-light);
  position: sticky;
  bottom: 0;
  z-index: 10;
  background: var(--bg-card);
}
</style>
