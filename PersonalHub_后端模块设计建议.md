# Personal Hub 后端模块设计建议

## 现状

当前模块：

-   ph-auth
-   ph-bookmark
-   ph-dashboard
-   ph-diary
-   ph-file
-   ph-note
-   ph-notification
-   ph-reading
-   ph-study
-   ph-studyplan
-   ph-tag
-   ph-todo
-   ph-common
-   ph-boot

对于**单体 Spring Boot + 单数据库**项目，这样拆分偏细，维护成本较高。

------------------------------------------------------------------------

# 推荐结构

    personal-hub
    ├── ph-boot            # 启动
    ├── ph-common          # 公共能力
    ├── ph-system          # 用户/认证/通知
    ├── ph-knowledge       # 知识管理
    ├── ph-planning        # 计划任务
    ├── ph-resource        # 文件收藏
    └── ph-dashboard       # 首页统计

------------------------------------------------------------------------

## ph-system

职责：

-   用户
-   登录(JWT)
-   权限
-   通知

------------------------------------------------------------------------

## ph-knowledge

合并：

-   Note
-   Diary
-   Study
-   Reading
-   Tag

统一抽象：

    Knowledge
     ├── Note
     ├── Diary
     ├── StudyRecord
     └── ReadingRecord

共享：

-   TagService
-   SearchService
-   Markdown

------------------------------------------------------------------------

## ph-planning

合并：

-   Todo
-   StudyPlan

统一：

    Plan
     ├── Todo
     └── StudyPlan

共享提醒、统计。

------------------------------------------------------------------------

## ph-resource

合并：

-   Bookmark
-   File

统一：

    Resource
     ├── Bookmark
     └── File

------------------------------------------------------------------------

## ph-dashboard

仅负责聚合查询，不保存业务。

------------------------------------------------------------------------

# 推荐目录

    personal-hub
    ├── ph-boot
    ├── ph-common
    ├── ph-system
    ├── ph-knowledge
    ├── ph-planning
    ├── ph-resource
    └── ph-dashboard

------------------------------------------------------------------------

# Controller 示例

    knowledge
    ├── NoteController
    ├── DiaryController
    ├── StudyController
    └── ReadingController

    planning
    ├── TodoController
    └── StudyPlanController

    resource
    ├── BookmarkController
    └── FileController

------------------------------------------------------------------------

# 为什么这样拆

优点：

1.  模块数量从14降到7。
2.  领域边界清晰。
3.  减少重复DTO/Mapper/Service。
4.  保持以后拆微服务的可能。
5.  更符合DDD中的领域划分。

------------------------------------------------------------------------

# 不建议

不要按每张表一个Module：

    ph-note
    ph-tag
    ph-study
    ph-reading
    ...

这是数据库思维，不是领域思维。

------------------------------------------------------------------------

# 最终建议

-   单数据库 + 单体项目：7个模块最佳。
-   业务按领域划分，而不是按表划分。
-   Dashboard作为聚合模块。
-   Common只放真正公共代码。
-   后续若微服务，可直接按领域拆分。
