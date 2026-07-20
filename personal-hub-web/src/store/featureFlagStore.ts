import { reactive } from 'vue'
import { defineStore } from 'pinia'
import type { FeatureFlags } from '@/types/layout'

const STORAGE_KEY = 'feature-flags'

/** 仅保留未落地实验项；mermaid/katex 已毕业为正式能力 */
const DEFAULTS: FeatureFlags = {
  aiAssistant: false,
  backlink: false,
}

export type FlagMeta = {
  key: keyof FeatureFlags
  label: string
  description: string
  /** false = 尚未接入运行时，设置页禁用开关 */
  available: boolean
}

const FLAG_META: FlagMeta[] = [
  { key: 'aiAssistant', label: 'AI 笔记助手', description: '选中文本后触发 AI 总结/润色/翻译（即将推出）', available: false },
  { key: 'backlink', label: '双向链接 (Backlink)', description: '笔记间 [[引用]] 关系图谱（即将推出）', available: false },
]

function sanitize(raw: Record<string, unknown>): FeatureFlags {
  return {
    aiAssistant: typeof raw.aiAssistant === 'boolean' ? raw.aiAssistant : DEFAULTS.aiAssistant,
    backlink: typeof raw.backlink === 'boolean' ? raw.backlink : DEFAULTS.backlink,
  }
}

function loadFlags(): FeatureFlags {
  try {
    const stored = localStorage.getItem(STORAGE_KEY)
    if (stored) {
      const flags = sanitize(JSON.parse(stored) as Record<string, unknown>)
      // 剥离已毕业的 mermaid/katex 等遗留键
      saveFlags(flags)
      return flags
    }
  } catch { /* ignore */ }
  return { ...DEFAULTS }
}

function saveFlags(flags: FeatureFlags) {
  localStorage.setItem(STORAGE_KEY, JSON.stringify(flags))
}

export const useFeatureFlagStore = defineStore('featureFlags', () => {
  const flags = reactive<FeatureFlags>(loadFlags())

  function isEnabled(key: keyof FeatureFlags): boolean {
    return !!flags[key]
  }

  function toggle(key: keyof FeatureFlags) {
    const meta = FLAG_META.find((m) => m.key === key)
    if (meta && !meta.available) return
    flags[key] = !flags[key]
    saveFlags({ ...flags })
  }

  function resetAll() {
    Object.assign(flags, { ...DEFAULTS })
    saveFlags({ ...flags })
  }

  return { flags, FLAG_META, isEnabled, toggle, resetAll }
})
