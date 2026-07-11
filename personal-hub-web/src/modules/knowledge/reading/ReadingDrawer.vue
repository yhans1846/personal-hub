<script setup lang="ts">
import { ref, watch } from 'vue'
import { createReading, updateReading, getReadingById } from '@/api/readingApi'
import { ElMessage } from 'element-plus'
import { Star } from 'lucide-vue-next'
import { UiInput, UiTextarea, UiDatePicker, UiButton, UiSection } from '@/components/ui'

const props = withDefaults(defineProps<{
  modelValue: boolean
  entityId?: number
}>(), { entityId: undefined })

const emit = defineEmits<{
  'update:modelValue': [value: boolean]
  'saved': []
}>()

const form = ref({
  bookTitle: '', author: '', coverUrl: '', totalChapters: 0, currentChapter: 0,
  progress: 0, rating: undefined as number | undefined, totalDuration: undefined as number | undefined,
  status: 0, notes: '', startDate: null as string | null, endDate: null as string | null
})
const saving = ref(false)

const statusOptions = [
  { value: 0, label: '未读' }, { value: 1, label: '在读' }, { value: 2, label: '读完' }
]

watch(() => props.modelValue, async (val) => {
  if (!val) return
  if (props.entityId) {
    const r = (await getReadingById(props.entityId)).data.data
    form.value = {
      bookTitle: r.bookTitle, author: r.author || '', coverUrl: r.coverUrl || '',
      totalChapters: r.totalChapters, currentChapter: r.currentChapter, progress: r.progress,
      rating: r.rating, totalDuration: r.totalDuration, status: r.status,
      notes: r.notes || '', startDate: r.startDate, endDate: r.endDate
    }
  } else {
    form.value = {
      bookTitle: '', author: '', coverUrl: '', totalChapters: 0, currentChapter: 0,
      progress: 0, rating: undefined, totalDuration: undefined, status: 0,
      notes: '', startDate: null, endDate: null
    }
  }
})

async function handleSave() {
  if (!form.value.bookTitle.trim()) { ElMessage.warning('请输入书名'); return }
  saving.value = true
  try {
    if (props.entityId) {
      await updateReading(props.entityId, form.value)
      ElMessage.success('已更新')
    } else {
      await createReading(form.value)
      ElMessage.success('已创建')
    }
    emit('update:modelValue', false)
    emit('saved')
  } finally { saving.value = false }
}

function onProgressChange(val: number) {
  form.value.progress = val
  if (val >= 100) form.value.status = 2
  else if (val > 0 && form.value.status === 0) form.value.status = 1
}
</script>

<template>
  <el-drawer
    :model-value="modelValue"
    :title="entityId ? '编辑阅读记录' : '添加书籍'"
    :size="560"
    @update:model-value="emit('update:modelValue', $event)"
  >
    <el-form label-position="top">
      <UiSection title="基本信息">
        <el-form-item label="书名" required>
          <UiInput v-model="form.bookTitle" placeholder="例如：深入理解Java虚拟机" maxlength="255" show-word-limit />
        </el-form-item>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="作者"><UiInput v-model="form.author" placeholder="作者（可选）" maxlength="200" /></el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="封面图"><UiInput v-model="form.coverUrl" placeholder="封面图片URL（可选）" /></el-form-item>
          </el-col>
        </el-row>
      </UiSection>

      <UiSection title="阅读进度">
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="总章节"><el-input-number v-model="form.totalChapters" :min="0" style="width:100%" /></el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="当前章节"><el-input-number v-model="form.currentChapter" :min="0" :max="form.totalChapters || 9999" style="width:100%" /></el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="阅读状态">
          <el-radio-group v-model="form.status">
            <el-radio v-for="s in statusOptions" :key="s.value" :value="s.value">{{ s.label }}</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="阅读进度">
          <el-slider v-model="form.progress" :min="0" :max="100" @change="onProgressChange">
            <span class="progress-label">{{ form.progress }}%</span>
          </el-slider>
        </el-form-item>
      </UiSection>

      <UiSection title="评分与时长">
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="评分">
              <div class="star-rating">
                <button v-for="i in 5" :key="i" class="star-btn" :class="{ active: i <= (form.rating || 0) }" @click="form.rating = i">
                  <Star :size="18" :fill="i <= (form.rating || 0) ? 'var(--warning)' : 'none'" :color="i <= (form.rating || 0) ? 'var(--warning)' : 'var(--text-tertiary)'" />
                </button>
                <span v-if="!form.rating" class="star-hint">点击评分</span>
              </div>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="阅读时长（分钟）"><el-input-number v-model="form.totalDuration" :min="0" :step="10" style="width:100%" /></el-form-item>
          </el-col>
        </el-row>
      </UiSection>

      <UiSection title="时间">
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="开始日期"><UiDatePicker v-model="form.startDate" type="date" value-format="YYYY-MM-DD" placeholder="开始日期" style="width:100%" clearable /></el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="读完日期"><UiDatePicker v-model="form.endDate" type="date" value-format="YYYY-MM-DD" placeholder="读完日期" style="width:100%" clearable /></el-form-item>
          </el-col>
        </el-row>
      </UiSection>

      <UiSection title="笔记">
        <el-form-item label="阅读笔记">
          <UiTextarea v-model="form.notes" placeholder="记录读后感、摘抄等" />
        </el-form-item>
      </UiSection>
    </el-form>

    <template #footer>
      <el-button @click="emit('update:modelValue', false)">取消</el-button>
      <UiButton type="primary" :loading="saving" @click="handleSave">保存</UiButton>
    </template>
  </el-drawer>
</template>

<style scoped>
.progress-label { font-size: var(--text-sm); font-weight: 600; margin-left: var(--sp-2); }
.star-rating { display: flex; align-items: center; gap: 2px; }
.star-btn { background: none; border: none; cursor: pointer; padding: 2px; transition: transform var(--transition); }
.star-btn:hover { transform: scale(1.2); }
.star-btn.active { transform: scale(1.05); }
.star-hint { font-size: var(--text-xs); color: var(--text-tertiary); margin-left: var(--sp-2); }
</style>
