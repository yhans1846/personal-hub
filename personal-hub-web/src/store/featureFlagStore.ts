import { reactive } from 'vue'
import { defineStore } from 'pinia'
import type { FeatureFlags } from '@/types/layout'

const STORAGE_KEY = 'feature-flags'

/** mermaid/katex 默认开启，与当前编辑器能力一致；未实现功能保持关闭 */
const DEFAULTS: FeatureFlags = {
  mermaid: true,
  katex: true,
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
  { key: 'mermaid', label: 'Mermaid 图表渲染', description: '右键菜单可插入 Mermaid 流程图等', available: true },
  { key: 'katex', label: '数学公式 (KaTeX)', description: '右键菜单可插入行内/块级公式', available: true },
  { key: 'aiAssistant', label: 'AI 笔记助手', description: '选中文本后触发 AI 总结/润色/翻译（即将推出）', available: false },
  { key: 'backlink', label: '双向链接 (Backlink)', description: '笔记间 [[引用]] 关系图谱（即将推出）', available: false },
]

function loadFlags(): FeatureFlags {
  try {
    const stored = localStorage.getItem(STORAGE_KEY)
    if (stored) return { ...DEFAULTS, ...JSON.parse(stored) }
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
