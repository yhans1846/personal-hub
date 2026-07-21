<script setup lang="ts">
import { ref, watch, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { Link2, ChevronRight } from 'lucide-vue-next'
import { getNoteBacklinks } from '@/modules/knowledge/api'
import { handleApiError } from '@/utils/apiResult'

const props = defineProps<{
  noteId: number
  enabled: boolean
}>()

const router = useRouter()
const loading = ref(false)
const items = ref<{ id: number; title: string }[]>([])

const showPanel = computed(() => props.enabled && (loading.value || items.value.length > 0))

async function load() {
  if (!props.enabled || !props.noteId) {
    items.value = []
    return
  }
  loading.value = true
  try {
    const res = await getNoteBacklinks(props.noteId)
    items.value = res.data?.data ?? []
  } catch (e) {
    handleApiError(e, '加载回链失败')
    items.value = []
  } finally {
    loading.value = false
  }
}

watch(
  () => [props.noteId, props.enabled] as const,
  () => { load() },
)

onMounted(load)

function openNote(id: number) {
  window.open(`/notes/${id}/preview`, '_blank')
}
</script>

<template>
  <section v-if="showPanel" class="note-backlinks">
    <div class="note-backlinks-head">
      <Link2 :size="14" class="note-backlinks-icon" />
      <h3 class="note-backlinks-title">被引用</h3>
      <span v-if="!loading && items.length" class="note-backlinks-count">{{ items.length }}</span>
    </div>
    <p v-if="loading" class="note-backlinks-empty">加载中…</p>
    <ul v-else class="note-backlinks-list">
      <li v-for="item in items" :key="item.id">
        <button type="button" class="note-backlinks-item" @click="openNote(item.id)">
          <span class="note-backlinks-item-title">{{ item.title }}</span>
          <ChevronRight :size="14" class="note-backlinks-item-arrow" />
        </button>
      </li>
    </ul>
  </section>
</template>

<style scoped>
.note-backlinks {
  margin-top: var(--sp-8);
  padding: var(--sp-4);
  background: var(--bg-hover);
  border: 1px solid var(--border-color);
  border-radius: var(--radius-md);
}
.note-backlinks-head {
  display: flex;
  align-items: center;
  gap: var(--sp-2);
  margin-bottom: var(--sp-3);
}
.note-backlinks-icon {
  color: var(--text-tertiary);
  flex-shrink: 0;
}
.note-backlinks-title {
  margin: 0;
  font-size: var(--text-xs);
  font-weight: 600;
  letter-spacing: 0.04em;
  text-transform: uppercase;
  color: var(--text-tertiary);
}
.note-backlinks-count {
  margin-left: auto;
  font-size: 11px;
  font-weight: 500;
  color: var(--text-tertiary);
  background: var(--bg-card);
  border: 1px solid var(--border-color);
  border-radius: 999px;
  padding: 0 7px;
  line-height: 18px;
}
.note-backlinks-empty {
  margin: 0;
  font-size: var(--text-xs);
  color: var(--text-tertiary);
}
.note-backlinks-list {
  list-style: none;
  margin: 0;
  padding: 0;
  display: flex;
  flex-direction: column;
  gap: 4px;
}
.note-backlinks-item {
  display: flex;
  align-items: center;
  gap: var(--sp-2);
  width: 100%;
  border: 1px solid transparent;
  background: var(--bg-card);
  padding: 8px 10px;
  border-radius: var(--radius-sm);
  cursor: pointer;
  text-align: left;
  transition: border-color var(--transition), background var(--transition);
}
.note-backlinks-item:hover {
  border-color: var(--accent-border);
  background: color-mix(in srgb, var(--accent) 6%, var(--bg-card));
}
.note-backlinks-item-title {
  flex: 1;
  min-width: 0;
  font-size: var(--text-sm);
  font-weight: 500;
  color: var(--text-primary);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.note-backlinks-item-arrow {
  flex-shrink: 0;
  color: var(--text-tertiary);
  opacity: 0;
  transition: opacity var(--transition);
}
.note-backlinks-item:hover .note-backlinks-item-arrow {
  opacity: 1;
  color: var(--accent);
}
</style>
