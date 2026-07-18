# 编辑框「编辑器气质」实现计划

> **面向 AI 代理的工作者：** 使用 executing-plans 或 subagent-driven-development 逐任务实现。步骤用复选框跟踪。

**目标：** 业务 Dialog + ProfileDrawer 统一为「标题区 → 属性块 → 写作区 → Footer」编辑器气质；笔记不动。

**架构：** 在 `components/ui` 新增积木，实体 Dialog 改为组合积木；删各页复制的 section/chip/date CSS。ProfileDrawer 仅包 PropCard + Footer 按钮对齐。

**技术栈：** Vue 3 · Element Plus（壳/开关）· 现有 CSS Token

**规格：** `docs/superpowers/specs/2026-07-18-dialog-editor-character-design.md`

---

## 文件结构

**创建**
- `personal-hub-web/src/components/ui/DialogTitleField.vue`
- `personal-hub-web/src/components/ui/DialogPropCard.vue`
- `personal-hub-web/src/components/ui/DialogPropGrid.vue`
- `personal-hub-web/src/components/ui/DialogChoiceRow.vue`
- `personal-hub-web/src/components/ui/DialogChipRow.vue`
- `personal-hub-web/src/components/ui/DialogDateChip.vue`
- `personal-hub-web/src/components/ui/DialogEditor.vue`
- `personal-hub-web/src/styles/dialog-editor.css`（可选：滑条/URL 等轻量共用类）

**修改**
- `personal-hub-web/src/components/ui/index.ts`
- 各 `*Dialog.vue` + Tag/Category Manage 内弹窗
- `personal-hub-web/src/components/ProfileDrawer.vue`
- `docs/STYLE_GUIDE.md` · `docs/CHANGELOG.md`

**验证：** `pnpm exec vue-tsc --noEmit`（在 `personal-hub-web`）

---

### 任务 1：积木组件

- [ ] 创建上表 7 个 Vue 组件并 export
- [ ] `DialogTitleField`：`v-model` string，基于 `UiInput` + 无边框标题样式
- [ ] `DialogPropCard`：props `label?: string`；默认槽
- [ ] `DialogPropGrid`：props `cols?: 1 | 2` 默认 2；CSS grid + 窄屏 1 列
- [ ] `DialogChoiceRow`：props `options: { value, label, emoji?, color? }[]`，`modelValue`，emit update；横向选择卡
- [ ] `DialogChipRow`：props `options: { id, name, color?, icon? }[]`，`modelValue` number | number[] | null，`multiple?`；chip 行
- [ ] `DialogDateChip`：`modelValue: string | null`，`placeholder`，可清空
- [ ] `DialogEditor`：`v-model`，`size: 'sm'|'md'|'lg'`，`placeholder`
- [ ] Commit：`feat: 添加编辑器气质 Dialog 积木组件`

### 任务 2：Todo 样板

- [ ] 重写 `TodoDialog.vue`：TitleField → PropGrid(优先级 Choice + 日期 DateChip) → Editor lg
- [ ] 删除本地 `.title-input` / `.priority-*` / `.due-date-*` / `.content-editor` 死样式
- [ ] `vue-tsc` 通过
- [ ] Commit：`style: 待办弹窗改为编辑器气质骨架`

### 任务 3：Study + Bookmark

- [ ] `StudyDialog`：TitleField；日期+时长进 PropGrid；两个 Editor（内容/心得）
- [ ] `BookmarkDialog`：TitleField + URL 行；分类/标签 ChipRow；首页开关进 PropCard；Editor md
- [ ] Commit

### 任务 4：Reading + StudyPlan

- [ ] 状态 ChoiceRow；进度/日期进 PropCard；Editor md
- [ ] 滑条/星级可留局部样式，外层必须 PropGrid/Card
- [ ] Commit

### 任务 5：Diary

- [ ] 心情 ChoiceRow；天气·地点 / 配图进 PropCard；Editor lg
- [ ] 保留 ImageLightbox + dropzone 逻辑
- [ ] Commit

### 任务 6：Tag / Category + ProfileDrawer

- [ ] 短表单：字段进 PropCard，`size=sm`
- [ ] ProfileDrawer：表单区分组包 PropCard；Footer 用 UiButton
- [ ] Commit

### 任务 7：文档与扫尾

- [ ] STYLE_GUIDE 积木表 + 骨架约定
- [ ] CHANGELOG 2026-07-18 条目
- [ ] 全量 `vue-tsc`；规格状态改为「实现中/已落地」
- [ ] Commit：`docs: 同步编辑器气质改造说明`
