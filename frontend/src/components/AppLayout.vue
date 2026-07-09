<script setup lang="ts">
// 后台布局组件 — 待 Step 3 完善
</script>

<template>
  <el-container class="app-layout">
    <el-header class="app-header">
      <span class="app-title">Personal Hub</span>
    </el-header>
    <el-container>
      <el-aside width="200px" class="app-aside">
        <el-menu router :default-active="$route.path">
          <el-menu-item index="/">
            <span>首页</span>
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
  padding: 0 24px;
}
.app-title {
  color: #fff;
  font-size: 20px;
  font-weight: bold;
}
.app-aside {
  border-right: 1px solid #e6e6e6;
}
.app-main {
  background: #f5f7fa;
  padding: 24px;
}
</style>
