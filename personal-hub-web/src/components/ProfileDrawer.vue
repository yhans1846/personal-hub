<script setup lang="ts">
import { ref, reactive, computed, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { getProfile, updateProfile, uploadAvatar } from '@/modules/system/api'
import { useAuthStore } from '@/store/authStore'
import type { ProfileUpdateDTO, Gender } from '@/types/user'
import { regionOptions } from '@/modules/system/settings/components/regionData'
import { User, Camera, X } from 'lucide-vue-next'
import { DialogPropCard, UiButton } from '@/components/ui'

const props = defineProps<{ visible: boolean }>()
const emit = defineEmits<{ 'update:visible': [value: boolean] }>()

const authStore = useAuthStore()
const loading = ref(true)
const saving = ref(false)
const avatarUploading = ref(false)
const avatarPreview = ref<string | null>(null)
const activeTab = ref<'basic' | 'contact' | 'bio'>('basic')

const form = reactive<ProfileUpdateDTO>({
  nickname: '', email: '', gender: 0 as Gender, birthday: null,
  phone: null, country: null, province: null, city: null, district: null,
  website: null, github: null, bio: null,
})

const initialForm = ref<string>('')
const formRef = ref<{ validate: () => Promise<void> } | null>(null)
const regionValue = ref<string[]>([])

const displayName = computed(() => form.nickname || authStore.user?.username || '未设置昵称')
const displayUsername = computed(() => authStore.user?.username || '')

const tabs = [
  { key: 'basic' as const, label: '基本' },
  { key: 'contact' as const, label: '联系' },
  { key: 'bio' as const, label: '简介' },
]

watch(() => props.visible, (v) => {
  if (v) {
    activeTab.value = 'basic'
    loadProfile()
  }
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
    } else {
      regionValue.value = []
    }
    avatarPreview.value = data.avatar
    initialForm.value = JSON.stringify({ ...form })
  } catch {
    ElMessage.error('加载个人资料失败')
  } finally {
    loading.value = false
  }
}

function onRegionChange(val: string[] | null) {
  form.country = null
  form.province = null
  form.city = null
  form.district = null
  if (!val || val.length === 0) return
  form.country = val[0]
  if (val.length >= 2) form.province = val[1]
  if (val.length >= 3) form.city = val[2]
  if (val.length >= 4) form.district = val[3]
}

async function handleAvatarUpload(file: File) {
  const allowed = ['image/jpeg', 'image/png', 'image/gif', 'image/webp']
  if (!allowed.includes(file.type)) {
    ElMessage.warning('仅支持 JPG/PNG/GIF/WebP 格式')
    return
  }
  if (file.size > 5 * 1024 * 1024) {
    ElMessage.warning('头像文件不能超过 5MB')
    return
  }
  avatarUploading.value = true
  try {
    const res = await uploadAvatar(file)
    avatarPreview.value = res.data.data.url
    if (authStore.user) {
      authStore.user.avatar = res.data.data.url
      authStore.setUser(authStore.user)
    }
    ElMessage.success('头像已更新')
  } catch {
    ElMessage.error('头像上传失败')
  } finally {
    avatarUploading.value = false
  }
}

function onAvatarSelect() {
  const input = document.createElement('input')
  input.type = 'file'
  input.accept = 'image/jpeg,image/png,image/gif,image/webp'
  input.onchange = (e: Event) => {
    const file = (e.target as HTMLInputElement).files?.[0]
    if (file) handleAvatarUpload(file)
  }
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
  } else {
    regionValue.value = []
  }
}

async function handleSave() {
  if (!formRef.value) return
  try {
    await formRef.value.validate()
  } catch {
    const needContact = !form.email || !/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(form.email)
    const needBasic = !form.nickname || form.nickname.length < 2 || form.nickname.length > 20
    if (needContact) activeTab.value = 'contact'
    else if (needBasic) activeTab.value = 'basic'
    ElMessage.warning('请检查表单填写')
    return
  }
  saving.value = true
  try {
    await updateProfile(form)
    if (authStore.user) {
      authStore.user.nickname = form.nickname
      authStore.setUser(authStore.user)
    }
    initialForm.value = JSON.stringify({ ...form })
    ElMessage.success('保存成功')
  } catch {
    ElMessage.error('保存失败')
  } finally {
    saving.value = false
  }
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
    { type: 'email' as const, message: '邮箱格式不正确', trigger: 'blur' },
  ],
  phone: [{ pattern: /^$|^1[3-9]\d{9}$/, message: '手机号格式不正确', trigger: 'blur' }],
  website: [{ pattern: /^$|^https?:\/\/.+/, message: '请输入正确的网址', trigger: 'blur' }],
  github: [{ pattern: /^$|^https:\/\/github\.com\/.+/, message: '请输入正确的 GitHub 地址', trigger: 'blur' }],
}
</script>

<template>
  <el-drawer
    :model-value="props.visible"
    class="profile-drawer"
    direction="rtl"
    size="420px"
    :with-header="false"
    @update:model-value="emit('update:visible', $event)"
  >
    <div class="profile-shell">
      <div class="drawer-header">
        <h2>个人资料</h2>
        <button type="button" class="drawer-close" aria-label="关闭" @click="emit('update:visible', false)">
          <X :size="18" />
        </button>
      </div>

      <div v-loading="loading" class="drawer-main">
        <div class="profile-hero">
          <button
            type="button"
            class="avatar-wrap"
            :disabled="avatarUploading"
            :aria-label="avatarUploading ? '上传中' : '更换头像'"
            @click="onAvatarSelect"
          >
            <img v-if="avatarPreview" :src="avatarPreview" class="avatar-img" alt="" />
            <div v-else class="avatar-placeholder"><User :size="36" /></div>
            <span class="avatar-overlay">
              <Camera :size="20" />
            </span>
          </button>
          <div class="hero-meta">
            <div class="hero-name">{{ displayName }}</div>
            <div v-if="displayUsername" class="hero-handle">@{{ displayUsername }}</div>
            <div class="hero-hint">JPG / PNG / GIF / WebP · 最大 5MB</div>
          </div>
        </div>

        <div class="profile-seg" role="tablist">
          <button
            v-for="tab in tabs"
            :key="tab.key"
            type="button"
            role="tab"
            class="seg-item"
            :class="{ active: activeTab === tab.key }"
            :aria-selected="activeTab === tab.key"
            @click="activeTab = tab.key"
          >
            {{ tab.label }}
          </button>
        </div>

        <el-form ref="formRef" class="profile-form" :model="form" :rules="rules" label-position="top">
          <div v-show="activeTab === 'basic'" class="tab-pane">
            <DialogPropCard label="基本信息">
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
              <el-date-picker
                v-model="form.birthday"
                type="date"
                placeholder="选择日期"
                value-format="YYYY-MM-DD"
                style="width: 100%"
                popper-class="ph-date-popper"
              />
              </el-form-item>
            </DialogPropCard>
          </div>

          <div v-show="activeTab === 'contact'" class="tab-pane">
            <DialogPropCard label="联系方式">
              <el-form-item label="邮箱" prop="email">
                <el-input v-model="form.email" placeholder="输入邮箱" />
              </el-form-item>
              <el-form-item label="手机号" prop="phone">
                <el-input v-model="form.phone" placeholder="输入手机号（选填）" maxlength="11" />
              </el-form-item>
              <el-form-item label="所在地区">
                <el-cascader
                  v-model="regionValue"
                  :options="regionOptions"
                  placeholder="选择地区（选填）"
                  clearable
                  style="width: 100%"
                  @change="onRegionChange"
                />
              </el-form-item>
            </DialogPropCard>
          </div>

          <div v-show="activeTab === 'bio'" class="tab-pane">
            <DialogPropCard label="简介与链接">
              <el-form-item label="GitHub" prop="github">
                <el-input v-model="form.github" placeholder="https://github.com/用户名（选填）" />
              </el-form-item>
              <el-form-item label="个人网站" prop="website">
                <el-input v-model="form.website" placeholder="https://（选填）" />
              </el-form-item>
              <el-form-item label="个人简介" prop="bio">
                <el-input
                  v-model="form.bio"
                  type="textarea"
                  :rows="4"
                  maxlength="200"
                  show-word-limit
                  placeholder="介绍自己…"
                />
              </el-form-item>
            </DialogPropCard>
          </div>
        </el-form>
      </div>

      <div class="drawer-footer">
        <UiButton text @click="handleReset">重置</UiButton>
        <UiButton type="primary" :loading="saving" @click="handleSave">保存</UiButton>
      </div>
    </div>
  </el-drawer>
</template>

<style scoped>
.profile-shell {
  display: flex;
  flex-direction: column;
  height: 100%;
  min-height: 0;
}

.drawer-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 16px 20px 0;
  flex-shrink: 0;
}

.drawer-header h2 {
  margin: 0;
  font-size: 16px;
  font-weight: 600;
  color: var(--text-primary);
}

.drawer-close {
  background: none;
  border: none;
  cursor: pointer;
  color: var(--text-tertiary);
  padding: 6px;
  border-radius: var(--radius-md);
  display: flex;
  transition: all var(--transition);
}

.drawer-close:hover {
  background: var(--bg-hover);
  color: var(--text-primary);
}

.drawer-main {
  flex: 1;
  min-height: 0;
  overflow-y: auto;
  padding: 16px 20px 12px;
}

.profile-hero {
  display: flex;
  align-items: center;
  gap: 18px;
  padding: 20px 18px;
  margin-bottom: 16px;
  border-radius: var(--radius-xl);
  background: linear-gradient(135deg, var(--bg-hover), var(--bg-card));
  border: 1px solid var(--border-color);
}

.avatar-wrap {
  position: relative;
  width: 88px;
  height: 88px;
  flex-shrink: 0;
  border-radius: 50%;
  overflow: hidden;
  cursor: pointer;
  border: 2px solid var(--border-color);
  padding: 0;
  background: var(--bg-card);
}

.avatar-wrap:disabled {
  cursor: wait;
  opacity: 0.8;
}

.avatar-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  display: block;
}

.avatar-placeholder {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  background: var(--bg-hover);
  color: var(--text-tertiary);
}

.avatar-overlay {
  position: absolute;
  inset: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  background: rgba(0, 0, 0, 0.45);
  color: #fff;
  opacity: 0;
  transition: opacity var(--transition);
}

.avatar-wrap:hover .avatar-overlay,
.avatar-wrap:focus-visible .avatar-overlay {
  opacity: 1;
}

.hero-meta {
  min-width: 0;
  flex: 1;
}

.hero-name {
  font-size: 20px;
  font-weight: 600;
  color: var(--text-primary);
  line-height: 1.3;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.hero-handle {
  margin-top: 4px;
  font-size: 14px;
  color: var(--text-secondary);
}

.hero-hint {
  margin-top: 8px;
  font-size: 12px;
  color: var(--text-tertiary);
}

.profile-seg {
  display: flex;
  gap: 4px;
  padding: 4px;
  margin-bottom: 14px;
  border-radius: var(--radius-md);
  background: var(--bg-hover);
}

.seg-item {
  flex: 1;
  border: none;
  background: transparent;
  color: var(--text-secondary);
  font-size: 13px;
  font-weight: 500;
  padding: 8px 0;
  border-radius: var(--radius-md);
  cursor: pointer;
  transition: all var(--transition);
}

.seg-item:hover {
  color: var(--text-primary);
}

.seg-item.active {
  background: var(--bg-card);
  color: var(--text-primary);
  font-weight: 600;
  box-shadow: 0 1px 2px rgba(0, 0, 0, 0.06);
}

.profile-form {
  min-height: 220px;
}

.tab-pane :deep(.el-form-item) {
  margin-bottom: 14px;
}

.tab-pane :deep(.el-form-item:last-child) {
  margin-bottom: 0;
}

.tab-pane :deep(.el-form-item__label) {
  font-size: 13px;
  font-weight: 500;
  color: var(--text-secondary);
  padding-bottom: 4px;
}

.drawer-footer {
  flex-shrink: 0;
  display: flex;
  justify-content: flex-end;
  gap: 10px;
  padding: 12px 20px 16px;
  border-top: 1px solid var(--border-color);
  background: var(--bg-card);
}
</style>

<style>
/* drawer 内容区占满高度，便于内部 flex + 吸底 */
.profile-drawer.el-drawer .el-drawer__body {
  padding: 0;
  height: 100%;
  overflow: hidden;
}
</style>
