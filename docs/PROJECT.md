# Personal Hub - 项目介绍

## 定位
个人知识管理系统，Spring Boot 3 + Vue 3 前后端分离。

## 设计理念
> **Notion 的简洁 + Linear 的精致 + Raycast 的现代感 + Apple 的克制**

不是企业后台，是长期陪伴的知识空间。简洁、专注、温暖、高级。详见 `STYLE_GUIDE.md`。

## 架构

```
Vue 3 + Vite + TS → Axios → RESTful → Spring Boot 3 → MyBatis-Plus → MySQL
```

## 目录

```
personal-hub
├── personal-hub-server/   # 后端（领域模块）
├── personal-hub-web/      # 前端（modules/）
├── sql/                   # init.sql
├── docs/                  # 项目文档
└── README.md
```

## 领域模块设计

```
后端 (ph-*)                       前端 (src/modules/)
──────────────────────────────────────────────────
ph-boot       # 启动入口
ph-common     # 公共能力            —
ph-system     # 用户/认证/通知/审计  system/
ph-knowledge  # 笔记/日记/学习/      knowledge/
              # 阅读/标签/分类       category/
ph-planning   # Todo/学习计划       planning/
ph-resource   # 收藏夹/文件         resource/
ph-dashboard  # 聚合查询/全局搜索   dashboard/
              —                     search/
```

### 划分原则
- **按领域聚合，不按表划分** — 避免数据库思维
- **跨模块引用** — 通过 Service 调用，不直接依赖 Entity
- **Dashboard 仅聚合** — 不保存业务数据

## 依赖关系

```
ph-system ─┬─ ph-knowledge
           ├─ ph-planning
           ├─ ph-resource
           └─ ph-dashboard（聚合查询）
```

## 项目原则

1. 可读性优先 — 代码是人读的
2. 统一命名/结构 — 降低心智负担
3. 按领域组织 — 前后端统一领域模型
4. 不重复代码 — 公共能力沉淀到 common
5. 不随意加依赖 — 评估体积和必要性
6. 前后端命名一致 — API/模块/模型对齐
7. 接口风格统一 — RESTful + Result/PageResult
8. 数据库设计一致 — 主键/时间/逻辑删除/索引
9. 纯函数优先 — 无副作用逻辑方便测试和复用
10. 改代码先改文档
