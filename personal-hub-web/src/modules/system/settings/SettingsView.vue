<script setup lang="ts">
import { useLayoutStore } from '@/store/layoutStore'
import { ElMessageBox, ElMessage } from 'element-plus'
import { Settings, Menu, LayoutDashboard, BookOpen, RotateCcw } from 'lucide-vue-next'
import PageHeader from '@/components/PageHeader.vue'
import MenuManager from './components/MenuManager.vue'
import DashboardManager from './components/DashboardManager.vue'
import ReadingExperience from './components/ReadingExperience.vue'

const layoutStore = useLayoutStore()

async function handleResetAll() {
  try {
    await ElMessageBox.confirm('确定恢复所有布局为默认设置吗？', '恢复默认', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning',
    })
    await layoutStore.resetAll()
    ElMessage.success('已恢复默认布局')
  } catch { /* cancelled */ }
}
</script>

<template>
  <div class="settings-page">
    <PageHeader title="系统设置" subtitle="自定义你的工作台布局">
      <template #actions>
        <el-button type="danger" plain size="small" @click="handleResetAll">
          <RotateCcw :size="14" />
          恢复默认
        </el-button>
      </template>
    </PageHeader>

    <div class="settings-tabs">
      <el-tabs class="layout-tabs">
        <el-tab-pane>
          <template #label>
            <span class="tab-label">
              <Menu :size="16" />
              菜单管理
            </span>
          </template>
          <MenuManager />
        </el-tab-pane>
        <el-tab-pane>
          <template #label>
            <span class="tab-label">
              <LayoutDashboard :size="16" />
              Dashboard 布局
            </span>
          </template>
          <DashboardManager />
        </el-tab-pane>
        <el-tab-pane>
          <template #label>
            <span class="tab-label">
              <BookOpen :size="16" />
              阅读体验
            </span>
          </template>
          <ReadingExperience />
        </el-tab-pane>
      </el-tabs>
    </div>
  </div>
</template>

<style scoped>
.settings-page { max-width: 800px; }
.settings-tabs { margin-top: var(--sp-6); }
.layout-tabs { min-height: 400px; }
.tab-label { display: flex; align-items: center; gap: var(--sp-2); font-size: var(--text-sm); }
</style>
