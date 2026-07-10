<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { createReading, updateReading, getReadingById } from '@/api/readingApi'
import { ElMessage } from 'element-plus'
import { ArrowLeft, Star } from 'lucide-vue-next'

const route = useRoute()
const router = useRouter()
const isEdit = !!route.params.id
const form = ref({ bookTitle: '', author: '', coverUrl: '', totalChapters: 0, currentChapter: 0, progress: 0, rating: undefined as number | undefined, totalDuration: undefined as number | undefined, status: 0, notes: '', startDate: null as string | null, endDate: null as string | null })
const saving = ref(false)

const statusOptions = [
  { value: 0, label: '未读' }, { value: 1, label: '在读' }, { value: 2, label: '读完' }
]

onMounted(async () => {
  if (isEdit) {
    const r = (await getReadingById(Number(route.params.id))).data.data
    form.value = { bookTitle: r.bookTitle, author: r.author || '', coverUrl: r.coverUrl || '', totalChapters: r.totalChapters, currentChapter: r.currentChapter, progress: r.progress, rating: r.rating, totalDuration: r.totalDuration, status: r.status, notes: r.notes || '', startDate: r.startDate, endDate: r.endDate }
  }
})

async function handleSave() {
  if (!form.value.bookTitle.trim()) { ElMessage.warning('请输入书名'); return }
  saving.value = true
  try {
    if (isEdit) { await updateReading(Number(route.params.id), form.value); ElMessage.success('已更新') }
    else { await createReading(form.value); ElMessage.success('已创建') }
    router.push('/readings')
  } finally { saving.value = false }
}

function onProgressChange(val: number) {
  form.value.progress = val
  if (val >= 100) form.value.status = 2
  else if (val > 0 && form.value.status === 0) form.value.status = 1
}
</script>

<template>
  <div class="form-page">
    <div class="form-topbar">
      <button class="icon-btn" @click="router.push('/readings')"><ArrowLeft :size="16" /> 返回</button>
      <h2>{{ isEdit ? '编辑阅读记录' : '添加书籍' }}</h2>
    </div>
    <div class="form-card">
      <el-form label-position="top" style="max-width:560px">
        <el-form-item label="书名" required>
          <el-input v-model="form.bookTitle" placeholder="例如：深入理解Java虚拟机" maxlength="255" show-word-limit />
        </el-form-item>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="作者">
              <el-input v-model="form.author" placeholder="作者（可选）" maxlength="200" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="封面图">
              <el-input v-model="form.coverUrl" placeholder="封面图片URL（可选）" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="总章节">
              <el-input-number v-model="form.totalChapters" :min="0" style="width:100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="当前章节">
              <el-input-number v-model="form.currentChapter" :min="0" :max="form.totalChapters || 9999" style="width:100%" />
            </el-form-item>
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
            <el-form-item label="阅读时长（分钟）">
              <el-input-number v-model="form.totalDuration" :min="0" :step="10" style="width:100%" placeholder="总阅读时长" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="开始日期">
              <el-date-picker v-model="form.startDate" type="date" value-format="YYYY-MM-DD" placeholder="开始日期" style="width:100%" clearable />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="读完日期">
              <el-date-picker v-model="form.endDate" type="date" value-format="YYYY-MM-DD" placeholder="读完日期" style="width:100%" clearable />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="阅读笔记">
          <el-input v-model="form.notes" type="textarea" :rows="5" placeholder="记录读后感、摘抄等" />
        </el-form-item>
        <el-form-item>
          <el-button @click="router.push('/readings')">取消</el-button>
          <el-button type="primary" :loading="saving" @click="handleSave">保存</el-button>
        </el-form-item>
      </el-form>
    </div>
  </div>
</template>

<style scoped>
.form-page { max-width:700px }
.form-topbar { display:flex;align-items:center;gap:var(--sp-4);margin-bottom:var(--sp-6) }
.form-topbar h2 { font-size:var(--text-xl);font-weight:600 }
.icon-btn { display:flex;align-items:center;gap:var(--sp-1);background:none;border:none;color:var(--text-secondary);font-size:var(--text-sm);cursor:pointer;padding:4px 8px;border-radius:var(--radius-sm);transition:all var(--transition) }
.icon-btn:hover { background:var(--bg-hover);color:var(--text-primary) }
.form-card { background:var(--bg-card);border:1px solid var(--border-color);border-radius:var(--radius-lg);padding:var(--sp-6) }
.progress-label { font-size:var(--text-sm);font-weight:600;margin-left:var(--sp-2) }
.star-rating { display:flex;align-items:center;gap:2px; }
.star-btn { background:none;border:none;cursor:pointer;padding:2px;transition:transform var(--transition); }
.star-btn:hover { transform:scale(1.2); }
.star-btn.active { transform:scale(1.05); }
.star-hint { font-size:var(--text-xs);color:var(--text-tertiary);margin-left:var(--sp-2); }
</style>
