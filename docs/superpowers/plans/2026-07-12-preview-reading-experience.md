# 预览页面阅读体验优化 实现计划

> **面向 AI 代理的工作者：** 必需子技能: 使用 superpowers:subagent-driven-development（推荐）或 superpowers:executing-plans 逐任务实现此计划。步骤使用复选框（`- [ ]`）语法来跟踪进度。

**目标：** 将 Markdown 预览页面从后台管理风格升级为专业的文档阅读器，支持主题切换、阅读设置、轻量目录、代码复制、图片放大。

**架构：** Preview.vue 拆分为 4 个子组件 + 2 个 composable，通过 props 和 emit 通信，阅读设置和主题存 localStorage。

**技术栈：** Vue 3 Composition API, TypeScript, md-editor-v3, Element Plus, medium-zoom, lucide-vue-next

---

### 任务 1：安装依赖

**文件：**
- 修改: `personal-hub-web/package.json`

- [ ] **步骤 1：安装 medium-zoom**

```bash
cd personal-hub-web && pnpm add medium-zoom
```

---

### 任务 2：新增 Sepia 主题 CSS 变量

**文件：**
- 修改: `personal-hub-web/src/styles/global.css`

- [ ] **步骤 1：在 global.css 末尾添加预览主题 CSS 变量**

```css
/* ========== 预览页主题 ========== */
/* Sepia 护眼主题 */
[data-preview-theme='sepia'] {
  --preview-bg: #FBF7F0;
  --preview-text: #5B4636;
  --preview-heading: #3E2723;
  --preview-border: #E8DCC8;
  --preview-code-bg: #F5F0E8;
  --preview-quote-bar: #D4A574;
  --preview-code-header: #EDE4D4;
}

/* Light 预览覆盖 */
[data-preview-theme='light'] {
  --preview-bg: #FFFFFF;
  --preview-text: #1F2937;
  --preview-heading: #111827;
  --preview-border: #E5E7EB;
  --preview-code-bg: #F9FAFB;
  --preview-quote-bar: #D1D5DB;
  --preview-code-header: #F3F4F6;
}

/* Dark 预览覆盖 */
[data-preview-theme='dark'] {
  --preview-bg: #0D1117;
  --preview-text: #C9D1D9;
  --preview-heading: #E6EDF3;
  --preview-border: #30363D;
  --preview-code-bg: #161B22;
  --preview-quote-bar: #8B949E;
  --preview-code-header: #161B22;
}
```

---

### 任务 3：创建 usePreviewSettings composable

**文件：**
- 创建: `personal-hub-web/src/modules/knowledge/note/preview/usePreviewSettings.ts`

- [ ] **步骤 1：编写 usePreviewSettings.ts**

```ts
import { reactive, watch } from 'vue'

const STORAGE_KEY = 'preview-reading-settings'

export interface PreviewSettings {
  fontSize: number    // 16 | 18 | 20 | 22
  readingWidth: number // 800 | 900 | 1000
  lineHeight: number  // 1.6 | 1.8 | 2.0
}

function loadDefaults(): PreviewSettings {
  try {
    const stored = localStorage.getItem(STORAGE_KEY)
    if (stored) return JSON.parse(stored)
  } catch {}
  return { fontSize: 18, readingWidth: 900, lineHeight: 1.8 }
}

export function usePreviewSettings() {
  const settings = reactive<PreviewSettings>(loadDefaults())

  watch(
    () => ({ ...settings }),
    (val) => {
      localStorage.setItem(STORAGE_KEY, JSON.stringify(val))
    },
    { deep: true }
  )

  return { settings }
}
```

---

### 任务 4：创建 usePreviewTheme composable

**文件：**
- 创建: `personal-hub-web/src/modules/knowledge/note/preview/usePreviewTheme.ts`

- [ ] **步骤 1：编写 usePreviewTheme.ts**

```ts
import { ref, watch, onMounted } from 'vue'

export type PreviewTheme = 'follow' | 'light' | 'dark' | 'sepia'

const STORAGE_KEY = 'preview-theme'
const THEMES: PreviewTheme[] = ['follow', 'light', 'dark', 'sepia']

export function usePreviewTheme() {
  const theme = ref<PreviewTheme>(loadTheme())

  function loadTheme(): PreviewTheme {
    try {
      const stored = localStorage.getItem(STORAGE_KEY) as PreviewTheme | null
      if (stored && THEMES.includes(stored)) return stored
    } catch {}
    return 'follow'
  }

  function resolveTheme(t: PreviewTheme): string {
    if (t === 'follow') {
      return document.documentElement.getAttribute('data-theme') === 'dark' ? 'dark' : 'light'
    }
    return t
  }

  function apply(t: PreviewTheme) {
    document.documentElement.setAttribute('data-preview-theme', t)
    // resolve 后的实际主题，用于 md-editor-v3 theme prop
    const resolved = resolveTheme(t)
    document.documentElement.setAttribute('data-preview-resolved', resolved)
    localStorage.setItem(STORAGE_KEY, t)
  }

  // 跟随系统主题变化
  function onSystemThemeChange() {
    if (theme.value === 'follow') {
      apply('follow')
    }
  }

  onMounted(() => {
    apply(theme.value)
    const observer = new MutationObserver(onSystemThemeChange)
    observer.observe(document.documentElement, { attributes: true, attributeFilter: ['data-theme'] })
  })

  watch(theme, (t) => apply(t))

  function cycleTheme() {
    const idx = THEMES.indexOf(theme.value)
    theme.value = THEMES[(idx + 1) % THEMES.length]
  }

  function setTheme(t: PreviewTheme) {
    theme.value = t
  }

  return { theme, resolvedTheme: computed(() => resolveTheme(theme.value)), cycleTheme, setTheme }
}
```

Wait, I need `computed` — let me fix that. Actually I should write the correct code.

```ts
import { ref, watch, computed, onMounted } from 'vue'

export type PreviewTheme = 'follow' | 'light' | 'dark' | 'sepia'

const STORAGE_KEY = 'preview-theme'
const THEMES: PreviewTheme[] = ['follow', 'light', 'dark', 'sepia']

export function usePreviewTheme() {
  const theme = ref<PreviewTheme>(loadTheme())

  function loadTheme(): PreviewTheme {
    try {
      const stored = localStorage.getItem(STORAGE_KEY) as PreviewTheme | null
      if (stored && THEMES.includes(stored)) return stored
    } catch {}
    return 'follow'
  }

  const resolvedTheme = computed(() => {
    if (theme.value === 'follow') {
      const systemTheme = document.documentElement.getAttribute('data-theme')
      return systemTheme === 'dark' ? 'dark' : 'light'
    }
    return theme.value
  })

  function apply(t: PreviewTheme) {
    document.documentElement.setAttribute('data-preview-theme', t)
    localStorage.setItem(STORAGE_KEY, t)
  }

  onMounted(() => {
    apply(theme.value)
    // 监听系统主题变化
    const observer = new MutationObserver(() => {
      if (theme.value === 'follow') apply('follow')
    })
    observer.observe(document.documentElement, { attributes: true, attributeFilter: ['data-theme'] })
  })

  watch(theme, (t) => apply(t))

  function setTheme(t: PreviewTheme) {
    theme.value = t
  }

  return { theme, resolvedTheme, setTheme }
}
```

---

### 任务 5：创建 PreviewToc.vue 组件

**文件：**
- 创建: `personal-hub-web/src/modules/knowledge/note/preview/PreviewToc.vue`

- [ ] **步骤 1：编写 PreviewToc.vue**

Props: items, activeId, collapsed
Emits: toggle-collapse, scroll-to
轻量化设计，更小字体，纯缩进，无圆点装饰，可收起 + 拖拽。

---

### 任务 6：创建 PreviewHeader.vue 组件

**文件：**
- 创建: `personal-hub-web/src/modules/knowledge/note/preview/PreviewHeader.vue`

- [ ] **步骤 1：编写 PreviewHeader.vue**

Props: title, isTrash
Emits: back, open-reading-settings, open-theme-picker, export, restore
Slots: actions (more dropdown)
极简顶部栏：← 返回 | 标题 | Aa ☀️ ⋯

---

### 任务 7：创建 ReadingSettings.vue 组件

**文件：**
- 创建: `personal-hub-web/src/modules/knowledge/note/preview/ReadingSettings.vue`

- [ ] **步骤 1：编写 ReadingSettings.vue**

使用 Element Plus Popover，接收 settings 对象，emit update 事件。
字体大小、阅读宽度、行高三项设置，实时生效。

---

### 任务 8：创建 PreviewTheme.vue 组件

**文件：**
- 创建: `personal-hub-web/src/modules/knowledge/note/preview/PreviewTheme.vue`

- [ ] **步骤 1：编写 PreviewTheme.vue**

使用 Element Plus Popover/Dropdown，展示 4 个主题选项。
点击选中某个主题，emit change 事件。

---

### 任务 9：重构 Preview.vue 主容器

**文件：**
- 修改: `personal-hub-web/src/modules/knowledge/note/Preview.vue`

- [ ] **步骤 1：重构 Preview.vue**

整合所有子组件和 composable：
- 使用 usePreviewSettings、usePreviewTheme
- 引入 PreviewHeader、PreviewToc、ReadingSettings、PreviewTheme
- 注入 CSS 变量到正文区域
- 添加 medium-zoom 图片放大
- 添加代码块一键复制
- 移除 Card 样式
- 添加 GitHub 风格的代码块/引用块/表格样式

- [ ] **步骤 2：移除旧样式，应用新阅读样式**

正文样式重构：
- 移除 `.preview-article` 的 border/background/border-radius
- 应用 `--preview-bg` 作为正文背景
- 应用 `--preview-content-width` 控制最大宽度
- 代码块、引用块、表格横向滚动

---

### 任务 10：代码块一键复制

- [ ] **步骤 1：实现代码复制功能**

在 Preview.vue 的 onMounted 中，监听 `.md-editor-preview` 渲染后，为每个 `pre code` 添加复制按钮。

---

### 任务 11：完善样式细节

- [ ] **步骤 1：引用块、表格、Callout 样式**

引用块：左侧 3px 色条 + 浅背景 + 圆角
表格：横向滚动容器
Callout：左侧强调色条 + 浅背景

---

### 任务 12：预览模式提示条

- [ ] **步骤 1：集成到 PreviewHeader**

顶部提示条保持现有逻辑（回收站/预览模式提示），但样式更轻。
