<script setup lang="ts">
import { ref, watch, computed, toRef } from 'vue'
import { createBookmark, updateBookmark, getBookmarkById } from '@/modules/resource/api'
import { getCategories } from '@/modules/knowledge/api'
import { getTags } from '@/modules/knowledge/api'
import { ElMessage } from 'element-plus'
import { FolderOpen, Link } from 'lucide-vue-next'
import {
  UiDialog,
  DialogTitleField,
  DialogPropGrid,
  DialogPropCard,
  DialogChipRow,
  DialogEditor,
  DialogFooterActions,
} from '@/components/ui'
import type { CategoryVO } from '@/types/category'
import type { TagVO } from '@/types/tag'
import { useEntityDialog } from '@/composables/useEntityDialog'

const props = withDefaults(defineProps<{
  modelValue: boolean
  entityId?: number
}>(), { entityId: undefined })

const emit = defineEmits<{
  'update:modelValue': [value: boolean]
  'saved': []
}>()

const form = ref({
  title: '',
  url: '',
  description: '',
  categoryId: null as number | null,
  tagIds: [] as number[],
  showOnDashboard: false,
})
const categories = ref<CategoryVO[]>([])
const tags = ref<TagVO[]>([])
const saving = ref(false)

const dialogTitle = computed(() => props.entityId ? '编辑收藏' : '新建收藏')
const dialogSubtitle = computed(() => props.entityId ? '' : '收藏一个值得记录的页面')

const categoryOptions = computed(() => categories.value.map(c => ({ id: c.id, name: c.name })))
const tagOptions = computed(() => tags.value.map(t => ({ id: t.id, name: t.name, color: t.color })))

async function loadEntity(id: number) {
  const res = await getBookmarkById(id)
  const r = res.data.data
  form.value = {
    title: r.title, url: r.url, description: r.description || '',
    categoryId: r.categoryId, tagIds: (r.tags || []).map((t: TagVO) => t.id),
    showOnDashboard: r.showOnDashboard === 1,
  }
}

watch(() => props.modelValue, async (val) => {
  if (!val) return
  try {
    const [catRes, tagRes] = await Promise.all([getCategories('bookmark'), getTags()])
    categories.value = catRes.data.data
    tags.value = tagRes.data.data
  } catch { /* ignore */ }
  if (!props.entityId) {
    form.value = { title: '', url: '', description: '', categoryId: null, tagIds: [], showOnDashboard: false }
  }
})

const { onSaved } = useEntityDialog({
  modelValue: toRef(props, 'modelValue'),
  entityId: toRef(props, 'entityId'),
  emit: (event, value) => emit(event as any, value),
  loadEntity,
})

async function handleSave() {
  if (!form.value.title.trim()) { ElMessage.warning('请输入标题'); return }
  if (!form.value.url.trim()) { ElMessage.warning('请输入网址'); return }
  let url = form.value.url.trim()
  if (!/^https?:\/\//i.test(url)) url = 'https://' + url
  saving.value = true
  try {
    const payload = {
      title: form.value.title,
      url,
      description: form.value.description,
      categoryId: form.value.categoryId,
      tagIds: form.value.tagIds,
      showOnDashboard: form.value.showOnDashboard ? 1 : 0,
    }
    if (props.entityId) {
      await updateBookmark(props.entityId, payload)
      ElMessage.success('已更新')
    } else {
      await createBookmark(payload)
      ElMessage.success('已创建')
    }
    onSaved()
  } finally { saving.value = false }
}
</script>

<template>
  <UiDialog
    :model-value="modelValue"
    :title="dialogTitle"
    :subtitle="dialogSubtitle"
    @update:model-value="emit('update:modelValue', $event)"
  >
    <DialogTitleField v-model="form.title" placeholder="页面标题" maxlength="255" />

    <div class="url-row">
      <Link :size="14" class="url-icon" />
      <input v-model="form.url" class="url-input" placeholder="https://" maxlength="2048" />
    </div>

    <DialogPropGrid :cols="1">
      <DialogPropCard label="分类">
        <DialogChipRow
          v-model="form.categoryId"
          :options="categoryOptions"
          empty-text="暂无分类"
        >
          <template #prefix>
            <FolderOpen :size="13" />
          </template>
        </DialogChipRow>
      </DialogPropCard>
      <DialogPropCard label="标签">
        <DialogChipRow
          v-model="form.tagIds"
          :options="tagOptions"
          multiple
          empty-text="暂无标签"
        />
      </DialogPropCard>
      <DialogPropCard label="首页">
        <div class="dashboard-row">
          <div>
            <div class="dashboard-label">展示到首页</div>
            <p class="dashboard-hint">开启后会出现在首页「外部快捷」卡片</p>
          </div>
          <el-switch v-model="form.showOnDashboard" />
        </div>
      </DialogPropCard>
    </DialogPropGrid>

    <DialogEditor v-model="form.description" size="md" placeholder="简要描述这个收藏…" />

    <template #footer>
      <DialogFooterActions :saving="saving" @cancel="emit('update:modelValue', false)" @confirm="handleSave" />
    </template>
  </UiDialog>
</template>

<style scoped>
.url-row {
  display: flex;
  align-items: center;
  gap: 6px;
  margin: calc(var(--sp-2) * -1) 0 var(--sp-4);
}
.url-icon { flex-shrink: 0; color: var(--text-tertiary); }
.url-input {
  flex: 1;
  border: none;
  background: transparent;
  font-size: var(--text-sm);
  color: var(--text-secondary);
  outline: none;
  padding: 4px 0;
}
.url-input::placeholder { color: var(--text-placeholder); }
.url-input:focus { color: var(--text-primary); }

.dashboard-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: var(--sp-3);
}
.dashboard-label {
  font-size: var(--text-sm);
  color: var(--text-primary);
  font-weight: 500;
}
.dashboard-hint {
  margin: 4px 0 0;
  font-size: var(--text-xs);
  color: var(--text-tertiary);
}
</style>
