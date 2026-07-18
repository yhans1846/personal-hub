import { ref, watch, type Ref } from 'vue'
import { ElMessage } from 'element-plus'
import { handleApiError } from '@/utils/apiResult'

/** 与 defineEmits<{ 'update:modelValue': [boolean]; saved: [] }> 对齐 */
export type EntityDialogEmit = {
  (e: 'update:modelValue', value: boolean): void
  (e: 'saved'): void
}

export function useEntityDialog(options: {
  modelValue: Ref<boolean>
  entityId: Ref<number | undefined>
  emit: EntityDialogEmit
  loadEntity: (id: number) => Promise<void>
}) {
  const loading = ref(false)

  watch(() => options.modelValue.value, async (val) => {
    if (val && options.entityId.value) {
      loading.value = true
      try {
        await options.loadEntity(options.entityId.value)
      } catch (e) {
        handleApiError(e, '加载失败')
      } finally {
        loading.value = false
      }
    }
  })

  function close() {
    options.emit('update:modelValue', false)
  }

  function onSaved() {
    options.emit('saved')
    close()
  }

  return { loading, close, onSaved }
}

/**
 * CRUD Dialog 保存：校验 → create/update → 提示 → onSaved
 */
export function useEntityFormSave(options: {
  entityId: Ref<number | undefined>
  /** 返回警告文案则中止；返回 null 继续 */
  validate: () => string | null
  create: () => Promise<void>
  update: (id: number) => Promise<void>
  onSaved: () => void
  createSuccessMessage?: string
  updateSuccessMessage?: string
  errorMessage?: string
}) {
  const saving = ref(false)

  async function handleSave() {
    const warn = options.validate()
    if (warn) {
      ElMessage.warning(warn)
      return
    }
    saving.value = true
    try {
      if (options.entityId.value != null) {
        await options.update(options.entityId.value)
        ElMessage.success(options.updateSuccessMessage ?? '已更新')
      } else {
        await options.create()
        ElMessage.success(options.createSuccessMessage ?? '已创建')
      }
      options.onSaved()
    } catch (e) {
      handleApiError(e, options.errorMessage ?? '保存失败')
    } finally {
      saving.value = false
    }
  }

  return { saving, handleSave }
}
