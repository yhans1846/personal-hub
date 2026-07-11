<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { createDiary, updateDiary, getDiaryById } from '@/api/diaryApi'
import { uploadFile } from '@/api/fileApi'
import { ElMessage } from 'element-plus'
import { ArrowLeft, Smile, Meh, Frown, Upload, X } from 'lucide-vue-next'

const route = useRoute()
const router = useRouter()
const isEdit = !!route.params.id

const form = ref({ date: '', title: '', content: '', mood: 2, weather: '', location: '', imageFileId: undefined as number | undefined })
const saving = ref(false)

onMounted(async () => {
  if (!isEdit) {
    // 优先使用查询参数中的日期（来自日历视图），否则默认今天
    const queryDate = route.query.date as string
    if (queryDate && /^\d{4}-\d{2}-\d{2}$/.test(queryDate)) {
      form.value.date = queryDate
    } else {
      const now = new Date()
      form.value.date = `${now.getFullYear()}-${String(now.getMonth() + 1).padStart(2, '0')}-${String(now.getDate()).padStart(2, '0')}`
    }
    return
  }
  const res = await getDiaryById(Number(route.params.id))
  const r = res.data.data
  form.value.date = r.date
  form.value.title = r.title || ''
  form.value.content = r.content || ''
  form.value.mood = r.mood || 2
  form.value.weather = r.weather || ''
  form.value.location = r.location || ''
  form.value.imageFileId = r.imageFileId
})

async function handleSave() {
  saving.value = true
  try {
    if (isEdit) {
      await updateDiary(Number(route.params.id), form.value)
      ElMessage.success('已更新')
    } else {
      await createDiary(form.value)
      ElMessage.success('已创建')
    }
    router.push('/diaries')
  } finally { saving.value = false }
}

const moodOptions = [
  { value: 1, label: '很好', icon: Smile, color: 'var(--success)' },
  { value: 2, label: '好', icon: Smile, color: 'var(--accent)' },
  { value: 3, label: '一般', icon: Meh, color: 'var(--text-secondary)' },
  { value: 4, label: '不好', icon: Frown, color: 'var(--warning)' },
  { value: 5, label: '很差', icon: Frown, color: 'var(--danger)' }
]

const weatherOptions = ['晴', '多云', '阴', '小雨', '大雨', '雷阵雨', '雪', '雾']

const uploading = ref(false)
const previewUrl = ref('')
const fileInput = ref<HTMLInputElement | null>(null)

async function handleImageUpload(file: File) {
  uploading.value = true
  try {
    const res = await uploadFile(file)
    form.value.imageFileId = res.data.data.id
  } finally { uploading.value = false }
}

async function onFileChange(e: Event) {
  const file = (e.target as HTMLInputElement).files?.[0]
  if (file) await handleImageUpload(file)
}

function removeImage() { form.value.imageFileId = undefined }
</script>

<template>
  <div class="form-page">
    <div class="form-topbar">
      <button class="icon-btn" @click="router.push('/diaries')">
        <ArrowLeft :size="16" /> 返回
      </button>
      <h2>{{ isEdit ? '编辑日记' : '写日记' }}</h2>
    </div>

    <div class="form-card">
      <el-form label-position="top" style="max-width: 640px">
        <el-form-item label="日期">
          <el-date-picker v-model="form.date" type="date" value-format="YYYY-MM-DD" placeholder="选择日期" style="width:100%" />
        </el-form-item>

        <el-form-item label="标题">
          <el-input v-model="form.title" placeholder="日记标题（可选）" maxlength="200" show-word-limit />
        </el-form-item>

        <el-form-item label="心情">
          <el-radio-group v-model="form.mood">
            <el-radio v-for="item in moodOptions" :key="item.value" :value="item.value" class="mood-radio">
              <component :is="item.icon" :size="16" :color="item.color" />
              <span :style="{ color: item.color }">{{ item.label }}</span>
            </el-radio>
          </el-radio-group>
        </el-form-item>

        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="天气">
              <el-select v-model="form.weather" placeholder="选择天气" clearable style="width:100%">
                <el-option v-for="w in weatherOptions" :key="w" :value="w" :label="w" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="地点">
              <el-input v-model="form.location" placeholder="记录地点（可选）" maxlength="200" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-form-item label="配图">
          <div v-if="form.imageFileId" class="image-preview">
            <img :src="`/api/files/${form.imageFileId}/download`" class="preview-img" />
            <button class="image-remove" @click="removeImage"><X :size="14" /></button>
          </div>
          <div v-else>
            <input type="file" accept="image/*" @change="onFileChange" class="file-input" />
          </div>
        </el-form-item>

        <el-form-item label="内容">
          <el-input v-model="form.content" type="textarea" :rows="12" placeholder="开始写吧...（支持 Markdown 格式）" />
          <div class="form-hint">支持 Markdown 格式：标题、列表、加粗、代码等</div>
        </el-form-item>

        <el-form-item>
          <el-button @click="router.push('/diaries')">取消</el-button>
          <el-button type="primary" :loading="saving" @click="handleSave">保存</el-button>
        </el-form-item>
      </el-form>
    </div>
  </div>
</template>

<style scoped>
.form-page { max-width: 700px; }
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
.mood-radio { display: flex; align-items: center; gap: 4px; }
.mood-radio :deep(.el-radio__label) { display: flex; align-items: center; gap: 4px; }
.file-input { font-size: var(--text-sm); color: var(--text-secondary); }
.image-preview { position: relative; display: inline-block; }
.preview-img { max-width: 200px; max-height: 150px; border-radius: var(--radius-sm); object-fit: cover; }
.image-remove { position: absolute; top: 4px; right: 4px; width: 24px; height: 24px; border-radius: 50%; background: rgba(0,0,0,0.5); color: #fff; border: none; cursor: pointer; display: flex; align-items: center; justify-content: center; }
</style>
