# Mermaid / KaTeX 毕业实现计划

> **面向 AI 代理的工作者：** 按任务顺序实现；步骤用复选框跟踪。

**目标：** 将 Mermaid/KaTeX 从实验开关毕业为始终可用的正式笔记能力，并统一文件 md 预览。

**架构：** 从 `FeatureFlags` 移除两键；右键菜单常显；选区可包行内公式；`FilePreviewDialog` 复用 `buildPreviewOptions`；文档同步。

**技术栈：** Vue 3 · Pinia · Vditor · Vitest

---

## 文件

| 文件 | 职责 |
|------|------|
| `featureFlagStore.ts` / `types/layout.ts` | 去掉 mermaid/katex；加载时剥离旧键 |
| `ExperimentalFeatures.vue` | 仅 AI/双向链接；「恢复默认」文案 |
| `EditorContextMenu.vue` | 去掉 flag 门控；选区加行内公式 |
| `FilePreviewDialog.vue` | 用 `buildPreviewOptions` |
| `contextMenuActions.test.ts` | 补 mermaid/公式插入断言 |
| `PROJECT.md` / `PAGE_SPEC.md` / `CHANGELOG.md` | 产品文档 |

### 任务 1：Store / 类型

- [x] 从 `FeatureFlags` 移除 `mermaid`/`katex`
- [x] `loadFlags` 合并后剥离遗留键并写回
- [x] `FLAG_META` 只留 aiAssistant、backlink
- [x] 「关闭全部」→「恢复默认」

### 任务 2：右键菜单

- [x] 去掉 flag 门控；无选区常显；有选区「行内公式」

### 任务 3：文件预览

- [x] markdown 分支改为 `buildPreviewOptions(theme)`

### 任务 4：测试

- [x] 单测覆盖 insert mermaid/math
- [x] 跑相关 vitest

### 任务 5：文档与提交

- [x] 更新 PROJECT / PAGE_SPEC / CHANGELOG
- [x] 提交（不 push）
