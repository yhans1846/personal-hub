import { ref, watch, onUnmounted, type Ref } from 'vue'
import { onBeforeRouteLeave } from 'vue-router'
import { createNote, updateNote } from '@/modules/knowledge/api'
import { ElMessageBox } from 'element-plus'

export type SaveStatus = 'idle' | 'dirty' | 'saving' | 'success' | 'error'

interface FormData {
  title: string
  content: string
  categoryIds: number[]
  tagIds: number[]
}

function formSnapshot(form: FormData): string {
  return JSON.stringify({
    title: form.title,
    content: form.content,
    categoryIds: form.categoryIds,
    tagIds: form.tagIds,
  })
}

/**
 * 自动保存状态机
 *
 * IDLE → (内容变化) → DIRTY → (2s debounce) → SAVING → SUCCESS → (2s) → IDLE
 *                                                         ↘ ERROR → (5s retry) → SAVING
 * 保存期间再修改 → 当前保存结束后立即再触发一次
 *
 * 初始加载通过 markReady() 建立基线，避免「打开已有笔记未改动却提示保存」。
 */
export function useAutoSave(
  form: Ref<FormData>,
  initialNoteId?: number,
  /** 新建落库时写入的文件夹；null/undefined = 未分类 */
  initialFolderId?: number | null,
) {
  const status = ref<SaveStatus>('idle')
  const lastSavedAt = ref<number | null>(null)
  const noteId = ref<number | null>(initialNoteId ?? null)

  let debounceTimer: ReturnType<typeof setTimeout> | null = null
  let retryTimer: ReturnType<typeof setTimeout> | null = null
  let dirtyDuringSave = false
  let isSaving = false
  let isDestroyed = false
  /** 初始 hydration 完成前忽略 watch */
  let ready = false
  let baseline = formSnapshot(form.value)

  function clearTimers() {
    if (debounceTimer) { clearTimeout(debounceTimer); debounceTimer = null }
    if (retryTimer) { clearTimeout(retryTimer); retryTimer = null }
  }

  function syncBaseline() {
    baseline = formSnapshot(form.value)
  }

  /**
   * 表单初始数据加载完毕后调用。
   * @param markDirty 若恢复了与服务器不同的草稿，传 true 以进入未保存态
   */
  function markReady(markDirty = false) {
    syncBaseline()
    ready = true
    if (markDirty) {
      status.value = 'dirty'
      if (debounceTimer) clearTimeout(debounceTimer)
      debounceTimer = setTimeout(() => save(), 2000)
    } else {
      status.value = 'idle'
    }
  }

  async function save(): Promise<void> {
    // 空笔记不自动保存
    if (!form.value.title.trim() && !form.value.content.trim()) {
      status.value = 'idle'
      dirtyDuringSave = false
      syncBaseline()
      return
    }

    if (isSaving) {
      dirtyDuringSave = true
      return
    }

    isSaving = true
    status.value = 'saving'

    try {
      const data: {
        title: string
        content: string
        categoryIds: number[]
        tagIds: number[]
        folderId?: number | null
      } = {
        title: form.value.title,
        content: form.value.content,
        categoryIds: form.value.categoryIds,
        tagIds: form.value.tagIds,
      }

      if (noteId.value) {
        await updateNote(noteId.value, data)
      } else {
        data.folderId = initialFolderId ?? null
        const res = await createNote(data)
        noteId.value = res.data.data.id
      }

      if (isDestroyed) return

      syncBaseline()
      status.value = 'success'
      lastSavedAt.value = Date.now()

      // localStorage 备份
      localStorage.setItem(getDraftKey(), JSON.stringify(data))

      // 2 秒后清除成功状态
      setTimeout(() => {
        if (isDestroyed) return
        if (status.value === 'success') {
          status.value = dirtyDuringSave ? 'dirty' : 'idle'
          dirtyDuringSave = false
        }
      }, 2000)
    } catch {
      if (!isDestroyed) {
        status.value = 'error'
        // 5 秒后自动重试
        retryTimer = setTimeout(() => {
          if (isDestroyed) return
          if (status.value === 'error') {
            isSaving = false
            save()
          }
        }, 5000)
      }
    } finally {
      isSaving = false
    }
  }

  /** 立即保存（跳过防抖） */
  function forceSave(): Promise<void> {
    if (debounceTimer) clearTimeout(debounceTimer)
    return save()
  }

  function getDraftKey(): string {
    return `draft_note_${noteId.value || 'new'}`
  }

  /** 从 localStorage 恢复草稿 */
  function restoreDraft(): boolean {
    const key = `draft_note_${initialNoteId || 'new'}`
    const raw = localStorage.getItem(key)
    if (!raw) return false
    try {
      const data = JSON.parse(raw)
      if (data.title || data.content) {
        form.value.title = data.title || ''
        form.value.content = data.content || ''
        form.value.categoryIds = data.categoryIds || []
        form.value.tagIds = data.tagIds || []
        return true
      }
    } catch { /* ignore */ }
    return false
  }

  function clearDraft() {
    localStorage.removeItem(getDraftKey())
  }

  // 监听表单变化 → 2s 防抖自动保存
  watch(
    () => [
      form.value.title,
      form.value.content,
      JSON.stringify(form.value.categoryIds),
      JSON.stringify(form.value.tagIds),
    ],
    () => {
      if (!ready) return
      if (formSnapshot(form.value) === baseline) {
        if (status.value === 'dirty') status.value = 'idle'
        if (debounceTimer) { clearTimeout(debounceTimer); debounceTimer = null }
        return
      }
      if (debounceTimer) clearTimeout(debounceTimer)
      if (status.value === 'success') status.value = 'idle'
      status.value = 'dirty'
      debounceTimer = setTimeout(() => save(), 2000)
    },
  )

  // Ctrl+S: 强制保存
  function handleKeydown(e: KeyboardEvent) {
    if ((e.ctrlKey || e.metaKey) && e.key === 's') {
      e.preventDefault()
      forceSave()
    }
  }
  if (typeof window !== 'undefined') {
    window.addEventListener('keydown', handleKeydown)
  }

  // 路由离开守卫
  onBeforeRouteLeave((_to, _from, next) => {
    if (status.value === 'dirty') {
      ElMessageBox.confirm('有未保存的更改，是否保存后离开？', '提示', {
        confirmButtonText: '保存并离开',
        cancelButtonText: '不保存',
        distinguishCancelAndClose: true,
        type: 'warning',
      })
        .then(async () => {
          try {
            await forceSave()
          } catch { /* ignore save error on leave */ }
          next()
        })
        .catch(() => {
          next()
        })
    } else {
      next()
    }
  })

  // 浏览器关闭/刷新提示
  function handleBeforeUnload(e: BeforeUnloadEvent) {
    if (status.value === 'dirty') {
      e.preventDefault()
      e.returnValue = ''
    }
  }
  if (typeof window !== 'undefined') {
    window.addEventListener('beforeunload', handleBeforeUnload)
  }

  onUnmounted(() => {
    isDestroyed = true
    clearTimers()
    if (typeof window !== 'undefined') {
      window.removeEventListener('keydown', handleKeydown)
      window.removeEventListener('beforeunload', handleBeforeUnload)
    }
  })

  return {
    status,
    noteId,
    lastSavedAt,
    save: forceSave,
    restoreDraft,
    clearDraft,
    markReady,
  }
}
