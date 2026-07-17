<script setup lang="ts">
import { ref } from 'vue'
import { useAuthStore } from '@/store/authStore'
import { ElMessage } from 'element-plus'
import { Layers } from 'lucide-vue-next'

const authStore = useAuthStore()
const form = ref({ username: 'admin', password: '123456' })
const loading = ref(false)

async function handleLogin() {
  if (!form.value.username || !form.value.password) {
    ElMessage.warning('请输入用户名和密码')
    return
  }
  loading.value = true
  try {
    await authStore.login(form.value.username, form.value.password)
    ElMessage.success('登录成功')
  } catch (e: any) {
    const msg = e?.response?.data?.message || '登录失败'
    ElMessage.error(msg)
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <div class="login-page">
    <div class="login-bg">
      <div class="login-bg-shape shape-1" />
      <div class="login-bg-shape shape-2" />
    </div>
    <div class="login-card">
      <div class="login-brand">
        <Layers :size="32" stroke="var(--accent)" />
        <span class="login-brand-text">Personal Hub</span>
      </div>
      <h2 class="login-title">登录你的账户</h2>
      <p class="login-subtitle">个人知识管理系统</p>

      <el-form @submit.prevent="handleLogin" class="login-form">
        <el-form-item>
          <el-input v-model="form.username" placeholder="用户名" size="large" clearable />
        </el-form-item>
        <el-form-item>
          <el-input v-model="form.password" type="password" placeholder="密码" size="large" show-password @keyup.enter="handleLogin" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" size="large" :loading="loading" class="login-btn" @click="handleLogin">
            {{ loading ? '登录中...' : '登录' }}
          </el-button>
        </el-form-item>
      </el-form>
    </div>
  </div>
</template>

<style scoped>
.login-page {
  position: relative; display: flex; align-items: center; justify-content: center;
  height: 100vh; background: var(--bg-body); overflow: hidden;
}
.login-bg { position: absolute; inset: 0; pointer-events: none; }
.login-bg-shape { position: absolute; border-radius: 50%; filter: blur(80px); opacity: 0.15; }
.shape-1 { width: 400px; height: 400px; background: var(--accent); top: -100px; right: -100px; }
.shape-2 { width: 300px; height: 300px; background: var(--info); bottom: -80px; left: -80px; }
.login-card {
  position: relative; width: 400px; padding: var(--sp-10) var(--sp-8);
  background: var(--bg-card); border: 1px solid var(--border-color);
  border-radius: var(--radius-xl); box-shadow: var(--shadow-lg);
}
.login-brand { display: flex; align-items: center; gap: var(--sp-2); justify-content: center; margin-bottom: var(--sp-8); }
.login-brand-text { font-size: var(--text-xl); font-weight: 700; }
.login-title { text-align: center; font-size: var(--text-lg); font-weight: 600; margin-bottom: var(--sp-1); }
.login-subtitle { text-align: center; font-size: var(--text-sm); color: var(--text-secondary); margin-bottom: var(--sp-8); }
.login-form { max-width: 100%; }
.login-btn { width: 100%; height: 44px; font-size: var(--text-base); }

@media (max-width: 480px) {
  .login-page { padding: 0 var(--sp-4); }
  .login-card {
    width: 100%;
    padding: var(--sp-8) var(--sp-5);
  }
}
</style>
