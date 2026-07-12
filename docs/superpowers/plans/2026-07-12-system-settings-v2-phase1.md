# 系统设置 V2 Phase 1 实现计划

> **面向 AI 代理的工作者：** 必需子技能：使用 subagent-driven-development（推荐）或 executing-plans 逐任务实现此计划。步骤使用复选框（`- [ ]`）语法来跟踪进度。

**目标：** 将系统设置页从 V1 升级到 V2 视觉规范，新增外观模块（主题/强调色迁移）、缓存管理模块、Tab 导航升级，现有 3 个模块 UI 对齐 Card/Section 规范。

**架构：** SettingsView 作为容器编排 6 个 Tab（3 个现有改造 + 2 个新建 + 1 个占位），每个 Tab 为独立组件。外观配置通过 localStorage 同步到 data-theme/data-accent 属性。阅读配置扩展字段存在 readingConfigStore。

**技术栈：** Vue 3 + TypeScript + Pinia + Element Plus + Lucide Vue Next + SortableJS

---

## 文件结构

### 新建
| 文件 | 职责 |
|------|------|
| `components/AppearanceSettings.vue` | 外观模块：主题切换(light/dark) + 强调色选择(5色) |
| `components/CacheSettings.vue` | 缓存管理：缓存大小估算 + 清空缓存 |
| `components/PlaceholderTab.vue` | 通用占位 Tab：空状态 + "即将上线"提示，复用给通知和实验功能 |

### 修改
| 文件 | 职责 |
|------|------|
| `SettingsView.vue` | 全面重写：Tab 导航从 el-tabs 升级为自定义 Icon+Label 样式，编排 6 个 Tab，自身 CSS 对齐 V2 规范 |
| `components/MenuManager.vue` | UI 改造：对齐 Card/Section 规范（24px padding、12px 圆角、Section 分割线） |
| `components/DashboardManager.vue` | UI 改造：与 MenuManager 保持一致的 Card/Section 视觉 |
| `components/ReadingExperience.vue` | 扩展：新增段距、代码字体、代码字号三个配置项，对齐 Card/Section 规范 |
| `store/readingConfigStore.ts` | 扩展：新增 paragraphGap / codeFontSize / codeFontFamily 字段、默认值、CSS 变量映射 |
| `store/layoutStore.ts` | 扩展：新增 saveAppearanceConfig / resetAppearanceConfig 方法 |
| `types/layout.ts` | 扩展：新增 AppearanceConfig 接口 |
| `AppLayout.vue` | 微调：外观入口引导至设置页，保留快速切换下拉 |

### 不变
| 文件 | 说明 |
|------|------|
| `api/layoutApi.ts` | 现有接口通用支持新 layout_type |
| 后端所有文件 | 无变更 |

---

### 任务 1：扩展类型定义

**文件：** 修改 `types/layout.ts`

- [ ] **步骤 1：添加 AppearanceConfig 类型**

在 `types/layout.ts` 末尾追加：

```ts
/** 外观配置 */
export interface AppearanceConfig {
  theme: 'light' | 'dark'
  accent: 'blue' | 'purple' | 'green' | 'orange' | 'cyan'
}
```

- [ ] **步骤 2：提交**

```bash
git add personal-hub-web/src/types/layout.ts
git commit -m "chore: 新增 AppearanceConfig 类型定义"
```

---

### 任务 2：扩展 readingConfigStore

**文件：** 修改 `store/readingConfigStore.ts`

- [ ] **步骤 1：在 ReadingConfig 中新增三个字段**

找到 `ReadingConfig` 接口，追加：

```ts
export interface ReadingConfig {
  // ...现有字段...
  paragraphGap: number          // 段距: 1.0 / 1.2 / 1.5 (em)
  codeFontSize: string          // 代码字号: 'same' | '13px' | '14px'
  codeFontFamily: string        // 代码字体: 'system' | 'monospace'
}
```

- [ ] **步骤 2：更新 DEFAULTS 常量**

```ts
const DEFAULTS: ReadingConfig = {
  // ...现有默认值保持不变...
  paragraphGap: 1.2,
  codeFontSize: 'same',
  codeFontFamily: 'monospace',
}
```

- [ ] **步骤 3：在 ReadingConfigStore 返回值中添加 CSS 变量辅助方法**

在 `return` 前添加：

```ts
/** 生成 CSS 变量映射，供预览页使用 */
const cssVars = computed(() => ({
  '--md-paragraph-gap': `${config.paragraphGap}em`,
  '--md-code-font-size': config.codeFontSize === 'same' ? 'inherit' : config.codeFontSize,
  '--md-code-font-family': config.codeFontFamily === 'system' ? 'inherit' : 'var(--font-mono)',
}))

return {
  config, loaded, cssVars, // ← 新增 cssVars
  updateConfig, resetConfig, fetchFromBackend,
}
```

- [ ] **步骤 4：提交**

```bash
git add personal-hub-web/src/store/readingConfigStore.ts
git commit -m "feat: readingConfigStore 新增段距/代码字体/代码字号字段"
```

---

### 任务 3：扩展 layoutStore

**文件：** 修改 `store/layoutStore.ts`

- [ ] **步骤 1：导入 AppearanceConfig 类型**

在文件顶部 import 后追加：

```ts
import type { MenuItem, CardItem, LayoutItem, AppearanceConfig } from '@/types/layout'
```

- [ ] **步骤 2：新增外观配置状态和持久化方法**

在 `return` 前追加：

```ts
// ---- 外观配置 ----
const STORAGE_KEY_APPEARANCE = 'appearance-config'

const DEFAULT_APPEARANCE: AppearanceConfig = {
  theme: 'light',
  accent: 'blue',
}

const appearanceConfig = ref<AppearanceConfig>(loadAppearance())

function loadAppearance(): AppearanceConfig {
  try {
    const stored = localStorage.getItem(STORAGE_KEY_APPEARANCE)
    if (stored) return { ...DEFAULT_APPEARANCE, ...JSON.parse(stored) }
  } catch { /* ignore */ }
  return { ...DEFAULT_APPEARANCE }
}

function saveAppearanceLocally(config: AppearanceConfig) {
  localStorage.setItem(STORAGE_KEY_APPEARANCE, JSON.stringify(config))
}

async function fetchAppearanceFromBackend() {
  try {
    const res = await getLayoutAll()
    const appLayout = (res.data.data as any[]).find((l: any) => l.layoutType === 'appearance')
    if (appLayout?.layoutJson) {
      const parsed = JSON.parse(appLayout.layoutJson)
      Object.assign(appearanceConfig.value, { ...DEFAULT_APPEARANCE, ...parsed })
      saveAppearanceLocally(appearanceConfig.value)
      applyAppearanceToDOM(appearanceConfig.value)
    }
  } catch { /* ignore */ }
}

function applyAppearanceToDOM(config: AppearanceConfig) {
  document.documentElement.setAttribute('data-theme', config.theme)
  document.documentElement.setAttribute('data-accent', config.accent)
  localStorage.setItem('theme-preference', config.theme)
  localStorage.setItem('accent-preference', config.accent)
}

async function saveAppearanceConfig(config: AppearanceConfig) {
  appearanceConfig.value = config
  saveAppearanceLocally(config)
  applyAppearanceToDOM(config)
  await saveLayout({
    layoutType: 'appearance',
    layoutJson: JSON.stringify({ theme: config.theme, accent: config.accent }),
  })
}

async function resetAppearanceConfig() {
  await saveAppearanceConfig({ ...DEFAULT_APPEARANCE })
  // 同步顶栏的响应式状态
  if ((window as any).__setTheme) (window as any).__setTheme(DEFAULT_APPEARANCE.theme)
}
```

- [ ] **步骤 3：在 fetchLayout 末尾追加 appearance 加载**

找到 `fetchLayout` 函数的 `finally` 块前，在 `}` 后追加：

```ts
await fetchAppearanceFromBackend()
```

- [ ] **步骤 4：更新 return 导出**

在 return 对象中追加：

```ts
appearanceConfig, DEFAULT_APPEARANCE,
saveAppearanceConfig, resetAppearanceConfig,
```

- [ ] **步骤 5：提交**

```bash
git add personal-hub-web/src/store/layoutStore.ts
git commit -m "feat: layoutStore 新增外观配置状态和持久化"
```

---

### 任务 4：实现通用占位 Tab 组件

**文件：** 新建 `components/PlaceholderTab.vue`

- [ ] **步骤 1：创建 PlaceholderTab.vue**

```vue
<script setup lang="ts">
defineProps<{
  icon: string
  title: string
  description: string
  hint?: string
}>()
</script>

<template>
  <div class="placeholder-tab">
    <div class="placeholder-content">
      <span class="placeholder-icon">{{ icon }}</span>
      <h3 class="placeholder-title">{{ title }}</h3>
      <p class="placeholder-desc">{{ description }}</p>
      <p v-if="hint" class="placeholder-hint">{{ hint }}</p>
    </div>
  </div>
</template>

<style scoped>
.placeholder-tab {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 300px;
}
.placeholder-content {
  text-align: center;
  max-width: 360px;
}
.placeholder-icon {
  font-size: 48px;
  display: block;
  margin-bottom: var(--sp-4);
  opacity: 0.6;
}
.placeholder-title {
  margin: 0 0 var(--sp-2);
  font-size: var(--text-lg);
  font-weight: 600;
  color: var(--text-primary);
}
.placeholder-desc {
  margin: 0;
  font-size: var(--text-sm);
  color: var(--text-tertiary);
  line-height: 1.6;
}
.placeholder-hint {
  margin: var(--sp-2) 0 0;
  font-size: var(--text-xs);
  color: var(--text-placeholder);
}
</style>
```

- [ ] **步骤 2：提交**

```bash
git add personal-hub-web/src/modules/system/settings/components/PlaceholderTab.vue
git commit -m "feat: 添加通用占位 Tab 组件 (PlaceholderTab)"
```

---

### 任务 5：实现外观模块组件

**文件：** 新建 `components/AppearanceSettings.vue`

- [ ] **步骤 1：创建 AppearanceSettings.vue**

```vue
<script setup lang="ts">
import { useLayoutStore } from '@/store/layoutStore'
import { ElMessage } from 'element-plus'
import { storeToRefs } from 'pinia'
import { Sun, Moon, RotateCcw } from 'lucide-vue-next'

const layoutStore = useLayoutStore()
const { appearanceConfig } = storeToRefs(layoutStore)

const ACCENT_OPTIONS = [
  { key: 'blue', label: '蓝色', color: '#4F7BFF' },
  { key: 'purple', label: '紫色', color: '#8B5CF6' },
  { key: 'green', label: '绿色', color: '#10B981' },
  { key: 'orange', label: '橙色', color: '#F97316' },
  { key: 'cyan', label: '青色', color: '#06B6D4' },
]

function setTheme(theme: 'light' | 'dark') {
  layoutStore.saveAppearanceConfig({ ...appearanceConfig.value, theme })
}

function setAccent(key: string) {
  layoutStore.saveAppearanceConfig({ ...appearanceConfig.value, accent: key as any })
}

async function handleReset() {
  await layoutStore.resetAppearanceConfig()
  ElMessage.success('外观已恢复默认')
}
</script>

<template>
  <div class="appearance-settings">
    <!-- 主题 -->
    <section class="setting-section">
      <h3 class="section-title">主题</h3>
      <div class="theme-cards">
        <button
          :class="['theme-card', { active: appearanceConfig.theme === 'light' }]"
          @click="setTheme('light')"
        >
          <Sun :size="20" class="theme-card-icon" />
          <div class="theme-card-info">
            <span class="theme-card-name">浅色</span>
            <span class="theme-card-desc">白底深色文字</span>
          </div>
        </button>
        <button
          :class="['theme-card', { active: appearanceConfig.theme === 'dark' }]"
          @click="setTheme('dark')"
        >
          <Moon :size="20" class="theme-card-icon" />
          <div class="theme-card-info">
            <span class="theme-card-name">深色</span>
            <span class="theme-card-desc">深底浅色文字</span>
          </div>
        </button>
      </div>
    </section>

    <!-- 强调色 -->
    <section class="setting-section">
      <h3 class="section-title">强调色</h3>
      <div class="accent-grid">
        <button
          v-for="opt in ACCENT_OPTIONS"
          :key="opt.key"
          :class="['accent-btn', { active: appearanceConfig.accent === opt.key }]"
          :style="{ '--accent-color': opt.color }"
          @click="setAccent(opt.key)"
        >
          <span class="accent-dot" :style="{ background: opt.color }" />
          <span class="accent-label">{{ opt.label }}</span>
        </button>
      </div>
    </section>

    <!-- 恢复默认 -->
    <div class="reset-area">
      <button class="reset-btn" @click="handleReset">
        <RotateCcw :size="14" />
        恢复默认
      </button>
    </div>
  </div>
</template>

<style scoped>
.appearance-settings { max-width: 520px; }

.setting-section { margin-bottom: 28px; }

.section-title {
  margin: 0 0 14px;
  font-size: 13px;
  font-weight: 600;
  color: var(--text-secondary);
}

/* 主题卡片 */
.theme-cards { display: grid; grid-template-columns: 1fr 1fr; gap: 8px; }
.theme-card {
  display: flex; align-items: center; gap: 10px;
  padding: 14px 16px;
  border: 1px solid var(--border-color);
  background: var(--bg-card);
  border-radius: 8px;
  cursor: pointer;
  text-align: left;
  transition: all 150ms ease;
}
.theme-card:hover { border-color: var(--accent-border); background: var(--bg-hover); }
.theme-card.active {
  border-color: var(--accent);
  background: color-mix(in srgb, var(--accent) 8%, transparent);
  box-shadow: 0 0 0 1px var(--accent);
}
.theme-card-icon { flex-shrink: 0; color: var(--text-secondary); }
.theme-card.active .theme-card-icon { color: var(--accent); }
.theme-card-info { display: flex; flex-direction: column; gap: 2px; }
.theme-card-name { font-size: 13px; font-weight: 500; color: var(--text-primary); }
.theme-card-desc { font-size: 11px; color: var(--text-tertiary); }

/* 强调色 */
.accent-grid { display: flex; gap: 8px; flex-wrap: wrap; }
.accent-btn {
  display: flex; align-items: center; gap: 8px;
  padding: 8px 14px;
  border: 1px solid var(--border-color);
  background: var(--bg-card);
  border-radius: 8px;
  cursor: pointer;
  transition: all 150ms ease;
}
.accent-btn:hover { border-color: var(--accent-border); background: var(--bg-hover); }
.accent-btn.active {
  border-color: var(--accent-color);
  background: color-mix(in srgb, var(--accent-color) 10%, transparent);
}
.accent-dot {
  width: 14px; height: 14px; border-radius: 50%; flex-shrink: 0;
}
.accent-label { font-size: 13px; color: var(--text-primary); }
.accent-btn.active .accent-label { color: var(--accent-color); font-weight: 500; }

/* 恢复默认 */
.reset-area { padding-top: 12px; border-top: 1px solid var(--border-light); }
.reset-btn {
  display: inline-flex; align-items: center; gap: 6px;
  padding: 8px 16px; font-size: 13px; color: var(--text-tertiary);
  border: 1px solid var(--border-color); background: var(--bg-card);
  border-radius: 6px; cursor: pointer; transition: all 150ms ease;
}
.reset-btn:hover { color: var(--danger); border-color: var(--danger); background: var(--danger-light); }
</style>
```

- [ ] **步骤 2：提交**

```bash
git add personal-hub-web/src/modules/system/settings/components/AppearanceSettings.vue
git commit -m "feat: 新增外观设置模块 (AppearanceSettings)"
```

---

### 任务 6：实现缓存管理组件

**文件：** 新建 `components/CacheSettings.vue`

- [ ] **步骤 1：创建 CacheSettings.vue**

```vue
<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { RotateCcw } from 'lucide-vue-next'

const cacheSize = ref('计算中...')
const PROTECTED_KEYS = ['token', 'theme-preference', 'accent-preference', 'appearance-config']

function estimateCacheSize(): string {
  let totalBytes = 0
  for (let i = 0; i < localStorage.length; i++) {
    const key = localStorage.key(i)
    if (key && !PROTECTED_KEYS.includes(key)) {
      totalBytes += (localStorage.getItem(key) || '').length * 2 // UTF-16
    }
  }
  if (totalBytes === 0) return '约 0 KB'
  if (totalBytes < 1024) return `约 ${totalBytes} B`
  if (totalBytes < 1024 * 1024) return `约 ${(totalBytes / 1024).toFixed(1)} KB`
  return `约 ${(totalBytes / 1024 / 1024).toFixed(1)} MB`
}

async function handleClearCache() {
  try {
    await ElMessageBox.confirm('清空缓存后，部分页面首次加载可能略慢，数据不会丢失。', '清空缓存', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'info',
    })
    const sizeBefore = estimateCacheSize()
    const keysToRemove: string[] = []
    for (let i = 0; i < localStorage.length; i++) {
      const key = localStorage.key(i)
      if (key && !PROTECTED_KEYS.includes(key)) keysToRemove.push(key)
    }
    keysToRemove.forEach(key => localStorage.removeItem(key))
    cacheSize.value = estimateCacheSize()
    ElMessage.success(`已清空缓存（${sizeBefore}）`)
  } catch { /* cancelled */ }
}

onMounted(() => { cacheSize.value = estimateCacheSize() })
</script>

<template>
  <div class="cache-settings">
    <section class="setting-section">
      <h3 class="section-title">本地缓存</h3>
      <div class="cache-info">
        <p class="cache-detail">
          当前缓存数据：<strong>{{ cacheSize }}</strong>
        </p>
        <p class="cache-hint">
          缓存用于加速页面加载。清空后系统会自动重建缓存，登录状态和个性化设置不会丢失。
        </p>
      </div>
      <button class="action-btn" @click="handleClearCache">
        <RotateCcw :size="14" />
        清空缓存
      </button>
    </section>

    <section class="setting-section">
      <h3 class="section-title">数据备份与导出</h3>
      <div class="disabled-area">
        <button class="action-btn" disabled>导出数据</button>
        <button class="action-btn" disabled>导入数据</button>
        <p class="disabled-hint">数据备份与导出功能即将上线，敬请期待。</p>
      </div>
    </section>
  </div>
</template>

<style scoped>
.cache-settings { max-width: 520px; }
.setting-section { margin-bottom: 28px; }
.section-title {
  margin: 0 0 14px;
  font-size: 13px;
  font-weight: 600;
  color: var(--text-secondary);
}
.cache-info { margin-bottom: 16px; }
.cache-detail { margin: 0 0 6px; font-size: var(--text-sm); color: var(--text-primary); }
.cache-hint { margin: 0; font-size: var(--text-xs); color: var(--text-tertiary); line-height: 1.5; }

.action-btn {
  display: inline-flex; align-items: center; gap: 6px;
  padding: 8px 16px; font-size: 13px;
  border: 1px solid var(--border-color); background: var(--bg-card);
  border-radius: 6px; cursor: pointer;
  color: var(--text-secondary); transition: all 150ms ease;
  margin-right: 8px; margin-bottom: 8px;
}
.action-btn:hover:not(:disabled) { border-color: var(--accent); color: var(--accent); }
.action-btn:disabled { opacity: 0.4; cursor: not-allowed; }

.disabled-hint {
  margin: 8px 0 0; font-size: var(--text-xs); color: var(--text-placeholder);
}
</style>
```

- [ ] **步骤 2：提交**

```bash
git add personal-hub-web/src/modules/system/settings/components/CacheSettings.vue
git commit -m "feat: 新增缓存管理模块 (CacheSettings)"
```

---

### 任务 7：重写 SettingsView（核心容器）

**文件：** 修改 `SettingsView.vue`

- [ ] **步骤 1：完全重写 SettingsView.vue**

```vue
<script setup lang="ts">
import { useLayoutStore } from '@/store/layoutStore'
import { ElMessageBox, ElMessage } from 'element-plus'
import { LayoutDashboard, BookOpen, Palette, Bell, HardDrive, FlaskConical, RotateCcw } from 'lucide-vue-next'
import PageHeader from '@/components/PageHeader.vue'
import MenuManager from './components/MenuManager.vue'
import DashboardManager from './components/DashboardManager.vue'
import ReadingExperience from './components/ReadingExperience.vue'
import AppearanceSettings from './components/AppearanceSettings.vue'
import CacheSettings from './components/CacheSettings.vue'
import PlaceholderTab from './components/PlaceholderTab.vue'

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

const activeTab = defineModel<string>('activeTab', { default: 'workspace' })

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
            <UiCard title="菜单管理">
              <MenuManager />
            </UiCard>
            <UiCard title="Dashboard 卡片" class="spacer-top">
              <DashboardManager />
            </UiCard>
          </div>

          <!-- 阅读体验 -->
          <div v-else-if="activeTab === 'reading'" class="tab-inner">
            <UiCard title="阅读设置">
              <ReadingExperience />
            </UiCard>
          </div>

          <!-- 外观 -->
          <div v-else-if="activeTab === 'appearance'" class="tab-inner">
            <UiCard title="外观">
              <AppearanceSettings />
            </UiCard>
          </div>

          <!-- 通知（占位） -->
          <div v-else-if="activeTab === 'notification'" class="tab-inner">
            <PlaceholderTab
              icon="🔔"
              title="通知设置即将上线"
              description="你可以在这里管理桌面通知和提醒偏好。"
              hint="Phase 2 规划中"
            />
          </div>

          <!-- 数据 -->
          <div v-else-if="activeTab === 'data'" class="tab-inner">
            <UiCard title="缓存管理">
              <CacheSettings />
            </UiCard>
          </div>

          <!-- 实验功能（占位） -->
          <div v-else-if="activeTab === 'experimental'" class="tab-inner">
            <PlaceholderTab
              icon="🧪"
              title="实验功能即将上线"
              description="在这里开启和关闭正在实验中的 Beta 功能。"
              hint="Phase 2 规划中"
            />
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
```

- [ ] **步骤 2：提交**

```bash
git add personal-hub-web/src/modules/system/settings/SettingsView.vue
git commit -m "feat: 重写 SettingsView — 自定义 Tab 导航 + V2 布局"
```

---

### 任务 8：改造 MenuManager UI

**文件：** 修改 `components/MenuManager.vue`

- [ ] **步骤 1：对齐 Card/Section 视觉规范**

将外部容器样式改为符合 Card/Section 规范。由于 MenuManager 被放在 UiCard 内，需要去除自身的外边距和边框。修改 `<style scoped>`：

替换整个 style 块中 `.menu-groups` 及之后的样式，确保：
- 分组标题使用 13px/600w/`var(--text-secondary)`（对齐 Section 规范）
- 分组间距 32px
- 控件间距 16px
- 恢复默认按钮样式与 CacheSettings 保持一致

修改点：
1. `.manager-toolbar` — 保留，只调整 margin-bottom 为 16px
2. `.menu-groups` — gap 改为 32px
3. `.group-header` — 字体 13px/600w/`var(--text-secondary)`，移除大写
4. `.group-header::after` — 改为与 ReadingExperience 一致的 Section 分割线风格

```css
/* 覆盖 style 块 */
.manager-toolbar { display: flex; justify-content: space-between; align-items: center; margin-bottom: var(--sp-4); }
.manager-hint { font-size: var(--text-xs); color: var(--text-tertiary); }
.menu-groups { display: flex; flex-direction: column; gap: 32px; }

.group-header {
  display: flex; align-items: center; gap: 6px;
  font-size: 13px; font-weight: 600;
  color: var(--text-secondary); padding: 0 0 8px 0;
  cursor: pointer; user-select: none;
  border-bottom: 1px solid var(--border-light);
  transition: color 150ms ease;
}
.group-header:hover { color: var(--text-primary); }
.group-chevron {
  flex-shrink: 0; transition: transform 200ms ease; opacity: 0.45;
}
.group-header:hover .group-chevron { opacity: 0.8; }
.group-header.collapsed .group-chevron { transform: rotate(-90deg); }

.group-items { display: flex; flex-direction: column; gap: var(--sp-1); padding-top: 8px; }

.menu-item { ... } /* 保持不变 */
.menu-item--hidden { opacity: 0.5; }
.menu-item--fixed { border-left: 3px solid var(--accent); }
.menu-item-title { flex: 1; font-size: var(--text-sm); }
.drag-handle { cursor: grab; color: var(--text-tertiary); display: flex; }
.drag-handle:active { cursor: grabbing; }
.vis-toggle { ... } /* 保持不变 */
.menu-item--ghost { opacity: 0.3; background: var(--accent-light); }

/* 折叠动画 */
.collapse-enter-active,
.collapse-leave-active {
  transition: max-height 200ms ease, opacity 200ms ease;
  overflow: hidden;
}
.collapse-enter-from,
.collapse-leave-to { max-height: 0; opacity: 0; }
.collapse-enter-to,
.collapse-leave-from { max-height: 400px; opacity: 1; }
```

- [ ] **步骤 2：提交**

```bash
git add personal-hub-web/src/modules/system/settings/components/MenuManager.vue
git commit -m "refactor: MenuManager 对齐 Card/Section 规范"
```

---

### 任务 9：改造 DashboardManager UI

**文件：** 修改 `components/DashboardManager.vue`

- [ ] **步骤 1：对齐 Card/Section 规范**

与 MenuManager 相同的改造原则。修改 `<style scoped>`：

```css
.manager-toolbar { display: flex; justify-content: space-between; align-items: center; margin-bottom: var(--sp-4); }
.manager-hint { font-size: var(--text-xs); color: var(--text-tertiary); }
.card-list { display: flex; flex-direction: column; gap: var(--sp-1); }

.card-item {
  display: flex; align-items: center; gap: var(--sp-3);
  padding: var(--sp-2) var(--sp-3);
  border-radius: var(--radius-sm);
  transition: background var(--transition);
  cursor: default;
}
.card-item:hover { background: var(--bg-hover); }
.card-item--hidden { opacity: 0.5; }
.card-item-title { flex: 1; font-size: var(--text-sm); }
.drag-handle { cursor: grab; color: var(--text-tertiary); display: flex; }
.drag-handle:active { cursor: grabbing; }
.vis-toggle { ... } /* 与 MenuManager 一致 */
.card-item--ghost { opacity: 0.3; background: var(--accent-light); }
```

关键改动：
- `.manager-toolbar` margin-bottom 从 `var(--sp-3)` 改为 `var(--sp-4)`（16px）
- 移除 `.card-list` 的外部边框/背景（由父级 UiCard 提供）
- 其他交互样式与 MenuManager 保持一致

- [ ] **步骤 2：提交**

```bash
git add personal-hub-web/src/modules/system/settings/components/DashboardManager.vue
git commit -m "refactor: DashboardManager 对齐 Card/Section 规范"
```

---

### 任务 10：扩展 ReadingExperience UI

**文件：** 修改 `components/ReadingExperience.vue`

- [ ] **步骤 1：新增三个配置项 UI**

在"图片显示" section 后，"恢复默认"前插入：

```html
<!-- 段距 -->
<section class="setting-section">
  <h3 class="section-title">段落间距</h3>
  <div class="inline-options">
    <button
      v-for="gap in [1.0, 1.2, 1.5]"
      :key="gap"
      :class="['inline-btn', { active: config.paragraphGap === gap }]"
      @click="store.updateConfig({ paragraphGap: gap })"
    >{{ gap }}em</button>
  </div>
</section>

<!-- 代码字体 -->
<section class="setting-section">
  <h3 class="section-title">代码字体</h3>
  <div class="inline-options">
    <button
      :class="['inline-btn', { active: config.codeFontFamily === 'system' }]"
      @click="store.updateConfig({ codeFontFamily: 'system' })"
    >系统字体</button>
    <button
      :class="['inline-btn', { active: config.codeFontFamily === 'monospace' }]"
      @click="store.updateConfig({ codeFontFamily: 'monospace' })"
    >等宽字体</button>
  </div>
</section>

<!-- 代码字号 -->
<section class="setting-section">
  <h3 class="section-title">代码字号</h3>
  <div class="inline-options">
    <button
      :class="['inline-btn', { active: config.codeFontSize === 'same' }]"
      @click="store.updateConfig({ codeFontSize: 'same' })"
    >同正文</button>
    <button
      :class="['inline-btn', { active: config.codeFontSize === '13px' }]"
      @click="store.updateConfig({ codeFontSize: '13px' })"
    >13px</button>
    <button
      :class="['inline-btn', { active: config.codeFontSize === '14px' }]"
      @click="store.updateConfig({ codeFontSize: '14px' })"
    >14px</button>
  </div>
</section>
```

- [ ] **步骤 2：添加 inline-options 样式**

在 style 块中追加：

```css
/* ─── 内联选项组 ─── */
.inline-options {
  display: flex;
  gap: 4px;
}

.inline-btn {
  padding: 6px 14px;
  font-size: 13px;
  border: 1px solid var(--border-color);
  background: var(--bg-card);
  border-radius: 6px;
  color: var(--text-secondary);
  cursor: pointer;
  transition: all 150ms ease;
}

.inline-btn:hover {
  border-color: var(--accent);
  color: var(--accent);
}

.inline-btn.active {
  border-color: var(--accent);
  color: var(--accent);
  background: color-mix(in srgb, var(--accent) 8%, transparent);
}
```

- [ ] **步骤 3：提交**

```bash
git add personal-hub-web/src/modules/system/settings/components/ReadingExperience.vue
git commit -m "feat: ReadingExperience 新增段距/代码字体/代码字号配置"
```

---

### 任务 11：调整 AppLayout 外观入口

**文件：** 修改 `components/AppLayout.vue`

- [ ] **步骤 1：暴露响应式 api 供 layoutStore 同步**

在 `toggleTheme` 函数前注入全局钩子：

```ts
// 暴露给 layoutStore 用于外观模块同步
;(window as any).__setTheme = (theme: string) => {
  isDark.value = theme === 'dark'
}
```

在 `setAccent` 旁添加同名的全局更新：

```ts
;(window as any).__setAccent = (key: string) => {
  currentAccent.value = key
}
```

- [ ] **步骤 2：确保顶栏强调色选择器也写入 localStorage**

`setAccent` 已经在写 localStorage，不需要额外变更。但需要确保当 layoutStore.saveAppearanceConfig 被调用时，顶栏的响应式状态也同步更新。在 `onMounted` 中监听：

```ts
import { watchEffect } from 'vue'
// 在 onMounted 之前或之后
watchEffect(() => {
  const appConfig = layoutStore.appearanceConfig
  if (appConfig) {
    if (appConfig.theme) isDark.value = appConfig.theme === 'dark'
    if (appConfig.accent) currentAccent.value = appConfig.accent
  }
})
```

注意：如果 `watchEffect` 引入导致循环，改用 `watch` 加一次性标记。实际实现时在 `onMounted` 中做一次同步即可：

```ts
// 在 onMounted 末尾追加
const appConfig = layoutStore.appearanceConfig
if (appConfig) {
  if (appConfig.theme) isDark.value = appConfig.theme === 'dark'
  if (appConfig.accent) currentAccent.value = appConfig.accent
}
```

- [ ] **步骤 3：提交**

```bash
git add personal-hub-web/src/components/AppLayout.vue
git commit -m "fix: AppLayout 同步外观配置状态"
```

---

### 任务 12：验证运行

- [ ] **步骤 1：检查编译是否通过**

```bash
cd personal-hub-web && npx vue-tsc --noEmit 2>&1 | head -30
```

预期：无类型错误

- [ ] **步骤 2：检查构建是否通过**

```bash
npx vite build 2>&1 | tail -10
```

预期：构建成功，无报错

- [ ] **步骤 3：提交最终修复（如有）**

```bash
git add -A
git commit -m "chore: Phase 1 编译修复"
```

- [ ] **步骤 4：同步文档**

更新 `docs/CHANGELOG.md`，在 `[Unreleased]` 下新增 Phase 1 条目：

```markdown
### 2026-07-12 系统设置 V2 Phase 1
- 全面重写 SettingsView：自定义 Icon+Label Tab 导航（6个Tab），内容 fadeIn 切换动画
- 新增外观设置模块：主题切换（浅色/深色）+ 强调色选择（5色），从 AppLayout 迁入设置页
- 新增缓存管理模块：缓存大小估算 + 清空缓存（保留登录态和主题偏好）
- 新增通用占位 Tab 组件（通知/实验功能占位）
- MenuManager / DashboardManager UI 对齐 Card/Section 规范（Section 分割线、间距 32px）
- ReadingExperience 扩展：段落间距、代码字体、代码字号三个新配置
- readingConfigStore 新增 paragraphGap / codeFontSize / codeFontFamily 字段 + CSS 变量映射
- layoutStore 新增 appearanceConfig 状态 + saveAppearanceConfig / resetAppearanceConfig
- 顶栏外观入口保留快速切换，状态与设置页双向同步
```

- [ ] **步骤 5：最终提交文档**

```bash
git add docs/CHANGELOG.md
git commit -m "docs: 更新 Phase 1 变更日志"
```
