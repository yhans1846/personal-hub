# 阅读记录 Product 列表改造 — 设计规格

日期：2026-07-16  
状态：已实现

## 目标

将 `/readings` 对齐 `LIST_PAGE_SPEC` 与学习计划交互：Product Table/Card、一屏铺满、XLSX 导出；保留左侧小封面。

## 已确认决策

| 项 | 选择 |
|----|------|
| 视图 | Table + Card 切换（localStorage） |
| Header 统计 | 不要徽章 / 不做 `/stats`，仅「共 N 本」 |
| 卡片封面 | 左侧小封面约 48×64 |
| 导出 | 要：导出当前 / 导出全部，XLSX 内存流不落盘 |
| 路径 | 对齐学习计划全量改造（方案 A） |

## 产品与页面

骨架：`reading-page` → top（PageHeader + toolbar）→ middle → foot（分页）  
`main-content--fill`；`PAGE_SIZE=10`；槽位不足 pad。

工具栏：搜索（书名/作者）· 状态 · 排序 · 视图切换 · 导出 ▾ · 新建  

表格列：书名 · 作者 · 状态点 · 进度 · 章节 · 评分 · 起止 · 更新 · `⋯`  

卡片：左封面 → 书名|状态 → 作者 → 进度 → 章节/评分 → 底栏相对更新 + `⋯`；桌面 5×2。

交互：保留 `ReadingDialog`；深链 `?edit=` / `?create=1`；删除确认后刷新。

## 后端

- `ReadingQueryDTO` 增加 `sortBy` / `sortDir`  
  支持：`updatedAt`（默认 desc）、`createdAt`、`progress`、`startDate`、`bookTitle`
- `GET /api/readings/export?scope=filtered|all`（在 `/{id}` 前）  
  POI 内存生成；列：书名、作者、状态、进度、当前章、总章、评分、时长(分)、开始、结束、备注、更新时间
- `ph-knowledge` 依赖 `poi-ooxml`

## 前端

- 重写 `reading/List.vue`（对照 `studyplan/List.vue`）
- `exportReadings` + blob 下载 `阅读记录.xlsx`
- 类型补 `sortBy` / `sortDir`

## 不做

`/stats`、封面进 Excel、异步落盘、改 Dialog/表结构/路由 path。

## 验收

1. Table/Card 切换且偏好持久化  
2. 筛选/排序重置页码；一屏 10 条 + fill  
3. 导出当前/全部可下载 xlsx  
4. 深链仍可用  
5. 无 `/stats`、无服务器落盘  

## 文档

实现后更新 `API.md`、`CHANGELOG.md`。
