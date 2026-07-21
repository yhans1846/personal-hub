# 笔记编辑器三栏布局（编辑 / 预览 / 大纲）

> 日期：2026-07-21 · 方案 A（改造现有 Editor）  
> 范围：Overlay/嵌入态 Editor 默认三栏；Hub Token；顶栏标题 + 属性抽屉。不做米色换肤、拖拽分栏、滚动同步。

## 目标

- 默认：**左 Markdown 编辑 · 中实时预览 · 右标题大纲**，贴近参考工具的写作工作区，而非「表单 + 互斥预览」。
- 沿用 Personal Hub Token 与现有保存/Overlay/脏关语义。
- 标题进顶栏；分类/标签进属性抽屉，主区留给写作。

## 背景

- 当前 Editor：`edit` / `preview` 互斥；标题与 meta 在编辑区上方；预览内可带 TOC。
- 刚落地列表全屏 Overlay；本改在该工作区内完成。
- 已有 `parseTocFromMarkdown`、`PreviewToc`、`NoteMarkdownPreview` 可复用。

## 已锁定决策

| 项 | 选择 |
|----|------|
| 右侧栏 | 正文 heading 大纲（非属性栏） |
| 默认形态 | 三栏常驻；顶栏可切仅编辑 / 仅预览 |
| 视觉 | Hub Token，不单独米色皮肤 |
| 标题/属性 | 顶栏可编辑标题 +「属性」抽屉 |
| 实现路径 | 改造现有 Editor（方案 A） |

## 设计

### 1. 布局与模式

| 项 | 约定 |
|----|------|
| 默认 | 三栏：左 `NoteVditor` · 中 `NoteMarkdownPreview` · 右大纲 |
| 模式 | `split`（默认）\| `edit` \| `preview` |
| 顶栏 | ✕/返回 · 标题输入 · 属性 · 模式切换 · 收藏/全屏/更多 |
| 比例 | v1 固定约 38% / 42% / 20%；不可拖拽 |
| 主题 | CSS 变量；跟浅色/深色 |
| 小屏 ≤768 | 默认 `edit` 全宽；大纲用抽屉；不强制三栏 |

### 2. 组件与数据流

```
Editor
  EditorHeader（标题 + 属性入口 + split/edit/preview）
  EditorPropsDrawer（分类、标签等原 meta）
  .editor-split
    NoteVditor           // mode ≠ preview
    NoteMarkdownPreview  // mode ≠ edit；分屏时关闭内嵌 TOC
    EditorOutline        // split 与 preview：PreviewToc + parseToc
  EditorStatusBar
```

| 数据 | 流向 |
|------|------|
| `form.title` | Header ↔ `useAutoSave` |
| `form.content` | Vditor → Preview + Outline（debounce ≤200ms） |
| 分类/标签 | 仅属性抽屉 |
| 大纲点击 | 预览区对应 heading `scrollIntoView` |
| Overlay / 脏关 / Esc 关工作区 | **不变**（Esc 仍不关 Overlay；可关属性抽屉） |

独立页 `/notes/:id/preview` **本轮不改**。

### 3. 模式与快捷键

| 模式 | 表现 |
|------|------|
| `split` | 三栏；编辑与预览同时挂载 |
| `edit` | 仅编辑全宽；隐藏右大纲 |
| `preview` | 预览 + 右大纲 |

- 顶栏三按钮切换模式。
- Ctrl+Shift+P：`split` ↔ `preview`（纯编辑仅点顶栏）。

### 4. 大纲

- 数据：`parseTocFromMarkdown(form.content)`。
- UI：复用 `PreviewToc`（或薄包装 `EditorOutline`）。
- v1：点击跳转；**滚动联动高亮为可选加分，非必须**。
- 无 heading：短空态文案。

### 5. 属性抽屉

- 承载原编辑区 meta：分类、标签（及必要静态信息如创建时间/阅读时长，若仍需要）。
- Esc 关闭抽屉，**不**关闭 Overlay。

## 明确不做

米色独立皮肤 · 可拖拽分栏 · 多标签编辑 · AI 状态条 · 仿极窄图标轨 · 改只读预览页 · 编辑↔预览光标/滚动同步

## 涉及文件（预期）

- `Editor.vue` · `EditorHeader.vue` · `useEditorMode.ts`
- 新建或薄封装：`EditorOutline.vue` · `EditorPropsDrawer.vue`（命名可微调）
- `NoteMarkdownPreview.vue`（分屏禁用内嵌 TOC）
- `PAGE_SPEC.md` · `CHANGELOG.md`

## 验收

1. 新建/编辑默认三栏；左侧输入，中间及时更新  
2. 大纲可点击跳到预览对应标题  
3. 顶栏改标题可保存  
4. 属性抽屉可改分类/标签  
5. Esc 不关 Overlay；可关属性抽屉  
6. `edit` / `preview` / `split` 切换正确；≤768 不横溢  
7. 深浅色 Token 正常；脏关 / Overlay ✕ 行为不变  
