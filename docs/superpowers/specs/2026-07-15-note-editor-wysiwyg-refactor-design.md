# 笔记模块 Typora 式 WYSIWYG 重构 — 设计文档

> 将笔记编辑体验从 md-editor-v3 源码编辑迁移至 Vditor IR 即时渲染，移除全部工具栏，以右键菜单驱动全部格式化能力，并统一列表/编辑/预览/回收站交互。
>
> 设计日期：2026-07-15  
> 状态：待实现  
> 关联计划：`.cursor/plans/笔记模块_wysiwyg_重构_c0e69f41.plan.md`

---

## 1. 背景

### 1.1 现状问题

Personal Hub 笔记模块当前基于 **md-editor-v3 + CodeMirror** 实现：

| 问题 | 说明 |
|------|------|
| 编辑范式不匹配 | 源码编辑 + 独立预览，非 Typora 式「输入即渲染」 |
| 工具栏驱动 | 格式化依赖顶部工具栏，与「沉浸式创作」理念冲突 |
| 无右键菜单 | 缺少 Guanmo / Typora 风格的上下文操作 |
| 分栏预览冗余 | IR 模式下 splitpanes 双栏与即时渲染能力重叠 |
| 渲染管道分裂 | 编辑态（MdEditor）与阅读态（MdPreview）共用包但体验割裂 |

现有 Focus Mode（2026-07-14）已隐藏工具栏与 AppLayout chrome，但底层仍是源码编辑，未解决 WYSIWYG 与右键交互问题。

### 1.2 目标

1. **Typora 式 WYSIWYG**：Vditor IR 模式，Markdown 输入即时渲染
2. **零工具栏**：所有编辑/插入/格式化能力通过右键菜单触发
3. **参考 Guanmo-open**：菜单结构、定位逻辑、选区/光标插入模式
4. **Typora 级功能**：H1–H6 标题、脚注、链接引用、水平分割、表格（NxM 选择器）、代码块、公式块、Mermaid 等
5. **完整模块重构**：列表、编辑、预览、回收站、导入统一交互
6. **后端零变更**：`Note.content` 仍为 Markdown 字符串

### 1.3 用户确认项

| 决策 | 选择 |
|------|------|
| 编辑范式 | Typora WYSIWYG（非 Guanmo 源码编辑） |
| 重构范围 | 完整笔记模块（List / Editor / Preview / RecycleBin / Import） |

---

## 2. 参考项目

### 2.1 Guanmo-open（交互参考）

仓库：[we-used-to-be/Guanmo-open](https://github.com/we-used-to-be/Guanmo-open)

**借鉴项**（React + CodeMirror，移植至 Vue + Vditor）：

| 模式 | 说明 |
|------|------|
| `ContextMenu` 原语 | 视口边界修正、click/scroll 自动关闭 |
| 双态菜单 | 有选区 / 无选区展示不同菜单项 |
| `wrapSelection` | 包裹选区并定位光标 |
| `insertAtCursor` | 智能换行后在光标处插入 Markdown |
| `replaceSelection` | 模板中 `$selection` 占位符替换 |

**不借鉴项**：

- CodeMirror 源码编辑器（已选 Vditor IR）
- AI 助手右键菜单（Personal Hub 无此能力）
- 多标签页 TabBar

### 2.2 Typora（功能参考）

| 能力 | Markdown 语法 | 右键入口 |
|------|--------------|---------|
| 标题 H1–H6 | `# ` … `###### ` | 插入 → 标题子菜单 |
| 加粗/斜体/删除线 | `**` / `*` / `~~` | 选区 → 格式 |
| 引用 | `> ` | 插入 / 包裹 |
| 列表 | `- ` / `1. ` / `- [ ]` | 插入 |
| 链接 | `[text](url)` | 插入 / 包裹 |
| 链接引用 | `[text][ref]` + `[ref]: url` | 插入 |
| 脚注 | `[^n]` + `[^n]: content` | 插入 / 包裹 |
| 水平分割 | `---` | 插入 |
| 表格 | GFM pipe table | 插入 → N×M 网格选择器 |
| 代码块 | ` ``` ` | 插入 / 包裹 |
| 行内代码 | `` ` `` | 选区 → 格式 |
| 公式 | `$...$` / `$$...$$` | 插入 |
| Mermaid | ` ```mermaid ` | 插入 |
| 图片 | `![](url)` | 插入 / 上传 |

---

## 3. 技术选型

### 3.1 编辑器：Vditor IR 模式

| 方案 | 优点 | 缺点 | 结论 |
|------|------|------|------|
| **Vditor IR** | Typora 式即时渲染；内置脚注/GFM/KaTeX/Mermaid；API 丰富 | 包体较大（~22MB） | **采用** |
| Milkdown | 插件化、可定制 | 需自行实现 IR 体验与菜单；集成成本高 | 不采用 |
| 保留 md-editor-v3 + 右键 | 改动小 | 仍是源码编辑，非 WYSIWYG | 不采用 |
| Guanmo 式 CodeMirror | 右键模式成熟 | 非 Typora WYSIWYG | 不采用 |

**Vditor 关键 API**（右键菜单调用）：

| 方法 | 用途 |
|------|------|
| `getValue()` | 读取 Markdown（绑定 auto-save） |
| `setValue(md)` | 加载笔记内容 |
| `getSelection()` | 获取选区文本 |
| `insertMD(md)` | 光标处插入 Markdown |
| `updateValue(text)` | 替换选区 |
| `insertValue(text, render?)` | 插入并可选渲染 |
| `focus()` / `blur()` | 焦点管理 |

**初始化配置要点**：

```typescript
{
  mode: 'ir',
  toolbar: [],
  toolbarConfig: { hide: true },
  cache: { enable: false },
  counter: { enable: false },
  preview: {
    markdown: {
      footnotes: true,
      mathBlockPreview: true,
      codeBlockPreview: true,
    },
  },
  cdn: '/vditor',
}
```

### 3.2 依赖变更

| 操作 | 包 | 说明 |
|------|-----|------|
| 新增 | `vditor@^3.11` | 笔记编辑 + 预览渲染 |
| 移除 | `md-editor-v3` | 笔记模块迁移完成后删除 |
| 保留 | `katex`、`mermaid` | Vditor 本地 CDN 配置 |
| 保留 | `medium-zoom`、`splitpanes` | 预览图片缩放；splitpanes 从编辑页移除但包可暂留 |

### 3.3 静态资源策略

- 构建时将 `node_modules/vditor/dist/` 复制至 `public/vditor/`
- `cdn` 配置为 `/vditor`，避免 unpkg 外部依赖
- KaTeX / highlight.js / Mermaid 等同理本地化
- Vite chunk：`vendor-vditor` 替代 `vendor-editor`
- 笔记路由 lazy load，控制首屏体积

---

## 4. 架构

### 4.1 总体数据流

```
┌─────────────────────────────────────────────────────────────┐
│  Editor.vue                                                  │
│  ┌──────────────┐  ┌─────────────────────────────────────┐  │
│  │ EditorHeader │  │ 标题 + 分类/标签 Meta                │  │
│  └──────────────┘  └─────────────────────────────────────┘  │
│  ┌─────────────────────────────────────────────────────────┐│
│  │ NoteVditor (IR)  ←── EditorContextMenu (右键)           ││
│  └─────────────────────────────────────────────────────────┘│
│  ┌──────────────┐                                            │
│  │EditorStatusBar│ ← useAutoSave → PUT/POST /api/notes      │
│  └──────────────┘                                            │
└─────────────────────────────────────────────────────────────┘
                              │
                              ▼ Markdown string
┌─────────────────────────────────────────────────────────────┐
│  Preview.vue / Editor preview 模式                           │
│  NoteMarkdownPreview (Vditor 预览管道)                       │
│  + TOC + 阅读主题 + 图片代理 + medium-zoom                   │
└─────────────────────────────────────────────────────────────┘
```

### 4.2 模块目录结构

```
personal-hub-web/src/modules/knowledge/note/
├── List.vue                          # 卡片列表 + 右键菜单
├── Editor.vue                        # 编辑页主容器（重构）
├── Preview.vue                       # 独立阅读页（换预览组件）
├── RecycleBin.vue                    # 回收站（UI 对齐）
├── ImportMarkdownDialog.vue          # 导入（微调）
├── editor/
│   ├── NoteVditor.vue                # ★ Vditor IR 封装
│   ├── NoteMarkdownPreview.vue       # ★ 统一只读预览
│   ├── vditorSetup.ts                # ★ 初始化配置
│   ├── EditorHeader.vue              # 精简（移除分栏）
│   ├── EditorStatusBar.vue           # 保留
│   ├── useAutoSave.ts                # 保留
│   ├── useEditorMode.ts              # 简化模式
│   ├── useEditorPreferences.ts       # 移除 splitRatio
│   ├── useImageUpload.ts             # 保留，对接 Vditor upload
│   ├── parseToc.ts                   # 保留
│   └── context-menu/
│       ├── ContextMenu.vue
│       ├── ContextMenuItem.vue
│       ├── ContextMenuSubmenu.vue
│       ├── ContextMenuSeparator.vue
│       ├── TableGridPicker.vue       # ★ Typora 式表格网格
│       ├── EditorContextMenu.vue
│       ├── contextMenuActions.ts
│       └── useEditorContextMenu.ts
└── preview/                          # 阅读页 composable（保留）
    ├── PreviewHeader.vue
    ├── PreviewToc.vue
    ├── useReadingTheme.ts
    └── usePreviewSettings.ts
```

### 4.3 删除文件

| 文件 | 原因 |
|------|------|
| `editor/NoteMdEditor.vue` | 由 NoteVditor 替代 |
| `editor/EditorPreviewPanel.vue` | IR 模式无需编辑态分栏预览 |
| `editor/mdEditorToolbars.ts` | 零工具栏 |
| `editor/mdEditorSetup.ts` | 由 vditorSetup 替代 |

---

## 5. 右键菜单规格

### 5.1 通用行为

- 监听 `.vditor-ir` 区域 `@contextmenu.prevent`
- 记录 `{ x, y, hasSelection }` 打开菜单
- 菜单 z-index 高于 Vditor 内部浮层（参考 global.css 中 md-editor z-index 处理）
- 点击菜单外 / scroll 关闭
- 视口边界修正：超出底部则向上弹出

### 5.2 无选区菜单

```
基础操作
  粘贴
  全选
─────────────
插入
  标题 ▸          H1 / H2 / H3 / H4 / H5 / H6 / 正文
  引用
  无序列表
  有序列表
  任务列表
─────────────
  链接
  链接引用
  脚注
─────────────
  水平分割线
  表格 ▸          [8×6 TableGridPicker]
  代码块
  Mermaid 图
  公式块 ▸        块级公式 / 行内公式
  图片
```

### 5.3 有选区菜单

```
基础操作
  复制
─────────────
格式
  加粗
  斜体
  删除线
  行内代码
  上标
  下标
─────────────
  引用
  链接            包裹 [text](url)，光标定位 url
  脚注标记
─────────────
转换为
  标题 ▸          H1–H6 / 正文
```

### 5.4 Markdown 模板

**标题插入**（光标定位到「标题」占位）：

```markdown
# 标题

## 标题
...
###### 标题
```

**链接引用**：

```markdown
[链接文字][ref1]

[ref1]: https://example.com
```

**脚注**（自增编号，扫描文档现有 `[^n]`）：

```markdown
[^1]

[^1]: 脚注内容
```

**表格**（rows × cols，表头空单元格）：

```markdown
|   |   |   |
| --- | --- | --- |
|   |   |   |
|   |   |   |
```

**公式**：

```markdown
$$
E = mc^2
$$

行内：$E = mc^2$
```

**Mermaid**：

````markdown
```mermaid
graph TD
  A --> B
```
````

### 5.5 TableGridPicker 交互

| 属性 | 值 |
|------|-----|
| 网格尺寸 | 8 列 × 6 行（与 md-editor-v3 原 `table-shape` 一致） |
| hover | 高亮已选区域（如 3×4 则高亮前 3 列前 4 行） |
| 标签 | hover 时显示「3 × 4」 |
| 点击 | 调用 `insertTable(vditor, rows, cols)` 并关闭菜单 |
| 样式 | 使用 `--bg-elevated`、`--accent`、`--radius-sm` |

### 5.6 contextMenuActions.ts

纯函数 + Vditor 实例，便于单元测试：

```typescript
export function buildTableMarkdown(rows: number, cols: number): string
export function nextFootnoteIndex(markdown: string): number
export function wrapHeading(level: 1 | 2 | 3 | 4 | 5 | 6, text: string): string
export function insertAtCursor(vditor: IVditor, md: string, cursor?: [number, number]): void
export function wrapSelection(vditor: IVditor, before: string, after: string, select?: [number, number]): void
```

---

## 6. 编辑页设计

### 6.1 编辑器模式（简化）

| 模式 | 行为 | 快捷键 |
|------|------|--------|
| `edit` | Vditor IR 单栏写作（默认） | — |
| `preview` | 只读 NoteMarkdownPreview | `Ctrl+Shift+P` |
| `focus` | 隐藏 sidebar/header/breadcrumb，最小 Header | `Ctrl+Shift+F` 旁专注按钮；`Esc` 退出 |

**移除**：

- `livePreview` 分栏实时预览
- `toggleLivePreview` / `Ctrl+Shift+L`
- `splitRatio` localStorage 偏好
- splitpanes 动态导入与滚动同步逻辑

**保留**：

- 浏览器全屏（`Ctrl+Shift+F` 保留或调整，避免与 Focus 冲突时需重新映射）
- Focus Mode `document.body.classList.add('editor-focus-mode')`
- 自动保存（2s 防抖）、草稿恢复、Ctrl+S
- 分类/标签 Popover、收藏、导出、删除

### 6.2 NoteVditor.vue 接口

```typescript
// Props
interface NoteVditorProps {
  modelValue: string
  editorId: string
  theme: 'light' | 'dark'
  placeholder?: string
  readonly?: boolean
  noteId?: number | null       // 图片上传
  onUploadImg?: UploadHandler
}

// Emits
'update:modelValue': [value: string]
'ready': [vditor: IVditor]

// Expose
getVditor(): IVditor | null
focus(): void
```

### 6.3 EditorHeader 变更

| 变更 | 说明 |
|------|------|
| 移除 | 「分栏预览 / Columns3」按钮 |
| 保留 | 返回、保存状态、专注、编辑↔预览、全屏、收藏、更多（导出/删除） |

---

## 7. 预览页设计

### 7.1 NoteMarkdownPreview.vue

统一只读渲染，供 Preview.vue 与 Editor preview 模式共用。

**能力迁移**（来自 EditorPreviewPanel + Preview.vue）：

| 能力 | 实现 |
|------|------|
| Markdown 渲染 | Vditor.preview / md2html，footnotes + math + GFM 开启 |
| TOC | parseToc.ts + PreviewToc.vue |
| 图片代理 | `/api/notes/{id}/images/...?token=` |
| 图片缩放 | medium-zoom |
| 代码复制 | 预览 DOM 注入复制按钮 |
| 阅读主题 | useReadingTheme / usePreviewSettings（Preview.vue 专用） |

### 7.2 与 2026-07-12 阅读体验设计的关系

[预览阅读体验设计](2026-07-12-preview-reading-experience-design.md) 中的 Header / TOC / Aa 设置 / 主题切换 **全部保留**，仅将底层 `MdPreview` 替换为 `NoteMarkdownPreview`，确保脚注/表格/公式渲染一致。

---

## 8. 列表与回收站

### 8.1 List.vue 卡片右键菜单

```
编辑
新标签页预览
─────────────
收藏 / 取消收藏
导出 Markdown
─────────────
移入回收站
```

- 复用 `context-menu/ContextMenu.vue` 原语
- 菜单在卡片 `@contextmenu.prevent` 触发，不影响卡片 click → 编辑

### 8.2 RecycleBin.vue

- 卡片布局对齐 List.vue
- 右键菜单：恢复 / 永久删除 / 预览
- 空状态、骨架屏样式统一

### 8.3 ImportMarkdownDialog.vue

- API 不变（`POST /notes/import`、`/import-content`）
- 导入成功后跳转 Editor，Vditor `setValue` 加载
- 验证 GFM 扩展语法（脚注、表格、公式）IR 渲染

---

## 9. 后端与 API

**无变更**。继续使用：

| 接口 | 用途 |
|------|------|
| `GET/POST/PUT/DELETE /api/notes` | CRUD |
| `POST /api/notes/{id}/images` | 图片上传 |
| `GET /api/notes/{id}/preview` | 独立预览页数据 |
| 回收站 / 收藏 / 导入 | 现有接口 |

存储字段 `Note.content` 为 Markdown 文件地址（非内联存储）。

---

## 10. 样式规范

### 10.1 编辑区

- 字体：`var(--font-sans)`，16px，行高 1.8
- 背景：透明，继承页面 `--bg-primary`
- 无边框、无阴影（延续现有 NoteMdEditor 覆写风格）
- Vditor IR 内部链接/标题/代码块颜色跟随 `--text-primary` / `--accent`

### 10.2 右键菜单

- 背景：`var(--bg-elevated)`
- 圆角：`var(--radius-md)`
- 阴影：`var(--shadow-lg)`
- 菜单项 hover：`var(--bg-hover)`
- 分组标题：10px uppercase，`var(--text-tertiary)`
- 最小宽度：176px

### 10.3 Focus Mode

- 保留 `editor-focus-mode` body class
- 隐藏 AppLayout sidebar / topbar / breadcrumb
- 编辑区 `min-height: 100%`，标题 + IR 编辑器 + 状态栏

---

## 11. 实施阶段

```
Phase 0 ─→ Phase 1 ─→ Phase 2 ─→ Phase 3 ─→ Phase 4 ─→ Phase 5
设计文档    Vditor 内核   右键菜单    编辑页收尾   预览/列表    文档/验证
(本文档)
```

| 阶段 | 内容 | 预估 |
|------|------|------|
| **Phase 0** | 设计文档（本文档） | 0.5 天 |
| **Phase 1** | NoteVditor + vditorSetup，Editor 接入，auto-save / 上传 / 主题 | 2–3 天 |
| **Phase 2** | ContextMenu 原语 + EditorContextMenu + TableGridPicker + actions 单测 | 2–3 天 |
| **Phase 3** | 简化 useEditorMode / EditorHeader，移除 splitpanes | 1 天 |
| **Phase 4** | NoteMarkdownPreview，Preview/List/RecycleBin 迁移 | 1–2 天 |
| **Phase 5** | 删除 md-editor-v3，更新 docs，pnpm build，验证清单 | 0.5 天 |

---

## 12. 验证清单

| # | 场景 | 预期 |
|---|------|------|
| 1 | 新建笔记，右键插入 H3 | IR 即时渲染三级标题 |
| 2 | 右键表格 3×4 | 生成 3 列 4 行 GFM 表格 |
| 3 | 插入脚注 | `[^1]` + 底部定义，预览可点击 |
| 4 | 插入链接引用 | `[text][ref]` + 底部 URL |
| 5 | 插入块级/行内公式 | KaTeX 渲染正确 |
| 6 | 插入 Mermaid | 图表渲染 |
| 7 | 选区加粗/链接 | 包裹正确，光标定位 url |
| 8 | 粘贴/全选/复制 | 正常 |
| 9 | 图片右键 + 拖拽上传 | POST images API |
| 10 | 自动保存 2s + Ctrl+S | dirty → saving → success |
| 11 | Focus Mode + Esc | chrome 隐藏/恢复 |
| 12 | Preview.vue 阅读主题/TOC | 与 IR 渲染一致 |
| 13 | 列表卡片右键 | 编辑/预览/收藏/删除 |
| 14 | 导入 .md（GFM 扩展） | 内容完整 |
| 15 | 暗色主题切换 | Vditor + 预览同步 |
| 16 | 已有笔记打开 | Markdown 兼容无乱码 |

---

## 13. 风险与应对

| 风险 | 概率 | 应对 |
|------|------|------|
| Vditor IR 与历史 Markdown 不兼容 | 中 | 用现有笔记样本回归；存储格式不变可回滚 |
| 包体增大影响首屏 | 中 | lazy load + chunk 拆分 + 静态资源本地化 |
| 右键菜单与 Vditor 内部事件冲突 | 低 | 限定 `.vditor-ir` 区域；阻止冒泡 |
| 脚注编号冲突 | 低 | `nextFootnoteIndex()` 扫描自增 |
| 光标定位不精确 | 中 | 优先 `insertMD`；复杂场景二次 `focus()` |
| md-editor-v3 移除影响其他模块 | 无 | 已确认仅 note 模块使用 |

---

## 14. 不在本次范围

- 后端 API / 数据库 / 表结构变更
- AI 助手右键菜单
- 日记模块编辑器迁移（后续独立任务）
- 多标签页编辑
- 协同编辑

---

## 15. 实现后文档同步

实现完成并验证通过后，同步更新：

| 文档 | 变更 |
|------|------|
| `docs/TECH_STACK.md` | md-editor-v3 → vditor |
| `docs/STYLE_GUIDE.md` | NoteVditor、ContextMenu、Editor Composable 表 |
| `docs/CHANGELOG.md` | 重构条目 |
| `docs/PROJECT.md` | 路线图里程碑 |
| `README.md` | 编辑器描述 |

---

## 16. 附录：快捷键保留

| 快捷键 | 行为 | 来源 |
|--------|------|------|
| `Ctrl+S` | 强制保存 | useAutoSave |
| `Ctrl+Shift+P` | 编辑 ↔ 预览 | useEditorMode |
| `Ctrl+Shift+F` | 浏览器全屏 | useEditorMode |
| `Esc` | 退出 Focus / 全屏 | useEditorMode |
| Typora 原生 | `#`、`*`、`-` 等 Markdown 快捷键 | Vditor IR 内置 |

**移除**：`Ctrl+Shift+L`（分栏预览）
