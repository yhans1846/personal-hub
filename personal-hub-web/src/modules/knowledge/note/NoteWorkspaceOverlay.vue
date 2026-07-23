<script setup lang="ts">
import { ref } from 'vue'
import { FolderTree } from 'lucide-vue-next'
import Editor from './Editor.vue'
import NoteFolderShell from './NoteFolderShell.vue'
import type { NoteFolderSelection } from '@/types/note'
import UiTooltip from '@/components/UiTooltip.vue'

const props = defineProps<{
  noteId?: number
  /** 新建时落入的文件夹；null = 未分类 */
  folderId?: number | null
}>()

const emit = defineEmits<{
  close: []
  'note-id': [id: number]
}>()

const editorRef = ref<InstanceType<typeof Editor> | null>(null)
const folderSelection = ref<NoteFolderSelection>('home')
const folderDrawerOpen = ref(false)
const currentNoteId = ref<number | undefined>(props.noteId)
const createFolderId = ref<number | null>(props.folderId ?? null)
const editorKey = ref(0)

async function onOpenNote(id: number) {
  if (id === currentNoteId.value) return
  const ok = (await editorRef.value?.requestLeave?.()) ?? true
  if (!ok) return
  currentNoteId.value = id
  createFolderId.value = null
  editorKey.value += 1
}

function onNoteId(id: number) {
  currentNoteId.value = id
  emit('note-id', id)
}

function toggleFolderDrawer() {
  folderDrawerOpen.value = !folderDrawerOpen.value
}
</script>

<template>
  <Teleport to="body">
    <div
      class="note-workspace-overlay"
      role="dialog"
      aria-modal="true"
      aria-label="笔记工作区"
    >
      <div class="workspace-shell">
        <NoteFolderShell
          v-model="folderSelection"
          v-model:drawer-open="folderDrawerOpen"
          :active-note-id="currentNoteId ?? null"
          @open-note="onOpenNote"
        />
        <div class="workspace-main">
          <div class="workspace-mobile-bar">
            <UiTooltip content="文件夹" placement="bottom">
              <button
                type="button"
                class="workspace-folder-toggle"
                @click="toggleFolderDrawer"
              >
                <FolderTree :size="16" />
              </button>
            </UiTooltip>
          </div>
          <Editor
            :key="editorKey"
            ref="editorRef"
            embedded
            :initial-note-id="currentNoteId"
            :initial-folder-id="createFolderId"
            @close="emit('close')"
            @note-id="onNoteId"
          />
        </div>
      </div>
    </div>
  </Teleport>
</template>

<style scoped>
.note-workspace-overlay {
  position: fixed;
  inset: 0;
  z-index: 2000;
  background: var(--bg-body);
}
.workspace-shell {
  display: flex;
  height: 100%;
  min-height: 0;
  position: relative;
}
.workspace-main {
  flex: 1;
  min-width: 0;
  min-height: 0;
  display: flex;
  flex-direction: column;
  position: relative;
}
.workspace-mobile-bar {
  display: none;
}
.workspace-folder-toggle {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 32px;
  height: 32px;
  border: none;
  border-radius: var(--radius-sm);
  background: transparent;
  color: var(--text-tertiary);
  cursor: pointer;
}
.workspace-folder-toggle:hover {
  color: var(--text-primary);
  background: var(--bg-hover);
}
@media (max-width: 768px) {
  .workspace-mobile-bar {
    display: flex;
    align-items: center;
    flex-shrink: 0;
    padding: 6px 8px;
    border-bottom: 1px solid var(--border-color);
    background: var(--bg-card);
  }
}
</style>
