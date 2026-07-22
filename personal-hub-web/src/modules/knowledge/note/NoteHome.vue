<script setup lang="ts">
import { computed } from 'vue'
import { BookOpen, FileText, FolderClosed, Pencil } from 'lucide-vue-next'
import { ElMessage, ElMessageBox } from 'element-plus'
import type { NoteFolderNoteItem, NoteFolderTreeVO, NoteFolderVO } from '@/types/note'
import { formatRelativeUpdated } from '@/utils/formatTime'
import { renameNoteFolder } from '@/modules/knowledge/api'
import { handleApiError } from '@/utils/apiResult'
import UiTooltip from '@/components/UiTooltip.vue'

const props = defineProps<{
  tree: NoteFolderTreeVO | null
}>()

const emit = defineEmits<{
  'open-note': [id: number]
  'select-folder': [id: number]
  changed: []
}>()

const flatFolders = computed(() => {
  const out: { folder: NoteFolderVO; depth: number }[] = []
  const walk = (nodes: NoteFolderVO[], depth: number) => {
    for (const n of nodes) {
      out.push({ folder: n, depth })
      if (n.children?.length) walk(n.children, depth + 1)
    }
  }
  walk(props.tree?.folders ?? [], 0)
  return out
})

const uncategorizedNotes = computed(() => props.tree?.uncategorizedNotes ?? [])
const uncategorizedNoteCount = computed(() => uncategorizedNotes.value.length)

function noteTitle(n: NoteFolderNoteItem) {
  return n.title?.trim() || '无标题笔记'
}

function folderNoteCount(f: NoteFolderVO): number {
  return f.notes?.length ?? 0
}

function onFolderClick(folderId: number) {
  emit('select-folder', folderId)
}

async function onRenameFolder(e: Event, folder: NoteFolderVO) {
  e.stopPropagation()
  try {
    const { value } = await ElMessageBox.prompt('文件夹名称', '重命名', {
      confirmButtonText: '保存',
      cancelButtonText: '取消',
      inputValue: folder.name,
      inputPattern: /\S+/,
      inputErrorMessage: '名称不能为空',
    })
    await renameNoteFolder(folder.id, String(value).trim())
    ElMessage.success('已重命名')
    emit('changed')
  } catch (err) {
    if (err === 'cancel' || err === 'close') return
    handleApiError(err, '重命名失败')
  }
}
</script>

<template>
  <div class="note-home">
    <header class="note-home-hero">
      <div class="note-home-icon">
        <BookOpen :size="28" />
      </div>
      <div>
        <h1 class="note-home-title">笔记</h1>
        <p class="note-home-stats">{{ tree?.totalCount ?? 0 }} 篇文档</p>
      </div>
    </header>

    <section class="note-home-welcome">
      <h2>欢迎来到知识库</h2>
      <p>用文件夹把多篇笔记结构化，方便创作与沉淀。</p>
    </section>

    <section class="note-home-toc">
      <h3 class="note-home-toc-title">目录</h3>

      <template v-for="{ folder, depth } in flatFolders" :key="folder.id">
        <div class="toc-section" :style="{ marginLeft: `${depth * 16}px` }">
          <button
            type="button"
            class="toc-folder"
            @click="onFolderClick(folder.id)"
          >
            <FolderClosed :size="13" class="toc-folder-icon" />
            <span class="toc-folder-name">{{ folder.name }}</span>
            <span class="toc-folder-count">{{ folderNoteCount(folder) }}</span>
            <UiTooltip content="重命名">
              <span class="toc-folder-edit" @click="onRenameFolder($event, folder)">
                <Pencil :size="12" />
              </span>
            </UiTooltip>
          </button>
          <button
            v-for="note in folder.notes ?? []"
            :key="note.id"
            type="button"
            class="toc-note"
            @click="emit('open-note', note.id)"
          >
            <FileText :size="14" class="toc-note-icon" />
            <span class="toc-note-title">{{ noteTitle(note) }}</span>
            <span class="toc-note-time">{{ formatRelativeUpdated(note.updatedAt) }}</span>
          </button>
          <p v-if="!(folder.notes?.length)" class="toc-empty">暂无笔记</p>
        </div>
      </template>

      <div v-if="uncategorizedNoteCount > 0" class="toc-section">
        <div class="toc-folder toc-folder--static">
          <FolderClosed :size="13" class="toc-folder-icon" />
          <span class="toc-folder-name">未分类</span>
          <span class="toc-folder-count">{{ uncategorizedNoteCount }}</span>
        </div>
        <button
          v-for="note in uncategorizedNotes"
          :key="note.id"
          type="button"
          class="toc-note"
          @click="emit('open-note', note.id)"
        >
          <FileText :size="14" class="toc-note-icon" />
          <span class="toc-note-title">{{ noteTitle(note) }}</span>
          <span class="toc-note-time">{{ formatRelativeUpdated(note.updatedAt) }}</span>
        </button>
      </div>

      <p v-if="!(tree?.totalCount)" class="toc-empty">还没有笔记，可在左侧新建文件夹或点击「新建笔记」</p>
    </section>
  </div>
</template>

<style scoped>
.note-home {
  flex: 1;
  min-height: 0;
  overflow: auto;
  padding: 8px 16px 24px;
}
.note-home-toc {
  padding-left: 4px;
}
.note-home-hero {
  display: flex;
  align-items: center;
  gap: 14px;
  margin-bottom: 20px;
}
.note-home-icon {
  width: 52px;
  height: 52px;
  border-radius: var(--radius-lg);
  background: color-mix(in srgb, var(--accent) 18%, transparent);
  color: var(--accent);
  display: flex;
  align-items: center;
  justify-content: center;
}
.note-home-title {
  margin: 0;
  font-size: 22px;
  font-weight: 700;
  line-height: 1.2;
}
.note-home-stats {
  margin: 4px 0 0;
  font-size: var(--text-sm);
  color: var(--text-tertiary);
}
.note-home-welcome {
  padding: 16px 18px;
  border-radius: var(--radius-lg);
  background: color-mix(in srgb, var(--bg-hover) 80%, var(--bg-card));
  margin-bottom: 24px;
}
.note-home-welcome h2 {
  margin: 0;
  font-size: var(--text-base);
  font-weight: 600;
}
.note-home-welcome p {
  margin: 8px 0 0;
  font-size: var(--text-sm);
  color: var(--text-secondary);
  line-height: 1.5;
}
.note-home-toc-title {
  margin: 0 0 16px;
  font-size: var(--text-sm);
  font-weight: 600;
  color: var(--text-secondary);
}

/* 文件夹分组 */
.toc-section {
  margin-bottom: 18px;
}
.toc-folder {
  display: flex;
  align-items: center;
  gap: 6px;
  width: 100%;
  padding: 5px 4px 8px;
  font-size: 13px;
  font-weight: 600;
  color: var(--text-primary);
  border: none;
  border-bottom: 1px solid var(--border-color);
  margin-bottom: 4px;
  background: transparent;
  cursor: pointer;
  text-align: left;
  border-radius: 0;
  transition: color var(--transition-duration) ease;
}
.toc-folder:hover {
  color: var(--accent);
}
.toc-folder--static {
  cursor: default;
}
.toc-folder--static:hover {
  color: var(--text-primary);
}
.toc-folder-icon {
  flex-shrink: 0;
  opacity: 0.5;
  color: var(--text-secondary);
}
.toc-folder-name {
  flex: 1;
  min-width: 0;
}
.toc-folder-count {
  font-size: 11px;
  font-weight: 500;
  color: var(--text-tertiary);
  background: color-mix(in srgb, var(--text-tertiary) 10%, transparent);
  padding: 0 6px;
  border-radius: var(--radius-full);
  line-height: 18px;
}
.toc-folder-edit {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 22px;
  height: 22px;
  border-radius: var(--radius-sm);
  color: var(--text-tertiary);
  opacity: 0;
  transition: opacity var(--transition-duration) ease, background var(--transition-duration) ease, color var(--transition-duration) ease;
}
.toc-folder:hover .toc-folder-edit {
  opacity: 1;
}
.toc-folder-edit:hover {
  background: var(--bg-hover);
  color: var(--text-primary);
}

/* 笔记行 */
.toc-note {
  display: flex;
  align-items: center;
  gap: 8px;
  width: 100%;
  border: none;
  background: transparent;
  padding: 7px 8px;
  cursor: pointer;
  text-align: left;
  color: var(--text-primary);
  font-size: var(--text-sm);
  border-radius: var(--radius-sm);
  transition: background var(--transition-duration) ease;
}
.toc-note:hover {
  background: var(--bg-hover);
}
.toc-note-icon {
  flex-shrink: 0;
  opacity: 0.45;
  color: var(--text-tertiary);
}
.toc-note-title {
  flex: 1;
  min-width: 0;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.toc-note-time {
  flex-shrink: 0;
  font-size: 12px;
  color: var(--text-tertiary);
  font-variant-numeric: tabular-nums;
}
.toc-empty {
  margin: 4px 0 0;
  font-size: 12px;
  color: var(--text-tertiary);
}
</style>
