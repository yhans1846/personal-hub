<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { RefreshCw, Check } from 'lucide-vue-next'
import { getCaptcha, checkCaptcha } from '@/modules/system/api'

const emit = defineEmits<{
  change: [payload: { captchaId: string; sliderX: number; ready: boolean }]
}>()

const loading = ref(false)
const checking = ref(false)
const captchaId = ref('')
const emptyIndex = ref(0)
const shelfBooks = ref<string[]>([])
const dragBook = ref('📘')
const ready = ref(false)
const placedIndex = ref<number | null>(null)
const dragging = ref(false)
const dragX = ref(0)
const dragY = ref(0)
const startPointer = ref({ x: 0, y: 0 })
const slotRefs = ref<(HTMLElement | null)[]>([])

function setSlotRef(el: unknown, i: number) {
  if (el && typeof el === 'object' && 'getBoundingClientRect' in (el as object)) {
    slotRefs.value[i] = el as HTMLElement
  }
}

function emitChange() {
  emit('change', {
    captchaId: captchaId.value,
    sliderX: placedIndex.value ?? -1,
    ready: ready.value && !!captchaId.value,
  })
}

async function load() {
  loading.value = true
  ready.value = false
  placedIndex.value = null
  dragging.value = false
  dragX.value = 0
  dragY.value = 0
  try {
    const res = await getCaptcha()
    const data = res.data.data
    captchaId.value = data.captchaId
    emptyIndex.value = data.emptyIndex
    shelfBooks.value = data.shelfBooks
    dragBook.value = data.dragBook
    slotRefs.value = Array.from({ length: data.slotCount }, () => null)
  } catch {
    captchaId.value = ''
  } finally {
    loading.value = false
    emitChange()
  }
}

const shelfDisplay = computed(() =>
  shelfBooks.value.map((b, i) => ({
    book: placedIndex.value === i ? dragBook.value : b,
    empty: i === emptyIndex.value && placedIndex.value !== i,
    filled: placedIndex.value === i,
  })),
)

function onPointerDown(e: PointerEvent) {
  if (loading.value || checking.value || ready.value || !captchaId.value) return
  e.preventDefault()
  const target = e.currentTarget as HTMLElement
  dragging.value = true
  ready.value = false
  placedIndex.value = null
  startPointer.value = { x: e.clientX, y: e.clientY }
  dragX.value = 0
  dragY.value = 0
  target.setPointerCapture?.(e.pointerId)
  emitChange()
}

function onPointerMove(e: PointerEvent) {
  if (!dragging.value) return
  dragX.value = e.clientX - startPointer.value.x
  dragY.value = e.clientY - startPointer.value.y
}

function hitSlot(clientX: number, clientY: number): number | null {
  for (let i = 0; i < slotRefs.value.length; i++) {
    const el = slotRefs.value[i]
    if (!el) continue
    const r = el.getBoundingClientRect()
    const pad = 6
    if (
      clientX >= r.left - pad
      && clientX <= r.right + pad
      && clientY >= r.top - pad
      && clientY <= r.bottom + pad
    ) {
      return i
    }
  }
  return null
}

async function onPointerUp(e: PointerEvent) {
  if (!dragging.value) return
  dragging.value = false
  const hit = hitSlot(e.clientX, e.clientY)
  dragX.value = 0
  dragY.value = 0

  if (hit === null) {
    placedIndex.value = null
    emitChange()
    return
  }

  checking.value = true
  try {
    const res = await checkCaptcha(captchaId.value, hit)
    if (res.data.data?.matched === true) {
      placedIndex.value = hit
      ready.value = true
      emitChange()
    } else {
      placedIndex.value = null
      emitChange()
    }
  } catch {
    placedIndex.value = null
    emitChange()
  } finally {
    checking.value = false
  }
}

onMounted(() => load())
defineExpose({ refresh: load })
</script>

<template>
  <div class="shelf-captcha" :class="{ loading: loading || checking, ready }">
    <div class="shelf-head">
      <span class="shelf-title">请把知识放回书架</span>
      <button type="button" class="refresh-btn" title="换一题" :disabled="loading || checking || ready" @click="load">
        <RefreshCw :size="14" stroke-width="1.5" :class="{ spin: loading }" />
      </button>
    </div>

    <div class="board">
      <div class="drag-zone">
        <div
          v-show="!ready"
          class="drag-book"
          :class="{ dragging, 'hidden-src': placedIndex !== null }"
          :style="dragging ? { transform: `translate(${dragX}px, ${dragY}px)` } : undefined"
          @pointerdown="onPointerDown"
          @pointermove="onPointerMove"
          @pointerup="onPointerUp"
          @pointercancel="onPointerUp"
        >
          <span class="book-emoji">{{ dragBook }}</span>
        </div>
        <div v-if="ready" class="ok-tip">
          <Check :size="14" stroke-width="2.25" />
          <span>知识已归位</span>
        </div>
        <div v-else-if="!dragging && placedIndex === null" class="hint-arrow">↓</div>
      </div>

      <div class="shelf" role="list">
        <div
          v-for="(slot, i) in shelfDisplay"
          :key="i"
          :ref="(el) => setSlotRef(el, i)"
          class="slot"
          :class="{ empty: slot.empty, filled: slot.filled }"
          role="listitem"
        >
          <span v-if="slot.book" class="book-emoji">{{ slot.book }}</span>
          <span v-else class="slot-gap" />
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.shelf-captcha {
  --avocado: #8fbc5a;
  --hint: #9ca3af;

  width: 100%;
  border-radius: 14px;
  background: linear-gradient(165deg, #f3fbf6 0%, #eef8f9 100%);
  padding: 14px 14px 16px;
  user-select: none;
  touch-action: none;
}
.shelf-captcha.ready {
  background: linear-gradient(165deg, #eaf8ee 0%, #e8f6f4 100%);
}
.shelf-captcha.loading { opacity: 0.9; }

.shelf-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 10px;
}
.shelf-title {
  font-size: 13px;
  font-weight: 500;
  color: #4b5563;
}
.refresh-btn {
  width: 28px;
  height: 28px;
  border: none;
  border-radius: 8px;
  background: rgba(255, 255, 255, 0.85);
  color: #8aa88f;
  display: grid;
  place-items: center;
  cursor: pointer;
}
.refresh-btn:hover:not(:disabled) { color: var(--avocado); }
.refresh-btn:disabled { opacity: 0.4; cursor: default; }
.spin { animation: spin 0.85s linear infinite; }
@keyframes spin { to { transform: rotate(360deg); } }

.board { display: flex; flex-direction: column; align-items: center; gap: 4px; }

.drag-zone {
  position: relative;
  height: 52px;
  width: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
}
.drag-book {
  width: 44px;
  height: 44px;
  border-radius: 12px;
  background: rgba(255, 255, 255, 0.95);
  box-shadow: 0 2px 8px rgba(17, 24, 39, 0.06);
  display: grid;
  place-items: center;
  cursor: grab;
  z-index: 3;
  touch-action: none;
  animation: book-float 3.2s ease-in-out infinite;
}
.drag-book.dragging {
  cursor: grabbing;
  z-index: 6;
  box-shadow: 0 8px 20px rgba(143, 188, 90, 0.22);
  animation: none;
}
.drag-book.hidden-src { visibility: hidden; pointer-events: none; animation: none; }
@keyframes book-float {
  0%, 100% { transform: translateY(0); }
  50% { transform: translateY(-4px); }
}
.book-emoji { font-size: 26px; line-height: 1; }
.hint-arrow {
  position: absolute;
  bottom: 0;
  font-size: 13px;
  color: var(--hint);
  pointer-events: none;
  animation: arrow-bob 1.6s ease-in-out infinite;
}
@keyframes arrow-bob {
  0%, 100% { transform: translateY(0); opacity: 0.55; }
  50% { transform: translateY(3px); opacity: 1; }
}
.ok-tip {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  font-size: 13px;
  color: var(--avocado);
  font-weight: 500;
}

.shelf {
  display: flex;
  gap: 8px;
  width: 100%;
  justify-content: center;
  padding: 8px 4px 2px;
}
.slot {
  width: 48px;
  height: 52px;
  border-radius: 10px;
  background: rgba(255, 255, 255, 0.75);
  display: grid;
  place-items: center;
  transition: transform 0.2s ease-out, background 0.2s ease-out, box-shadow 0.2s ease-out;
}
.slot:hover:not(.empty) {
  transform: translateY(-3px);
}
.slot.empty {
  background: rgba(255, 255, 255, 0.4);
  box-shadow: inset 0 0 0 1.5px rgba(143, 188, 90, 0.4);
  animation: slot-pulse 2s ease-in-out infinite;
}
@keyframes slot-pulse {
  0%, 100% { box-shadow: inset 0 0 0 1.5px rgba(143, 188, 90, 0.35); }
  50% { box-shadow: inset 0 0 0 1.5px rgba(143, 188, 90, 0.7); }
}
.slot.filled {
  background: #fff;
  box-shadow: 0 2px 8px rgba(143, 188, 90, 0.14);
}
.slot-gap {
  width: 16px;
  height: 26px;
  border-radius: 3px;
  border: 1.5px dashed rgba(143, 188, 90, 0.5);
}
</style>
