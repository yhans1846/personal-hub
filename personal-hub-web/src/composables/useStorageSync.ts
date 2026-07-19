import { reactive, ref, type Ref } from 'vue'

// ========== 类型定义 ==========

export interface StorageSyncOptions<T extends object> {
  /** localStorage 键名 */
  storageKey: string
  /** 默认配置（重置时恢复到此值） */
  defaults: T
  /** 从后端拉取数据的 API 调用，返回原始数据或 null */
  fetchApi: () => Promise<Partial<T> | null>
  /** 向后端保存数据的 API 调用 */
  saveApi: (data: T) => Promise<unknown>
  /** 重置后端的 API 调用（可选，缺省则只重置本地） */
  resetApi?: () => Promise<unknown>
  /** 后端数据与默认值的合并策略：replace（覆盖）| mergeDeep（深层合并） */
  mergeStrategy?: 'replace' | 'mergeDeep'
  /** 调试标签，用于 console.warn 消息 */
  debugLabel?: string
  /** 在读取 localStorage 之前执行（如旧 key 迁移） */
  beforeLoad?: () => void
  /** 从 localStorage 读取数据后的转换钩子（如数据迁移） */
  afterLoad?: (data: Partial<T>) => void
  /** 静默模式：不输出 console.warn（默认 false） */
  silent?: boolean
}

export interface StorageSyncResult<T extends object> {
  /** 响应式配置数据（reactive，可直接 Object.assign 修改） */
  data: T
  /** 后端是否已加载完成 */
  loaded: Ref<boolean>
  /** 从 localStorage 读取 */
  loadFromLocal: () => Partial<T>
  /** 保存到 localStorage */
  saveToLocal: (data: T) => void
  /** 从后端拉取并合并到 data */
  fetchFromBackend: () => Promise<void>
  /** 保存到后端（可选传参，不传则使用内部 data） */
  saveToBackend: (data?: T) => Promise<void>
  /** 重置为默认值 */
  resetConfig: () => Promise<void>
}

// ========== 工具 ==========

/** 简易深层合并（仅处理一层嵌套对象） */
function deepMerge<T extends object>(target: T, source: Partial<T>): T {
  const result: Record<string, unknown> = { ...target }
  const src = source as Record<string, unknown>
  for (const key of Object.keys(src)) {
    const sv = src[key]
    const tv = result[key]
    if (sv !== null && typeof sv === 'object' && !Array.isArray(sv)) {
      result[key] =
        tv !== undefined && typeof tv === 'object' && tv !== null && !Array.isArray(tv)
          ? { ...(tv as object), ...(sv as object) }
          : { ...(sv as object) }
    } else {
      result[key] = sv
    }
  }
  return result as T
}

// ========== Composable ==========

/**
 * localStorage + 后端同步通用 composable
 *
 * 封装以下共通模式：
 * - localStorage 读写
 * - 后端同步（fetch / save / reset）
 * - 错误 fallback 与 console.warn
 * - 合并策略（replace / mergeDeep）
 */
export function useStorageSync<T extends object>(
  options: StorageSyncOptions<T>,
): StorageSyncResult<T> {
  const { storageKey, defaults, debugLabel = '', silent = false } = options
  const label = debugLabel ? `[${debugLabel}]` : ''

  // ---- Hooks ----

  options.beforeLoad?.()

  // ---- localStorage 读写 ----

  function loadFromLocal(): Partial<T> {
    try {
      const stored = localStorage.getItem(storageKey)
      if (stored) {
        const parsed = JSON.parse(stored) as Partial<T>
        options.afterLoad?.(parsed)
        return parsed
      }
    } catch {
      /* ignore */
    }
    return {}
  }

  function saveToLocal(data: T) {
    localStorage.setItem(storageKey, JSON.stringify(data))
  }

  // ---- 响应式状态 ----

  const data = reactive<T>({ ...defaults, ...loadFromLocal() }) as T
  const loaded = ref(false)

  // ---- 后端同步 ----

  async function fetchFromBackend(): Promise<void> {
    try {
      const result = await options.fetchApi()
      if (result != null) {
        const merged =
          options.mergeStrategy === 'mergeDeep'
            ? deepMerge(defaults, result)
            : ({ ...defaults, ...result } as T)
        Object.assign(data, merged)
        saveToLocal(data)
      }
    } catch {
      if (!silent) console.warn(`${label} 后端加载失败，使用本地缓存`)
    } finally {
      loaded.value = true
    }
  }

  async function saveToBackend(dataArg?: T): Promise<void> {
    try {
      await options.saveApi(dataArg ?? data)
    } catch {
      if (!silent) console.warn(`${label} 后端保存失败`)
    }
  }

  async function resetConfig(): Promise<void> {
    Object.assign(data, { ...defaults })
    saveToLocal(data)
    if (options.resetApi) {
      try {
        await options.resetApi()
      } catch {
        if (!silent) console.warn(`${label} 重置后端失败`)
      }
    }
  }

  // ---- 返回 ----

  return {
    data,
    loaded,
    loadFromLocal,
    saveToLocal,
    fetchFromBackend,
    saveToBackend,
    resetConfig,
  }
}
