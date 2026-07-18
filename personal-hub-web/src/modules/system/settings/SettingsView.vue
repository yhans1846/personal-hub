<script setup lang="ts">
import { ref } from 'vue'
import { useLayoutStore } from '@/store/layoutStore'
import { ElMessageBox, ElMessage } from 'element-plus'
import { LayoutDashboard, BookOpen, Palette, Settings2, RotateCcw } from 'lucide-vue-next'
import PageHeader from '@/components/PageHeader.vue'
import UiCard from '@/components/ui/UiCard.vue'
import MenuManager from './components/MenuManager.vue'
import DashboardManager from './components/DashboardManager.vue'
import StatsManager from './components/StatsManager.vue'
import ReadingExperience from './components/ReadingExperience.vue'
import AppearanceSettings from './components/AppearanceSettings.vue'
import AdvancedSettings from './components/AdvancedSettings.vue'
import './settings-layout.css'

const layoutStore = useLayoutStore()

interface TabItem {
  key: string
  icon: any
  label: string
}

const tabs: TabItem[] = [
  { key: 'workspace', icon: LayoutDashboard, label: '工作台' },
  { key: 'reading', icon: BookOpen, label: '阅读' },
  { key: 'appearance', icon: Palette, label: '外观' },
  { key: 'advanced', icon: Settings2, label: '高级' },
]

const activeTab = ref('workspace')
const showSaveHint = ref(false)

async function handleResetAll() {
  try {
    await ElMessageBox.confirm('确定恢复所有设置为默认值吗？', '恢复默认', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning',
    })
    await layoutStore.resetAll()
    await layoutStore.resetAppearanceConfig()
    ElMessage.success('已恢复所有默认设置')
  } catch { /* cancelled */ }
}

function onAutoSaved() {
  showSaveHint.value = true
  setTimeout(() => { showSaveHint.value = false }, 2000)
}
</script>

<template>
  <div class="settings-page">
    <PageHeader title="系统设置" subtitle="自定义你的工作台">
      <template #actions>
        <el-button type="danger" plain size="small" @click="handleResetAll">
          <RotateCcw :size="14" />
          恢复所有默认
        </el-button>
      </template>
    </PageHeader>

    <div class="settings-tabs-nav">
      <button
        v-for="tab in tabs"
        :key="tab.key"
        :class="['tab-btn', { active: activeTab === tab.key }]"
        @click="activeTab = tab.key"
      >
        <component :is="tab.icon" :size="16" />
        <span>{{ tab.label }}</span>
      </button>
    </div>

    <div class="settings-content">
      <Transition name="tab-fade" mode="out-in">
        <div class="tab-pane settings-stack" :key="activeTab">
          <!-- 工作台 -->
          <template v-if="activeTab === 'workspace'">
            <UiCard class="settings-card">
              <h3 class="card-title">菜单管理</h3>
              <MenuManager />
            </UiCard>
            <UiCard class="settings-card">
              <h3 class="card-title">Dashboard 卡片</h3>
              <DashboardManager />
            </UiCard>
            <UiCard class="settings-card">
              <h3 class="card-title">统计卡片</h3>
              <StatsManager />
            </UiCard>
          </template>

          <!-- 阅读 -->
          <UiCard v-else-if="activeTab === 'reading'" class="settings-card">
            <h3 class="card-title">阅读设置</h3>
            <ReadingExperience />
          </UiCard>

          <!-- 外观 -->
          <UiCard v-else-if="activeTab === 'appearance'" class="settings-card">
            <h3 class="card-title">外观</h3>
            <AppearanceSettings />
          </UiCard>

          <!-- 高级 -->
          <AdvancedSettings v-else-if="activeTab === 'advanced'" />
        </div>
      </Transition>
    </div>

    <Transition name="save-fade">
      <div v-if="showSaveHint" class="auto-save-bar">
        <span class="auto-save-dot" />
        已自动保存
      </div>
    </Transition>
  </div>
</template>

<style scoped>
.settings-page {
  width: 100%;
}

.settings-tabs-nav {
  display: flex;
  gap: 4px;
  margin-top: var(--sp-6);
  padding-bottom: 12px;
  border-bottom: 1px solid var(--border-light);
  overflow-x: auto;
  flex-shrink: 0;
}

.tab-btn {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 8px 16px;
  font-size: 13px;
  font-weight: 500;
  color: var(--text-tertiary);
  background: transparent;
  border: none;
  border-radius: var(--radius-md);
  cursor: pointer;
  white-space: nowrap;
  transition: all var(--transition);
  position: relative;
}
.tab-btn:hover { color: var(--text-secondary); background: var(--bg-hover); }
.tab-btn.active {
  color: var(--accent);
  background: color-mix(in srgb, var(--accent) 8%, transparent);
}
.tab-btn.active::after {
  content: '';
  position: absolute;
  bottom: -13px;
  left: 50%;
  transform: translateX(-50%);
  width: 60%;
  height: 2px;
  background: var(--accent);
  border-radius: 1px;
}

.settings-content {
  margin-top: var(--sp-6);
  min-height: 300px;
}

.auto-save-bar {
  display: flex;
  align-items: center;
  gap: 6px;
  justify-content: center;
  padding: 10px 0;
  margin-top: 24px;
  font-size: 12px;
  color: var(--text-tertiary);
  border-top: 1px solid var(--border-light);
}
.auto-save-dot {
  width: 6px;
  height: 6px;
  border-radius: 50%;
  background: var(--success);
}

.tab-fade-enter-active,
.tab-fade-leave-active {
  transition: opacity var(--transition-duration) ease, transform var(--transition-duration) ease;
}
.tab-fade-enter-from { opacity: 0; transform: translateY(6px); }
.tab-fade-leave-to { opacity: 0; transform: translateY(-6px); }

.save-fade-enter-active,
.save-fade-leave-active { transition: opacity var(--transition-duration) ease; }
.save-fade-enter-from,
.save-fade-leave-to { opacity: 0; }
</style>
