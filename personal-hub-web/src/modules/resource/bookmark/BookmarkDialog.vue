<script setup lang="ts">
import { ref, watch, computed, toRef } from 'vue'
import { createBookmark, updateBookmark, getBookmarkById } from '@/modules/resource/api'
import { getCategories } from '@/modules/knowledge/api'
import { getTags } from '@/modules/knowledge/api'
import { ElMessage } from 'element-plus'
import { FolderOpen, Link } from 'lucide-vue-next'
import { UiDialog, UiInput, DialogSection, DialogDivider, DialogFooterActions } from '@/components/ui'
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

const selectedCategory = computed(() => categories.value.find(c => c.id === form.value.categoryId))

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

const { loading, close, onSaved } = useEntityDialog({
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
    <!-- 标题 -->
    <UiInput
      v-model="form.title"
      placeholder="页面标题"
      class="title-input"
      maxlength="255"
    />

    <!-- URL -->
    <div class="url-row">
      <Link :size="14" class="url-icon" />
      <input
        v-model="form.url"
        class="url-input"
        placeholder="https://"
        maxlength="2048"
      />
    </div>

    <DialogDivider />

    <DialogSection label="分类">
      <div class="chip-row">
        <button
          v-for="c in categories"
          :key="c.id"
          type="button"
          class="chip"
          :class="{ active: form.categoryId === c.id }"
          @click="form.categoryId = form.categoryId === c.id ? null : c.id"
        >
          <FolderOpen :size="13" />
          {{ c.name }}
        </button>
        <span v-if="categories.length === 0" class="chip-empty">暂无分类</span>
      </div>
    </DialogSection>

    <DialogSection label="标签">
      <div class="chip-row">
        <button
          v-for="t in tags"
          :key="t.id"
          type="button"
          class="chip tag-chip"
          :class="{ active: form.tagIds.includes(t.id) }"
          :style="form.tagIds.includes(t.id) ? { '--chip-color': t.color } : {}"
          @click="form.tagIds = form.tagIds.includes(t.id) ? form.tagIds.filter(id => id !== t.id) : [...form.tagIds, t.id]"
        >
          <span class="tag-dot" :style="{ background: t.color }" />
          {{ t.name }}
        </button>
        <span v-if="tags.length === 0" class="chip-empty">暂无标签</span>
      </div>
    </DialogSection>

    <DialogDivider />

    <div class="dashboard-row">
      <span class="dashboard-label">展示到首页</span>
      <el-switch v-model="form.showOnDashboard" />
    </div>
    <p class="dashboard-hint">开启后会出现在首页「外部快捷」卡片</p>

    <DialogDivider />

    <div class="content-section">
      <textarea
        v-model="form.description"
        class="content-editor"
        placeholder="简要描述这个收藏…"
      />
    </div>

    <template #footer>
      <DialogFooterActions :saving="saving" @cancel="emit('update:modelValue', false)" @confirm="handleSave" />
    </template>
  </UiDialog>
</template>

<style scoped>
.title-input {
  margin-bottom: var(--sp-2);
}
.title-input :deep(input) {
  font-size: var(--text-lg) !important;
  font-weight: 600;
  border: none !important;
  padding-left: 0 !important;
  background: transparent !important;
}
.title-input :deep(input)::placeholder {
  color: var(--text-placeholder);
  font-weight: 400;
}

/* ---- URL ---- */
.url-row {
  display: flex;
  align-items: center;
  gap: 6px;
  margin-bottom: var(--sp-3);
}

.url-icon {
  flex-shrink: 0;
  color: var(--text-tertiary);
}

.url-input {
  flex: 1;
  border: none;
  background: transparent;
  font-size: var(--text-sm);
  color: var(--text-secondary);
  outline: none;
  padding: 4px 0;
}
.url-input::placeholder {
  color: var(--text-placeholder);
}
.url-input:focus {
  color: var(--text-primary);
}

/* ---- Chip 行 ---- */
.chip-row {
  display: flex;
  flex-wrap: wrap;
  gap: var(--sp-2);
}

.chip {
  display: inline-flex;
  align-items: center;
  gap: 5px;
  padding: 5px 12px;
  border-radius: var(--radius-full);
  border: 1px solid var(--border-color);
  background: var(--bg-card);
  font-size: var(--text-xs);
  color: var(--text-secondary);
  cursor: pointer;
  transition: all var(--transition);
  white-space: nowrap;
}
.chip:hover {
  border-color: var(--accent-border);
  color: var(--text-primary);
}
.chip.active {
  border-color: var(--chip-color, var(--accent));
  color: var(--chip-color, var(--accent));
  background: var(--accent-light);
}

.tag-chip.active {
  background: color-mix(in srgb, var(--chip-color, var(--accent)) 12%, transparent);
}

.tag-dot {
  width: 7px;
  height: 7px;
  border-radius: 50%;
  display: inline-block;
  flex-shrink: 0;
}

.chip-empty {
  font-size: var(--text-xs);
  color: var(--text-placeholder);
}

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
  margin: var(--sp-2) 0 0;
  font-size: var(--text-xs);
  color: var(--text-tertiary);
}

/* ---- 描述 ---- */
.content-section {
  margin-bottom: var(--sp-2);
}

.content-editor {
  width: 100%;
  min-height: 180px;
  border: none;
  outline: none;
  resize: vertical;
  font-family: var(--font-sans);
  font-size: var(--text-base);
  line-height: var(--leading-relaxed);
  color: var(--text-primary);
  background: transparent;
  padding: 0;
}
.content-editor::placeholder {
  color: var(--text-placeholder);
}
</style>
