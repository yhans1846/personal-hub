<script setup lang="ts">
import { ref, watch, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { Link2 } from 'lucide-vue-next'
import { getNoteBacklinks } from '@/modules/knowledge/api'
import { handleApiError } from '@/utils/apiResult'

const props = defineProps<{
  noteId: number
  enabled: boolean
}>()

const router = useRouter()
const loading = ref(false)
const items = ref<{ id: number; title: string }[]>([])

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
  router.push(`/notes/${id}/preview`)
}
</script>

<template>
  <section v-if="enabled" class="note-backlinks">
    <h3 class="note-backlinks-title">
      <Link2 :size="14" />
      被引用
    </h3>
    <p v-if="loading" class="note-backlinks-empty">加载中…</p>
    <p v-else-if="items.length === 0" class="note-backlinks-empty">暂无其他笔记引用</p>
    <ul v-else class="note-backlinks-list">
      <li v-for="item in items" :key="item.id">
        <button type="button" class="note-backlinks-link" @click="openNote(item.id)">
          {{ item.title }}
        </button>
      </li>
    </ul>
  </section>
</template>

<style scoped>
.note-backlinks {
  margin-top: var(--sp-6);
  padding-top: var(--sp-4);
  border-top: 1px solid var(--border-color);
}
.note-backlinks-title {
  display: flex;
  align-items: center;
  gap: var(--sp-2);
  margin: 0 0 var(--sp-3);
  font-size: var(--text-sm);
  font-weight: 600;
  color: var(--text-secondary);
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
.note-backlinks-link {
  border: none;
  background: transparent;
  padding: 4px 0;
  font-size: var(--text-sm);
  color: var(--accent);
  cursor: pointer;
  text-align: left;
}
.note-backlinks-link:hover {
  text-decoration: underline;
}
</style>
