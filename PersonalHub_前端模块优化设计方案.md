# Personal Hub 前端模块优化设计方案

## 目标

将当前按页面组织的 views
目录优化为按业务领域组织，提高可维护性、可扩展性，并与后端领域模块保持一致。

------------------------------------------------------------------------

# 当前结构

``` text
src/views
├── bookmark
├── diary
├── files
├── notes
├── reading
├── search
├── stats
├── study
├── studyplan
├── tags
├── todos
├── Dashboard.vue
└── Login.vue
```

问题：

-   一级目录越来越多
-   相似业务分散
-   Dashboard 容易越来越臃肿
-   页面组件复用困难
-   与后端模块不一致

------------------------------------------------------------------------

# 推荐目录

``` text
src
├── api
├── assets
├── components
│   ├── common
│   ├── business
│   └── charts
├── composables
├── layouts
├── router
├── stores
├── styles
├── types
├── utils
└── modules
    ├── dashboard
    ├── knowledge
    │   ├── note
    │   ├── diary
    │   ├── study
    │   ├── reading
    │   └── tag
    ├── planning
    │   ├── todo
    │   └── studyplan
    ├── resource
    │   ├── bookmark
    │   └── file
    ├── search
    └── system
        └── login
```

------------------------------------------------------------------------

# Dashboard

建议拆分：

``` text
dashboard
├── Dashboard.vue
├── components
│   ├── Greeting.vue
│   ├── QuickActions.vue
│   ├── StatCards.vue
│   ├── TodoPanel.vue
│   ├── RecentReading.vue
│   └── RecentNotes.vue
└── api.ts
```

Dashboard.vue 仅负责布局和数据组装。

------------------------------------------------------------------------

# Knowledge 模块

``` text
knowledge
├── note
│   ├── List.vue
│   ├── Detail.vue
│   ├── Edit.vue
│   └── components
├── diary
├── study
├── reading
└── tag
```

统一管理所有知识类页面。

------------------------------------------------------------------------

# Planning 模块

``` text
planning
├── todo
└── studyplan
```

共享筛选、状态、提醒等逻辑。

------------------------------------------------------------------------

# Resource 模块

``` text
resource
├── bookmark
└── file
```

统一资源管理。

------------------------------------------------------------------------

# API 建议

不要：

``` text
api
├── note.ts
├── study.ts
├── reading.ts
```

推荐：

``` text
api
├── auth.ts
├── dashboard.ts
├── knowledge.ts
├── planning.ts
├── resource.ts
```

与后端领域保持一致。

------------------------------------------------------------------------

# 公共组件

建议抽取：

-   SearchBar
-   StatCard
-   EmptyState
-   MarkdownEditor
-   ConfirmDialog
-   Tag
-   PriorityTag
-   ProgressCard
-   FileUploader
-   Pagination

目录：

``` text
components
├── common
├── business
└── charts
```

------------------------------------------------------------------------

# Pinia 建议

保留全局状态：

-   user
-   permission
-   dashboard
-   theme

业务列表数据直接页面请求，不建议所有业务都建 Store。

------------------------------------------------------------------------

# 最终建议

1.  按领域组织 modules，而不是按页面一级目录。
2.  Dashboard 继续组件化，避免单文件过大。
3.  API 与后端模块命名保持一致。
4.  公共组件沉淀到 components。
5.  前后端统一领域模型（Knowledge、Planning、Resource、System、Dashboard）。

这样更接近企业级 Vue3 + Spring Boot
项目的组织方式，也方便未来扩展和团队协作。
