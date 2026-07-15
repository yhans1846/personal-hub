import type { ToolbarNames } from 'md-editor-v3'

/** 编辑器实例 ID 前缀（MdCatalog / 多实例隔离） */
export const EDITOR_ID_PREFIX = 'ph-note-editor'

export function buildEditorId(noteId?: number | null) {
  return noteId ? `${EDITOR_ID_PREFIX}-${noteId}` : `${EDITOR_ID_PREFIX}-new`
}

/**
 * 完整工具栏：覆盖日常 Markdown 写作全部需求
 * - 撤销/重做、排版、标题、列表、代码、链接/图片/表格
 * - Mermaid / KaTeX、格式化、目录、预览与全屏
 */
export const FULL_TOOLBARS: ToolbarNames[] = [
  'revoke', 'next', '|',
  'bold', 'underline', 'italic', 'strikeThrough', '|',
  'title', 'sub', 'sup', 'quote', '|',
  'unorderedList', 'orderedList', 'task', '|',
  'codeRow', 'code', 'link', 'image', 'table', '|',
  'mermaid', 'katex', '|',
  'prettier', 'catalog', '-',
  'preview', 'pageFullscreen', 'fullscreen',
]

/** 分栏模式工具栏：保留高频能力，为预览区让出空间 */
export const SPLIT_TOOLBARS: ToolbarNames[] = [
  'revoke', 'next', '|',
  'bold', 'underline', 'italic', 'strikeThrough', 'title', '|',
  'quote', 'unorderedList', 'orderedList', 'task', '|',
  'codeRow', 'code', 'link', 'image', 'table', '|',
  'mermaid', 'katex', 'prettier',
]
