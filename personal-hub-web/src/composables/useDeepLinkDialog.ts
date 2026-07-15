import { onMounted, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { parseDeepLinkQuery } from '@/utils/deepLink'

/**
 * 消费列表页深链 query（?edit=&create=），打开对应弹窗并清理 URL。
 */
export function useDeepLinkDialog(handlers: {
  openCreate: () => void
  openEdit: (id: number) => void
}) {
  const route = useRoute()
  const router = useRouter()

  function clearDeepLinkQuery() {
    if (route.query.edit == null && route.query.create == null) return
    const next = { ...route.query }
    delete next.edit
    delete next.create
    router.replace({ path: route.path, query: next })
  }

  function consume() {
    const { editId, create } = parseDeepLinkQuery(route.query as Record<string, unknown>)
    if (editId != null) {
      handlers.openEdit(editId)
      clearDeepLinkQuery()
      return
    }
    if (create) {
      handlers.openCreate()
      clearDeepLinkQuery()
    }
  }

  onMounted(consume)
  watch(() => [route.query.edit, route.query.create], consume)
}
