<script setup lang="ts">
import { ref, watch } from 'vue'
import { createStudyRecord, updateStudyRecord, getStudyRecordById } from '@/api/studyApi'
import { ElMessage } from 'element-plus'
import { UiInput, UiTextarea, UiDatePicker, UiButton, UiSection } from '@/components/ui'

const props = withDefaults(defineProps<{
  modelValue: boolean
  entityId?: number
}>(), { entityId: undefined })

const emit = defineEmits<{
  'update:modelValue': [value: boolean]
  'saved': []
}>()

const form = ref({ subject: '', date: new Date().toISOString().slice(0, 10), duration: 60, content: '', reflection: '' })
const saving = ref(false)

watch(() => props.modelValue, async (val) => {
  if (!val) return
  if (props.entityId) {
    const r = (await getStudyRecordById(props.entityId)).data.data
    form.value = { subject: r.subject, date: r.date, duration: r.duration, content: r.content || '', reflection: r.reflection || '' }
  } else {
    form.value = { subject: '', date: new Date().toISOString().slice(0, 10), duration: 60, content: '', reflection: '' }
  }
})

async function handleSave() {
  if (!form.value.subject.trim()) { ElMessage.warning('请输入学习主题'); return }
  if (!form.value.duration || form.value.duration < 1) { ElMessage.warning('时长至少1分钟'); return }
  saving.value = true
  try {
    if (props.entityId) {
      await updateStudyRecord(props.entityId, form.value)
      ElMessage.success('已更新')
    } else {
      await createStudyRecord(form.value)
      ElMessage.success('已创建')
    }
    emit('update:modelValue', false)
    emit('saved')
  } finally { saving.value = false }
}
</script>

<template>
  <el-drawer
    :model-value="modelValue"
    :title="entityId ? '编辑学习记录' : '新建学习记录'"
    :size="480"
    @update:model-value="emit('update:modelValue', $event)"
  >
    <el-form label-position="top">
      <UiSection title="基本信息">
        <el-form-item label="学习主题" required>
          <UiInput v-model="form.subject" placeholder="例如：Spring Boot 基础学习" />
        </el-form-item>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="日期" required>
              <UiDatePicker v-model="form.date" type="date" value-format="YYYY-MM-DD" style="width:100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="时长（分钟）" required>
              <el-input-number v-model="form.duration" :min="1" :max="1440" style="width:100%" />
            </el-form-item>
          </el-col>
        </el-row>
      </UiSection>

      <UiSection title="学习内容">
        <el-form-item label="学习内容">
          <UiTextarea v-model="form.content" placeholder="记录今天学了什么..." />
        </el-form-item>
        <el-form-item label="心得">
          <UiTextarea v-model="form.reflection" placeholder="有什么收获或思考？" />
        </el-form-item>
      </UiSection>
    </el-form>

    <template #footer>
      <el-button @click="emit('update:modelValue', false)">取消</el-button>
      <UiButton type="primary" :loading="saving" @click="handleSave">保存</UiButton>
    </template>
  </el-drawer>
</template>
