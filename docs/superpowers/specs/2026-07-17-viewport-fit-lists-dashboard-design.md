# 一屏铺满：列表 + Dashboard 设计规格

日期：2026-07-17  
状态：已批准（头脑风暴）  
方案：扩展现有 LIST_PAGE_SPEC（方案 1）

## 目标

在常见桌面视口下，以下页面**主内容区不出现整页滚动条**：

| 页面 | 策略 |
|------|------|
| Dashboard（首页工作台） | 外层锁高不滚 · 列表类 widget 块内滚动 |
| 笔记 / 日记 / 学习记录 / 待办 | Table + Card 一屏铺满 · 超出分页 |

成功标准：内容区可视高度 ≥ 约 640px 时，上述页无整页滚动；数据超出当页用分页（列表）或块内滚动（Dashboard）。

## 非目标

- 阅读记录 / 学习计划（已有 `main-content--fill`，本次只复用约定，不重做）
- 收藏 / 文件 / 搜索 / 统计页
- 按「高度 ÷ 最小行高」完全动态计算 `PAGE_SIZE`
- Dashboard 整页分页
- 后端 API / 表结构变更（除非待办筛选发现硬缺口，另开子任务）

## 决策摘要

| 项 | 决策 |
|----|------|
| 首页 | Dashboard 工作台 |
| 列表视图 | Table **与** Card 均需一屏铺满 |
| 列表条数 | 默认 10；矮屏降档 8 / 6 |
| Dashboard | 外层不滚 / 块内可滚 |
| 实现路线 | 扩展 LIST_PAGE_SPEC + 学习计划骨架，抽 composable |

---

## 1. 列表页（笔记 / 日记 / 学习记录 / 待办）

### 1.1 骨架

对齐 `docs/LIST_PAGE_SPEC.md` 与 `modules/planning/studyplan/List.vue`：

```
plan-page（flex 列 · height 100% · overflow:hidden）
  → plan-top：PageHeader + toolbar
  → plan-middle（flex:1 · min-height:0）：loading | Empty | Table | Card
  → plan-foot：ListPagination
```

- 挂载：`.main-content` 加 `main-content--fill`
- 卸载：成对移除

### 1.2 矮屏降档（`useFillPageSize`）

以 `.main-content`（或内容容器）**可视高度**为基准，防抖 150ms：

| 内容区高度 | `PAGE_SIZE` | Card 网格（桌面） |
|------------|-------------|-------------------|
| ≥ 640px | 10 | 5×2 |
| 520–639px | 8 | 4×2 |
| &lt; 520px | 6 | 3×2 |

- `size` 变化时：`page = 1` 并重新请求列表
- Table 行 / Card 格：`height:100%` 均分 middle；不足条数用空槽占位（同学习计划）
- 窄屏列数仍可按现有 LIST_PAGE_SPEC 断点（≤1100→4 列等），与 `PAGE_SIZE` 独立

### 1.3 待办特例

现状：`size: 9999` + 前端 Tab 过滤 + 拖拽排序。

改造：

- 改为服务端分页：`page` + `size`（来自 `useFillPageSize`）
- Tab（全部 / 逾期 / 今日 / …）变为 **query 筛选条件**，切换 Tab 时 `page=1` 重拉
- 一屏铺满骨架与其它三页一致（Table/Card 视现 UI 收敛到 Product 列表形态；若保留清单样式，仍须 fill + 分页 + 槽位高度，不得整页滚动）
- 拖拽排序：仅对**当前页**数据生效；跨页排序不在本次范围（保持现状能力边界或禁用跨页暗示）

### 1.4 公共 composable

| API | 职责 |
|-----|------|
| `useMainContentFill()` | onMounted 加 class / onUnmounted 移除 |
| `useFillPageSize(options?)` | 返回响应式 `pageSize`（10/8/6）；监听 resize |

四列表页与（可选）学习计划/阅读后续可迁移到同一 hook，本次至少四页接入。

---

## 2. Dashboard

### 2.1 外层

- 挂载 `main-content--fill`
- 根节点：`height:100%; overflow:hidden; display:flex; flex-direction:column`
- Bento / 自定义布局网格：占满剩余高度；格子 `min-height:0`

### 2.2 块内滚动

| Widget 类型 | 行为 |
|-------------|------|
| 近期笔记 / 学习 / 阅读等列表体 | 卡片 body `overflow-y:auto`，不撑破页面 |
| 快捷入口 / 外链 / 今日摘要 | 优先压缩间距或裁切，不强制内滚 |
| 纯统计数字 | 不滚动 |

### 2.3 不做

- Dashboard 底部分页切 widget 组
- 为「塞满一屏」而改后端统计接口

---

## 3. 文档与变更记录

| 文件 | 变更 |
|------|------|
| `docs/LIST_PAGE_SPEC.md` | 「一屏铺满」从可选改为本批页面默认；写入矮屏降档表与 composable 名 |
| `docs/CHANGELOG.md` | 记录一屏铺满落地范围 |
| 本规格 | `docs/superpowers/specs/2026-07-17-viewport-fit-lists-dashboard-design.md` |

---

## 4. 测试

| 类型 | 内容 |
|------|------|
| 单元 | `useFillPageSize`：高度边界 → 10 / 8 / 6 |
| 手工 / 浏览器 | 四列表 Table+Card、Dashboard：常见高度无整页滚动；列表翻页与筛选重置 page |
| 回归 | 学习计划 / 阅读 fill 行为不被破坏 |

---

## 5. 风险与缓解

| 风险 | 缓解 |
|------|------|
| 待办从全量变分页，拖拽体验变化 | 文档写明仅当前页；UI 不暗示跨页排序 |
| Resize 频繁改 size 导致抖动 | 150ms 防抖；仅 size 真变化才重置 page |
| Dashboard 自定义布局格子过高 | `min-height:0` + widget 内滚；极矮屏接受块内滚动条 |

---

## 6. 实现顺序（供计划拆任务）

1. `useMainContentFill` + `useFillPageSize` + 单测  
2. 笔记列表接入  
3. 日记 / 学习记录接入  
4. 待办改造（分页 + fill）  
5. Dashboard 外锁内滚  
6. 更新 LIST_PAGE_SPEC + CHANGELOG  
