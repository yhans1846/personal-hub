# Personal Hub — UI 设计规范

> 版本 1.0 · 风格参考: Notion / Linear / Obsidian / GitHub / Raycast
> 全部 Token 实现在 `frontend/src/styles/global.css`

---

## 1. 设计 Token

### 1.1 间距（8px 基准）

| Token | 值 | 使用场景 |
|-------|-----|---------|
| `--sp-1/2/3/4/5/6/8/10/12` | 4 / **8** / 12 / **16** / 20 / **24** / **32** / 40 / 48 | 核心 8/16/24/32，禁止随机值 |

### 1.2 圆角 & 阴影

| Token | 值 | 场景 |
|-------|-----|------|
| `--radius-sm/md/lg/xl/full` | 6px / 10px / **12px** / 16px / 9999px | 按钮 → 卡片 → 标签云 |
| `--shadow-sm/md/lg/xl` | 逐级增强 | 卡片常态 → hover → 下拉 → 弹窗 |

### 1.3 色彩

| 场景 | 浅色 | 深色 |
|------|------|------|
| 背景 `--bg-body` | `#F8FAFC` | `#0B1120` |
| 卡片 `--bg-card` | `#FFFFFF` | `#1E293B` |
| 边框 `--border-color` | `#E2E8F0` | `#334155` |
| 文字 `--text-primary` | `#0F172A` | `#F1F5F9` |
| 次要文字 `--text-secondary` | `#64748B` | `#94A3B8` |
| **主色 `--accent`** | `#3B82F6` | `#60A5FA` |
| 危险/警告/成功 | `#EF4444` / `#F59E0B` / `#10B981` | 同色浅色版 |

### 1.4 字体 & 布局

| Token | 值 |
|-------|-----|
| 字体栈 | `system-ui, -apple-system, 'Segoe UI', 'Noto Sans SC', sans-serif` |
| 等宽 | `'SF Mono', 'Fira Code', Consolas, monospace` |
| 字号 | xs:12 sm:14 base:16 lg:18 xl:20 2xl:24 3xl:30 (px) |
| 侧栏/顶栏 | 240px / 52px |
| 内容/阅读宽 | 1200px / 800px |

---

## 2. 组件规范

| 组件 | 圆角 | 背景 | 边框 | 其他 |
|------|------|------|------|------|
| **卡片** | `--radius-lg` (12px) | `--bg-card` | `1px solid var(--border-color)` | Hover `--shadow-md`；padding 16-20px |
| **按钮** | `--radius-sm` (6px) | `--accent`(主) / transparent(默认) | 同背景 | 字重 500；图标按钮 hover 显示背景 |
| **输入框** | `--radius-sm` | `--bg-card` | `1px solid var(--border-color)` | Focus `2px solid var(--accent)` |
| **表格** | `--radius-lg` (整体) | 透明表头 | 仅行底分割线 | 无斑马纹，hover `--bg-hover` |
| **对话框** | `--radius-xl` (16px) | `--bg-elevated` | 无 | `--shadow-xl`，标题/内容 padding 分层 |
| **空状态** | — | — | — | Lucide 48px + 引导文案 + 操作按钮 |
| **侧栏** | 项 `--radius-sm` | 项透明 | 无 | 激活 `--accent-light` + `--accent` 文字 |
| **标签** | 4px | `--bg-hover` 或语义浅色 | 无 | 12px，字重 500 |

### 空状态标准模板

```html
<div class="empty-state">
  <div class="empty-state__icon"><LucideIcon :size="48" /></div>
  <div class="empty-state__text">引导文案</div>
  <el-button type="primary" @click="goCreate">操作按钮</el-button>
</div>
```

---

## 3. 页面布局

| 类型 | 结构 | 最大宽 |
|------|------|--------|
| **Dashboard** | 欢迎区 + 3 卡片垂直排列（待办/笔记/学习） | 800px |
| **列表页** | page-header + toolbar(搜索/筛选 + 按钮) + 卡片网格/列表/时间线 | 1200px |
| **表单页** | 返回按钮 + 标题 + 卡片包裹的 label-top 表单 | 640px |
| **编辑器** | Meta 栏(分类/标签/保存) + 大标题 + 等宽编辑区 | 800px |

**铁律**：列表页不用 table（改用卡片网格 / 列表 / 时间线）；hover 操作按钮默认隐藏，悬停显示。

---

## 4. 交互

| 状态 | 实现 |
|------|------|
| 悬浮操作 | `opacity: 0` → `1` on hover, 150ms |
| 加载 | 骨架屏 `--bg-hover` + `pulse 1.5s` |
| 反馈 | `ElMessage.success/error()` |
| 删除确认 | `ElMessageBox.confirm()` |
| 过渡 | 所有 `150ms ease` |

---

## 5. 图标

- 图标库：**Lucide Icons**（`lucide-vue-next`），禁止内联 SVG
- 尺寸：默认 16px，按钮内 14px，空状态 48px
- 常用映射：`FileText`(笔记) `BookOpen`(学习) `CheckSquare`(待办) `LayoutDashboard`(首页) `Plus`(新建) `Search`(搜索) `Trash2`(删除) `Pencil`(编辑) `ArrowLeft`(返回)

---

## 6. 文件结构

```
frontend/src/
├── styles/global.css  ← Token + Element Plus 覆盖（唯一全局样式入口）
├── api/ router/ store/ types/ views/ components/
```

> **铁律**：所有新增/修改页面以此规范为准，global.css 为主控文件。
