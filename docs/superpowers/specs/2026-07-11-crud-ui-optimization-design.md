# Personal Hub CRUD UI 优化设计

> 基于「Personal Hub - CRUD UI 设计规范」对项目进行系统化优化
> 设计日期：2026-07-11

---

## 背景

当前 Personal Hub 的 CRUD 交互存在以下问题：

1. **CRUD UI 规范分散** — 完整的 CRUD UI 规范存在于项目根目录的独立 `.md` 文件中，未整合进 `docs/` 体系
2. **主实体使用全页面表单** — Todo、学习记录、阅读记录、书签等所有主要实体均使用路由跳转式 `Form.vue`，不符合 Notion/Linear 风格的 Dialog/Drawer 体验
3. **无统一 UI 组件库** — 页面对 Element Plus 的直接使用没有封装层，样式难以统一修改
4. **Dialog 样式不一致** — 现有内联 Dialog（标签/分类管理）宽度、表单、按钮均未遵循统一规格

---

## 执行策略

**增量式推进，每阶段完成后用户确认再提交：**

```
阶段 1 ─→ 阶段 2 ─→ 阶段 3a ─→ 阶段 3b ─→ 阶段 4
文档更新    组件封装    简单→Dialog   中等→Drawer    收尾
```

---

## 阶段 1：STYLE_GUIDE.md 更新

将 CRUD 交互规范合并到现有 `docs/STYLE_GUIDE.md`。

### 新增章节

在现有「UI/UX 设计原则」大章节中新增以下小节：

#### 1.1 CRUD 交互模式

定义三种交互方式及其适用场景：

| 模式 | 适用 | 示例模块 |
|------|------|---------|
| **Dialog** | 简单实体（字段少、操作快） | Todo、标签、分类、收藏夹、文件分类 |
| **Drawer** | 中等复杂度（需预览上下文） | 学习记录、阅读记录、学习计划、文件/通知详情 |
| **Full Page** | 专注编辑（内容量大） | Markdown 编辑器、日记编辑、Dashboard 自定义 |

#### 1.2 Dialog 设计规范

- 尺寸：520px（默认）/ 600px（复杂）/ 720px（最大），不超过 800px
- 圆角：16px
- Padding：24px
- Header：仅标题 + 关闭按钮，无图标/彩色背景
- Footer：取消(Default) + 保存(Primary)，按钮右对齐

#### 1.3 表单设计规范

- Label：14px/500w/`--text-primary`
- 输入框高度：40px
- Textarea：自动高度或 4~6 行
- 字段间距：20px
- Label 与 Input 间距：6px
- Section 间距：24px
- 每个字段是独立内容块，不是传统表单行

#### 1.4 页面布局规范

```
Card ─→ Section ─→ Field
```
不是传统 `Form ─→ FormItem` 堆砌。

#### 1.5 CRUD 页面映射表

| 功能 | UI 模式 | 文件名模式 |
|------|---------|-----------|
| Todo | Dialog | `TodoList.vue` + `TodoDialog.vue` |
| Tag | Dialog | `TagManage.vue` |
| Category | Dialog | `CategoryManage.vue` |
| Bookmark | Dialog | `BookmarkList.vue` + `BookmarkDialog.vue` |
| File Category | Dialog | `CategoryManage.vue` |
| Reading Record | Drawer | `ReadingList.vue` + `ReadingDrawer.vue` |
| Study Record | Drawer | `StudyList.vue` + `StudyDrawer.vue` |
| Study Plan | Drawer | `StudyPlanList.vue` + `StudyPlanDrawer.vue` |
| Markdown | Full Page | `NoteEditor.vue` |
| Diary | Full Page | `DiaryForm.vue` |

#### 1.6 UX 原则

补充「不打断用户、保留上下文、操作路径最短」原则。

#### 1.7 组件封装要求

明确要求业务页面禁止直接使用 `el-dialog`/`el-input`/`el-select` 等 Element Plus 组件进行布局，必须通过 `Ui` 前缀组件封装。

### 需要合并/覆盖的现有内容

- 「按钮规范」部分 (lines 329-333)：补充 Dialog Footer 中的按钮配对规则
- 「Element Plus 使用规范」部分 (lines 392-401)：补充 Dialog 覆盖规范
- 「新页面模板」部分 (line 361)：扩展为完整 CRUD 页面模板
- 「Checklist」部分 (lines 409-416)：增加 CRUD 交互检查项

---

## 阶段 2：UI 组件库封装

在 `personal-hub-web/src/components/ui/` 下创建统一组件。

### 组件清单

| 组件 | 封装目标 | 关键 Props |
|------|---------|-----------|
| `UiDialog.vue` | `el-dialog` | `modelValue`, `title`, `width`(520/600/720) |
| `UiInput.vue` | `el-input` | `modelValue`, `placeholder`, `type` |
| `UiTextarea.vue` | `el-input textarea` | `modelValue`, `placeholder`, `rows`, `autoSize` |
| `UiSelect.vue` | `el-select` | `modelValue`, `options`, `placeholder` |
| `UiDatePicker.vue` | `el-date-picker` | `modelValue`, `type`(date/month) |
| `UiButton.vue` | `el-button` | `type`(primary/default/danger), `loading` |
| `UiSection.vue` | 表单区块容器 | `title`, `description?` |
| `UiCard.vue` | 卡片容器 | `padding?`, `hoverable?` |

### 设计要点

- 每个组件是 thin wrapper，预设规范中的尺寸/圆角/间距
- 通过 `$attrs` 透传未声明的属性和事件，不限制 Element Plus 的灵活性
- 导出 `index.ts` 桶文件

### 组件不使用目录结构

```
src/components/ui/
├── index.ts
├── UiDialog.vue
├── UiInput.vue
├── UiTextarea.vue
├── UiSelect.vue
├── UiDatePicker.vue
├── UiButton.vue
├── UiSection.vue
└── UiCard.vue
```

---

## 阶段 3a：简单实体 → Dialog 重构

### 重构清单

| 模块 | 当前 | 目标 | 改动要点 |
|------|------|------|---------|
| **Todo** | `Form.vue` 全页面 | `TodoDialog.vue` 内联 Dialog | 在 List.vue 中嵌入 Dialog，移除独立路由 |
| **Tag** | 内联 Dialog | 使用 UiDialog 统一样式 | 替换 `el-dialog` → `UiDialog` |
| **Category**(note/bookmark/file) | 内联 Dialog | 使用 UiDialog 统一样式 | 替换 `el-dialog` → `UiDialog` |
| **Bookmark** | `Form.vue` 全页面 | `BookmarkDialog.vue` 内联 Dialog | 在 List.vue 中嵌入 Dialog |
| **File Category** | 内联 Dialog | 使用 UiDialog 统一样式 | 替换 `el-dialog` → `UiDialog` |

### Dialog 重构模式（以 Todo 为例）

```
重构前：
  TodoList.vue          ← 列表路由
  TodoForm.vue          ← /todo/new, /todo/:id/edit 独立路由

重构后：
  TodoList.vue          ← 唯一路由，内嵌 TodoDialog
    └─ TodoDialog.vue   ← 新增/编辑共用一个 Dialog 组件
```

### 通用模式

每个 Dialog 组件：
- Props: `modelValue`(visible), `entityId`(编辑时传 ID)
- Emits: `update:modelValue`, `saved`(保存后通知列表刷新)
- 新增/编辑逻辑通过 `entityId` 区分：无 ID 为新增，有 ID 为编辑

### 路由变更

移除以下路由（如果存在）：
- `/todo/new`, `/todo/:id/edit`
- `/bookmark/new`, `/bookmark/:id/edit`

核心路由只保留列表页。

---

## 阶段 3b：中等实体 → Drawer 重构

### 重构清单

| 模块 | 当前 | 目标 | 改动要点 |
|------|------|------|---------|
| **Reading Record** | `Form.vue` 全页面 | `ReadingDrawer.vue` Drawer | 保留列表上下文 |
| **Study Record** | `Form.vue` 全页面 | `StudyDrawer.vue` Drawer | 保留列表上下文 |
| **Study Plan** | `Form.vue` 全页面 | `StudyPlanDrawer.vue` Drawer | 保留列表上下文 |

### Drawer 设计规范

- 宽度：480px（默认）/ 560px（复杂内容）
- 保留 `el-drawer` 原生的滑动进入效果
- Header：标题 + 关闭按钮（与 Dialog 一致）
- Footer：取消 + 保存（与 Dialog 一致）
- 打开时列表背景保持可见

### Drawer 重构模式

与 Dialog 一致：在 List.vue 中嵌入 Drawer 组件，通过 `modelValue` 控制显隐。

---

## 阶段 4：收尾清理

1. 删除旧的 `Form.vue` 文件（已迁移的模块）
2. 删除对应的路由配置
3. 全局样式统一验证
4. 深色模式/响应式验证

---

## 不变的内容

以下页面保持 Full Page 不变：
- **笔记编辑器** (`note/Editor.vue`) — 需要专注 Markdown 编辑
- **日记编辑** (`diary/Form.vue`) — 同上
- **Dashboard 自定义** (`settings/components/DashboardManager.vue`) — 大型配置
- **系统设置** (`settings/SettingsView.vue`) — 配置页面

---

## 影响范围

### 需修改的文件

| 阶段 | 文件 |
|------|------|
| 阶段 1 | `docs/STYLE_GUIDE.md` |
| 阶段 2 | `src/components/ui/*.vue` (8 个新文件) |
| 阶段 3a | `src/modules/planning/todo/List.vue`, `src/modules/resource/bookmark/List.vue`, 各标签/分类 Manage.vue（5 个） |
| 阶段 3b | `src/modules/knowledge/reading/List.vue`, `src/modules/knowledge/study/List.vue`, `src/modules/planning/studyplan/List.vue` |
| 阶段 4 | 路由文件、删除旧 Form.vue |

### 不涉及的文件

- 后端 Java 代码（纯前端改造）
- `src/styles/global.css`（现有 Dialog 样式覆盖已可用）
- 笔记/日记编辑器

---

## 验证方式

- **阶段 1**：阅读 `docs/STYLE_GUIDE.md` 确认章节完整、无矛盾
- **阶段 2**：在 dev 环境引入组件，确认渲染正常，无样式冲突
- **阶段 3a/b**：每个模块改完后逐一测试：
  - 新建 → Dialog/Drawer 弹出 → 填写 → 保存成功 → 列表刷新
  - 编辑 → Dialog/Drawer 弹出 → 预填数据 → 修改保存成功
  - 点击取消 → Dialog/Drawer 关闭 → 列表不变
  - 深色模式切换验证
- **阶段 4**：运行 `npm run dev` 无报错，所有 CRUD 功能正常
