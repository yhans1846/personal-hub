---
name: update-docs
description: 更新 Personal Hub 项目文档。当用户要求更新文档、同步进度、整理 docs、添加 changelog、或在提交前需要同步文档时触发。也用于将 CLAUDE.md 中的内容梳理到 docs/ 对应文件中。
---

# 更新项目文档

同步 `docs/` 主文档以反映最新代码与进度。  
**铁律：各司其职、不互相拷贝正文；只改受影响文件；保持精简。**

`qa/`、`docs/superpowers/` 默认不动（归档/专项），除非用户点名。

---

## 文档职责（唯一真相源）

| 文档 | 只写什么 | 不写什么 |
|------|----------|----------|
| `PROJECT.md` | 定位、模块架构、原则、路线图、**后续可扩展**、开发历史摘要 | 版本号、接口明细、表字段、页面契约 |
| `TECH_STACK.md` | 技术版本与环境最低要求 | 模块划分说明、页面/编码细则 |
| `DEPLOYMENT.md` | Ubuntu / Compose / Runner 部署运维 | 技术栈罗列、业务功能说明 |
| `DATABASE.md` | 表结构、ER、索引（对齐 `sql/init.sql`） | API 路径、前端交互 |
| `API.md` | HTTP 契约（路径/参数/认证） | 表 DDL、UI 规范 |
| `STYLE_GUIDE.md` | 编码约定、UI Token、共享组件/工具目录 | 列表铺满/槽位等页面契约 |
| `PAGE_SPEC.md` | 页面形态与交互契约（列表/预览等） | Token 色值、组件 Props 全表 |
| `CHANGELOG.md` | 按日变更摘要 | 规范正文、长篇设计说明 |
| `CLAUDE.md` | 简介 + 索引 + 流程 | docs 已有内容 |
| `README.md` | GitHub 门面：功能矩阵、快速开始、文档索引 | 详细规范正文 |

页面形态一律进 **`PAGE_SPEC.md`**（已合并原 LIST / PREVIEW SPEC），不另起 `*_PAGE_SPEC`。

---

## 工作流程

### 1. 了解当前状态

- `git log --oneline -10` 与相关 diff
- 对照上表判断「改动落在哪几个职责」
- 只打开将要改的文档（勿全量通读再抄）

### 2. 匹配变更 → 文档

| 变更类型 | 更新 |
|----------|------|
| 新/改 HTTP 接口 | `API.md` + `CHANGELOG.md` |
| 新/改表或字段 | `DATABASE.md` + **`sql/init.sql`** + `CHANGELOG.md` |
| 新共享组件 / Token / 编码约定 | `STYLE_GUIDE.md`（组件表追加一行即可） |
| 新页面形态或列表/预览契约 | `PAGE_SPEC.md` |
| 模块合并 / 架构调整 | `PROJECT.md`（架构图）+ 必要时 `TECH_STACK.md`（仅版本） |
| 依赖/运行时版本变化 | `TECH_STACK.md` |
| 部署流程变化 | `DEPLOYMENT.md` |
| 功能阶段完成 / 可扩展项 | `PROJECT.md` 路线图或 **后续可扩展** + `CHANGELOG.md` |
| 门面功能清单 / 结构 | `README.md`（保持精致） |
| 前后端模块 README | `personal-hub-server/README.md` / `personal-hub-web/readme.md`（仅模块说明变时） |

`CLAUDE.md` 若出现架构/规范细节 → **移入对应 docs**，索引表保持一行职责描述。

### 3. 怎么写（减重约定）

- **PROJECT**：路线图追加阶段行；历史用摘要表；**保留「后续可扩展」表格**（勿删成一句带过）
- **DATABASE**：紧凑字段说明 + ER；与 `init.sql` 同步，勿写 API
- **API**：路径表 + 关键请求/响应字段；枚举文案指向代码/`DATABASE`，勿贴迁移故事
- **PAGE_SPEC**：契约与 Checklist；导出细节指向 `API.md`；Token 指向 `STYLE_GUIDE`
- **STYLE_GUIDE**：组件/composable 表追加；列表 Checklist 不在此重复（指 `PAGE_SPEC`）
- **CHANGELOG**：`[Unreleased]` 下按日合并条目，一条多点；禁贴 superpowers 长路径当正文
- **TECH_STACK / DEPLOYMENT**：只更新版本或步骤，互不抄模块图/版本表

### 4. 验证

- `git diff --stat`：范围是否只含受影响文档
- 无两份主文档写同一段规范正文
- `CLAUDE.md` 仍只有索引，无规范正文
- 表结构变更时 `DATABASE.md` 与 `sql/init.sql` 一致

---

## 原则

1. **各司其职** — 上表是边界，跨界用一句话链接  
2. **精简** — 能一行不写一段；同日 CHANGELOG 可合并  
3. **只改需要的** — 未触及的文档不动  
4. **README 门面** — 高级、专业、无口语、不为凑字数  
5. **init.sql** — 有 schema 变更必须同步，注释/索引风格与现有一致  
