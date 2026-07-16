# 笔记预览页开发规范

> 面向 AI 代理与开发者

**最后更新：** 2026-07-12 v2（阅读配置迁移至系统设置）

---

## 1. 文件结构

```
src/
├── components/DocLayout.vue              # Header + TOC + 正文
├── styles/markdown-prose.css             # 全局 Markdown 排版
├── store/readingConfigStore.ts           # 阅读配置（local + 后端）
└── modules/knowledge/note/
    ├── Preview.vue
    └── preview/
        ├── PreviewHeader.vue · PreviewToc.vue
        ├── PreviewTheme.vue（备用）
        └── useReadingTheme.ts            # data-preview-theme
```

---

## 2. 组件使用规范

### 2.1 DocLayout

用 `DocLayout` 包正文：`:title` / `:toc-items` / `:active-heading` / `:meta` / `:is-trash`；`#header-actions` 可选。

正文容器须同时设 `fontSize`/`lineHeight` 与 `--prose-font-size`/`--prose-line-height`。  
`.doc-content` 仅垂直 padding；水平留白由预览区 `padding-inline` 控制。

### 2.2 阅读配置

设置页「阅读体验」→ `readingConfigStore`；预览页 Header 仅保留导出/恢复等，不再嵌阅读设置 popover。

### 2.3 PreviewToc

`:items` + `:active-id`；默认宽 220，收起 34；拖拽条调宽 160–400。

---

## 3. 排版系统（markdown-prose）

`main.ts` 已导入。容器加 `class="markdown-prose"`。

关键变量：`--prose-color` / `--prose-heading-color` / `--prose-border-color` / `--prose-code-bg` / `--prose-quote-bar`，以及 `--prose-font-size` / `--prose-line-height`（父级传入）。

覆盖：h1–h6、p、a、列表、引用、代码、表格、图、hr、任务列表、标题锚点、代码复制。  
标题锚点：绝对定位左侧，hover 显 `#`。

---

## 4. 阅读主题

### 4.1 主题 CSS 变量

`global.css` 中 `[data-preview-theme='xxx']`：`--preview-bg/text/heading/border/code-bg/quote-bar`。

### 4.2 主题切换链路

设 `data-preview-theme` → `--preview-*` → `--prose-*` 跟随；库样式必要时 `:deep()` + `!important`。  
sepia 时 MdPreview theme 传 `light`，颜色靠 CSS 变量。

### 4.3 持久化

| 层 | 方式 |
|----|------|
| localStorage | `reading-config` |
| 后端 | `user_layout(layout_type='preview')`，防抖 500ms |

旧 key `preview-theme` / `preview-reading-settings` 首次合并后清理；`readingWidth=960`→`1100`。

### 4.4 图片显示比例

`--image-max-width`（60%–100%，设置页调节）；Preview/Editor 预览区一致。

---

## 5. 注意事项

### 5.1 图片认证

`/api/notes/{noteId}/images/{filename}`；`<img>` 用 `?token=`。`setupImageProxy()` 改写相对路径。

### 5.2 `:deep()` 使用原则

仅覆盖编辑器/主题库硬编码；排版写在 `markdown-prose.css`；每块注释原因。

### 5.3 md-editor-v3 已知问题与修复

| 问题 | 修复 |
|------|------|
| font-size/line-height 硬编码 | `:deep` → `var(--prose-*)` / inherit |
| word-break: break-all | → break-word |
| 颜色/背景不跟主题 | → prose 变量 / transparent |
| 代码头 z-index 过高 | popper z-index 抬高 |

### 5.4 目录定位

`scrollToHeading` 用 `getElementById`；禁 `textContent` 匹配（锚点 `#` 会污染文本）。

### 5.5 内容加载顺序

`onMounted` → 加载 → `watch(content)` → nextTick：`setupImageProxy` → Observer(200ms) → Zoom/CodeCopy/Links/HeadingAnchors（防重复）。  
主题切换后须重调 `setupImageZoom()`。

### 5.6 其他页面复用

帮助/README/只读：`DocLayout` + `markdown-prose`；壳只管 Header/TOC/滚动，宽度由页面自控。
