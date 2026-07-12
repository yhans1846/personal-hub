<script setup lang="ts">
import { ref } from 'vue'
import { useLayoutStore } from '@/store/layoutStore'
import { ElMessageBox, ElMessage } from 'element-plus'
import { LayoutDashboard, BookOpen, Palette, Bell, HardDrive, FlaskConical, RotateCcw } from 'lucide-vue-next'
import PageHeader from '@/components/PageHeader.vue'
import UiCard from '@/components/ui/UiCard.vue'
import MenuManager from './components/MenuManager.vue'
import DashboardManager from './components/DashboardManager.vue'
import ReadingExperience from './components/ReadingExperience.vue'
import AppearanceSettings from './components/AppearanceSettings.vue'
import NotificationSettings from './components/NotificationSettings.vue'
import DataManagement from './components/DataManagement.vue'
import ExperimentalFeatures from './components/ExperimentalFeatures.vue'

const layoutStore = useLayoutStore()

interface TabItem {
  key: string
  icon: any
  label: string
}

const tabs: TabItem[] = [
  { key: 'workspace', icon: LayoutDashboard, label: '工作台' },
  { key: 'reading', icon: BookOpen, label: '阅读体验' },
  { key: 'appearance', icon: Palette, label: '外观' },
  { key: 'notification', icon: Bell, label: '通知' },
  { key: 'data', icon: HardDrive, label: '数据' },
  { key: 'experimental', icon: FlaskConical, label: '实验功能' },
]

const activeTab = ref('workspace')

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

    <!-- Tab 导航 -->
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

    <!-- Tab 内容 -->
    <div class="settings-content">
      <Transition name="tab-fade" mode="out-in">
        <div class="tab-pane" :key="activeTab">
          <!-- 工作台 -->
          <div v-if="activeTab === 'workspace'" class="tab-inner">
            <UiCard>
              <h3 class="card-title">菜单管理</h3>
              <MenuManager />
            </UiCard>
            <UiCard class="spacer-top">
              <h3 class="card-title">Dashboard 卡片</h3>
              <DashboardManager />
            </UiCard>
          </div>

          <!-- 阅读体验 -->
          <div v-else-if="activeTab === 'reading'" class="tab-inner">
            <UiCard>
              <h3 class="card-title">阅读设置</h3>
              <ReadingExperience />
            </UiCard>
          </div>

          <!-- 外观 -->
          <div v-else-if="activeTab === 'appearance'" class="tab-inner">
            <UiCard>
              <h3 class="card-title">外观</h3>
              <AppearanceSettings />
            </UiCard>
          </div>

          <!-- 通知 -->
          <div v-else-if="activeTab === 'notification'" class="tab-inner">
            <UiCard>
              <h3 class="card-title">通知偏好</h3>
              <NotificationSettings />
            </UiCard>
          </div>

          <!-- 数据 -->
          <div v-else-if="activeTab === 'data'" class="tab-inner">
            <UiCard>
              <DataManagement />
            </UiCard>
          </div>

          <!-- 实验功能 -->
          <div v-else-if="activeTab === 'experimental'" class="tab-inner">
            <UiCard>
              <h3 class="card-title">实验功能</h3>
              <ExperimentalFeatures />
            </UiCard>
          </div>
        </div>
      </Transition>
    </div>
  </div>
</template>

<style scoped>
.settings-page { max-width: 1080px; }

/* ─── Tab 导航 ─── */
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
  border-radius: 8px;
  cursor: pointer;
  white-space: nowrap;
  transition: all 150ms ease;
  position: relative;
}

.tab-btn:hover {
  color: var(--text-secondary);
  background: var(--bg-hover);
}

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

/* ─── 内容 ─── */
.settings-content {
  margin-top: var(--sp-6);
  min-height: 300px;
}

.tab-inner {
  max-width: 720px;
}

.spacer-top {
  margin-top: 24px;
}

/* UiCard 内的标题 */
.card-title {
  margin: 0 0 var(--sp-4);
  font-size: 15px;
  font-weight: 600;
  color: var(--text-primary);
}

/* ─── Tab 切换动画 ─── */
.tab-fade-enter-active,
.tab-fade-leave-active {
  transition: opacity 150ms ease, transform 150ms ease;
}
.tab-fade-enter-from {
  opacity: 0;
  transform: translateY(6px);
}
.tab-fade-leave-to {
  opacity: 0;
  transform: translateY(-6px);
}
</style>
