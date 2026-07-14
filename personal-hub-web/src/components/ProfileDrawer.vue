<script setup lang="ts">
import { ref, reactive, computed, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { getProfile, updateProfile, uploadAvatar } from '@/modules/system/api'
import { useAuthStore } from '@/store/authStore'
import type { ProfileUpdateDTO, Gender } from '@/types/user'
import { regionOptions } from '@/modules/system/settings/components/regionData'
import { User, Camera, X } from 'lucide-vue-next'

const props = defineProps<{ visible: boolean }>()
const emit = defineEmits<{ 'update:visible': [value: boolean] }>()

const authStore = useAuthStore()
const loading = ref(true)
const saving = ref(false)
const avatarUploading = ref(false)
const avatarPreview = ref<string | null>(null)

const form = reactive<ProfileUpdateDTO>({
  nickname: '', email: '', gender: 0 as Gender, birthday: null,
  phone: null, country: null, province: null, city: null, district: null,
  website: null, github: null, bio: null,
})

const initialForm = ref<string>('')
const formRef = ref<any>(null)
const regionValue = ref<string[]>([])

watch(() => props.visible, (v) => {
  if (v) loadProfile()
})

async function loadProfile() {
  loading.value = true
  try {
    const res = await getProfile()
    const data = res.data.data
    Object.assign(form, {
      nickname: data.nickname || '', email: data.email || '',
      gender: data.gender ?? 0, birthday: data.birthday || null,
      phone: data.phone || null, country: data.country || null,
      province: data.province || null, city: data.city || null,
      district: data.district || null,
      website: data.website || null, github: data.github || null,
      bio: data.bio || null,
    })
    if (data.country && data.province) {
      const arr = [data.country, data.province]
      if (data.city) arr.push(data.city)
      if (data.district) arr.push(data.district)
      regionValue.value = arr
    }
    avatarPreview.value = data.avatar
    initialForm.value = JSON.stringify({ ...form })
  } catch { ElMessage.error('加载个人资料失败') }
  finally { loading.value = false }
}

function onRegionChange(val: string[]) {
  form.country = null; form.province = null; form.city = null; form.district = null
  if (val.length === 0) return
  form.country = val[0]
  if (val.length >= 2) form.province = val[1]
  if (val.length >= 3) form.city = val[2]
  if (val.length >= 4) form.district = val[3]
}

async function handleAvatarUpload(file: File) {
  const allowed = ['image/jpeg', 'image/png', 'image/gif', 'image/webp']
  if (!allowed.includes(file.type)) { ElMessage.warning('仅支持 JPG/PNG/GIF/WebP 格式'); return }
  if (file.size > 5 * 1024 * 1024) { ElMessage.warning('头像文件不能超过 5MB'); return }
  avatarUploading.value = true
  try {
    const res = await uploadAvatar(file)
    avatarPreview.value = res.data.data.url
    if (authStore.user) { authStore.user.avatar = res.data.data.url; authStore.setUser(authStore.user) }
    ElMessage.success('头像已更新')
  } catch { ElMessage.error('头像上传失败') }
  finally { avatarUploading.value = false }
}

function onAvatarSelect() {
  const input = document.createElement('input')
  input.type = 'file'; input.accept = 'image/jpeg,image/png,image/gif,image/webp'
  input.onchange = (e: any) => { const file = e.target.files?.[0]; if (file) handleAvatarUpload(file) }
  input.click()
}

function handleReset() {
  const saved = JSON.parse(initialForm.value)
  Object.assign(form, saved)
  if (form.country && form.province) {
    const arr = [form.country, form.province]
    if (form.city) arr.push(form.city)
    if (form.district) arr.push(form.district)
    regionValue.value = arr
  } else { regionValue.value = [] }
}

async function handleSave() {
  if (!formRef.value) return
  try { await formRef.value.validate() }
  catch { ElMessage.warning('请检查表单填写'); return }
  saving.value = true
  try {
    await updateProfile(form)
    if (authStore.user) { authStore.user.nickname = form.nickname; authStore.setUser(authStore.user) }
    initialForm.value = JSON.stringify({ ...form })
    ElMessage.success('保存成功')
  } catch { ElMessage.error('保存失败') }
  finally { saving.value = false }
}

const genderOptions = [
  { value: 0, label: '保密' }, { value: 1, label: '男' }, { value: 2, label: '女' },
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
  phone: [{ pattern: /^$|^1[3-9]\d{9}$/, message: '手机号格式不正确', trigger: 'blur' }],
  website: [{ pattern: /^$|^https?:\/\/.+/, message: '请输入正确的网址', trigger: 'blur' }],
  github: [{ pattern: /^$|^https:\/\/github\.com\/.+/, message: '请输入正确的 GitHub 地址', trigger: 'blur' }],
}
</script>

<template>
  <el-drawer
    :model-value="props.visible"
    @update:model-value="emit('update:visible', $event)"
    direction="ltr"
    size="480px"
    :with-header="false"
    class="profile-drawer"
  >
    <div class="drawer-header">
      <h2>个人资料</h2>
      <button class="drawer-close" @click="emit('update:visible', false)">
        <X :size="18" />
      </button>
    </div>

    <div v-loading="loading" class="drawer-body">
      <!-- 头像 -->
      <div class="profile-card avatar-card">
        <div class="avatar-wrap" @click="onAvatarSelect">
          <img v-if="avatarPreview" :src="avatarPreview" class="avatar-img" alt="" />
          <div v-else class="avatar-placeholder"><User :size="40" /></div>
          <div class="avatar-overlay">
            <Camera :size="18" />
            <span>{{ avatarUploading ? '上传中...' : '更换头像' }}</span>
          </div>
        </div>
        <p class="avatar-hint">JPG / PNG / GIF / WebP，最大 5MB</p>
      </div>

      <el-form ref="formRef" :model="form" :rules="rules" label-position="top">
        <!-- 基本信息 -->
        <div class="profile-card">
          <h3 class="card-title">基本信息</h3>
          <el-form-item label="用户名">
            <el-input :model-value="authStore.user?.username" disabled />
          </el-form-item>
          <el-form-item label="昵称" prop="nickname">
            <el-input v-model="form.nickname" placeholder="输入昵称" maxlength="20" show-word-limit />
          </el-form-item>
          <el-form-item label="性别">
            <el-radio-group v-model="form.gender">
              <el-radio v-for="g in genderOptions" :key="g.value" :value="g.value">{{ g.label }}</el-radio>
            </el-radio-group>
          </el-form-item>
          <el-form-item label="出生日期">
            <el-date-picker v-model="form.birthday" type="date" placeholder="选择日期" value-format="YYYY-MM-DD" style="width:100%" />
          </el-form-item>
        </div>

        <!-- 联系方式 -->
        <div class="profile-card">
          <h3 class="card-title">联系方式</h3>
          <el-form-item label="邮箱" prop="email">
            <el-input v-model="form.email" placeholder="输入邮箱" />
          </el-form-item>
          <el-form-item label="手机号" prop="phone">
            <el-input v-model="form.phone" placeholder="输入手机号（选填）" maxlength="11" />
          </el-form-item>
          <el-form-item label="所在地区">
            <el-cascader
              v-model="regionValue" :options="regionOptions"
              placeholder="选择地区（选填）" clearable style="width:100%"
              @change="onRegionChange"
            />
          </el-form-item>
        </div>

        <!-- 个人简介 -->
        <div class="profile-card">
          <h3 class="card-title">个人简介</h3>
          <el-form-item label="GitHub" prop="github">
            <el-input v-model="form.github" placeholder="https://github.com/用户名（选填）" />
          </el-form-item>
          <el-form-item label="个人网站" prop="website">
            <el-input v-model="form.website" placeholder="https://（选填）" />
          </el-form-item>
          <el-form-item label="个人简介" prop="bio">
            <el-input v-model="form.bio" type="textarea" :rows="3" maxlength="200" show-word-limit placeholder="介绍自己…" />
          </el-form-item>
        </div>

        <div class="form-actions">
          <el-button size="default" @click="handleReset">重置</el-button>
          <el-button type="primary" :loading="saving" @click="handleSave">保存修改</el-button>
        </div>
      </el-form>
    </div>
  </el-drawer>
</template>

<style scoped>
.drawer-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 20px 24px 0;
}
.drawer-header h2 {
  margin: 0;
  font-size: 18px;
  font-weight: 600;
}
.drawer-close {
  background: none; border: none; cursor: pointer;
  color: var(--text-tertiary); padding: 4px; border-radius: 6px;
  display: flex; transition: all var(--transition);
}
.drawer-close:hover { background: var(--bg-hover); color: var(--text-primary); }

.drawer-body {
  padding: 20px 24px 40px;
  overflow-y: auto;
  height: calc(100% - 60px);
}
.profile-card {
  background: var(--bg-card);
  border: 1px solid var(--border-color);
  border-radius: 12px;
  padding: 20px;
  margin-bottom: 16px;
}
.card-title {
  margin: 0 0 16px;
  font-size: 14px;
  font-weight: 600;
}

.avatar-card { text-align: center; }
.avatar-wrap {
  position: relative;
  width: 72px; height: 72px;
  margin: 0 auto 10px;
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
.avatar-hint { font-size: 11px; color: var(--text-tertiary); margin: 0; }

:deep(.el-form-item) { margin-bottom: 16px; }
:deep(.el-form-item__label) {
  font-size: 13px; font-weight: 500;
  color: var(--text-secondary); padding-bottom: 4px;
}

.form-actions {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  padding-top: 4px;
}
</style>
