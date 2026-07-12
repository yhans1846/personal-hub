<script setup lang="ts">
import { ref, watch, computed } from 'vue'
import { createStudyPlan, updateStudyPlan, getStudyPlanById } from '@/api/studyplanApi'
import { ElMessage } from 'element-plus'
import { UiDialog, UiInput, UiTextarea, UiDatePicker, UiButton, UiSection } from '@/components/ui'

const props = withDefaults(defineProps<{
  modelValue: boolean
  entityId?: number
}>(), { entityId: undefined })

const emit = defineEmits<{
  'update:modelValue': [value: boolean]
  'saved': []
}>()

const form = ref({
  name: '', goal: '', progress: 0,
  startDate: null as string | null, endDate: null as string | null, status: 0
})
const saving = ref(false)

const statusOptions = [
  { value: 0, label: '未开始' }, { value: 1, label: '进行中' },
  { value: 2, label: '已完成' }, { value: 3, label: '已放弃' }
]

const progressColor = computed(() => {
  if (form.value.progress >= 100) return 'var(--success)'
  if (form.value.progress >= 50) return 'var(--accent)'
  return 'var(--warning)'
})

watch(() => props.modelValue, async (val) => {
  if (!val) return
  if (props.entityId) {
    const r = (await getStudyPlanById(props.entityId)).data.data
    form.value = { name: r.name, goal: r.goal || '', progress: r.progress, startDate: r.startDate, endDate: r.endDate, status: r.status }
  } else {
    form.value = { name: '', goal: '', progress: 0, startDate: null, endDate: null, status: 0 }
  }
})

async function handleSave() {
  if (!form.value.name.trim()) { ElMessage.warning('请输入计划名称'); return }
  saving.value = true
  try {
    if (props.entityId) {
      await updateStudyPlan(props.entityId, form.value)
      ElMessage.success('已更新')
    } else {
      await createStudyPlan(form.value)
      ElMessage.success('已创建')
    }
    emit('update:modelValue', false)
    emit('saved')
  } finally { saving.value = false }
}
</script>

<template>
  <UiDialog
    :model-value="modelValue"
    :title="entityId ? '编辑计划' : '新建计划'"
    @update:model-value="emit('update:modelValue', $event)"
  >
    <el-form label-position="top">
      <UiSection title="基本信息">
        <el-form-item label="计划名称" required>
          <UiInput v-model="form.name" placeholder="例如：Spring Boot 深入学习" maxlength="200" show-word-limit />
        </el-form-item>
        <el-form-item label="学习目标">
          <UiTextarea v-model="form.goal" placeholder="设定学习目标（可选）" />
        </el-form-item>
      </UiSection>

      <UiSection title="时间安排">
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="开始日期"><UiDatePicker v-model="form.startDate" type="date" value-format="YYYY-MM-DD" placeholder="开始日期" style="width:100%" clearable /></el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="结束日期"><UiDatePicker v-model="form.endDate" type="date" value-format="YYYY-MM-DD" placeholder="结束日期" style="width:100%" clearable /></el-form-item>
          </el-col>
        </el-row>
      </UiSection>

      <UiSection title="进度">
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
      </UiSection>
    </el-form>

    <template #footer>
      <el-button @click="emit('update:modelValue', false)">取消</el-button>
      <UiButton type="primary" :loading="saving" @click="handleSave">保存</UiButton>
    </template>
  </UiDialog>
</template>

<style scoped>
.progress-label { font-size: var(--text-sm); font-weight: 600; margin-left: var(--sp-2); }
</style>
