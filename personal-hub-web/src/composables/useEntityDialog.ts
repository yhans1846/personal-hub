import { ref, watch, type Ref } from 'vue'

export function useEntityDialog(options: {
  modelValue: Ref<boolean>
  entityId: Ref<number | undefined>
  emit: (event: 'update:modelValue' | 'saved', value?: boolean) => void
  loadEntity: (id: number) => Promise<void>
}) {
  const loading = ref(false)

  watch(() => options.modelValue.value, async (val) => {
    if (val && options.entityId.value) {
      loading.value = true
      try {
        await options.loadEntity(options.entityId.value)
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
