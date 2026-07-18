# 编辑框「编辑器气质」改造（方案 B）

> 日期：2026-07-18  
> 状态：已落地  
> 痛点：B 控件不统一 · C 疏密别扭 · D 缺产品感

## 目标

业务编辑表面统一成「标题区 → 属性块 → 写作区 → Footer」骨架，控件与疏密一致，具备 Personal Hub 产品感；不改字段语义与 API。

## 范围

| 纳入 | 排除 |
|------|------|
| 待办 / 日记 / 阅读 / 学习计划 / 学习记录 / 收藏 Dialog | 笔记 Full Page 编辑器 |
| 标签 / 分类 Manage 内嵌 Dialog（短表单变体） | 笔记 Markdown 导入弹窗 |
| ProfileDrawer（仍为 Drawer，气质对齐） | 双栏布局（方案 C） |
| | MessageBox 确认框换肤 |
| | 接口 / 字段语义变更 |

## 信息架构

### 标准实体弹窗

1. **壳**：`UiDialog`（size 沿用 sm/md/lg）+ 可选 subtitle  
2. **标题区**：`DialogTitleField`（无边框大字；Study 等例外一并收口）  
3. **属性区**：`DialogPropGrid` + `DialogPropCard`（1～2 列）；块内放 Choice / Chip / Date / Switch / Slider 等  
4. **写作区**：`DialogEditor`（borderless textarea，高度档位）  
5. **Footer**：既有 `DialogFooterActions`

少用 `DialogDivider`；靠属性块间距形成呼吸感。

### 短表单变体（标签 / 分类）

- 无写作区  
- 字段仍包在 `DialogPropCard` 内（名称、颜色等）  
- `size="sm"`

### 资料抽屉

- 保持 `el-drawer` + 现有 Tab  
- 表单控件包进 `DialogPropCard`（或同名 Drawer 别名组件，样式同源）  
- Footer 对齐 `UiButton` / 与弹窗主次按钮一致  
- 不改 Tab 信息架构，不改成 Dialog

## 新增积木

| 组件 | 职责 |
|------|------|
| `DialogTitleField` | 无边框大标题输入，统一字号/字重/placeholder |
| `DialogPropCard` | 圆角属性块；可选 `label` |
| `DialogPropGrid` | 属性块网格，默认 2 列，窄宽可叠 |
| `DialogChoiceRow` | 优先级 / 心情 / 状态等同级单选卡片行 |
| `DialogChipRow` | 标签/分类多选或单选 chip 行 |
| `DialogDateChip` | 日期 pill + 原生 date input 隐藏层 |
| `DialogEditor` | 写作区；`size`: sm≈140 / md≈200 / lg≈280 |
| 既有 | `UiDialog` · `DialogSection`（可保留给短标签）· `DialogDivider`（慎用）· `DialogFooterActions` · `ImageLightbox` |

进度条、星级、URL 行、配图 dropzone：本轮可先抽公共 CSS class（或轻量组件），优先保证外层骨架与属性块一致。

## 疏密与视觉约定

- PropCard：`border-radius: var(--radius-lg)`；padding `var(--sp-3)`；网格 gap `10–12px`  
- 属性块内控件间距统一，消灭各 Dialog 复制的 `.section-label` / `.chip-row` / `.status-card` 分叉样式  
- 写作区：待办/日记正文 → `lg`；阅读笔记/收藏描述/计划备注 → `md`；学习双栏内容/心得 → `sm` 或 `md`  
- Bookmark「展示到首页」：`el-switch` 放进 PropCard，带短 hint  
- 弹窗内滚动条继续隐藏（已有全局规则）

## 迁移顺序

1. 落地积木 + STYLE_GUIDE 条目  
2. Todo（样板）→ Study（收口标题）→ Bookmark（开关进块）  
3. Reading / StudyPlan（滑条、日期、状态）  
4. Diary（心情/天气/配图进属性区 + 写作区）  
5. Tag / Category 短表单  
6. ProfileDrawer 气质对齐  
7. 扫尾：删死 CSS、`vue-tsc`、更新 CHANGELOG

## 成功标准

- 打开任意纳入范围的编辑表面，一眼能分出「标题 / 属性 / 正文」  
- 同种控件（状态卡、日期 pill、chip）视觉一致  
- 无新增 API；笔记相关零改动  
- 窄屏属性网格可单列，不裁切 Footer

## 参考

- 前序壳统一：`docs/superpowers/specs/2026-07-18-dialog-ui-unification-design.md`  
- 头脑风暴方向：编辑器气质（非精修 A、非双栏 C）
