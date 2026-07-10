# 学习计划模块 实现计划

**目标：** 新增学习计划模块，支持计划 CRUD、进度跟踪、时间管理、关联学习记录

**架构：** 后端新增 ph-studyplan 子模块，study_plan 表 + study_record 表追加 plan_id 关联

---

### 任务 1：数据库 + 模块脚手架

1. sql/init.sql 追加 study_plan 表，study_record 表追加 plan_id 字段
2. 创建 backend/ph-studyplan/pom.xml
3. backend/pom.xml 添加 ph-studyplan 模块 + 依赖管理
4. backend/ph-boot/pom.xml 添加 ph-studyplan 依赖

### 任务 2：后端 Entity + DTO + VO

1. entity/StudyPlan.java
2. dto/StudyPlanCreateDTO.java
3. dto/StudyPlanQueryDTO.java
4. vo/StudyPlanVO.java

### 任务 3：后端 Mapper + Service + Controller

1. mapper + service + impl + controller

### 任务 4：前端类型 + API

1. types/studyplan.ts
2. api/studyplanApi.ts

### 任务 5：前端页面

1. StudyPlanList.vue — 计划列表（进度条、状态标签、记录数）
2. StudyPlanForm.vue — 新建/编辑

### 任务 6：前端路由 + 导航

### 任务 7：编译验证

### 任务 8：更新文档
