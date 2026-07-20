<script setup lang="ts">
import { ref } from 'vue'
import type Vditor from 'vditor'
import ContextMenu from './ContextMenu.vue'
import ContextMenuItem from './ContextMenuItem.vue'
import ContextMenuSeparator from './ContextMenuSeparator.vue'
import ContextMenuSubmenu from './ContextMenuSubmenu.vue'
import TableGridPicker from './TableGridPicker.vue'
import {
  convertSelectionToHeading,
  convertSelectionToParagraph,
  copySelection,
  formatBold,
  formatInlineCode,
  formatItalic,
  formatStrike,
  formatSubscript,
  formatSuperscript,
  insertCodeBlock,
  insertFootnote,
  insertHorizontalRule,
  insertInlineMath,
  insertLink,
  insertList,
  insertMathBlock,
  insertMermaid,
  insertQuote,
  insertReferenceLink,
  insertTable,
  pasteFromClipboard,
  wrapFootnote,
  wrapLink,
  wrapQuote,
  type HeadingLevel,
} from './contextMenuActions'

const props = defineProps<{
  vditor: Vditor | null
}>()

const emit = defineEmits<{ close: []; 'upload-image': [files: File[]] }>()

const menu = ref<{ x: number; y: number; hasSelection: boolean } | null>(null)
const fileInputRef = ref<HTMLInputElement | null>(null)

function close() {
  menu.value = null
  emit('close')
}

function run(action: () => void) {
  if (!props.vditor) return
  action()
  close()
}

function openAt(e: MouseEvent) {
  e.preventDefault()
  const hasSelection = !!props.vditor?.getSelection()
  menu.value = { x: e.clientX, y: e.clientY, hasSelection }
}

function heading(level: HeadingLevel) {
  run(() => convertSelectionToHeading(props.vditor!, level))
}

function onTableSelect(rows: number, cols: number) {
  run(() => insertTable(props.vditor!, rows, cols))
}

function pickImage() {
  fileInputRef.value?.click()
}

function onImagePicked(e: Event) {
  const input = e.target as HTMLInputElement
  const files = input.files
  if (!files?.length) return
  emit('upload-image', Array.from(files))
  input.value = ''
  close()
}

function selectAll() {
  props.vditor?.focus()
  document.execCommand('selectAll')
  close()
}

defineExpose({ openAt })
</script>

<template>
  <input
    ref="fileInputRef"
    type="file"
    accept="image/*"
    multiple
    hidden
    @change="onImagePicked"
  />

  <ContextMenu
    v-if="menu"
    :position="{ x: menu.x, y: menu.y }"
    @close="close"
  >
    <template v-if="menu.hasSelection">
      <div class="group-title">基础操作</div>
      <ContextMenuItem @click="run(copySelection)">复制</ContextMenuItem>
      <ContextMenuSeparator />
      <div class="group-title">格式</div>
      <ContextMenuItem @click="run(() => formatBold(vditor!))">加粗</ContextMenuItem>
      <ContextMenuItem @click="run(() => formatItalic(vditor!))">斜体</ContextMenuItem>
      <ContextMenuItem @click="run(() => formatStrike(vditor!))">删除线</ContextMenuItem>
      <ContextMenuItem @click="run(() => formatInlineCode(vditor!))">行内代码</ContextMenuItem>
      <ContextMenuItem @click="run(() => formatSuperscript(vditor!))">上标</ContextMenuItem>
      <ContextMenuItem @click="run(() => formatSubscript(vditor!))">下标</ContextMenuItem>
      <ContextMenuSeparator />
      <ContextMenuItem @click="run(() => wrapQuote(vditor!))">引用</ContextMenuItem>
      <ContextMenuItem @click="run(() => wrapLink(vditor!))">链接</ContextMenuItem>
      <ContextMenuItem @click="run(() => wrapFootnote(vditor!))">脚注标记</ContextMenuItem>
      <ContextMenuItem @click="run(() => insertInlineMath(vditor!))">行内公式</ContextMenuItem>
      <ContextMenuSeparator />
      <div class="group-title">转换为</div>
      <ContextMenuSubmenu label="标题">
        <ContextMenuItem @click="heading(1)">一级标题</ContextMenuItem>
        <ContextMenuItem @click="heading(2)">二级标题</ContextMenuItem>
        <ContextMenuItem @click="heading(3)">三级标题</ContextMenuItem>
        <ContextMenuItem @click="heading(4)">四级标题</ContextMenuItem>
        <ContextMenuItem @click="heading(5)">五级标题</ContextMenuItem>
        <ContextMenuItem @click="heading(6)">六级标题</ContextMenuItem>
        <ContextMenuSeparator />
        <ContextMenuItem @click="run(() => convertSelectionToParagraph(vditor!))">正文</ContextMenuItem>
      </ContextMenuSubmenu>
    </template>

    <template v-else>
      <div class="group-title">基础操作</div>
      <ContextMenuItem @click="run(() => pasteFromClipboard(vditor!))">粘贴</ContextMenuItem>
      <ContextMenuItem @click="selectAll">全选</ContextMenuItem>
      <ContextMenuSeparator />
      <div class="group-title">插入</div>
      <ContextMenuSubmenu label="标题">
        <ContextMenuItem @click="heading(1)">一级标题</ContextMenuItem>
        <ContextMenuItem @click="heading(2)">二级标题</ContextMenuItem>
        <ContextMenuItem @click="heading(3)">三级标题</ContextMenuItem>
        <ContextMenuItem @click="heading(4)">四级标题</ContextMenuItem>
        <ContextMenuItem @click="heading(5)">五级标题</ContextMenuItem>
        <ContextMenuItem @click="heading(6)">六级标题</ContextMenuItem>
      </ContextMenuSubmenu>
      <ContextMenuItem @click="run(() => insertQuote(vditor!))">引用</ContextMenuItem>
      <ContextMenuItem @click="run(() => insertList(vditor!, 'unordered'))">无序列表</ContextMenuItem>
      <ContextMenuItem @click="run(() => insertList(vditor!, 'ordered'))">有序列表</ContextMenuItem>
      <ContextMenuItem @click="run(() => insertList(vditor!, 'task'))">任务列表</ContextMenuItem>
      <ContextMenuSeparator />
      <ContextMenuItem @click="run(() => insertLink(vditor!))">链接</ContextMenuItem>
      <ContextMenuItem @click="run(() => insertReferenceLink(vditor!))">链接引用</ContextMenuItem>
      <ContextMenuItem @click="run(() => insertFootnote(vditor!))">脚注</ContextMenuItem>
      <ContextMenuSeparator />
      <ContextMenuItem @click="run(() => insertHorizontalRule(vditor!))">水平分割线</ContextMenuItem>
      <ContextMenuSubmenu label="表格">
        <TableGridPicker @select="onTableSelect" />
      </ContextMenuSubmenu>
      <ContextMenuItem @click="run(() => insertCodeBlock(vditor!))">代码块</ContextMenuItem>
      <ContextMenuItem @click="run(() => insertMermaid(vditor!))">Mermaid 图</ContextMenuItem>
      <ContextMenuSubmenu label="公式">
        <ContextMenuItem @click="run(() => insertMathBlock(vditor!))">块级公式</ContextMenuItem>
        <ContextMenuItem @click="run(() => insertInlineMath(vditor!))">行内公式</ContextMenuItem>
      </ContextMenuSubmenu>
      <ContextMenuItem @click="pickImage">图片</ContextMenuItem>
    </template>
  </ContextMenu>
</template>

<style scoped>
.group-title {
  margin: 4px 8px 2px;
  font-size: 10px;
  font-weight: 500;
  letter-spacing: 0.08em;
  text-transform: uppercase;
  color: var(--text-tertiary);
}
</style>
