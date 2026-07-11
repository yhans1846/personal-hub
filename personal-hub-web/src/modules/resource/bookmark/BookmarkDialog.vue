<script setup lang="ts">
import { ref, watch } from 'vue'
import { createBookmark, updateBookmark, getBookmarkById, getBookmarkCategories } from '@/api/bookmarkApi'
import { getTags } from '@/api/tagApi'
import { ElMessage } from 'element-plus'
import { UiDialog, UiInput, UiTextarea, UiSelect, UiButton } from '@/components/ui'
import { FolderOpen } from 'lucide-vue-next'
import type { BookmarkCategoryVO } from '@/types/bookmark'
import type { TagVO } from '@/types/tag'

const props = withDefaults(defineProps<{
  modelValue: boolean
  entityId?: number
}>(), { entityId: undefined })

const emit = defineEmits<{
  'update:modelValue': [value: boolean]
  'saved': []
}>()

const form = ref({ title: '', url: '', description: '', categoryId: null as number | null, tagIds: [] as number[] })
const categories = ref<BookmarkCategoryVO[]>([])
const tags = ref<TagVO[]>([])
const saving = ref(false)

watch(() => props.modelValue, async (val) => {
  if (!val) return
  try {
    const [catRes, tagRes] = await Promise.all([getBookmarkCategories(), getTags()])
    categories.value = catRes.data.data
    tags.value = tagRes.data.data
  } catch { /* ignore */ }

  if (props.entityId) {
    const res = await getBookmarkById(props.entityId)
    const r = res.data.data
    form.value = {
      title: r.title, url: r.url, description: r.description || '',
      categoryId: r.categoryId, tagIds: (r.tags || []).map((t: TagVO) => t.id)
    }
  } else {
    form.value = { title: '', url: '', description: '', categoryId: null, tagIds: [] }
  }
})

async function handleSave() {
  if (!form.value.title.trim()) { ElMessage.warning('请输入标题'); return }
  if (!form.value.url.trim()) { ElMessage.warning('请输入网址'); return }
  let url = form.value.url.trim()
  if (!/^https?:\/\//i.test(url)) url = 'https://' + url
  saving.value = true
  try {
    if (props.entityId) {
      await updateBookmark(props.entityId, { ...form.value, url })
      ElMessage.success('已更新')
    } else {
      await createBookmark({ ...form.value, url })
      ElMessage.success('已创建')
    }
    emit('update:modelValue', false)
    emit('saved')
  } finally { saving.value = false }
}
</script>

<template>
  <UiDialog
    :model-value="modelValue"
    :title="entityId ? '编辑收藏' : '新建收藏'"
    @update:model-value="emit('update:modelValue', $event)"
  >
    <el-form label-position="top">
      <el-form-item label="标题" required>
        <UiInput v-model="form.title" placeholder="例如：GitHub" maxlength="255" show-word-limit />
      </el-form-item>
      <el-form-item label="网址" required>
        <UiInput v-model="form.url" placeholder="例如：https://github.com" maxlength="2048" />
      </el-form-item>
      <el-form-item label="描述">
        <UiTextarea v-model="form.description" placeholder="简要描述（可选）" />
      </el-form-item>
      <el-form-item label="分类">
        <el-select v-model="form.categoryId" placeholder="选择分类" clearable style="width:100%">
          <el-option v-for="c in categories" :key="c.id" :value="c.id" :label="c.name">
            <span><FolderOpen :size="14" /> {{ c.name }}</span>
          </el-option>
        </el-select>
      </el-form-item>
      <el-form-item label="标签">
        <el-select v-model="form.tagIds" multiple placeholder="选择标签" style="width:100%" clearable>
          <el-option v-for="t in tags" :key="t.id" :value="t.id" :label="t.name">
            <span class="tag-option"><span class="tag-dot" :style="{ background: t.color }" /> {{ t.name }}</span>
          </el-option>
        </el-select>
        <div class="form-hint">选择已有标签，可在标签管理页面创建新标签</div>
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="emit('update:modelValue', false)">取消</el-button>
      <el-button type="primary" :loading="saving" @click="handleSave">保存</el-button>
    </template>
  </UiDialog>
</template>

<style scoped>
.tag-option { display: flex; align-items: center; gap: 6px; }
.tag-dot { width: 8px; height: 8px; border-radius: 50%; display: inline-block; }
.form-hint { font-size: var(--text-xs); color: var(--text-tertiary); margin-top: 4px; }
</style>
