# 笔记编辑页布局优化（方案 A）

> 日期：2026-07-21 · 方案 A（加宽日常栏 + 专注常驻工具头）  
> 范围：编辑/预览内容宽、专注模式 chrome；不改自动保存与快捷键语义。

## 目标

- 日常编辑写作区加宽到约 **1280px**（居中，保留左右边距），接近专注观感且不贴边。
- 专注模式：继续隐藏 **App 侧栏 / 顶栏 / 面包屑**；**常驻**笔记 `editor-header` 细条（不再 `display: none`）。
- 预览模式内容宽与编辑对齐。

## 背景

- 当前 `.editor-content-wrap { max-width: 1000px }`，大屏两侧留白过多。
- 专注通过 `body.editor-focus-mode` + AppLayout `.focus-hidden` 藏工作区 chrome；同时 `EditorHeader.is-focus { display: none }` 把笔记工具头也藏掉，无法直观退出/返回。
- 专注下内容区已是 `max-width: none`，宽度体验更好。

## 设计

### 日常编辑 / 预览

| 项 | 约定 |
|----|------|
| 内容栏 | `.editor-content-wrap`：`max-width: 1280px`；`padding` 桌面约 `0 48px`，`≤768` 约 `0 20px` |
| 回链 | 与内容栏同宽，去掉单独 `max-width: 720px` 限制（或改为跟随父级） |
| 预览 | `NoteMarkdownPreview` 落在同一 wrap 内，宽度自然对齐 |

### 专注模式

| 项 | 约定 |
|----|------|
| App chrome | 保持现状：藏侧栏、顶栏、面包屑 |
| 笔记 header | **显示**；专注态样式：高度约 `48px`、背景 `var(--bg-body)`、弱底边；保留返回、保存态、退出专注、收藏、全屏、更多 |
| 预览按钮 | 专注下可继续隐藏（与现逻辑一致：`v-if="mode !== 'focus'"`），避免模式纠缠；退出专注后再预览 |
| 内容区 | 保持 `max-width: none`，顶为常驻 header |
| 状态栏 | **保留**（字数 / 模式 / 保存） |
| Esc | 仍退出专注（现有 `useEditorMode`） |

### 全屏

浏览器 Fullscreen API 行为不变；可与专注叠加。

## 明确不做

可拖拽分栏 · 目录双栏 · 把专注改成仅浏览器全屏 · 默认近全宽无 max-width · 阅读舒适窄栏（~720）

## 涉及文件

- `personal-hub-web/src/modules/knowledge/note/Editor.vue`
- `personal-hub-web/src/modules/knowledge/note/editor/EditorHeader.vue`
- `EditorStatusBar.vue`（可选：专注背景对齐）
- `docs/PAGE_SPEC.md` · `docs/CHANGELOG.md`

## 验收

1. 日常编辑：写作区明显宽于 1000px 方案，侧栏 + header 可见  
2. 专注：侧栏/顶栏消失，header 细条仍在，可退出专注 / 返回  
3. Esc 可退出专注  
4. 预览内容宽与编辑一致  
5. ≤768 不横向溢出  

## 文档

实现后更新 `PAGE_SPEC.md`（笔记编辑布局一句）与 `CHANGELOG.md`。
