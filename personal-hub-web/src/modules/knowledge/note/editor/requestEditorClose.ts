export type CloseConfirmChoice = 'save' | 'discard' | 'cancel'
export type CloseResolve = 'proceed' | 'abort'

export async function resolveEditorClose(opts: {
  dirty: boolean
  confirm: () => Promise<CloseConfirmChoice>
  forceSave: () => Promise<unknown>
}): Promise<CloseResolve> {
  if (!opts.dirty) return 'proceed'
  const choice = await opts.confirm()
  if (choice === 'cancel') return 'abort'
  if (choice === 'save') {
    try {
      await opts.forceSave()
    } catch {
      /* 保存失败仍允许调用方决定；本版与路由离开一致：仍 proceed */
    }
    return 'proceed'
  }
  return 'proceed'
}
