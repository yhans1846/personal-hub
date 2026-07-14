<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getProfile, updateProfile, uploadAvatar } from '@/modules/system/api'
import { useAuthStore } from '@/store/authStore'
import type { ProfileVO, ProfileUpdateDTO, Gender } from '@/types/user'
import { regionOptions } from './regionData'
import { User, Mail, Camera } from 'lucide-vue-next'

const authStore = useAuthStore()
const loading = ref(true)
const saving = ref(false)
const avatarUploading = ref(false)
const avatarPreview = ref<string | null>(null)

const form = reactive<ProfileUpdateDTO>({
  nickname: '',
  email: '',
  gender: 0 as Gender,
  birthday: null,
  phone: null,
  country: null,
  province: null,
  city: null,
  website: null,
  github: null,
  bio: null,
})

const initialForm = ref<string>('')
const formRef = ref<any>(null)
const regionValue = ref<string[]>([])

const bioCount = computed(() => (form.bio || '').length)

onMounted(async () => {
  try {
    const res = await getProfile()
    const data = res.data.data as ProfileVO
    Object.assign(form, {
      nickname: data.nickname || '',
      email: data.email || '',
      gender: data.gender ?? 0,
      birthday: data.birthday || null,
      phone: data.phone || null,
      country: data.country || null,
      province: data.province || null,
      city: data.city || null,
      website: data.website || null,
      github: data.github || null,
      bio: data.bio || null,
    })
    if (data.country && data.province) {
      regionValue.value = data.city ? [data.country, data.province, data.city] : [data.country, data.province]
    }
    avatarPreview.value = data.avatar
    initialForm.value = JSON.stringify({ ...form })
  } catch {
    ElMessage.error('加载个人资料失败')
  } finally {
    loading.value = false
  }
})

function onRegionChange(val: string[]) {
  if (val.length >= 1) form.country = val[0]
  if (val.length >= 2) form.province = val[1]
  if (val.length >= 3) form.city = val[2]
  if (val.length === 0) {
    form.country = null; form.province = null; form.city = null
  }
}

async function handleAvatarUpload(file: File) {
  const allowed = ['image/jpeg', 'image/png', 'image/gif', 'image/webp']
  if (!allowed.includes(file.type)) { ElMessage.warning('仅支持 JPG/PNG/GIF/WebP 格式'); return }
  if (file.size > 5 * 1024 * 1024) { ElMessage.warning('头像文件不能超过 5MB'); return }
  avatarUploading.value = true
  try {
    const res = await uploadAvatar(file)
    avatarPreview.value = res.data.data.url
    if (authStore.user) {
      authStore.user.avatar = res.data.data.url
      authStore.setUser(authStore.user)
    }
    ElMessage.success('头像已更新')
  } catch { ElMessage.error('头像上传失败') }
  finally { avatarUploading.value = false }
}

function onAvatarSelect() {
  const input = document.createElement('input')
  input.type = 'file'
  input.accept = 'image/jpeg,image/png,image/gif,image/webp'
  input.onchange = (e: any) => {
    const file = e.target.files?.[0]
    if (file) handleAvatarUpload(file)
  }
  input.click()
}

function handleReset() {
  const saved = JSON.parse(initialForm.value)
  Object.assign(form, saved)
  if (form.country && form.province) {
    regionValue.value = form.city ? [form.country, form.province, form.city] : [form.country, form.province]
  } else { regionValue.value = [] }
  ElMessage.info('已重置')
}

async function handleSave() {
  if (!formRef.value) return
  try { await formRef.value.validate() }
  catch { ElMessage.warning('请检查表单填写'); return }

  saving.value = true
  try {
    const res = await updateProfile(form)
    if (authStore.user) {
      authStore.user.nickname = form.nickname
      authStore.setUser(authStore.user)
    }
    initialForm.value = JSON.stringify({ ...form })
    ElMessage.success('保存成功')
  } catch { ElMessage.error('保存失败') }
  finally { saving.value = false }
}

const genderOptions = [
  { value: 0, label: '保密' },
  { value: 1, label: '男' },
  { value: 2, label: '女' },
]

const rules = {
  nickname: [
    { required: true, message: '请输入昵称', trigger: 'blur' },
    { min: 2, max: 20, message: '昵称长度 2~20 个字符', trigger: 'blur' },
  ],
  email: [
    { required: true, message: '请输入邮箱', trigger: 'blur' },
    { type: 'email', message: '邮箱格式不正确', trigger: 'blur' },
  ],
  phone: [
    { pattern: /^$|^1[3-9]\d{9}$/, message: '手机号格式不正确', trigger: 'blur' },
  ],
  website: [
    { pattern: /^$|^https?:\/\/.+/, message: '请输入正确的网址（以 https:// 开头）', trigger: 'blur' },
  ],
  github: [
    { pattern: /^$|^https:\/\/github\.com\/.+/, message: '请输入正确的 GitHub 地址', trigger: 'blur' },
  ],
}
</script>

<template>
  <div v-loading="loading" class="profile-settings">
    <!-- 头像 -->
    <div class="profile-card avatar-card">
      <div class="avatar-wrap" @click="onAvatarSelect">
        <img v-if="avatarPreview" :src="avatarPreview" class="avatar-img" alt="头像" />
        <div v-else class="avatar-placeholder"><User :size="40" /></div>
        <div class="avatar-overlay">
          <Camera :size="18" />
          <span>{{ avatarUploading ? '上传中...' : '更换头像' }}</span>
        </div>
      </div>
      <p class="avatar-hint">支持 JPG、PNG、GIF、WebP，最大 5MB</p>
    </div>

    <el-form ref="formRef" :model="form" :rules="rules" label-position="top" class="profile-form">
      <!-- 基本信息 -->
      <div class="profile-card">
        <h3 class="card-title">基本信息</h3>
        <div class="form-grid">
          <el-form-item label="用户名">
            <el-input :model-value="authStore.user?.username" disabled class="field-input" />
          </el-form-item>
          <el-form-item label="昵称" prop="nickname">
            <el-input v-model="form.nickname" placeholder="输入昵称" class="field-input" maxlength="20" show-word-limit />
          </el-form-item>
          <el-form-item label="性别">
            <el-radio-group v-model="form.gender">
              <el-radio v-for="g in genderOptions" :key="g.value" :value="g.value">{{ g.label }}</el-radio>
            </el-radio-group>
          </el-form-item>
          <el-form-item label="出生日期">
            <el-date-picker v-model="form.birthday" type="date" placeholder="选择日期" value-format="YYYY-MM-DD" class="field-input" />
          </el-form-item>
        </div>
      </div>

      <!-- 联系方式 -->
      <div class="profile-card">
        <h3 class="card-title">联系方式</h3>
        <div class="form-grid">
          <el-form-item label="邮箱" prop="email">
            <el-input v-model="form.email" placeholder="输入邮箱" class="field-input" />
          </el-form-item>
          <el-form-item label="手机号" prop="phone">
            <el-input v-model="form.phone" placeholder="输入手机号（选填）" class="field-input" maxlength="11" />
          </el-form-item>
          <el-form-item label="所在地区">
            <el-cascader
              v-model="regionValue"
              :options="regionOptions"
              placeholder="选择地区（选填）"
              class="field-input"
              clearable
              @change="onRegionChange"
            />
          </el-form-item>
        </div>
      </div>

      <!-- 个人简介 -->
      <div class="profile-card">
        <h3 class="card-title">个人简介</h3>
        <div class="form-grid">
          <el-form-item label="GitHub" prop="github">
            <el-input v-model="form.github" placeholder="https://github.com/用户名（选填）" class="field-input" />
          </el-form-item>
          <el-form-item label="个人网站" prop="website">
            <el-input v-model="form.website" placeholder="https://（选填）" class="field-input" />
          </el-form-item>
          <el-form-item label="个人简介" prop="bio">
            <el-input
              v-model="form.bio"
              type="textarea"
              :rows="4"
              maxlength="200"
              show-word-limit
              placeholder="介绍自己…"
              class="field-textarea"
            />
          </el-form-item>
        </div>
      </div>
    </el-form>

    <div class="form-actions">
      <el-button @click="handleReset">重置</el-button>
      <el-button type="primary" :loading="saving" @click="handleSave">保存修改</el-button>
    </div>
  </div>
</template>

<style scoped>
.profile-settings { max-width: 640px; }
.profile-card {
  background: var(--bg-card);
  border: 1px solid var(--border-color);
  border-radius: 16px;
  padding: 24px;
  margin-bottom: 20px;
}
.card-title {
  margin: 0 0 20px;
  font-size: 15px;
  font-weight: 600;
  color: var(--text-primary);
}

.avatar-card { text-align: center; }
.avatar-wrap {
  position: relative;
  width: 80px; height: 80px;
  margin: 0 auto 12px;
  border-radius: 50%;
  overflow: hidden;
  cursor: pointer;
  border: 2px solid var(--border-color);
}
.avatar-img { width: 100%; height: 100%; object-fit: cover; }
.avatar-placeholder {
  width: 100%; height: 100%;
  display: flex; align-items: center; justify-content: center;
  background: var(--bg-hover);
  color: var(--text-tertiary);
}
.avatar-overlay {
  position: absolute; inset: 0;
  display: flex; flex-direction: column;
  align-items: center; justify-content: center;
  gap: 4px;
  background: rgba(0,0,0,0.5);
  color: #fff;
  font-size: 11px;
  opacity: 0;
  transition: opacity var(--transition);
}
.avatar-wrap:hover .avatar-overlay { opacity: 1; }
.avatar-hint { font-size: 12px; color: var(--text-tertiary); margin: 0; }

.profile-form :deep(.el-form-item) { margin-bottom: 18px; }
.profile-form :deep(.el-form-item__label) {
  font-size: 13px;
  font-weight: 500;
  color: var(--text-secondary);
  padding-bottom: 4px;
}
.form-grid { display: flex; flex-direction: column; }
.field-input { width: 420px; max-width: 100%; }
.field-textarea { width: 420px; max-width: 100%; }

.form-actions {
  display: flex;
  justify-content: flex-end;
  gap: var(--sp-3);
  padding-top: 8px;
}
</style>
