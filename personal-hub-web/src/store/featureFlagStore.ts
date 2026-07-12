import { reactive } from 'vue'
import { defineStore } from 'pinia'
import type { FeatureFlags } from '@/types/layout'

const STORAGE_KEY = 'feature-flags'

const DEFAULTS: FeatureFlags = {
  mermaid: false,
  katex: false,
  aiAssistant: false,
  backlink: false,
}

const FLAG_META: { key: keyof FeatureFlags; label: string; description: string }[] = [
  { key: 'mermaid', label: 'Mermaid 图表渲染', description: '在编辑器和预览中支持 Mermaid 流程图、时序图等' },
  { key: 'katex', label: '数学公式 (KaTeX)', description: '支持行内/块级数学公式渲染' },
  { key: 'aiAssistant', label: 'AI 笔记助手', description: '选中文本后触发 AI 总结/润色/翻译' },
  { key: 'backlink', label: '双向链接 (Backlink)', description: '笔记间 [[引用]] 关系图谱和反向链接面板' },
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
    return flags[key]
  }

  function toggle(key: keyof FeatureFlags) {
    flags[key] = !flags[key]
    saveFlags({ ...flags })
  }

  function resetAll() {
    Object.assign(flags, { ...DEFAULTS })
    saveFlags({ ...flags })
  }

  return { flags, FLAG_META, isEnabled, toggle, resetAll }
})
