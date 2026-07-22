<script setup lang="ts">
/**
 * 统一悬停提示：封装 Element Plus Tooltip，默认延迟与 ph-tooltip 样式。
 * 图标按钮等短文案用 content；禁使用原生 title（浏览器默认气泡）。
 */
withDefaults(
  defineProps<{
    content?: string
    placement?: 'top' | 'top-start' | 'top-end' | 'bottom' | 'bottom-start' | 'bottom-end' | 'left' | 'left-start' | 'left-end' | 'right' | 'right-start' | 'right-end'
    disabled?: boolean
    /** 悬停多久后出现（ms） */
    showAfter?: number
    hideAfter?: number
  }>(),
  {
    content: '',
    placement: 'top',
    disabled: false,
    showAfter: 280,
    hideAfter: 0,
  },
)
</script>

<template>
  <el-tooltip
    :content="content"
    :placement="placement"
    :disabled="disabled || !content"
    :show-after="showAfter"
    :hide-after="hideAfter"
    effect="dark"
    popper-class="ph-tooltip"
  >
    <slot />
  </el-tooltip>
</template>
