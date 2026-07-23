<script setup lang="ts">
/**
 * 统一悬停提示：封装 Element Plus Tooltip，默认延迟与 ph-tooltip 样式。
 * 图标按钮等短文案用 content；禁使用原生 title（浏览器默认气泡）。
 *
 * 根节点必须是原生元素：el-tooltip 为 Fragment 根，父级（如 el-dropdown）
 * 挂上的 v-click-outside 等运行时指令不能落在组件根上。
 */
defineOptions({ inheritAttrs: false })

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
  <span class="ui-tooltip-root" v-bind="$attrs">
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
  </span>
</template>

<style scoped>
.ui-tooltip-root {
  display: inline-flex;
  max-width: 100%;
  vertical-align: middle;
}
</style>
