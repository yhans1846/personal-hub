<script setup lang="ts" generic="T">
import { onMounted, ref, watch } from 'vue'
import { useVirtualList } from '@/composables/useVirtualList'

const props = withDefaults(defineProps<{
  items: T[]
  itemHeight: number
  overscan?: number
  /** 滚动容器最大高度 */
  maxHeight?: string | number
  /** 从 item 取稳定 key；缺省用 index */
  getKey?: (item: T, index: number) => string | number
}>(), {
  overscan: 4,
  maxHeight: 340,
})

const rootRef = ref<HTMLElement | null>(null)
const { totalHeight, visibleItems, itemHeight, onScroll, setViewportHeight } = useVirtualList<T>({
  items: () => props.items,
  itemHeight: props.itemHeight,
  overscan: props.overscan,
})

function maxHeightCss(): string {
  return typeof props.maxHeight === 'number' ? `${props.maxHeight}px` : String(props.maxHeight)
}

onMounted(() => {
  if (rootRef.value) setViewportHeight(rootRef.value.clientHeight)
})

watch(
  () => props.items.length,
  () => {
    if (rootRef.value) setViewportHeight(rootRef.value.clientHeight)
  },
)
</script>

<template>
  <div
    ref="rootRef"
    class="virtual-list"
    :style="{ maxHeight: maxHeightCss() }"
    @scroll="onScroll"
  >
    <div class="virtual-list__spacer" :style="{ height: `${totalHeight}px` }">
      <div
        v-for="row in visibleItems"
        :key="getKey ? getKey(row.item, row.index) : row.index"
        class="virtual-list__row"
        :style="{
          height: `${itemHeight}px`,
          transform: `translateY(${row.offset}px)`,
        }"
      >
        <slot :item="row.item" :index="row.index" />
      </div>
    </div>
  </div>
</template>

<style scoped>
.virtual-list {
  overflow-y: auto;
  position: relative;
}
.virtual-list__spacer {
  position: relative;
  width: 100%;
}
.virtual-list__row {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  box-sizing: border-box;
}
</style>
