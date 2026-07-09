<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { createStudyRecord, updateStudyRecord, getStudyRecordById } from '@/api/studyApi'
import { ElMessage } from 'element-plus'

const route = useRoute()
const router = useRouter()
const isEdit = !!route.params.id

const form = ref({
  subject: '',
  date: new Date().toISOString().slice(0, 10),
  duration: 60,
  content: '',
  reflection: ''
})
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
    <h3>{{ isEdit ? '编辑学习记录' : '新建学习记录' }}</h3>
    <el-form label-width="100px" style="max-width:600px">
      <el-form-item label="学习主题" required>
        <el-input v-model="form.subject" placeholder="例如：Spring Boot 基础" />
      </el-form-item>
      <el-form-item label="学习日期" required>
        <el-date-picker v-model="form.date" type="date" value-format="YYYY-MM-DD" style="width:100%" />
      </el-form-item>
      <el-form-item label="学习时长" required>
        <el-input-number v-model="form.duration" :min="1" :max="1440" /> 分钟
      </el-form-item>
      <el-form-item label="学习内容">
        <el-input v-model="form.content" type="textarea" :rows="4" />
      </el-form-item>
      <el-form-item label="学习心得">
        <el-input v-model="form.reflection" type="textarea" :rows="4" />
      </el-form-item>
      <el-form-item>
        <el-button @click="router.push('/study-records')">取消</el-button>
        <el-button type="primary" :loading="saving" @click="handleSave">保存</el-button>
      </el-form-item>
    </el-form>
  </div>
</template>

<style scoped>
.form-page { max-width: 640px; }
.form-page h3 { margin-bottom: 20px; }
</style>
