<script setup lang="ts">
import { useAuthStore } from '@/store/authStore'

const authStore = useAuthStore()
</script>

<template>
  <el-container class="app-layout">
    <el-header class="app-header">
      <span class="app-title">Personal Hub</span>
      <div class="header-right">
        <span class="user-name">{{ authStore.user?.nickname || authStore.user?.username }}</span>
        <el-button text style="color: #fff" @click="authStore.logout()">退出</el-button>
      </div>
    </el-header>
    <el-container>
      <el-aside width="220px" class="app-aside">
        <el-menu router :default-active="$route.path">
          <el-menu-item index="/dashboard">
            <span>首页</span>
          </el-menu-item>
          <el-sub-menu index="notes">
            <template #title>笔记管理</template>
            <el-menu-item index="/notes">笔记列表</el-menu-item>
            <el-menu-item index="/notes/categories">分类管理</el-menu-item>
            <el-menu-item index="/notes/tags">标签管理</el-menu-item>
            <el-menu-item index="/notes/recycle">回收站</el-menu-item>
          </el-sub-menu>
          <el-menu-item index="/study-records">
            <span>学习记录</span>
          </el-menu-item>
        </el-menu>
      </el-aside>
      <el-main class="app-main">
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>

<style scoped>
.app-layout {
  height: 100vh;
}
.app-header {
  background: #409eff;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 24px;
}
.app-title {
  color: #fff;
  font-size: 20px;
  font-weight: bold;
}
.header-right {
  display: flex;
  align-items: center;
  gap: 12px;
}
.user-name {
  color: #fff;
  font-size: 14px;
}
.app-aside {
  border-right: 1px solid #e6e6e6;
}
.app-main {
  background: #f5f7fa;
  padding: 24px;
}
</style>
