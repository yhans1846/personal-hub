# 预览页面阅读体验优化 — 设计文档

## 概述

将 Personal Hub 的 Markdown 预览页面从后台管理风格升级为专业的文档阅读器，参考 Notion、GitHub Docs、Apple Documentation 的设计语言。

## 架构

```
Preview.vue              ← 主容器，编排所有子组件
├── usePreviewSettings    ← Composable: 阅读设置 (fontSize/readingWidth/lineHeight)
├── usePreviewTheme       ← Composable: 预览主题 (follow/light/dark/sepia)
├── PreviewHeader.vue     ← 顶部栏（返回、标题、Aa/主题/更多工具）
├── PreviewToc.vue        ← 左侧轻量目录
├── ReadingSettings.vue   ← Aa 阅读设置弹窗
├── PreviewTheme.vue      ← 主题切换（follow/light/dark/sepia）
└── PreviewImagePreview   ← 图片点击放大（使用 medium-zoom）
```

## 组件职责

### PreviewHeader.vue
- 左侧：纯文字"← 返回"按钮，无背景框
- 中部：笔记标题（溢出省略）
- 右侧：`Aa`（阅读设置触发器）、主题图标（循环/下拉切换）、`⋯`（更多操作下拉）
- 无彩色背景、无 Card 感

### PreviewToc.vue
- Props: `items: TocItem[]`, `activeId: string`, `collapsed: boolean`
- 更小的字体（12-13px）、更淡的颜色（text-tertiary）
- 纯缩进层级（无圆点装饰）
- 当前章节轻高亮、hover 反馈
- 可收起 + 拖拽调整宽度（保留现有功能）

### ReadingSettings.vue
- Popover 形式，点击 Aa 触发
- **字体大小**: 16 / 18 / 20 / 22 (默认 18)
- **阅读宽度**: 窄(800px) / 标准(900px) / 宽(1000px) (默认 900)
- **行高**: 1.6 / 1.8 / 2.0 (默认 1.8)
- 实时生效，保存到 localStorage

### PreviewTheme.vue
- 下拉/弹窗形式，点击主题图标触发
- 4 个选项：跟随系统 / 浅色 / 深色 / 护眼(Sepia)
- 用 `data-preview-theme` 属性隔离作用域
- 保存到 localStorage
- follow 模式动态跟随系统 `data-theme`

### usePreviewSettings.ts (Composable)
```ts
interface PreviewSettings {
  fontSize: number    // 16-22
  readingWidth: number // 800-1000
  lineHeight: number  // 1.6-2.0
}
```
- localStorage key: `preview-reading-settings`
- 读写、响应式、向 DOM 注入 CSS 变量

### usePreviewTheme.ts (Composable)
```ts
type PreviewTheme = 'follow' | 'light' | 'dark' | 'sepia'
```
- localStorage key: `preview-theme`
- 管理 `data-preview-theme` 属性
- follow 模式：读取系统 `data-theme`；sepia 模式：应用 sepia CSS 变量

## 数据流

```
[localStorage] ──→ usePreviewSettings ──→ Preview.vue (CSS变量注入)
                ──→ usePreviewTheme   ──→ Preview.vue (data-preview-theme)
                                          ↓
                                   子组件通过 props 接收
```

- 主题和阅读设置**只影响预览页面**，不影响整个 Personal Hub
- 关闭时不清除 localStorage（下次打开恢复）
- 多标签页间不自动同步（可接受）

## CSS 变量

### 新增 Sepia 主题
```css
[data-preview-theme='sepia'] {
  --preview-bg: #FBF7F0;
  --preview-text: #5B4636;
  --preview-heading: #3E2723;
  --preview-border: #E8DCC8;
  --preview-code-bg: #F5F0E8;
  --preview-quote: #EDE4D4;
  --preview-quote-bar: #D4A574;
}
```

### 正文区域重构
- 移除 Card 样式（`bg-card` + `border` + `border-radius`）
- 正文背景透明，继承预览主题背景
- 阅读宽度通过 CSS 变量 `--preview-content-width` 控制

## 依赖新增

| 包 | 用途 |
|---|---|
| `medium-zoom` | 图片点击放大 |

## 不在此范围

- 不修改后端 API
- 不修改现有 Markdown 渲染逻辑（md-editor-v3）
- 不修改系统主题切换机制
- 不新增 Pinia store

## 文件清单

### 新增
- `src/modules/knowledge/note/preview/usePreviewSettings.ts`
- `src/modules/knowledge/note/preview/usePreviewTheme.ts`
- `src/modules/knowledge/note/preview/PreviewHeader.vue`
- `src/modules/knowledge/note/preview/PreviewToc.vue`
- `src/modules/knowledge/note/preview/ReadingSettings.vue`
- `src/modules/knowledge/note/preview/PreviewTheme.vue`

### 修改
- `src/modules/knowledge/note/Preview.vue`（重构为编排容器）
- `src/styles/global.css`（新增 Sepia 和预览主题变量）
