# Personal Hub 系统设置 V2 设计规范

**Version:** 2.0  
**Date:** 2026-07-12  
**Status:** Final (Phase 1 & 2 implemented)  

---

## 一、设计目标

系统设置不仅是功能配置页面，更是 Personal Hub 的"工作台配置中心（Workspace Preferences）"。

- **简洁** — 不冗余、不堆砌，每个 Tab 聚焦一个主题
- **所见即所得** — 修改即时生效，无需点击保存按钮
- **可扩展** — 模块化 Tab 架构，新增模块不伤现有结构
- **一致** — 所有 Tab 共用 Card/Section/间距规范
- **自动保存** — 拖拽排序、开关切换等操作自动持久化

参考产品：Notion Settings、Obsidian Preferences、VS Code Settings、Raycast Extensions、Arc Browser Preferences。

---

## 二、整体架构

### 2.1 页面布局

```
┌──────────────────────────────────────────┐
│  系统设置                                 │  ← PageHeader（标题 + 描述）
│  自定义你的工作台                          │
├──────────────────────────────────────────┤
│   ☐ 工作台  │  📖 阅读  │  🎨 外观  │ …  │  ← Tab 导航（Icon + Label）
├──────────────────────────────────────────┤
│                                          │
│   ┌────────────────────────────────┐     │
│   │  Card                          │     │  ← 各 Tab 内容
│   │  ── Section ──                │     │
│   │  控件 控件 控件                 │     │
│   └────────────────────────────────┘     │
│                                          │
│   ┌────────────────────────────────┐     │
│   │  Card                          │     │
│   │  ...                           │     │
│   └────────────────────────────────┘     │
│                                          │
└──────────────────────────────────────────┘

max-width: 1080px, margin: auto, padding: 32px
```

### 2.2 Tab 导航规范

使用横向 Tab（不同于侧边栏导航），每个 Tab 包含 Icon + Label：

| Icon | Tab | 说明 |
|------|-----|------|
| ☐ / LayoutDashboard | 工作台 | 菜单管理 + Dashboard 布局 |
| 📖 / BookOpen | 阅读体验 | 字体/主题/Markdown |
| 🎨 / Palette | 外观 | 主题色/强调色/圆角 |
| 🔔 / Bell | 通知 | 通知偏好（Phase 2） |
| 💾 / Save | 数据 | 缓存/备份/导入导出（Phase 2） |
| 🧪 / FlaskConical | 实验功能 | Beta 功能开关（Phase 2） |

- Tab 激活态：accent 色下划线 + 轻微背景
- Tab hover：背景微变
- Tab 切换：内容 fadeIn 200ms
- Tab 固定为横向一行，不换行，超宽时 scroll

### 2.3 Card 规范

```
┌──────────────────────────────────┐
│  24px padding                    │
│                                  │
│  阅读设置         ← Section      │
│  ───────────────                 │
│  字号  A−  18  A+               │
│  宽度  [窄] [标准] [宽]          │
│  行高  ─────●──────  1.8        │
│                                  │
│  主题             ← Section      │
│  ───────────────                 │
│  [☀️ 浅色] [🌙 深色] [🌿 护眼]   │
│                                  │
└──────────────────────────────────┘
```

- Border: `1px solid var(--border-color)`
- Radius: `12px`（`--radius-lg`）
- Padding: `24px`
- Card 间距: `24px`
- 背景: `var(--bg-card)`

### 2.4 Section 规范

- Section Title: 13px / 600w / `var(--text-secondary)`
- Section 分割线: 1px solid `var(--border-light)`
- Section 间距: 32px（上下）
- 控件间距: 16px（左右）
- Label 宽度: 80px（固定）
- 禁止同一 Card 中出现超过 4 种字号

---

## 三、模块详细设计

### 3.1 工作台（现有改造）

**目标：** 保持现有 MenuManager + DashboardManager 功能，UI 升级对齐 Card/Section 规范。

#### 菜单管理

```
┌──────────────────────────────────┐
│  菜单管理                         │
│  ─────────────────────────       │
│  拖拽排序，点击切换显示/隐藏       │
│                                  │
│  ▼ 工作区                        │
│    ⋮⋮  首页                👁    │
│                                  │
│  ▼ 知识                          │
│    ⋮⋮  笔记                👁    │
│    ⋮⋮  日记                👁    │
│    ...                           │
│                                  │
│  [恢复默认]                      │
└──────────────────────────────────┘
```

- 保留现有 SortableJS 拖拽、分组折叠、可见性切换
- 视觉上对齐 Card/Section 规范
- 恢复默认按钮放 Card 底部

#### Dashboard 布局

```
┌──────────────────────────────────────┐
│  Dashboard 卡片                       │
│  ────────────────────────────         │
│  拖拽排序，隐藏的卡片不会出现在首页     │
│                                       │
│  ⋮⋮  今日计划                    👁   │
│  ⋮⋮  最近阅读                    👁   │
│  ⋮⋮  待办任务                    👁   │
│  ...                                │
│                                       │
│  [恢复默认]                           │
└──────────────────────────────────────┘
```

- 使用与 MenuManager 一致的列表样式
- 同步更新 `layoutStore` 即时生效
- 保持现有功能不变

### 3.2 阅读体验（现有扩展）

**目标：** 保留现有功能，补充代码字体/段距等选项。

```
┌──────────────────────────────────────┐
│  阅读设置                             │
│  ────────────────────────────         │
│  字号    A−  18  A+                  │
│  宽度    [窄] [标准] [宽]             │
│  行高    ─────●────────  1.8         │
│  段距    ──●───────────  1.2em       │  ← 新增
│  图片    [60%] [70%] [80%] [90%] [100%]│
│  代码字体   [系统] [等宽]              │  ← 新增
│  代码字号   同正文  [13px] [14px]     │  ← 新增
│                                       │
│  ────────────────────────────         │
│  预览主题                             │
│                                       │
│  [☀️ 浅色] [🌙 深色] [🌿 护眼] [💻 跟随]│
│                                       │
│  [恢复默认设置]                        │
└──────────────────────────────────────┘
```

**新增配置项：**
- `paragraphGap` — 段距（1.0em / 1.2em / 1.5em），CSS 变量 `--md-paragraph-gap`
- `codeFontSize` — 代码字号（same / 13px / 14px），CSS 变量 `--md-code-font-size`
- `codeFontFamily` — 代码字体（system / monospace），CSS 变量 `--md-code-font-family`

**后端存储：** 沿用 `readingConfigStore`（localStorage + `layout_type='preview'`）

### 3.3 外观（全新模块）

**目标：** 将 AppLayout 顶栏散落的主题/强调色设置迁入设置页，新增圆角/动画选项。

```
┌──────────────────────────────────────┐
│  主题                                 │
│  ────────────────────────────         │
│                                       │
│  [☀️ 浅色           ] [🌙 深色       ]│
│  白底深色文字        深底浅色文字      │
│                                       │
│  ────────────────────────────         │
│  强调色                               │
│                                       │
│  [🔵 蓝色] [🟣 紫色] [🟢 绿色]       │
│  [🟠 橙色] [🟦 青色]                  │
│                                       │
│  ────────────────────────────         │
│  界面密度                             │
│                                       │
│  [舒适] [标准] [紧凑]                  │  ← 新增（预留）
│                                       │
└──────────────────────────────────────┘
```

**实现要点：**
- 主题切换：使用现有 `data-theme` 属性 + `localStorage('theme-preference')`
- 强调色切换：使用现有 `data-accent` 属性 + `localStorage('accent-preference')`
- 外观设置存储在 `layout_type='appearance'` 的后端 `user_layout` 表
- 顶栏保留「快捷切换」下拉（主题/强调色），但主设置入口在设置页

### 3.4 通知（Phase 2）

**目标：** 提供通知偏好管理面板，控制桌面通知、声音提醒、系统消息的类型和频率。

```
┌──────────────────────────────────────┐
│  通知偏好                             │
│  ────────────────────────────         │
│                                       │
│  桌面通知                             │
│  ─────────────────                    │
│                                       │
│  启用桌面通知              [开关]     │  ← 请求浏览器 Notification API 权限
│  ──                                    │
│  通知类型                             │
│  □ 新笔记提醒                         │
│  □ 待办到期提醒                       │
│  □ 学习计划进度提醒                    │
│  □ 系统更新通知                       │
│                                       │
│  ────────────────────────────         │
│  声音                                 │
│  ─────────────────                    │
│                                       │
│  通知音效              [开关]          │
│  提示音                [下拉选择]      │  ← 预设 3-5 种音效 + 静音
│                                       │
│  ────────────────────────────         │
│  免打扰                               │
│  ─────────────────                    │
│                                       │
│  免打扰模式              [开关]        │
│  开始时间              [时间选择器]    │
│  结束时间              [时间选择器]    │
│                                       │
└──────────────────────────────────────┘
```

**功能详述：**

| 功能 | 行为 |
|------|------|
| 桌面通知开关 | 点击请求 `Notification.permission`，已授权时直接启用；拒绝时显示引导提示 |
| 通知类型 | 多选 Chip，勾选 = 接收该类通知。存储为 `enabledTypes: string[]` |
| 通知音效 | 开关控制是否播放声音。下拉菜单选择预设音效（默认/Bell/Chime/Pop） |
| 免打扰 | 开启后在设定时间段内不推送通知。免打扰期间的通知暂存为未读，结束后统一显示 |

**前端 Store 设计（`notificationConfigStore.ts`）：**

```ts
interface NotificationConfig {
  desktopEnabled: boolean            // 桌面通知
  enabledTypes: string[]             // 启用的通知类型: ['note_reminder', 'todo_due', 'study_progress', 'system']
  soundEnabled: boolean              // 通知音效
  soundName: string                  // 音效名称: 'default' | 'bell' | 'chime' | 'pop'
  doNotDisturb: boolean              // 免打扰
  dndStart: string                   // HH:mm 格式
  dndEnd: string                     // HH:mm 格式
}
```

**存储：** `localStorage('notification-config')` + `user_layout(layout_type='notification')`

**后端扩展：**
- 数据库 `user_layout` 新增 `layout_type='notification'`（表结构不变）
- 通知过滤逻辑：后端在推送通知时检查该用户的 notification config

---

### 3.5 数据（Phase 2）

**目标：** 提供缓存管理、数据备份/恢复、导入/导出能力，让用户掌握自己的数据。

```
┌──────────────────────────────────────┐
│  缓存管理                             │
│  ────────────────────────────         │
│                                       │
│  本地缓存                             │
│  当前缓存大小：约 2.3MB               │
│  [清空缓存]                           │
│  缓存说明：清空后部分页面首次加载        │
│  可能略慢，数据不会丢失。              │
│                                       │
├──────────────────────────────────────┤
│  数据备份                             │
│  ────────────────────────────         │
│                                       │
│  自动备份                [开关]        │
│  备份频率          [每天/每周/每月]     │
│  最后备份：2026-07-10 14:30           │
│  [立即备份] [导出备份]                 │
│                                       │
│  ────────────────────────────         │
│  数据恢复                             │
│  ─────────────────                    │
│                                       │
│  警告：恢复操作将覆盖当前数据，不可撤销。 │
│  [导入备份文件]                        │  ← 文件选择器，仅接受 .json.gz
│                                       │
├──────────────────────────────────────┤
│  数据导出                             │
│  ────────────────────────────         │
│                                       │
│  选择导出内容：                        │
│  ☑ 笔记 ☑ 日记 ☑ 待办                │
│  ☑ 收藏夹 ☑ 学习记录 ☑ 阅读记录       │
│  ☑ 标签 ☑ 分类 ☑ 系统配置             │
│                                       │
│  导出格式：[Markdown (ZIP)] [JSON]    │
│                                       │
│  [导出数据]                            │
│                                       │
└──────────────────────────────────────┘
```

**功能详述：**

#### 缓存管理

| 功能 | 行为 |
|------|------|
| 缓存大小估算 | 遍历 `localStorage` 计算所有 key 的 value 长度总和，格式化显示 |
| 清空缓存 | `localStorage.clear()` + 保留 `token`、`theme-preference`、`accent-preference` 等认证和偏好 key |
| 结果提示 | Toast 显示"已清空约 X MB 缓存" |

#### 数据备份

| 功能 | 行为 |
|------|------|
| 自动备份 | 基于 `@Scheduled` / Quartz 定时任务（后端），导出全量数据并写入备份表或文件 |
| 备份频率 | 前端控制 frequency 设置，后端根据配置执行 |
| 立即备份 | 触发后端 `POST /api/backup/now`，返回备份文件下载链接 |
| 导出备份 | 下载 `.json.gz` 格式的完整数据包 |
| 导入备份 | 上传 `.json.gz` → 后端校验 → 事务性恢复（失败自动回滚） |

#### 数据导出

| 功能 | 行为 |
|------|------|
| 选择内容 | 全量/勾选模块导出 |
| 导出格式 | Markdown 模式：按模块导出 `.md` 文件 + 元数据 JSON；JSON 模式：导出纯 JSON 数据结构 |
| 文件生成 | 后端 `POST /api/export` → 异步打包 → 下载链接（或直接下载流） |
| 导出历史 | `GET /api/export/history` 查看历史导出记录 |

**后端扩展：**

| 新增文件 | 说明 |
|----------|------|
| `BackupController` | `POST /backup/now`, `GET /backup/download/{id}`, `POST /backup/import` |
| `ExportController` | `POST /export`, `GET /export/history` |
| `DataBackupService` | 全量数据聚合 + 压缩 + 恢复逻辑 |
| `user_backup` 表 | 存储备份记录（无实体变更，可考虑文件系统存储） |

**新增数据库表 `user_backup`：**

| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT | 主键 |
| user_id | BIGINT | 所属用户 |
| file_path | VARCHAR(500) | 备份文件路径 |
| file_size | BIGINT | 文件大小（字节） |
| module_count | INT | 包含的模块数 |
| created_at | DATETIME | 备份时间 |
| is_deleted | TINYINT | 逻辑删除 |

#### 数据导出格式规范

**Markdown 导出结构：**
```
export-{timestamp}/
├── notes/
│   ├── 笔记标题1.md
│   └── 笔记标题2.md
├── diaries/
│   ├── 2026-07-01.md
│   └── 2026-07-02.md
├── todos.json              ← 元数据
├── bookmarks.json
├── tags.json
├── categories.json
└── export-metadata.json    ← 导出时间/版本/模块列表
```

**JSON 导出结构：**
```json
{
  "version": "2.0",
  "exportedAt": "2026-07-12T10:00:00",
  "modules": {
    "notes": [ ... ],
    "diaries": [ ... ],
    "todos": [ ... ],
    "bookmarks": [ ... ],
    "settings": { ... }
  }
}
```

---

### 3.6 外观扩展（圆角 & 动画强度）（Phase 2）

**目标：** 在外观模块中增加圆角自定义和动画强度控制，让用户微调界面视觉风格。

```
┌──────────────────────────────────────┐
│  主题                                 │
│  ────────────────────────────         │
│                                       │
│  [☀️ 浅色] [🌙 深色]                   │
│                                       │
│  ────────────────────────────         │
│  强调色                               │
│                                       │
│  [🔵 蓝] [🟣 紫] [🟢 绿] [🟠 橙] [🦋 青]│
│                                       │
│  ────────────────────────────         │
│  界面圆角                             │  ← Phase 2 新增
│                                       │
│  卡片    [4px] [8px] [12px] [16px]    │
│  按钮    [4px] [6px] [8px]            │
│                                       │
│  ────────────────────────────         │
│  动画                                 │  ← Phase 2 新增
│                                       │
│  动画速度  [关闭] [慢] [标准] [快]     │
│                                       │
|  ────────────────────────────         |
│  界面密度                             |
|                                       |
|  [舒适] [标准] [紧凑]                  |
│                                       │
└──────────────────────────────────────┘
```

**实现要点：**

- 圆角控制：通过 CSS 变量覆盖 `--radius-sm`、`--radius-md`、`--radius-lg`、`--radius-xl`
- 动画速度：通过 CSS 变量 `--transition-duration`（0ms / 100ms / 200ms / 350ms）
- 界面密度：通过 CSS 变量控制间距缩放（`--sp-density`），舒适=1.5, 标准=1.0, 紧凑=0.6
- 存储：合并到 `layout_type='appearance'` 的 `layoutJson` 中

```ts
interface AppearanceConfig {
  theme: 'light' | 'dark'
  accent: 'blue' | 'purple' | 'green' | 'orange' | 'cyan'
  borderRadius: 'sm' | 'md' | 'lg' | 'xl'       // Phase 2
  animationSpeed: 'off' | 'slow' | 'normal' | 'fast'  // Phase 2
  density: 'comfortable' | 'standard' | 'compact'     // Phase 2
}
```

---

### 3.7 实验功能（Phase 2）

**目标：** 提供 Beta 功能开关面板，让用户选择性启用尚未正式发布的功能。

```
┌──────────────────────────────────────┐
│  实验功能                             │
│  ────────────────────────────         │
│                                       │
│  实验性功能可能不稳定，请谨慎启用。     │
│                                       │
│  ────────────────────────────         │
│  功能开关                             │
│                                       │
│  Mermaid 图表渲染         [开关]       │  ← 示例
│  在编辑器和预览中支持 Mermaid          │
│                                       │
│  数学公式 (KaTeX)         [开关]       │  ← 示例
│  行内/块级数学公式渲染                 │
│                                       │
│  AI 笔记助手              [开关]       │  ← 示例
│  选中文本后触发 AI 总结/润色           │
│                                       │
│  双向链接 (Backlink)      [开关]       │  ← 示例
│  笔记间 [[引用]] 关系图谱              │
│                                       │
│  ────────────────────────────         │
│  反馈                                 │
│                                       │
│  遇到问题？[提交反馈]                  │
│                                       │
└──────────────────────────────────────┘
```

**实现要点：**

| 功能 | 行为 |
|------|------|
| 功能开关 | 每个开关存储为 `featureFlags: Record<string, boolean>` |
| 生效方式 | 开关值注入全局 `featureFlags` 对象，各功能模块自行检查 |
| 默认状态 | 所有实验功能默认关闭 |
| 存储 | `localStorage('feature-flags')` + 可选 `user_layout(layout_type='features')` |
| 动态生效 | 切换即时生效，无需刷新页面 |

```ts
interface FeatureFlags {
  mermaid: boolean          // Mermaid 图表
  katex: boolean            // 数学公式
  aiAssistant: boolean      // AI 笔记助手
  backlink: boolean         // 双向链接
}

// 全局注入方式（main.ts 或 composable）
const featureFlags = reactive<FeatureFlags>({ ... })
// 业务组件通过 composable useFeatureFlag('mermaid') 读取
```

**前端 Store 设计（`featureFlagStore.ts`）：**

```ts
export const useFeatureFlagStore = defineStore('featureFlags', () => {
  const flags = reactive<FeatureFlags>(loadFlags())
  
  function isEnabled(key: keyof FeatureFlags): boolean {
    return flags[key]
  }
  
  function toggle(key: keyof FeatureFlags) {
    flags[key] = !flags[key]
    persist()
  }
  
  return { flags, isEnabled, toggle }
})
```

---

## 四、交互规范

### 4.1 即时生效

| 操作 | 行为 |
|------|------|
| 拖拽排序 | onEnd 立即持久化（防抖 500ms 写后端） |
| 开关切换 | 立即更新 DOM + 写 localStorage + 防抖写后端 |
| 单选按钮组 | 点击立即切换 + 写 localStorage + 防抖写后端 |
| 滑块 | input 实时更新预览 + 防抖写后端 |

### 4.2 恢复默认

- 每个独立模块有各自的"恢复默认"按钮
- 点击后弹出 ConfirmDialog 二次确认
- 确认后重置为内置默认值 + 清除后端配置 + 显示 Toast

### 4.3 按钮状态

- Hover: 背景微变
- Active: 短暂按下态
- Disabled: opacity 0.4 + `cursor: not-allowed`
- Loading: 显示旋转图标（仅涉及后端请求时）

### 4.4 Toast

统一使用 `ElMessage`，位置 bottom-center：
- 保存成功 / 排序完成：`success`
- 重置成功：`success`
- 操作失败：`error`

---

## 五、数据流

### 5.1 存储策略

| 数据类型 | 存储位置 |
|----------|----------|
| 菜单配置 | localStorage + `user_layout(layout_type='menu')` |
| Dashboard 配置 | localStorage + `user_layout(layout_type='dashboard')` |
| 阅读配置 | localStorage + `user_layout(layout_type='preview')` |
| 外观配置（含圆角/动画/密度） | localStorage(`appearance-config`) + `user_layout(layout_type='appearance')` |
| 通知偏好 | localStorage(`notification-config`) + `user_layout(layout_type='notification')` |
| 实验功能开关 | localStorage(`feature-flags`) + `user_layout(layout_type='features')` |
| 主题切换 | localStorage(`theme-preference`) |
| 强调色 | localStorage(`accent-preference`) |

### 5.2 加载顺序

1. 从后端 `GET /api/layout` 加载所有类型配置
2. 覆盖 localStorage 中的对应项
3. 如果后端无数据，使用内置默认值
4. 外观配置中的 theme/accent 额外同步到 `data-theme` / `data-accent` 属性

### 5.3 后端扩展

当前 `layout_type` 支持: `menu`, `dashboard`, `preview`  
Phase 1 新增: `appearance`  
Phase 2 新增: `notification`, `features`

`user_layout` 表结构不变（uk_user_layout 唯一约束天然支持新增类型）。

Phase 2 额外新增后端接口和表：

| 新增内容 | 说明 |
|----------|------|
| `BackupController` | `POST /backup/now`, `GET /backup/download/{id}`, `POST /backup/import` |
| `ExportController` | `POST /export`, `GET /export/history` |
| `DataBackupService` | 全量数据聚合 + 压缩 + 事务性恢复 |
| `user_backup` 表 | 备份记录（file_path / file_size / module_count） |

---

## 六、文件变更清单

### Phase 1 变更

#### 前端

| 文件 | 操作 | 说明 |
|------|------|------|
| `SettingsView.vue` | 重写 | Tab 导航升级 + Phase 1 模块编排 |
| `SettingsView.vue` 同级 CSS | 重写 | 对齐 V2 规范 |
| `components/MenuManager.vue` | 改造 | UI 对齐 Card/Section 规范 |
| `components/DashboardManager.vue` | 改造 | UI 对齐 Card/Section 规范 |
| `components/ReadingExperience.vue` | 扩展 | 新增段距/代码字体/代码字号 |
| `components/AppearanceSettings.vue` | **新建** | 外观模块（主题+强调色） |
| `components/CacheSettings.vue` | **新建** | 缓存管理 |
| `components/PlaceholderTab.vue` | **新建** | 占位 Tab（通知/实验功能） |
| `readingConfigStore.ts` | 扩展 | 新增段距/代码字体/代码字号字段 |
| `layoutStore.ts` | 改造 | 新增 saveAppearanceConfig / resetAppearanceConfig |
| `types/layout.ts` | 扩展 | 新增 AppearanceConfig 类型 |
| `AppLayout.vue` | 微调 | 保留外观快捷入口 |

#### 后端

| 文件 | 操作 | 说明 |
|------|------|------|
| 无变更 | — | 现有布局接口通用支持新 `layout_type` |

### Phase 2 变更

#### 前端

| 文件 | 操作 | 说明 |
|------|------|------|
| `components/NotificationSettings.vue` | **新建** | 通知偏好面板 |
| `components/DataManagement.vue` | **新建** | 备份/恢复/导出 |
| `components/ExperimentalFeatures.vue` | **新建** | 实验功能开关面板 |
| `components/AppearanceSettings.vue` | 扩展 | 新增圆角/动画/密度 |
| `store/notificationConfigStore.ts` | **新建** | 通知配置 Store |
| `store/featureFlagStore.ts` | **新建** | 实验功能 Store |
| `composables/useFeatureFlag.ts` | **新建** | 功能开关读取 composable |
| `SettingsView.vue` | 改造 | Phase 1 占位 Tab 替换为正式组件 |
| `types/layout.ts` | 扩展 | 新增 NotificationConfig / FeatureFlags 类型 |

#### 后端

| 文件 | 操作 | 说明 |
|------|------|------|
| `BackupController.java` | **新建** | 备份/恢复接口 |
| `ExportController.java` | **新建** | 数据导出接口 |
| `DataBackupService.java` / `DataBackupServiceImpl.java` | **新建** | 备份业务逻辑 |
| `BackupConfigDTO.java` | **新建** | 备份配置 DTO |
| `user_backup` 表 | **新建** | 备份记录表 |

---

## 七、不包含在 Phase 1 的内容

- 通知偏好设置（桌面通知权限、声音开关）→ **Phase 2**
- 数据备份/恢复/导入/导出 → **Phase 2**
- 实验功能开关 → **Phase 2**
- 圆角/动画强度/界面密度自定义 → **Phase 2**
- 工作台模板/多配置/云同步 → **V3 规划**
- 快捷键自定义 → **V3 规划**
- 插件系统 → **V3 规划**
- 自定义 CSS → **V3 规划**
- AI 推荐布局 → **V3 规划**

---

## 八、视觉参考

整体风格延续 Personal Hub 现有设计语言：
- 强调色: `--accent`（默认 `#4F7BFF`）
- 背景: `--bg-body` / `--bg-card`
- 圆角: `--radius-lg(12px)` / `--radius-sm(8px)`
- 间距体系: 8px 基准
- 字体: 系统字体栈
- 深色模式: 通过 `data-theme` 属性，CSS 变量自动适配
