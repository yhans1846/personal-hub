# 文件管理模块 — 设计规格

## 概述

Personal Hub 第一阶段最后一个模块：文件上传/下载/分类管理。支持 PDF、Word、Excel、PPT、Markdown、图片、ZIP 等常见格式。

## 技术方案

- 后端：新增 `ph-file` Maven 子模块，遵循现有 CRUD 模式（Entity/DTO/VO/Mapper/Service/Controller）
- 前端：文件列表页（Grid 卡片视图）+ 分类管理页，遵循现有 UI 模式（Element Plus + Lucide 图标）
- 文件存储：本地磁盘按 `YYYY/MM/UUID.ext` 分目录存储
- 上传限制：50MB（Spring 配置 `spring.servlet.multipart.max-file-size`）

## 数据库

表结构已在 `DATABASE.md` 定义（file_resource + file_category），无需 DDL 变更。

### file_resource

| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT PK | 主键 |
| user_id | BIGINT | 所属用户 |
| name | VARCHAR(255) | 原始文件名 |
| stored_name | VARCHAR(255) | UUID 存储名 |
| path | VARCHAR(500) | 存储路径 |
| size | BIGINT | 字节数 |
| type | VARCHAR(50) | 扩展名 |
| mime_type | VARCHAR(100) | MIME 类型 |
| category_id | BIGINT | 文件分类 ID |
| is_deleted | TINYINT | 逻辑删除 |
| created_at / updated_at | DATETIME | 时间戳 |

### file_category

| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT PK | 主键 |
| user_id | BIGINT | 所属用户 |
| name | VARCHAR(50) | 分类名称 |
| sort_order | INT | 排序 |
| created_at / updated_at | DATETIME | 时间戳 |

## API 接口

所有接口需 JWT 认证。

### 文件管理 `/api/files`

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | /api/files | 分页列表（keyword/type/categoryId） |
| GET | /api/files/{id} | 详情 |
| POST | /api/files/upload | 上传（multipart/form-data） |
| GET | /api/files/{id}/download | 下载 |
| DELETE | /api/files/{id} | 删除（逻辑删除） |

### 文件分类 `/api/file-categories`

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | /api/file-categories | 列表 |
| POST | /api/file-categories | 新建 |
| PUT | /api/file-categories/{id} | 修改 |
| DELETE | /api/file-categories/{id} | 删除 |

## 后端实现

### ph-file 模块

- `pom.xml`：继承父 POM，依赖 ph-common
- 包路径：`com.personalhub.module.file`
- Entity：`FileResource` / `FileCategory` — 对应数据库两张表，MyBatis-Plus 注解
- DTO：`FileQueryDTO extends PageParam`（keyword, type, categoryId）/ `FileCategoryCreateDTO`（name, sortOrder）
- VO：`FileVO`（含 sizeFormatted、typeLabel、typeIcon 等前端展示字段）/ `FileCategoryVO`
- Mapper：`FileResourceMapper` / `FileCategoryMapper` — 继承 BaseMapper
- Service：`FileServiceImpl` / `FileCategoryServiceImpl` — 遵循现有模式，@Transactional

### 上传流程

1. 接收 `MultipartFile` + 可选 `categoryId`
2. 校验文件扩展名（白名单：pdf/doc/docx/xls/xlsx/ppt/pptx/md/png/jpg/jpeg/gif/svg/webp/zip/rar）
3. 生成存储路径：`upload/2026/07/{UUID}.{ext}`
4. 写入磁盘（`Files.copy`），插入数据库记录
5. 返回 `FileVO`

### 下载流程

1. 查询 file_resource 记录，校验归属
2. 构造 `FileSystemResource`，设置 `Content-Type` / `Content-Disposition: attachment`
3. 使用 `ResponseEntity<Resource>` 流式输出

### 删除

逻辑删除（MyBatis-Plus 逻辑删除），磁盘文件保留。

## 前端实现

### 类型定义 `types/file.ts`

```ts
interface FileVO { id, name, size, sizeFormatted, type, typeLabel, typeIcon, mimeType, categoryId, categoryName, createdAt }
interface FileQuery { page?, size?, keyword?, type?, categoryId? }
interface FileCategoryVO { id, name, sortOrder }
interface FileCategoryDTO { name, sortOrder }
```

### 文件列表 `views/files/FileList.vue`

- **工具栏**：搜索框 + 类型筛选（下拉）+ 分类筛选（下拉）+ 上传按钮
- **视图**：Grid 卡片布局，每张卡片显示：
  - 文件类型图标（Lucide: File / FileImage / FileText / FileArchive 等）
  - 文件名、格式化大小、上传时间
  - 悬停操作：下载、删除（el-tooltip + icon button）
- **上传弹窗**：Dialog 内嵌 file input + 分类选择器 + 上传按钮，显示上传进度
- **空状态**：📁 图标 + "还没有文件，上传一个吧"
- **分页**：el-pagination

### 分类管理 `views/files/FileCategoryManage.vue`

与笔记分类管理完全一致的风格，Dialog CRUD。

### 路由与导航

路由：`/files` → FileList，`/files/categories` → FileCategoryManage
侧边栏：在"待办任务"下方添加"文件"导航项和"分类管理"子项

## 需修改的文件

| 文件 | 变更 |
|------|------|
| backend/pom.xml | +module ph-file |
| backend/ph-boot/pom.xml | +ph-file 依赖 |
| frontend/src/router/index.ts | +文件路由 |
| frontend/src/components/AppLayout.vue | +文件导航 |
| docs/DEVELOPMENT_PLAN.md | Step 10 ✅ |
| docs/ROADMAP.md | 文件管理 ✅ |
| docs/API.md | 验证接口是否匹配 |
