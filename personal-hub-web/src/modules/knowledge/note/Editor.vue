<script setup lang="ts">
import { ref, computed, onMounted, watch, onUnmounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { createNote, updateNote, getNoteById, getCategories } from '@/api/noteApi'
import { getTags } from '@/api/tagApi'
import { ElMessage } from 'element-plus'
import { ArrowLeft, Download } from 'lucide-vue-next'
import { MdEditor } from 'md-editor-v3'
import 'md-editor-v3/lib/style.css'
import { UiButton } from '@/components/ui'

const route = useRoute()
const router = useRouter()
const isEdit = !!route.params.id

const form = ref({ title: '', content: '', categoryIds: [] as number[], tagIds: [] as number[] })
const categories = ref<any[]>([])
const tags = ref<any[]>([])
const saving = ref(false)
const draftKey = `draft_note_${route.params.id || 'new'}`

const editorTheme = computed(() =>
  document.documentElement.getAttribute('data-theme') === 'dark' ? 'dark' : 'light'
)

// 自动保存草稿
let draftTimer: ReturnType<typeof setTimeout> | null = null
watch(() => [form.value.title, form.value.content], () => {
  if (draftTimer) clearTimeout(draftTimer)
  draftTimer = setTimeout(() => {
    const data = { title: form.value.title, content: form.value.content, categoryIds: form.value.categoryIds, tagIds: form.value.tagIds }
    localStorage.setItem(draftKey, JSON.stringify(data))
  }, 30000) // 30 秒无操作后保存
}, { deep: true })
onUnmounted(() => { if (draftTimer) clearTimeout(draftTimer) })

function restoreDraft() {
  const raw = localStorage.getItem(draftKey)
  if (!raw) return false
  try {
    const data = JSON.parse(raw)
    if (data.title || data.content) {
      form.value.title = data.title || ''
      form.value.content = data.content || ''
      form.value.categoryIds = data.categoryIds || []
      form.value.tagIds = data.tagIds || []
      return true
    }
  } catch { /* ignore */ }
  return false
}

function clearDraft() {
  localStorage.removeItem(draftKey)
}

onMounted(async () => {
  const [catRes, tagRes] = await Promise.all([getCategories(), getTags()])
  categories.value = catRes.data.data
  tags.value = tagRes.data.data

  if (isEdit) {
    const res = await getNoteById(Number(route.params.id))
    const note = res.data.data
    form.value.title = note.title
    form.value.content = note.content
    form.value.categoryIds = note.categories.map((c: any) => c.id)
    form.value.tagIds = note.tags.map((t: any) => t.id)
  } else if (restoreDraft()) {
    ElMessage.info('已恢复未保存的草稿')
  }
})

async function handleSave() {
  if (!form.value.title.trim()) { ElMessage.warning('请输入标题'); return }
  saving.value = true
  try {
    if (isEdit) {
      await updateNote(Number(route.params.id), form.value)
      ElMessage.success('笔记已更新')
    } else {
      await createNote(form.value)
      ElMessage.success('笔记已创建')
    }
    clearDraft()
    router.push('/notes')
  } finally {
    saving.value = false
  }
}

function handleExport() {
  const title = form.value.title || '无标题笔记'
  const content = `# ${title}\n\n${form.value.content || ''}`
  const blob = new Blob([content], { type: 'text/markdown;charset=utf-8' })
  const url = URL.createObjectURL(blob)
  const a = document.createElement('a')
  a.href = url
  a.download = `${title}.md`
  a.click()
  URL.revokeObjectURL(url)
}
</script>

<template>
  <div class="editor-page">
    <!-- 顶部 Meta 栏 -->
    <div class="editor-topbar">
      <button class="icon-btn" @click="router.push('/notes')">
        <ArrowLeft :size="16" /> 返回
      </button>
      <div class="editor-topbar-right">
        <el-select v-model="form.categoryIds" multiple placeholder="分类" size="small" style="width:140px">
          <el-option v-for="c in categories" :key="c.id" :label="c.name" :value="c.id" />
        </el-select>
        <el-select v-model="form.tagIds" multiple placeholder="标签" size="small" style="width:140px">
          <el-option v-for="t in tags" :key="t.id" :label="t.name" :value="t.id" />
        </el-select>
        <el-tooltip content="导出 Markdown" placement="top">
          <button v-if="form.content" class="icon-btn" @click="handleExport"><Download :size="14" /></button>
        </el-tooltip>
        <UiButton :loading="saving" type="primary" size="small" @click="handleSave">
          {{ isEdit ? '保存更新' : '发布' }}
        </UiButton>
      </div>
    </div>

    <!-- 编辑器 -->
    <div class="editor-body">
      <input v-model="form.title" class="editor-title" placeholder="无标题笔记" />
      <MdEditor
        v-model="form.content"
        :theme="editorTheme"
        :toolbars="['bold', 'italic', 'heading', 'strikeThrough', '|', 'quote', 'unorderedList', 'orderedList', '|', 'code', 'codeBlock', 'link', 'image', 'table', '|', 'preview', 'catalog']"
        language="zh-CN"
        placeholder="开始写作..."
        style="min-height: 60vh; border-radius: var(--radius-lg);"
      />
    </div>
  </div>
</template>

<style scoped>
.editor-page { max-width: var(--reading-max-width); margin: 0 auto; }
.editor-topbar {
  display: flex; justify-content: space-between; align-items: center;
  margin-bottom: var(--sp-6); padding-bottom: var(--sp-4); border-bottom: 1px solid var(--border-color);
}
.icon-btn {
  display: flex; align-items: center; gap: var(--sp-1);
  background: none; border: none; color: var(--text-secondary); font-size: var(--text-sm);
  cursor: pointer; padding: 4px 8px; border-radius: var(--radius-sm); transition: all var(--transition);
}
.icon-btn:hover { background: var(--bg-hover); color: var(--text-primary); }
.editor-topbar-right { display: flex; align-items: center; gap: var(--sp-2); }
.editor-body { padding: 0; }
.editor-title {
  width: 100%; border: none; outline: none;
  font-size: var(--text-3xl); font-weight: 700; color: var(--text-primary);
  background: transparent; padding: 0; margin-bottom: var(--sp-6); font-family: var(--font-sans);
}
.editor-title::placeholder { color: var(--text-placeholder); }
</style>
