<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { createBookmark, updateBookmark, getBookmarkById, getBookmarkCategories } from '@/api/bookmarkApi'
import { getTags } from '@/api/tagApi'
import { ElMessage } from 'element-plus'
import { ArrowLeft, FolderOpen } from 'lucide-vue-next'
import type { BookmarkCategoryVO } from '@/types/bookmark'
import type { TagVO } from '@/types/tag'

const route = useRoute()
const router = useRouter()
const isEdit = !!route.params.id

const form = ref({ title: '', url: '', description: '', categoryId: null as number | null, tagIds: [] as number[] })
const categories = ref<BookmarkCategoryVO[]>([])
const tags = ref<TagVO[]>([])
const saving = ref(false)

onMounted(async () => {
  try {
    const [catRes, tagRes] = await Promise.all([getBookmarkCategories(), getTags()])
    categories.value = catRes.data.data
    tags.value = tagRes.data.data
  } catch { /* ignore */ }

  if (isEdit) {
    const res = await getBookmarkById(Number(route.params.id))
    const r = res.data.data
    form.value.title = r.title
    form.value.url = r.url
    form.value.description = r.description || ''
    form.value.categoryId = r.categoryId
    form.value.tagIds = (r.tags || []).map((t: TagVO) => t.id)
  }
})

async function handleSave() {
  if (!form.value.title.trim()) { ElMessage.warning('请输入标题'); return }
  if (!form.value.url.trim()) { ElMessage.warning('请输入网址'); return }
  // 自动补全 https://
  let url = form.value.url.trim()
  if (!/^https?:\/\//i.test(url)) url = 'https://' + url

  saving.value = true
  try {
    if (isEdit) {
      await updateBookmark(Number(route.params.id), { ...form.value, url })
      ElMessage.success('已更新')
    } else {
      await createBookmark({ ...form.value, url })
      ElMessage.success('已创建')
    }
    router.push('/bookmarks')
  } finally { saving.value = false }
}
</script>

<template>
  <div class="form-page">
    <div class="form-topbar">
      <button class="icon-btn" @click="router.push('/bookmarks')">
        <ArrowLeft :size="16" /> 返回
      </button>
      <h2>{{ isEdit ? '编辑收藏' : '新建收藏' }}</h2>
    </div>

    <div class="form-card">
      <el-form label-position="top" style="max-width: 560px">
        <el-form-item label="标题" required>
          <el-input v-model="form.title" placeholder="例如：GitHub" maxlength="255" show-word-limit />
        </el-form-item>
        <el-form-item label="网址" required>
          <el-input v-model="form.url" placeholder="例如：https://github.com" maxlength="2048" />
        </el-form-item>
        <el-form-item label="描述">
          <el-input v-model="form.description" type="textarea" :rows="3" placeholder="简要描述（可选）" />
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
              <span class="tag-option">
                <span class="tag-dot" :style="{ background: t.color }" />
                {{ t.name }}
              </span>
            </el-option>
          </el-select>
          <div class="form-hint">选择已有标签，可在标签管理页面创建新标签</div>
        </el-form-item>
        <el-form-item>
          <el-button @click="router.push('/bookmarks')">取消</el-button>
          <el-button type="primary" :loading="saving" @click="handleSave">保存</el-button>
        </el-form-item>
      </el-form>
    </div>
  </div>
</template>

<style scoped>
.form-page { max-width: 640px; }
.form-topbar { display: flex; align-items: center; gap: var(--sp-4); margin-bottom: var(--sp-6); }
.form-topbar h2 { font-size: var(--text-xl); font-weight: 600; }
.icon-btn {
  display: flex; align-items: center; gap: var(--sp-1);
  background: none; border: none; color: var(--text-secondary); font-size: var(--text-sm);
  cursor: pointer; padding: 4px 8px; border-radius: var(--radius-sm); transition: all var(--transition);
}
.icon-btn:hover { background: var(--bg-hover); color: var(--text-primary); }
.form-card {
  background: var(--bg-card); border: 1px solid var(--border-color);
  border-radius: var(--radius-lg); padding: var(--sp-6);
}
.form-hint { font-size: var(--text-xs); color: var(--text-tertiary); margin-top: 4px; }
.tag-option { display: flex; align-items: center; gap: 6px; }
.tag-dot { width: 8px; height: 8px; border-radius: 50%; display: inline-block; }
</style>
