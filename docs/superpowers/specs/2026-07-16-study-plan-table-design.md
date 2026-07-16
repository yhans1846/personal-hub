# 学习计划表格化完全重构 — 设计规格

**日期：** 2026-07-16  
**状态：** 已批准（对话确认）

## 背景

学习计划原为卡片列表（名称 / 目标 / 进度 / 日期 / 状态），与用户常用的课程表（电子表格）形态不一致。需按表格形态完全重构数据模型与列表 UI。

## 目标

1. 列表改为 `el-table`（数据密集场景，作为 STYLE_GUIDE「卡片优先」的例外）。
2. 字段对齐课程表：名称、来源、作者、起止、状态、地址、备注；分类用统一标签。
3. 去掉 `goal` / `progress` / `recordCount` 主模型与展示。
4. 编辑仍用 Dialog，不做单元格内联编辑；分类列不合并单元格。

## 非目标

- 不内联编辑表格单元格。
- 不合并分类单元格（rowspan）。
- 不强制迁移学习记录关联语义（`study_record.plan_id` 可保留，但不进计划列表）。

## 数据模型

`study_plan` 保留：`id`, `user_id`, `name`, `start_date`, `end_date`, `status`, 审计/软删。

新增：`source`, `author`, `url`, `remark`。  
删除：`goal`, `progress`（迁移时 `remark = COALESCE(remark, goal)` 后 drop）。

状态文案（数值不变）：

| 值 | 文案 |
|----|------|
| 0 | 待学习 |
| 1 | 进行中 |
| 2 | 已完成 |
| 3 | 已暂停 |

分类：`tag` + `tag_rel`，`entity_type = study_plan`。DTO `tagIds`，VO `tags`。

## API

- GET：`page, size, keyword, status, tagId`；keyword 搜 name/source/author/remark
- POST/PUT：name, source, author, startDate, endDate, status, url, remark, tagIds

## UI

- 表列：分类(标签 Chip) | 名称 | 来源 | 作者 | 开始 | 结束 | 状态 | 地址 | 备注 | 操作
- 工具栏：搜索 + 状态 + 标签筛选 + 新建
- Dialog：字段对齐新模型；标签 Chip 多选（同收藏夹）
- TodayWidget：去掉进度条，展示状态标签

## 文档

实现后更新：DATABASE / API / CHANGELOG / STYLE_GUIDE；本规格与实现对齐。

## 验收

1. `/study-plans` 为表格，分类为标签 Chip，不合并单元格  
2. CRUD + 标签绑定/筛选/搜索可用  
3. API/UI 无 goal/progress/recordCount  
4. 首页今日计划不依赖进度  
5. migration、init.sql、文档已同步  
