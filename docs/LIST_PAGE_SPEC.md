# Product 列表页开发规范

> 参考：`modules/planning/studyplan/List.vue` · 配套 `STYLE_GUIDE.md`

**最后更新：** 2026-07-17  
**定位：** Product 列表（非 Admin 密表）。

---

## 1. 何时使用

| 场景 | 形态 |
|------|------|
| 多列对照 | **Product Table**（禁默认 `el-table`） |
| 浏览发现 | **Product Card** |
| 都要 | Table/Card 切换 + `localStorage`（`useProductViewMode`） |
| 时间线浏览 | **学习记录**专用形态（见 §2.1） |

禁：灰底密表、行内一堆 Primary、独立编辑页（笔记除外）。

### 2.1 学习记录（时间线例外）

`/study-records`（`knowledge/study/List.vue`）**不采用** Product Table/Card，为已批准例外：

| 项 | 约定 |
|----|------|
| 骨架 | `plan-page` + `useMainContentFill` + `useFillPageSize` |
| 中区 | 时间线列表；`plan-middle` 内 `flex:1; overflow:auto` 纵向滚动 |
| 槽位 | 无 Table/Card 固定行 pad；条数随 `pageSize` 分页，不强制均分行高 |
| 工具栏 | `ListToolbar`；无 Table/Card 切换 |

其余 fill 列表仍按 §2 固定槽位契约。

---

## 2. 页面骨架

`plan-page`（flex 列 · 100% 高 · overflow hidden）  
→ `plan-top`：PageHeader + 统计徽章 + toolbar  
→ `plan-middle`（flex:1）：loading | EmptyState | Table | Card  
→ `plan-foot`：ListPagination

### 一屏铺满（默认）

挂载加 / 卸载移 `main-content--fill`（见 AppLayout）。适用：学习计划、阅读记录、笔记、日记、学习记录、待办、收藏、文件、Dashboard。

使用 `useMainContentFill()` + `useFillPageSize()`（禁止再手写 DOM class / 固定 PAGE_SIZE）。

#### 矮屏降档（内容区可视高）

| 高度 | PAGE_SIZE | Card 桌面网格 |
|------|-----------|---------------|
| ≥640px | 10 | 5×2 |
| 520–639 | 8 | 4×2 |
| &lt;520 | 6 | 3×2 |

`size` 变化时 `page=1` 重拉；不足条数用空槽占位。

---

## 3. 分页与槽位

| 规则 | 要求 |
|------|------|
| PAGE_SIZE | 默认 10 |
| 筛选/搜索/排序 | 重置 page=1 |
| 分页 | total>0 即显 |
| Table/Card 槽位 | 固定 PAGE_SIZE；不足 pad 占位 |

---

## 4. Product Table

弱横线、无竖线/斑马；表头次要色；约 10 行均分高度；行 hover + 整行点编辑。

层级：主列 600 → 状态点/✅ → 软色标签 → 细 progress → 次要列 tertiary → 图标+`⋯`（禁多文字按钮）。

禁：默认 el-table 边框/stripe；主信息堆成混乱副标题。

---

## 5. Product Card

桌面 **5×2**（`minmax(0,1fr)`）；卡 `height:100%` flex 列。窄屏：≤1100→4×3；≤720→2×5。

顺序：标题|状态 → 标签（最多 2 +`+N`）→ `来源 · 作者` → 进度 → `YYYY.MM.DD ~` → 备注 → 底栏「更新于…」+ 链接/`⋯`。

### 相对时间
<1分刚刚 · <1时 N分 · <24时 N时 · <7天 N天 · 否则 YYYY-MM-DD（前缀「更新于 」）。

---

## 6. Header / 工具栏 / 统计

PageHeader（共 N，动态计数即可；禁营销副标题）+ 可选状态徽章（勿做成 KPI 大卡）  
toolbar：统一 **`ListToolbar`**（搜索 / `#filters` / `#actions` / createLabel）  
视图切换：`useProductViewMode` + `.view-toggle`（`styles/product-list.css`）  
列表主路由建议 `meta.hideBreadcrumb: true`（与 PageHeader 二选一标题）

适用 fill 页：学习计划、阅读、笔记、日记、学习记录、待办、收藏、文件、回收站、Dashboard。

### 导出（可选）

| 项 | 约定 |
|----|------|
| 入口 | 工具栏「新建」左侧 `el-dropdown` |
| 菜单 | 导出当前（当前筛选）/ 导出全部（忽略筛选） |
| 格式 | XLSX（Apache POI 内存生成，不落盘） |
| API | `GET …/export?scope=filtered\|all` + 列表同款筛选参数（filtered） |
| 前端 | `responseType: 'blob'` → `URL.createObjectURL` 触发下载 |

参考：学习计划 `GET /api/study-plans/export`。

---

## 7. 交互与深链

Dialog 编辑 · `useDeepLinkDialog`（`?edit=`/`?create=1`）· 删除确认后刷新 · EmptyState · 骨架 · lucide

---

## 8. 后端约定

Query 继承 PageParam · keyword/筛选/sortBy·sortDir（默认 updatedAt desc）· 可选 `GET …/stats` · VO 带 `tags[]`

---

## 9. Checklist（新列表页对照）

1. PageHeader + 工具栏 + EmptyState + ListPagination  
2. Product Table/Card，禁企业风 el-table  
3. 条数与槽位一致；筛选重置页码  
4. 软标签/状态点/图标操作  
5. Dialog + 深链  
6. 深色 + Token + 骨架  
7. fill 成对挂载/卸载  
8. 同步 CHANGELOG / API / DATABASE  

---

## 10. 参考文件

`studyplan/List.vue` · `StudyPlanDialog.vue` · `AppLayout.vue` · `ListPagination`/`EmptyState`/`PageHeader` · `useDeepLinkDialog` · `useProductViewMode` · `STYLE_GUIDE.md`
