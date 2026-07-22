import { ref } from 'vue'

/** 跨列表/树共享的拖拽类型（dragover 阶段 getData 不可用） */
export const noteFolderDraggingKind = ref<'note' | 'folder' | null>(null)

export type FolderDropPlace = 'before' | 'after' | 'inside'

export type FolderDropHint =
  | { kind: 'none' }
  | { kind: 'root' }
  | { kind: 'folder'; id: number; place: FolderDropPlace }
