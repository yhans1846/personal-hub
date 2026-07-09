<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { createNote, updateNote, getNoteById, getCategories, getTags } from '@/api/noteApi'
import { ElMessage } from 'element-plus'

const route = useRoute()
const router = useRouter()
const isEdit = !!route.params.id

const form = ref({ title: '', content: '', categoryIds: [] as number[], tagIds: [] as number[] })
const categories = ref<any[]>([])
const tags = ref<any[]>([])
const saving = ref(false)

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
  }
})

async function handleSave() {
  if (!form.value.title.trim()) {
    ElMessage.warning('请输入标题')
    return
  }
  saving.value = true
  try {
    if (isEdit) {
      await updateNote(Number(route.params.id), form.value)
      ElMessage.success('笔记已更新')
    } else {
      await createNote(form.value)
      ElMessage.success('笔记已创建')
    }
    router.push('/notes')
  } finally {
    saving.value = false
  }
}

function goBack() { router.push('/notes') }
</script>

<template>
  <div class="note-editor">
    <div class="toolbar">
      <el-button @click="goBack">返回</el-button>
      <el-button type="primary" :loading="saving" @click="handleSave">保存</el-button>
    </div>
    <el-input v-model="form.title" placeholder="笔记标题" size="large" style="margin-bottom:16px" />
    <el-row :gutter="16" style="margin-bottom:16px">
      <el-col :span="12">
        <el-select v-model="form.categoryIds" multiple placeholder="选择分类" style="width:100%">
          <el-option v-for="c in categories" :key="c.id" :label="c.name" :value="c.id" />
        </el-select>
      </el-col>
      <el-col :span="12">
        <el-select v-model="form.tagIds" multiple placeholder="选择标签" style="width:100%">
          <el-option v-for="t in tags" :key="t.id" :label="t.name" :value="t.id" />
        </el-select>
      </el-col>
    </el-row>
    <el-input
      v-model="form.content"
      type="textarea"
      :rows="20"
      placeholder="Markdown 内容..."
    />
  </div>
</template>

<style scoped>
.toolbar {
  display: flex;
  justify-content: space-between;
  margin-bottom: 16px;
}
</style>
