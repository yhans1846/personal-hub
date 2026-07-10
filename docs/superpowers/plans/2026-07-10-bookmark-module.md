# 收藏夹模块 实现计划

**目标：** 新增收藏夹模块，支持网址 CRUD、分类管理、标签管理、关键词搜索

**架构：** 后端新增 ph-bookmark 子模块（BookmarkUrl + BookmarkCategory 两条线），前端新增收藏夹列表/表单/分类管理页面

**参考模块：** ph-diary + ph-note（分类管理模式）

---

### 任务 1：数据库表 + 后端模块脚手架

1. sql/init.sql 追加 bookmark_category、bookmark_url 表
2. 创建 backend/ph-bookmark/pom.xml
3. backend/pom.xml 添加 ph-bookmark 模块 + 依赖管理
4. backend/ph-boot/pom.xml 添加 ph-bookmark 依赖

### 任务 2：后端 Entity + DTO + VO

1. entity/BookmarkCategory.java
2. entity/BookmarkUrl.java
3. dto/BookmarkCategoryDTO.java
4. dto/BookmarkCreateDTO.java
5. dto/BookmarkQueryDTO.java
6. vo/BookmarkCategoryVO.java
7. vo/BookmarkVO.java

### 任务 3：后端 Mapper + Service + Controller

1. mapper/BookmarkCategoryMapper.java
2. mapper/BookmarkUrlMapper.java
3. service/BookmarkCategoryService.java + impl
4. service/BookmarkUrlService.java + impl
5. controller/BookmarkCategoryController.java
6. controller/BookmarkUrlController.java

### 任务 4：前端类型 + API

1. types/bookmark.ts
2. api/bookmarkApi.ts

### 任务 5：前端页面

1. views/bookmark/BookmarkList.vue — 收藏夹列表（卡片网格、分类/标签筛选、搜索）
2. views/bookmark/BookmarkForm.vue — 新建/编辑收藏
3. views/bookmark/BookmarkCategoryManage.vue — 分类管理

### 任务 6：前端路由 + 导航

1. router/index.ts 添加收藏夹路由
2. AppLayout.vue 添加侧边栏导航

### 任务 7：编译验证

### 任务 8：更新文档
