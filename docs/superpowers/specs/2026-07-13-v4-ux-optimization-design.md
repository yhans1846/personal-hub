# Personal Hub V4 — UX 优化设计规格

> 基于 V4 实施规范，定位差距，按阶段推进
>
> 日期：2026-07-13

---

## 一、Dashboard 重构

### 当前问题

- 8 张统计卡占据首屏视觉焦点
- 用户不知道"今天该做什么"
- 内容卡片（今日计划/待办/最近笔记）被压在下面

### 设计方案

**布局变化：2 列网格 → 单列内容流**

```
今日任务     ← 最突出
最近编辑
最近阅读
最近学习
快捷操作     ← 四个快速创建按钮
统计卡       ← 始终放底部
```

- 使用 `layoutStore.dashboardCards` + `visibleDashboardCards` 控制排序/显隐
- 统一使用 LocalStorage 持久化配置
- 每张卡片加拖拽手柄和显隐开关

### 卡片变更

| 当前卡片 | 变更 | 默认显隐 |
|---------|------|---------|
| today_plan (今日计划) | 保留，放首位 | visible |
| recent_notes (最近更新) | 保留，放第二位 | visible |
| recent_reading (最近阅读) | 保留，放第三位 | visible |
| recent_studies (学习记录) | 保留，放第四位 | visible |
| **快捷操作** | **新增** | visible |
| recent_bookmarks (最近收藏) | 保留 | hidden |
| pending_todos (待办任务) | 保留（与 today_plan 有重叠，默认隐藏） | hidden |
| **统计卡** | **从顶部移到底部固定区域** | always visible |

### 数据流

```
Dashboard.vue
  ├─ onMounted → Promise.all([
  │   getDashboardStats,
  │   getRecentNotes,
  │   getTodayTodos,
  │   ...
  │ ])
  ├─ layoutStore.visibleDashboardCards → 渲染卡片
  ├─ 统计卡片作为独立区域渲染在底部
  └─ TodoDialog (快捷创建)
```

---

## 二、Sidebar 优化

### 分组重构

| 当前 5 组 | → | V4 3 组 | 包含 |
|-----------|---|---------|------|
| workspace | → | **工作区** | 首页、待办任务、学习计划 |
| knowledge | → | | |
| resource | → | **管理** | 笔记、日记、阅读记录、学习记录、收藏夹、文件、标签、分类、设置、回收站 |
| manage | → | | |
| stats | → | **统计** | 数据统计 |

### 新增「最近访问」

- 位置：工作区上方
- 存储：localStorage，key `recent-visits`
- 容量：最多 10 条，自动去重（按 route path）
- 实现方式：`router.afterEach` 钩子记录
- 显示格式：仅图标 + 标题，无分组

### 菜单规范

- 高度 38px（当前已有 ✓）
- hover/active 样式已统一 ✓
- 图标大小 18px（当前已有 ✓）
- 字体 14px（当前已有 ✓）

---

## 三、CRUD 页面统一

### 当前状态

几乎所有页面已遵循 `PageHeader → ListToolbar → Content → Pagination` 模式。

### 需要微调的差异

| 文件 | 差异 | 处理方式 |
|------|------|---------|
| study/List.vue | 学习记录表格 | 检查是否缺失 EmptyState |
| file/List.vue | 文件管理表格 | 检查 toolbar 结构 |
| diary/List.vue | 日记列表 | 检查 toolbar 结构 |
| bookmark/List.vue | 收藏列表 | 检查 toolbar 结构 |
| studyplan/List.vue | 学习计划 | 检查 toolbar 结构 |

### Toolbar 统一规则

- 左侧：搜索框（200~240px）
- 右侧：新建按钮
- 可选：filter 插槽
- 禁止自定义布局

---

## 四、Dialog 统一

### 当前 UiDialog 已达标

- 宽度 720px ✓
- border-radius: 16px (var(--radius-xl)) ✓
- Header 固定 ✓
- Body 滚动（max-height: 65vh）✓
- Footer 固定 ✓
- 取消 左 / 保存 右 ✓

### 新增能力

- **UiButton 新增 danger 属性**：危险操作用红色按钮
- **动画**：已使用 fade + scale（dialog-fade-in）

---

## 五、Markdown Editor 优化

### 已达标

- 自动保存：2s debounce ✓ (useAutoSave.ts)
- Ctrl+S：强制保存 ✓
- 保存状态：saving/dirty/success/error ✓
- 占位符："请输入标题..." ✓
- 图片上传 ✓
- 代码块/引用/表格 ✓

### 新增

- **默认标题**：新建笔记时显示"未命名笔记"

---

## 六、Dashboard Customization

### 已有

- 拖拽排序 ✓ (SortableJS)
- 显隐切换 ✓
- 恢复默认 ✓
- 双存储（localStorage + 后端）✓

### 仅需

- 同步更新 `DashboardManager.vue` 中的默认卡片列表
- 确保统计卡固定不可完全隐藏

---

## 七、搜索优化

### 新增能力

- **最近搜索**：存储最近 5 条搜索关键词到 localStorage
- **最近打开**：复用 Sidebar 的最近访问数据
- **搜索历史**：在空输入时展示

### 实现方式

```typescript
// CommandPalette.vue 新增
const searchHistory = ref<string[]>(loadSearchHistory())

function saveSearchHistory(keyword: string) {
  const history = loadSearchHistory()
  history.unshift(keyword)
  // 去重 + 限5条
  localStorage.setItem('search-history', JSON.stringify(
    [...new Set(history)].slice(0, 5)
  ))
}
```

---

## 八、动画统一

### 规格

| 元素 | 动画 | 时长 | 现状 | 变更 |
|------|------|------|------|------|
| 页面切换 | Fade | 150ms | 200ms | 调整 |
| 按钮 Hover | 背景/颜色 | 200ms | 200ms ✓ | 无 |
| Card Hover | translateY(-2px) + shadow | 200ms | 部分有 | 统一 |
| Dialog | Fade + Scale | 200ms | 已有 ✓ | 无 |

### 实施

- global.css `.page-fade` 的 200ms → 150ms
- 逐个检查所有 card hover 效果，统一使用通用 mixin/类

---

## 九、验收检查项

- [ ] Dashboard：今日内容突出、统计卡在底部、拖拽/显隐正常
- [ ] Sidebar：3 分组、最近访问 5~10 条、折叠正常
- [ ] CRUD 页面：PageHeader → Toolbar → Content → Pagination 完全一致
- [ ] Dialog：尺寸/动画/Header/Body/Footer 统一
- [ ] Editor：自动保存/Ctrl+S/保存状态/默认标题
- [ ] Dashboard Customization：拖拽/排序/恢复默认
- [ ] 搜索：最近搜索/历史/空状态
- [ ] 动画：所有页面动画统一，timing 一致

---

## 十、开发顺序

### 第一阶段（当前实施）

1. Dashboard 布局重构
2. Sidebar 分组调整 + 最近访问
3. CRUD 页面差异对齐

### 第二阶段

4. Dialog 完善（danger 按钮）
5. Editor 默认标题
6. Dashboard Customization 默认卡片更新

### 第三阶段

7. 搜索历史
8. 动画统一
9. 细节优化
