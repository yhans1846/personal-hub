import { computed } from 'vue'
import { useFeatureFlagStore } from '@/store/featureFlagStore'
import type { FeatureFlags } from '@/types/layout'

/**
 * 实验功能开关读取 composable
 * @param key 功能 key
 * @returns ref<boolean> 响应式开关状态
 */
export function useFeatureFlag(key: keyof FeatureFlags) {
  const store = useFeatureFlagStore()
  return computed(() => store.flags[key])
}
