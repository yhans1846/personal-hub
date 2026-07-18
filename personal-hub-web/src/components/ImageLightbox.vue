<script setup lang="ts">
import { computed, watch, nextTick, ref } from 'vue'
import { X, ChevronLeft, ChevronRight } from 'lucide-vue-next'

const props = defineProps<{
  modelValue: boolean
  urls: string[]
  index?: number
}>()

const emit = defineEmits<{
  'update:modelValue': [boolean]
  'update:index': [number]
}>()

const rootRef = ref<HTMLElement | null>(null)

const current = computed({
  get: () => Math.min(Math.max(props.index ?? 0, 0), Math.max(props.urls.length - 1, 0)),
  set: (v: number) => emit('update:index', v),
})

const src = computed(() => props.urls[current.value] || '')
const multi = computed(() => props.urls.length > 1)

function close() {
  emit('update:modelValue', false)
}

function prev() {
  if (!multi.value) return
  current.value = (current.value - 1 + props.urls.length) % props.urls.length
}

function next() {
  if (!multi.value) return
  current.value = (current.value + 1) % props.urls.length
}

function onKey(e: KeyboardEvent) {
  if (e.key === 'Escape') close()
  if (e.key === 'ArrowLeft') prev()
  if (e.key === 'ArrowRight') next()
}

watch(() => props.modelValue, async (open) => {
  if (!open) return
  await nextTick()
  rootRef.value?.focus()
})
</script>

<template>
  <Teleport to="body">
    <div
      v-if="modelValue && src"
      ref="rootRef"
      class="lightbox"
      tabindex="0"
      @click.self="close"
      @keydown="onKey"
    >
      <button type="button" class="lb-close" aria-label="关闭" @click="close">
        <X :size="18" />
      </button>
      <button v-if="multi" type="button" class="lb-nav lb-nav--prev" aria-label="上一张" @click="prev">
        <ChevronLeft :size="22" />
      </button>
      <img :src="src" class="lb-img" alt="" @click.stop />
      <button v-if="multi" type="button" class="lb-nav lb-nav--next" aria-label="下一张" @click="next">
        <ChevronRight :size="22" />
      </button>
      <div v-if="multi" class="lb-counter">{{ current + 1 }} / {{ urls.length }}</div>
    </div>
  </Teleport>
</template>

<style scoped>
.lightbox {
  position: fixed;
  inset: 0;
  z-index: 4000;
  display: flex;
  align-items: center;
  justify-content: center;
  background: rgba(15, 23, 42, 0.45);
  padding: 32px 48px;
  outline: none;
}
.lb-img {
  max-width: min(720px, 88vw);
  max-height: min(72vh, 640px);
  object-fit: contain;
  border-radius: var(--radius-md);
  box-shadow: 0 16px 48px rgba(0, 0, 0, 0.28);
  background: var(--bg-card, #fff);
}
.lb-close {
  position: absolute;
  top: 16px;
  right: 16px;
  width: 36px;
  height: 36px;
  border: none;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.92);
  color: var(--text-secondary);
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.12);
}
.lb-close:hover { background: #fff; }
.lb-nav {
  position: absolute;
  top: 50%;
  transform: translateY(-50%);
  width: 40px;
  height: 40px;
  border: none;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.92);
  color: var(--text-secondary);
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.12);
}
.lb-nav:hover { background: #fff; }
.lb-nav--prev { left: 16px; }
.lb-nav--next { right: 16px; }
.lb-counter {
  position: absolute;
  bottom: 20px;
  left: 50%;
  transform: translateX(-50%);
  padding: 4px 12px;
  border-radius: var(--radius-md);
  background: rgba(15, 23, 42, 0.65);
  color: #fff;
  font-size: 12px;
  font-weight: 600;
}
</style>
