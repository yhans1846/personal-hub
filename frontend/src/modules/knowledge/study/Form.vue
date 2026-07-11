<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { createStudyRecord, updateStudyRecord, getStudyRecordById } from '@/api/studyApi'
import { ElMessage } from 'element-plus'
import { ArrowLeft } from 'lucide-vue-next'

const route = useRoute()
const router = useRouter()
const isEdit = !!route.params.id

const form = ref({ subject: '', date: new Date().toISOString().slice(0, 10), duration: 60, content: '', reflection: '' })
const saving = ref(false)

onMounted(async () => {
  if (isEdit) {
    const res = await getStudyRecordById(Number(route.params.id))
    const r = res.data.data
    form.value.subject = r.subject
    form.value.date = r.date
    form.value.duration = r.duration
    form.value.content = r.content || ''
    form.value.reflection = r.reflection || ''
  }
})

async function handleSave() {
  if (!form.value.subject.trim()) { ElMessage.warning('请输入学习主题'); return }
  if (!form.value.duration || form.value.duration < 1) { ElMessage.warning('时长至少1分钟'); return }
  saving.value = true
  try {
    if (isEdit) {
      await updateStudyRecord(Number(route.params.id), form.value)
      ElMessage.success('已更新')
    } else {
      await createStudyRecord(form.value)
      ElMessage.success('已创建')
    }
    router.push('/study-records')
  } finally {
    saving.value = false
  }
}
</script>

<template>
  <div class="form-page">
    <div class="form-topbar">
      <button class="icon-btn" @click="router.push('/study-records')">
        <ArrowLeft :size="16" /> 返回
      </button>
      <h2>{{ isEdit ? '编辑学习记录' : '新建学习记录' }}</h2>
    </div>

    <div class="form-card">
      <el-form label-position="top" style="max-width: 560px">
        <el-form-item label="学习主题" required>
          <el-input v-model="form.subject" placeholder="例如：Spring Boot 基础学习" />
        </el-form-item>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="日期" required>
              <el-date-picker v-model="form.date" type="date" value-format="YYYY-MM-DD" style="width:100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="时长（分钟）" required>
              <el-input-number v-model="form.duration" :min="1" :max="1440" style="width:100%" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="学习内容">
          <el-input v-model="form.content" type="textarea" :rows="5" placeholder="记录今天学了什么..." />
        </el-form-item>
        <el-form-item label="心得">
          <el-input v-model="form.reflection" type="textarea" :rows="3" placeholder="有什么收获或思考？" />
        </el-form-item>
        <el-form-item>
          <el-button @click="router.push('/study-records')">取消</el-button>
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
</style>
