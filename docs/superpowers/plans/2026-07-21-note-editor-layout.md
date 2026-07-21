# 笔记编辑页布局优化 实现计划

> **面向 AI 代理的工作者：** 按任务顺序实现；步骤用复选框跟踪。

**目标：** 日常编辑栏宽 1280px；专注模式常驻细工具头；预览宽对齐。

**架构：** 仅前端样式/显示逻辑；App focus-hidden 不变；去掉 header 专注隐藏。

**技术栈：** Vue 3 · 既有 Editor / EditorHeader

---

## 文件

| 文件 | 职责 |
|------|------|
| `Editor.vue` | max-width 1280；回链跟父宽；专注内容区 |
| `EditorHeader.vue` | 专注矮条样式，取消 display:none |
| `PAGE_SPEC.md` / `CHANGELOG.md` | 文档 |

### 任务 1：加宽内容栏 + 回链

### 任务 2：专注 header 常驻细条

### 任务 3：文档 + 提交
