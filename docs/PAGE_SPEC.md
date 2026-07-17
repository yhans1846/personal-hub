# 页面开发规范（PAGE_SPEC）

> 职责：页面形态与交互契约。Token/组件目录 → `STYLE_GUIDE.md`；接口参数 → `API.md`。  
> 参考：`studyplan/List.vue` · `note/Preview.vue`  
> 新形态（表单/设置等）追加本文，不另起 SPEC。

---

## 1. 选型

| 场景 | 形态 |
|------|------|
| 多列对照 | Product Table（禁默认企业风 `el-table`） |
| 浏览发现 | Product Card |
| 都要 | Table/Card + `useProductViewMode` |
| 时间线 | 学习记录例外（§2.4） |
| Markdown 只读 | DocLayout + `markdown-prose`（§3） |
| 编辑 | 默认 Dialog；仅笔记等 Full Page |

禁：灰底密表、行内一堆 Primary、无必要独立编辑页。

---

## 2. 列表页

### 骨架

`plan-page` → `plan-top`（PageHeader + 徽章 + ListToolbar）→ `plan-middle`（loading / EmptyState / Table / Card）→ `plan-foot`（ListPagination）

### 一屏铺满

`useMainContentFill` + `useFillPageSize`（禁手写 class / 固定 PAGE_SIZE）。  
适用：计划、阅读、笔记、日记、学习记录、待办、收藏、文件、回收站、Dashboard。

| 可视高 | PAGE_SIZE | Card 桌面 |
|--------|-----------|-----------|
| ≥640 | 10 | 5×2 |
| 520–639 | 8 | 4×2 |
| &lt;520 | 6 | 3×2 |

筛选/搜索/排序 → `page=1`；槽位 = PAGE_SIZE，不足 pad；`total>0` 显分页。

### 学习记录例外

`/study-records`：时间线 + fill + ListToolbar，无 Table/Card 切换。

### Product Table / Card

Table：弱横线、均分行、整行编辑；主列→状态点→软标签→progress→tertiary→`⋯`。  
Card：桌面 5×2；≤1100→4；≤720→2。标题|状态→标签(≤2)→来源·作者→进度→日期→备注→「更新于…」。

相对时间：刚刚 / N分 / N时 / N天 / YYYY-MM-DD。

### 工具栏与导出

ListToolbar + 可选状态徽章（非 KPI 大卡）。禁营销副标题。

| 导出 | 约定 |
|------|------|
| 入口 | 「新建」左 dropdown：当前 / 全部 |
| 格式 | XLSX（内存流） |
| 契约 | 见 `API.md` 对应 `/export` |

交互：Dialog · `useDeepLinkDialog` · 删后刷新 · 骨架 · lucide。

---

## 3. 预览 / 文档页

`DocLayout` + `markdown-prose` + `readingConfigStore`（设置页配置；Header 不嵌阅读 popover）。  
TOC 宽 220（收起 34，拖 160–400）。主题：`data-preview-theme` → `--preview-*` → `--prose-*`。  
持久化：`reading-config` + `user_layout(preview)` 防抖 500ms。

| 注意 | 约定 |
|------|------|
| 图 | `?token=` + `setupImageProxy` |
| `:deep()` | 仅盖库硬编码；排版进 prose CSS |
| TOC | `getElementById`，禁 textContent |
| 加载 | content → nextTick 挂 Proxy/Zoom/Copy/Anchors；换主题重调 Zoom |

---

## 4. Checklist

**列表：** Header+Toolbar+Empty+Pagination · Table/Card 或例外 · 槽位/fill · Dialog+深链 · Token+骨架+深色  
**预览：** DocLayout+prose · readingConfig · §3 注意项
