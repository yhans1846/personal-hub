<script setup lang="ts">
import { ref, type Ref, watch, onMounted, onBeforeUnmount, nextTick, computed } from 'vue'
import Vditor from 'vditor'
import 'vditor/dist/index.css'
import { buildVditorBaseOptions, mapAppThemeToVditor } from './vditorSetup'
import EditorContextMenu from './context-menu/EditorContextMenu.vue'
import { insertImageMarkdown } from './context-menu/contextMenuActions'
import WikiLinkSuggest from './WikiLinkSuggest.vue'
import { useFeatureFlagStore } from '@/store/featureFlagStore'
import { rewriteNoteAssetSrc } from './previewEnhancements'

const props = withDefaults(defineProps<{
  modelValue: string
  editorId: string
  theme: 'light' | 'dark'
  placeholder?: string
  compact?: boolean
  readonly?: boolean
  onUploadImg?: (files: File[], callback: (urls: string[]) => void) => void
  /** 用于重写相对路径图片（images/、attachments/）为完整 API URL */
  noteId?: number | null
}>(), {
  placeholder: '开始写作...',
  compact: false,
  readonly: false,
  noteId: null,
})

const emit = defineEmits<{
  'update:modelValue': [value: string]
  ready: [vditor: Vditor]
}>()

const featureFlags = useFeatureFlagStore()
const wikiEnabled = computed(() => featureFlags.isEnabled('backlink') && !props.readonly)

const containerRef = ref<HTMLElement | null>(null)
const irRoot = ref<HTMLElement | null>(null)
const contextMenuRef = ref<InstanceType<typeof EditorContextMenu> | null>(null)
const vditorRef = ref<Vditor | null>(null) as Ref<Vditor | null>
const vditorReady = ref(false)
let vditor: Vditor | null = null
let syncingExternal = false
let pendingExternalValue: string | null = null
/** 初始化完成前忽略 input，避免归一化内容误触发父级 dirty */
let suppressInput = true
/** 编辑器内图片 src 重写 MutationObserver */
let imgObserver: MutationObserver | null = null

function getVditor() {
  return vditorRef.value
}

function focus() {
  vditor?.focus()
}

function onContextMenu(e: MouseEvent) {
  contextMenuRef.value?.openAt(e)
}

function onUploadImage(files: File[]) {
  if (!props.onUploadImg || !vditor) return
  props.onUploadImg(files, (urls) => {
    urls.forEach((url) => insertImageMarkdown(vditor!, url))
  })
}

function resolveIrRoot() {
  const host = containerRef.value
  if (!host) {
    irRoot.value = null
    return
  }
  irRoot.value = host.querySelector('.vditor-ir') as HTMLElement | null
}

/** 实际产生滚动的节点（IR 或内部 reset） */
function getScrollEl(): HTMLElement | null {
  const host = containerRef.value
  const ir = irRoot.value ?? (host?.querySelector('.vditor-ir') as HTMLElement | null)
  if (!ir) return null
  if (ir.scrollHeight > ir.clientHeight + 1) return ir
  const reset = ir.querySelector('.vditor-reset') as HTMLElement | null
  if (reset && reset.scrollHeight > reset.clientHeight + 1) return reset
  // 尚未撑开时仍返回 ir，供后续绑定；滚动事件挂在可 overflow 的节点上
  return ir
}

/** 重写编辑器内相对路径图片（images/、attachments/）为完整 API URL */
function rewriteEditorImages() {
  const root = irRoot.value
  if (!root) return
  const token = localStorage.getItem('token')
  if (!token || !props.noteId) return
  root.querySelectorAll('img[src^="images/"], img[src^="attachments/"]').forEach((img) => {
    rewriteNoteAssetSrc(img as HTMLImageElement, props.noteId!, token)
  })
}

/** 在编辑器 IR 根节点上监听新 img 元素，自动重写相对路径 */
function setupEditorImageRewrite() {
  imgObserver?.disconnect()
  imgObserver = null
  const root = irRoot.value
  if (!root) return

  // 立即重写已有图片
  rewriteEditorImages()

  imgObserver = new MutationObserver((mutations) => {
    let needsRewrite = false
    for (const m of mutations) {
      if (m.type === 'childList') {
        for (const node of m.addedNodes) {
          if (node instanceof HTMLImageElement ||
              (node instanceof Element && node.querySelector('img'))) {
            needsRewrite = true
            break
          }
        }
      }
      if (needsRewrite) break
    }
    if (needsRewrite) rewriteEditorImages()
  })

  imgObserver.observe(root, { childList: true, subtree: true })
}

function onWikiPick(title: string, query: string) {
  if (!vditor) return
  const openQuery = query ?? ''
  const md = vditor.getValue()
  const open = `[[${openQuery}`
  // 从后往前找未闭合的 [[query，整段换成 [[title]]，避免 DOM 删不全留下多余 [[
  let idx = md.lastIndexOf(open)
  while (idx >= 0) {
    const rest = md.slice(idx + open.length)
    const closeAt = rest.indexOf(']]')
    const nlAt = rest.indexOf('\n')
    const stillOpen = closeAt < 0 || (nlAt >= 0 && nlAt < closeAt)
    if (stillOpen) {
      const next = `${md.slice(0, idx)}[[${title}]]${rest}`
      syncingExternal = true
      vditor.setValue(next)
      syncingExternal = false
      emit('update:modelValue', next)
      vditor.focus()
      return
    }
    idx = md.lastIndexOf(open, idx - 1)
  }
  // 回退：光标处直接插入完整 wiki（不先留 [[）
  vditor.insertValue(`[[${title}]]`)
  vditor.focus()
}

async function initVditor() {
  if (!containerRef.value) return
  suppressInput = true
  vditor = new Vditor(containerRef.value, {
    ...buildVditorBaseOptions({
      height: props.compact ? '100%' : 'auto',
      minHeight: props.compact ? 200 : 400,
      placeholder: props.placeholder,
      value: props.modelValue,
      input: (value: string) => {
        if (syncingExternal || suppressInput) return
        emit('update:modelValue', value)
      },
      after: () => {
        vditorRef.value = vditor
        vditor?.setTheme(mapAppThemeToVditor(props.theme))
        if (props.readonly) vditor?.disabled()
        vditorReady.value = true
        resolveIrRoot()
        if (pendingExternalValue !== null && vditor && vditor.getValue() !== pendingExternalValue) {
          syncingExternal = true
          vditor.setValue(pendingExternalValue)
          syncingExternal = false
          pendingExternalValue = null
        }
        nextTick(() => {
          if (vditor) {
            const finalValue = vditor.getValue()
            if (finalValue !== props.modelValue) {
              emit('update:modelValue', finalValue)
            }
          }
          suppressInput = false
          resolveIrRoot()
          setupEditorImageRewrite()
          emit('ready', vditor!)
        })
      },
      upload: props.onUploadImg
        ? {
            accept: 'image/*',
            handler: (files: File[]): Promise<null> => {
              return new Promise<null>((resolve) => {
                props.onUploadImg!(files, (urls) => {
                  urls.forEach((url) => insertImageMarkdown(vditor!, url))
                  resolve(null)
                })
              })
            },
          }
        : undefined,
    }),
  })
}

watch(
  () => props.modelValue,
  (val) => {
    if (!vditor || !vditorReady.value) {
      pendingExternalValue = val
      return
    }
    if (vditor.getValue() === val) return
    syncingExternal = true
    vditor.setValue(val)
    syncingExternal = false
  },
)

watch(
  () => props.theme,
  (theme) => {
    vditor?.setTheme(mapAppThemeToVditor(theme))
  },
)

watch(
  () => props.noteId,
  (id) => {
    if (id && vditorReady.value) rewriteEditorImages()
  },
)

onMounted(() => {
  nextTick(initVditor)
})

onBeforeUnmount(() => {
  imgObserver?.disconnect()
  imgObserver = null
  vditor?.destroy()
  vditor = null
  vditorRef.value = null
})

defineExpose({ getVditor, focus, getScrollEl })
</script>

<template>
  <div
    class="note-vditor"
    :class="{ 'note-vditor--compact': compact, 'note-vditor--readonly': readonly }"
    @contextmenu="onContextMenu"
  >
    <div :id="editorId" ref="containerRef" class="note-vditor-host" />
    <WikiLinkSuggest
      :enabled="wikiEnabled"
      :root-el="irRoot"
      @pick="onWikiPick"
    />
    <EditorContextMenu
      ref="contextMenuRef"
      :vditor="vditorRef"
      @upload-image="onUploadImage"
    />
  </div>
</template>

<style scoped>
.note-vditor {
  position: relative;
  min-height: 60vh;
}
.note-vditor--compact {
  min-height: 0;
  height: 100%;
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}
.note-vditor-host {
  flex: 1;
  min-height: 0;
  display: flex;
  flex-direction: column;
}
.note-vditor :deep(.vditor) {
  border: none !important;
  background: transparent !important;
}
.note-vditor--compact :deep(.vditor) {
  height: 100% !important;
  display: flex !important;
  flex-direction: column !important;
}
.note-vditor :deep(.vditor-toolbar) {
  display: none !important;
}
.note-vditor :deep(.vditor-ir) {
  font-family: var(--font-sans);
  font-size: 16px;
  line-height: 1.8;
  color: var(--text-primary);
  padding: 0 !important;
}
.note-vditor :deep(.vditor-content) {
  background: transparent !important;
}
.note-vditor--compact :deep(.vditor-content) {
  flex: 1 !important;
  min-height: 0 !important;
  height: auto !important;
  overflow: hidden !important;
}
.note-vditor--compact :deep(.vditor-ir) {
  height: 100% !important;
  overflow-y: auto !important;
  box-sizing: border-box;
  padding: 16px 24px !important;
}
.note-vditor--readonly :deep(.vditor-ir) {
  opacity: 0.95;
}
</style>
