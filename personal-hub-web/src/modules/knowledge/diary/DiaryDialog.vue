<script setup lang="ts">
import { ref, watch } from 'vue'
import { createDiary, updateDiary, getDiaryById } from '@/api/diaryApi'
import { uploadFile } from '@/api/fileApi'
import { ElMessage } from 'element-plus'
import { Smile, Meh, Frown, X } from 'lucide-vue-next'
import { UiDialog, UiInput, UiTextarea, UiSelect, UiDatePicker, UiButton, UiSection } from '@/components/ui'

const props = withDefaults(defineProps<{
  modelValue: boolean
  entityId?: number
  initialDate?: string
}>(), { entityId: undefined, initialDate: '' })

const emit = defineEmits<{
  'update:modelValue': [value: boolean]
  'saved': []
}>()

const form = ref({ date: '', title: '', content: '', mood: 2, weather: '', location: '', imageFileId: undefined as number | undefined })
const saving = ref(false)

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

watch(() => props.modelValue, async (val) => {
  if (!val) return

  if (props.entityId) {
    const res = await getDiaryById(props.entityId)
    const r = res.data.data
    form.value = {
      date: r.date, title: r.title || '', content: r.content || '',
      mood: r.mood || 2, weather: r.weather || '', location: r.location || '',
      imageFileId: r.imageFileId
    }
  } else {
    const now = new Date()
    const today = `${now.getFullYear()}-${String(now.getMonth() + 1).padStart(2, '0')}-${String(now.getDate()).padStart(2, '0')}`
    form.value = {
      date: props.initialDate || today,
      title: '', content: '', mood: 2, weather: '', location: '', imageFileId: undefined
    }
  }
})

async function handleSave() {
  saving.value = true
  try {
    if (props.entityId) {
      await updateDiary(props.entityId, form.value)
      ElMessage.success('已更新')
    } else {
      await createDiary(form.value)
      ElMessage.success('已创建')
    }
    emit('update:modelValue', false)
    emit('saved')
  } finally { saving.value = false }
}

async function handleImageUpload(file: File) {
  uploading.value = true
  try {
    const res = await uploadFile(file)
    form.value.imageFileId = res.data.data.id
  } finally { uploading.value = false }
}

function onFileChange(e: Event) {
  const file = (e.target as HTMLInputElement).files?.[0]
  if (file) handleImageUpload(file)
}

function removeImage() { form.value.imageFileId = undefined }
</script>

<template>
  <UiDialog
    :model-value="modelValue"
    :title="entityId ? '编辑日记' : '写日记'"
    width="600"
    @update:model-value="emit('update:modelValue', $event)"
  >
    <el-form label-position="top">
      <UiSection title="日记信息">
        <el-form-item label="日期">
          <UiDatePicker v-model="form.date" type="date" value-format="YYYY-MM-DD" placeholder="选择日期" style="width:100%" />
        </el-form-item>
        <el-form-item label="标题">
          <UiInput v-model="form.title" placeholder="日记标题（可选）" maxlength="200" show-word-limit />
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
              <UiSelect v-model="form.weather" placeholder="选择天气" clearable>
                <el-option v-for="w in weatherOptions" :key="w" :value="w" :label="w" />
              </UiSelect>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="地点">
              <UiInput v-model="form.location" placeholder="记录地点（可选）" maxlength="200" />
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
      </UiSection>

      <UiSection title="正文">
        <el-form-item label="内容">
          <UiTextarea v-model="form.content" placeholder="开始写吧...（支持 Markdown 格式）" />
          <div class="form-hint">支持 Markdown 格式：标题、列表、加粗、代码等</div>
        </el-form-item>
      </UiSection>
    </el-form>

    <template #footer>
      <el-button @click="emit('update:modelValue', false)">取消</el-button>
      <UiButton type="primary" :loading="saving" @click="handleSave">保存</UiButton>
    </template>
  </UiDialog>
</template>

<style scoped>
.form-hint { font-size: var(--text-xs); color: var(--text-tertiary); margin-top: 4px; }
.mood-radio { display: flex; align-items: center; gap: 4px; }
.mood-radio :deep(.el-radio__label) { display: flex; align-items: center; gap: 4px; }
.file-input { font-size: var(--text-sm); color: var(--text-secondary); }
.image-preview { position: relative; display: inline-block; }
.preview-img { max-width: 200px; max-height: 150px; border-radius: var(--radius-sm); object-fit: cover; }
.image-remove { position: absolute; top: 4px; right: 4px; width: 24px; height: 24px; border-radius: 50%; background: rgba(0,0,0,0.5); color: #fff; border: none; cursor: pointer; display: flex; align-items: center; justify-content: center; }
</style>
