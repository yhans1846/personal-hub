# 笔记预览页开发规范

> 面向 AI 代理与开发者

**最后更新：** 2026-07-12

---

## 1. 文件结构

```
src/
├── components/
│   └── DocLayout.vue                          # 统一文档布局（Header + TOC + 正文）
├── styles/
│   └── markdown-prose.css                     # 全局 Markdown 排版系统（全局导入）
└── modules/knowledge/note/
    ├── Preview.vue                            # 预览页入口
    └── preview/
        ├── PreviewHeader.vue                  # 顶栏（返回 / 标题 / meta / Aa / 更多）
        ├── PreviewToc.vue                     # 左侧目录（可折叠 / 可拖拽）
        ├── ReadingSettings.vue                # 阅读设置面板（字号 / 宽度 / 行高 / 主题）
        ├── PreviewTheme.vue                   # 主题选择器（备用）
        ├── usePreviewSettings.ts              # 阅读设置 composable（reactive + localStorage）
        └── usePreviewTheme.ts                 # 主题 composable（data-preview-theme + MutationObserver）
```

---

## 2. 组件使用规范

### 2.1 DocLayout

```html
<DocLayout
  :title="标题"
  :toc-items="toc"
  :active-heading="activeHeading"
  :meta="{ updatedAt: '2026-07-12 12:00', readingTime: '3 分钟' }"
  :is-trash="false"
  @back="handleClose"
  @scroll-to-heading="scrollToHeading"
>
  <!-- 可选：覆盖 Header 右侧操作区 -->
  <template #header-actions>
    <el-popover>...</el-popover>
  </template>

  <!-- 正文 -->
  <div class="preview-content-wrap" :style="{ maxWidth, fontSize, lineHeight, '--prose-font-size', '--prose-line-height' }">
    <div class="preview-content markdown-prose">
      <MdPreview :model-value="content" :theme="mdTheme" />
    </div>
  </div>
</DocLayout>
```

**关键约束：**
- 正文区必须同时设 `fontSize` / `lineHeight` 和 `--prose-font-size` / `--prose-line-height` 两个 CSS 变量，否则 `markdown-prose` 内部拿不到变量值
- `.doc-content` 只设垂直内边距（`padding: var(--sp-2) 0`），**水平留白由 `:deep(.md-editor-preview)` 的 `padding-inline: 48px` 控制**

### 2.2 ReadingSettings

```html
<ReadingSettings
  :settings="settings"
  :theme="theme"
  @update:settings="Object.assign(settings, $event)"
  @update:theme="setTheme($event as any)"
/>
```

**注意：** `settings` 必须是 `reactive` 对象，`Object.assign` 直接修改触发响应式更新。不要替换整个对象。

### 2.3 PreviewToc

```html
<PreviewToc
  :items="tocItems"
  :active-id="activeHeading"
  @scroll-to="emit('scroll-to-heading', $event)"
/>
```

**交互行为：** 默认展开 220px，点收起按钮缩为 34px（保留展开按钮），右侧 4px 拖拽条可调宽度 160~400px。

---

## 3. 排版系统（markdown-prose）

全局 CSS 文件：`src/styles/markdown-prose.css`，已由 `main.ts` 导入。

**用法：** 内容容器加 `class="markdown-prose"`，自动获得全部 Markdown 排版规则。

```css
.markdown-prose {
  --prose-color: var(--preview-text, var(--text-primary));
  --prose-heading-color: var(--preview-heading, var(--text-primary));
  --prose-border-color: var(--preview-border, var(--border-color));
  --prose-code-bg: var(--preview-code-bg, var(--bg-hover));
  --prose-quote-bar: var(--preview-quote-bar, var(--accent));

  color: var(--prose-color);
  font-size: var(--prose-font-size);    /* 从父级 inline style 传入 */
  line-height: var(--prose-line-height);
}
```

**覆盖元素总览：** h1-h6 / p / a / ul/ol / blockquote / pre / code / table / img / hr / 任务列表 / 标题锚点 / 代码复制按钮。

**标题锚点：** `position: absolute; left: -1em; opacity: 0` — hover 标题时显示 `#`，不越界。

---

## 4. 阅读主题

### 4.1 主题 CSS 变量

定义在 `global.css`，通过 `[data-preview-theme='xxx']` 选择器切换：

| 变量 | 用途 |
|------|------|
| `--preview-bg` | 页面背景 |
| `--preview-text` | 正文颜色 |
| `--preview-heading` | 标题颜色 |
| `--preview-border` | 边框/分割线 |
| `--preview-code-bg` | 代码块背景 |
| `--preview-quote-bar` | 引用块左侧色条 |

### 4.2 主题切换链路

```
用户点击主题按钮
→ ReadingSettings emit('update:theme')
→ setTheme() → document.documentElement.setAttribute('data-preview-theme', t)
→ CSS 变量切换（--preview-bg / --preview-text 等）
→ markdown-prose 的 --prose-* 变量跟随变化
→ :deep(.md-editor-preview) 的 color/background 通过 !important 覆盖库样式
```

**MdPreview 的 theme prop：** `mdTheme = resolvedTheme === 'sepia' ? 'light' : resolvedTheme`，sepia 时传 `light`，颜色完全由 CSS 变量控制。

### 4.3 持久化

| Key | 存储内容 |
|-----|---------|
| `preview-theme` | 主题偏好（follow/light/dark/sepia） |
| `preview-reading-settings` | 字号/宽度/行高 JSON |

---

## 5. 注意事项

### 5.1 `:deep()` 使用原则

- **仅用于覆盖 md-editor-v3 和 @vavt/markdown-theme 的硬编码样式**
- 排版规则统一写在 `markdown-prose.css`，不要写在 `:deep()` 里
- 每个 `:deep()` 块必须注释说明为什么需要覆盖

### 5.2 md-editor-v3 已知问题与修复

| 问题 | 源代码位置 | 修复 |
|------|-----------|------|
| `font-size: 16px` 硬编码 | `.md-editor-preview` | `:deep()` → `var(--prose-font-size) !important` |
| `line-height: 1.6` 硬编码 | `div.default-theme p/li` | `:deep()` → `inherit !important` |
| `line-height: 2em` 硬编码 | `div.default-theme blockquote` | 同上 |
| `word-break: break-all` | `div.default-theme` | `:deep()` → `break-word !important` |
| 颜色独立不跟随主题 | `--md-theme-color` 等 | `:deep()` → `var(--prose-color) !important` |
| 白色背景 | `.md-editor` `background-color` | `:deep()` → `transparent !important` |
| 代码块标题 `z-index: 10000` | `.md-editor-code-head` | 全局 `.preview-settings-popper { z-index: 10001 }` |

### 5.3 目录定位

`scrollToHeading(id)` 必须用 `document.getElementById(id)`，**禁止用 `textContent` 匹配**。`setupHeadingAnchors` 给每个标题插入了 `.heading-anchor`（`#` 文字），`textContent` 会包含它，导致匹配失败。

### 5.4 内容加载顺序

```
onMounted → 加载数据 → watch(content) → nextTick → 
  setupObserver()      (200ms 延迟)
  setupImageZoom()     (nextTick)
  setupCodeCopy()      (nextTick, 检查 .code-copy-btn 防重复)
  setupExternalLinks() (nextTick)
  setupHeadingAnchors()(nextTick, 检查 .heading-anchor 防重复)
```

**主题切换时需重新调用 `setupImageZoom()`**，因为 MdPreview 重渲染会替换 DOM。

### 5.5 其他页面复用

帮助文档、README、笔记只读模式等页面复用 DocLayout + markdown-prose：

```html
<DocLayout :title="title" :toc-items="toc" @back="...">
  <div class="markdown-prose">
    <!-- 任意 Markdown 渲染内容 -->
  </div>
</DocLayout>
```

DocLayout 只提供壳（Header + TOC + 滚动容器），页面自己控制内容宽度和排版。
