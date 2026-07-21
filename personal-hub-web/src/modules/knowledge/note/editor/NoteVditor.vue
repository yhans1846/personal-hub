<script setup lang="ts">
import { ref, type Ref, watch, onMounted, onBeforeUnmount, nextTick, computed } from 'vue'
import Vditor from 'vditor'
import 'vditor/dist/index.css'
import { buildVditorBaseOptions, mapAppThemeToVditor } from './vditorSetup'
import EditorContextMenu from './context-menu/EditorContextMenu.vue'
import { insertImageMarkdown } from './context-menu/contextMenuActions'
import WikiLinkSuggest from './WikiLinkSuggest.vue'
import { useFeatureFlagStore } from '@/store/featureFlagStore'

const props = withDefaults(defineProps<{
  modelValue: string
  editorId: string
  theme: 'light' | 'dark'
  placeholder?: string
  compact?: boolean
  readonly?: boolean
  onUploadImg?: (files: File[], callback: (urls: string[]) => void) => void
}>(), {
  placeholder: '开始写作...',
  compact: false,
  readonly: false,
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

function onWikiPick(title: string, query: string) {
  if (!vditor) return
  const sel = window.getSelection()
  if (sel?.rangeCount && irRoot.value) {
    try {
      const range = sel.getRangeAt(0)
      if (query.length > 0 && range.startContainer.nodeType === Node.TEXT_NODE
        && range.startOffset >= query.length) {
        range.setStart(range.startContainer, range.startOffset - query.length)
        range.deleteContents()
      }
    } catch { /* fall through to insert */ }
  }
  vditor.insertValue(`${title}]]`)
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

onMounted(() => {
  nextTick(initVditor)
})

onBeforeUnmount(() => {
  vditor?.destroy()
  vditor = null
  vditorRef.value = null
})

defineExpose({ getVditor, focus })
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
}
.note-vditor-host {
  flex: 1;
}
.note-vditor :deep(.vditor) {
  border: none !important;
  background: transparent !important;
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
  height: 100% !important;
}
.note-vditor--readonly :deep(.vditor-ir) {
  opacity: 0.95;
}
</style>
