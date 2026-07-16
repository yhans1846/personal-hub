# 学习计划 XLSX 导出 — 设计规格

日期：2026-07-16  
状态：已实现

## 目标

学习计划列表支持导出 XLSX，含「导出当前」「导出全部」；接口内存生成文件流，浏览器直接下载，**不落盘**。

## 产品

- 入口：工具栏「导出 ▾」，位于「新建」左侧
- 菜单：导出当前（当前筛选）/ 导出全部（忽略筛选）
- 格式：`.xlsx`
- 列：名称、分类、状态、进度、开始、结束、来源、作者、URL、备注、更新时间

## 技术

- `GET /api/study-plans/export?scope=filtered|all` + 与列表相同的筛选参数（filtered 时）
- Apache POI `XSSFWorkbook` → `byte[]`；`Content-Disposition: attachment`
- 前端 `el-dropdown` + blob 下载

## 文档

`LIST_PAGE_SPEC.md` 导出章节、`API.md`、`CHANGELOG.md`、`TECH_STACK.md`
