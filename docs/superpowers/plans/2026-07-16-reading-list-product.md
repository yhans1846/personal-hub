# 阅读记录 Product 列表改造 实现计划

> **面向 AI 代理的工作者：** 按任务顺序实现。步骤使用复选框跟踪。

**目标：** `/readings` 对齐 LIST_PAGE_SPEC：Table/Card、排序、XLSX 导出；左封面；无 stats。

**规格：** `docs/superpowers/specs/2026-07-16-reading-list-product-design.md`

## 文件

| 文件 | 职责 |
|------|------|
| `ReadingQueryDTO.java` | sortBy/sortDir |
| `ReadingRecordService(.java/Impl)` | 排序 + exportXlsx |
| `ReadingRecordController.java` | GET /export |
| `ph-knowledge/pom.xml` | poi-ooxml |
| `types/reading.ts` | query 排序字段 |
| `modules/knowledge/api.ts` | exportReadings |
| `reading/List.vue` | Product 列表重写 |
| `docs/API.md` / `CHANGELOG.md` | 文档 |

## 任务

### Task 1: 后端排序 + 导出
- [x] QueryDTO 加 sortBy/sortDir；list 应用排序（默认 updatedAt desc）
- [x] exportXlsx + Controller `/export`；pom 加 poi-ooxml
- [x] 编译 ph-knowledge

### Task 2: 前端 API + 类型
- [x] ReadingQuery 加 sortBy/sortDir
- [x] exportReadings blob

### Task 3: List.vue Product 重写
- [x] 骨架/toolbar/table/card/export/fill/PAGE_SIZE=10
- [x] 对照 studyplan/List.vue，去掉 stats/tags

### Task 4: 文档
- [x] API.md / CHANGELOG.md
