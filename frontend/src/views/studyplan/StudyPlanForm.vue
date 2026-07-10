<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { createStudyPlan, updateStudyPlan, getStudyPlanById } from '@/api/studyplanApi'
import { ElMessage } from 'element-plus'
import { ArrowLeft } from 'lucide-vue-next'

const route = useRoute()
const router = useRouter()
const isEdit = !!route.params.id

const form = ref({
  name: '',
  goal: '',
  progress: 0,
  startDate: null as string | null,
  endDate: null as string | null,
  status: 0
})
const saving = ref(false)
const statusOptions = [
  { value: 0, label: '未开始' },
  { value: 1, label: '进行中' },
  { value: 2, label: '已完成' },
  { value: 3, label: '已放弃' }
]

onMounted(async () => {
  if (isEdit) {
    const res = await getStudyPlanById(Number(route.params.id))
    const r = res.data.data
    form.value.name = r.name
    form.value.goal = r.goal || ''
    form.value.progress = r.progress
    form.value.startDate = r.startDate
    form.value.endDate = r.endDate
    form.value.status = r.status
  }
})

async function handleSave() {
  if (!form.value.name.trim()) { ElMessage.warning('请输入计划名称'); return }
  saving.value = true
  try {
    if (isEdit) {
      await updateStudyPlan(Number(route.params.id), form.value)
      ElMessage.success('已更新')
    } else {
      await createStudyPlan(form.value)
      ElMessage.success('已创建')
    }
    router.push('/study-plans')
  } finally { saving.value = false }
}

const progressColor = computed(() => {
  if (form.value.progress >= 100) return 'var(--success)'
  if (form.value.progress >= 50) return 'var(--accent)'
  return 'var(--warning)'
})
</script>

<template>
  <div class="form-page">
    <div class="form-topbar">
      <button class="icon-btn" @click="router.push('/study-plans')">
        <ArrowLeft :size="16" /> 返回
      </button>
      <h2>{{ isEdit ? '编辑计划' : '新建计划' }}</h2>
    </div>

    <div class="form-card">
      <el-form label-position="top" style="max-width: 560px">
        <el-form-item label="计划名称" required>
          <el-input v-model="form.name" placeholder="例如：Spring Boot 深入学习" maxlength="200" show-word-limit />
        </el-form-item>
        <el-form-item label="学习目标">
          <el-input v-model="form.goal" type="textarea" :rows="3" placeholder="设定学习目标（可选）" />
        </el-form-item>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="开始日期">
              <el-date-picker v-model="form.startDate" type="date" value-format="YYYY-MM-DD" placeholder="开始日期" style="width:100%" clearable />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="结束日期">
              <el-date-picker v-model="form.endDate" type="date" value-format="YYYY-MM-DD" placeholder="结束日期" style="width:100%" clearable />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="状态">
          <el-radio-group v-model="form.status">
            <el-radio v-for="s in statusOptions" :key="s.value" :value="s.value">{{ s.label }}</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="学习进度">
          <el-slider v-model="form.progress" :min="0" :max="100" :format-tooltip="(v: number) => v + '%'">
            <span class="progress-label" :style="{ color: progressColor }">{{ form.progress }}%</span>
          </el-slider>
        </el-form-item>
        <el-form-item>
          <el-button @click="router.push('/study-plans')">取消</el-button>
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
.progress-label { font-size: var(--text-sm); font-weight: 600; margin-left: var(--sp-2); }
</style>
