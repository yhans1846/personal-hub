<script setup lang="ts">
import { ref, watch, onBeforeUnmount } from 'vue'
import { getNoteList } from '@/modules/knowledge/api'
import { extractOpenWikiQuery } from './wikiLinkUtils'

const props = defineProps<{
  enabled: boolean
  rootEl: HTMLElement | null
}>()

const emit = defineEmits<{
  pick: [title: string, query: string]
}>()

const visible = ref(false)
const items = ref<{ id: number; title: string }[]>([])
const active = ref(0)
const query = ref('')
const pos = ref({ top: 0, left: 0 })
let debounceTimer: ReturnType<typeof setTimeout> | null = null

function getTextBeforeCaret(root: HTMLElement): string | null {
  const sel = window.getSelection()
  if (!sel?.anchorNode || !root.contains(sel.anchorNode)) return null
  try {
    const pre = document.createRange()
    pre.selectNodeContents(root)
    pre.setEnd(sel.anchorNode, sel.anchorOffset)
    return pre.toString()
  } catch {
    return null
  }
}

function updatePosition(root: HTMLElement) {
  const sel = window.getSelection()
  if (!sel?.rangeCount) return
  const rect = sel.getRangeAt(0).getBoundingClientRect()
  const rootRect = root.getBoundingClientRect()
  pos.value = {
    top: rect.bottom - rootRect.top + 4,
    left: Math.max(0, rect.left - rootRect.left),
  }
}

async function search(q: string) {
  query.value = q
  try {
    const res = await getNoteList({ page: 1, size: 8, keyword: q || undefined })
    items.value = (res.data?.data?.records ?? []).map((n) => ({ id: n.id, title: n.title }))
    active.value = 0
    visible.value = true
  } catch {
    items.value = []
    visible.value = false
  }
}

function hide() {
  visible.value = false
  items.value = []
  query.value = ''
}

function onKeyUp(e: KeyboardEvent) {
  if (!props.enabled || !props.rootEl) return
  if (e.key === 'Escape') {
    hide()
    return
  }
  if (visible.value && (e.key === 'ArrowDown' || e.key === 'ArrowUp' || e.key === 'Enter')) {
    e.preventDefault()
    e.stopPropagation()
    if (e.key === 'ArrowDown') {
      active.value = Math.min(active.value + 1, Math.max(0, items.value.length - 1))
    } else if (e.key === 'ArrowUp') {
      active.value = Math.max(active.value - 1, 0)
    } else if (e.key === 'Enter' && items.value[active.value]) {
      pick(items.value[active.value].title)
    }
    return
  }

  const before = getTextBeforeCaret(props.rootEl)
  if (before == null) {
    hide()
    return
  }
  const q = extractOpenWikiQuery(before)
  if (q === null) {
    hide()
    return
  }
  updatePosition(props.rootEl)
  if (debounceTimer) clearTimeout(debounceTimer)
  debounceTimer = setTimeout(() => search(q), 180)
}

function pick(title: string) {
  emit('pick', title, query.value)
  hide()
}

function bind() {
  props.rootEl?.addEventListener('keyup', onKeyUp, true)
}

function unbind() {
  props.rootEl?.removeEventListener('keyup', onKeyUp, true)
}

watch(
  () => [props.enabled, props.rootEl] as const,
  ([enabled, el], _, onCleanup) => {
    unbind()
    if (enabled && el) {
      bind()
      onCleanup(unbind)
    } else {
      hide()
    }
  },
  { immediate: true },
)

onBeforeUnmount(() => {
  unbind()
  if (debounceTimer) clearTimeout(debounceTimer)
})

defineExpose({ hide })
</script>

<template>
  <div
    v-if="visible && items.length"
    class="wiki-suggest"
    :style="{ top: pos.top + 'px', left: pos.left + 'px' }"
  >
    <button
      v-for="(item, i) in items"
      :key="item.id"
      type="button"
      class="wiki-suggest-item"
      :class="{ active: i === active }"
      @mousedown.prevent="pick(item.title)"
    >
      {{ item.title }}
    </button>
  </div>
</template>

<style scoped>
.wiki-suggest {
  position: absolute;
  z-index: 40;
  min-width: 180px;
  max-width: 320px;
  max-height: 240px;
  overflow-y: auto;
  background: var(--bg-card);
  border: 1px solid var(--border-color);
  border-radius: var(--radius-md);
  box-shadow: var(--shadow-md, 0 8px 24px rgba(0, 0, 0, 0.12));
  padding: 4px;
}
.wiki-suggest-item {
  display: block;
  width: 100%;
  text-align: left;
  border: none;
  background: transparent;
  padding: 6px 10px;
  font-size: var(--text-sm);
  color: var(--text-primary);
  border-radius: var(--radius-sm);
  cursor: pointer;
}
.wiki-suggest-item:hover,
.wiki-suggest-item.active {
  background: var(--accent-light);
  color: var(--accent);
}
</style>
