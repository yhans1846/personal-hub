<script setup lang="ts">
import { ref } from 'vue'
import { useLayoutStore } from '@/store/layoutStore'
import { ElMessageBox, ElMessage } from 'element-plus'
import { LayoutDashboard, BookOpen, Palette, Settings2, RotateCcw } from 'lucide-vue-next'
import PageHeader from '@/components/PageHeader.vue'
import UiCard from '@/components/ui/UiCard.vue'
import MenuManager from './components/MenuManager.vue'
import DashboardManager from './components/DashboardManager.vue'
import ReadingExperience from './components/ReadingExperience.vue'
import AppearanceSettings from './components/AppearanceSettings.vue'
import AdvancedSettings from './components/AdvancedSettings.vue'

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

// 自动保存指示（其他组件修改后触发）
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
          <div v-if="activeTab === 'workspace'" class="workspace-grid">
            <UiCard class="settings-card">
              <h3 class="card-title">菜单管理</h3>
              <MenuManager />
            </UiCard>
            <UiCard class="settings-card">
              <h3 class="card-title">Dashboard 卡片</h3>
              <DashboardManager />
            </UiCard>
          </div>

          <!-- 阅读 -->
          <div v-else-if="activeTab === 'reading'">
            <UiCard class="settings-card">
              <h3 class="card-title">阅读设置</h3>
              <ReadingExperience />
            </UiCard>
          </div>

          <!-- 外观 -->
          <div v-else-if="activeTab === 'appearance'">
            <UiCard class="settings-card">
              <h3 class="card-title">外观</h3>
              <AppearanceSettings />
            </UiCard>
          </div>

          <!-- 高级 -->
          <div v-else-if="activeTab === 'advanced'">
            <AdvancedSettings />
          </div>
        </div>
      </Transition>
    </div>

    <!-- 底部自动保存指示 -->
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
  max-width: 1200px;
  margin: 0 auto;
  width: 100%;
}

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

/* ─── 内容 ─── */
.settings-content {
  margin-top: var(--sp-6);
  min-height: 300px;
}

.settings-card {
  margin-bottom: 20px;
}

/* Card 标题（去掉下边距，内容直接跟上） */
.card-header {
  width: 100%;
}
.card-title {
  margin: 0 0 12px;
  font-size: 15px;
  font-weight: 700;
  color: var(--text-primary);
}

/* ─── 两栏布局 ─── */
.workspace-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 20px;
  align-items: start;
}
.workspace-grid > .settings-card {
  margin-bottom: 0;
  min-width: 0;
}

/* ─── 自动保存条 ─── */
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

/* ─── Tab 切换动画 ─── */
.tab-fade-enter-active,
.tab-fade-leave-active {
  transition: opacity 150ms ease, transform 150ms ease;
}
.tab-fade-enter-from { opacity: 0; transform: translateY(6px); }
.tab-fade-leave-to { opacity: 0; transform: translateY(-6px); }

/* ─── 保存指示动画 ─── */
.save-fade-enter-active,
.save-fade-leave-active { transition: opacity 300ms ease; }
.save-fade-enter-from,
.save-fade-leave-to { opacity: 0; }
</style>
